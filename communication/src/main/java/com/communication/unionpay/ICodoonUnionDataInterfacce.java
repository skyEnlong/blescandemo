package com.communication.unionpay;

import com.communication.data.AccessoryValues;
import com.communication.data.ISyncCallBack;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

/**
 * Created by workEnlong on 2015/6/25.
 */
public interface ICodoonUnionDataInterfacce extends ISyncCallBack{

        void onDeviceUnBind(String address);
        void onDeviceBind(String address);


        void onGetVersionAndId(String version,String deviceID);

        void onUpdateTimeSuccessed();

        void onUpdateAlarmReminderSuccessed();

        void onBattery(int battery);

        void onClearDataSuccessed();

        void onGetDeviceTime(String time);

        void onSyncDataProgress(int progress);

        void onUpdateUserinfoSuccessed();

        void onGetUserInfo(int height,int weigh,int age,int gender,int stepLength,int runLength,int sportType,int goalValue);

        /**
         *
         * curday_StartTime,  0
         * curday_During,    1
         * cur_Steps,  		2
         * cur_Calories,	3
         * cur_Metes, 		4
        total_StartTime, 5
        total_During     6
        total_Steps, 	7
        total_Calories, 	8
        total_Metes, 	9
        cur_SleepStartTime, 10
        cur_SleepEndTime, 11
        cur_SleepDuring,	12
        sleepTotaltime, 	13
        endTime}			14
         * @return
         */


        void onSyncDataOver(HashMap<String, AccessoryValues> data,  ByteArrayOutputStream baos);


    }
