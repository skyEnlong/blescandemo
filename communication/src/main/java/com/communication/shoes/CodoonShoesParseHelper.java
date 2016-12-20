package com.communication.shoes;

import android.util.Log;

import com.communication.bean.CodoonShoesMinuteModel;
import com.communication.bean.CodoonShoesModel;
import com.communication.bean.CodoonShoesState;
import com.communication.data.CLog;
import com.communication.util.CommonUtils;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;


/**
 * Created by enlong on 2016/12/7.
 */

public class CodoonShoesParseHelper {

    private static final int FREAME_LENGTH = 8;

    private static final int FLAG_START = 0xfa;
    private static final int FLAG_END = 0xfb;
    private static final int FLAG_TOTAL = 0xfc;

    private static final String TAG = "ble_parse";


    /**
     * 解析详情跑步数据
     * @param bytes
     * @return
     */
    public List<CodoonShoesModel> parseData(byte[] bytes) {
        if (null == bytes || bytes.length < 24) return null;
        List<CodoonShoesModel> models = new ArrayList<>();
        CodoonShoesModel model = null;

        int parse_index = 0;

        while (parse_index < bytes.length){
            int start_index = findFlag(FLAG_START, bytes, parse_index, bytes.length);
            if (-1 == start_index) {
                CLog.e(TAG, " not find start_tag");
                return models;
            }
            parse_index = start_index;

            CLog.i(TAG, "===========find start_tag========");

            byte[] arr = Arrays.copyOfRange(bytes, start_index, start_index + FREAME_LENGTH);

            parse_index += FREAME_LENGTH;

            long start = getSysTime(arr);

            model = new CodoonShoesModel();
            model.startDateTime = start;
            model.minutesModels = new ArrayList();


            //找到了汇总信息的标志位，顺序查找，如果是其他TAG， 放弃解析本次运动
            int absTotalIndex = findFlagSeq(FLAG_TOTAL, bytes, parse_index, bytes.length, true);

            if (-1 == absTotalIndex) {
                CLog.e(TAG, " not end start_tag");
                continue;
            }

            CLog.i(TAG, "===========find flag_abstract========");

            //每分钟数据占位两贞
            for (int j = parse_index; j < absTotalIndex - FREAME_LENGTH; j += FREAME_LENGTH * 2) {
                arr = Arrays.copyOfRange(bytes, j, j + FREAME_LENGTH * 2);
                model.minutesModels.add(parseMinuteModel(arr));

            }
            parse_index = absTotalIndex;


            //找到了结束的标志位，顺序查找，如果是其他TAG， 放弃解析本次运动
            int end_flag_index = findFlagSeq(FLAG_END, bytes, parse_index, bytes.length, true);
            if (-1 == absTotalIndex) {
                CLog.e(TAG, " not end start_tag");
                continue;
            }

            CLog.i(TAG, "===========find end_tag========");

            arr = Arrays.copyOfRange(bytes, parse_index, end_flag_index - FREAME_LENGTH);


            parseTotalMode(model, arr);

            parse_index = end_flag_index;
            arr = Arrays.copyOfRange(bytes, parse_index, parse_index + FREAME_LENGTH);

            long endTIme = getSysTime(arr);
            CLog.i(TAG, "find end_time:" + endTIme);
            model.endDateTIme = endTIme;

            models.add(model);

            parse_index += FREAME_LENGTH;
        }


        return models;

    }


    /**
     * 找到对应的标志位，并且返回标志位结束后下一位index
     * 再使用的时候，最好自行减去标识长度
     * @param flag
     * @param allContent
     * @param start_index 查找开始范围
     * @param end_index   查找结束范围
     * @return
     */
    private int findFlag(int flag, byte[] allContent, int start_index, int end_index) {

        return findFlagSeq(flag, allContent, start_index, end_index, false);
    }

    private int findFlagSeq(int flag, byte[] allContent, int start_index, int end_index, boolean isNeedSeq){
        int parse_index = start_index;
        int total = end_index / FREAME_LENGTH;

        byte[] arr = new byte[FREAME_LENGTH];

        int flag_parse = -1;
        for (int i = start_index / FREAME_LENGTH; i < total; i += 1) {

            for (int k = 0; k < FREAME_LENGTH; k++) {
                arr[k] = (byte) (allContent[parse_index++] & 0xff);
            }

            int length = arr.length;
            if (length == FREAME_LENGTH) {

                int count_flag_start = 0;

                int count_flag_total = 0;
                int count_flag_end = 0;
                for (int v : arr) {

                    switch (v & 0xff) {
                        case FLAG_START:
                            count_flag_start++;
                            break;
                        case FLAG_TOTAL:
                            count_flag_total++;
                            break;

                        case FLAG_END:
                            count_flag_end++;
                            break;
                    }

                }


                if (count_flag_end == FREAME_LENGTH) {
                    flag_parse = FLAG_END;
                }else

                if (count_flag_total == FREAME_LENGTH ) {
                    flag_parse = FLAG_TOTAL;

                }else  if (count_flag_start == FREAME_LENGTH ) {
                    flag_parse = FLAG_START;
                }

                if(flag_parse == flag){
                    return  parse_index;
                }else if(isNeedSeq){
                    CLog.e(TAG, "tag not match,quit this sport... ");
                    return -1;
                }
            }
        }
        return -1;
    }

