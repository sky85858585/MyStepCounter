package com.elliott85.mystepcounter.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.elliott85.mystepcounter.content.StepCountData;

import java.util.ArrayList;

/**
 * Created by 박현우 on 2016-11-06.
 */
public class StepDBManager {
    private static final String TAG = "StepDBManager";

    private static final String dbName = "totalstep.db";
    public static final int dbVersion = 1;

    private static final String STEP_COUNTER_TABLE = "STEPCOUNTER";

    // Key of databases for raw data
    public static final String KEY_ROWID = "_id";
    public static final String KEY_DATE = "date";
    public static final String KEY_STEP_COUNT = "count";
    public static final String KEY_DISTANCE = "distance";

    private DBOpenHelper mDBOpenHelper;
    private SQLiteDatabase db;

    public StepDBManager(Context context) {
        mDBOpenHelper = new DBOpenHelper(context, dbName, null, dbVersion);
        db = mDBOpenHelper.getWritableDatabase();

        if (db == null) Log.e(TAG, "getWritableDatabase is null");
        else Log.e(TAG, "getWritableDatabase success");
    }

    private class DBOpenHelper extends SQLiteOpenHelper {
        public DBOpenHelper (Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, null, version);
            Log.d(TAG, "DBOpenHelper constructor");
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(TAG, "DBOpenHelper onCreate");
            createTable(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }

    private void createTable(SQLiteDatabase db) {
        try {
            Log.i(TAG, "createTable is called");
            db.execSQL("CREATE TABLE " + STEP_COUNTER_TABLE + " ("
                            + KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                            + KEY_DATE + " TEXT,"
                            + KEY_STEP_COUNT + " INTERGER,"
                            + KEY_DISTANCE +" REAL) ");

            Log.i(TAG, "success to create database");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "failed to create table");
        }
    }

    public long insertData(ContentValues values) {
        return db.insert(STEP_COUNTER_TABLE, null, values);
    }

    public long updateData(ContentValues values, String date, long count) {
        return db.update(STEP_COUNTER_TABLE, values, KEY_DATE + "=?", new String[]{date});
    }

    public int getCount() {
        String sql = "SELECT * FROM " + STEP_COUNTER_TABLE + ";";
        Cursor results = db.rawQuery(sql, null);

        if (results == null) {
            results.close();

            return 0;
        } else {
            int count = results.getCount();
            results.close();

            return count;
        }
    }

    public int isExistData(String date) {
        String sql = "SELECT * FROM " + STEP_COUNTER_TABLE
                + " WHERE " + KEY_DATE + " = '" + date + "';";
        Cursor results = db.rawQuery(sql, null);

        if (results == null || results.getCount() == 0) {
            Log.i(TAG, "isExistData, NULL");
            results.close();

            return -1;
        } else {
            results.moveToLast();

            int count = results.getCount();
            int stepCount = results.getInt(results.getColumnIndex(KEY_STEP_COUNT));

            Log.i(TAG, "isExistData, count = " + count + " / stepCount = " + stepCount);

            if (count == 0) {
                results.close();
                return -1;
            } else {
                results.close();
                return stepCount;
            }
        }
    }

    public ArrayList<StepCountData> selectAll() {
        Log.i(TAG, "selectAll is called");

        ArrayList<StepCountData> temp = new ArrayList<>();

        String sql = "SELECT * FROM " + STEP_COUNTER_TABLE + ";";
        Cursor results = db.rawQuery(sql, null);

        if (results != null && results.getCount() > 0) {
            Log.i(TAG, "result size = " + results.getCount());
            results.moveToFirst();

            do {
                String date = results.getString(results.getColumnIndex(KEY_DATE));
                int count = results.getInt(results.getColumnIndex(KEY_STEP_COUNT));
                double distance = results.getDouble(results.getColumnIndex(KEY_DISTANCE));

                Log.i(TAG, "Add to temp list, date = " + date + " / count = " + count + " / distance = " + distance);
                temp.add(new StepCountData(date, count, distance));
                results.moveToNext();
            } while (!results.isAfterLast());
        }
        return temp;
    }
}
