package com.linde.activity;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.linde.adapter.DrugAdapter;
import com.linde.custom.CustomActivity;
import com.linde.presenter.DrugMainPresenter;
import com.linde.presenter.IDrugMainPresenter;
import com.linde.refrigeratormanagementsystem.R;

public class DrugMainActivity extends CustomActivity {
    private RecyclerView recyclerViewDrug;
    private DrugAdapter drugAdapter;
    private boolean isFirst = true;
    private Button btnExit;
    private TextView tvUserNameMain;

    private IDrugMainPresenter drugMainPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drug_main);
        drugMainPresenter = new DrugMainPresenter(this);
        initView();

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
//                startActivity(new Intent(DrugMainActivity.this,SerialActivity.class));
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