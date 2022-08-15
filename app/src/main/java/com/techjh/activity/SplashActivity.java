package com.techjh.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.techjh.custom.CustomActivity;
import com.techjh.refrigeratormanagementsystem.R;

public class SplashActivity extends CustomActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        jumpToMain();
    }

    private void jumpToMain(){

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }
        },1000);
    }
}