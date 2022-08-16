package com.techjh.base;

import android.os.Bundle;
import android.os.PersistableBundle;

import androidx.annotation.NonNull;

public abstract class MVPCompatActivity <T extends BasePresenter> extends RootActivity{
    protected T mPresenter;
    /**
     * 创建一个Presenter
     * @return
     */
    protected abstract T createPresenter();
    @Override
    protected void onStart() {
        super.onStart();
        if(mPresenter==null){
            mPresenter=createPresenter();
        }
        mPresenter.start();
    }


    @Override
    protected void onStop() {
        super.onStop();
        mPresenter.clearPresenter();
        mPresenter=null;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        mPresenter.clearPresenter();
        mPresenter=null;
    }
}
