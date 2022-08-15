package com.techjh.trans2000;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

import android_serialport_api.SerialPort;

public class MessageTran {
    private InputStream mInStream = null;
    private OutputStream mOutStream = null;
    private SerialPort mSerialPort = null;
    private boolean connected = false;
    private int mType = 0;
    private UhfTcpReader mtcp = new UhfTcpReader();

    public MessageTran(int mType) {
        this.mType = mType;
    }

    public boolean isOpen() {
        return this.mType == 0 ? this.connected : this.mtcp.connected();
    }

    public int openCom(String ComPort, int BaudRate) {
        try {
            this.mSerialPort = new SerialPort(new File(ComPort), BaudRate, 0);
        } catch (SecurityException var4) {
        } catch (IOException var5) {
        } catch (InvalidParameterException var6) {
        }

        if (this.mSerialPort != null) {
            this.mInStream = this.mSerialPort.getInputStream();
            this.mOutStream = this.mSerialPort.getOutputStream();
            this.connected = true;
            return 0;
        } else {
            return -1;
        }
    }

    public int closeCom() {
        if (this.mInStream != null) {
            try {
                this.mInStream.close();
                this.mOutStream.close();
                this.mSerialPort.close();
                this.mSerialPort = null;
            } catch (IOException var2) {
                var2.printStackTrace();
            }
        }

        this.connected = false;
        return 0;
    }

    public int openTcp(String ip, int port) {
        return this.mtcp.connect(ip, port);
    }

    public int CloseTcp() {
        return this.mtcp.disconnect();
    }

    public byte[] Read() {
        if (this.mType == 0) {
            if (!this.connected) {
                return null;
            } else {
                try {
                    int count = this.mInStream.available();
                    if (count > 0) {
                        byte[] RecvBuff = new byte[count];
                        int len = this.mInStream.read(RecvBuff);
                        if (len > 0) {
                            byte[] buff = new byte[len];
                            System.arraycopy(RecvBuff, 0, buff, 0, len);
                            return buff;
                        }
                    }
                } catch (IOException var5) {
                    var5.printStackTrace();
                }

                return null;
            }
        } else {
            return !this.mtcp.connected() ? null : this.mtcp.read();
        }
    }

    public int Write(byte[] buffer, int len) {
        if (buffer.length < len) {
            return -1;
        } else {
            byte[] cmd;
            if (this.mType == 0) {
                if (!this.connected) {
                    return -1;
                } else {
                    try {
                        cmd = new byte[len];
                        System.arraycopy(buffer, 0, cmd, 0, cmd.length);
                        this.mOutStream.write(cmd);
                        return 0;
                    } catch (IOException var4) {
                        var4.printStackTrace();
                        return -1;
                    }
                }
            } else if (!this.mtcp.connected()) {
                return -1;
            } else {
                cmd = new byte[len];
                System.arraycopy(buffer, 0, cmd, 0, cmd.length);
                return this.mtcp.write(cmd);
            }
        }
    }
}