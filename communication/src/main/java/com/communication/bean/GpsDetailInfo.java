package com.communication.bean;

import java.util.List;

/**
 * Created by workEnlong on 2015/12/7.
 */
public class GpsDetailInfo {

//    GPS_LO 经度 4 int 原 double 型乘以 10 万再强制转换为 int 型
//    GPS_LA 纬度 4 int 原 double 型乘以 10 万再强制转换为 int 型
//    GPS_ALT 海拔 2 int -9999 米 ˜9999 米
//    GPS_STEP 步数 1 char 2 秒钟之内的步数
//    GPS_INTERVAL 采点间隔 1 char GPS 采点间隔，默认 2 秒，可扩展到 11 字节
//    SYS_TIME 时间戳 6 BCD 系统时间，如 2015/08/31/12:10:01,
//    表示为 0x150831121001，扩展时前面补 0
   public List<GPSBandPoint> points;
    public int step;
    public  int interval;
    public  long start_time;
    public long end_time;

    public String data_id;

 @Override
 public String toString() {
  return "GpsDetailInfo{" +
          "points=" + points +
          ", step=" + step +
          ", interval=" + interval +
          ", start_time=" + start_time +
          ", end_time=" + end_time +
          ", data_id='" + data_id + '\'' +
          '}';
 }
}
