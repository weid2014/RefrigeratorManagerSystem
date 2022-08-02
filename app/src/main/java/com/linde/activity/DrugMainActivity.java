package com.linde.activity;


import android.content.Intent;
import android.os.Bundle;
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

import com.linde.adapter.DrugAdapter;
import com.linde.adapter.OutDrugAdapter;
import com.linde.bean.DrugBean;
import com.linde.custom.CustomActivity;
import com.linde.global.UserType;
import com.linde.presenter.DrugMainPresenter;
import com.linde.refrigeratormanagementsystem.R;
import com.linde.ui.MyDialog;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class DrugMainActivity extends CustomActivity {
//    private List<DrugBean> drugBeanList = null;
    private RecyclerView recyclerViewDrug;
    private DrugAdapter drugAdapter;
    private PopupWindow popupWindow;
//    private MyCountDownTimer myCountDownTimer;
    private boolean isFirst = true;
    private Button btnExit;
    private MyDialog myDialog;
    private TextView tvCountDownTime;
    private String userType = "OutUser";
    private String userName;
    private TextView tvUserNameMain;

    private DrugMainPresenter drugMainPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drug_main);
        drugMainPresenter=new DrugMainPresenter(this);
        initView();
    }


    /*private void initData() {
        if (getIntent() != null) {
            userType = getIntent().getStringExtra("UserType");
        }
        if (userType.equals(UserType.OutUser.toString())) {
            userName = getString(R.string.user_out);
        } else {
            userName = getString(R.string.user_in);
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
    }*/

    private void initView() {
        recyclerViewDrug = findViewById(R.id.recyclerViewDrug);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewDrug.setLayoutManager(layoutManager);
        drugAdapter = new DrugAdapter(drugMainPresenter.getDrugBeanList());
        recyclerViewDrug.setAdapter(drugAdapter);
        btnExit = findViewById(R.id.btnExit);
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //退出按键,弹出dialog
                drugMainPresenter.showDiaLog();
            }
        });
        tvUserNameMain=findViewById(R.id.tvUserNameMain);
        tvUserNameMain.setText(drugMainPresenter.getUserName());
    }

    /*private void showDiaLog() {
        myDialog = new MyDialog(this, R.style.MyDialog);
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
    }*/


   /* private void showPop() {
        View contentView = null;
        if (contentView == null) {
            contentView = LayoutInflater.from(this).inflate(R.layout.pup_tip, null);
            popupWindow = new PopupWindow(contentView, 900,
                    1200, true);
            popupWindow.setFocusable(false);
            popupWindow.setOutsideTouchable(false);
            popupWindow.setClippingEnabled(false);

        } else {
            contentView = popupWindow.getContentView();
        }
        TextView tvUserName = contentView.findViewById(R.id.tvUserName);
        tvUserName.setText(userName);
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
        View rootView = LayoutInflater.from(this).inflate(R.layout.activity_drug_main, null);
        popupWindow.showAtLocation(rootView, Gravity.CENTER, 0, 0);
        setAlpha(0.2f);
        myCountDownTimer = new MyCountDownTimer(5000, 1000);
        myCountDownTimer.start();
    }*/

    /**
     * 出库列表
     */
   /* private PopupWindow popupWindowOut;
    private MyCountDownTimerOut myCountDownTimerOut;*/

    /*private void showPopOut(String userType) {
        int status = 0;
        if (userType.equals(UserType.OutUser.toString())) {
            status = 1;
        } else {
            status = 0;
        }
        View contentView = null;
        if (contentView == null) {
            contentView = LayoutInflater.from(this).inflate(R.layout.pup_out_list, null);
            popupWindowOut = new PopupWindow(contentView, 900,
                    1200, true);
            popupWindowOut.setFocusable(false);
            popupWindowOut.setOutsideTouchable(false);
            popupWindowOut.setClippingEnabled(false);

        } else {
            contentView = popupWindowOut.getContentView();
        }
        TextView tvUserName = contentView.findViewById(R.id.tvUserName);
        tvUserName.setText(status == 0 ? "入库列表" : "出库列表");

        RecyclerView recyclerViewDrugOut = contentView.findViewById(R.id.recyclerViewDrugOut);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
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
        View rootView = LayoutInflater.from(this).inflate(R.layout.activity_drug_main, null);
        popupWindowOut.showAtLocation(rootView, Gravity.CENTER, 0, 0);
        setAlpha(0.2f);
        myCountDownTimerOut = new MyCountDownTimerOut(5000, 1000);
        myCountDownTimerOut.start();
    }*/

    /*private void setAlpha(float f) {
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        attributes.alpha = f;
        getWindow().setAttributes(attributes);
    }*/

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (isFirst && hasFocus) {
            drugMainPresenter.showPop();
            isFirst = false;
        }
    }

    /*class MyCountDownTimer extends CountDownTimer {

        public MyCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long l) {
            int progress = (int) (l / 1000);
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
            tvCountDownTime.setText(progress + "秒后将自动关闭");
        }

        @Override
        public void onFinish() {
            LogOut();
        }
    }*/

  /*  private void LogOut() {
        if (popupWindow != null) {
            popupWindow.dismiss();
            setAlpha(1.0f);
            startActivity(new Intent(DrugMainActivity.this, MainActivity.class));
            finish();
        }
    }*/
}