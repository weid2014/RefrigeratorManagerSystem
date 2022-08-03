package com.linde.presenter;

import android.content.Intent;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.linde.activity.DrugMainActivity;
import com.linde.activity.MainActivity;

import com.linde.adapter.OutDrugAdapter;
import com.linde.bean.DrugBean;
import com.linde.global.UserType;
import com.linde.refrigeratormanagementsystem.R;
import com.linde.ui.MyDialog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class DrugMainPresenter {
    private WeakReference<DrugMainActivity> drugMainActivity = null;
    private List<DrugBean> drugBeanList = null;
    private PopupWindow popupWindow;
    private MyCountDownTimer myCountDownTimer;
    private MyDialog myDialog;
    private String userType = "OutUser";
    private String userName;
    private PopupWindow popupWindowOut;
    private MyCountDownTimerOut myCountDownTimerOut;
    private TextView tvCountDownTime;


    public DrugMainPresenter(DrugMainActivity activity) {
        drugMainActivity = new WeakReference<>(activity);
        initData();
    }

    private void initData() {
        if (drugMainActivity.get().getIntent() != null) {
            userType = drugMainActivity.get().getIntent().getStringExtra("UserType");
        }
        if (userType.equals(UserType.OutUser.toString())) {
            userName = drugMainActivity.get().getString(R.string.user_out);
        } else {
            userName = drugMainActivity.get().getString(R.string.user_in);
        }

        drugBeanList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            DrugBean drugBean = new DrugBean("新型冠状病毒[2019-nCoV]", "2538520", "2022-07-31 22:10:36", "2022-07-31 22:10:36", "txteFsds344jjdjjkiKPd233", 0);
            DrugBean drugBean1 = new DrugBean("新型冠状病毒[2022-nCoV]", "3321111", "2022-08-01 18:10:36", "2022-08-01 18:10:36", "aaaaaaaaaaaabbbbbbbbbccc", 1);
            DrugBean drugBean2 = new DrugBean("新型冠状病毒[2021-nCoV]", "3321112", "2022-08-01 18:10:36", "2022-08-01 18:10:36", "aaaaaaaaaaaabbbbbbbbbcdd", 1);
            DrugBean drugBean4 = new DrugBean("新型冠状病毒[2019-nCoV]", "2538521", "2022-07-31 22:10:36", "2022-07-31 22:10:36", "txteFsds344jjdjjkiKPd666", 0);
            drugBeanList.add(drugBean);
            drugBeanList.add(drugBean1);
            if (i % 2 == 0) {
                drugBeanList.add(drugBean2);
            }
            if (i % 3 == 0) {
                drugBeanList.add(drugBean4);
            }
        }
    }

    public List<DrugBean> getDrugBeanList() {
        return drugBeanList;
    }

    public String getUserName() {
        return userName;
    }

    public void showDiaLog() {
        myDialog = new MyDialog(drugMainActivity.get(), R.style.MyDialog);
        myDialog.setYesOnclickListener("确定", new MyDialog.onYesOnclickListener() {
            @Override
            public void onYesOnclick() {
                //退出并锁定
                setAlpha(1.0f);
                showPopOut(userType);
                myDialog.dismiss();

            }
        });
        myDialog.setNoOnclickListener("取消", new MyDialog.onNoOnclickListener() {
            @Override
            public void onNoClick() {
                myDialog.dismiss();
                setAlpha(1.0f);
            }
        });
        myDialog.show();
        setAlpha(0.2f);
    }

    public void showPop() {
        View contentView = null;
        if (contentView == null) {
            contentView = LayoutInflater.from(drugMainActivity.get()).inflate(R.layout.pup_tip, null);
            popupWindow = new PopupWindow(contentView, 600,
                    800, true);
            popupWindow.setFocusable(false);
            popupWindow.setOutsideTouchable(false);
            popupWindow.setClippingEnabled(false);

        } else {
            contentView = popupWindow.getContentView();
        }
        TextView tvUserName = contentView.findViewById(R.id.tvUserName);
        tvUserName.setText("你好，" + userName);
        tvCountDownTime = contentView.findViewById(R.id.tvCountDownTime);
        ImageButton btnClose = contentView.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
                setAlpha(1.0f);
                //取消定时器
                myCountDownTimer.cancel();
            }
        });
        //显示PopupWindow
        View rootView = LayoutInflater.from(drugMainActivity.get()).inflate(R.layout.activity_drug_main, null);
        popupWindow.showAtLocation(rootView, Gravity.CENTER, 0, 0);
        setAlpha(0.2f);
        myCountDownTimer = new MyCountDownTimer(5000, 1000);
        myCountDownTimer.start();
    }

    private void showPopOut(String userType) {
        int status = 0;
        if (userType.equals(UserType.OutUser.toString())) {
            status = 1;
        } else {
            status = 0;
        }
        View contentView = null;
        if (contentView == null) {
            contentView = LayoutInflater.from(drugMainActivity.get()).inflate(R.layout.pup_out_list, null);
            popupWindowOut = new PopupWindow(contentView, 600,
                    800, true);
            popupWindowOut.setFocusable(false);
            popupWindowOut.setOutsideTouchable(false);
            popupWindowOut.setClippingEnabled(false);

        } else {
            contentView = popupWindowOut.getContentView();
        }
        TextView tvUserName = contentView.findViewById(R.id.tvUserName);
        tvUserName.setText(status == 0 ? "入库列表" : "出库列表");

        RecyclerView recyclerViewDrugOut = contentView.findViewById(R.id.recyclerViewDrugOut);
        LinearLayoutManager layoutManager = new LinearLayoutManager(drugMainActivity.get());
        recyclerViewDrugOut.setLayoutManager(layoutManager);
        HashSet<String> tempSet = new HashSet<>();
        List<List<DrugBean>> allOutList = new ArrayList<>();
        for (DrugBean drugBean : drugBeanList) {
            if (drugBean.getDrugStatus() == status) {
                //wait wait wait
                tempSet.add(drugBean.getDrugSN());
            }
        }
        for (String s : tempSet) {
            List<DrugBean> outDrugList = new ArrayList<>();
            for (DrugBean drugBean : drugBeanList) {
                if (s.equals(drugBean.getDrugSN())) {
                    outDrugList.add(drugBean);
                }
            }
            allOutList.add(outDrugList);
        }

        OutDrugAdapter outDrugAdapter = new OutDrugAdapter(allOutList);
        recyclerViewDrugOut.setAdapter(outDrugAdapter);

        tvCountDownTime = contentView.findViewById(R.id.tvCountDownTime);
        ImageButton btnClose = contentView.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogOut();
                //取消定时器
                myCountDownTimerOut.cancel();

            }
        });
        //显示PopupWindow
        View rootView = LayoutInflater.from(drugMainActivity.get()).inflate(R.layout.activity_drug_main, null);
        popupWindowOut.showAtLocation(rootView, Gravity.CENTER, 0, 0);
        setAlpha(0.2f);
        myCountDownTimerOut = new MyCountDownTimerOut(5000, 1000);
        myCountDownTimerOut.start();
    }

    class MyCountDownTimer extends CountDownTimer {

        public MyCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long l) {
            int progress = (int) (l / 1000);
            //wait wait wait
            tvCountDownTime.setText(progress + "秒后将自动关闭");
        }

        @Override
        public void onFinish() {
            if (popupWindow != null) {
                popupWindow.dismiss();
                setAlpha(1.0f);
            }
        }
    }

    class MyCountDownTimerOut extends CountDownTimer {

        public MyCountDownTimerOut(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long l) {
            int progress = (int) (l / 1000);
            //wait wait wait
            tvCountDownTime.setText(progress + "秒后将自动关闭");
        }

        @Override
        public void onFinish() {
            LogOut();
        }
    }

    private void setAlpha(float f) {
        WindowManager.LayoutParams attributes = drugMainActivity.get().getWindow().getAttributes();
        attributes.alpha = f;
        drugMainActivity.get().getWindow().setAttributes(attributes);
    }

    private void LogOut() {
        if (popupWindow != null) {
            popupWindow.dismiss();
            setAlpha(1.0f);
            drugMainActivity.get().startActivity(new Intent(drugMainActivity.get(), MainActivity.class));
            drugMainActivity.get().finish();
        }
    }
}
