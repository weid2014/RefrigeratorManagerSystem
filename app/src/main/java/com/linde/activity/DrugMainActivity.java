package com.linde.activity;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import com.xiasuhuei321.loadingdialog.view.LoadingDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

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
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            //if(isCanceled) return;
            switch (msg.what) {
                case MSG_UPDATE_LISTVIEW:
                    scanloadingDialog.loadSuccess();
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
                        drugMainPresenter.showDiaLog();
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
        setContentView(R.layout.activity_drug_main);
        drugMainPresenter = new DrugMainPresenter(this);
        initView();
        initData();
    }

    private void initData() {
        scanloadingDialog.setLoadingText("扫描中..").setSuccessText("扫描完毕!").setFailedText("扫描失败!").show();
        connectCount = 0;
        //初始化登入和登出的列表
        mFirstCurIvtClist = new ArrayList<HashMap<String, String>>();
        mExitCurIvtClist = new ArrayList<HashMap<String, String>>();
        if (GlobalData.debugger) {
            initDebuggerData();
            testThread();
        } else {
            connect232(connectCount);
        }
    }

    private void initDebuggerData() {
        //添加参与扫描的天线


//        mThread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    //调试模式
//                    mCurIvtClist = new ArrayList<>();
//                    for (int i = 0; i < 10; i++) {
//                        HashMap<String, String> stringStringHashMap = new HashMap<>();
//                        stringStringHashMap.put("tagRssi", i + "");
//                        stringStringHashMap.put("tagAnt", i + "");
//                        stringStringHashMap.put("tagUid", "dsa3234sfadf43545435435" + i);
//                        mFirstCurIvtClist.add(stringStringHashMap);
//                        if (i < 8) {
//                            mExitCurIvtClist.add(stringStringHashMap);
//                        }
//                    }
//                    Thread.sleep(5000);
//
//                    mHandler.sendEmptyMessage(MSG_UPDATE_LISTVIEW);
//                    isScan = false;
//                } catch (Exception ex) {
//                    mThread = null;
//                    mHandler.sendEmptyMessage(MSG_UPDATE_FAIL);
//                }
//            }
//        });
//        mThread.start();
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
                isFinish = false;
                if (!GlobalData.debugger) {
                    //todo
//                    getDataNew();
                    scanloadingDialog.setLoadingText("扫描中..").setSuccessText("扫描完毕!").setFailedText("扫描失败!").show();
                    scanloadingDialog.show();
                    dtIndexMap = new LinkedHashMap<String, Integer>();
                    Reader.rrlib.StartRead();
                } else {
                    Log.d("lalala", "退出按键,弹出dialog");
                    mHandler.sendEmptyMessage(MSG_UPDATE_LISTVIEW);
                    drugMainPresenter.showDiaLog();
                }
            }
        });
        tvUserNameMain = findViewById(R.id.tvUserNameMain);
        tvUserNameMain.setText(drugMainPresenter.getUserName());
        tvUserNameMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                startActivity(new Intent(DrugMainActivity.this,SerialActivity.class));
                drugMainPresenter.showPopSerialPortTest();
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

    private boolean isFinish = false;
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
            return;
        }
        connect232(count);
    }

    private CountDownLatch mCountDownLatch;

    private void testThread() {
        mCountDownLatch = new CountDownLatch(10);
        for (int i = 1; i < 12; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.d("TAG", Thread.currentThread().getName() + " start:" + mCountDownLatch.getCount());
                    mCountDownLatch.countDown();
                    Log.d("TAG", Thread.currentThread().getName() + " end:" + mCountDownLatch.getCount());

                }
            }).start();
        }

    }


    public boolean isEmpty(String strEPC) {
        return strEPC == null || strEPC.length() == 0;
    }

    public int checkIsExist(String strUID, ArrayList<HashMap<String, String>> mList) {
        int existFlag = -1;
        if (isEmpty(strUID)) {
            return existFlag;
        }
        if (mList == null) {
            return existFlag;
        }
        String tempStr = "";
        for (int i = 0; i < mList.size(); i++) {
            HashMap<String, String> temp = new HashMap<String, String>();
            temp = mList.get(i);
            tempStr = temp.get("tagUid");
            if (strUID.equals(tempStr)) {
                existFlag = i;
                break;
            }
        }
        return existFlag;
    }

    public String CheckAddList(ArrayList<HashMap<String, String>> mList, ArrayList<HashMap<String, String>> nList) {
        String result = "";
        HashMap<String, String> temp = new HashMap<String, String>();
        if (nList != null) {
            for (int m = 0; m < nList.size(); m++) {
                temp = nList.get(m);
                String uid = temp.get("tagUid");
                int index = checkIsExist(uid, mList);
                if (index == -1) {
                    result += (uid + "-" + temp.get("tagAnt") + " ");
                } else {

                }
            }
        }
        return result;
    }

    public String CheckLostList(ArrayList<HashMap<String, String>> mList, ArrayList<HashMap<String, String>> nList) {
        String result = "";
        HashMap<String, String> temp = new HashMap<String, String>();
        if (mList != null) {
            for (int m = 0; m < mList.size(); m++) {
                temp = mList.get(m);
                String uid = temp.get("tagUid");
                int index = checkIsExist(uid, nList);
                if (index == -1) {
                    result += (uid + "-" + temp.get("tagAnt") + " ");
                } else {

                }
            }
        }
        return result;
    }

}