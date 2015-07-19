package com.weicheng.fingerkiss;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.gesture.Gesture;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.gesture.GestureOverlayView.OnGesturingListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.weicheng.fingerkiss.NetUtil.MySocket;
import com.weicheng.fingerkiss.NetUtil.FingerViewUtil;

import java.lang.reflect.Field;


public class MainActivity extends Activity implements OnGesturePerformedListener,OnGesturingListener{
    private GestureOverlayView mDrawGestureView;
    private boolean canTouch;//能不能绘制点
    private Bitmap icon;
    private int icon_width, icon_height;
    private MySocket socket;
    private FingerViewUtil mFingerViewUtil;
    private ImageView myfingerView;
    private ImageView otherfingerView;
    public static final int DRAW = 0;
    public static final int DISAPPEAR = 1;
    public static final int USERINFO = 2;
    private String myUsername;
    private String otherUsername;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        canTouch = true;

        //手势View的设置
        mDrawGestureView = (GestureOverlayView)findViewById(R.id.gestureView);
        mDrawGestureView.setGestureColor(Color.argb(0, 0, 0, 0));//设置手势颜色 透明
        mDrawGestureView.setGestureStrokeWidth(40);//设置手势粗细
        mDrawGestureView.setUncertainGestureColor(Color.argb(0, 0, 0, 0));//设置未确认为手势之前的颜色 透明
        mDrawGestureView.setGestureStrokeType(GestureOverlayView.GESTURE_STROKE_TYPE_MULTIPLE);//设置手势可多笔画绘制，默认情况为单笔画绘制

        //绑定监听器
        mDrawGestureView.addOnGesturePerformedListener(this);
        mDrawGestureView.addOnGesturingListener(this);


        //设置指纹图案极其ImageView
        icon = BitmapFactory.decodeResource(getResources(), R.mipmap.fingerprint);
        setIconSize();
        mFingerViewUtil = FingerViewUtil.instance(otherfingerView, icon);

        //设置socket 单机测试时将下面两行注释
        socket = MySocket.instance(handler);
        socket.start();

        //先确认用户名
        LayoutInflater li = LayoutInflater.from(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View Dialogview = li.inflate(R.layout.dialog_username, null);
        builder.setTitle("请根据提示输入");
        builder.setView(Dialogview);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                EditText myUsernameEdit = (EditText) Dialogview.findViewById(R.id.myUsernameEditTetxt);
                myUsername = myUsernameEdit.getText().toString();
                EditText otherUsernameEdit = (EditText) Dialogview.findViewById(R.id.otherUsernameEditText);
                otherUsername = otherUsernameEdit.getText().toString();
                if (myUsername.isEmpty() || otherUsername.isEmpty()) {
                    try {
                        //不关闭
                        Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                        field.setAccessible(true);
                        field.set(dialog, false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    dialog.dismiss();
                    socket.sendMsg(USERINFO+" "+myUsername+" "+otherUsername);
                }
            }
        });
        builder.create();
        builder.show();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case DRAW:
                    int x = msg.getData().getInt("x");
                    int y = msg.getData().getInt("y");
                    mFingerViewUtil.drawBitmap(x,y);
                    break;
                case DISAPPEAR:
                    System.out.println("disapper other finger");
                    mFingerViewUtil.disappearBitmap();
                    break;
            }
        }
    };

    private void setIconSize(){
        WindowManager wm = this.getWindowManager();
        float screenWidth = wm.getDefaultDisplay().getWidth();
        int Wwidth = (int)screenWidth/5;
        float scale = (float)icon.getWidth()/Wwidth;
        icon_width = (int)(icon.getWidth()*scale);
        icon_height = (int) (icon.getHeight() * scale);
        myfingerView = (ImageView)findViewById(R.id.myfingerView);
        otherfingerView = (ImageView)findViewById(R.id.otherfingerView);
        ViewGroup.LayoutParams param = myfingerView.getLayoutParams();
        param.width = icon_width;
        param.height = icon_height;
        myfingerView.setLayoutParams(param);
        otherfingerView.setLayoutParams(param);
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
            socket.sendMsg(DISAPPEAR+" "+event.getRawX()+" "+event.getRawY());
            canTouch = false;
            myfingerView.setVisibility(View.INVISIBLE);
            canTouch = true;
        }
        else{
            //此处描绘点的移动
            socket.sendMsg(DRAW+" "+event.getRawX()+" "+event.getRawY());
            myfingerView.setX(cx-icon_width/2);
            myfingerView.setY(cy - icon_height / 2);
            myfingerView.setVisibility(View.VISIBLE);
        }
        return super.onTouchEvent(event);
    }

    private void showMessage(String s)
    {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
        socket.sendMsg(DISAPPEAR+" 0 0");
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

        }
    }
}
