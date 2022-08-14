package com.linde.activity;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.linde.adapter.DrugAdapter;
import com.linde.bean.DrugBean;
import com.linde.custom.CustomActivity;
import com.linde.global.GlobalData;
import com.linde.presenter.DrugMainPresenter;
import com.linde.presenter.IDrugMainPresenter;
import com.linde.refrigeratormanagementsystem.R;
import com.linde.trans2000.ReadTag;
import com.linde.trans2000.TagCallback;
import com.linde.trans2000.UHFLib;
import com.linde.ui.MyDialog;
import com.xiasuhuei321.loadingdialog.view.LoadingDialog;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import android_serialport_api.ComBean;
import android_serialport_api.MyFunc;
import android_serialport_api.SerialHelper;
import android_serialport_api.SerialPortFinder;

public class DrugMainActivity extends CustomActivity {
    private RecyclerView recyclerViewDrug;
    private DrugAdapter drugAdapter;
    private boolean isFirst = true;
    private boolean isLockAndExit = false;//锁定并退出

    private IDrugMainPresenter drugMainPresenter;
    private TextView tvUserNameMain;
    private static final int MSG_UPDATE_LISTVIEW = 0;
    private static final int MSG_UPDATE_INFO = 1;
    private static final int MSG_UPDATE_FAIL = 2;
    private long Number = 0;
    private Thread mThread;
    private Thread mTimeOutThread;
    private int Count = 0;
    private boolean isScan = true;
    private int connectCount = 0;//连接次数
    private static ArrayList<HashMap<String, String>> mCurIvtClist;
    private static ArrayList<HashMap<String, String>> mFirstCurIvtClist;//第一次获取的列表
    private static ArrayList<HashMap<String, String>> mExitCurIvtClist;//退出时候获取的列表
    private static ArrayList<HashMap<String, String>> mlastIvtClist;
    private static ArrayList<HashMap<String, String>> mnewIvtClist;
    private static ArrayList<HashMap<String, String>> mIvtInfolist;
    private List<DrugBean> drugBeanList = null;
    private List<DrugBean> drugInBeanList = null;
    private List<DrugBean> drugOutBeanList = null;
    private LoadingDialog scanloadingDialog;
    SerialControl serialCom4;//串口
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            //if(isCanceled) return;
            switch (msg.what) {
                case MSG_UPDATE_LISTVIEW:
                    scanloadingDialog.loadSuccess();
                    scanloadingDialog.close();
                    if (isLockAndExit) {
                        drugOutBeanList = new ArrayList<>();
                        tvUserNameMain.setText("开锁:" + "个，落锁时候还有=" + drugInBeanList.size() + "个");

                        Log.d("lalala", "drugInBeanList" + drugInBeanList.size());
                        if (drugInBeanList.size() > drugBeanList.size()) {
                            drugMainPresenter.setUserName("入库员");
                            Log.d("lalala", "入库员");
                            //判断出库入库
                            for (DrugBean drugBean : drugInBeanList) {
                                if (!drugBeanList.contains(drugBean)) {
                                    drugOutBeanList.add(drugBean);
                                }
                            }
                        } else {
                            drugMainPresenter.setUserName("出库员");
                            Log.d("lalala", "出库员");
                            for (DrugBean drugBean : drugBeanList) {
                                if (!drugInBeanList.contains(drugBean)) {
                                    drugOutBeanList.add(drugBean);
                                }
                            }
                        }
                        drugMainPresenter.setDrugBeanList(drugOutBeanList);
//                        drugMainPresenter.showDiaLog();
                        drugMainPresenter.showPopOut();
                        Log.d("lalala", "drugOutBeanList" + drugOutBeanList.size());
                    } else {

                        drugAdapter.setDrugBeanList(drugBeanList);
                        drugAdapter.notifyDataSetChanged();
                        Log.d("lalala", "drugBeanList" + drugBeanList.size());
                        tvUserNameMain.setText("开锁总数:" + drugBeanList.size() + "个");
                    }
                    break;

                case MSG_UPDATE_FAIL:
                    scanloadingDialog.loadFailed();
                default:
                    break;
            }
            super.handleMessage(msg);
        }

    };

    private void showDialog() {
        setAlpha(0.1f);
        MyDialog myDialog = new MyDialog(DrugMainActivity.this, R.style.MyDialog);
        myDialog.setYesOnclickListener("登出", new MyDialog.onYesOnclickListener() {
            @Override
            public void onYesOnclick() {
                //退出并锁定
                Log.d("lalala", "退出并锁定");
                myDialog.dismiss();
                sendLockHexByStatus(false);
//                Reader.rrlib.DisConnect()
                scanloadingDialog.setLoadingText("扫描中..").setSuccessText("扫描完毕!").setFailedText("扫描失败!").show();
                scanloadingDialog.show();
                if (!isConnect232) {
                    mHandler.removeMessages(MSG_UPDATE_LISTVIEW);
                    mHandler.sendEmptyMessage(MSG_UPDATE_LISTVIEW);
                    return;
                }
                currentTime = System.currentTimeMillis();
                timeOut();
                dtIndexMap = new LinkedHashMap<String, Integer>();
                Reader.rrlib.StartRead();
                setAlpha(1.0f);
            }
        });
        myDialog.setNoOnclickListener("返回", new MyDialog.onNoOnclickListener() {
            @Override
            public void onNoClick() {
                myDialog.dismiss();
                setAlpha(1.0f);
            }
        });
        myDialog.show();
    }

    private String getDateString() {
        Date date = new Date();

        long times = date.getTime();
        //时间戳
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(date);
        return dateString;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = this.getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION//布局位于状态栏下方
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE//保持布局状态
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION//隐藏导航栏
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION//布局隐藏导航栏
                | View.SYSTEM_UI_FLAG_IMMERSIVE//避免某些用户交互造成系统自动清除全屏状态。
                | View.SYSTEM_UI_FLAG_FULLSCREEN;//全屏
        window.getDecorView().setSystemUiVisibility(uiOptions);
        setContentView(R.layout.activity_drug_main);
        drugMainPresenter = new DrugMainPresenter(this);
        initView();
        initData();

    }


    private void initSerialtty4() {
        DispQueue = new DispQueueThread();
        DispQueue.start();
        serialCom4 = new SerialControl();
        mSerialPortFinder = new SerialPortFinder();
        serialCom4.setPort("/dev/ttyS4");
        serialCom4.setBaudRate(9600);
        OpenComPort(serialCom4);
    }

    private void initData() {
        scanloadingDialog.setShowTime(5000).setLoadingText("扫描中..").setSuccessText("扫描完毕!").setFailedText("扫描失败!").show();
        connectCount = 0;
        //初始化登入和登出的列表
        mFirstCurIvtClist = new ArrayList<HashMap<String, String>>();
        mExitCurIvtClist = new ArrayList<HashMap<String, String>>();
        if (GlobalData.debugger) {
            initDebuggerData();
        } else {
            initSerialtty4();
            connect232(connectCount);
        }
    }

    private void initDebuggerData() {
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //调试模式
                    mCurIvtClist = new ArrayList<>();
                    for (int i = 0; i < 10; i++) {
                        DrugBean drugBean = new DrugBean("新型冠状病毒[2019-nCoV]" + i + "", i + "", getDateString(), getDateString(), "dsa3234sfadf43545435435" + i, 0);
                        drugBeanList.add(drugBean);
                        if (i < 8) {
                            drugInBeanList.add(drugBean);
                        }
                    }
                    Thread.sleep(3000);
                    mHandler.sendEmptyMessage(MSG_UPDATE_LISTVIEW);
                    isScan = false;
                    mThread = null;
                } catch (Exception ex) {
                    mThread = null;
                    mHandler.sendEmptyMessage(MSG_UPDATE_FAIL);
                }
            }
        });
        mThread.start();
    }


    private void initView() {
        isLockAndExit = false;
        drugBeanList = new ArrayList<>();
        drugInBeanList = new ArrayList<>();
        recyclerViewDrug = findViewById(R.id.recyclerViewDrug);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewDrug.setLayoutManager(layoutManager);
        drugAdapter = new DrugAdapter(drugBeanList);
        recyclerViewDrug.setAdapter(drugAdapter);
        Button btnExit = findViewById(R.id.btnExit);
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //退出按键,弹出dialog
                isLockAndExit = true;
                if (!GlobalData.debugger) {
                    //todo
                    showDialog();

                } else {
               /*     Log.d("lalala", "退出按键,弹出dialog");
                    mHandler.sendEmptyMessage(MSG_UPDATE_LISTVIEW);
                    drugMainPresenter.showDiaLog();*/
                    showDialog();
                }
            }
        });
        tvUserNameMain = findViewById(R.id.tvUserNameMain);
        tvUserNameMain.setText(drugMainPresenter.getUserName());
        tvUserNameMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                startActivity(new Intent(DrugMainActivity.this,SerialActivity.class));
