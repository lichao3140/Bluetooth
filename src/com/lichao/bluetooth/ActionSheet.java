package com.lichao.bluetooth;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.KeyEvent;

public class ActionSheet extends Dialog {

    public ActionSheet(Context context) {
        super(context);
    }

    public ActionSheet(Context context,int gravity) {
        super(context);
        getWindow().setGravity(gravity);
    }
    public ActionSheet(Context context,int gravity,int style) {
        super(context, style);
        getWindow().setGravity(gravity);
    }

    public void toggle() {
        if (isShowing()) {
            dismiss();
        } else {
            show();
        }
    }

    @Override
    public void show() {
        super.show();
    }
    
    public void setGravity(int gravity){
        getWindow().setGravity(gravity);
    }
    
    public void setAnimation(int animation){
        getWindow().setWindowAnimations(animation);
    }
    

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            dismiss();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}