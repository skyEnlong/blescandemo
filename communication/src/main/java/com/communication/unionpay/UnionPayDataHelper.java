package com.communication.unionpay;

import android.content.Context;

import com.communication.ble.UnionPayDeviceSyncManager;
import com.communication.data.AccessoryConfig;

/**
 * Created by workEnlong on 2015/6/15.
 */
public class UnionPayDataHelper implements UnionPayCommand{

    private UnionPayDeviceSyncManager syncManager;
    private boolean hasATR = false;
    private Context mContext;
    private final String KEY_ATR_TIME = "atr_time";

    public  UnionPayDataHelper(Context mContext,
                               UnionPayDeviceSyncManager syncManager){
        this.syncManager = syncManager;
        this.mContext = mContext;
    }

    public void setATRTime(String address,long time){
        AccessoryConfig.setLongValue(mContext, KEY_ATR_TIME+ address, time);
    }


    public boolean checkHasATR(String address){
       long time =  AccessoryConfig.getLongValue(mContext, KEY_ATR_TIME+ address, 0);
       if(System.currentTimeMillis() - time > 30000){
           return false;
       }
       return true;
    }
}
