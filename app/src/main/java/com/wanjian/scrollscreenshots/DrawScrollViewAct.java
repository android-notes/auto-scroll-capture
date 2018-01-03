package com.wanjian.scrollscreenshots;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Created by wanjian on 2018/1/3.
 */

public class DrawScrollViewAct extends Activity {

    private ScrollView scrollView;
    private ViewGroup container;

    private ImageView img1;
    private ImageView img2;
    private ImageView img3;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw_scrollview);
        scrollView = findViewById(R.id.scrollView);
        container = findViewById(R.id.container);
        img1 = findViewById(R.id.img1);
        img2 = findViewById(R.id.img2);
        img3 = findViewById(R.id.img3);

        ViewGroup itemContainer = findViewById(R.id.itemContainer);
        for (int i = 0; i < 20; i++) {
            View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.item, itemContainer, false);
            ((TextView) view.findViewById(R.id.index)).setText("" + i);
            itemContainer.addView(view);
        }

        img1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawScrollView(img1);
            }
        });
        img2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawScrollView(img2);
            }
        });
        img3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawScrollViewContainer(img3);
            }
        });

    }

    private void drawScrollViewContainer(ImageView imageView) {
        Bitmap bitmap = Bitmap.createBitmap(container.getWidth(), container.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        container.draw(canvas);
        imageView.setImageBitmap(bitmap);
    }

    private void drawScrollView(ImageView imageView) {
        Bitmap bitmap = Bitmap.createBitmap(scrollView.getWidth(), scrollView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        scrollView.draw(canvas);
        imageView.setImageBitmap(bitmap);
    }

}
