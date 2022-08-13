package com.linde.trans2000;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class UhfTcpReader {
    private String reader_ip = "192.168.0.250";
    private int reader_port = 27011;
    private byte reader_adr = 0;
    public boolean debug_log = true;
    private UhfTcpClient tcpClient = null;

    public UhfTcpReader() {
        this.tcpClient = new UhfTcpClient();
    }

    public int connect(String ip, int port) {
        this.reader_ip = ip;
        this.reader_port = port;
        return this.tcpClient.connect(this.reader_ip, this.reader_port);
    }

    public int disconnect() {
        return this.tcpClient.disconnect();
    }

    public boolean connected() {
        return this.tcpClient.connected();
    }

    public int write(byte[] cmd) {
        return this.tcpClient.sendData(cmd);
    }

    public byte[] read() {
        return this.tcpClient.readData();
    }

    private class UhfTcpClient {
        private Socket socket;
        private OutputStream os;
        private InputStream is;

        public UhfTcpClient() {
        }

        public int connect(String ip, int port) {
            try {
                this.socket = new Socket(ip, port);
                this.socket.setSoTimeout(5000);
                this.os = this.socket.getOutputStream();
                this.is = this.socket.getInputStream();
                return 0;
            } catch (Exception var4) {
                var4.printStackTrace();
                return 48;
            }
        }

        public int disconnect() {
            try {
                if (this.socket != null) {
                    this.socket.close();
                }

                if (this.is != null) {
                    this.is.close();
                }

                if (this.os != null) {
                    this.os.close();
                }

                return 0;
            } catch (Exception var2) {
                var2.printStackTrace();
                return 48;
            }
        }

        public boolean connected() {
            return this.socket != null && !this.socket.isClosed();
        }

        public byte sendData(byte[] data) {
            try {
                this.os.write(data);
                return 0;
            } catch (Exception var3) {
                var3.printStackTrace();
                return -1;
            }
        }

        public byte[] readData() {
            try {
                byte[] buffer = new byte[256];
                int length = this.is.read(buffer);
                if (length > 0) {
                    byte[] result = new byte[length];
                    System.arraycopy(buffer, 0, result, 0, length);
                    return result;
                }
            } catch (Exception var4) {
                var4.printStackTrace();
            }

            return null;
        }
    }
}
