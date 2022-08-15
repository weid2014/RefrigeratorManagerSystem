package com.techjh.rfid;

import java.util.ArrayList;
import java.util.HashMap;

public class InventoryTagMap {
	public int Antenna;//天线号
	public ArrayList<HashMap<String, String>> newlist;//当前列表
	public ArrayList<HashMap<String, String>> oldlist;//历史列表
	public boolean isCheck;//是否启用盘点
}
