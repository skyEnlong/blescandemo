package com.example.enlong.blescandemo;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.support.annotation.Nullable;
import android.util.Log;

import de.greenrobot.event.EventBus;

/**
 * Created by enlong on 16/10/18.
 */
public class SearchService extends Service  implements OnDeviceSearch {
    private BleScanMananfer mScanner;
    private int currentValue = 0;
    private int start = 0;
    private Handler mHandler;
    Runnable researchRunnable;
    private StringBuffer str;
    private long totalLost = 0;

    ISearResult iSearResult;
    private MyBinder binder;
    private  boolean isSearing;
    ShoesConnector connector ;

    private long jump1 = 0;
    private long jump2 = 0;
    private long jump3 = 0;
    private long jumpOther = 0;

    private  PowerManager.WakeLock wakeLock;

    @Override
    public void onCreate() {
        super.onCreate();
        mScanner = new BleScanMananfer(this);
        str = new StringBuffer();

        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {

                if(msg.what == 1){
                    connector.startDevice((String)msg.obj);
                }
            }
        };
        if(Build.VERSION.SDK_INT > 18) {

            connector = new ShoesConnector(this);
        }

        researchRunnable = new Runnable() {
            @Override
            public void run() {

                if (mScanner.isScan()) {

                    mScanner.stopScan();
                    mHandler.postDelayed(this, 200);
                    if(null != iSearResult) iSearResult.showResult("pause");

                } else {

                    mScanner.startScan();
                    mHandler.postDelayed(this, 30 * 1000);
                    if(null != iSearResult) iSearResult.showResult("restart");

                }


            }
        };

        mScanner.setCallBack(this);
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

        wakeLock =  pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ble");

        Log.e("enlong", "Service create");
    }


    private void showResult(int value){
        Notification.Builder builder = new Notification.Builder(this);
        builder.setWhen(System.currentTimeMillis());
        builder.setShowWhen(true);
        builder.setSmallIcon(R.drawable.about_qq);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.about_qq));
//        builder.setVisibility(View.VISIBLE);
        builder.setContentTitle("scan");
        Intent pedometerIntent = new Intent(Intent.ACTION_MAIN);

        pedometerIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        pedometerIntent.setClass(this, MainActivity.class);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                pedometerIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);
        builder.setContentText("receive:" + value);
        Notification notification = builder.build();

        startForeground(1000, notification);

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if(null == binder) binder = new MyBinder();
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, START_STICKY, startId);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            wakeLock.release();
            wakeLock = null;
        }catch (Exception e){

        }

    }

    @Override
    public void onDeviceSearch(final ParsedAd dev) {
//        Log.i("enlong", "isScreenOn:" + SystemUtils.isScreenOn(this));

        if ("COD_SMART1".equals(dev.localName)) {

//            mHandler.removeCallbacks(researchRunnable);
//            mScanner.stopScan();
//            isSearing = false;
//
//            MsgEvent event = new MsgEvent();
//            event.event_id = 0;
//            event.msg = "已找到,开始连接 " + dev.address;
//            EventBus.getDefault().post(event);
//
//            Message msg = mHandler.obtainMessage();
//            msg.what = 1;
//            msg.obj = dev.address;
//            mHandler.sendMessage(msg);
//
//            return;

            int parse_vale_h = dev.manufacturer[3] & 0xff;
            int parse_vale_l = dev.manufacturer[2] & 0xff;

            int value = parse_vale_h * 256 + parse_vale_l;

            if(0 == start){
                start = value;
            }

            if (currentValue != value) {
                showResult(value);
                Log.i("cod_smart", "get new value:" + dev.toString());
                if (currentValue != 0 && value - currentValue > 1) {
                    int   jump = value - currentValue - 1;

                    if(jump <= 0) return;
                    if(jump <= 2) jump1++;
                    if(jump <= 5) jump2++;
                    if(jump <= 10) jump3++;
                    if(jump > 10) jumpOther++;

                    totalLost += jump;


                    String s = "pre:" + currentValue + "  now:" + value + "jump:" + jump + " total lost:" + totalLost;


                    CLog.recordAction(s);
                    iSearResult.showResult(s);


                }
                currentValue = value;
                MsgEvent event = new MsgEvent();
                event.start = start;
                event.current = value;
                event.lost2 = jump1;
                event.lost5 = jump2;
                event.lost10 = jump3;
                event.lostMore = jumpOther;
                event.totalLost = totalLost;

                EventBus.getDefault().post(event);
                if(null != iSearResult) iSearResult.showResult("" + value);
            } else {
                Log.i("cod_smart", "get same value " + value);
            }

        }
    }


    public class MyBinder extends Binder{
        public void setCallBack( ISearResult iSearResult){
            SearchService.this.iSearResult = iSearResult;

        }
        public void reset(){
            str = null;
            str = new StringBuffer();
            start = 0;
            currentValue = 0;
            totalLost = 0;
            jumpOther = 0;
            jump3 = 0;
            jump2 = 0;
            jump1 = 0;

        }

        public String getReuslt(){
            StringBuffer str = new StringBuffer();
            String s1 = "连续丢包<=2次数:" + jump1;
            String s2 = "连续丢包<=5次数:" + jump2;
            String s3 = "连续丢包<=10次数:" + jump3;
            String s4 = "连续丢包>10次个数" + jumpOther;

            str.append(s1);
            str.append("\n");

            str.append(s2);
            str.append("\n");

            str.append(s3);
            str.append("\n");

            str.append(s4);
            str.append("\n");

            try{
                String s = "开始发送:" + start + " 结束:" + currentValue +
                        " 共发:" + (currentValue - start ) + " 共丢失:" + totalLost
                        + " 10s丢率:" + (jumpOther * 100 * 10/ (currentValue - start))
                        + " 5s丢率:" + ((jumpOther + jump3) * 100 * 5/ (currentValue - start));
                str.append(s);
            }catch (Exception e){

            }

            return  str.toString();

        }
        public void startScan(){
            reset();
            mScanner.startScan();
            isSearing = true;
            mHandler.postDelayed(researchRunnable, 30 *1000);
        }

        public void stopScan(){
            isSearing = false;
            mHandler.removeCallbacks(researchRunnable);
            mScanner.stopScan();
            if(null != iSearResult){
                iSearResult.showResult(getReuslt());
            }
        }

        public void disConnect(){
            connector.disconnect();
        }
        public boolean isSearch(){
            return isSearing;
        }

        public void light(){
            Log.i("cod_smart", " light");

            wakeLock.acquire();
        }

        public void closeLight(){
            Log.i("cod_smart", " close light");

            wakeLock.release();
        }

    }

    public void setAppForeground()
    {
        Process.setThreadPriority(Process.myPid(), Process.THREAD_PRIORITY_AUDIO);

        Intent pedometerIntent = new Intent(Intent.ACTION_MAIN);

        pedometerIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        pedometerIntent.setClass(this, MainActivity.class);

        pedometerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

       startActivity(pedometerIntent);
//        CLog.i("kevin","set app froeground");
    }
}
