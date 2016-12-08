package com.communication.data;

import com.communication.bean.ShoseDataSummary;
import com.communication.bean.ShoseDataDetail;

import java.util.List;

/**
 * Created by workEnlong on 2016/1/21.
 */
public interface IShoesSyncCallBack extends ISyncDataCallback{

    public void onGetShoseSummary(ShoseDataSummary summary);
    public void onGetShoseDetail(List<ShoseDataDetail> details);
    public void onGetShoseClear();
    public void onGetShoseTotal(int dis);

}
