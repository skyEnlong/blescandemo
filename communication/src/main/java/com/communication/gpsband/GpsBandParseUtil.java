package com.communication.gpsband;

import android.util.Log;

import com.communication.bean.GPSBandPoint;
import com.communication.bean.GpsDetailInfo;
import com.communication.bean.GpsSummaryInfo;
import com.communication.data.CLog;
import com.communication.util.CommonUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by workEnlong on 2015/12/7.
 */
public class GpsBandParseUtil {
    //    FLAG_GPS GPS 位置 11 0xFCFCFCFCFCFCFCFCFCFCFC
//    FLAG_SUMM 运动摘要 11 0xFBFBFBFBFBFBFBFBFBFBFB
//    FLAG_START 开始运动 11 0xFAFAFAFAFAFAFAFAFAFAFA
//    FLAG_PAUSE 暂停运动 11 0xF9F9F9F9F9F9F9F9F9F9F9
//    FLAG_CONT 继续运动 11 0xF8F8F8F8F8F8F8F8F8F8F8
//    FLAG_STOP 结束运动 11 0xF7F7F7F7F7F7F7F7F7F7F7

    //    FLAG_GPS GPS 位置 11 0xFCFCFCFCFCFCFCFCFCFCFC
//    FLAG_SUMM 运动摘要 11 0xFBFBFBFBFBFBFBFBFBFBFB
//    FLAG_START 开始运动 11 0xFAFAFAFAFAFAFAFAFAFAFA
//    FLAG_PAUSE 暂停运动 11 0xF9F9F9F9F9F9F9F9F9F9F9
//    FLAG_CONT 继续运动 11 0xF8F8F8F8F8F8F8F8F8F8F8
//    FLAG_STOP 结束运动 11 0xF7F7F7F7F7F7F7F7F7F7F7

    private static final String TAG = "GpsBandParseUtil";
    public static final int FLAG_GPS = 0xFC;
    public static final int FLAG_SUMM = 0xFB;
    public static final int FLAG_START = 0xFA;
    public static final int FLAG_PAUSE = 0xF9;
    public static final int FLAG_CONTINUE = 0xF8;
    public static final int FLAG_STOP = 0xF7;

    public static final int FREAME_LENGTH = 11;


    public static final int STATE_SUM = 1;
    public static final int STATE_START_TIME = 2;

    public static final int STATE_GPS = 3;
    public static final int STATE_GPS_INTERVAL = 4;
    public static final int STATE_START = 5;
    public static final int STATE_PAUSE = 6;
    public static final int STATE_CONTINUE = 7;
    public static final int STATE_END = 8;
    public static final int STATE_END_TIME = 9;
    public static final int STATE_NORMAL_POINT = 10;

    public static final int STATE_PAUSE_POINT = 13;
    public static final int STATE_CONTINUE_POINT = 14;

    public static final int STATE_PAUSE_TIME = 11;
    public static final int STATE_CONTINUE_TIME = 12;

