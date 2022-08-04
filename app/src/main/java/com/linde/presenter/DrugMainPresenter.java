package com.linde.presenter;

import android.content.Intent;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.linde.activity.DrugMainActivity;
import com.linde.activity.MainActivity;
import com.linde.adapter.DrugAdapter;
import com.linde.adapter.OutDrugAdapter;
import com.linde.bean.DrugBean;
import com.linde.global.UserType;
import com.linde.refrigeratormanagementsystem.R;
import com.linde.ui.MyDialog;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import android_serialport_api.ComBean;
import android_serialport_api.MyFunc;
import android_serialport_api.SerialHelper;
import android_serialport_api.SerialPortFinder;

public class DrugMainPresenter {
    private WeakReference<DrugMainActivity> drugMainActivity=null;
    private List<DrugBean> drugBeanList = null;
    private PopupWindow popupWindow;
    private MyCountDownTimer myCountDownTimer;
    private MyDialog myDialog;
    private String userType = "OutUser";
    private String userName;
    private PopupWindow popupWindowOut;
    private MyCountDownTimerOut myCountDownTimerOut;
    private  TextView tvCountDownTime;

    SerialControl serialCom;//串口

    public DrugMainPresenter(DrugMainActivity activity){
        drugMainActivity=new WeakReference<>(activity);
        initData();
        DispQueue = new DispQueueThread();
        DispQueue.start();
    }

