package com.communication.util;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.communication.data.CLog;

import java.lang.reflect.Method;

public class MobileUtil {

    public static boolean isCurMobileManuConnect() {
        boolean isNeed = false;
        String curMobile = android.os.Build.BRAND.toLowerCase();
        String version = android.os.Build.MODEL.toLowerCase();
        CLog.i("MobileUtil", "curMobile:" + curMobile + " version:" + version);
        if (!curMobile.contains("samsung")) {
            isNeed = true;
        }

        return isNeed;
    }

    public static boolean isAutoConnect() {
        boolean isAuto = false;
        String curMobile = android.os.Build.BRAND.toLowerCase();
        String version = android.os.Build.MODEL.toLowerCase();
// curMobile:huawei version:huawei 
//
        CLog.i("MobileUtil", "curMobile:" + curMobile + " version:" + version);
        if (curMobile.equalsIgnoreCase("samsung") && !version.equals("sm-n9006")
//                || version.toLowerCase().contains("gra-ul00")  // huawei
                ) {
            isAuto = true;
        }

        return isAuto;
    }
    
    public static boolean isUnionPayAutoConnect(){
    	 boolean isAuto = false;
         String curMobile = android.os.Build.BRAND.toLowerCase();
         String version = android.os.Build.MODEL.toLowerCase();
 // curMobile:huawei version:huawei 
 //
         CLog.i("MobileUtil", "curMobile:" + curMobile + " version:" + version);
         if ((curMobile.equals("huawei") && version.equals("p7-l00"))
        		) {
             isAuto = true;
         }else if(curMobile.equalsIgnoreCase("sumsung")){
             isAuto = true;
         }


        return isAuto;
    }
    
    public static int getTimeOutDelay(){
    	String curMobile = android.os.Build.BRAND.toLowerCase();
    	if(curMobile.equals("huawei")){
    		return 15000;
    	}
    	return 10000;
    }

    public static boolean createBond(BluetoothDevice btDevice)
            throws Exception {
        Method createBondMethod = BluetoothDevice.class.getMethod("createBond");
        Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }

    public static boolean isSupportBLE(Context context) {

        return context.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_BLUETOOTH_LE);

    }

    public static boolean isNoDescriptorRes(){
        String curMobile = android.os.Build.BRAND.toLowerCase();
        boolean isNoDescriptorRes = false;
        if (curMobile.equals("huawei")){
            isNoDescriptorRes = true;
        }

        return isNoDescriptorRes;

    }

    public static boolean getBooleanMetaValue(Context mContext, String key){
        boolean value = false;
        PackageManager manager = mContext.getPackageManager();
        try {
            ApplicationInfo info = manager.getApplicationInfo(mContext.getPackageName(),
                    PackageManager.GET_META_DATA);
            value  = info.metaData.getBoolean(key);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return value;
    }
}