    /**
     * SUMM_DIST 运动距离 4 int 运动后统计结果，单位米
     * SUMM_TIME 运动时间 4 int 运动后统计结果，界面显示时分秒
     * SUMM_PACE 配速 4 int 运动后统计结果，秒每米
     * SUMM_KA 卡路里 4 int 运动后统计结果，
     * SUMM_SPD 时速 4 int 界面显示公里每小时，有小数点，需乘以 100
     * SUMM_SF 步频 2 short 步每分钟
     *
     * @param bytes
     * @return
     */
    public static GpsSummaryInfo parseSummaryInfo(byte[] bytes) {
        if (null == bytes || bytes.length < 40) return null;
        for (int i = 0; i < FREAME_LENGTH; i++) {
            if ((bytes[i] & 0xff) != FLAG_SUMM) {
                return null;
            }
        }

        GpsSummaryInfo info = new GpsSummaryInfo();
        int index_data = 11;
        byte[] sys_time = Arrays.copyOfRange(bytes, index_data, index_data + 7);
        info.start_time = getSysTime(CommonUtils.changeArraytoList(sys_time));

        index_data += 7;
        info.total_distance = ((bytes[index_data] & 0xff) << 24) +
                ((bytes[index_data + 1] & 0xff) << 16) +
                ((bytes[index_data + 2] & 0xff) << 8) +
                (bytes[index_data + 3] & 0xff);

        index_data += 4;
        info.duration = ((bytes[index_data] & 0xff) << 24) +
                ((bytes[index_data + 1] & 0xff) << 16) +
                ((bytes[index_data + 2] & 0xff) << 8) +
                (bytes[index_data + 3] & 0xff);

        index_data += 4;
        info.average_pace = ((bytes[index_data] & 0xff) << 24) +
                ((bytes[index_data + 1] & 0xff) << 16) +
                ((bytes[index_data + 2] & 0xff) << 8) +
                (bytes[index_data + 3] & 0xff);

        index_data += 4;
        info.calories = ((bytes[index_data] & 0xff) << 24) +
                ((bytes[index_data + 1] & 0xff) << 16) +
                ((bytes[index_data + 2] & 0xff) << 8) +
                (bytes[index_data + 3] & 0xff);

        index_data += 4;
        info.average_speed = ((bytes[index_data] & 0xff) << 24) +
                ((bytes[index_data + 1] & 0xff) << 16) +
                ((bytes[index_data + 2] & 0xff) << 8) +
                (bytes[index_data + 3] & 0xff);
        info.average_speed = info.average_speed / 100.0f;

        index_data += 4;
        info.step_frequency = ((bytes[index_data] & 0xff) << 8) +
                ((bytes[index_data + 1] & 0xff));

        index_data += 2;
        info.height =  ((bytes[index_data] & 0xff) << 24) +
                ((bytes[index_data + 1] & 0xff) << 16) +
                ((bytes[index_data + 2] & 0xff) << 8) +
                (bytes[index_data + 3] & 0xff);

        index_data += 4;
        info.gender =  ((bytes[index_data] & 0xff) << 24) +
                ((bytes[index_data + 1] & 0xff) << 16) +
                ((bytes[index_data + 2] & 0xff) << 8) +
                (bytes[index_data + 3] & 0xff);

        index_data += 4;
        int mile_count =  (short) ((bytes[index_data] & 0xff) << 8) +
                (short) ((bytes[index_data + 1] & 0xff));

        try {
            index_data += 2;
            info.milesTime = new long[mile_count];
            for(int i = 0; i < mile_count; i++){
                info.milesTime[i] =  (short) ((bytes[index_data] & 0xff) << 8) +
                        (short)  ((bytes[index_data + 1] & 0xff));

                index_data += 2;
            }
        } catch (Exception e){

            Log.e(TAG, "has mile count :" + mile_count + " but len is " + bytes.length);
        }



        CLog.i(TAG, info.toString());

        return info;
    }


