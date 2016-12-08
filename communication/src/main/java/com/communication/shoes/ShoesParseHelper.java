package com.communication.shoes;

import com.communication.bean.ShoseDataSummary;
import com.communication.bean.ShoseDataDetail;
import com.communication.data.CLog;
import com.communication.data.DataUtil;
import com.communication.gpsband.GpsBandParseUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by workEnlong on 2016/1/21.
 */
public class ShoesParseHelper {

    private static final int FREAME_LENGTH = 0x10;
    private static final int FLAG_RECORD = 0xfe;
    private static final int STATE_TIME = 1;
    private static final int STATE_CONTENT = 2;
    private static final int STATE_RECORD = 0;
    private static final String TAG = "ble_parse";

    final static int flagCount = 8;

    public static ShoseDataSummary parseSummary(List<Integer> data) {
        ShoseDataSummary summary = null;
        if (null == data || data.size() < 16) return summary;
        summary = new ShoseDataSummary();

        int index = 0;
        summary.walk_steps = ((data.get(index) & 0xff) << 8) +
                (data.get(index + 1) & 0xff);
        summary.walk_steps *= 10;

        index += 2;
        summary.walk_distance = ((data.get(index) & 0xff) << 8) +
                (data.get(index + 1) & 0xff);
        summary.walk_distance *= 10;

        index += 2;
        summary.walk_duration = ((data.get(index) & 0xff) << 8) +
                (data.get(index + 1) & 0xff);

        index += 2;
        summary.run_steps = ((data.get(index) & 0xff) << 8) +
                (data.get(index + 1) & 0xff);
        summary.run_steps *= 10;

        index += 2;
        summary.run_distance = ((data.get(index) & 0xff) << 8) +
                (data.get(index + 1) & 0xff);
        summary.run_distance *= 10;

        index += 2;
        summary.run_duration = ((data.get(index) & 0xff) << 8) +
                (data.get(index + 1) & 0xff);

        index += 2;
        summary.total_steps = ((data.get(index) & 0xff) << 24) +
                ((data.get(index + 1) & 0xff) << 16) + ((data.get(index + 2) & 0xff) << 8) +
                (data.get(index + 3) & 0xff);

        summary.total_duration = summary.run_duration + summary.walk_duration;
        summary.total_distance = summary.run_distance + summary.walk_distance;
        summary.run_steps = summary.total_steps - summary.walk_steps;
        ;
        summary.day_string = new SimpleDateFormat("yyyy-MM-dd").
                format(new Date(System.currentTimeMillis()));

        CLog.i(TAG, summary.toString());
        return summary;
    }