//                drugMainPresenter.showPopSerialPortTest();
            }
        });
        scanloadingDialog = new LoadingDialog(this);

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (isFirst && hasFocus && !isScan) {
            //登入时候弹出欢迎界面
            drugMainPresenter.showPop();
            isFirst = false;
        }
    }


    @Override
    protected void onDestroy() {
        CloseComPort(serialCom4);
        onActivityDestroy();
        setResult(RESULT_OK, new Intent());
        finish();
        drugMainPresenter = null;
        super.onDestroy();
    }

    private void onActivityDestroy() {
        isScan = false;
        Reader.rrlib.DisConnect();
        if (mThread != null) {
            mThread = null;
        }
    }


    private boolean isConnect232 = false;

    private void connect232(int count) {
        LoadingDialog connecttloadingDialog = new LoadingDialog(this);
        connecttloadingDialog.setLoadingText("连接扫描仪...").setSuccessText("连接成功!").setFailedText("连接失败!").show();
        try {
//            int result = 0x30;
            String rfidPort = "/dev/ttyUSB" + count;
            int rfidBaudRate = 115200;

            Reader.rrlib = new UHFLib(0, "");
            byte[] data = new byte[1];
            int result = Reader.rrlib.Connect(rfidPort, rfidBaudRate);
            data[0] = (byte) (result);
//                    mHandler.obtainMessage(MSG_SHOW_RESULT, 1, -1, data).sendToTarget();
            if (result == 0) {
                connecttloadingDialog.loadSuccess();
                getDataNew();
                isConnect232 = true;
            } else {
                connecttloadingDialog.loadFailed();
                continueConnect(count);
            }


        } catch (Exception e) {
            connecttloadingDialog.loadFailed();
            continueConnect(count);
        }
    }


    private void getDataNew() {
        scanloadingDialog.show();
        currentTime = System.currentTimeMillis();
        dtIndexMap = new LinkedHashMap<String, Integer>();
        MsgCallback callback = new MsgCallback();
        Reader.rrlib.SetCallBack(callback);
        if (Reader.rrlib.StartRead() == 0) {

/*            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mHandler.removeMessages(MSG_UPDATE_TIME);
                    mHandler.sendEmptyMessage(MSG_UPDATE_TIME);
                }
            }, 0, SCAN_INTERVAL);*/
            lsTagList = new ArrayList<InventoryTagMap>();
            dtIndexMap = new LinkedHashMap<String, Integer>();
        }
        timeOut();
    }

    private void timeOut() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (System.currentTimeMillis() - currentTime >= 3000) {
                    Reader.rrlib.StopRead();
                    timer.cancel();
                    if (!isLockAndExit) {
                        sendLockHexByStatus(true);//扫描超时开锁
                    }
                }
            }
        }, 3000);
    }

    private static final int MSG_UPDATE_TIME = 6;
    private static final int SCAN_INTERVAL = 20;
    public static List<InventoryTagMap> lsTagList = new ArrayList<InventoryTagMap>();
    public Map<String, Integer> dtIndexMap = new LinkedHashMap<String, Integer>();
    private List<InventoryTagMap> data;

    public static class InventoryTagMap {
        public String strEPC;
        public int antenna;
        public String strRSSI;
        public int nReadCount;
    }

    long currentTime = 0;

    public class MsgCallback implements TagCallback {

        @Override
        public void tagCallback(ReadTag arg0) {
            // TODO Auto-generated method stub
            String epc = arg0.epcId.toUpperCase();
            String DevName = arg0.DevName;
            InventoryTagMap m;
            Integer findIndex = dtIndexMap.get(epc);
            if (findIndex == null) {
                dtIndexMap.put(epc, dtIndexMap.size());
                m = new InventoryTagMap();
                m.strEPC = epc;
                m.antenna = 1 << (arg0.antId - 1);
                m.strRSSI = String.valueOf(arg0.rssi);
                m.nReadCount = 1;
                lsTagList.add(m);

                String tagRssi = m.strRSSI;
                String tagAnt = m.antenna + "";
                String tagUid = m.strEPC;
                DrugBean drugBean = new DrugBean("新型冠状病毒[2019-nCoV]" + tagRssi, tagAnt, getDateString(), getDateString(), tagUid, 0);
                currentTime = System.currentTimeMillis();
                if (isLockAndExit) {
                    drugInBeanList.add(drugBean);
                    Log.d("weid", "drugInBeanList.size=" + drugInBeanList.size());
                } else {
                    drugBeanList.add(drugBean);
                    Log.d("weid", "drugBeanList.size=" + drugBeanList.size());
                }


            } else {
                m = lsTagList.get(findIndex);
                m.antenna |= 1 << (arg0.antId - 1);
                m.nReadCount++;
                m.strRSSI = String.valueOf(arg0.rssi);

                if (System.currentTimeMillis() - currentTime > 3000) {
                    Log.d("weid", "===================");
                    Reader.rrlib.StopRead();
                    if (isLockAndExit) {
//                        sendLockHexByStatus(false);
                    } else {
                        sendLockHexByStatus(true);
                    }
                }
            }

        }

        @Override
        public int tagCallbackFailed(int reason) {
            // TODO Auto-generated method stub
            Log.d("weid", "tagCallbackFailed===================");
//            scanloadingDialog.loadFailed();
            return 0;
        }

        @Override
        public void ReadOver() {
            Log.d("weid", "ReadOver===================");
            mHandler.removeMessages(MSG_UPDATE_LISTVIEW);
            mHandler.sendEmptyMessage(MSG_UPDATE_LISTVIEW);
        }
    }

    ;


    //失败之后重新连接
    private void continueConnect(int count) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        count++;
        if (count > 3) {
            scanloadingDialog.close();
            return;
        }
        connect232(count);
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
        showTipsInfo(sbMsg.toString());
        //使用我的手机NFC，读取到信息就开锁

    }

    private String openHex = "AA55010D";
    private String lockHex = "AA55020D";

    //开关锁
    private void sendLockHexByStatus(boolean status) {
        String sendHex = status ? openHex : lockHex;
        if (serialCom4 != null) {
            serialCom4.sendHex(sendHex);
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

    protected void setAlpha(float f) {
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        attributes.alpha = f;
        getWindow().setAttributes(attributes);
    }

}