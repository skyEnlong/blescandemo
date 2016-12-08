package com.communication.gpsband;

import com.communication.bean.GpsSummaryInfo;
import com.communication.data.ISyncDataCallback;

/**
 * Created by workEnlong on 2015/12/3.
 */
public interface GpsBandCallBack extends ISyncDataCallback {

    public void onGetGpsInfo(GpsSummaryInfo info);
    public void onEphemerisProgressQurey(int progress, String file_name);
    public void onEphemerisProgressReset();
    public void onEphemerisUpdateSuccess();
    public void onCrcCheckResult(int status);
//    public String getEphemerisFilePath();


}
