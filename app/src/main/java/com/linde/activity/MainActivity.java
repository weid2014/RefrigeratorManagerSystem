package com.linde.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.linde.custom.CustomActivity;
import com.linde.global.UserType;
import com.linde.presenter.DrugMainPresenter;
import com.linde.presenter.MainPresenter;
import com.linde.refrigeratormanagementsystem.R;

public class MainActivity extends CustomActivity implements View.OnClickListener{

    private ImageView imageLock;
    private ImageView imageArrow;
    private ImageView imageSwipeStatus;
    private TextView tvLockStatus1;
    private TextView tvLockStatus2;
    private boolean isLocked=true;
    private TextView btnOutUser;
    private TextView btnInUser;
    private TextView btnNoAccess;
    private MainPresenter mainPresenter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        init();
        mainPresenter=new MainPresenter(this);
    }
    private void init(){
        imageSwipeStatus=findViewById(R.id.imageSwipeStatus);
        imageSwipeStatus.setBackgroundResource(R.mipmap.icon_noswipe);

        imageArrow=findViewById(R.id.imageArrow);

        imageLock=findViewById(R.id.imageLock);
        imageLock.setOnClickListener(this);
        imageLock.setBackgroundResource(R.mipmap.icon_lock);

        tvLockStatus1=findViewById(R.id.tvLockStatus1);
        tvLockStatus2=findViewById(R.id.tvLockStatus2);
        btnOutUser=findViewById(R.id.btnOutUser);
        btnInUser=findViewById(R.id.btnInUser);
        btnNoAccess=findViewById(R.id.btnNoAccess);
        btnOutUser.setOnClickListener(this);
        btnInUser.setOnClickListener(this);
        btnNoAccess.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.imageLock:
                openLock(UserType.OutUser);
                break;
            case R.id.btnOutUser:
                openLock(UserType.OutUser);
                break;
            case R.id.btnInUser:
                openLock(UserType.InUser);
                break;
            case R.id.btnNoAccess:
                mainPresenter.showCanNotAccess();
                break;
            default:
                break;
        }
    }

    private void openLock(UserType type){
        if(isLocked){
            imageLock.setBackgroundResource(R.mipmap.icon_nolock);
            imageSwipeStatus.setBackgroundResource(R.mipmap.icon_swipe);
            imageArrow.setVisibility(View.INVISIBLE);
            tvLockStatus1.setText(getString(R.string.unlock_tip));
            tvLockStatus2.setText("");
            isLocked=!isLocked;
            jumpToIdentity(type);
        }else {
            imageLock.setBackgroundResource(R.mipmap.icon_lock);
            imageSwipeStatus.setBackgroundResource(R.mipmap.icon_noswipe);
            imageArrow.setVisibility(View.VISIBLE);
            tvLockStatus1.setText(getString(R.string.lock_tip1));
            tvLockStatus2.setText(getString(R.string.lock_tip2));
            isLocked=!isLocked;
        }
    }

    private void jumpToIdentity(UserType type){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent=new Intent(MainActivity.this, DrugMainActivity.class);
                intent.putExtra("UserType",type.toString());
                startActivity(intent);
                finish();
            }
        },2000);
    }
}