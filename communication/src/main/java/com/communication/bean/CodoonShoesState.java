package com.communication.bean;

import java.io.Serializable;

/**
 * Created by enlong on 2016/12/7.
 */

public class CodoonShoesState implements Serializable{
    /** 0x00 日常运动模式; 0x01 已开始跑步模式 **/
    public int sportState;

    /**电量百分比**/
    public int elvationState;

    /** 日常运动存储数据域已使用百分比 **/
    public int normalStoreState;

    /**跑步存储数据域已使用百分比**/
    public int runStoreState;

    /**时间正常0x00  时间丢失0x01**/
    public int timeState;

    @Override
    public String toString() {
        return "{" +
                "sportState=" + sportState +
                ", elvationState=" + elvationState +
                ", normalStoreState=" + normalStoreState +
                ", runStoreState=" + runStoreState +
                ", timeState=" + timeState +
                '}';
    }
}
