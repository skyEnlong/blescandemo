package com.example.enlong.blescandemo;

import android.accounts.Account;
import android.app.Service;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by enlong on 2016/11/14.
 */

public class AccountSyncService extends Service{
    private static final Object sSyncAdapterLock = new Object();

    private  SyncAdapter sSyncAdapter = null;

    @Override

    public void onCreate() {
        CLog.e("en_long", " AccountSyncService service onCreate ");

        synchronized (sSyncAdapterLock) {

            if (sSyncAdapter == null) {

                sSyncAdapter = new SyncAdapter(getApplicationContext(), true);

            }

        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        CLog.e("en_long", " AccountSyncService service onDestroy ");

    }

    @Override

    public IBinder onBind(Intent intent) {

        return sSyncAdapter.getSyncAdapterBinder();

    }

    /**
     * Created by enlong on 2016/11/14.
     */

    public static class SyncAdapter extends AbstractThreadedSyncAdapter {
        ContentResolver mContentResolver;
        private Context mContext;
        public SyncAdapter(Context context, boolean autoInitialize) {
            super(context, autoInitialize);
            mContentResolver = context.getContentResolver();
            mContext = context;


        }

        /**
         * Set up the sync adapter. This form of the
         * constructor maintains compatibility with Android 3.0
         * and later platform versions
         */
        public SyncAdapter(
                Context context,
                boolean autoInitialize,
                boolean allowParallelSyncs) {
            super(context, autoInitialize, allowParallelSyncs);
            /*
             * If your app uses a content resolver, get an instance of it
             * from the incoming Context
             */
            mContext = context;

            mContentResolver = context.getContentResolver();
        }


        @Override
        public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient contentProviderClient, SyncResult syncResult) {

            synchronized (mContentResolver){
                while (SystemUtils.isReal){
                    syncResult.stats.numUpdates++;
                    Log.e("en_long",  "do onPerformSync we can start our app");
                    try {

                        Intent pedometerIntent = new Intent(Intent.ACTION_MAIN);

                        pedometerIntent.addCategory(Intent.CATEGORY_LAUNCHER);

                        pedometerIntent.setClass(mContext, MainActivity.class);

                        pedometerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

                        mContext.startActivity(pedometerIntent);

                        Thread.sleep(1000);

                    }catch (Exception e){}
                }


            }

        }
    }
}
