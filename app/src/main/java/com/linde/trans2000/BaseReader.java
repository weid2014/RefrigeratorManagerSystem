package com.linde.trans2000;

import android.os.SystemClock;
import android.util.Log;


import java.util.List;

public class BaseReader {
    private MessageTran msg = null;
    private long maxScanTime = 2000L;
    private int[] recvLength = new int[1];
    private byte[] recvBuff = new byte[20000];
    private int logswitch = 0;
    private TagCallback callback;
    private int mType = 0;
    private int MaxAntennaNum = 4;
    String DevName;
    private int lastPacket = 0;
    private String strEPC = "";

    public BaseReader(int mType, String DevName) {
        this.msg = new MessageTran(mType);
        this.mType = mType;
        this.MaxAntennaNum = 4;
        this.DevName = DevName;
    }

    private void getCRC(byte[] data, int Len) {
        try {
            int current_crc_value = 65535;

            int i;
            for(i = 0; i < Len; ++i) {
                current_crc_value ^= data[i] & 255;

                for(int j = 0; j < 8; ++j) {
                    if ((current_crc_value & 1) != 0) {
                        current_crc_value = current_crc_value >> 1 ^ '萈';
                    } else {
                        current_crc_value >>= 1;
                    }
                }
            }

            data[i++] = (byte)(current_crc_value & 255);
            data[i] = (byte)(current_crc_value >> 8 & 255);
        } catch (Exception var6) {
        }

    }

    private boolean CheckCRC(byte[] data, int len) {
        try {
            byte[] daw = new byte[256];
            System.arraycopy(data, 0, daw, 0, len);
            this.getCRC(daw, len);
            return daw[len + 1] == 0 && daw[len] == 0;
        } catch (Exception var4) {
            return false;
        }
    }

    public String bytesToHexString(byte[] src, int offset, int length) {
        StringBuilder stringBuilder = new StringBuilder("");

        try {
            if (src != null && src.length > 0) {
                for(int i = offset; i < length; ++i) {
                    int v = src[i] & 255;
                    String hv = Integer.toHexString(v);
                    if (hv.length() == 1) {
                        stringBuilder.append(0);
                    }

                    stringBuilder.append(hv);
                }

                return stringBuilder.toString().toUpperCase();
            } else {
                return null;
            }
        } catch (Exception var8) {
            return null;
        }
    }

    public byte[] hexStringToBytes(String hexString) {
        try {
            if (hexString != null && !hexString.equals("")) {
                hexString = hexString.toUpperCase();
                int length = hexString.length() / 2;
                char[] hexChars = hexString.toCharArray();
                byte[] d = new byte[length];

                for(int i = 0; i < length; ++i) {
                    int pos = i * 2;
                    d[i] = (byte)(this.charToByte(hexChars[pos]) << 4 | this.charToByte(hexChars[pos + 1]));
                }

                return d;
            } else {
                return null;
            }
        } catch (Exception var7) {
            return null;
        }
    }

    private byte charToByte(char c) {
        return (byte)"0123456789ABCDEF".indexOf(c);
    }

    public void SetCallBack(TagCallback callback) {
        this.callback = callback;
    }

    public int Connect(String ComPort, int BaudRate, int mType) {
        this.logswitch = 1;
        return mType == 0 ? this.msg.openCom(ComPort, BaudRate) : this.msg.openTcp(ComPort, BaudRate);
    }

    public int DisConnect() {
        return this.mType == 0 ? this.msg.closeCom() : this.msg.CloseTcp();
    }

    public int SendCMD(byte[] CMD) {
        if (this.logswitch == 1) {
            Log.d("Send", this.bytesToHexString(CMD, 0, (CMD[0] & 255) + 1));
        }

        return this.msg.Write(CMD, (CMD[0] & 255) + 1);
    }

    private int GetCMDData(byte[] data, int[] Nlen, int cmd, int endTime) {
        int Count = 0;
        byte[] btArray = new byte[2000];
        int btLength = 0;
        long beginTime = System.currentTimeMillis();

        try {
            while(System.currentTimeMillis() - beginTime < (long)endTime) {
                try {
                    Thread.sleep(10L);
                } catch (InterruptedException var15) {
                    var15.printStackTrace();
                }

                byte[] buffer = this.msg.Read();
                if (buffer != null) {
                    if (this.logswitch == 1) {
                        Log.d("Recv", this.bytesToHexString(buffer, 0, buffer.length));
                    }

                     Count = buffer.length;
                    if (Count != 0) {
                        byte[] daw = new byte[Count + btLength];
                        System.arraycopy(btArray, 0, daw, 0, btLength);
                        System.arraycopy(buffer, 0, daw, btLength, Count);
                        int index = 0;

                        while(daw.length - index > 4) {
                            if ((daw[index] & 255) >= 4 && ((daw[index + 2] & 255) == cmd || (daw[index + 2] & 255) == 0)) {
                                int len = daw[index] & 255;
                                if (daw.length < index + len + 1) {
                                    break;
                                }

                                byte[] epcArr = new byte[len + 1];
                                System.arraycopy(daw, index, epcArr, 0, epcArr.length);
                                if (this.CheckCRC(epcArr, epcArr.length)) {
                                    System.arraycopy(epcArr, 0, data, 0, epcArr.length);
                                    Nlen[0] = epcArr.length + 1;
                                    return 0;
                                }

                                ++index;
                            } else {
                                ++index;
                            }
                        }

                        if (daw.length > index) {
                            btLength = daw.length - index;
                            System.arraycopy(daw, index, btArray, 0, btLength);
                        } else {
                            btLength = 0;
                        }
                    }
                }
            }
        } catch (Exception var16) {
            var16.toString();
        }

        return 48;
    }

