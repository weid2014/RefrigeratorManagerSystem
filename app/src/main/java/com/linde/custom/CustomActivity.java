package com.linde.custom;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class CustomActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //让虚拟键一直不显示

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow()
                .getDecorView()
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE;
        window.setAttributes(params);
    }

    private Toast toast;
    @SuppressLint("ShowToast")
    public void showTipsInfo(String info){
        if(TextUtils.isEmpty(info))
            return;
        //this.getWindow( 解决8.0系统bug，从27版本开始
        //9.0以上toast直接用原生方法即可，并不用settext防止重复显示的问题
        if(Build.VERSION.SDK_INT>=27){
            if(toast!=null){
                toast.cancel();
            }
            toast=Toast.makeText(this,info,Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER,0,0);
        }else {
            if(toast==null){
                toast=Toast.makeText(this,"",Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER,0,0);
            }
            if(toast==null)return;
            toast.setText(info);
            toast.setDuration(Toast.LENGTH_SHORT);
        }
        toast.show();
    }
}
