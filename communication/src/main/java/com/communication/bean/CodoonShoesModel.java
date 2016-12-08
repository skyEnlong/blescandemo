package com.communication.bean;

import java.io.Serializable;
import java.util.List;

/**
 * Created by enlong on 2016/12/7.
 */

public class CodoonShoesModel implements Serializable{

    public List<CodoonShoesMinuteModel> minutesModels;
    public List<Long> paces;

    /**冲刺次数**/
    public int sprintCounts;

    /**平均着地时间**/
    public long avgTouchTime;

    /**平均支撑时间**/
    public long avgHoldTime;

    /**平均登离时间**/
    public long flyTime;

    public long startDateTime;

    public long endDateTIme;

    public float total_dis;

    public float total_cal;

    @Override
    public String toString() {
        return "{" +
                "minutesModels=" + minutesModels +
                ", paces=" + paces +
                ", sprintCounts=" + sprintCounts +
                ", avgTouchTime=" + avgTouchTime +
                ", avgHoldTime=" + avgHoldTime +
                ", flyTime=" + flyTime +
                ", startDateTime=" + startDateTime +
                ", endDateTIme=" + endDateTIme +
                ", total_dis=" + total_dis +
                ", total_cal=" + total_cal +
                '}';
    }
}