    private int GetInventoryData(byte ComAddr, int cmd, byte[] epcdata, int[] epcNum, int[] dlen, List<ReadTag> tagList) {
        epcNum[0] = 0;
        dlen[0] = 0;
        int Count = 0;
        byte[] btArray = new byte[2000];
        int btLength = 0;
        long beginTime = System.currentTimeMillis();

        try {
            do {
                byte[] buffer = this.msg.Read();
                if (buffer == null) {
                    SystemClock.sleep(5L);
                } else {
                    if (this.logswitch == 1) {
                        Log.d("Recv", this.bytesToHexString(buffer, 0, buffer.length));
                    }

                     Count = buffer.length;
                    if (Count != 0) {
                        byte[] daw = new byte[Count + btLength];
                        System.arraycopy(btArray, 0, daw, 0, btLength);
                        System.arraycopy(buffer, 0, daw, btLength, Count);
                        int index = 0;

                        while(daw.length - index > 5) {
                            if ((ComAddr & 255) == 255) {
                                ComAddr = 0;
                            }

                            if ((daw[index] & 255) >= 5 && daw[index + 1] == ComAddr && (daw[index + 2] & 255) == cmd) {
                                int len = daw[index] & 255;
                                if (daw.length >= index + len + 1) {
                                    byte[] epcArr = new byte[len + 1];
                                    System.arraycopy(daw, index, epcArr, 0, epcArr.length);
                                    if (this.CheckCRC(epcArr, epcArr.length)) {
                                        int nLen = (epcArr[0] & 255) + 1;
                                        index += nLen;
                                        int status = epcArr[3] & 255;
                                        if (status != 1 && status != 2 && status != 3 && status != 4) {
                                            if (this.callback != null) {
                                                this.callback.tagCallbackFailed(status);
                                            }

                                            return status;
                                        }

                                        int num = epcArr[5] & 255;
                                        if (num > 0) {
                                            int epclen = (epcArr[0] & 255) - 7;
                                            System.arraycopy(epcArr, 6, epcdata, dlen[0], epclen);
                                            int var10002 = epcNum[0]++;
                                            dlen[0] += epclen;
                                            ReadTag tag = new ReadTag();
                                            int curant = epcArr[4] & 255;
                                            if (this.MaxAntennaNum < 16) {
                                                switch(curant) {
                                                    case 1:
                                                        tag.antId = 1;
                                                        break;
                                                    case 2:
                                                        tag.antId = 2;
                                                        break;
                                                    case 4:
                                                        tag.antId = 3;
                                                        break;
                                                    case 8:
                                                        tag.antId = 4;
                                                        break;
                                                    case 16:
                                                        tag.antId = 5;
                                                        break;
                                                    case 32:
                                                        tag.antId = 6;
                                                        break;
                                                    case 64:
                                                        tag.antId = 7;
                                                        break;
                                                    case 128:
                                                        tag.antId = 8;
                                                }
                                            } else {
                                                tag.antId = curant + 1;
                                            }

                                            epclen = epcArr[6] & 255;
                                            byte[] btArr = new byte[epclen];
                                            System.arraycopy(epcArr, 7, btArr, 0, btArr.length);
                                            tag.epcId = this.bytesToHexString(btArr, 0, btArr.length);
                                            tag.rssi = epcArr[7 + epclen] & 255;
                                            tag.DevName = this.DevName;
                                            if (tagList != null) {
                                                tagList.add(tag);
                                            }

                                            if (this.callback != null) {
                                                this.callback.tagCallback(tag);
                                            }
                                        }

                                        if (status == 1 || status == 2) {
                                            if (epcNum[0] > 0) {
                                                return 0;
                                            }

                                            return 1;
                                        }
                                    } else {
                                        ++index;
                                    }
                                    continue;
                                }
                                break;
                            } else {
                                ++index;
                            }
                        }

                        if (daw.length > index) {
                            btLength = daw.length - index;
                            System.arraycopy(daw, index, btArray, 0, btLength);
                        } else {
                            btLength = 0;
                        }
                    }
                }
            } while(System.currentTimeMillis() - beginTime < this.maxScanTime * 2L + 10000L);
        } catch (Exception var24) {
            var24.toString();
        }

        if (this.callback != null) {
            this.callback.tagCallbackFailed(48);
        }

        return 48;
    }

