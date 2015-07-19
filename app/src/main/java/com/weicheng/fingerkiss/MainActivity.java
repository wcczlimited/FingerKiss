package com.weicheng.fingerkiss;

import android.app.Activity;
import android.gesture.Gesture;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.gesture.GestureOverlayView.OnGesturingListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import com.weicheng.fingerkiss.NetUtil.MySocket;

import java.net.Socket;


public class MainActivity extends Activity implements OnGesturePerformedListener,OnGesturingListener{
    private GestureOverlayView mDrawGestureView;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;   //surfaceView的 控制器
    private Paint paint;
    private boolean canTouch;//能不能绘制点
    Bitmap icon;
    int icon_width, icon_height;
    MySocket socket;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        socket = new MySocket();
        new Thread(socket).start();

        canTouch = true;
        //设置canvas上绘制用的图案
        icon = BitmapFactory.decodeResource(getResources(), R.mipmap.fingerprint);
        icon_width = icon.getWidth();
        icon_height = icon.getHeight();
        //手势View的设置
        mDrawGestureView = (GestureOverlayView)findViewById(R.id.gestureView);
        mDrawGestureView.setGestureColor(Color.argb(255,0,0,0));//设置手势颜色 透明
        mDrawGestureView.setGestureStrokeWidth(40);//设置手势粗细
        mDrawGestureView.setUncertainGestureColor(Color.argb(255, 0, 0, 0));//设置未确认为手势之前的颜色 透明
        mDrawGestureView.setGestureStrokeType(GestureOverlayView.GESTURE_STROKE_TYPE_MULTIPLE);//设置手势可多笔画绘制，默认情况为单笔画绘制

        //绑定监听器
        mDrawGestureView.addOnGesturePerformedListener(this);
        mDrawGestureView.addOnGesturingListener(this);

        //SurfaceView的设置
        mSurfaceView = (SurfaceView)findViewById(R.id.surfaceView);
        mSurfaceHolder = mSurfaceView.getHolder();
        paint = new Paint();
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                // 锁定整个SurfaceView
                Canvas canvas = holder.lockCanvas();
                paint.setColor(Color.WHITE);
                canvas.drawColor(Color.WHITE);
                // 绘制完成，释放画布，提交修改
                holder.unlockCanvasAndPost(canvas);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }
        });// 自动运行surfaceCreated以及surfaceChanged
    }
    //手动调用onTouchEvent，以防GestureOverlayView截获之后不返回
    @Override
    public boolean dispatchTouchEvent(MotionEvent e) {
        this.onTouchEvent(e);
        return super.dispatchTouchEvent(e);
    }
    //更新每个点的轨迹
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int cx = (int) event.getX();
        int cy = (int) event.getY();
        if(canTouch==false)
            return true;
        if(event.getActionMasked() == MotionEvent.ACTION_UP){
            canTouch = false;
            new Thread(new CircleDisappear(cx, cy)).start();
        }
        else{
            Canvas canvas = mSurfaceHolder.lockCanvas();
            canvas.drawColor(Color.WHITE);
            paint.setColor(Color.CYAN);
            paint.setAntiAlias(true);

            canvas.drawBitmap(icon,cx-icon_width/2,cy-icon_height/2,paint);
            mSurfaceHolder.unlockCanvasAndPost(canvas);
        }
        return super.onTouchEvent(event);
    }

    private void showMessage(String s)
    {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
        showMessage("手势绘制完成");
    }

    @Override
    public void onGesturingStarted(GestureOverlayView overlay) {
        //showMessage("正在绘制手势");
    }

    @Override
    public void onGesturingEnded(GestureOverlayView overlay) {
        //showMessage("结束正在绘制手势");
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        //移除绑定的监听器
        mDrawGestureView.removeOnGesturePerformedListener(this);
        mDrawGestureView.removeOnGesturingListener(this);
    }

    private class CircleDisappear implements Runnable {
        private int cx;
        private int cy;
        public CircleDisappear(int x, int y){
            this.cx = x;
            this.cy = y;
        }
        @Override
        public void run() {
            Canvas canvas = null;
            int alpha = 255;
            while (alpha>=0) {
                try {
                    canvas = mSurfaceHolder.lockCanvas();
                    canvas.drawColor(Color.WHITE);
                    paint.setColor(Color.CYAN);
                    paint.setAntiAlias(true);
                    paint.setAlpha(alpha);
                    paint.setAntiAlias(true);

                    canvas.drawBitmap(icon, cx - icon_width / 2, cy - icon_height/2,paint);
                    //canvas.drawCircle(cx, cy, 50, paint);
                    mSurfaceHolder.unlockCanvasAndPost(canvas);// 解锁画布，提交画好的图像
                    Thread.sleep(10);
                    alpha-=15;
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            canTouch = true;
        }

    }
}
