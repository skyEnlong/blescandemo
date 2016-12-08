package com.communication.data;

import android.content.Context;

import com.communication.provider.SportDetailDB;
import com.communication.provider.SportDetailTB;

import java.util.List;

/**
 * Created by workEnlong on 2015/3/13.
 */
public class AccessorySportDetailManager {
    private SportDetailDB mSportDetailDB = null;
    protected static  final int STEP_TYPE = 0, CALORIE_TYPE = 1, DISTANCE_TYPE = 2;

    public AccessorySportDetailManager(Context mContext){
        mSportDetailDB = new SportDetailDB(mContext.getApplicationContext());
    }


    public SportDetailTB getTenMinuteData(String user_id, long begin_time){
        long tmp_time = begin_time / 600000 * 600000;
        mSportDetailDB.open();
        SportDetailTB table = null;
        table =	mSportDetailDB.get( AccessoryConfig.userID, tmp_time);
        mSportDetailDB.close();
        return table;
    }

    public void update(SportDetailTB tb){
        mSportDetailDB.open();
        mSportDetailDB.update(tb);
        mSportDetailDB.close();
    }


    /**
     * 将数据更新到数据库，并返回得到去重值
     * @param user_id
     * @param result
     * @param time
     */
    public  void updateDetailWithDB(String user_id, int[] result, String date,long time) {
        mSportDetailDB.open();
        updateDetailWithDBIntrans(user_id, result, date, time);
        mSportDetailDB.close();
    }


    /**
     * 将数据更新到数据库，并返回得到去重值
     * @param user_id
     * @param result
     * @param time
     */
    public  void updateDetailWithDBIntrans(String user_id, int[] result, String date,long time) {
        SportDetailTB table = null;
        table =	mSportDetailDB.get(user_id, time);
        if(null == table){
            table = new SportDetailTB();
            table.time = time;
            table.userid = user_id;
            table.step_value  = result[STEP_TYPE];
            table.calorie = result[CALORIE_TYPE];
            table.distance = result[DISTANCE_TYPE];
            table.date = date;
            mSportDetailDB.insert(table);
        }else{
            table.time = time;
            table.userid = user_id;
            table.date = date;

            if(table.step_value <  result[STEP_TYPE]){
                // get the bigger value
                table.step_value =  result[STEP_TYPE];
                table.calorie =  result[CALORIE_TYPE];
                table.distance =   result[DISTANCE_TYPE];

                mSportDetailDB.update(table);
            }else{
                // update the total cost
                result[STEP_TYPE] = table.step_value;
                result[CALORIE_TYPE] = table.calorie ;
                result[DISTANCE_TYPE] = table.distance ;
            }

        }

    }

    /**
     * delete all data below start_time
     * @param user_id
     * @param time
     * @return
     */
    public void deleteDataBelowTime(String user_id, long time){
        mSportDetailDB.open();
        mSportDetailDB.deleteByUserId(user_id, time);
        mSportDetailDB.close();
    }

    /**
     *get total data between start and end
     * @param user_id
     * @param start_time
     * @param end_time
     */
    public SportDetailTB getTotalDataByDate(String user_id, long start_time, long end_time){
        mSportDetailDB.open();
        SportDetailTB tb = mSportDetailDB.getTotal(user_id, start_time, end_time);
        mSportDetailDB.close();
        return  tb;
    }

    /**
     * 获取某天的详情
     * @param user_id
     * @param date
     * @return
     */
    public List<SportDetailTB> getDateData(String user_id, String date){
        mSportDetailDB.open();
        List<SportDetailTB> list = mSportDetailDB.getDateDetail(user_id, date);
        mSportDetailDB.close();
        return list;
    }


    /**
     * 获取某一天的总值
     * @param user_id
     * @param date
     * @return
     */
    public AccessoryValues getDateTotal(String user_id, String date){
        mSportDetailDB.open();
        AccessoryValues list = mSportDetailDB.getDateTotal(user_id, date);
        mSportDetailDB.close();
        return list;
    }

    /**
     * 获取所有的日期
     * @param user_id
     * @return
     */
    public List<String> getAllDate(String user_id){
        mSportDetailDB.open();
        List<String> list = mSportDetailDB.getAllDate(user_id);
        mSportDetailDB.close();
        return list;
    }

    /**
     * delete all data before end_time
     * @param user_id
     * @param end_time
     */
    public void deleteData(String user_id, long end_time){
        mSportDetailDB.open();
        mSportDetailDB.deleteByUserId(user_id, end_time);
        mSportDetailDB.close();
    }


    /**
     * delete special date data
     * @param user_id
     * @param date
     * @return
     */
    public int deleteDateData(String user_id, String date){
        mSportDetailDB.open();
        int l = mSportDetailDB.deleteDateData( user_id, date);
        mSportDetailDB.close();
        return l;
    }

    public void updateAnonymous(String user_id){
        mSportDetailDB.open();
        mSportDetailDB.updateAnonymous(user_id);
        mSportDetailDB.close();
    }



    public void open() {
        mSportDetailDB.open();
    }

    public void close() {
        mSportDetailDB.close();
    }

    public void beginTransaction() {
        mSportDetailDB.beginTransaction();
    }

    public void setTransactionSuccessful() {
        mSportDetailDB.setTransactionSuccessful();
    }

    public void endTransaction() {
        mSportDetailDB.endTransaction();
    }
}
