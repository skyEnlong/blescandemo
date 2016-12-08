package com.communication.util;

/**
 * Created by workEnlong on 2015/6/18.
 */
public class CodoonDeviceUtil {

    public static String[] parseUnionPayInfo(byte[] data){
        if(null != data && data.length > 12) {
            int offset = 2;
            String mainVersion = Integer.toHexString((data[offset] & 0xff));

            String secondVersion = Integer.toHexString((data[offset + 1] & 0xff));

            String version = mainVersion
                    + "." + secondVersion;

            offset += 2;

            String manuCode = Integer.toHexString(offset);

            offset += 1;

            String product_num = Integer.toHexString(data[offset] & 0xff) +
                    Integer.toHexString(data[offset + 1] & 0xff) + Integer.toHexString(data[offset + 2] & 0xff);

            offset += 3;
            String liushui_num = Integer.toHexString(data[offset] & 0xff) +
                    Integer.toHexString(data[offset + 1] & 0xff) +
                    Integer.toHexString(data[offset + 2] & 0xff) +
                    Integer.toHexString(data[offset + 3] & 0xff);

            String id = manuCode + "-" + product_num + "-" + liushui_num;

            return new String[]{version, id};
        }

        return null;
    }


    public static boolean isRomDevice(String deviceName) {

        if(null == deviceName) return  false;

        boolean is =
                deviceName.equals("codoon")
                        || deviceName.startsWith("cod_");
        return is;
    }

}
