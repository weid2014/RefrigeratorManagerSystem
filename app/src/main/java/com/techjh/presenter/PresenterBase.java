package com.techjh.presenter;



import android.os.CountDownTimer;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.techjh.custom.CustomActivity;

public abstract class PresenterBase {
    protected CustomActivity customActivity=null;

    protected PopupWindow popupWindow;
    protected MyCountDownTimer myCountDownTimer;

    protected PresenterBase(CustomActivity activity){
        this.customActivity=activity;
    }

    protected void setAlpha(float f) {
        WindowManager.LayoutParams attributes = customActivity.getWindow().getAttributes();
        attributes.alpha = f;
        customActivity.getWindow().setAttributes(attributes);
    }

    /**
     * 设置提示西悉尼
     */
    protected  abstract  void setTipMsg(String msg);

     class MyCountDownTimer extends CountDownTimer {

        public MyCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long l) {
            int progress = (int) (l / 1000);
            //wait wait wait
            setTipMsg(progress + "秒后将自动关闭");
        }

        @Override
        public void onFinish() {
            if (popupWindow != null) {
                popupWindow.dismiss();
                setAlpha(1.0f);
            }
        }
    }

}
