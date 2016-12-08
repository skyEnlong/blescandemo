package com.example.enlong.blescandemo;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;

import static android.content.Context.ACCOUNT_SERVICE;
import static android.content.Context.POWER_SERVICE;

/**
 * Created by enlong on 2016/11/14.
 */

public class SystemUtils {

    public static final String AUTHORITY = "com.example.android.datasync.provider";
    // An account type, in the form of a domain name
    public static final String ACCOUNT_TYPE = "com.example.enlong.blescandemo.AccountSyncService";
    // The account name
    public static final String ACCOUNT = "enlongAccount";
    // Instance fields

    public static boolean isReal = true;

    /**
     * Create a new dummy account for the sync adapter
     *
     * @param context The application context
     */
    public static void CreateSyncAccount(Context context) {
        // Create the account type and default account
        Account newAccount = null;
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(
                        ACCOUNT_SERVICE);

        Account[] accounts = accountManager.getAccountsByType(context.getResources().getString(R.string.account_type));
        if(null != accounts && accounts.length > 0){
            newAccount = accounts[0];
        }else {
            newAccount =  new Account(
                    ACCOUNT, context.getResources().getString(R.string.account_type));;
        }
        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
        ContentResolver mResolver = context.getContentResolver();
        if (accountManager.addAccountExplicitly(newAccount, "hello", new Bundle())) {

            Log.e("enlong", "add account success");

            ContentResolver.setIsSyncable(newAccount, SystemUtils.AUTHORITY, 1);
            ContentResolver.setSyncAutomatically(newAccount, SystemUtils.AUTHORITY,true);
            mResolver.addPeriodicSync(newAccount, AUTHORITY, new Bundle(), 5 );
        } else {

            Log.e("enlong", "add account failed");
            Bundle b = new Bundle();
            // Disable sync backoff and ignore sync preferences. In other words...perform sync NOW!
            b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
            b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
            ContentResolver.requestSync(
                    newAccount, // Sync account
                    AUTHORITY,                 // Content authority
                    b);

        }
    }

    public static void stopSync(Context context){
        Account newAccount = null;
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(
                        ACCOUNT_SERVICE);

        Account[] accounts = accountManager.getAccountsByType(context.getResources().getString(R.string.account_type));
        if(null != accounts && accounts.length > 0){
            newAccount = accounts[0];
        }else {
            newAccount =  new Account(
                    ACCOUNT, context.getResources().getString(R.string.account_type));;
        }
        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
        ContentResolver mResolver = context.getContentResolver();
        mResolver.removePeriodicSync(newAccount,AUTHORITY, new Bundle());

    }

    public   static  boolean isScreenOn (Context mContext) {
        if (android.os.Build.VERSION.SDK_INT >= 20) {
            // I'm counting
            // STATE_DOZE, STATE_OFF, STATE_DOZE_SUSPENDED
            // all as "OFF"
            DisplayManager dm = (DisplayManager) mContext.getSystemService(Context.DISPLAY_SERVICE);
            Display[] displays = dm.getDisplays();
            for (Display display : displays) {
                if (display.getState () == Display.STATE_ON
                        || display.getState () == Display.STATE_UNKNOWN) {
                    return true;
                }
            }
            return false;
        }

        // If you use less than API20:
        PowerManager powerManager = (PowerManager) mContext.getSystemService(POWER_SERVICE);
        if (powerManager.isScreenOn ()) {
            return true;
        }
        return false;
    }


    public static void startPollingService(Context context, int seconds, Class<?> cls,String action) {
        //获取AlarmManager系统服务
        AlarmManager manager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);

        //包装需要执行Service的Intent
        Intent intent = new Intent(context, cls);
        intent.setAction(action);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //触发服务的起始时间
        long triggerAtTime = SystemClock.elapsedRealtime();

        //使用AlarmManger的setRepeating方法设置定期执行的时间间隔（seconds秒）和需要执行的Service
        manager.setRepeating(AlarmManager.ELAPSED_REALTIME, triggerAtTime,
                seconds * 1000, pendingIntent);
    }
    //停止轮询服务
    public static void stopPollingService(Context context, Class<?> cls,String action) {
        AlarmManager manager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, cls);
        intent.setAction(action);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //取消正在执行的服务
        manager.cancel(pendingIntent);
    }

    public static boolean isMobile(String type){
        boolean isRight = false;
        String brand = Build.BRAND;

        isRight = (TextUtils.isEmpty(brand)) ? false :brand.toLowerCase().contains(type.toLowerCase());

        String manu = Build.MANUFACTURER;
        isRight |= (TextUtils.isEmpty(manu)) ? false :manu.toLowerCase().contains(type.toLowerCase());

        String model = Build.MODEL;
        isRight |= (TextUtils.isEmpty(model)) ? false :model.toLowerCase().contains(type.toLowerCase());


        String device = Build.DEVICE;
        isRight |= (TextUtils.isEmpty(device)) ? false :device.toLowerCase().contains(type.toLowerCase());

        return isRight;
    }
}
