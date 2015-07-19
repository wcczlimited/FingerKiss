package com.weicheng.fingerkiss.NetUtil;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by weicheng on 2015/7/19.
 */
public class SurfaceUtil {
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private Bitmap icon;
    public static SurfaceUtil gSurfaceUtil;
    private  int icon_width, icon_height;
    private SurfaceUtil(){}

    public static SurfaceUtil instance(){
        return gSurfaceUtil;
    }
    public static SurfaceUtil instance(SurfaceView surfaceView, Bitmap icon){
        if(gSurfaceUtil==null) {
            gSurfaceUtil = new SurfaceUtil();
            gSurfaceUtil.mSurfaceView = surfaceView;
            gSurfaceUtil.mSurfaceHolder = surfaceView.getHolder();
            gSurfaceUtil.icon = icon;
            gSurfaceUtil.icon_width = icon.getWidth();
            gSurfaceUtil.icon_height = icon.getHeight();
        }
        return gSurfaceUtil;
    }
    public void DrawBitmap(int cx, int cy){
        Canvas canvas = mSurfaceHolder.lockCanvas();
        canvas.drawColor(Color.WHITE);
        Paint paint = new Paint();
        paint.setColor(Color.CYAN);
        paint.setAntiAlias(true);
        canvas.drawBitmap(icon,cx-icon_width/2,cy-icon_height/2,paint);
        mSurfaceHolder.unlockCanvasAndPost(canvas);
    }
}
