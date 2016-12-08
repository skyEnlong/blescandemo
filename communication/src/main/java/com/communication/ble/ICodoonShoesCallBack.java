package com.communication.ble;

import com.communication.bean.CodoonShoesMinuteModel;
import com.communication.bean.CodoonShoesModel;
import com.communication.bean.CodoonShoesState;
import com.communication.data.ISyncDataCallback;

import java.util.List;

/**
 * Created by enlong on 2016/12/7.
 */

public interface ICodoonShoesCallBack extends ISyncDataCallback {
    public void onResponse(byte[] data);

    /**
     * 准备同步数据
     */
    void onShoesDataSyncRedy();

    /**
     * 加速度传感器标定
     * 0 : success  1: failed
     *
     * @param i
     */
    void onAccessoryBDSuccess(int i);

    /**
     * 开始跑步
     *
     * @param i 状态	说明
     *          0x00	成功
     *          0x01	时间丢失
     *          0x02	存储空间满
     *          0x03	电量低
     */
    void onStartRunResult(int i);

    /**
     * 结束跑步
     */
    void onStopRun();

    /**读取当前状态**/
    void onGetShoesState(CodoonShoesState codoonShoesState);

    void onGetTotalRun(int totalRun);

    /**获取到的start-end模式的运动**/
    void onGetRunSports(List<CodoonShoesModel> ls);


    /**读取跑步姿态数据**/
    void onGetRunState(CodoonShoesMinuteModel model);
}
