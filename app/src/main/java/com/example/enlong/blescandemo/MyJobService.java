package com.example.enlong.blescandemo;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

/**
 * Created by enlong on 2016/11/7.
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MyJobService extends JobService {
    private final String TAG ="en_long";
    private Handler mHandler = new Handler();
    JobParameters params;
    private Runnable r = new Runnable() {
        @Override
        public void run() {
            CLog.e("en_long", " MyJobService start activity ");

            Intent pedometerIntent = new Intent(Intent.ACTION_MAIN);

            pedometerIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            pedometerIntent.setClass(MyJobService.this, MainActivity.class);

            pedometerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

            MyJobService.this.startActivity(pedometerIntent);
                    jobFinished(params, false);//任务执行完后记得调用jobFinsih通知系统释放相关资源

        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        CLog.e("en_long", " MyJobService service onCreate ");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(r);
        CLog.e("en_long", " MyJobService service onDestroy");

    }
    @Override
    public boolean onStartJob(JobParameters params) {
        Log.i(TAG, "onStartJob:" + params.getJobId());
        this.params = params;
        mHandler.removeCallbacks(r);

        mHandler.postDelayed(r, 3000);



//        Toast.makeText(MyJobService.this, "start job:" + params.getJobId(), Toast.LENGTH_SHORT).show();
//        jobFinished(params, false);//任务执行完后记得调用jobFinsih通知系统释放相关资源
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.i(TAG, "onStopJob:" + params.getJobId());
        return false;
    }

}
