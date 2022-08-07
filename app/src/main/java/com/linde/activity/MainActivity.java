package com.linde.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.linde.custom.CustomActivity;
import com.linde.global.UserType;
import com.linde.presenter.IMainPresenter;
import com.linde.presenter.MainPresenter;
import com.linde.refrigeratormanagementsystem.R;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.LinkedList;
import java.util.Queue;

import android_serialport_api.ComBean;
import android_serialport_api.MyFunc;
import android_serialport_api.SerialHelper;
import android_serialport_api.SerialPortFinder;

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


    SerialControl serialCom3;//串口
    SerialControl serialCom4;//串口

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        init();
        DispQueue = new DispQueueThread();
        DispQueue.start();
        initSerialtty3();
        initSerialtty4();
    }

    private void initSerialtty3() {
        serialCom3 = new SerialControl();
        mSerialPortFinder = new SerialPortFinder();
        serialCom3.setPort("/dev/ttyS3");
        serialCom3.setBaudRate(9600);
        OpenComPort(serialCom3);
    }

    private void initSerialtty4() {
        serialCom4 = new SerialControl();
        mSerialPortFinder = new SerialPortFinder();
        serialCom4.setPort("/dev/ttyS4");
        serialCom4.setBaudRate(9600);
        OpenComPort(serialCom4);
    }


    private void init() {

        mainPresenter = new MainPresenter(this);

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
        btnNoAccess = findViewById(R.id.btnNoAccess);

        btnOutUser.setOnClickListener(this);
        btnInUser.setOnClickListener(this);
        btnNoAccess.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imageLock:
                changeUI();
                break;
            case R.id.btnOutUser:
                changeUI();
                break;
            case R.id.btnInUser:
                changeUI();
                break;
            case R.id.btnNoAccess:
                mainPresenter.showCanNotAccess();
                break;
            default:
                break;
        }
    }

    private void changeUI() {
        if (!isLocked) {
            imageLock.setBackgroundResource(R.mipmap.icon_nolock);
            imageSwipeStatus.setBackgroundResource(R.mipmap.icon_swipe);
            imageArrow.setVisibility(View.INVISIBLE);
            tvLockStatus1.setText(getString(R.string.unlock_tip));
            tvLockStatus2.setText("");
        } else {
            imageLock.setBackgroundResource(R.mipmap.icon_lock);
            imageSwipeStatus.setBackgroundResource(R.mipmap.icon_noswipe);
            imageArrow.setVisibility(View.VISIBLE);
            tvLockStatus1.setText(getString(R.string.lock_tip1));
            tvLockStatus2.setText(getString(R.string.lock_tip2));
        }
    }



    private void jumpToIdentity(UserType type) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this, DrugMainActivity.class);
                intent.putExtra("UserType", type.toString());
                startActivityForResult(intent,998);
            }
        }, 2000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==998){
            //关锁
            serialCom4.sendHex("AA55020D");
            isLocked=true;
            changeUI();
        }
    }

    @Override
    protected void onDestroy() {
        CloseComPort(serialCom3);
        CloseComPort(serialCom4);
        super.onDestroy();
    }

    DispQueueThread DispQueue;//刷新显示线程
    SerialPortFinder mSerialPortFinder;//串口设备搜索

    //----------------------------------------------------串口控制类
    private class SerialControl extends SerialHelper {
        public SerialControl() {
        }

        @Override
        protected void onDataReceived(final ComBean ComRecData) {
            DispQueue.AddQueue(ComRecData);// 线程定时刷新显示(推荐)
        }
    }

    /**
     * 关闭串口
     */
    private void CloseComPort(SerialHelper ComPort) {
        if (ComPort != null) {
            ComPort.stopSend();
            ComPort.close();
        }
    }

    //----------------------------------------------------打开串口
    private void OpenComPort(SerialHelper ComPort) {
        try {
            ComPort.open();
        } catch (SecurityException e) {
            ShowMessage(getString(R.string.No_read_or_write_permissions));
        } catch (IOException e) {
            ShowMessage(getString(R.string.Unknown_error));
        } catch (InvalidParameterException e) {
            ShowMessage(getString(R.string.Parameter_error));
        }
    }

    //----------------------------------------------------刷新显示线程
    private class DispQueueThread extends Thread {
        private Queue<ComBean> QueueList = new LinkedList<ComBean>();

        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                final ComBean ComData;
                while ((ComData = QueueList.poll()) != null) {

                    runOnUiThread(new Runnable() {
                        public void run() {
                            DispRecData(ComData);
                        }
                    });

                    try {
                        Thread.sleep(10);// 显示性能高的话，可以把此数值调小。
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        public synchronized void AddQueue(ComBean ComData) {
            QueueList.add(ComData);
        }
    }

    //----------------------------------------------------显示接收数据
    private void DispRecData(ComBean ComRecData) {
        StringBuilder sMsg = new StringBuilder();
        byte[] temp = new byte[20];
        long cardint = 0;
        long wg26_1 = 0;
        long wg26_2 = 0;
        long wg34_1 = 0;
        long wg34_2 = 0;
        try {
            sMsg.append("recv: " + MyFunc.ByteArrToHex(ComRecData.bRec));

            if (solveRecv(ComRecData.bRec, temp) == 0) {    //主动刷卡的数据处理
                int len = temp[0];
                byte[] cardnum = new byte[4];
                System.arraycopy(temp, 1, cardnum, 0, 4);   //只保留前面4个字节卡号

                sMsg.append("\n原始卡号：" + MyFunc.ByteArrToHex(cardnum) + "\n");

                cardint = Long.parseLong(MyFunc.ByteArrToHex(cardnum, 0, 4), 16);
                sMsg.append("正码转十进制：" + String.format("%010d", cardint) + "\n");

                wg26_1 = Long.parseLong(MyFunc.ByteArrToHex(cardnum, 1, 1), 16);
                wg26_2 = Long.parseLong(MyFunc.ByteArrToHex(cardnum, 2, 2), 16);
                sMsg.append("正码转韦根26：" + String.format("%03d,%05d", wg26_1, wg26_2) + "\n");

                wg34_1 = Long.parseLong(MyFunc.ByteArrToHex(cardnum, 0, 2), 16);
                wg34_2 = Long.parseLong(MyFunc.ByteArrToHex(cardnum, 2, 2), 16);
                sMsg.append("正码转韦根34：" + String.format("%05d,%05d", wg34_1, wg34_2) + "\n");

                MyFunc.reverseByte(cardnum);
                cardint = Long.parseLong(MyFunc.ByteArrToHex(cardnum, 0, 4), 16);
                sMsg.append("反码转十进制：" + String.format("%010d", cardint) + "\n");

                wg26_1 = Long.parseLong(MyFunc.ByteArrToHex(cardnum, 1, 1), 16);
                wg26_2 = Long.parseLong(MyFunc.ByteArrToHex(cardnum, 2, 2), 16);
                sMsg.append("反码转韦根26：" + String.format("%03d,%05d", wg26_1, wg26_2) + "\n");

                wg34_1 = Long.parseLong(MyFunc.ByteArrToHex(cardnum, 0, 2), 16);
                wg34_2 = Long.parseLong(MyFunc.ByteArrToHex(cardnum, 2, 2), 16);
                sMsg.append("反码转韦根34：" + String.format("%05d,%05d", wg34_1, wg34_2) + "\n");

            }

            ShowMessage(sMsg.toString());

        } catch (Exception ex) {
            Log.d("lalala", ex.getMessage());
        }
    }

    //------------------------------------------显示消息
    private void ShowMessage(String sMsg) {
        StringBuilder sbMsg = new StringBuilder();
//        sbMsg.append(editTextRecDisp.getText());
//        sbMsg.append(m_sdfDate.format(new Date()));
        sbMsg.append(sMsg);
        sbMsg.append("\r\n");
    /*    editTextRecDisp.setText(sbMsg);
        editTextRecDisp.setSelection(sbMsg.length(), sbMsg.length());*/
        showTipsInfo(sbMsg.toString());
        //使用我的手机NFC，读取到信息就开锁
        if(isLocked&&(sbMsg.toString().contains("F4")||sbMsg.toString().contains("FE")||sbMsg.toString().contains("FF"))){
            serialCom4.sendHex("AA55010D");
            isLocked=false;
            changeUI();
            jumpToIdentity(UserType.OutUser);
        }
    }

    //识别主动刷卡数据
    private int solveRecv(byte[] bRec, byte[] retRec) {
        int sta = -1;

        if ((byte) bRec[0] == 0x02) {

            byte len = bRec[1];
            if (len <= bRec.length) {

                byte result = MyFunc.bccCalc(bRec, 1, len - 3);
                if ((byte) bRec[len - 2] == (byte) result) {
                    retRec[0] = (byte) (len - 5);
                    System.arraycopy(bRec, 3, retRec, 1, len - 5);
                    sta = 0;
                }
            }
        }

        return sta;
    }
}