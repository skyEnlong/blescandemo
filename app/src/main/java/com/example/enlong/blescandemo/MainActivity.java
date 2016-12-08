package com.example.enlong.blescandemo;

import android.accounts.Account;
import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import de.greenrobot.event.EventBus;

public class MainActivity extends AppCompatActivity implements ISearResult, View.OnClickListener {

    private TextView value_txt;
    SearchService.MyBinder myBinder;

    private Button scanBtn;
    final int STATE_SCAN = 0;
    final int STATE_CONNECT = 1;
    final int STATE_CONNECT_ING = 2;
    final int STATE_NONE = -1;
    int cur_state = STATE_NONE;
    private  Button light;
    ScrollView scrollView;
    private TextView summary;
    private int mJobId  = 0;
    private  Account account ;


    public static final String AUTHORITY = "com.example.android.datasync.provider";
    // Account

    // Sync interval constants
    public static final long SECONDS_PER_MINUTE = 60L;
    public static final long SYNC_INTERVAL_IN_MINUTES = 60L;
    public static final long SYNC_INTERVAL =
            SYNC_INTERVAL_IN_MINUTES *
                    SECONDS_PER_MINUTE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SystemUtils.isReal = true;
        summary = (TextView) findViewById(R.id.summary);
        scrollView  = (ScrollView) findViewById(R.id.scrollView);
        scanBtn = (Button)  findViewById(R.id.scan_button);
        scanBtn.setOnClickListener(this);
        light = (Button) findViewById(R.id.light);
        light.setOnClickListener(this);
        value_txt = (TextView) findViewById(R.id.value_txt);

        Intent intent = new Intent("com.example.enlong.blescandemo.SearchService");
        ComponentName componentName = new ComponentName(getPackageName(), "com.example.enlong.blescandemo.SearchService");
        intent.setComponent(componentName);
        startService(intent);

        bindService(intent, connection, BIND_AUTO_CREATE);

        EventBus.getDefault().register(this);

        BluetoothAdapter.getDefaultAdapter().enable();
        startJob();
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                int i = 5 /0 ;
//            }
//        }, 15000);
         SystemUtils.CreateSyncAccount(this);

        SystemUtils.isMobile("huawei");
    }

    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.i("cod_smart", "bind service");
            myBinder = ((SearchService.MyBinder) iBinder);
            myBinder.setCallBack(MainActivity.this);
            if(myBinder.isSearch()){
                scanBtn.setText("停止扫描");
                Log.i("cod_smart", " service is searching");

            }else {
                scanBtn.setText("开始扫描");
                Log.i("cod_smart", "service not searching");
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            myBinder = null;
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

    }



    @Override
    protected void onDestroy() {
        Log.i("cod_smart", "onDestroy");
        EventBus.getDefault().unregister(this);
        super.onDestroy();
        if(null != myBinder){
            myBinder.stopScan();
            myBinder.disConnect();
            myBinder.setCallBack(null);

        }
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        unbindService(connection);

        Intent intent = new Intent("com.example.enlong.blescandemo.SearchService");
        ComponentName componentName = new ComponentName(getPackageName(), "com.example.enlong.blescandemo.SearchService");
        intent.setComponent(componentName);


        stopService(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }


    @Override
    public void showResult(final String value) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                value_txt.setText(value_txt.getText().toString() + "\n" +
                        value);
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.scan_button){

            if(null != myBinder){

                switch (cur_state){
                    case  STATE_NONE:
                        myBinder.startScan();
                        scanBtn.setText("停止扫描");
                        cur_state = STATE_SCAN;

                        break;
                    case STATE_SCAN:
                        myBinder.stopScan();
                        scanBtn.setText("开始扫描");
                        cur_state =  STATE_NONE;
                        break;

                    case STATE_CONNECT:
                        myBinder.disConnect();
                        scanBtn.setText("开始扫描");
                        cur_state = STATE_NONE;
                        break;
                }

            }
        }else if(view.getId() == R.id.light){
            if(light.getText().toString().equals(getString(R.string.state_light))){
                light.setText(R.string.state_keep_light);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

//                myBinder.light();
            }else {
                light.setText(getString(R.string.state_light));
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

//                myBinder.closeLight();
            }
        }
    }

    public void onEventMainThread(MsgEvent event){
        String s0 = "开始:" + event.start + " 现在:" + event.current +
                " 共:" +  (event.current - event.start) + " 贞,丢:" +
                event.lostMore ;

        String s1 = "\n连续丢包<=2次个数:" + event.lost2;
        String s2 = "\n连续丢包<=5次个数:" + event.lost5;
        String s3 = "\n连续丢包<=10次个数:" + event.lost10;
        String s4 = "\n连续丢包>10次个数" + event.lostMore;

        summary.setText(s0 + s1 + s2 + s3 + s4);
        showResult(event.msg);

        switch (event.event_id){
            case  -1:
                cur_state = STATE_NONE;
                scanBtn.setText("开始扫描");
                break;
            case 0:
                cur_state = STATE_CONNECT_ING;
                scanBtn.setText("连接中...");
                break;

            case 2:  //
                cur_state = STATE_CONNECT;
                scanBtn.setText("断开连接");
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if(null != myBinder && myBinder.isSearch()) {
            Toast.makeText(this, "正在测试中,不能退出", Toast.LENGTH_SHORT).show();
            return;
        }
        SystemUtils.isReal = false;
        super.onBackPressed();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void startJob(){
        if(Build.VERSION.SDK_INT < 21) return;
        JobScheduler scheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        ComponentName componentName = new ComponentName(getPackageName(),"com.example.enlong.blescandemo.MyJobService");
        JobInfo.Builder builder = new JobInfo.Builder(++mJobId, componentName);
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE);
        builder.setRequiresDeviceIdle(false);
        builder.setPeriodic(1000);
        scheduler.schedule(builder.build());
    }


}
