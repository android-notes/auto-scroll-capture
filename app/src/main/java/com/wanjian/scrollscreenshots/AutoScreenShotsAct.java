package com.wanjian.scrollscreenshots;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wanjian on 2018/1/3.
 */

public class AutoScreenShotsAct extends Activity {
    private ListView listView;

    private ImageView img;

    private ViewGroup container;

    private List<Bitmap> bitmaps = new ArrayList<>();

    private boolean isScreenShots = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_scroll_demo);
        listView = findViewById(R.id.listview);
        img = findViewById(R.id.img);
        container = findViewById(R.id.container);

        setAdapter();

        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bitmaps.clear();
                img.setImageBitmap(null);
                isScreenShots = true;
                autoScroll();
            }
        });
        findViewById(R.id.stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isScreenShots = false;

            }
        });
    }

    private void autoScroll() {
        final int delay = 16;//延时16毫秒分发滑动事件
        final int step = 10;//每次滑动距离5像素，可以根据需要调整（若卡顿的话实际滚动距离可能小于5）
        final MotionEvent motionEvent = MotionEvent.obtain(SystemClock.uptimeMillis()
                , SystemClock.uptimeMillis()
                , MotionEvent.ACTION_DOWN
                , listView.getWidth() / 2
                , listView.getHeight() / 2
                , 0);
        //先分发 MotionEvent.ACTION_DOWN 事件，我们指定为按下位置是listview的中间位置，当然其他位置也可以
        listView.dispatchTouchEvent(motionEvent);
        /*
        注意：
        查看Listview源码可知 滑动距离大于ViewConfiguration.get(view.getContext()).getScaledTouchSlop()时listview才开始滚动
         private boolean startScrollIfNeeded(int x, int y, MotionEvent vtev) {
            // Check if we have moved far enough that it looks more like a
            // scroll than a tap
            final int deltaY = y - mMotionY;
            final int distance = Math.abs(deltaY);
            final boolean overscroll = mScrollY != 0;
            if ((overscroll || distance > mTouchSlop) && (getNestedScrollAxes() & SCROLL_AXIS_VERTICAL) == 0) {
                ...
                return true;
            }
            return false;
        }
         */
        motionEvent.setAction(MotionEvent.ACTION_MOVE);
        motionEvent.setLocation(motionEvent.getX(), motionEvent.getY() - (ViewConfiguration.get(listView.getContext()).getScaledTouchSlop()));
        listView.dispatchTouchEvent(motionEvent);

        final int startScrollY = (int) motionEvent.getY();

        listView.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isScreenShots == false) {//注意：我们无法通过滚动距离来判断是否滚动到了最后，所以需要通过其他方式停止滚动
                    drawRemainAndAssemble(startScrollY, (int) motionEvent.getY());
                    return;
                }
                //滚动刚好一整屏时画到bitmap上
                drawIfNeeded(startScrollY, (int) motionEvent.getY());

                motionEvent.setAction(MotionEvent.ACTION_MOVE); //延时分发 MotionEvent.ACTION_MOVE 事件
                /*
                  改变motionEvent的y坐标，nextStep越大滚动越快，但太大可能会导致掉帧，导致实际滚动距离小于我们滑动的距离

                  因为我们是根据(curScrollY - startScrollY) % container.getHeight() == 0来判定是否刚好滚动了一屏幕的，
                  所以快要滚动到刚好一屏幕位置时，修改nextStep的值，使下次滚动后刚好是一屏幕的距离。
                  当然nextStep也可以一直是1，这时就不需要凑整了，但这样会导致滚动的特别慢
                 */
                int nextStep;
                int gap = (startScrollY - (int) motionEvent.getY() + step) % container.getHeight();
                if (gap > 0 && gap < step) {
                    nextStep = step - gap;
                } else {
                    nextStep = step;
                }

                motionEvent.setLocation((int) motionEvent.getX(), (int) motionEvent.getY() - nextStep);
                listView.dispatchTouchEvent(motionEvent);

                listView.postDelayed(this, delay);
            }
        }, delay);
    }

    private void drawRemainAndAssemble(int startScrollY, int curScrollY) {
        //最后的可能不足一屏幕，需要单独处理
        if ((curScrollY - startScrollY) % container.getHeight() != 0) {
            Bitmap film = Bitmap.createBitmap(container.getWidth(), container.getHeight(), Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas();
            canvas.setBitmap(film);
            container.draw(canvas);

            int part = (startScrollY - curScrollY) / container.getHeight();
            int remainHeight = startScrollY - curScrollY - container.getHeight() * part;
            Bitmap remainBmp = Bitmap.createBitmap(film, 0, container.getHeight() - remainHeight, container.getWidth(), remainHeight);
            bitmaps.add(remainBmp);
        }

        assembleBmp();

    }

    private void assembleBmp() {
        int h = 0;
        for (Bitmap bitmap : bitmaps) {
            h += bitmap.getHeight();
        }
        Bitmap bitmap = Bitmap.createBitmap(container.getWidth(), h, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        for (Bitmap b : bitmaps) {
            canvas.drawBitmap(b, 0, 0, null);
            canvas.translate(0, b.getHeight());
        }
        ViewGroup.LayoutParams params = img.getLayoutParams();
        params.width = bitmap.getWidth() * 2;
        params.height = bitmap.getHeight() * 2;
        img.requestLayout();
        img.setImageBitmap(bitmap);
    }

    private void drawIfNeeded(int startScrollY, int curScrollY) {

        if ((curScrollY - startScrollY) % container.getHeight() == 0) {
            //正好滚动满一屏

            //为了更通用，我们是把ListView的父布局（和ListView宽高相同）画到了bitmap上
            Bitmap film = Bitmap.createBitmap(container.getWidth(), container.getHeight(), Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas();
            canvas.setBitmap(film);
            container.draw(canvas);
            bitmaps.add(film);
        }
    }

    private void setAdapter() {

        listView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return 100;
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.item, listView, false);
                    convertView.setTag(R.id.index, convertView.findViewById(R.id.index));
                }
                ((TextView) convertView.getTag(R.id.index)).setText(position + "");
                return convertView;
            }
        });

    }
}
