package com.communication.bean;

import java.io.Serializable;
import java.util.List;

/**
 * Created by workEnlong on 2016/1/21.
 */
public class ShoseDataDetail implements Serializable {
    public String route_id;
    public String local_id;    //  123 ,    //string
    public float total_distance;    // 1,     //double, 单位：米
    public List<Integer> stride_frequency_list;    // [123,456,789,...],   //元素int, 单位：步/分钟
    public List<Integer> stride_length_list;    // [123,456,789,...],  //元素：int, 单位：cm/步
    public List<Double> stride_speed_list;    // [32.4,43.3,...],  //元素：double, 单位：km/h
    public String start_date;    // “” ,    //格式：2016-01-01 12:30 :00
    public String device_id;    //   ,
    public List<List<Integer>> gait_list;    // 内部数组的三个数据，为百分比，总和为100
    public int total_steps;    // 1,   //int
    public int total_duration;    // 1,  //：int, 单位：秒
    public List<List<Integer>> touchdown_list;    // [
    //int, 内部数组的三个数据，为百分比，总和为100
    public float stride_frequency_max;    // 1,         //
    public float stride_frequency_avg;    // 1,
    public float stride_length_max;    // 1,
    public float stride_length_avg;    // 1,
    public double stride_speed_max;    // 1, 元素：double
    public double stride_speed_avg;    // 1,元素：double
    public int touchdown_half_avg;    // 1,
    public int touchdown_after_avg;    // 1,
    public float gait_pigeon_avg;    // 1,   normal state
    public float gait_toe_out_avg;    // 1,

    public List<Integer> time_array;
    @Override
    public String toString() {
        return "{" +
                "route_id='" + route_id + '\'' +
                ", local_id='" + local_id + '\'' +
                ", total_distance=" + total_distance +
                ", stride_frequency_list=" + stride_frequency_list +
                ", stride_length_list=" + stride_length_list +
                ", stride_speed_list=" + stride_speed_list +
                ", start_date='" + start_date + '\'' +
                ", device_id='" + device_id + '\'' +
                ", gait_list=" + gait_list +
                ", total_steps=" + total_steps +
                ", total_duration=" + total_duration +
                ", touchdown_list=" + touchdown_list +
                ", stride_frequency_max=" + stride_frequency_max +
                ", stride_frequency_avg=" + stride_frequency_avg +
                ", stride_length_max=" + stride_length_max +
                ", stride_length_avg=" + stride_length_avg +
                ", stride_speed_max=" + stride_speed_max +
                ", stride_speed_avg=" + stride_speed_avg +
                ", touchdown_half_avg=" + touchdown_half_avg +
                ", touchdown_after_avg=" + touchdown_after_avg +
                ", gait_pigeon_avg=" + gait_pigeon_avg +
                ", gait_toe_out_avg=" + gait_toe_out_avg +
                '}';
    }
}