    public static GpsDetailInfo parseDetailData(byte[] bytes) {

        if (null == bytes) {
            CLog.e(TAG, "bytes null");
            return null;
        }
        if (0 != bytes.length % FREAME_LENGTH) {
            CLog.e(TAG, "bytes length not right");
            return null;
        }

        GpsDetailInfo info = new GpsDetailInfo();

        int state = -1;
        boolean isParseOver = false;
        long time_new = 0;
        GPSBandPoint lastLocation = null;
        int parse_index = 0;
        int total = bytes.length / FREAME_LENGTH;


        ArrayList<Integer> arr = new ArrayList();
        for (int i = 0; i < total; i++) {

            arr.clear();

            if (isParseOver) {
                break;
            }
            for (int k = 0; k < FREAME_LENGTH; k++) {
                arr.add(bytes[parse_index++] & 0xff);
            }

            int length = arr.size();
            if (length == FREAME_LENGTH) {
                int count_flag_gps = 0;
                int count_flag_start = 0;
                int count_flag_continue = 0;
                int count_flag_pause = 0;
                int count_flag_end = 0;
                for (int v : arr) {

                    switch (v) {
                        case FLAG_START:
                            count_flag_start++;
                            break;
                        case FLAG_GPS:
                            count_flag_gps++;
                            break;
                        case FLAG_CONTINUE:
                            count_flag_continue++;
                            break;
                        case FLAG_PAUSE:
                            count_flag_pause++;
                            break;
                        case FLAG_STOP:
                            count_flag_end++;
                            break;
                    }

                }

                //check state
                if (count_flag_gps == FREAME_LENGTH) {
                    state = STATE_GPS;
                    CLog.i(TAG, "find flag_gps");
                }

                if (count_flag_end == FREAME_LENGTH) {
                    state = STATE_END;
                    CLog.i(TAG, "find flag_end");
                }

                if (count_flag_pause == FREAME_LENGTH) {
                    state = STATE_PAUSE;
                    CLog.i(TAG, "find flag_pause");
                }

                if (count_flag_continue == FREAME_LENGTH) {
                    state = STATE_CONTINUE;
                    CLog.i(TAG, "find flag_continue");
                }

                if (count_flag_start == FREAME_LENGTH) {
                    state = STATE_START;
                    CLog.i(TAG, "find flag_start");
                }

                switch (state) {
                    case STATE_GPS:
                        state = STATE_GPS_INTERVAL;

                        break;
                    case STATE_GPS_INTERVAL:
                        info.interval = arr.get(0) & 0xff;

                        break;
                    case STATE_START:
                        CLog.i("parse:", "find start flag");
                        state = STATE_START_TIME;
                        break;

                    case STATE_START_TIME:
                        info.start_time = getSysTime(arr);

                        state = STATE_NORMAL_POINT;

                        break;

                    case STATE_NORMAL_POINT: {
                        GPSBandPoint location = parseGpsInfo(arr);
                        if (null == info.points || info.points.size() == 0) {
                            info.points = new ArrayList<GPSBandPoint>();
                            location.time = info.start_time;
                        } else {
                            location.time = info.points.get(info.points.size() - 1).time +
                                    info.interval * 1000;
                        }
                        info.points.add(location);


                        if (location.lat != 0 && location.longti != 0) {
                            //real point record it
                            lastLocation = location;
                        }
                    }


                    break;


                    case STATE_CONTINUE:
                        CLog.i("parse:", "find continue flag");
                        state = STATE_CONTINUE_TIME;
                        break;

                    case STATE_CONTINUE_TIME:
                        CLog.i("parse:", "find continue time");
                        time_new = getSysTime(arr);
                        state = STATE_CONTINUE_POINT;

                        break;

                    case STATE_CONTINUE_POINT: {

                        GPSBandPoint location = parseGpsInfo(arr);
                        location.state = 2;
                        if (null == info.points || info.points.size() == 0) {
                            info.points = new ArrayList<GPSBandPoint>();
                            location.time = time_new;
                        } else {
                            location.time = time_new;
                        }
                        info.points.add(location);

                        if (location.lat == 0 || location.longti == 0) {
                            state = STATE_CONTINUE_POINT;

                        } else {

                            state = STATE_NORMAL_POINT;
                            lastLocation = location;
                        }

                        CLog.i("parse:", "find continue point:" + location);


                    }

                    break;
                    case STATE_PAUSE:
                        CLog.i("parse:", "find pause flag");
                        state = STATE_PAUSE_TIME;
                        break;

                    case STATE_PAUSE_TIME:
                        state = STATE_PAUSE_POINT;
                        time_new = getSysTime(arr);
                        CLog.i("parse:", "find pause time: " + time_new);

                        if (null != lastLocation) {
                            GPSBandPoint location = lastLocation.clone();
                            location.state = 1;
                            location.step = 0;
                            info.points.add(location);
                            CLog.i("parse:", "find pause point:" + location);
                        }

                        break;

                    case STATE_PAUSE_POINT: {

                        GPSBandPoint location = parseGpsInfo(arr);
                        location.state = 1;
                        if (null == info.points || info.points.size() == 0) {
                            info.points = new ArrayList<GPSBandPoint>();
                            location.time = time_new;
                        } else {
                            location.time = time_new;
                        }
                        info.points.add(location);
                        if (location.lat != 0 || location.longti != 0) {

                            lastLocation = location;
                        }
                    }

                    break;

                    case STATE_END:

                        CLog.i("parse:", "find end flag");
                        state = STATE_END_TIME;
                        break;

                    case STATE_END_TIME:
//                        ArrayList timeList = new ArrayList();
//                        for (int i = 5; i < FREAME_LENGTH; i++) {
//                            timeList.add(arr.get(i));
//                        }
                        info.end_time = getSysTime(arr);
                        isParseOver = true;
                        break;

                }


            }

        }

        CLog.i(TAG, info.toString());
        return info;
    }