    private int GetInventoryMixData(byte ComAddr, int cmd, byte[] epcdata, int[] epcNum, int[] dlen) {
        epcNum[0] = 0;
        dlen[0] = 0;
        int Count = 0;
        byte[] btArray = new byte[2000];
        int btLength = 0;
        long beginTime = System.currentTimeMillis();

        while(true) {
            try {
                byte[] buffer = this.msg.Read();
                if (buffer == null) {
                    SystemClock.sleep(5L);
                } else {
                     Count = buffer.length;
                    if (Count != 0) {
                        byte[] daw = new byte[Count + btLength];
                        System.arraycopy(btArray, 0, daw, 0, btLength);
                        System.arraycopy(buffer, 0, daw, btLength, Count);
                        int index = 0;

                        label99:
                        while(true) {
                            while(true) {
                                if (daw.length - index > 5) {
                                    if ((daw[index] & 255) < 5 || (daw[index + 2] & 255) != cmd) {
                                        ++index;
                                        continue;
                                    }

                                    int len = daw[index] & 255;
                                    if (daw.length >= index + len + 1) {
                                        byte[] epcArr = new byte[len + 1];
                                        System.arraycopy(daw, index, epcArr, 0, epcArr.length);
                                        if (this.CheckCRC(epcArr, epcArr.length)) {
                                            int nLen = (epcArr[0] & 255) + 1;
                                            index += nLen;
                                            int status = epcArr[3] & 255;
                                            if (status != 1 && status != 2 && status != 3 && status != 4) {
                                                if (this.callback != null) {
                                                    this.callback.tagCallbackFailed(status);
                                                }

                                                return status;
                                            }

                                            int num = epcArr[5] & 255;
                                            if (num > 0) {
                                                int m = 6;
                                                int PacketParam = epcArr[m] & 255;
                                                int epcfullen = epcArr[m + 1] & 255;
                                                int epclen = epcArr[m + 1] & 255 & 127;
                                                byte[] uid = new byte[epclen];
                                                System.arraycopy(epcArr, m + 2, uid, 0, epclen);
                                                String strMem = "";
                                                if (PacketParam < 128) {
                                                    this.strEPC = this.bytesToHexString(uid, 0, uid.length);
                                                    this.lastPacket = PacketParam;
                                                } else if (this.lastPacket == (PacketParam & 127) - 1 || this.lastPacket == 127 && (PacketParam & 127) == 0) {
                                                    strMem = this.bytesToHexString(uid, 0, uid.length);
                                                    ReadTag tag = new ReadTag();
                                                    tag.antId = epcArr[4] & 255;
                                                    tag.epcId = this.strEPC + "--" + strMem;
                                                    tag.rssi = epcArr[8 + epclen] & 255;
                                                    tag.DevName = this.DevName;
                                                    if (this.callback != null) {
                                                        this.callback.tagCallback(tag);
                                                    }

                                                    this.strEPC = "";
                                                }
                                            }

                                            if (status != 1 && status != 2) {
                                                continue;
                                            }

                                            if (epcNum[0] > 0) {
                                                return 0;
                                            }

                                            return 1;
                                        }

                                        ++index;
                                        continue;
                                    }
                                }

                                if (daw.length > index) {
                                    btLength = daw.length - index;
                                    System.arraycopy(daw, index, btArray, 0, btLength);
                                } else {
                                    btLength = 0;
                                }
                                break label99;
                            }
                        }
                    }
                }

                if (System.currentTimeMillis() - beginTime < this.maxScanTime * 2L + 10000L) {
                    continue;
                }
            } catch (Exception var26) {
                var26.toString();
            }

            if (this.callback != null) {
                this.callback.tagCallbackFailed(48);
            }

            return 48;
        }
    }

