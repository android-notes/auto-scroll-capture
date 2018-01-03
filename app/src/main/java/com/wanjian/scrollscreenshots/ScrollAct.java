package com.wanjian.scrollscreenshots;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Created by wanjian on 2018/1/3.
 */

public class ScrollAct extends Activity {

    private ListView listView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_scroll);
        listView = findViewById(R.id.listview);

        setAdapter();

        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoScroll();
            }
        });
    }

    private void autoScroll() {
        final int delay = 16;
        final MotionEvent motionEvent = MotionEvent.obtain(SystemClock.uptimeMillis()
                , SystemClock.uptimeMillis()
                , MotionEvent.ACTION_DOWN
                , listView.getWidth() / 2
                , listView.getHeight() / 2
                , 0);
        listView.dispatchTouchEvent(motionEvent);//先分发 MotionEvent.ACTION_DOWN 事件

        listView.postDelayed(new Runnable() {
            @Override
            public void run() {
                motionEvent.setAction(MotionEvent.ACTION_MOVE); //延时分发 MotionEvent.ACTION_MOVE 事件
                //改变y坐标，越大滚动越快，但太大可能会导致掉帧
                motionEvent.setLocation((int) motionEvent.getX(), (int) motionEvent.getY() - 10);
                listView.dispatchTouchEvent(motionEvent);
                listView.postDelayed(this, delay);
            }
        }, delay);
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
