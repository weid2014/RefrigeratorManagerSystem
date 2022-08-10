package com.linde.application;

import android.app.Application;

import com.xiasuhuei321.loadingdialog.manager.StyleManager;
import com.xiasuhuei321.loadingdialog.view.LoadingDialog;

/**
 * @author wade
 * @Description:(用一句话描述)
 * @date 2022/8/10 21:46
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        StyleManager styleManager=new StyleManager();
        styleManager.Anim(false).repeatTime(0).contentSize(-1).intercept(true);
        LoadingDialog.initStyle(styleManager);
    }
}
