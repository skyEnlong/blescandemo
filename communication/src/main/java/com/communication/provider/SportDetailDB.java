package com.communication.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;

import com.communication.data.AccessoryValues;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by workEnlong on 2015/2/25.
 */
public class SportDetailDB extends AccessoryDataBaseHelper {
    public static final String DATABASE_TABLE = "sport_detail";
    public static final String COLUMN_ID = BaseColumns._ID;
    public static final String COLUMN_USERID = "userid";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_STEP = "step_count";
    public static final String COLUMN_CALORIE = "calorie";
    public static final String COLUMN_DISTANCE = "distance";

    public static final String COLUMN_DATE = "date";

    public static final String createTableSql = "create table "
            + " IF NOT EXISTS " + DATABASE_TABLE + "(" + COLUMN_ID
            + " integer primary key autoincrement not null," + COLUMN_USERID
            + " NVARCHAR(100) not null," + COLUMN_TIME
            + " integer ," + COLUMN_DATE + " NVARCHAR(20), " + COLUMN_STEP
            + " integer ," + COLUMN_DISTANCE
            + " integer , " + COLUMN_CALORIE
            + " integer )";

    public static final String[] dispColumns = {COLUMN_ID, COLUMN_USERID,
            COLUMN_TIME, COLUMN_STEP, COLUMN_CALORIE, COLUMN_DISTANCE, COLUMN_DATE};

    public SportDetailDB(Context mContext) {
        super(mContext);
    }

    public long insert(SportDetailTB mod) {
        long count = 0;
        ContentValues initialValues = new ContentValues();
        initialValues.put(COLUMN_USERID, mod.userid);
        initialValues.put(COLUMN_TIME, mod.time);
        initialValues.put(COLUMN_STEP, mod.step_value);
        initialValues.put(COLUMN_DISTANCE, mod.distance);
        initialValues.put(COLUMN_CALORIE, mod.calorie);
        initialValues.put(COLUMN_DATE, mod.date);

        count = db.insert(DATABASE_TABLE, null, initialValues);
        return count;
    }


    public List<SportDetailTB> get(String userid, long start, long end) {
        String where = COLUMN_USERID + " ='" + userid + "' and " + COLUMN_TIME
                + " < " + end + " and " + COLUMN_TIME + " >= " + start;
        Cursor c = null;

        List<SportDetailTB> list = null;
        try {
           c =  db.query(DATABASE_TABLE, dispColumns, where, null, null,
                    null, COLUMN_TIME + " ASC");

            if (c == null) {
                return null;
            }

            if (c.moveToFirst()) {
                list = new ArrayList<SportDetailTB>(c.getCount());
                do {
                    SportDetailTB mod = new SportDetailTB();

                    mod.ID = c.getInt(c.getColumnIndex(COLUMN_ID));
                    mod.userid = c.getString(c.getColumnIndex(COLUMN_USERID));
                    mod.time = c.getLong(c.getColumnIndex(COLUMN_TIME));
                    mod.distance = c.getInt(c.getColumnIndex(COLUMN_DISTANCE));
                    mod.step_value = c.getInt(c.getColumnIndex(COLUMN_STEP));
                    mod.calorie = c.getInt(c.getColumnIndex(COLUMN_CALORIE));
                    mod.date = c.getString(c.getColumnIndex(COLUMN_DATE));

                    list.add(mod);
                } while (c.moveToNext());
            }
        } catch (Exception e) {

        } finally {
            if(c != null)
            c.close();
        }
        return list;
    }


