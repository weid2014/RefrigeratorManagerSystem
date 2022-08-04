package com.linde.rfid;

import java.util.ArrayList;
import java.util.HashMap;

public class RfidAdapter {
    String devicePath = "/dev/ttyUSB1";
    int speed = 19200;
    int mType = 0;
    private String strIP = "192.168.1.192";
    private String strPort = "6000";
    private boolean isConnected = false;


    public boolean getIsConnected() {
        return isConnected;
    }

    public void connect() {
        int result = 0x30;
        if (mType == 0) {
            result = HfData.reader.OpenReader(speed, devicePath, mType, 1, null);
        } else {
            int nPort = Integer.valueOf(strPort);
            result = HfData.reader.OpenReader(nPort, strIP, mType, 1, null);
        }
        isConnected = result == 0;
        if (isConnected){
            init();
        }
    }

    private void init() {
        InventoryTagMap map = new InventoryTagMap();//默认启用第一个天线盘点
        map.Antenna = 1;
        map.isCheck = true;
        map.newlist = new ArrayList<HashMap<String, String>>();
        map.oldlist = new ArrayList<HashMap<String, String>>();
        HfData.mlist.add(map);
    }

}