    private void initData() {
        if (drugMainActivity.get().getIntent() != null) {
            userType = drugMainActivity.get().getIntent().getStringExtra("UserType");
        }
        if (userType.equals(UserType.OutUser.toString())) {
            userName = drugMainActivity.get().getString(R.string.user_out);
        } else {
            userName = drugMainActivity.get().getString(R.string.user_in);
        }

        drugBeanList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            DrugBean drugBean = new DrugBean("新型冠状病毒[2019-nCoV]", "2538520", "2022-07-31 22:10:36", "2022-07-31 22:10:36", "txteFsds344jjdjjkiKPd233", 0);
            DrugBean drugBean1 = new DrugBean("新型冠状病毒[2022-nCoV]", "3321111", "2022-08-01 18:10:36", "2022-08-01 18:10:36", "aaaaaaaaaaaabbbbbbbbbccc", 1);
            DrugBean drugBean2 = new DrugBean("新型冠状病毒[2021-nCoV]", "3321112", "2022-08-01 18:10:36", "2022-08-01 18:10:36", "aaaaaaaaaaaabbbbbbbbbcdd", 1);
            DrugBean drugBean4 = new DrugBean("新型冠状病毒[2019-nCoV]", "2538521", "2022-07-31 22:10:36", "2022-07-31 22:10:36", "txteFsds344jjdjjkiKPd666", 0);
            drugBeanList.add(drugBean);
            drugBeanList.add(drugBean1);
            if (i % 2 == 0) {
                drugBeanList.add(drugBean2);
            }
            if (i % 3 == 0) {
                drugBeanList.add(drugBean4);
            }
        }
    }

    public List<DrugBean> getDrugBeanList(){
        return drugBeanList;
    }

    public String getUserName(){
        return userName;
    }

    public void showDiaLog() {
        myDialog = new MyDialog(drugMainActivity.get(), R.style.MyDialog);
        myDialog.setYesOnclickListener("确定", new MyDialog.onYesOnclickListener() {
            @Override
            public void onYesOnclick() {
                //退出并锁定
                setAlpha(1.0f);
                showPopOut(userType);
                myDialog.dismiss();

            }
        });
        myDialog.setNoOnclickListener("取消", new MyDialog.onNoOnclickListener() {
            @Override
            public void onNoClick() {
                myDialog.dismiss();
                setAlpha(1.0f);
            }
        });
        myDialog.show();
        setAlpha(0.2f);
    }

    public void showPop() {
        View contentView = null;
        if (contentView == null) {
            contentView = LayoutInflater.from(drugMainActivity.get()).inflate(R.layout.pup_tip, null);
            popupWindow = new PopupWindow(contentView, 600,
                    800, true);
            popupWindow.setFocusable(false);
            popupWindow.setOutsideTouchable(false);
            popupWindow.setClippingEnabled(false);

        } else {
            contentView = popupWindow.getContentView();
        }
        TextView tvUserName = contentView.findViewById(R.id.tvUserName);
        tvUserName.setText("你好，"+userName);
        tvCountDownTime = contentView.findViewById(R.id.tvCountDownTime);
        ImageButton btnClose = contentView.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
                setAlpha(1.0f);
                //取消定时器
                myCountDownTimer.cancel();
            }
        });
        //显示PopupWindow
        View rootView = LayoutInflater.from(drugMainActivity.get()).inflate(R.layout.activity_drug_main, null);
        popupWindow.showAtLocation(rootView, Gravity.CENTER, 0, 0);
        setAlpha(0.2f);
        myCountDownTimer = new MyCountDownTimer(5000, 1000);
        myCountDownTimer.start();
    }

    public void showPopSerialPortTest() {
        serialCom = new SerialControl();

        View contentView = null;
        if (contentView == null) {
            contentView = LayoutInflater.from(drugMainActivity.get()).inflate(R.layout.pup_serial_port, null);
            popupWindow = new PopupWindow(contentView, 600,
                    800, true);
            popupWindow.setFocusable(false);
            popupWindow.setOutsideTouchable(false);
            popupWindow.setClippingEnabled(false);

        } else {
            contentView = popupWindow.getContentView();
        }
        ImageButton btnClose = contentView.findViewById(R.id.btnClose);

        //wait wait wait


        EditText edDevicePath=contentView.findViewById(R.id.edDevicePath);
        EditText edPort=contentView.findViewById(R.id.edPort);
        EditText edMessage=contentView.findViewById(R.id.edMessage);
        Button btnConnect=contentView.findViewById(R.id.btnConnect);
        Button btnDisConnect=contentView.findViewById(R.id.btnDisConnect);
        Button btnSend=contentView.findViewById(R.id.btnSend);
        TextView tvResult=contentView.findViewById(R.id.tvResult);

        String devicePath=edDevicePath.getText().toString();
        String port=edPort.getText().toString();
        String message=edMessage.getText().toString();

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                    }
                }).start();

                drugMainActivity.get().showTipsInfo("port"+port+"devicePath="+devicePath);
                tvResult.setText("port"+port+"devicePath="+devicePath);
            }
        });
        btnDisConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //关闭串口
                drugMainActivity.get().showTipsInfo("serialPortService.close()");
            }
        });
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //发送16进制的字符串
                tvResult.setText("发送16进制的字符串");
            }
        });


        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
                setAlpha(1.0f);
                //取消定时器
                myCountDownTimer.cancel();
            }
        });
        //显示PopupWindow
        View rootView = LayoutInflater.from(drugMainActivity.get()).inflate(R.layout.activity_drug_main, null);
        popupWindow.showAtLocation(rootView, Gravity.CENTER, 0, 0);
        setAlpha(0.2f);
    }

    private void showPopOut(String userType) {
        int status = 0;
        if (userType.equals(UserType.OutUser.toString())) {
            status = 1;
        } else {
            status = 0;
        }
        View contentView = null;
        if (contentView == null) {
            contentView = LayoutInflater.from(drugMainActivity.get()).inflate(R.layout.pup_out_list, null);
            popupWindowOut = new PopupWindow(contentView, 600,
                    800, true);
            popupWindowOut.setFocusable(false);
            popupWindowOut.setOutsideTouchable(false);
            popupWindowOut.setClippingEnabled(false);

        } else {
            contentView = popupWindowOut.getContentView();
        }
        TextView tvUserName = contentView.findViewById(R.id.tvUserName);
        tvUserName.setText(status == 0 ? "入库列表" : "出库列表");

        RecyclerView recyclerViewDrugOut = contentView.findViewById(R.id.recyclerViewDrugOut);
        LinearLayoutManager layoutManager = new LinearLayoutManager(drugMainActivity.get());
        recyclerViewDrugOut.setLayoutManager(layoutManager);
        HashSet<String> tempSet = new HashSet<>();
        List<List<DrugBean>> allOutList = new ArrayList<>();
        for (DrugBean drugBean : drugBeanList) {
            if (drugBean.getDrugStatus() == status) {
                //wait wait wait
                tempSet.add(drugBean.getDrugSN());
            }
        }
        for (String s : tempSet) {
            List<DrugBean> outDrugList = new ArrayList<>();
            for (DrugBean drugBean : drugBeanList) {
                if (s.equals(drugBean.getDrugSN())) {
                    outDrugList.add(drugBean);
                }
            }
            allOutList.add(outDrugList);
        }

        OutDrugAdapter outDrugAdapter = new OutDrugAdapter(allOutList);
        recyclerViewDrugOut.setAdapter(outDrugAdapter);

        tvCountDownTime = contentView.findViewById(R.id.tvCountDownTime);
        ImageButton btnClose = contentView.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogOut();
                //取消定时器
                myCountDownTimerOut.cancel();

            }
        });
        //显示PopupWindow
        View rootView = LayoutInflater.from(drugMainActivity.get()).inflate(R.layout.activity_drug_main, null);
        popupWindowOut.showAtLocation(rootView, Gravity.CENTER, 0, 0);
        setAlpha(0.2f);
        myCountDownTimerOut = new MyCountDownTimerOut(5000, 1000);
        myCountDownTimerOut.start();
    }

    class MyCountDownTimer extends CountDownTimer {

        public MyCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long l) {
            int progress = (int) (l / 1000);
            //wait wait wait
            tvCountDownTime.setText(progress + "秒后将自动关闭");
        }

        @Override
        public void onFinish() {
            if (popupWindow != null) {
                popupWindow.dismiss();
                setAlpha(1.0f);
            }
        }
    }

    class MyCountDownTimerOut extends CountDownTimer {

        public MyCountDownTimerOut(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long l) {
            int progress = (int) (l / 1000);
            //wait wait wait
            tvCountDownTime.setText(progress + "秒后将自动关闭");
        }

        @Override
        public void onFinish() {
            LogOut();
        }
    }

    private void setAlpha(float f) {
        WindowManager.LayoutParams attributes = drugMainActivity.get().getWindow().getAttributes();
        attributes.alpha = f;
        drugMainActivity.get().getWindow().setAttributes(attributes);
    }

    private void LogOut() {
        if (popupWindow != null) {
            popupWindow.dismiss();
            setAlpha(1.0f);
            drugMainActivity.get().startActivity(new Intent(drugMainActivity.get(), MainActivity.class));
            drugMainActivity.get().finish();
        }
    }
    public void onDestroy(){
            CloseComPort(serialCom);
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
            ShowMessage(drugMainActivity.get().getString(R.string.No_read_or_write_permissions));
        } catch (IOException e) {
            ShowMessage(drugMainActivity.get().getString(R.string.Unknown_error));
        } catch (InvalidParameterException e) {
            ShowMessage(drugMainActivity.get().getString(R.string.Parameter_error));
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

                    drugMainActivity.get().runOnUiThread(new Runnable() {
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
        long cardint=0;
        long wg26_1=0;
        long wg26_2=0;
        long wg34_1=0;
        long wg34_2=0;
        try {
            sMsg.append("recv: "+ MyFunc.ByteArrToHex(ComRecData.bRec));

            if(solveRecv(ComRecData.bRec, temp)==0){    //主动刷卡的数据处理
                int len = temp[0];
                byte[] cardnum = new byte[4];
                System.arraycopy( temp,1, cardnum, 0, 4);   //只保留前面4个字节卡号

                sMsg.append("\n原始卡号："+MyFunc.ByteArrToHex(cardnum)+"\n");

                cardint = Long.parseLong(MyFunc.ByteArrToHex(cardnum, 0, 4), 16);
                sMsg.append("正码转十进制："+String.format("%010d", cardint)+"\n");

                wg26_1 = Long.parseLong(MyFunc.ByteArrToHex(cardnum,1,1), 16);
                wg26_2 = Long.parseLong(MyFunc.ByteArrToHex(cardnum,2,2), 16);
                sMsg.append("正码转韦根26："+String.format("%03d,%05d", wg26_1, wg26_2)+"\n");

                wg34_1 = Long.parseLong(MyFunc.ByteArrToHex(cardnum,0,2), 16);
                wg34_2 = Long.parseLong(MyFunc.ByteArrToHex(cardnum,2,2), 16);
                sMsg.append("正码转韦根34："+String.format("%05d,%05d", wg34_1, wg34_2)+"\n");

                MyFunc.reverseByte(cardnum);
                cardint = Long.parseLong(MyFunc.ByteArrToHex(cardnum, 0, 4), 16);
                sMsg.append("反码转十进制："+String.format("%010d", cardint)+"\n");

                wg26_1 = Long.parseLong(MyFunc.ByteArrToHex(cardnum,1,1), 16);
                wg26_2 = Long.parseLong(MyFunc.ByteArrToHex(cardnum,2,2), 16);
                sMsg.append("反码转韦根26："+String.format("%03d,%05d", wg26_1, wg26_2)+"\n");

                wg34_1 = Long.parseLong(MyFunc.ByteArrToHex(cardnum,0,2), 16);
                wg34_2 = Long.parseLong(MyFunc.ByteArrToHex(cardnum,2,2), 16);
                sMsg.append("反码转韦根34："+String.format("%05d,%05d", wg34_1, wg34_2)+"\n");

            }

            ShowMessage(sMsg.toString());

        } catch (Exception ex) {
            Log.d("lalala",ex.getMessage());
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
        drugMainActivity.get().showTipsInfo(sbMsg.toString());
    }

    //识别主动刷卡数据
    private int solveRecv(byte[] bRec, byte[] retRec) {
        int sta = -1;

        if((byte)bRec[0] == 0x02){

            byte len = bRec[1];
            if(len <= bRec.length){

                byte result = MyFunc.bccCalc(bRec, 1, len-3);
                if((byte)bRec[len-2] == (byte)result){
                    retRec[0] = (byte) (len-5);
                    System.arraycopy( bRec,3,retRec, 1,len-5);
                    sta = 0;
                }
            }
        }

        return sta;
    }
}
