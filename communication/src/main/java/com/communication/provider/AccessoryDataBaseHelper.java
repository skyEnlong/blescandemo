package com.communication.provider;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class AccessoryDataBaseHelper extends SQLiteOpenHelper {

    private final static String DATABASE_NAME = "Codoon_Accessory.db";
    public final static int DATABASE_VERSION = 12;
    static SQLiteDatabase db;
    static private AtomicInteger mOpenCounter = new AtomicInteger();
    private static SQLiteOpenHelper instance;

    public AccessoryDataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        if (instance == null) {
            instance = this;
        }
    }

    protected synchronized SQLiteDatabase getDatabase() {

        if (db != null && db.isOpen()) {
            return db;
        } else {
            db = getWritableDatabase();
        }
        return db;
    }

    protected synchronized void closeDatabase() {

        if (db != null && db.isOpen() && !db.isDbLockedByCurrentThread()
                && !db.isDbLockedByOtherThreads()) {
            db.close();
            db = null;
        }

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SleepDetailDB.createTableSql);
        db.execSQL(HeartRateDB.CREAT_DAY_HEART_TABLE);
        db.execSQL(SportDetailDB.createTableSql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (!isTableExist(SleepDetailDB.DATABASE_TABLE)) {

            db.execSQL(SleepDetailDB.createTableSql);
        }
        if (!isTableExist(HeartRateDB.DAY_HEART_TABLE_NAME)) {

            db.execSQL(HeartRateDB.CREAT_DAY_HEART_TABLE);
        }
        if (!isTableExist(SportDetailDB.DATABASE_TABLE)) {
            db.execSQL(SportDetailDB.createTableSql);
        }

        if (!isColumnExist(db, SleepDetailDB.DATABASE_TABLE, SleepDetailDB.COLUMN_SLEEP)) {
            db.execSQL(" ALTER TABLE " + SleepDetailDB.DATABASE_TABLE
                    + " ADD Column " + SleepDetailDB.COLUMN_SLEEP
                    + " integer NOT NULL default 0 ");
        }
    }


    public boolean isColumnExist(SQLiteDatabase db, String tableName,
                                 String columnName) {
        boolean result = false;
        if (tableName == null) {
            return false;
        }

        try {
            Cursor cursor = null;
            String sql = "select count(1) as c from sqlite_master where type ='table' and name ='"
                    + tableName.trim()
                    + "' and sql like '%"
                    + columnName.trim() + "%'";
            cursor = db.rawQuery(sql, null);
            if (cursor.moveToNext()) {
                int count = cursor.getInt(0);
                if (count > 0) {
                    result = true;
                }
            }

            cursor.close();
        } catch (Exception e) {
        }
        return result;
    }

    public boolean isTableExist(String tableName) {
        boolean result = false;
        if (tableName == null) {
            return false;
        }

        try {
            Cursor cursor = null;
            String sql = "select count(1) as c from sqlite_master where type ='table' and name ='"
                    + tableName.trim() + "'";
            cursor = getDatabase().rawQuery(sql, null);
            if (cursor.moveToNext()) {
                int count = cursor.getInt(0);
                if (count > 0) {
                    result = true;
                }
            }

            cursor.close();
        } catch (Exception e) {
        }
        return result;
    }

    public  void open() {
        synchronized (mOpenCounter) {
            if (mOpenCounter.incrementAndGet() == 1 || (mOpenCounter.incrementAndGet() > 1 && db == null)) {
                mOpenCounter.set(1);
                db = getDatabase();
            }
        }
    }


    public   void close() {
        synchronized (mOpenCounter) {
            if (mOpenCounter.decrementAndGet() == 0) {// 自减
                instance.close();
            }
        }
    }


    public void beginTransaction() {
        db.beginTransaction();
    }

    public void setTransactionSuccessful() {
        db.setTransactionSuccessful();
    }

    public void endTransaction() {
        db.endTransaction();
    }

}
