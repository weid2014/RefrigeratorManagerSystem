package com.linde.activity;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.linde.rfid.HfData;
import com.linde.rfid.InventoryTagMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DrugMainActivity extends CustomActivity {
    private RecyclerView recyclerViewDrug;
    private DrugAdapter drugAdapter;
    private boolean isFirst = true;
    private boolean isFirstIn = true;
    private Button btnExit;
    private TextView tvUserNameMain;

    private IDrugMainPresenter drugMainPresenter;

    private String rfidPort = "/dev/ttyUSB";
    private int rfidBaudRate = 19200;
    private Handler mHandler;
    private static final int MSG_UPDATE_LISTVIEW = 0;
    private static final int MSG_UPDATE_INFO = 1;
    long ScanTime = 0;
    long Number = 0;
    private long currentTime = 0;

    public static ArrayList<HashMap<String, String>> mCurIvtClist;
    public static ArrayList<HashMap<String, String>> mlastIvtClist;
    public static ArrayList<HashMap<String, String>> mnewIvtClist;
    public static ArrayList<HashMap<String, String>> mIvtInfolist;
    private List<DrugBean> drugBeanList = null;
    private List<DrugBean> drugOldInBeanList = null;
    private List<DrugBean> drugInBeanList = null;
    private List<DrugBean> drugOutBeanList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drug_main);
        drugMainPresenter = new DrugMainPresenter(this);
        initView();
        connectCount = 0;
        if(GlobalData.debugger){
            //调试模式
        }else {
            connect232(connectCount);
        }

        mHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                //if(isCanceled) return;
                switch (msg.what) {
                    case MSG_UPDATE_LISTVIEW:
                        currentTime = System.currentTimeMillis();
                        drugBeanList = new ArrayList<>();
                        for (HashMap<String, String> stringStringHashMap : mCurIvtClist) {
                            String tagRssi = stringStringHashMap.get("tagRssi");
                            String tagAnt = stringStringHashMap.get("tagAnt");
                            String tagUid = stringStringHashMap.get("tagUid");
                            DrugBean drugBean = new DrugBean("新型冠状病毒[2019-nCoV]" + tagRssi, tagAnt, "2022-07-31 22:10:36", "2022-07-31 22:10:36", tagUid, 0);
                            drugBeanList.add(drugBean);
                        }

//                        drugAdapter = new DrugAdapter(drugBeanList);
//                        recyclerViewDrug.setAdapter(drugAdapter);
                        drugAdapter.setDrugBeanList(drugBeanList);
                        drugAdapter.notifyDataSetChanged();
                        Toast.makeText(DrugMainActivity.this, "总数=" + Number + "个", Toast.LENGTH_SHORT).show();
                        if (isFirstIn) {
                            drugOldInBeanList = drugBeanList;
                        } else {
                            drugOutBeanList = new ArrayList<>();
                            for (DrugBean drugBean : drugBeanList) {
                                //进来时候的数据跟退出时候的数据对比，
                                if (!drugOldInBeanList.contains(drugBean)) {
                                    drugOutBeanList.add(drugBean);
                                    drugBean.setDrugStatus(1);
                                }
                            }
                            drugAdapter.setDrugBeanList(drugBeanList);
                            drugAdapter.notifyDataSetChanged();
                            drugMainPresenter.showDiaLog();
                        }
                        break;
                    case MSG_UPDATE_INFO:
                        break;
                    default:
                        break;
                }
                super.handleMessage(msg);
            }

        };
    }


    private void initView() {
        currentTime = System.currentTimeMillis();
        drugBeanList = new ArrayList<>();
        recyclerViewDrug = findViewById(R.id.recyclerViewDrug);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewDrug.setLayoutManager(layoutManager);
        drugAdapter = new DrugAdapter(drugBeanList);
        recyclerViewDrug.setAdapter(drugAdapter);
        btnExit = findViewById(R.id.btnExit);
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //退出按键,弹出dialog
                isFirstIn = false;
                getRfidData();
                drugMainPresenter.showDiaLog();
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
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (isFirst && hasFocus) {
            drugMainPresenter.showPop();
            isFirst = false;
        }
    }


    @Override
    protected void onDestroy() {
        onActivityDestroy();
        setResult(RESULT_OK, new Intent());
        super.onDestroy();
        finish();
    }

    private void onActivityDestroy() {

        isScan = false;
        HfData.reader.CloseReader();
    }


    private int connectCount = 0;

    private void connect232(int count) {
        try {
            int result = 0x30;
            rfidPort = "/dev/ttyUSB" + count;
            Toast.makeText(
                    getApplicationContext(),
                    "开始连接232，地址:" + rfidPort + "---波特率:" + rfidBaudRate,
                    Toast.LENGTH_SHORT).show();
            result = HfData.reader.OpenReader(rfidBaudRate, rfidPort, 0, 1, null);

            if (result == 0) {
                InventoryTagMap map = new InventoryTagMap();//默认启用第一个天线盘点
                map.Antenna = 1;
                map.isCheck = true;
                map.newlist = new ArrayList<HashMap<String, String>>();
                map.oldlist = new ArrayList<HashMap<String, String>>();
                HfData.mlist.add(map);

                Toast.makeText(
                        getApplicationContext(),
                        "连接232成功",
                        Toast.LENGTH_SHORT).show();
                getRfidData();

            } else {
                Toast.makeText(
                        getApplicationContext(),
                        "连接失败",
                        Toast.LENGTH_SHORT).show();
                continueConnect(count);
            }
        } catch (Exception e) {
            Toast.makeText(
                    getApplicationContext(),
                    "连接失败",
                    Toast.LENGTH_SHORT).show();
            continueConnect(count);
        }
    }
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

    private void AddAntenna(int antenna) {
        InventoryTagMap map = new InventoryTagMap();
        map.Antenna = antenna;
        map.isCheck = true;
        map.newlist = new ArrayList<HashMap<String, String>>();
        map.oldlist = new ArrayList<HashMap<String, String>>();
        HfData.mlist.add(map);
    }

    Thread mThread;
    int Count = 0;
    private boolean isScan = true;

    private void getRfidData() {
        //添加参与扫描的天线
        for (int i = 1; i < 11; i++) {
            AddAntenna(i);
        }


        mCurIvtClist = new ArrayList<HashMap<String, String>>();
        mlastIvtClist = new ArrayList<HashMap<String, String>>();
        mIvtInfolist = new ArrayList<HashMap<String, String>>();
        mnewIvtClist = new ArrayList<HashMap<String, String>>();
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (isScan) {
                        long beginTime = System.currentTimeMillis();
                        boolean selectant = false;
                        mnewIvtClist.clear();
                        for (int m = 0; m < HfData.mlist.size(); m++) {
                            InventoryTagMap map = HfData.mlist.get(m);
                            if (map.isCheck) {
                                InventoryTagByAnt(map);
                                selectant = true;
                            }
                        }
                        Count++;
                        mCurIvtClist.clear();
                        if (mnewIvtClist.size() > 0) {
                            for (int i = 0; i < mnewIvtClist.size(); i++) {
                                HashMap<String, String> temp = new HashMap<String, String>();
                                temp = mnewIvtClist.get(i);
                                mCurIvtClist.add(temp);
                            }
                        }
                        if (Count > 1) {
                            String adduid = CheckAddList(mlastIvtClist, mCurIvtClist);
                            String lostuid = CheckLostList(mlastIvtClist, mCurIvtClist);
                            if (adduid.length() > 0 || lostuid.length() > 0) {
                                String logStr = "";
                                logStr += ("第 ");
                                logStr += (String.valueOf(Count) + "轮, 标签总数量从") + String.valueOf(mlastIvtClist.size())
                                        + "到" + String.valueOf(mCurIvtClist.size());
                                if (adduid.length() > 0)
                                    logStr += ("\n" + "增加UID：") + adduid;
                                if (lostuid.length() > 0)
                                    logStr += ("\n" + "减少UID：") + lostuid;

                                HashMap<String, String> temp = new HashMap<String, String>();
                                temp.put("tagInfo", logStr);
                                if (mIvtInfolist.size() == 0)
                                    mIvtInfolist.add(temp);
                                else
                                    mIvtInfolist.add(0, temp);
                                mHandler.removeMessages(MSG_UPDATE_INFO);
                                mHandler.sendEmptyMessage(MSG_UPDATE_INFO);
                            }
                        }
                        mlastIvtClist.clear();
                        if (mnewIvtClist.size() > 0) {
                            for (int i = 0; i < mnewIvtClist.size(); i++) {
                                HashMap<String, String> temp = new HashMap<String, String>();
                                temp = mnewIvtClist.get(i);
                                mlastIvtClist.add(temp);
                            }
                        }
                        Number = mCurIvtClist.size();
                        ScanTime = System.currentTimeMillis() - beginTime;
                        mHandler.removeMessages(MSG_UPDATE_LISTVIEW);
                        mHandler.sendEmptyMessage(MSG_UPDATE_LISTVIEW);
                        //doTimeWork();
                        isScan = false;
                        mThread = null;
                    }

                } catch (Exception ex) {
                    mThread = null;
                }
            }
        });
        mThread.start();
    }

    private void InventoryTagByAnt(InventoryTagMap map) {
        int Antenna = map.Antenna - 1;
        int fCmdRet = HfData.reader.SetAntenna((byte) Antenna);
        byte state = (byte) 0x86;
        do {
            byte[] UID = new byte[25600];
            int[] CardNum = new int[1];
            CardNum[0] = 0;
            fCmdRet = HfData.reader.Inventory(state, UID, CardNum);
            if (CardNum[0] > 0) {
                for (int m = 0; m < CardNum[0]; m++) {
                    byte[] daw = new byte[10];
                    System.arraycopy(UID, m * 10, daw, 0, 10);
                    String uidStr = HfData.bytesToHexString(daw, 1, 8);
                    int rssi = daw[9] & 255;
                    HashMap<String, String> temp = new HashMap<String, String>();
                    //"tagUid","tagAnt","tagRssi"
                    temp.put("tagUid", uidStr);
                    temp.put("tagAnt", String.valueOf(map.Antenna));
                    temp.put("tagRssi", String.valueOf(rssi));
                    int index = checkIsExist(uidStr, mnewIvtClist);
                    if (index == -1)//不存在
                    {
                        mnewIvtClist.add(temp);
                    } else {
                        int tagrssi = Integer.parseInt(
                                mnewIvtClist.get(index).get("tagRssi"), 10);
                        if (rssi > tagrssi) {
                            mnewIvtClist.set(index, temp);
                        }
                    }
                }
            }
            state = (byte) 0x82;
        } while (fCmdRet != 0x0E);
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
                    ;
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
                    ;
                }
            }
        }
        return result;
    }

}