package com.linde.activity;


import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.linde.adapter.DrugAdapter;
import com.linde.bean.DrugBean;
import com.linde.custom.CustomActivity;
import com.linde.refrigeratormanagementsystem.R;

import java.util.ArrayList;
import java.util.List;

public class DrugMainActivity extends CustomActivity {
    private List<DrugBean> drugBeanList=null;
    private RecyclerView recyclerViewDrug;
    private DrugAdapter drugAdapter;
    private PopupWindow popupWindow;
    private MyCountDownTimer myCountDownTimer;
    private boolean isFirst=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drug_main);
        initData();
        init();

    }

    private void initData(){
        drugBeanList=new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            DrugBean drugBean=new DrugBean("新型冠状病毒[2019-nCoV]","2538520","2022-07-31 22:10:36","txteFsds344jjdjjkiKPd233");
            DrugBean drugBean1=new DrugBean("新型冠状病毒[2022-nCoV]","3321111","2022-08-01 18:10:36","aaaaaaaaaaaabbbbbbbbbccc");
            drugBeanList.add(drugBean);
            drugBeanList.add(drugBean1);
        }
    }

    private void init(){
        recyclerViewDrug=findViewById(R.id.recyclerViewDrug);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewDrug.setLayoutManager(layoutManager);
        drugAdapter=new DrugAdapter(drugBeanList);
        recyclerViewDrug.setAdapter(drugAdapter);

    }

    private TextView tvCountDownTime;
    private void showPop(){
        View contentView=null;
        if(contentView==null){
            contentView= LayoutInflater.from(this).inflate(R.layout.pup_tip,null);
            popupWindow=new PopupWindow(contentView, 640,
                    760,true);
            popupWindow.setFocusable(false);
            popupWindow.setOutsideTouchable(false);
            popupWindow.setClippingEnabled(false);

        }else{
            contentView=popupWindow.getContentView();
        }
        TextView tvUserName=contentView.findViewById(R.id.tvUserName);
        tvCountDownTime=contentView.findViewById(R.id.tvCountDownTime);
        ImageButton btnClose=contentView.findViewById(R.id.btnClose);
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
        View rootView=LayoutInflater.from(this).inflate(R.layout.activity_drug_main,null);
        popupWindow.showAtLocation(rootView, Gravity.CENTER,0,0);
        setAlpha(0.2f);
        myCountDownTimer=new MyCountDownTimer(5000,1000);
        myCountDownTimer.start();
    }

    private void setAlpha(float f){
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        attributes.alpha=f;
        getWindow().setAttributes(attributes);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(isFirst&&hasFocus){
            showPop();
            isFirst=false;
        }
    }

    class MyCountDownTimer extends CountDownTimer {

        public MyCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long l) {
            int progress = (int) (l/1000);
            tvCountDownTime.setText(progress+"秒后将自动关闭");
        }

        @Override
        public void onFinish() {
            if(popupWindow!=null) {
                popupWindow.dismiss();
                setAlpha(1.0f);
            }
        }
    }
}