package com.communication.bean;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by workEnlong on 2015/12/7.
 */
public class GpsSummaryInfo {
    //    SUMM_DIST 运动距离 4 int 运动后统计结果，单位米
//    SUMM_TIME 运动时间 4 int 运动后统计结果，界面显示时分秒
//    SUMM_PACE 配速 4 int 运动后统计结果，秒每米
//    SUMM_KA 卡路里 4 int 运动后统计结果，
//    SUMM_SPD 时速 4 int 界面显示公里每小时，有小数点，需乘以 100
//    SUMM_SF 步频 2 short 步每分钟
    public String data_id;

    public long start_time;
    /**
     * unit m
     */
    public int total_distance;

    /**
     * unit s
     */
    public int duration;

    /**
     * unit ka?
     */
    public int calories;

    /**
     * unit s/m
     */
    public int average_pace;

    /**
     * unit km/h
     * <p/>
     * *
     */
    public float average_speed;

    /**
     * unit step/min
     */
    public float step_frequency;


    public GpsDetailInfo detailInfo;

    public int height; //cm

    public int weight; //kg

    public int gender; // man : 1  girl: 0

    public long[] milesTime;

    @Override
    public String toString() {
        return "GpsSummaryInfo{" +
                "milesTime=" + Arrays.toString(milesTime) +
                ", data_id='" + data_id + '\'' +
                ", start_time=" + format.format(new Date(start_time)) +
                ", total_distance=" + total_distance +
                ", duration=" + duration +
                ", calories=" + calories +
                ", average_pace=" + average_pace +
                ", average_speed=" + average_speed +
                ", step_frequency=" + step_frequency +
                ", detailInfo=" + detailInfo +
                ", height=" + height +
                ", weight=" + weight +
                ", gender=" + gender +
                '}';
    }

    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
}