    public static List<ShoseDataDetail> parseShoseDetail(ArrayList<ArrayList<Integer>> lists) {
        if (null == lists || lists.size() == 0) return null;

        appendEndTag(lists);

        int state = -1;

        int fre_max = 0;
        int fre_avg = 0;
        int len_max = 0;
        int len_avg = 0;
        double speed_max = 0;
        double speed_avg = 0;

        int touchdown_half = 0;
        int touchdown_after = 0;
        int gait_pigeon = 0;
        int gait_toe_out = 0;

        int total_time = 0;
        int total_dis = 0;
        int total_step = 0;

        boolean isValidItem = false;
        int validFreCount = 0;

        List<ShoseDataDetail> details = new ArrayList<>();
        ShoseDataDetail detail = null;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (ArrayList<Integer> arr : lists) {
            int length = arr.size();
            if (length == FREAME_LENGTH) {
                int count_record = 0;
                for (int v : arr) {
                    if ((v & 0xff) == FLAG_RECORD) {
                        count_record++;

                        if (count_record == flagCount) {// has eight 0xfe, start parse
                            state = STATE_RECORD;
                            break;
                        }

                    } else {
                        break;
                    }
                }
            }


            switch (state) {
                case STATE_RECORD:
                    if (null == detail) {

                        detail = new ShoseDataDetail();
                        detail.stride_frequency_list = new ArrayList<>();
                        detail.stride_length_list = new ArrayList<>();
                        detail.stride_speed_list = new ArrayList<>();
                        detail.gait_list = new ArrayList<>();
                        detail.touchdown_list = new ArrayList<>();

                    } else {
                        detail.gait_pigeon_avg = gait_pigeon / detail.gait_list.size();
                        detail.gait_toe_out_avg = gait_toe_out / detail.gait_list.size();

                        detail.stride_frequency_avg = fre_avg / detail.stride_frequency_list.size();
                        detail.stride_length_avg = len_avg / detail.stride_length_list.size();
                        detail.stride_speed_avg = speed_avg / detail.stride_speed_list.size();

                        detail.touchdown_after_avg = touchdown_after / detail.touchdown_list.size();
                        detail.touchdown_half_avg = touchdown_half / detail.touchdown_list.size();

                        detail.stride_frequency_max = fre_max;
                        detail.stride_length_max = len_max;
                        detail.stride_speed_max = speed_max;

                        detail.total_distance = total_dis / 100.f;
                        detail.total_duration = total_time;
                        detail.total_steps = total_step;

                        if (detail.total_distance >= 10 && isValidItem) {
                            // only record above 10 m's
                            details.add(detail);
                        } else {
                            CLog.e(TAG, "isValidItem:" + isValidItem);
                        }

                        CLog.i(TAG, detail.toString());
                        isValidItem = false;

                        detail = new ShoseDataDetail();
                        detail.stride_frequency_list = new ArrayList<>();
                        detail.stride_length_list = new ArrayList<>();
                        detail.stride_speed_list = new ArrayList<>();
                        detail.gait_list = new ArrayList<>();
                        detail.touchdown_list = new ArrayList<>();
                        detail.time_array = arr;
                        total_time = 0;
                        total_dis = 0;
                        total_step = 0;
                        fre_max = 0;
                        fre_avg = 0;
                        len_max = 0;
                        len_avg = 0;
                        speed_max = 0;
                        speed_avg = 0;

                        touchdown_half = 0;
                        touchdown_after = 0;
                        gait_pigeon = 0;
                        gait_toe_out = 0;
                    }

                    long time = GpsBandParseUtil.getSysTime(
                            new ArrayList<Integer>(arr.subList(flagCount, flagCount + 7)));
                    detail.start_date = format.format(new Date(time));
                    state = STATE_CONTENT;
                    break;

                case STATE_CONTENT:

                    int index = 0;
                    for (index = 0; index < 10; index += 2) {
                        int fre = arr.get(index) & 0xff;
                        int ln = arr.get(index + 1) & 0xff;

                        ln = (ln > 140) ? 140 : ln; // 过滤处理， 步幅上限为1.4m

                        if (!isValidItem)
                            isValidItem = fre > 130;       // check fre , once  get 130 up, it's run

                        double speed = ln * fre * 0.0006f;

                        fre_max = (fre_max > fre) ? fre_max : fre;
                        len_max = (len_max > ln) ? len_max : ln;
                        speed_max = (speed_max > speed) ? speed_max : speed;

                        fre_avg += fre;
                        len_avg += ln;
                        speed_avg += speed;

                        total_dis += ln * fre;
                        total_step += fre;

                        detail.stride_frequency_list.add(fre);
                        detail.stride_length_list.add(ln);
                        detail.stride_speed_list.add(speed);
                    }
                    total_time += 5 * 60; //ever item is 5 minute

                    int frn = arr.get(index++);
                    int mrn = arr.get(index++);
                    int brn = arr.get(index++);


                    touchdown_half += frn;
                    touchdown_after += brn;


                    List<Integer> touchList = new ArrayList<>();
                    touchList.add(frn);
                    touchList.add(mrn);
                    touchList.add(brn);
                    detail.touchdown_list.add(touchList);

                    CLog.i(TAG, "touchList:");
                    DataUtil.DebugPrint(touchList);

                    int irn = arr.get(index++);
                    int rrn = arr.get(index++);
                    int orn = arr.get(index++);

                    List<Integer> gaitList = new ArrayList<>();
                    gaitList.add(irn);
                    gaitList.add(rrn);
                    gaitList.add(orn);
                    detail.gait_list.add(gaitList);

                    gait_pigeon += irn;
                    gait_toe_out += orn;

                    CLog.i(TAG, "gaitList:");
                    DataUtil.DebugPrint(gaitList);


                    break;
            }
        }


        return details;
    }

    private static void appendEndTag(ArrayList<ArrayList<Integer>> lists) {
        if (lists.size() < 2) return;

        ArrayList<Integer> last = lists.get(lists.size() - 1);
        boolean isHasEndTag = false;

        int length = last.size();
        if (length == FREAME_LENGTH) {
            int count_record = 0;
            for (int v : last) {
                if ((v & 0xff) == FLAG_RECORD) {
                    count_record++;

                    if (count_record == flagCount) {// has eight 0xfe, start parse
                        isHasEndTag = true;
                        break;
                    }
                }
            }
        }

        CLog.i(TAG, "not find end tag ");

        if (!isHasEndTag) {
            ArrayList<Integer> end = new ArrayList<>();
            for (int i = 0; i < FREAME_LENGTH / 2; i++) {
                end.add(FLAG_RECORD);
            }

            for (int i = FREAME_LENGTH / 2; i < FREAME_LENGTH; i++) {
                end.add(0);
            }

            lists.add(end);
        }

    }


    public  static  int CalCrc(byte[]  p_data, int size, int  p_crc) {
        int i;
        int crc = (p_crc == 0) ? 0xffff : p_crc;
        for (i = 0; i < size; i++)
        {
            crc = (0xff & (crc >> 8)) | ((crc & 0xff) << 8);
            crc ^= p_data[i] & 0xff;
            crc ^= (crc & 0xff) >> 4;
            crc ^= (crc << 8) << 4;
            crc ^= ((crc & 0xff) << 4) << 1;
        }
        return crc & 0xffff;

    }
}