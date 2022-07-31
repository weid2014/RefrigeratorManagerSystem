package com.linde.base;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public abstract class RootActivity extends AppCompatActivity {
    protected Context mContext;
    protected Context mAppContext;
    private View mContentView;
    private Bundle mBundleObj;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAppContext=getApplicationContext();
        mContext=this;
        mContentView=getLayoutInflater().inflate(getLayoutRes(),null);
        setContentView(mContentView);
        init();
    }

    protected abstract int getLayoutRes();
    protected abstract void init();

    /**
     * findViewById
     * @param resId
     * @param <T>
     * @return
     */
    protected <T extends View> T $(int resId){
        return (T) findViewById(resId);
    }

    /**
     * Toast
     * @param toast
     */
    protected void showToast(String toast){
        Toast.makeText(this,toast,Toast.LENGTH_SHORT).show();
    }

    /**
     * get a bundle from reuse
     * @return
     */
    protected Bundle obtainBundle(){
        if(mBundleObj==null){
            mBundleObj=new Bundle();
        }else {
            mBundleObj.clear();
        }
        return mBundleObj;
    }

}
