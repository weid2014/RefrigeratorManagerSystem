package com.techjh.rfid;

//public class HfData {
//   public static hflib reader = new hflib();
//    public String AntennaList="";
//    public static List<InventoryTagMap> mlist = new ArrayList<InventoryTagMap>();
//    /**
//     * Convert byte[] to hex
//     * string
//     *
//     * @param src byte[] data
//     * @return hex string
//     */
//    public static String bytesToHexString(byte[] src) {
//        StringBuilder stringBuilder = new StringBuilder("");
//        if (src == null || src.length <= 0) {
//            return null;
//        }
//        for (int i = 0; i < src.length; i++) {
//            int v = src[i] & 0xFF;
//            String hv = Integer.toHexString(v);
//            if (hv.length() == 1){
//                hv = '0' + hv;
//            }
//            stringBuilder.append(hv);
//        }
//        return stringBuilder.toString();
//    }
//
//    public static String bytesToHexString(byte[] src, int offset, int length) {
//        StringBuilder stringBuilder = new StringBuilder("");
//        if (src == null || src.length <= 0) {
//            return null;
//        }
//        for (int i = offset; i < offset+length; i++) {
//            int v = src[i] & 0xFF;
//            String hv = Integer.toHexString(v);
//            if (hv.length() == 1) {
//                stringBuilder.append(0);
//            }
//            stringBuilder.append(hv);
//        }
//        return stringBuilder.toString().toUpperCase();
//    }
//
//    public static byte[] hexStringToBytes(String hexString) {
//        if (hexString == null || hexString.equals("")) {
//            return null;
//        }
//        hexString = hexString.toUpperCase();
//        int length = hexString.length() / 2;
//        char[] hexChars = hexString.toCharArray();
//        byte[] d = new byte[length];
//        for (int i = 0; i < length; i++) {
//            int pos = i * 2;
//            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
//        }
//        return d;
//    }
//    private static byte charToByte(char c) {
//        return (byte) "0123456789ABCDEF".indexOf(c);
//    }
//}
