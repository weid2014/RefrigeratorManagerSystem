package com.linde.presenter;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;

import com.linde.custom.CustomActivity;
import com.linde.refrigeratormanagementsystem.R;

public class MainPresenter extends PresenterBase implements IMainPresenter {

    public MainPresenter(CustomActivity activity) {
        super(activity);
    }

    @Override
    protected void setTipMsg(String msg) {
        // 没有任何tip消息
    }

    @Override
    public void showCanNotAccess() {

        if (popupWindow == null) {
            View contentView = LayoutInflater.from(customActivity).inflate(R.layout.pup_failt, null);
            popupWindow = new PopupWindow(contentView, 700,
                    1000, true);
            popupWindow.setFocusable(false);
            popupWindow.setOutsideTouchable(false);
            popupWindow.setClippingEnabled(false);

        }

        //显示PopupWindow
        View rootView = LayoutInflater.from(customActivity).inflate(R.layout.activity_main, null);
        popupWindow.showAtLocation(rootView, Gravity.CENTER, 0, 0);
        setAlpha(0.2f);
        myCountDownTimer = new MyCountDownTimer(5000, 1000);
        myCountDownTimer.start();
    }
}
