package com.weicheng.fingerkiss.NetUtil;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by weicheng on 2015/7/19.
 */
public class FingerViewUtil {
    private ImageView otherfingerView;
    private Bitmap icon;
    public static FingerViewUtil gfingerViewUtil;
    private  int icon_width, icon_height;
    private FingerViewUtil(){}

    public static FingerViewUtil instance(){
        return gfingerViewUtil;
    }
    public static FingerViewUtil instance(ImageView otherfingerView, Bitmap icon){
        if(gfingerViewUtil==null) {
            gfingerViewUtil = new FingerViewUtil();
            gfingerViewUtil.otherfingerView = otherfingerView;
            gfingerViewUtil.icon = icon;
            gfingerViewUtil.icon_width = otherfingerView.getLayoutParams().width;
            gfingerViewUtil.icon_height = otherfingerView.getLayoutParams().height;
        }
        return gfingerViewUtil;
    }
    public void drawBitmap(int cx, int cy){
        otherfingerView.setX(cx - icon_width/2);
        otherfingerView.setY(cy - icon_height/2);
        otherfingerView.setVisibility(View.VISIBLE);
    }

    public void disappearBitmap(){
        otherfingerView.setVisibility(View.INVISIBLE);
    }
}