    /**
     * 获取某天的详情
     * @param userid
     * @param date
     * @return
     */
    public List<SportDetailTB> getDateDetail(String userid, String date) {
        String where = COLUMN_USERID + " ='" + userid + "' and " + COLUMN_DATE
                + " ='" + date + "'" ;
        Cursor c = db.query(DATABASE_TABLE, dispColumns, where, null, null,
                null, COLUMN_TIME + " ASC");
        if (c == null) {
            return null;
        }
        List<SportDetailTB> list = null;
        boolean find_first_0 = false;
        try {
            if (c.moveToFirst()) {
                list = new ArrayList<SportDetailTB>(c.getCount());
                do {
                    SportDetailTB mod = new SportDetailTB();

                    mod.ID = c.getInt(c.getColumnIndex(COLUMN_ID));
                    mod.userid = c.getString(c.getColumnIndex(COLUMN_USERID));
                    mod.time = c.getLong(c.getColumnIndex(COLUMN_TIME));
                    mod.distance = c.getInt(c.getColumnIndex(COLUMN_DISTANCE));
                    mod.step_value = c.getInt(c.getColumnIndex(COLUMN_STEP));
                    mod.calorie = c.getInt(c.getColumnIndex(COLUMN_CALORIE));
                    mod.date = c.getString(c.getColumnIndex(COLUMN_DATE));

                    if(!find_first_0 &&  mod.step_value == 0){
                        continue;
                    }

                    if( mod.step_value != 0){  // 找到了第一个!0
                        find_first_0 = true;
                    }

                    list.add(mod);
                } while (c.moveToNext());
            }
        } catch (IllegalStateException e) {

        } finally {
            c.close();
        }
        return list;
    }



    public SportDetailTB getTotal(String userid, long start, long end) {
        String where = COLUMN_USERID + " ='" + userid + "' and " + COLUMN_TIME
                + " < " + end + " and " + COLUMN_TIME + " >= " + start;
        Cursor c = db.query(DATABASE_TABLE, dispColumns, where, null, null,
                null, COLUMN_TIME + " ASC");
        if (c == null) {
            return null;
        }
        SportDetailTB mod = null;
        try {
            if (c.moveToFirst()) {
                mod = new SportDetailTB();
                do {
                    mod.ID = c.getInt(c.getColumnIndex(COLUMN_ID));
                    mod.userid = c.getString(c.getColumnIndex(COLUMN_USERID));
                    mod.time = c.getLong(c.getColumnIndex(COLUMN_TIME));
                    mod.distance += c.getInt(c.getColumnIndex(COLUMN_DISTANCE));
                    mod.step_value += c.getInt(c.getColumnIndex(COLUMN_STEP));
                    mod.calorie += c.getInt(c.getColumnIndex(COLUMN_CALORIE));
                    mod.date = c.getString(c.getColumnIndex(COLUMN_DATE));

                } while (c.moveToNext());
            }
        } catch (IllegalStateException e) {

        } finally {
            c.close();
        }
        return mod;
    }


    public AccessoryValues getDateTotal(String userid, String date) {
        String where = COLUMN_USERID + " ='" + userid + "' and " + COLUMN_DATE
                + " ='" + date + "'";
        Cursor c = db.query(DATABASE_TABLE, dispColumns, where, null, null,
                null, COLUMN_TIME + " ASC");
        if (c == null) {
            return null;
        }
        AccessoryValues total = null;
        try {
            if (c.moveToFirst()) {
                total = new AccessoryValues();
                do {
                    int calories = c.getInt(c.getColumnIndex(COLUMN_CALORIE));
                    total.calories += calories;
                    total.distances += c.getInt(c.getColumnIndex(COLUMN_DISTANCE));
                    total.steps += c.getInt(c.getColumnIndex(COLUMN_STEP));
                    total.time = c.getString(c.getColumnIndex(COLUMN_DATE));

                    if(calories != 0){
                        total.sport_duration += 10;
                    }

                } while (c.moveToNext());
            }
        } catch (IllegalStateException e) {

        } finally {
            c.close();
        }
        return total;
    }

