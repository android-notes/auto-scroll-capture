### 跟miui一样的自动滚动截屏

>> 很久之前写过一篇长截屏的博客，不过很仓促，现在重新整理一下，绝对是你从没见过的长截屏方式  [android长截屏beta1](http://blog.csdn.net/qingchunweiliang/article/details/52248643)
 

#### 画

* 给滚动控件外面嵌套一个`FrameLayout`（`LinearLayout`等也可以）
* 手动调用`FrameLayout`的`draw`方法把`view`画到`bitmap`上

 ```java
   Bitmap bitmap = Bitmap.createBitmap(container.getWidth(), container.getHeight(), Bitmap.Config.ARGB_8888);
Canvas canvas = new Canvas(bitmap);
container.draw(canvas);
 
 ```
 >> 具体参考  `DrawScrollViewAct` `DrawListViewAct`
 
 
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

 >> 参考 ScrollAct

自动滚动效果
![自动滚动](https://github.com/android-notes/auto-scroll-capture/blob/master/auto_scroll.gif?raw=true) 
 
#### 截屏
 
 
 * 每自动滚动完一屏幕调用`view.draw()`把`view`画到`bitmap`上，最后拼接bitmap

 >> 参考 `AutoScreenShotsAct`
 
 
 #### 为什么要嵌套一层view
 
 listview不嵌套时，不管是否滚动，都能得到正确的结果
 
 ![listview](https://github.com/android-notes/auto-scroll-capture/blob/master/listview_capture.png?raw=true)
 
 
 但scrollview滚动后即使顶部的已经看不到了，但调用scrollview的draw时还是会把scrollview不可见的地方画进去
 ![scrollview](https://github.com/android-notes/auto-scroll-capture/blob/master/scrollview_capture.png?raw=true)
 
 为了通用起间，我们给view外面嵌套了一层view
 
 
 #### 最终效果
 ![效果](https://github.com/android-notes/auto-scroll-capture/blob/master/auto_cap_demo.gif?raw=true)
 
 
 
 