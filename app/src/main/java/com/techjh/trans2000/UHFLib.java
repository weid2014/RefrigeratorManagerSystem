package com.techjh.trans2000;

import android.media.SoundPool;
import android.os.SystemClock;



import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class UHFLib {
    private BaseReader reader = null;
    private ReaderParameter param = new ReaderParameter();
    private volatile boolean mWorking = true;
    private volatile Thread mThread = null;
    private volatile boolean soundworking = true;
    private volatile boolean isSound = false;
    private volatile Thread sThread = null;
    private byte[] pOUcharIDList = new byte[25600];
    private volatile int NoCardCOunt = 0;
    private int mType = 0;
    private Integer soundid = null;
    private SoundPool soundPool = null;
    private TagCallback callback;
    private boolean CmdWorking = false;
    private Lock lock = new ReentrantLock();

    public UHFLib(int type, String DevName) {
        this.param.ComAddr = -1;
        this.param.IvtType = 0;
        this.param.Memory = 2;
        this.param.Password = "00000000";
        this.param.TidPtr = 0;
        this.param.TidLen = 0;
        this.param.Session = 1;
        this.param.QValue = 4;
        this.param.ScanTime = 10;
        this.param.Target = 0;
        this.param.reTryCount = 16;
        this.param.Antenna = 1;
        this.mType = type;
        this.reader = new BaseReader(type, DevName);
        this.CmdWorking = false;
        this.param.MaxAntennaNum = 4;
    }

    public int Connect(String ComPort, int BaudRate) {
        int result = this.reader.Connect(ComPort, BaudRate, this.mType);
        if (result == 0) {
            byte[] Version = new byte[2];
            byte[] Power = new byte[1];
            byte[] band = new byte[1];
            byte[] MaxFre = new byte[1];
            byte[] MinFre = new byte[1];
            byte[] BeepEn = new byte[1];
            int[] Ant = new int[1];
            this.GetUHFInformation(Version, Power, band, MaxFre, MinFre, BeepEn, Ant);
        }

        return result;
    }

    public int DisConnect() {
        this.soundworking = false;
        int result = this.reader.DisConnect();
        this.StopRead();
        return result;
    }

    public void SetInventoryPatameter(ReaderParameter param) {
        this.param = param;
    }

    public ReaderParameter GetInventoryPatameter() {
        return this.param;
    }

    public int GetUHFInformation(byte[] Version, byte[] Power, byte[] band, byte[] MaxFre, byte[] MinFre, byte[] BeepEn, int[] Ant) {
        byte[] ReaderType = new byte[1];
        byte[] TrType = new byte[1];
        byte[] ScanTime = new byte[1];
        byte[] OutputRep = new byte[1];
        byte[] CheckAnt = new byte[1];
        byte[] ComAddr = new byte[]{-1};
        byte[] AntCfg0 = new byte[1];
        byte[] AntCfg1 = new byte[1];
        this.lock.lock();
        int result = this.reader.GetReaderInformation(ComAddr, Version, ReaderType, TrType, band, MaxFre, MinFre, Power, ScanTime, AntCfg0, BeepEn, AntCfg1, CheckAnt);
        this.lock.unlock();
        if (result == 0) {
            Ant[0] = ((AntCfg1[0] & 255) << 8) + (AntCfg0[0] & 255);
            this.param.ComAddr = ComAddr[0];
            this.param.Antenna = ((AntCfg1[0] & 255) << 8) + (AntCfg0[0] & 255);
            this.isSound = false;
            this.soundworking = true;
            switch(ReaderType[0] & 255) {
                case 38:
                case 104:
                    this.param.MaxAntennaNum = 8;
                    break;
                case 39:
                case 101:
                    this.param.MaxAntennaNum = 16;
                    break;
                default:
                    this.param.MaxAntennaNum = 4;
            }
        }

        return result;
    }

    public int SetRfPower(int Power) {
        this.lock.lock();
        int result = this.reader.SetRfPower(this.param.ComAddr, (byte)Power);
        this.lock.unlock();
        return result;
    }

    public int SetRegion(int band, int maxfre, int minfre) {
        this.lock.lock();
        int result = this.reader.SetRegion(this.param.ComAddr, band, maxfre, minfre);
        this.lock.unlock();
        return result;
    }

    public int SetAntenna(byte SetOnce, int AntCfg) {
        this.lock.lock();
        int result;
        if (this.param.MaxAntennaNum > 4) {
            byte AntCfg1 = (byte)(AntCfg >> 8);
            byte AntCfg2 = (byte)(AntCfg & 255);
            result = this.reader.SetAntennaMultiplexing(this.param.ComAddr, SetOnce, AntCfg1, AntCfg2);
            if (result == 0) {
                this.param.Antenna = AntCfg;
            }
        } else {
            if (SetOnce == 1) {
                AntCfg |= 128;
            }

            result = this.reader.SetAntennaMultiplexing(this.param.ComAddr, (byte)AntCfg);
            if (result == 0) {
                this.param.Antenna = AntCfg;
            }
        }

        this.lock.unlock();
        return result;
    }

    public int SetBeepNotification(int BeepEn) {
        this.lock.lock();
        int result = this.reader.SetBeepNotification(this.param.ComAddr, (byte)BeepEn);
        this.lock.unlock();
        return result;
    }

    public int SetRfPowerByAnt(byte[] Power) {
        this.lock.lock();
        int result = this.reader.SetRfPowerByAnt(this.param.ComAddr, Power);
        this.lock.unlock();
        return result;
    }

    public byte[] GetRfPowerByAnt() {
        this.lock.lock();
        byte[] Power = this.reader.GetRfPowerByAnt(this.param.ComAddr);
        this.lock.unlock();
        return Power;
    }

    public int ConfigDRM(byte[] DRM) {
        this.lock.lock();
        int result = this.reader.ConfigDRM(this.param.ComAddr, DRM);
        this.lock.unlock();
        return result;
    }

    public int SetRelay(byte RelayTime) {
        this.lock.lock();
        int result = this.reader.SetRelay(this.param.ComAddr, RelayTime);
        this.lock.unlock();
        return result;
    }

    public int SetGPIO(byte GPIO) {
        this.lock.lock();
        int result = this.reader.SetGPIO(this.param.ComAddr, GPIO);
        this.lock.unlock();
        return result;
    }

    public int GetGPIOStatus(byte[] OutputPin) {
        this.lock.lock();
        int result = this.reader.GetGPIOStatus(this.param.ComAddr, OutputPin);
        this.lock.unlock();
        return result;
    }

    public String GetDeviceID() {
        byte[] btArr = new byte[4];
        this.lock.lock();
        int result = this.reader.GetDeviceID(this.param.ComAddr, btArr);
        this.lock.unlock();
        if (result == 0) {
            String temp = this.reader.bytesToHexString(btArr, 0, btArr.length);
            return temp;
        } else {
            return "";
        }
    }

    public int MeasureReturnLoss(byte[] TestFreq, byte Ant, byte[] ReturnLoss) {
        this.lock.lock();
        int result = this.reader.MeasureReturnLoss(this.param.ComAddr, TestFreq, Ant, ReturnLoss);
        this.lock.unlock();
        return result;
    }

    public int SetCheckAnt(byte CheckAnt) {
        this.lock.lock();
        int result = this.reader.SetCheckAnt(this.param.ComAddr, CheckAnt);
        this.lock.unlock();
        return result;
    }

    public int SetWritePower(byte WritePower) {
        this.lock.lock();
        int result = this.reader.SetWritePower(this.param.ComAddr, WritePower);
        this.lock.unlock();
        return result;
    }

    public int GetWritePower(byte[] WritePower) {
        this.lock.lock();
        int result = this.reader.GetWritePower(this.param.ComAddr, WritePower);
        this.lock.unlock();
        return result;
    }

    public int SetRetryTimes(byte times) {
        byte[] btTimes = new byte[]{(byte)(times | 128)};
        this.lock.lock();
        int result = this.reader.RetryTimes(this.param.ComAddr, btTimes);
        this.lock.unlock();
        return result;
    }

    public int GetRetryTimes(byte[] times) {
        times[0] = 0;
        this.lock.lock();
        int result = this.reader.RetryTimes(this.param.ComAddr, times);
        this.lock.unlock();
        return result;
    }

    public String ReadDataByEPC(String EPCStr, byte Mem, byte WordPtr, byte Num, byte[] Password) {
        if (EPCStr.length() % 4 != 0) {
            return "FF";
        } else {
            byte ENum = (byte)(EPCStr.length() / 4);
            byte[] EPC = this.reader.hexStringToBytes(EPCStr);
            byte MaskMem = 0;
            byte[] MaskAdr = new byte[2];
            byte MaskLen = 0;
            byte[] MaskData = new byte[12];
            byte MaskFlag = 0;
            byte[] Data = new byte[Num * 2];
            byte[] Errorcode = new byte[1];
            this.lock.lock();
            int result = this.reader.ReadData_G2(this.param.ComAddr, ENum, EPC, Mem, WordPtr, Num, Password, MaskMem, MaskAdr, MaskLen, MaskData, MaskFlag, Data, Errorcode);
            this.lock.unlock();
            return result == 0 ? this.reader.bytesToHexString(Data, 0, Data.length) : "";
        }
    }

    public String ReadDataByTID(String TIDStr, byte Mem, byte WordPtr, byte Num, byte[] Password) {
        if (TIDStr.length() % 4 != 0) {
            return "FF";
        } else {
            byte ENum = -1;
            byte[] EPC = new byte[12];
            byte[] TID = this.reader.hexStringToBytes(TIDStr);
            byte MaskMem = 2;
            byte[] MaskAdr = new byte[2];
            MaskAdr[0] = MaskAdr[1] = 0;
            byte MaskLen = (byte)(TIDStr.length() * 4);
            byte[] MaskData = new byte[TIDStr.length()];
            System.arraycopy(TID, 0, MaskData, 0, TID.length);
            byte MaskFlag = 1;
            byte[] Data = new byte[Num * 2];
            byte[] Errorcode = new byte[1];
            this.lock.lock();
            int result = this.reader.ReadData_G2(this.param.ComAddr, ENum, EPC, Mem, WordPtr, Num, Password, MaskMem, MaskAdr, MaskLen, MaskData, MaskFlag, Data, Errorcode);
            this.lock.unlock();
            return result == 0 ? this.reader.bytesToHexString(Data, 0, Data.length) : "";
        }
    }

    public int WriteDataByEPC(String EPCStr, byte Mem, byte WordPtr, byte[] Password, String wdata) {
        if (EPCStr.length() % 4 != 0) {
            return 255;
        } else if (wdata.length() % 4 != 0) {
            return 255;
        } else {
            byte ENum = (byte)(EPCStr.length() / 4);
            byte WNum = (byte)(wdata.length() / 4);
            byte[] EPC = this.reader.hexStringToBytes(EPCStr);
            byte[] data = this.reader.hexStringToBytes(wdata);
            byte MaskMem = 0;
            byte[] MaskAdr = new byte[2];
            byte MaskLen = 0;
            byte[] MaskData = new byte[12];
            byte MaskFlag = 0;
            byte[] Errorcode = new byte[1];
            this.lock.lock();
            int result = this.reader.WriteData_G2(this.param.ComAddr, WNum, ENum, EPC, Mem, WordPtr, data, Password, MaskMem, MaskAdr, MaskLen, MaskData, MaskFlag, Errorcode);
            this.lock.unlock();
            return result;
        }
    }

    public int WriteDataByTID(String TIDStr, byte Mem, byte WordPtr, byte[] Password, String wdata) {
        if (TIDStr.length() % 4 != 0) {
            return 255;
        } else if (wdata.length() % 4 != 0) {
            return 255;
        } else {
            byte ENum = -1;
            byte WNum = (byte)(wdata.length() / 4);
            byte[] EPC = new byte[12];
            byte[] data = this.reader.hexStringToBytes(wdata);
            byte[] TID = this.reader.hexStringToBytes(TIDStr);
            byte MaskMem = 2;
            byte[] MaskAdr = new byte[2];
            MaskAdr[0] = MaskAdr[1] = 0;
            byte MaskLen = (byte)(TIDStr.length() * 4);
            byte[] MaskData = new byte[TIDStr.length()];
            System.arraycopy(TID, 0, MaskData, 0, TID.length);
            byte MaskFlag = 1;
            byte[] Errorcode = new byte[1];
            this.lock.lock();
            int result = this.reader.WriteData_G2(this.param.ComAddr, WNum, ENum, EPC, Mem, WordPtr, data, Password, MaskMem, MaskAdr, MaskLen, MaskData, MaskFlag, Errorcode);
            this.lock.unlock();
            return result;
        }
    }

    public int WriteEPC(String EPCStr, byte[] Password) {
        if (EPCStr.length() % 4 != 0) {
            return 255;
        } else {
            byte WNum = (byte)(EPCStr.length() / 4);
            byte[] Errorcode = new byte[1];
            byte[] data = this.reader.hexStringToBytes(EPCStr);
            this.lock.lock();
            int result = this.reader.WriteEPC_G2(this.param.ComAddr, WNum, Password, data, Errorcode);
            this.lock.unlock();
            return result;
        }
    }

    public int WriteEPCByTID(String TIDStr, String EPCStr, byte[] Password) {
        if (TIDStr.length() % 4 != 0) {
            return 255;
        } else if (EPCStr.length() % 4 != 0) {
            return 255;
        } else {
            byte ENum = -1;
            byte WNum = (byte)(EPCStr.length() / 4);
            byte[] EPC = new byte[12];
            String PCStr = "";
            switch(WNum) {
                case 1:
                    PCStr = "0800";
                    break;
                case 2:
                    PCStr = "1000";
                    break;
                case 3:
                    PCStr = "1800";
                    break;
                case 4:
                    PCStr = "2000";
                    break;
                case 5:
                    PCStr = "2800";
                    break;
                case 6:
                    PCStr = "3000";
                    break;
                case 7:
                    PCStr = "3800";
                    break;
                case 8:
                    PCStr = "4000";
                    break;
                case 9:
                    PCStr = "4800";
                    break;
                case 10:
                    PCStr = "5000";
                    break;
                case 11:
                    PCStr = "5800";
                    break;
                case 12:
                    PCStr = "6000";
                    break;
                case 13:
                    PCStr = "6800";
                    break;
                case 14:
                    PCStr = "7000";
                    break;
                case 15:
                    PCStr = "7800";
                    break;
                case 16:
                    PCStr = "8000";
            }

            String wdata = PCStr + EPCStr;
            ++WNum;
            byte[] data = this.reader.hexStringToBytes(wdata);
            byte[] TID = this.reader.hexStringToBytes(TIDStr);
            byte MaskMem = 2;
            byte[] MaskAdr = new byte[2];
            MaskAdr[0] = MaskAdr[1] = 0;
            byte MaskLen = (byte)(TIDStr.length() * 4);
            byte[] MaskData = new byte[TIDStr.length()];
            System.arraycopy(TID, 0, MaskData, 0, TID.length);
            byte MaskFlag = 1;
            byte[] Errorcode = new byte[1];
            byte Mem = 1;
            byte WordPtr = 1;
            this.lock.lock();
            int result = this.reader.WriteData_G2(this.param.ComAddr, WNum, ENum, EPC, Mem, WordPtr, data, Password, MaskMem, MaskAdr, MaskLen, MaskData, MaskFlag, Errorcode);
            this.lock.unlock();
            return result;
        }
    }

    public int Lock(String EPCStr, byte select, byte setprotect, String PasswordStr) {
        if (EPCStr.length() % 4 != 0) {
            return 255;
        } else if (PasswordStr.length() != 8) {
            return 255;
        } else {
            byte ENum = (byte)(EPCStr.length() / 4);
            byte[] EPC = this.reader.hexStringToBytes(EPCStr);
            byte[] Password = this.reader.hexStringToBytes(PasswordStr);
            byte[] Errorcode = new byte[1];
            this.lock.lock();
            int result = this.reader.Lock_G2(this.param.ComAddr, ENum, EPC, select, setprotect, Password, Errorcode);
            this.lock.unlock();
            return result;
        }
    }

    public int Kill(String EPCStr, String PasswordStr) {
        if (EPCStr.length() % 4 != 0) {
            return 255;
        } else if (PasswordStr.length() != 8) {
            return 255;
        } else {
            byte ENum = (byte)(EPCStr.length() / 4);
            byte[] EPC = this.reader.hexStringToBytes(EPCStr);
            byte[] Password = this.reader.hexStringToBytes(PasswordStr);
            byte[] Errorcode = new byte[1];
            this.lock.lock();
            int result = this.reader.Kill_G2(this.param.ComAddr, ENum, EPC, Password, Errorcode);
            this.lock.unlock();
            return result;
        }
    }

    public void SetCallBack(TagCallback callback) {
        this.callback = callback;
        this.reader.SetCallBack(callback);
    }

    public int StartRead() {
        if (this.mThread == null) {
            this.mWorking = true;
            this.mThread = new Thread(new Runnable() {
                public void run() {
                    param.Target = 0;
                    if (param.Session == 2 || param.Session == 3) {
                        byte Selaction = 0;
                        byte Truncate = 0;
                        if (param.MaxAntennaNum == 16) {
                            reader.SelectCMD(param.ComAddr, param.Antenna, (byte) param.Session, Selaction, Truncate);
                        } else {
                            reader.SelectCMD(param.ComAddr, (byte) param.Antenna, (byte) param.Session, Selaction, Truncate);
                        }
                    }

                    while(mWorking) {
                        byte Ant = 0;

                        for(int antindex = 0; antindex < param.MaxAntennaNum && mWorking; ++antindex) {
                            int Current = 1 << antindex;
                            if ((param.Antenna & Current) == Current) {
                                byte Antx = (byte)(128 | antindex);
                                int[] pOUcharTagNum = new int[1];
                                int[] pListLen = new int[1];
                                pOUcharTagNum[0] = pListLen[0] = 0;
                                if (param.Session == 0 || param.Session == 1) {
                                    param.Target = 0;
                                    NoCardCOunt = 0;
                                }

                                if (param.IvtType == 0) {
                                    reader.Inventory_G2(param.ComAddr, (byte) param.QValue, (byte) param.Session, (byte) param.TidPtr, (byte)0, (byte) param.Target, Antx, (byte) param.ScanTime, pOUcharIDList, pOUcharTagNum, pListLen, (List)null);
                                } else if (param.IvtType == 1) {
                                    if (param.TidLen == 0) {
                                        param.TidLen = 6;
                                    }

                                    reader.Inventory_G2(param.ComAddr, (byte) param.QValue, (byte) param.Session, (byte) param.TidPtr, (byte) param.TidLen, (byte) param.Target, Antx, (byte) param.ScanTime, pOUcharIDList, pOUcharTagNum, pListLen, (List)null);
                                } else {
                                    byte MaskMem = 0;
                                    byte[] MaskAdr = new byte[2];
                                    byte MaskLen = 0;
                                    byte[] MaskData = new byte[100];
                                    byte MaskFlag = 0;
                                    byte[] ReadAdr = new byte[]{(byte)(param.TidPtr >> 8), (byte)(param.TidPtr & 255)};
                                    byte ReadLen = (byte) param.TidLen;
                                    byte[] Pwd = reader.hexStringToBytes(param.Password);
                                  reader.Inventory_Mix(param.ComAddr, (byte) param.QValue, (byte) param.Session, MaskMem, MaskAdr, MaskLen, MaskData, MaskFlag, (byte) param.Memory, ReadAdr, ReadLen, Pwd, (byte) param.Target, Antx, (byte) param.ScanTime, pOUcharIDList, pOUcharTagNum, pListLen);
                                }

                                SystemClock.sleep(5L);
                                if (pOUcharTagNum[0] == 0) {
                                    isSound = false;
                                    if ((param.Session == 2 || param.Session == 3) && param.reTryCount > 0) {

                                      NoCardCOunt = NoCardCOunt + 1;
                                        if (NoCardCOunt > param.reTryCount) {
                                            param.Target = (byte)(1 - param.Target);
                                            NoCardCOunt = 0;
                                        }
                                    }
                                } else {
                                    isSound = true;
                                    NoCardCOunt = 0;
                                }
                            }
                        }
                    }

                    isSound = false;
                    mThread = null;
                    if (callback != null) {
                        callback.ReadOver();
                    }

                }
            });
            this.mThread.start();
            return 0;
        } else {
            return -1;
        }
    }

    public void StopRead() {
        if (this.mThread != null) {
            this.mWorking = false;
            this.reader.StopInventory(this.param.ComAddr);
        }

    }

    public int Inventory(byte session, byte qvalue, byte tidAddr, byte tidLen, byte antenna, byte target, byte scantime, List<ReadTag> tagList) {
        int[] pOUcharTagNum = new int[1];
        int[] pListLen = new int[1];
        return this.reader.Inventory_G2(this.param.ComAddr, qvalue, session, tidAddr, tidLen, target, antenna, scantime, this.pOUcharIDList, pOUcharTagNum, pListLen, tagList);
    }
}
