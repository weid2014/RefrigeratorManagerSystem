package com.linde.base;

import android.content.Context;

public abstract class BasePresenter<AttachView extends IView> {
    private Context mContext;
    private AttachView mView;

    public BasePresenter(Context context,AttachView attachView){
        if(context==null){
            throw new NullPointerException("context==null");
        }
        mContext=context.getApplicationContext();
        mView=attachView;
    }

    /**
     * 获取关联的View
     */
    public AttachView getAttachView(){
        if(mView==null){
            throw new NullPointerException("AttachView is null" );
        }
        return mView;
    }
    /**
     * 获取关联的Context
     */
    public Context getContext(){
        return mContext;
    }
    /**
     * View 是否关联
     */
    public  boolean isViewAttached(){
        return mView!=null;
    }

    public abstract void start();
    public abstract void destroy();
    public abstract void clearPresenter();
}
