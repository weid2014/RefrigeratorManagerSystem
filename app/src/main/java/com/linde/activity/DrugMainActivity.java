package com.linde.activity;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.linde.adapter.DrugAdapter;
import com.linde.custom.CustomActivity;
import com.linde.presenter.DrugMainPresenter;
import com.linde.refrigeratormanagementsystem.R;

public class DrugMainActivity extends CustomActivity {
    private RecyclerView recyclerViewDrug;
    private DrugAdapter drugAdapter;
    private boolean isFirst = true;
    private Button btnExit;
    private TextView tvUserNameMain;

    private DrugMainPresenter drugMainPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drug_main);
        drugMainPresenter = new DrugMainPresenter(this);
        initView();
/*//获取所有串口地址
        String[] devices = new SerialPortFinder().getDevices();
        //打开串口，设置读取返回时间
        SerialPortService serialPortService=new SerialPortBuilder().setTimeOut(100L).setBaudrate(9600).setDevicePath("dev/ttys4").createService();
        //发送指令
        //发送byte数组
        byte[] receiveData=serialPortService.sendData(new byte[2]);
        //发送16进制的字符串
        byte[] receiveData64=serialPortService.sendData("55AA0101010002");
        showTipsInfo(ByteStringUtil.byteArrayToHexStr(receiveData64));
        //打开或者关闭日志，默认关闭
        serialPortService.isOutputLog(true);
        //关闭串口
        serialPortService.close();*/
    }


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
        tvUserNameMain = findViewById(R.id.tvUserNameMain);
        tvUserNameMain.setText(drugMainPresenter.getUserName());
        tvUserNameMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drugMainPresenter.showPopSerialPortTest();
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (isFirst && hasFocus) {
            drugMainPresenter.showPop();
            isFirst = false;
        }
    }

}