package com.techjh.trans2000;

import java.io.File;
import java.io.FileOutputStream;

public class Utils {
    public Utils() {
    }

    public static void saveFile(String filename, byte[] data) throws Exception {
        if (data != null) {
            String filepath = "D:\\" + filename;
            File file = new File(filepath);
            if (file.exists()) {
                file.delete();
            }

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data, 0, data.length);
            fos.flush();
            fos.close();
        }

    }

    public static byte[] hexStringToBytes(String hexString) {
        if (hexString != null && !hexString.equals("")) {
            hexString = hexString.toUpperCase();
            int length = hexString.length() / 2;
            char[] hexChars = hexString.toCharArray();
            byte[] d = new byte[length];

            for(int i = 0; i < length; ++i) {
                int pos = i * 2;
                d[i] = (byte)(charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
            }

            return d;
        } else {
            return null;
        }
    }

    public static byte hexStringToByte(String hexString) {
        if (hexString != null && !hexString.equals("")) {
            hexString = hexString.toUpperCase();
            int length = hexString.length() / 2;
            char[] hexChars = hexString.toCharArray();
            byte[] d = new byte[length];
            int i = 0;
            if (i < length) {
                int pos = i * 2;
                d[i] = (byte)(charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
                return d[i];
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    private static byte charToByte(char c) {
        return (byte)"0123456789ABCDEF".indexOf(c);
    }

    public static String bytes2hex02(byte[] bytes) {
        return bytes2hex02_(bytes, "");
    }

    public static String byte2hex02(byte b) {
        String tmp = Integer.toHexString(255 & b);
        if (tmp.length() == 1) {
            tmp = "0" + tmp;
        }

        return tmp;
    }

    public static String bytes2hex02_(byte[] bytes, String space) {
        StringBuilder sb = new StringBuilder();
        String tmp = null;
        byte[] var7 = bytes;
        int var6 = bytes.length;

        for(int var5 = 0; var5 < var6; ++var5) {
            byte b = var7[var5];
            tmp = byte2hex02(b);
            tmp = tmp + space;
            sb.append(tmp);
        }

        return sb.toString();
    }
}
