package com.linde.activity;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.linde.custom.CustomActivity;
import com.linde.global.UserType;
import com.linde.presenter.IMainPresenter;
import com.linde.presenter.MainPresenter;
import com.linde.refrigeratormanagementsystem.R;

import java.io.ByteArrayOutputStream;

public class MainActivity extends CustomActivity implements View.OnClickListener {

    private ImageView imageLock;
    private ImageView imageArrow;
    private ImageView imageSwipeStatus;
    private TextView tvLockStatus1;
    private TextView tvLockStatus2;
    private boolean isLocked = true;
    private TextView btnOutUser;
    private TextView btnInUser;
    private TextView btnNoAccess;

    private IMainPresenter mainPresenter;

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        init();
        initNFC();

    }


    private void init() {

        mainPresenter=new MainPresenter(this);

        imageSwipeStatus = findViewById(R.id.imageSwipeStatus);
        imageSwipeStatus.setBackgroundResource(R.mipmap.icon_noswipe);

        imageArrow = findViewById(R.id.imageArrow);

        imageLock = findViewById(R.id.imageLock);
        imageLock.setOnClickListener(this);
        imageLock.setBackgroundResource(R.mipmap.icon_lock);

        tvLockStatus1 = findViewById(R.id.tvLockStatus1);
        tvLockStatus2 = findViewById(R.id.tvLockStatus2);
        btnOutUser = findViewById(R.id.btnOutUser);
        btnInUser = findViewById(R.id.btnInUser);
        btnNoAccess=findViewById(R.id.btnNoAccess);

        btnOutUser.setOnClickListener(this);
        btnInUser.setOnClickListener(this);
        btnNoAccess.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
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

    private void openLock(UserType type) {
        if (isLocked) {
            imageLock.setBackgroundResource(R.mipmap.icon_nolock);
            imageSwipeStatus.setBackgroundResource(R.mipmap.icon_swipe);
            imageArrow.setVisibility(View.INVISIBLE);
            tvLockStatus1.setText(getString(R.string.unlock_tip));
            tvLockStatus2.setText("");
            isLocked = !isLocked;
            jumpToIdentity(type);
        } else {
            imageLock.setBackgroundResource(R.mipmap.icon_lock);
            imageSwipeStatus.setBackgroundResource(R.mipmap.icon_noswipe);
            imageArrow.setVisibility(View.VISIBLE);
            tvLockStatus1.setText(getString(R.string.lock_tip1));
            tvLockStatus2.setText(getString(R.string.lock_tip2));
            isLocked = !isLocked;
        }
    }

    private void jumpToIdentity(UserType type) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this, DrugMainActivity.class);
                intent.putExtra("UserType", type.toString());
                startActivity(intent);
                finish();
            }
        }, 2000);
    }

    private void initNFC() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        Intent addFlags = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        pendingIntent = PendingIntent.getActivity(this, 0, addFlags, 0);
        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            if (nfcAdapter.isEnabled()) {
                //nfc功能打开了
                showTipsInfo("nfc功能已经打开");
                nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
            } else {
                showTipsInfo("请打开nfc功能");
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (nfcAdapter != null) {
            if (nfcAdapter.isEnabled()) {
                //nfc功能打开了
                showTipsInfo("nfc功能已经打开");
                resolveIntent(intent);
            } else {
                showTipsInfo("请打开nfc功能");
            }
        }
    }


    //十六进制的字符串转化为String
    private static String hexString = "0123456789ABCDEF";

    public static String decodeStr(String bytes) {
        if (bytes.length() != 30) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream(bytes.length() / 2);
        // 将每2位16进制整数组装成一个字节
        for (int i = 0; i < bytes.length(); i += 2)
            baos.write((hexString.indexOf(bytes.charAt(i)) << 4 | hexString.indexOf(bytes.charAt(i + 1))));
        return new String(baos.toByteArray());
    }
    private String ByteArrayToHexString(byte[] inarray) {
        int i, j, in;
        String[] hex = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A",
                "B", "C", "D", "E", "F" };
        String out = "";
        for (j = inarray.length - 1; j >= 0 ; j--) {
            in = (int) inarray[j] & 0xff;
            i = (in >> 4) & 0x0f;
            out += hex[i];
            i = in & 0x0f;
            out += hex[i];
        }
        return out;
    }

    private void resolveIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs = null;
            //处理扫描蓝牙地址的
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
                for (final NdefRecord record : msgs[0].getRecords()) {
                    byte[] src = record.getPayload();
                    if (record.getPayload().length < 36) {
                        return;
                    }
                    StringBuilder macBuilder = new StringBuilder();
                    //解析蓝牙mac地址，倒过来放的
                    char[] buffer = new char[2];
                    for (int i = 7; i > 1; i--) {
                        buffer[0] = Character.toUpperCase(Character.forDigit(
                                (src[i] >>> 4) & 0x0F, 16));
                        buffer[1] = Character.toUpperCase(Character.forDigit(src[i] & 0x0F,
                                16));
                        macBuilder.append(buffer);
                        if (i == 2) {
                            break;
                        }
                        macBuilder.append(":");
                    }
                    StringBuilder boothBuilder = new StringBuilder();
                    for (int i = 21; i < src.length; i++) {
                        buffer[0] = Character.toUpperCase(Character.forDigit(
                                (src[i] >>> 4) & 0x0F, 16));
                        buffer[1] = Character.toUpperCase(Character.forDigit(src[i] & 0x0F,
                                16));
                        boothBuilder.append(buffer);
                    }
                    String strDeviceName = decodeStr(boothBuilder.toString());
                    String strDevice = strDeviceName + "|" + macBuilder.toString();
                }
            }
        }
    }

    private void resolveIntent1(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) {
            //获取卡id这里即uid
            byte[] aa = tag.getId();
            String str = ByteArrayToHexString(aa);//
//            String UID = flipHexStr(str);
//            Toast.makeText(this,"RFID已经连接，可以开始读写 UID:" + ID,
//                    Toast.LENGTH_SHORT).show();
            /*try {
                nfcV = NfcV.get(tag);
                if(nfcV != null) {
                    nfcV.connect();//建立连接
                    util = new NfcVUtil(nfcV);//创建工具类，操作连接
                }
            } catch (Exception e) {
                e.printStackTrace();
            }*/
        }
    }
}