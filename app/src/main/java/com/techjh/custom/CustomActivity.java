package com.techjh.custom;

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
//        closeBar();

        hideBottomUIMenu();
        hideBottomUIMenu1();
        hideNavigationBar();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow()
                .getDecorView()
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

      /*  Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE;
        window.setAttributes(params);
        hideNavigationBar();*/
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

    private void hideNavigationBar() {
        Window window = this.getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION//布局位于状态栏下方
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE//保持布局状态
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION//隐藏导航栏
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION//布局隐藏导航栏
                | View.SYSTEM_UI_FLAG_IMMERSIVE//避免某些用户交互造成系统自动清除全屏状态。
                | View.SYSTEM_UI_FLAG_FULLSCREEN;//全屏
        window.getDecorView().setSystemUiVisibility(uiOptions);
    }

    protected void hideBottomUIMenu() {
//隐藏虚拟按键，并且全屏

        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api

            View v = this.getWindow().getDecorView();

            v.setSystemUiVisibility(View.GONE);

        } else if (Build.VERSION.SDK_INT >= 19) {
            Window _window = getWindow();

            WindowManager.LayoutParams params = _window.getAttributes();

            params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE;

            _window.setAttributes(params);

        }

    }

    public static void closeBar() {
        try {
            // 需要root 权限
            Build.VERSION_CODES vc = new Build.VERSION_CODES();
            Build.VERSION vr = new Build.VERSION();
            String ProcID = "79";
            if (vr.SDK_INT >= vc.ICE_CREAM_SANDWICH) {
                ProcID = "42"; // ICS AND NEWER
            }
            // 需要root 权限
            Process proc = Runtime.getRuntime().exec(
                    new String[]{
                            "su",
                            "-c",
                            "service call activity " + ProcID
                                    + " s16 com.android.systemui"}); // WAS 79
            proc.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void hideBottomUIMenu1() {
        //隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                    | View.SYSTEM_UI_FLAG_IMMERSIVE;
            decorView.setSystemUiVisibility(uiOptions);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }
}