    /**
     * 解析详情数据每分钟的数据
     * @param arr
     * @return
     */
    private CodoonShoesMinuteModel parseMinuteModel(byte[] arr) {

        CodoonShoesMinuteModel model = new CodoonShoesMinuteModel();
        ByteBuffer byteBuffer = ByteBuffer.wrap(arr);
        model.step = byteBuffer.getShort() & 0xff;
        model.distance = (byteBuffer.getShort() & 0xff) / 10.0f;
        model.frontOnStep = byteBuffer.getShort() & 0xff;
        model.backOnStep = byteBuffer.getShort() & 0xff;
        model.inFootCount = byteBuffer.getShort() & 0xff;
        model.outFootCount = byteBuffer.getShort() & 0xff;
        model.cachPower = byteBuffer.getShort() & 0xff;

        CLog.i(TAG, model.toString());
        return model;
    }


    /**
     * 解析每分钟跑步数据的百分比
     * @param arr
     * @return
     */
    public CodoonShoesMinuteModel parseMinutePercents(byte[] arr){
        CodoonShoesMinuteModel model = new CodoonShoesMinuteModel();
        ByteBuffer byteBuffer = ByteBuffer.wrap(arr);
        model.step = byteBuffer.getShort() & 0xff;
        model.inFootCount = byteBuffer.getChar() & 0xff;
        model.outFootCount = byteBuffer.getChar() & 0xff;
        model.frontOnStep = byteBuffer.getChar() & 0xff;
        model.backOnStep = byteBuffer.getChar() & 0xff;
        model.cachPower = byteBuffer.getShort() & 0xff;

        CLog.i(TAG, model.toString());

        return model;
    }

    /**
     * 解析汇总数据
     * @param model
     * @param arr
     */
    public void parseTotalMode(CodoonShoesModel model, byte[] arr) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(arr);
        model.total_dis = byteBuffer.getInt() / 10.0f;
        model.total_cal = byteBuffer.getInt() / 10.0f;
        model.sprintCounts = byteBuffer.getShort() & 0xff;
        model.avgTouchTime = byteBuffer.getShort() & 0xff;
        model.avgHoldTime = byteBuffer.getShort() & 0xff;
        model.flyTime = byteBuffer.getShort() & 0xff;
        /**here to parse avpace**/
        int km =(int) model.total_dis/ 1000;
        for(int i =0; i < km; i++){
            model.paces.add(Long.valueOf(byteBuffer.getShort() & 0xff));
        }
        CLog.i(TAG, model.toString());
    }


    /**
     * 解析跑鞋状态
     * @param bytes
     * @return
     */
    public static CodoonShoesState parseState(byte[] bytes) {
        CodoonShoesState state = new CodoonShoesState();
        state.sportState = bytes[0] & 0xff;
        state.elvationState = bytes[1] & 0xff;
        state.normalStoreState = bytes[2] & 0xff;
        state.runStoreState = bytes[3] & 0xff;
        state.timeState = bytes[4] & 0xff;

        return state;
    }


    /**
     * decode start_time by list data, -1 is err start_time
     *
     * @param arrays
     * @return
     */
    public static long getSysTime(byte[] arrays) {
        Calendar mCalendar = Calendar.getInstance();
        int year = 0;
        int month = 0;
        int day = 0;
        int hour = 0;
        int iMin = 0;
        int second = 0;
        int index = 0;
        try {

            year = 2000
                    + Integer.parseInt(CommonUtils.getHexString(arrays[index++] & 0xff));
            month = Integer.parseInt(CommonUtils.getHexString(arrays[index++] & 0xff)) - 1;
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return -1;
        }

        try {
            day = Integer.parseInt(CommonUtils.getHexString(arrays[index++] & 0xff));
            hour = Integer.parseInt(CommonUtils.getHexString(arrays[index++] & 0xff));

            iMin = Integer.parseInt(CommonUtils.getHexString(arrays[index++] & 0xff));
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return -1;
        }
        second = Integer.parseInt(CommonUtils.getHexString(arrays[index++] & 0xff));

        mCalendar.setTimeZone(TimeZone.getDefault());
        mCalendar.set(year, month, day, hour, iMin, second);

        String format = "yyyy-MM-dd HH:mm:ss";
        CLog.i(TAG, new SimpleDateFormat(format).format(mCalendar.getTime()));
        return mCalendar.getTimeInMillis();
    }


    /**
     * 找到开始状态的帧数
     * @param datas
     * @return
     */
    public int findStartTags(byte[] datas){
        if(null == datas) return  -1;
        int result = findFlag(FLAG_START, datas, 0 , datas.length);

        return result == -1 ? -1 : (result - FREAME_LENGTH) / FREAME_LENGTH;

    }
}