    private static GPSBandPoint parseGpsInfo(ArrayList<Integer> arr) {

        GPSBandPoint location = new GPSBandPoint();
        int index = 0;
        double alti =
                (short) (((arr.get(index) & 0xff) << 8) + (short) ((arr.get(index + 1) & 0xff)));
        index += 2;
        double lonti = (((arr.get(index) & 0xff) << 24) + ((arr.get(index + 1) & 0xff) << 16)
                + ((arr.get(index + 2) & 0xff) << 8) + ((arr.get(index + 3) & 0xff))) / 100000.0f;


        index += 4;
        double lat = (((arr.get(index) & 0xff) << 24) + ((arr.get(index + 1) & 0xff) << 16)
                + ((arr.get(index + 2) & 0xff) << 8) + ((arr.get(index + 3) & 0xff))) / 100000.0f;


        index += 4;

        int step = arr.get(index) & 0xff;

        location.alti = alti;
        location.lat = lat;
        location.longti = lonti;
        location.step = step;

        return location;
    }

    /**
     * decode start_time by list data, -1 is err start_time
     *
     * @param list
     * @return
     */
    public static long getSysTime(ArrayList<Integer> list) {
        Calendar mCalendar = Calendar.getInstance();
        int year = 0;
        int month = 0;
        int day = 0;
        int hour = 0;
        int iMin = 0;
        int second = 0;
        try {

            year = Integer.parseInt(Integer.toHexString(list.get(0))) * 100
                    + Integer.parseInt(Integer.toHexString(list.get(1)));
            month = Integer.parseInt(Integer.toHexString(list.get(2))) - 1;
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return -1;
        }

        try {
            day = Integer.parseInt(Integer.toHexString(list.get(3)));
            hour = Integer.parseInt(Integer.toHexString(list.get(4)));

            iMin = Integer.parseInt(Integer.toHexString(list.get(5)));
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return -1;
        }
        if (list.size() > 6) {
            second = Integer.parseInt(Integer.toHexString(list.get(6)));
        }

        mCalendar.setTimeZone(TimeZone.getDefault());
        mCalendar.set(year, month, day, hour, iMin, second);
        return mCalendar.getTimeInMillis();
    }


    public static String getDeviceId(byte[] bytes) {
        if (null == bytes || bytes.length < 13) return "";
        ArrayList<Byte> lists = new ArrayList<>();
        for (byte b : bytes) {
            lists.add(b);
        }

        StringBuilder append = new StringBuilder();
        append.append(lists.get(0) & 0xff);
        append.append("-");

        append.append((((lists.get(1) & 0xff) << 8) + (lists.get(2) & 0xff)));
        append.append("-");

        append.append((((lists.get(3) & 0xff) << 8) + (lists.get(4) & 0xff)));
        append.append("-");

        append.append((((lists.get(5) & 0xff) << 8) + (lists.get(6) & 0xff)));
        append.append("-");

        append.append((lists.get(7) & 0xff));
        append.append("-");

        append.append((((lists.get(8) & 0xff) << 8) + (lists.get(9) & 0xff)));
        append.append("-");

        append.append((((lists.get(10) & 0xff) << 8) + (lists.get(11) & 0xff)));
        append.append("-");

        append.append((lists.get(12) & 0xff));

        return append.toString();
    }


}
