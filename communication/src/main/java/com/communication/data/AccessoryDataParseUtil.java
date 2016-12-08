package com.communication.data;

import android.content.Context;
import android.util.Log;

import com.communication.provider.SleepDetailDB;
import com.communication.provider.SleepDetailTB;
import com.communication.provider.SportDetailDB;
import com.communication.provider.SportDetailTB;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class AccessoryDataParseUtil extends LocalDecode {
    SleepDetailDB mSleepDetailDB = null;
    SportDetailDB mSportDetailDB = null;

    private int index = 0;
    private static int todaySleep;
    long startSleep = 0;
    long sleepEndtime = 0;

    private final int FLAG_SPORT = 0xfe;
    private final int FLAG_SLEEP = 0xfd;
    private final int FLAT_SLEEP_2 = 0xfb;

    private static AccessoryDataParseUtil instance;
    public AccessoryDataParseUtil(Context context) {
        super(context);
        mSleepDetailDB = new SleepDetailDB(mContext);
        mSportDetailDB = new SportDetailDB(mContext);

    }

    public static AccessoryDataParseUtil getInstance(Context context) {
        if (null == instance) {
            instance = new AccessoryDataParseUtil(context.getApplicationContext());
        }
        return instance;
    }

    public synchronized HashMap<String, AccessoryValues> analysisDatas(byte[] bytes) {
        if (null == bytes || bytes.length < 6) return null;
//        ArrayList<ArrayList<Integer>> newLists = new ArrayList<ArrayList<Integer>>();
//        ArrayList<Integer> data = new ArrayList<Integer>();
//        String mStr = "";
//
//        for (int i = 0; i < bytes.length; i++) {
//            data.add(bytes[i] & 0xff);
//            if (CLog.isDebug)
//                mStr += (bytes[i] & 0xff) + "  ";
//
//            if ((i % 6 == 5)) {
//
//                if (CLog.isDebug)
//                    mStr += "\r\n";
//
//                newLists.add(data);
//                data = new ArrayList<Integer>();
//            }
//        }
//        CLog.i(TAG, mStr);

        return parseCommonDayData(bytes);
    }

    public synchronized HashMap<String, AccessoryValues> analysisDatas(
            ArrayList<ArrayList<Integer>> lists) {
        if (lists == null || lists.size() < 1) {
            return null;
        }

        ArrayList<ArrayList<Integer>> newLists = new ArrayList<ArrayList<Integer>>();
        ArrayList<Integer> data = new ArrayList<Integer>();

        int index = 0;

        for (ArrayList<Integer> arr : lists) {

            for (Integer i : arr) {
                data.add(i);


                index++;
                if (index == 6) {

                    index = 0;
                    newLists.add(data);
                    data = new ArrayList<Integer>();
                }
            }
        }
        lists = null;
        return _analysis(newLists);
    }

    private HashMap<String, AccessoryValues> _analysis(
            ArrayList<ArrayList<Integer>> lists) {
        HashMap<String, AccessoryValues> dataMap = new HashMap<String, AccessoryValues>();
        mSleepDetailDB.open();
        mSleepDetailDB.beginTransaction();

        mSportDetailDB.open();
        mSportDetailDB.beginTransaction();
        startSleep = sleepEndtime = 0;
        String key = "";
        int state = -1;
        ArrayList<Integer> lastArr = null;

        boolean isInvalid = false;
//        delete3DaysAgoData();

        // //////////////////////////////////////////////////////////////////////////////////////
        for (ArrayList<Integer> arr : lists) {

            int length = arr.size();
            if (length == 6) {
                // logic whether head or not
                int FE = 0;
                int FD = 0;
                int FB = 0;
                for (int i : arr) {

                    switch (i){
                        case 0xfe:
                            FE++;
                            break;
                        case 0xfd:
                            FD++;
                            break;
                        case 0xfb:
                            FB++;
                            break;
                    }

                }

                // reset state
                // state = FE == 6 ? SPORTHEAD_STATE : FD == 6 ? SLEEPHEAD_STATE
                // : state;
                if (FE == 6) {
                    state = SPORTHEAD_STATE;
                } else if (FD == 6 || FB == 6) {
                    state = SLEEPHEAD_STATE;
                }

                switch (state) {
                    case SPORTHEAD_STATE:// sport head
                        state = SPORTDATE_STATE;
                        break;

                    case SPORTDATE_STATE:// sport date

                        curTime = getTime(arr);

                        showTime(curTime, "find sport time:");
                        isInvalid = isInvalidTime(curTime);

                        if (!isInvalid) {

                            key = getDateString(curTime);

                            AccessoryValues value = null;
                            if (!dataMap.containsKey(key)) {
                                value = new AccessoryValues();
                                dataMap.put(key, value);
                                value.time = key;
                            } else {
                                value = dataMap.get(key);
                            }

                            if (value.start_sport_time == 0) {

                                value.start_sport_time = curTime;
                            } else {
                                value.start_sport_time = (value.start_sport_time < curTime) ? value.start_sport_time
                                        : curTime;
                            }

                            state = SPORTDATA_STATE;
                        } else {
                            state = INVALID_STATE;
                        }
                        break;

                    case SPORTDATA_STATE: {// sport data


                        int[] sportValues = getSportData(arr);
                        dealDetailWithDB(sportValues, calStartTime(curTime), key);

                        curTime += 600000; // update start_time next 10 min

                        AccessoryValues value = null;
                        if (!dataMap.containsKey(key)) {
                            value = new AccessoryValues();
                            value.time = key;
                            dataMap.put(key, value);
                        } else {
                            value = dataMap.get(key);
                        }

                        if (sportValues[STEP_TYPE] != 0) {

                            value.sport_duration += 10;
                        }
                        value.steps += sportValues[STEP_TYPE];
                        value.calories += sportValues[CALORIE_TYPE];
                        value.distances += sportValues[DISTANCE_TYPE];
                    }
                    break;

                    case SLEEPHEAD_STATE:// sleep head
                        state = SLEEPDATE_STATE;

                        break;

                    case SLEEPDATE_STATE:// sleep date

                        curTime = getTime(arr);
                        isInvalid = isInvalidTime(curTime);
                        if (!isInvalid) {

                            key = getDateString(curTime);
                            AccessoryValues value = null;
                            if (!dataMap.containsKey(key)) {
                                value = new AccessoryValues();
                                dataMap.put(key, value);
                                value.time = key;
                            } else {
                                value = dataMap.get(key);
                            }


                            if (startSleep == 0) {
                                startSleep = calStartTime(curTime);
                            }

                            sleepEndtime = calcuEndtime(curTime);

                            showTime(sleepEndtime, "find sleep state:");

                            state = SLEEPDATA_STATE;

                            value.tmpEndSleep = (value.tmpEndSleep > curTime) ? value.tmpEndSleep : curTime;

                            long realNextStart = calcuEndtime(curTime);
                            long curLastStart = calStartTime(value.tmpEndSleep);

                            value.tmpEndSleep = curTime;
                            String str = "";
                            while (curLastStart >= realNextStart) {
                                try {
                                    Long time = Long.valueOf(realNextStart);
                                    int result = -1;
                                    if (value.sleepdetail.containsKey(time)) {
                                        result = value.sleepdetail.remove(time);
                                    }
//								str +="reduce " + start_time + " result:" + result + "\r\n";
//								Log.d(TAG, "err data have to reduce " + start_time + " result:" + result);

                                    realNextStart += 200 * 1000;
                                    time = Long.valueOf(realNextStart);
                                    if (value.sleepdetail.containsKey(time)) {
                                        result = value.sleepdetail.remove(time);
                                    }
                                    realNextStart += 200 * 1000;

//								Log.d(TAG, "err data have to reduce " + start_time + " result:" + result);
                                    str += "reduce " + time + " result:" + result + "\r\n";

                                    time = Long.valueOf(realNextStart);
                                    if (value.sleepdetail.containsKey(time)) {
                                        result = value.sleepdetail.remove(time);
                                    }
                                    realNextStart += 200 * 1000;
//								Log.d(TAG, "err data have to reduce " + start_time + " result:" + result);
//								str +="reduce " + start_time + " result:" + result + "\r\n";
//								SleepDataUtil.saveInfo2File(str);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }

                        } else {
                            state = INVALID_STATE;
                        }
                        break;

                    case SLEEPDATA_STATE:
                        lastArr = arr;
                        AccessoryValues value = null;
                        key = getDateString(curTime);
                        if (!dataMap.containsKey(key)) {
                            value = new AccessoryValues();
                            dataMap.put(key, value);
                            value.time = key;
                        } else {
                            value = dataMap.get(key);
                        }

                        int[] sleepValues = getSportData(arr);
                        String str = "";
                        long basetime = calStartTime(curTime);
                        for (int i = 0; i < sleepValues.length; i++) {

                            Long time = basetime + i * 200 * 1000;
                            Integer sleepvalue = 0;
                            if (value.sleepdetail.containsKey(time)) {
                                sleepvalue = value.sleepdetail.get(time);
                            }
                            str += " add start_time " + time;
//							sleepvalue = (sleepvalue > sleepValues[i]) ? sleepvalue : sleepValues[i];
                            sleepvalue += sleepValues[i];
                            value.sleepdetail.put(time, sleepvalue);
//							Log.d(TAG, "put data " + start_time );
                        }
                        SleepDataUtil.saveInfo2File(str);
                        value.tmpEndSleep += 600000;

                        sleepEndtime = (sleepEndtime > curTime) ? sleepEndtime : curTime;
                        sleepEndtime = calcuEndtime(sleepEndtime);
                        showTime(sleepEndtime, " data_parse end:");

                        curTime += 600000;

                        break;

                    default:
                        break;
                }
            }


        }
        calcSleepInTransacntion(dataMap);
        getSleepTotalIntrans(AccessoryConfig.userID, startSleep, sleepEndtime, dataMap);

        // add sleep over devider -1
        long dividetime = calStartTime(sleepEndtime);
        insetDB(dividetime, -1);
        dividetime += 200 * 1000;

        insetDB(dividetime, -1);
        dividetime += 200 * 1000;

        insetDB(dividetime, -1);
        dividetime += 200 * 1000;
        sleepEndtime = dividetime;

        calTotalSleep(dataMap);
        Log.d(TAG, "parse over");
        mSleepDetailDB.setTransactionSuccessful();
        mSleepDetailDB.endTransaction();
        mSleepDetailDB.close();

        mSportDetailDB.setTransactionSuccessful();
        mSportDetailDB.endTransaction();
        ;
        mSportDetailDB.close();

        return dataMap;
    }

    private HashMap<String, AccessoryValues> parseCommonDayData(byte[] bytes){
        if (null == bytes) {
            CLog.e(TAG, "bytes null");
            return null;
        }
        int  FREAME_LENGTH = 6;
        if (0 != bytes.length % FREAME_LENGTH) {
            CLog.e(TAG, "bytes length not right");
//            return null;
        }

        HashMap<String, AccessoryValues> dataMap = new HashMap<String, AccessoryValues>();
        mSleepDetailDB.open();
        mSleepDetailDB.beginTransaction();

        mSportDetailDB.open();
        mSportDetailDB.beginTransaction();
        startSleep = sleepEndtime = 0;
        String key = "";
        int state = -1;
        ArrayList<Integer> lastArr = null;

        boolean isInvalid = false;
//        delete3DaysAgoData();
        int parse_index = 0;
        int total = bytes.length / FREAME_LENGTH;

        CLog.i("ble", " total data has frame :" + total);

        // //////////////////////////////////////////////////////////////////////////////////////
        ArrayList<Integer> arr = new ArrayList();
        for (int j = 0; j < total; j++) {

            arr.clear();

            for (int k = 0; k < FREAME_LENGTH; k++) {
                arr.add(bytes[parse_index++] & 0xff);
            }

            int length = arr.size();
            if (length == 6) {
                // logic whether head or not
                int FE = 0;
                int FD = 0;
                int FB = 0;
                for (int i : arr) {

                    switch (i){
                        case 0xfe:
                            FE++;
                            break;
                        case 0xfd:
                            FD++;
                            break;
                        case 0xfb:
                            FB++;
                            break;
                    }

                }

                // reset state
                // state = FE == 6 ? SPORTHEAD_STATE : FD == 6 ? SLEEPHEAD_STATE
                // : state;
                if (FE == 6) {
                    state = SPORTHEAD_STATE;
                } else if (FD == 6 || FB == 6) {
                    state = SLEEPHEAD_STATE;
                }

                switch (state) {
                    case SPORTHEAD_STATE:// sport head
                        state = SPORTDATE_STATE;
                        break;

                    case SPORTDATE_STATE:// sport date

                        curTime = getTime(arr);

                        showTime(curTime, "find sport time:");
                        isInvalid = isInvalidTime(curTime);

                        if (!isInvalid) {

                            key = getDateString(curTime);

                            AccessoryValues value = null;
                            if (!dataMap.containsKey(key)) {
                                value = new AccessoryValues();
                                dataMap.put(key, value);
                                value.time = key;
                            } else {
                                value = dataMap.get(key);
                            }

                            if (value.start_sport_time == 0) {

                                value.start_sport_time = curTime;
                            } else {
                                value.start_sport_time = (value.start_sport_time < curTime) ? value.start_sport_time
                                        : curTime;
                            }

                            state = SPORTDATA_STATE;
                        } else {
                            state = INVALID_STATE;
                        }
                        break;

                    case SPORTDATA_STATE: {// sport data


                        int[] sportValues = getSportData(arr);
                        dealDetailWithDB(sportValues, calStartTime(curTime), key);

                        curTime += 600000; // update start_time next 10 min

                        AccessoryValues value = null;
                        if (!dataMap.containsKey(key)) {
                            value = new AccessoryValues();
                            value.time = key;
                            dataMap.put(key, value);
                        } else {
                            value = dataMap.get(key);
                        }

                        if (sportValues[STEP_TYPE] != 0) {

                            value.sport_duration += 10;
                        }
                        value.steps += sportValues[STEP_TYPE];
                        value.calories += sportValues[CALORIE_TYPE];
                        value.distances += sportValues[DISTANCE_TYPE];
                    }
                    break;

                    case SLEEPHEAD_STATE:// sleep head
                        state = SLEEPDATE_STATE;

                        break;

                    case SLEEPDATE_STATE:// sleep date

                        curTime = getTime(arr);
                        isInvalid = isInvalidTime(curTime);
                        if (!isInvalid) {

                            key = getDateString(curTime);
                            AccessoryValues value = null;
                            if (!dataMap.containsKey(key)) {
                                value = new AccessoryValues();
                                dataMap.put(key, value);
                                value.time = key;
                            } else {
                                value = dataMap.get(key);
                            }


                            if (startSleep == 0) {
                                startSleep = calStartTime(curTime);
                            }

                            sleepEndtime = calcuEndtime(curTime);

                            showTime(sleepEndtime, "find sleep state:");

                            state = SLEEPDATA_STATE;

                            value.tmpEndSleep = (value.tmpEndSleep > curTime) ? value.tmpEndSleep : curTime;

                            long realNextStart = calcuEndtime(curTime);
                            long curLastStart = calStartTime(value.tmpEndSleep);

                            value.tmpEndSleep = curTime;
                            String str = "";
                            while (curLastStart >= realNextStart) {
                                try {
                                    Long time = Long.valueOf(realNextStart);
                                    int result = -1;
                                    if (value.sleepdetail.containsKey(time)) {
                                        result = value.sleepdetail.remove(time);
                                    }
//								str +="reduce " + start_time + " result:" + result + "\r\n";
//								Log.d(TAG, "err data have to reduce " + start_time + " result:" + result);

                                    realNextStart += 200 * 1000;
                                    time = Long.valueOf(realNextStart);
                                    if (value.sleepdetail.containsKey(time)) {
                                        result = value.sleepdetail.remove(time);
                                    }
                                    realNextStart += 200 * 1000;

//								Log.d(TAG, "err data have to reduce " + start_time + " result:" + result);
                                    str += "reduce " + time + " result:" + result + "\r\n";

                                    time = Long.valueOf(realNextStart);
                                    if (value.sleepdetail.containsKey(time)) {
                                        result = value.sleepdetail.remove(time);
                                    }
                                    realNextStart += 200 * 1000;
//								Log.d(TAG, "err data have to reduce " + start_time + " result:" + result);
//								str +="reduce " + start_time + " result:" + result + "\r\n";
//								SleepDataUtil.saveInfo2File(str);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }

                        } else {
                            state = INVALID_STATE;
                        }
                        break;

                    case SLEEPDATA_STATE:
                        lastArr = arr;
                        AccessoryValues value = null;
                        key = getDateString(curTime);
                        if (!dataMap.containsKey(key)) {
                            value = new AccessoryValues();
                            dataMap.put(key, value);
                            value.time = key;
                        } else {
                            value = dataMap.get(key);
                        }

                        int[] sleepValues = getSportData(arr);
                        String str = "";
                        long basetime = calStartTime(curTime);
                        for (int i = 0; i < sleepValues.length; i++) {

                            Long time = basetime + i * 200 * 1000;
                            Integer sleepvalue = 0;
                            if (value.sleepdetail.containsKey(time)) {
                                sleepvalue = value.sleepdetail.get(time);
                            }
                            str += " add start_time " + time;
//							sleepvalue = (sleepvalue > sleepValues[i]) ? sleepvalue : sleepValues[i];
                            sleepvalue += sleepValues[i];
                            value.sleepdetail.put(time, sleepvalue);
                         }
                        SleepDataUtil.saveInfo2File(str);
                        value.tmpEndSleep += 600000;

                        sleepEndtime = (sleepEndtime > curTime) ? sleepEndtime : curTime;
                        sleepEndtime = calcuEndtime(sleepEndtime);
                        showTime(sleepEndtime, " data_parse end:");

                        curTime += 600000;

                        break;

                    default:
                        break;
                }
            }


        }

        if(startSleep != sleepEndtime ){
            CLog.i("ble", " find sleep");
            calcSleepInTransacntion(dataMap);
            getSleepTotalIntrans(AccessoryConfig.userID, startSleep, sleepEndtime, dataMap);

            // add sleep over devider -1
            long dividetime = calStartTime(sleepEndtime);
            insetDB(dividetime, -1);
            dividetime += 200 * 1000;

            insetDB(dividetime, -1);
            dividetime += 200 * 1000;

            insetDB(dividetime, -1);
            dividetime += 200 * 1000;
            sleepEndtime = dividetime;

            calTotalSleep(dataMap);
        }

        CLog.i(TAG, "parse over");
        mSleepDetailDB.setTransactionSuccessful();
        mSleepDetailDB.endTransaction();
        mSleepDetailDB.close();

        mSportDetailDB.setTransactionSuccessful();
        mSportDetailDB.endTransaction();

        mSportDetailDB.close();

        return dataMap;
    }

    private void calTotalSleep(HashMap<String, AccessoryValues> dataMap) {
        // TODO Auto-generated method stub
        if (null != dataMap && dataMap.size() > 0) {
            String key = getDateString(System.currentTimeMillis());
            if (dataMap.containsKey(key)) {
                AccessoryValues today = dataMap.get(key);
                long start = today.sleep_startTime;

                Set<String> keyset = dataMap.keySet();
                Iterator<String> iter = keyset.iterator();
                while (iter.hasNext()) {
                    String keydata = iter.next();
                    AccessoryValues values = dataMap.get(keydata);

                    CLog.i(TAG, values.toString());

                    HashMap<Long, Integer> sleepdetail = values.sleepdetail;
                    if (null != sleepdetail && sleepdetail.size() > 0) {
                        Set<Long> time_key = sleepdetail.keySet();
                        for (Long t : time_key) {
                            if (t.longValue() >= start) {
                                todaySleep += 200;
                            }
                        }
                    }
                }

            }
        }
    }

    public void getSleepTotalIntrans(String userid, long start, long end, HashMap<String, AccessoryValues> data) {
        List<SleepDetailTB> list = mSleepDetailDB.get(userid, start, end);

        if (null != list && list.size() > 0) {
            int size = list.size();


            List<AccessoryValues> values = new ArrayList<AccessoryValues>();
            AccessoryValues mv = new AccessoryValues();
            mv.sleep_startTime = start;
            mv.sleep_endTime = end;

            for (int i = 0; i < size; i++) {

                SleepDetailTB tb = list.get(i);

                if (mv.sleep_startTime == 0 && tb.time != 0) {
                    mv.sleep_startTime = mv.sleep_endTime = tb.time;
                } else if (tb.type != -1) {
                    mv.sleep_endTime = (mv.sleep_endTime < tb.time) ? tb.time : mv.sleep_endTime;
                } else if (tb.type == -1) {
                    mv.sleep_endTime = (mv.sleep_endTime < tb.time) ? tb.time : mv.sleep_endTime;
                    if (0 != mv.sleep_startTime) {

                        values.add(mv);
                        mv = new AccessoryValues();
                    }
                }
            }

            if (values.size() == 0) {
                values.add(mv);
            }

            String str1 = " find start begin from:" + start + " end:" + end;
            Log.d(TAG, str1);


//			String str = "";
            for (int i = 0; i < values.size(); i++) {

                mv = values.get(i);
//				str += mv.sleep_startTime + " , "+ mv.sleep_endTime + " ; ";

                String key = getDateString(mv.sleep_endTime);
                AccessoryValues sd = null;
                if (!data.containsKey(key)) {
                    sd = new AccessoryValues();
                    data.put(key, sd);
                    sd.time = key;
                } else {
                    sd = data.get(key);
                }

                if (sd.sleep_startTime != 0) {

                    sd.sleep_startTime = (mv.sleep_startTime < sd.sleep_startTime) ? mv.sleep_startTime : sd.sleep_startTime;
                } else {
                    sd.sleep_startTime = mv.sleep_startTime;
                }
                sd.sleep_endTime = (sd.sleep_endTime > mv.sleep_endTime) ? sd.sleep_endTime : mv.sleep_endTime;
            }
//			SleepDataUtil.saveInfo2File(str);

            if (values.size() == 0) {
                Log.e(TAG, "not find start or end");
            }
        } else {
            Log.e(TAG, "not find sleep detail between " + start + " end " + end);
        }

        return;
    }


    private void calcSleepInTransacntion(HashMap<String, AccessoryValues> dataMap) {
        if (null == dataMap || dataMap.size() == 0) {
            return;
        }
        long systime = calcuEndtime(System.currentTimeMillis());
        Set<String> keyset = dataMap.keySet();
        Iterator<String> iter = keyset.iterator();
        todaySleep = 0;
        while (iter.hasNext()) {
            String keydata = iter.next();
            AccessoryValues values = dataMap.get(keydata);

            values.sleep_startTime = values.sleep_endTime = 0;


            long dividetime = calcuEndtime(values.start_sport_time);
            insetDB(dividetime, -1);
            dividetime += 200 * 1000;
            insetDB(dividetime, -1);
            dividetime += 200 * 1000;
            insetDB(dividetime, -1);
            dividetime += 200 * 1000;

            Set<Long> keylong = values.sleepdetail.keySet();
            Iterator<Long> it = keylong.iterator();
            while (it.hasNext()) {
                Long st = it.next();
                if (st < systime) {
                    int sleepValue = values.sleepdetail.get(st);
                    insetDB(st, sleepValue);
                }
            }

        }

    }

    public static long calcuEndtime(long time) {
        int add = 600000;
        if (time % add == 0) {
            add = 0;
        }
        return time / 600000 * 600000 + add;
    }


    public static long calStartTime(long time) {
        return time / 600000 * 600000;
    }


    public SleepDetailTB insetDB(long time, int sleepValue) {
//		Log.d(TAG, " insetDB begin");
        SleepDetailTB table = null;
        table = mSleepDetailDB.get(AccessoryConfig.userID, time);
        if (null == table) {
            table = new SleepDetailTB();
            table.time = time;
            table.userid = AccessoryConfig.userID;
            table.sleepvalue = sleepValue;
            table.type = getSleepLevelByTime(table.sleepvalue);

            mSleepDetailDB.insert(table);
        } else {
            table.time = time;
            table.userid = AccessoryConfig.userID;
            if (sleepValue != -1) {

                table.sleepvalue = (table.sleepvalue > sleepValue) ? table.sleepvalue : sleepValue;               //ֱ�Ӹ���
            } else {
                table.sleepvalue = -1;
            }
            table.type = getSleepLevelByTime(table.sleepvalue);

            mSleepDetailDB.update(table);
        }

        return table;
    }

    private String getDateString(long curTime2) {
        // TODO Auto-generated method stub
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(new Date(curTime2));
    }

    /**
     * 0 curday_StartTime,1 curday_During,2 cur_Steps, 3 cur_Calories, 4
     * cur_Metes, 5 total_StartTime,6 total_During, 7 total_Steps,8
     * total_Calories,9 total_Metes,10 deepSleepValue, 11 lightSleepValue, 12
     * wakeSleepValue, 13 sleepTotaltime, 14 cur_SleepStartTime, 15
     * sleep_end_time, 16 total_sleep
     **/
    public static long[] getCurrentData(HashMap<String, AccessoryValues> data_map) {
        if (null != data_map && data_map.size() > 0) {
            long[] data = new long[17];
            for (int i = 0; i < data.length; i++) {
                data[i] = 0;
            }
            String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date(
                    System.currentTimeMillis()));
            if (data_map.containsKey(date)) {
                AccessoryValues today = data_map.get(date);
                data[0] = today.start_sport_time;
                data[1] = today.sport_duration;
                data[2] = today.steps;
                data[3] = today.calories;
                data[4] = today.distances;
                data[10] = today.deepSleep;
                data[11] = today.light_sleep;
                data[12] = today.wake_time;
                data[13] = todaySleep / 60;
                data[14] = today.sleep_startTime;
                data[15] = today.sleep_endTime;
            }
            Set<String> keyset = data_map.keySet();
            Iterator<String> iter = keyset.iterator();
            while (iter.hasNext()) {
                AccessoryValues values = data_map.get(iter.next());
                if (data[5] == 0) {
                    data[5] = values.start_sport_time;
                } else {
                    data[5] = (data[5] < values.start_sport_time) ? data[5]
                            : values.start_sport_time;
                }

                data[6] += values.sport_duration;
                data[7] += values.steps;
                data[8] += values.calories;
                data[9] += values.distances;
                data[16] += values.sleepmins;
            }
            todaySleep = 0;
            return data;
        }

        return null;
    }


//    /**
//     * get the start_time between in start_time % 10 min to (start_time + 10)% 10
//     *@param start_time
//     * @param arr
//     * @return
//     */
//    protected int[] getSportData(ArrayList<Integer> arr, long start_time, String date) {
//        int[] result = getSportData(arr);
//        dealDetailWithDB(result, start_time, date);
//        return result;
//    }

    /**
     * save to db
     *
     * @param result
     * @param time
     * @param date
     */
    private void dealDetailWithDB(int[] result, long time, String date) {

        SportDetailTB table = null;
        CLog.i(TAG, "deal sport data:" + date + result[0] + " " + result[1] + " " +result[2]);
        table = mSportDetailDB.get(AccessoryConfig.userID, time);
        if (null == table) {
            table = new SportDetailTB();
            table.time = time;
            table.userid = AccessoryConfig.userID;
            table.step_value = result[STEP_TYPE];
            table.calorie = result[CALORIE_TYPE];
            table.distance = result[DISTANCE_TYPE];
            table.date = date;
            mSportDetailDB.insert(table);
        } else {
            table.time = time;
            table.userid = AccessoryConfig.userID;
            table.date = date;
            if (table.step_value < result[STEP_TYPE]) {
                // get the bigger value
                table.step_value = result[STEP_TYPE];
                table.calorie = result[CALORIE_TYPE];
                table.distance = result[DISTANCE_TYPE];

                mSportDetailDB.update(table);
            } else {
                // update the total cost
                result[STEP_TYPE] = table.step_value;
                result[CALORIE_TYPE] = table.calorie;
                result[DISTANCE_TYPE] = table.distance;
            }

        }

    }

    private void showTime(long time, String pre) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        CLog.i(TAG, pre + " " + format.format(new Date(time)));
    }


    private void delete3DaysAgoData() {
        long threeDaysTimeAgo = System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000;
        mSleepDetailDB.deleteByUserId(AccessoryConfig.userID, threeDaysTimeAgo);
        mSportDetailDB.deleteByUserId(AccessoryConfig.userID, threeDaysTimeAgo);
    }

}