    public SportDetailTB get(String userid, long time) {
        String where = COLUMN_USERID + " ='" + userid + "' and " + COLUMN_TIME
                + " = " + time;
        Cursor c = db.query(DATABASE_TABLE, dispColumns, where, null, null,
                null, COLUMN_TIME + " ASC");
        if (c == null) {
            return null;
        }
        SportDetailTB mod = null;
        try {
            if (c.moveToFirst()) {
                mod = new SportDetailTB();
                mod.ID = c.getInt(c.getColumnIndex(COLUMN_ID));
                mod.userid = c.getString(c.getColumnIndex(COLUMN_USERID));
                mod.time = c.getLong(c.getColumnIndex(COLUMN_TIME));
                mod.distance = c.getInt(c.getColumnIndex(COLUMN_DISTANCE));
                mod.step_value = c.getInt(c.getColumnIndex(COLUMN_STEP));
                mod.calorie = c.getInt(c.getColumnIndex(COLUMN_CALORIE));
                mod.date = c.getString(c.getColumnIndex(COLUMN_DATE));
            }
        } catch (IllegalStateException e) {

        } finally {
            c.close();
        }
        return mod;
    }

    public int update(SportDetailTB mod) {
        String where = COLUMN_TIME + " = " + mod.time
                + " and " + COLUMN_USERID + "='" + mod.userid +"'";

        ContentValues initialValues = new ContentValues();
        initialValues.put(COLUMN_USERID, mod.userid);
        initialValues.put(COLUMN_TIME, mod.time);
        initialValues.put(COLUMN_STEP, mod.step_value);
        initialValues.put(COLUMN_DISTANCE, mod.distance);
        initialValues.put(COLUMN_CALORIE, mod.calorie);
        initialValues.put(COLUMN_DATE, mod.date);

        int count = db.update(DATABASE_TABLE, initialValues, where, null);
        return count;
    }

    public boolean deleteByUserId(String user_id) {
        return db.delete(DATABASE_TABLE, COLUMN_USERID + "='" + user_id + "'",
                null) > 0;
    }

    public int deleteBetweenTime(String user_id, long start, long end) {
        return db.delete(DATABASE_TABLE, COLUMN_USERID + "='" + user_id + "' and "
                        + COLUMN_TIME + " >= " + start + " and " + COLUMN_TIME + " < " + end,
                null);
    }

    public boolean deleteAll() {
        return db.delete(DATABASE_TABLE, null, null) > 0;
    }


    public int deleteDateData(String user_id, String date){
        return db.delete(DATABASE_TABLE, COLUMN_USERID + "='" + user_id + "' and "
                        + COLUMN_DATE + " ='" + date + "'", null);
    }
    /**
     * delete all data below start_time
     *
     * @param userID
     * @param time
     * @return
     */
    public int deleteByUserId(String userID, long time) {
        // TODO Auto-generated method stub
        return db.delete(DATABASE_TABLE, COLUMN_USERID + "='" + userID + "' and " +
                COLUMN_TIME + " < " + time, null);
    }


    public List<String> getAllDate(String user_id){

        Cursor c = db.query(DATABASE_TABLE, dispColumns,
                COLUMN_USERID + "='" + user_id +"'" , null, null, null, COLUMN_DATE + " ASC");

        ArrayList<String> dayList = new ArrayList<String>();
        if (c == null) {
            return null;
        } else {
            try {
                if (c.moveToFirst()) {
                    do {
                        String date = c.getString(c.getColumnIndex(COLUMN_DATE));
                        if(!dayList.contains(date)){

                            dayList.add(date);
                        }
                    } while (c.moveToNext());

                }
            } catch (IllegalStateException e) {

            } finally {
                c.close();
            }

        }

        return dayList;

    }

    public void updateAnonymous(String userid) {
        String updateString = " update " + DATABASE_TABLE + " set "
                + COLUMN_USERID + " = '" + userid + "' where " + COLUMN_USERID
                + " = '" + KeyConstants.USERANONYMOUSID_STRING_KEY + "'";
        db.execSQL(updateString);
    }

}
