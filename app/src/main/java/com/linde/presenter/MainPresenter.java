package com.linde.presenter;

import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.linde.activity.MainActivity;
import com.linde.bean.DrugBean;
import com.linde.custom.CustomActivity;
import com.linde.refrigeratormanagementsystem.R;


import java.lang.ref.WeakReference;
import java.util.List;

public class MainPresenter {
    private WeakReference<CustomActivity> mainActivity=null;
    private List<DrugBean> drugBeanList = null;
    private PopupWindow popupWindow;
    private MainPresenter.MyCountDownTimer myCountDownTimer;
    private DrugMainPresenter.MyCountDownTimerOut myCountDownTimerOut;

    public MainPresenter(CustomActivity activity){
        mainActivity=new WeakReference<>(activity);
    }
    public void showCanNotAccess(){
        View contentView = null;
        if (contentView == null) {
            contentView = LayoutInflater.from(mainActivity.get()).inflate(R.layout.pup_failt, null);
            popupWindow = new PopupWindow(contentView, 900,
                    1200, true);
            popupWindow.setFocusable(false);
            popupWindow.setOutsideTouchable(false);
            popupWindow.setClippingEnabled(false);

        } else {
            contentView = popupWindow.getContentView();
        }

        //显示PopupWindow
        View rootView = LayoutInflater.from(mainActivity.get()).inflate(R.layout.activity_main, null);
        popupWindow.showAtLocation(rootView, Gravity.CENTER, 0, 0);
        setAlpha(0.2f);
        myCountDownTimer = new MainPresenter.MyCountDownTimer(5000, 1000);
        myCountDownTimer.start();
    }
    class MyCountDownTimer extends CountDownTimer {

        public MyCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long l) {
            int progress = (int) (l / 1000);
        }

        @Override
        public void onFinish() {
            if (popupWindow != null) {
                popupWindow.dismiss();
                setAlpha(1.0f);
            }
        }
    }

    private void setAlpha(float f) {
        WindowManager.LayoutParams attributes = mainActivity.get().getWindow().getAttributes();
        attributes.alpha = f;
        mainActivity.get().getWindow().setAttributes(attributes);
    }
}
