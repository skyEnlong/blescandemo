package com.communication.gpsband;

import android.content.Context;
import android.text.TextUtils;

import com.communication.data.AccessoryConfig;
import com.communication.data.CLog;
import com.communication.data.DataUtil;
import com.communication.util.CommonUtils;

import java.util.Random;

/**
 * Created by workEnlong on 2015/12/10.
 */
public class SecretKeyUtil {
    private static final String TAG = "ble_parse_secret";
    public static final String KEY_DYNAMIC_CODE = "key_value_dynamic_code";

    public static byte[] Z = new byte[]{0x43, 0x14,
            0x58, 0x0D, 0x31, 0x63, 0x4C, 0x3C};

    public static byte[] getDynamicKey(Context mContext){
        byte[] value_code = null;

        String uid = AccessoryConfig.getStringValue(mContext, KEY_DYNAMIC_CODE);
        if (!TextUtils.isEmpty(uid)) {
            CLog.i(TAG, "dynamic code:" + uid);

            value_code = CommonUtils.convertHexStringToByte(uid);

        } else {
            Random random = new Random();
            value_code = new byte[8];
            for (int i = 0; i < 8; i++) { // RFU length 48
                value_code[i] = (byte) (random.nextInt(255) & 0xff);
            }
            String str = CommonUtils.convertByteToHexString(value_code);
            AccessoryConfig.setStringValue(mContext, KEY_DYNAMIC_CODE, str);
            CLog.i(TAG, "dynamic code:" + str);
        }

        return value_code;
    }

    public static byte[] getRealKey(Context mContext){
        byte[] secret_key = new byte[8];
        byte[] D = getDynamicKey(mContext);
        for (int i = 0; i < Z.length; i++){
            secret_key[i] = (byte)(((Z[i] & 0xff) ^ (D[i] & 0xff)) & 0xff);
        }
        DataUtil.DebugPrint("parse_Key:", secret_key);
        return secret_key ;
    }
}