    public int GetReaderInformation(byte[] ComAddr, byte[] TVersionInfo, byte[] ReaderType, byte[] TrType, byte[] band, byte[] dmaxfre, byte[] dminfre, byte[] powerdBm, byte[] ScanTime, byte[] Ant, byte[] BeepEn, byte[] OutputRep, byte[] CheckAnt) {
        byte[] buffer = new byte[]{4, ComAddr[0], 33, 0, 0};
        this.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 33, 1000);
        if (result == 0) {
            ComAddr[0] = this.recvBuff[1];
            TVersionInfo[0] = this.recvBuff[4];
            TVersionInfo[1] = this.recvBuff[5];
            ReaderType[0] = this.recvBuff[6];
            TrType[0] = this.recvBuff[7];
            dmaxfre[0] = (byte)(this.recvBuff[8] & 63);
            dminfre[0] = (byte)(this.recvBuff[9] & 63);
            band[0] = (byte)((this.recvBuff[8] & 192) >> 4 | (this.recvBuff[9] & 192) >> 6);
            powerdBm[0] = this.recvBuff[10];
            ScanTime[0] = this.recvBuff[11];
            this.maxScanTime = (long)((ScanTime[0] & 255) * 100);
            Ant[0] = this.recvBuff[12];
            BeepEn[0] = this.recvBuff[13];
            OutputRep[0] = this.recvBuff[14];
            CheckAnt[0] = this.recvBuff[15];
            switch(ReaderType[0] & 255) {
                case 38:
                    this.MaxAntennaNum = 8;
                    break;
                case 39:
                    this.MaxAntennaNum = 16;
                    break;
                default:
                    this.MaxAntennaNum = 4;
            }

            return 0;
        } else {
            return 48;
        }
    }

    public int SetInventoryScanTime(byte ComAddr, byte ScanTime) {
        byte[] buffer = new byte[]{5, ComAddr, 37, ScanTime, 0, 0};
        this.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 37, 500);
        return result == 0 ? this.recvBuff[3] & 255 : 48;
    }

    public int Inventory_G2(byte ComAddr, byte QValue, byte Session, byte AdrTID, byte LenTID, byte Target, byte Ant, byte Scantime, byte[] pOUcharIDList, int[] pOUcharTagNum, int[] pListLen, List<ReadTag> tagList) {
        byte[] buffer;
        if (LenTID == 0) {
            buffer = new byte[]{9, ComAddr, 1, QValue, Session, Target, Ant, Scantime, 0, 0};
        } else {
            buffer = new byte[]{11, ComAddr, 1, QValue, Session, AdrTID, LenTID, Target, Ant, Scantime, 0, 0};
        }

        this.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        return this.GetInventoryData(ComAddr, 1, pOUcharIDList, pOUcharTagNum, pListLen, tagList);
    }

    public int Inventory_Mix(byte ComAddr, byte QValue, byte Session, byte MaskMem, byte[] MaskAdr, byte MaskLen, byte[] MaskData, byte MaskFlag, byte ReadMem, byte[] ReadAdr, byte ReadLen, byte[] Pwd, byte Target, byte Ant, byte Scantime, byte[] pOUcharIDList, int[] pOUcharTagNum, int[] pListLen) {
        byte[] buffer = new byte[18];
        if (MaskFlag == 0) {
            buffer = new byte[]{17, ComAddr, 25, QValue, Session, ReadMem, ReadAdr[0], ReadAdr[1], ReadLen, Pwd[0], Pwd[1], Pwd[2], Pwd[3], Target, Ant, Scantime, 0, 0};
        } else {
            int len = (MaskLen + 7) / 8;
            buffer = new byte[22 + len];
            buffer[0] = (byte)(21 + len);
            buffer[1] = ComAddr;
            buffer[2] = 25;
            buffer[3] = QValue;
            buffer[4] = Session;
            buffer[5] = MaskMem;
            buffer[6] = MaskAdr[0];
            buffer[7] = MaskAdr[1];
            buffer[8] = MaskLen;
            if (len > 0) {
                System.arraycopy(MaskData, 0, buffer, 9, len);
            }

            buffer[9 + len] = ReadMem;
            buffer[10 + len] = ReadAdr[0];
            buffer[11 + len] = ReadAdr[1];
            buffer[12 + len] = ReadLen;
            buffer[13 + len] = Pwd[0];
            buffer[14 + len] = Pwd[1];
            buffer[15 + len] = Pwd[2];
            buffer[16 + len] = Pwd[3];
            buffer[17 + len] = Target;
            buffer[18 + len] = Ant;
            buffer[19 + len] = Scantime;
        }

        this.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        return this.GetInventoryMixData(ComAddr, 25, pOUcharIDList, pOUcharTagNum, pListLen);
    }

    public int SetRfPower(byte ComAddr, byte power) {
        byte[] buffer = new byte[]{5, ComAddr, 47, power, 0, 0};
        this.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 47, 1000);
        return result == 0 ? this.recvBuff[3] & 255 : 48;
    }

    public int SelectCMD(byte ComAddr, byte ant, byte Session, byte Selaction, byte Truncate) {
        byte[] buffer = new byte[]{12, ComAddr, -102, ant, Session, Selaction, 1, 0, 0, 0, Truncate, 0, 0};
        this.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 154, 500);
        return result == 0 ? this.recvBuff[3] & 255 : 48;
    }

    public int SelectCMD(byte ComAddr, int ant, byte Session, byte Selaction, byte Truncate) {
        byte[] buffer = new byte[]{13, ComAddr, -102, (byte)(ant >> 8), (byte)(ant & 255), Session, Selaction, 1, 0, 0, 0, Truncate, 0, 0};
        this.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 154, 500);
        return result == 0 ? this.recvBuff[3] & 255 : 48;
    }

    public int BuzzerAndLEDControl(byte ComAddr, byte AvtiveTime, byte SilentTime, byte Times) {
        byte[] buffer = new byte[]{7, ComAddr, 51, AvtiveTime, SilentTime, Times, 0, 0};
        this.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 51, 1000);
        return result == 0 ? this.recvBuff[3] & 255 : 48;
    }

    public int SetRfPowerByAnt(byte ComAddr, byte[] Power) {
        if (Power.length != 4 && Power.length != 8 && Power.length != 16) {
            return 255;
        } else {
            byte[] buffer = new byte[5 + Power.length];
            buffer[0] = (byte)(4 + Power.length);
            buffer[1] = ComAddr;
            buffer[2] = 47;
            System.arraycopy(Power, 0, buffer, 3, Power.length);
            this.getCRC(buffer, buffer[0] - 1);
            this.SendCMD(buffer);
            int result = this.GetCMDData(this.recvBuff, this.recvLength, 47, 1000);
            return result == 0 ? this.recvBuff[3] & 255 : 48;
        }
    }

    public byte[] GetRfPowerByAnt(byte ComAddr) {
        byte[] buffer = new byte[]{4, ComAddr, -108, 0, 0};
        this.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 148, 1000);
        if (result == 0) {
            byte[] Power = new byte[this.recvBuff[0] - 5];
            if (this.recvBuff[3] == 0) {
                System.arraycopy(this.recvBuff, 4, Power, 0, this.recvBuff[0] - 5);
            }

            return Power;
        } else {
            return null;
        }
    }

    public int SetAddress(byte ComAddr, byte newAddr) {
        byte[] buffer = new byte[]{5, ComAddr, 36, newAddr, 0, 0};
        this.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 36, 1000);
        return result == 0 ? this.recvBuff[3] & 255 : 48;
    }

    public int SetRegion(byte ComAddr, int band, int maxfre, int minfre) {
        byte[] buffer = new byte[]{6, ComAddr, 34, (byte)((band & 12) << 4 | maxfre & 63), (byte)((band & 3) << 6 | minfre & 63), 0, 0};
        this.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 34, 1000);
        return result == 0 ? this.recvBuff[3] & 255 : 48;
    }

    public int SetAntennaMultiplexing(byte ComAddr, byte AntCfg) {
        byte[] buffer = new byte[]{5, ComAddr, 63, AntCfg, 0, 0};
        this.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 63, 1000);
        return result == 0 ? this.recvBuff[3] & 255 : 48;
    }

    public int SetAntennaMultiplexing(byte ComAddr, byte SetOnce, byte AntCfg1, byte AntCfg2) {
        byte[] buffer = new byte[]{7, ComAddr, 63, SetOnce, AntCfg1, AntCfg2, 0, 0};
        this.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 63, 1000);
        return result == 0 ? this.recvBuff[3] & 255 : 48;
    }

    public int ConfigDRM(byte ComAddr, byte[] DRM) {
        byte[] buffer = new byte[]{5, ComAddr, -112, DRM[0], 0, 0};
        this.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 144, 1000);
        if (result == 0) {
            if (this.recvBuff[3] == 0) {
                DRM[0] = this.recvBuff[4];
            }

            return this.recvBuff[3] & 255;
        } else {
            return 48;
        }
    }

    public int SetRelay(byte ComAddr, byte RelayTime) {
        byte[] buffer = new byte[]{5, ComAddr, 69, RelayTime, 0, 0};
        this.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 69, 1000);
        return result == 0 ? this.recvBuff[3] & 255 : 48;
    }

    public int SetGPIO(byte ComAddr, byte OutputPin) {
        byte[] buffer = new byte[]{5, ComAddr, 70, OutputPin, 0, 0};
        this.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 70, 1000);
        return result == 0 ? this.recvBuff[3] & 255 : 48;
    }

    public int GetGPIOStatus(byte ComAddr, byte[] OutputPin) {
        byte[] buffer = new byte[]{4, ComAddr, 71, 0, 0};
        this.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 71, 1000);
        if (result == 0) {
            if (this.recvBuff[3] == 0) {
                OutputPin[0] = this.recvBuff[4];
            }

            return this.recvBuff[3] & 255;
        } else {
            return 48;
        }
    }

    public int SetBeepNotification(byte ComAddr, byte BeepEn) {
        byte[] buffer = new byte[]{5, ComAddr, 64, BeepEn, 0, 0};
        this.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 64, 1000);
        return result == 0 ? this.recvBuff[3] & 255 : 48;
    }

    public int SetReal_timeClock(byte ComAddr, byte[] paramer) {
        byte[] buffer = new byte[11];
        buffer[0] = 10;
        buffer[1] = ComAddr;
        buffer[2] = 65;
        System.arraycopy(paramer, 0, buffer, 3, 6);
        this.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 65, 1000);
        return result == 0 ? this.recvBuff[3] & 255 : 48;
    }

    public int GetTime(byte ComAddr, byte[] paramer) {
        byte[] buffer = new byte[]{4, ComAddr, 66, 0};
        this.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 66, 1000);
        if (result == 0) {
            if (this.recvBuff[3] == 0) {
                System.arraycopy(this.recvBuff, 4, paramer, 0, 6);
            }

            return this.recvBuff[3] & 255;
        } else {
            return 48;
        }
    }

    public int ReadData_G2(byte ComAddr, byte ENum, byte[] EPC, byte Mem, byte WordPtr, byte Num, byte[] Password, byte MaskMem, byte[] MaskAdr, byte MaskLen, byte[] MaskData, byte MaskFlag, byte[] Data, byte[] Errorcode) {
        int mLen;
        if (MaskFlag == 0) {
            byte[] buffer = new byte[13 + ENum * 2];
            buffer[0] = (byte)(12 + ENum * 2);
            buffer[1] = ComAddr;
            buffer[2] = 2;
            buffer[3] = ENum;
            System.arraycopy(EPC, 0, buffer, 4, ENum * 2);
            buffer[ENum * 2 + 4] = Mem;
            buffer[ENum * 2 + 5] = WordPtr;
            buffer[ENum * 2 + 6] = Num;
            System.arraycopy(Password, 0, buffer, ENum * 2 + 7, 4);
            this.getCRC(buffer, buffer[0] - 1);
            this.SendCMD(buffer);
            mLen = this.GetCMDData(this.recvBuff, this.recvLength, 2, 3000);
            if (mLen == 0) {
                if (this.recvBuff[3] == 0) {
                    Errorcode[0] = 0;
                    System.arraycopy(this.recvBuff, 4, Data, 0, Num * 2);
                } else if ((this.recvBuff[3] & 255) == 252) {
                    Errorcode[0] = this.recvBuff[4];
                }

                return this.recvBuff[3] & 255;
            } else {
                return 48;
            }
        } else if (MaskLen == 0) {
            return 255;
        } else {
            mLen = MaskLen & 255;
            int maskbyte;
            if (mLen % 8 == 0) {
                maskbyte = mLen / 8;
            } else {
                maskbyte = mLen / 8 + 1;
            }

            byte[] buffer = new byte[17 + maskbyte];
            buffer[0] = (byte)(16 + maskbyte);
            buffer[1] = ComAddr;
            buffer[2] = 2;
            buffer[3] = ENum;
            if ((ENum & 255) == 255) {
                ENum = 0;
            }

            System.arraycopy(EPC, 0, buffer, 4, ENum * 2);
            buffer[ENum * 2 + 4] = Mem;
            buffer[ENum * 2 + 5] = WordPtr;
            buffer[ENum * 2 + 6] = Num;
            System.arraycopy(Password, 0, buffer, ENum * 2 + 7, 4);
            buffer[ENum * 2 + 11] = MaskMem;
            buffer[ENum * 2 + 12] = MaskAdr[0];
            buffer[ENum * 2 + 13] = MaskAdr[1];
            buffer[ENum * 2 + 14] = MaskLen;
            System.arraycopy(MaskData, 0, buffer, ENum * 2 + 15, maskbyte);
            this.getCRC(buffer, buffer[0] - 1);
            this.SendCMD(buffer);
            int result = this.GetCMDData(this.recvBuff, this.recvLength, 2, 3000);
            if (result == 0) {
                if (this.recvBuff[3] == 0) {
                    Errorcode[0] = 0;
                    System.arraycopy(this.recvBuff, 4, Data, 0, Num * 2);
                } else if ((this.recvBuff[3] & 255) == 252) {
                    Errorcode[0] = this.recvBuff[4];
                }

                return this.recvBuff[3] & 255;
            } else {
                return 48;
            }
        }
    }

    public int ExtReadData_G2(byte ComAddr, byte ENum, byte[] EPC, byte Mem, byte[] WordPtr, byte Num, byte[] Password, byte[] Data, byte[] Errorcode) {
        byte[] buffer = new byte[14 + ENum * 2];
        buffer[0] = (byte)(13 + ENum * 2);
        buffer[1] = ComAddr;
        buffer[2] = 21;
        buffer[3] = ENum;
        if ((ENum & 255) == 255) {
            ENum = 0;
        }

        System.arraycopy(EPC, 0, buffer, 4, ENum * 2);
        buffer[ENum * 2 + 4] = Mem;
        buffer[ENum * 2 + 5] = WordPtr[0];
        buffer[ENum * 2 + 6] = WordPtr[1];
        buffer[ENum * 2 + 7] = Num;
        System.arraycopy(Password, 0, buffer, ENum * 2 + 8, 4);
        this.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 21, 3000);
        if (result == 0) {
            if (this.recvBuff[3] == 0) {
                Errorcode[0] = 0;
                System.arraycopy(this.recvBuff, 4, Data, 0, Num * 2);
            } else if ((this.recvBuff[3] & 255) == 252) {
                Errorcode[0] = this.recvBuff[4];
            }

            return this.recvBuff[3] & 255;
        } else {
            return 48;
        }
    }

    public int WriteData_G2(byte ComAddr, byte WNum, byte ENum, byte[] EPC, byte Mem, byte WordPtr, byte[] Writedata, byte[] Password, byte MaskMem, byte[] MaskAdr, byte MaskLen, byte[] MaskData, byte MaskFlag, byte[] Errorcode) {
        int mLen;
        if (MaskFlag == 0) {
            byte[] buffer = new byte[13 + (ENum + WNum) * 2];
            buffer[0] = (byte)(12 + (ENum + WNum) * 2);
            buffer[1] = ComAddr;
            buffer[2] = 3;
            buffer[3] = WNum;
            buffer[4] = ENum;
            System.arraycopy(EPC, 0, buffer, 5, ENum * 2);
            buffer[ENum * 2 + 5] = Mem;
            buffer[ENum * 2 + 6] = WordPtr;
            System.arraycopy(Writedata, 0, buffer, ENum * 2 + 7, WNum * 2);
            System.arraycopy(Password, 0, buffer, ENum * 2 + WNum * 2 + 7, 4);
            this.getCRC(buffer, buffer[0] - 1);
            this.SendCMD(buffer);
            mLen = this.GetCMDData(this.recvBuff, this.recvLength, 3, 3000);
            if (mLen == 0) {
                if (this.recvBuff[3] == 0) {
                    Errorcode[0] = 0;
                } else if ((this.recvBuff[3] & 255) == 252) {
                    Errorcode[0] = this.recvBuff[4];
                }

                return this.recvBuff[3] & 255;
            } else {
                return 48;
            }
        } else if (MaskLen == 0) {
            return 255;
        } else {
            mLen = MaskLen & 255;
            int maskbyte;
            if (mLen % 8 == 0) {
                maskbyte = mLen / 8;
            } else {
                maskbyte = mLen / 8 + 1;
            }

            byte[] buffer = new byte[17 + WNum * 2 + maskbyte];
            buffer[0] = (byte)(16 + WNum * 2 + maskbyte);
            buffer[1] = ComAddr;
            buffer[2] = 3;
            buffer[3] = WNum;
            buffer[4] = ENum;
            if ((ENum & 255) == 255) {
                ENum = 0;
            }

            System.arraycopy(EPC, 0, buffer, 5, ENum * 2);
            buffer[ENum * 2 + 5] = Mem;
            buffer[ENum * 2 + 6] = WordPtr;
            System.arraycopy(Writedata, 0, buffer, ENum * 2 + 7, WNum * 2);
            System.arraycopy(Password, 0, buffer, ENum * 2 + WNum * 2 + 7, 4);
            buffer[ENum * 2 + WNum * 2 + 11] = MaskMem;
            buffer[ENum * 2 + WNum * 2 + 12] = MaskAdr[0];
            buffer[ENum * 2 + WNum * 2 + 13] = MaskAdr[1];
            buffer[ENum * 2 + WNum * 2 + 14] = MaskLen;
            System.arraycopy(MaskData, 0, buffer, ENum * 2 + WNum * 2 + 15, maskbyte);
            this.getCRC(buffer, buffer[0] - 1);
            this.SendCMD(buffer);
            int result = this.GetCMDData(this.recvBuff, this.recvLength, 3, 3000);
            if (result == 0) {
                if (this.recvBuff[3] == 0) {
                    Errorcode[0] = 0;
                } else if ((this.recvBuff[3] & 255) == 252) {
                    Errorcode[0] = this.recvBuff[4];
                }

                return this.recvBuff[3] & 255;
            } else {
                return 48;
            }
        }
    }

    public int ExtWriteData_G2(byte ComAddr, byte WNum, byte ENum, byte[] EPC, byte Mem, byte[] WordPtr, byte[] Writedata, byte[] Password, byte[] Errorcode) {
        byte[] buffer = new byte[14 + (ENum + WNum) * 2];
        buffer[0] = (byte)(13 + (ENum + WNum) * 2);
        buffer[1] = ComAddr;
        buffer[2] = 22;
        buffer[3] = WNum;
        buffer[4] = ENum;
        if ((ENum & 255) == 255) {
            ENum = 0;
        }

        System.arraycopy(EPC, 0, buffer, 5, ENum * 2);
        buffer[ENum * 2 + 5] = Mem;
        buffer[ENum * 2 + 6] = WordPtr[0];
        buffer[ENum * 2 + 7] = WordPtr[1];
        System.arraycopy(Writedata, 0, buffer, ENum * 2 + 8, WNum * 2);
        System.arraycopy(Password, 0, buffer, ENum * 2 + WNum * 2 + 9, 4);
        this.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 22, 3000);
        if (result == 0) {
            if (this.recvBuff[3] == 0) {
                Errorcode[0] = 0;
            } else if ((this.recvBuff[3] & 255) == 252) {
                Errorcode[0] = this.recvBuff[4];
            }

            return this.recvBuff[3] & 255;
        } else {
            return 48;
        }
    }

    public int WriteEPC_G2(byte ComAddr, byte ENum, byte[] Password, byte[] WriteEPC, byte[] Errorcode) {
        byte[] buffer = new byte[10 + ENum * 2];
        buffer[0] = (byte)(9 + ENum * 2);
        buffer[1] = ComAddr;
        buffer[2] = 4;
        buffer[3] = ENum;
        System.arraycopy(Password, 0, buffer, 4, 4);
        System.arraycopy(WriteEPC, 0, buffer, 8, ENum * 2);
        this.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 4, 2000);
        if (result == 0) {
            if (this.recvBuff[3] == 0) {
                Errorcode[0] = 0;
            } else if ((this.recvBuff[3] & 255) == 252) {
                Errorcode[0] = this.recvBuff[4];
            }

            return this.recvBuff[3] & 255;
        } else {
            return 48;
        }
    }

    public int Lock_G2(byte ComAddr, byte ENum, byte[] EPC, byte select, byte setprotect, byte[] Password, byte[] Errorcode) {
        byte[] buffer = new byte[12 + ENum * 2];
        buffer[0] = (byte)(11 + ENum * 2);
        buffer[1] = ComAddr;
        buffer[2] = 6;
        buffer[3] = ENum;
        System.arraycopy(EPC, 0, buffer, 4, ENum * 2);
        buffer[ENum * 2 + 4] = select;
        buffer[ENum * 2 + 5] = setprotect;
        System.arraycopy(Password, 0, buffer, ENum * 2 + 6, 4);
        this.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 6, 1000);
        if (result == 0) {
            if (this.recvBuff[3] == 0) {
                Errorcode[0] = 0;
            } else if ((this.recvBuff[3] & 255) == 252) {
                Errorcode[0] = this.recvBuff[4];
            }

            return this.recvBuff[3] & 255;
        } else {
            return 48;
        }
    }

    public int Kill_G2(byte ComAddr, byte ENum, byte[] EPC, byte[] Password, byte[] Errorcode) {
        byte[] buffer = new byte[10 + ENum * 2];
        buffer[0] = (byte)(9 + ENum * 2);
        buffer[1] = ComAddr;
        buffer[2] = 5;
        buffer[3] = ENum;
        System.arraycopy(EPC, 0, buffer, 4, ENum * 2);
        System.arraycopy(Password, 0, buffer, ENum * 2 + 4, 4);
        this.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 5, 1000);
        if (result == 0) {
            if (this.recvBuff[3] == 0) {
                Errorcode[0] = 0;
            } else if ((this.recvBuff[3] & 255) == 252) {
                Errorcode[0] = this.recvBuff[4];
            }

            return this.recvBuff[3] & 255;
        } else {
            return 48;
        }
    }

    public int MeasureReturnLoss(byte ComAddr, byte[] TestFreq, byte Ant, byte[] ReturnLoss) {
        byte[] buffer = new byte[10];
        buffer[0] = 9;
        buffer[1] = ComAddr;
        buffer[2] = -111;
        System.arraycopy(TestFreq, 0, buffer, 3, 4);
        buffer[7] = Ant;
        this.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 145, 1000);
        if (result == 0) {
            if (this.recvBuff[3] == 0) {
                ReturnLoss[0] = this.recvBuff[4];
            }

            return this.recvBuff[3] & 255;
        } else {
            return 48;
        }
    }

    public int SetCheckAnt(byte ComAddr, byte CheckAnt) {
        byte[] buffer = new byte[]{5, ComAddr, 102, CheckAnt, 0, 0};
        this.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 102, 1000);
        return result == 0 ? this.recvBuff[3] & 255 : 48;
    }

    public int SetWritePower(byte ComAddr, byte WritePower) {
        byte[] buffer = new byte[]{5, ComAddr, 121, WritePower, 0, 0};
        this.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 121, 1000);
        return result == 0 ? this.recvBuff[3] & 255 : 48;
    }

    public int GetWritePower(byte ComAddr, byte[] WritePower) {
        byte[] buffer = new byte[]{4, ComAddr, 122, 0, 0};
        this.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 122, 1000);
        if (result == 0) {
            if (this.recvBuff[3] == 0) {
                WritePower[0] = this.recvBuff[4];
            }

            return this.recvBuff[3] & 255;
        } else {
            return 48;
        }
    }

    public int RetryTimes(byte ComAddr, byte[] Times) {
        byte[] buffer = new byte[]{5, ComAddr, 123, Times[0], 0, 0};
        this.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 123, 1000);
        if (result == 0) {
            if (this.recvBuff[3] == 0) {
                Times[0] = this.recvBuff[4];
            }

            return this.recvBuff[3] & 255;
        } else {
            return 48;
        }
    }

    public int SetReadParameter(byte ComAddr, byte[] Parameter) {
        byte[] buffer = new byte[]{9, ComAddr, 117, Parameter[0], Parameter[1], Parameter[2], Parameter[3], Parameter[4], 0, 0};
        this.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 117, 500);
        return result == 0 ? this.recvBuff[3] & 255 : 48;
    }

    public int GetReadParameter(byte ComAddr, byte[] Parameter) {
        byte[] buffer = new byte[]{4, ComAddr, 119, 0, 0};
        this.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 119, 1000);
        if (result == 0) {
            System.arraycopy(this.recvBuff, 4, Parameter, 0, 6);
            return this.recvBuff[3] & 255;
        } else {
            return 48;
        }
    }

    public int SetReadMode(byte ComAddr, byte ReadMode) {
        byte[] buffer = new byte[]{5, ComAddr, 118, ReadMode, 0, 0};
        this.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 118, 1000);
        return result == 0 ? this.recvBuff[3] & 255 : 48;
    }

    public int GetDeviceID(byte ComAddr, byte[] DeviceID) {
        byte[] buffer = new byte[]{4, ComAddr, 76, 0, 0};
        this.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
        int result = this.GetCMDData(this.recvBuff, this.recvLength, 76, 1000);
        if (result == 0) {
            if (this.recvBuff[3] == 0) {
                System.arraycopy(this.recvBuff, 4, DeviceID, 0, 4);
            }

            return this.recvBuff[3] & 255;
        } else {
            return 48;
        }
    }

    public void StopInventory(byte ComAddr) {
        byte[] buffer = new byte[]{4, ComAddr, -109, 0, 0};
        this.getCRC(buffer, buffer[0] - 1);
        this.SendCMD(buffer);
    }
}
