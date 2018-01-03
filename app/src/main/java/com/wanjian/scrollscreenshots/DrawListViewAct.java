package com.wanjian.scrollscreenshots;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Created by wanjian on 2018/1/3.
 */

public class DrawListViewAct extends Activity {

    private ListView listView;
    private ViewGroup container;

    private ImageView img1;
    private ImageView img2;
    private ImageView img3;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw_listview);
        listView = findViewById(R.id.listview);
        container = findViewById(R.id.container);
        img1 = findViewById(R.id.img1);
        img2 = findViewById(R.id.img2);
        img3 = findViewById(R.id.img3);

        setAdapter();

        img1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawListView(img1);
            }
        });
        img2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawListView(img2);
            }
        });
        img3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawListViewContainer(img3);
            }
        });

    }

    private void drawListViewContainer(ImageView imageView) {
        Bitmap bitmap = Bitmap.createBitmap(container.getWidth(), container.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        container.draw(canvas);
        imageView.setImageBitmap(bitmap);
    }

    private void drawListView(ImageView imageView) {
        Bitmap bitmap = Bitmap.createBitmap(listView.getWidth(), listView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        listView.draw(canvas);
        imageView.setImageBitmap(bitmap);
    }

    private void setAdapter() {

        listView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return 30;
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
