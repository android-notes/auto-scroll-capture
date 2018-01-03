### 跟miui一样的自动滚动截屏

> 很久之前写过一篇完全不同于其他长截屏方案的的博客，不过很仓促，现在重新整理一下  [android长截屏beta1](http://blog.csdn.net/qingchunweiliang/article/details/52248643)
 

#### miui自动滚动长截屏效果

![miui](https://github.com/android-notes/auto-scroll-capture/blob/master/miui_screen_cap.gif?raw=true)



#### 画

* 给滚动控件外面嵌套一个`FrameLayout`（`LinearLayout`等也可以）
* 手动调用`FrameLayout`的`draw`方法把`view`画到`bitmap`上

 ```java
Bitmap bitmap = Bitmap.createBitmap(container.getWidth(), container.getHeight(), Bitmap.Config.ARGB_8888);
Canvas canvas = new Canvas(bitmap);
container.draw(canvas);
 
 ```
 > 具体参考  `DrawScrollViewAct` `DrawListViewAct`
 
 
#### 滚
 
 *  通过不断改变motionEvent的y值并手动调用view的`dispatchTouchEvent`方法实现`view`滚动

```java

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


```

 > 参考 ScrollAct

自动滚动效果
![自动滚动](https://github.com/android-notes/auto-scroll-capture/blob/master/auto_scroll.gif?raw=true) 
 
#### 截屏
 
 
 * 每自动滚动完一屏幕调用`view.draw()`把`view`画到`bitmap`上，最后拼接bitmap

 > 参考 `AutoScreenShotsAct`
 
 
 #### 为什么要嵌套一层view
 
 listview不嵌套时，不管是否滚动，都能得到正确的结果
 
 ![listview](https://github.com/android-notes/auto-scroll-capture/blob/master/listview_capture.jpg?raw=true)
 
 
 但scrollview滚动后即使顶部的已经看不到了，但调用scrollview的draw时还是会把scrollview不可见的地方画进去
 ![scrollview](https://github.com/android-notes/auto-scroll-capture/blob/master/scrollview_capture.jpg?raw=true)
 
 为了通用起间，我们给view外面嵌套了一层view
 
 #### 代码实现
 
 关键逻辑如下，但有些细节还需要具体对待
 
 ```java
 
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
        //如果你需要透明度或者对图片质量要求很高的话请使用Config.ARGB_8888
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

 
 ```
 #### 最终效果
 
 左边是自动滚动的Listview，右边是停止截屏后的bitmap，可以看到完全没有拼接痕迹
 
 
 ![效果](https://github.com/android-notes/auto-scroll-capture/blob/master/auto_cap_demo.gif?raw=true)
 
 
 
 
 ```txt
 MIT License

Copyright (c) 2018 wanjian

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

 
 ```
 
