package com.matescorp.system.zaigle.adapter;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;



import java.sql.Date;
import java.util.ArrayList;

/**
 * Created by sjkim on 17. 9. 20.
 */

public class MainSQLiteHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "maindata.db";
    private static final int DATABASE_VERSION = 1107;


    public static final String TABLE_NAME_MAIN_LIST = "table_main_list";
    public static final String TABLE_NAME_USER_LIST = "table_user_list";
    public static final String TABLE_NAME_WORKING_LIST = "table_working_list";
    public static final String TABLE_NAME_CHECK_LIST = "table_check_list";
    public static final String TABLE_NAME_CHECK_LIST2 = "table_check_list2";

    public static final String COL_INEDEX = "_id";
    public static final String SENSORNAME = "sensorname";
    public static final String SENSORVALUE = "sensorvalue";

    public static final String U_NAME = "name";
    public static final String U_YEAR = "year";
    public static final String U_SEX = "sex";
    public static final String U_HEIGH = "tall";
    public static final String U_WEIGHT = "weight";
    public static final String ETC = "etc";

    public static final String DATE = "Tdate";
    public static final String W_WORK_COUNT = "work_count";
    public static final String W_WORK_KCAL = "work_kcal";

    public static final String C_HR = "hr";
    public static final String C_SPO2 = "spo2";
    public static final String C_BMI = "bmi";
    public static final String C_STRESS = "stress";
    public static final String C_FATP = "fatp";
    public static final String C_FATM = "fatm";
    public static final String C_MUSCLEM = "musclem";
    public static final String C_BASICM = "basicm";


    private static final String DATABASE_CREATE_MAIN_LIST = "create table "
            + TABLE_NAME_MAIN_LIST + "(" + COL_INEDEX
            + " integer primary key autoincrement, "
            + SENSORNAME + " text , "
            + SENSORVALUE + " text);";

    private static final String DATABASE_CREATE_USER_LIST = "create table "
            + TABLE_NAME_USER_LIST + "(" + COL_INEDEX
            + " integer primary key autoincrement, "
            + U_NAME + " text , "
            + U_YEAR + "  INTEGER , "
            + U_SEX + " INTEGER , "
            + U_HEIGH + " REAL , "
            + U_WEIGHT + " REAL , "
            + ETC + " text);";

    private static final String DATABASE_CREATE_WORKING_LIST = "create table "
            + TABLE_NAME_WORKING_LIST + "(" + COL_INEDEX
            + " integer primary key autoincrement, "
            + DATE + " text , "
            + W_WORK_COUNT+ "  INTEGER, "
            + W_WORK_KCAL + " INTEGER);";


//    private static final String DATABASE_CREATE_CHECK_LIST = "create table "
//            + TABLE_NAME_CHECK_LIST+ "(" + COL_INEDEX
//            + " integer primary key autoincrement, "
//            + DATE + " text , "
//            + C_HR + "  REAL, "
//            + C_SPO2 + " REAL , "
//            + C_BMI + " REAL , "
//            + C_STRESS + " text , "
//            + C_FATP + " REAL , "
//            + C_FATM + " REAL , "
//            + C_MUSCLEM + " REAL , "
//            + C_BASICM + " REAL , "
//            + ETC + " text);";

    private static final String DATABASE_CREATE_CHECK_LIST = "create table "
            + TABLE_NAME_CHECK_LIST+ "(" + COL_INEDEX
            + " integer primary key autoincrement, "
            + DATE + " date , "
            + C_HR + "  text, "
            + C_SPO2 + " text , "
            + C_BMI + " text , "
            + C_BASICM + " text , "
            + C_STRESS + " text , "
            + C_MUSCLEM + " text , "
            + C_FATM  + " text , "
            + C_FATP  + " text , "
            + ETC + " text);";

    private static final String DATABASE_CREATE_CHECK_LIST2 = "create table "
            + TABLE_NAME_CHECK_LIST+ "(" + COL_INEDEX
            + " integer primary key autoincrement, "
            + DATE + " date , "
            + C_HR + "  text, "
            + C_BMI + " text , "
            + C_SPO2 + " text , "
            + C_BASICM + " text , "
            + C_STRESS + " text , "
            + C_MUSCLEM + " text , "
            + C_FATP  + " text , "
            + C_FATM  + " text , "
            + ETC + " text);";


    public MainSQLiteHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.e("aa","tjdwnd");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
//        db.execSQL(DATABASE_CREATE_MAIN_LIST);
//        db.execSQL(DATABASE_CREATE_USER_LIST);
//        db.execSQL(DATABASE_CREATE_WORKING_LIST);
        Log.e("sql", "onCreate(SQLiteDatabase db)  ") ;
        db.execSQL(DATABASE_CREATE_CHECK_LIST);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_MAIN_LIST);
//        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_USER_LIST);
//        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_WORKING_LIST);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_CHECK_LIST);
        onCreate(db);
    }

    public void dropCheck(SQLiteDatabase db){
        db.execSQL("delete from " + TABLE_NAME_CHECK_LIST);
        onCreate(db);
    }


    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }


    public void insert(SQLiteDatabase db, String name, String value){

        String sql = "insert into " + TABLE_NAME_MAIN_LIST + " values(NULL, '" + name + "', '" + value +  "');";

        db.execSQL(sql);


    }
    public void userInsert(SQLiteDatabase db, String name, int year, int sex, int tall, int weight, String etc){
        Log.e("sqlite ", " ---------------userInsert------------ ") ;
        String sql = "insert into " + TABLE_NAME_USER_LIST + " values(NULL, '" + name + "', '" + year +  "', '" + sex +  "', '" + tall +  "', '" + weight +  "', '" + etc + "');";
        db.execSQL(sql);

    }

    public void workingInsert(SQLiteDatabase db, String date, int count, int kcal){
        String sql = "insert into " + TABLE_NAME_WORKING_LIST + " values(NULL, '" + date + "', '" + count +  "', '" + kcal +"');";
        db.execSQL(sql);


    }

//    public void checkInsert(SQLiteDatabase db, String date, int hr , int spo2, int bmi, String stress, int fatp, int fatm, int musclem, int basicm ,String etc){
//        String sql = "insert into " + TABLE_NAME_CHECK_LIST + " values(NULL, '" + date + "', '" +hr +  "', '" +spo2 +  "', '" +bmi +  "', '" +stress +  "', '" +fatp +  "', '" +fatm +  "', '" +musclem +  "', '" +basicm +  "', '" +etc +  "');";
//        db.execSQL(sql);
//    }

    public void checkInsert(SQLiteDatabase db, String Tdate, String hr , String spo2, String bmi, String basicm , String stress, String musclem, String fatm, String fatp, String etc){
        String sql = "insert into " + TABLE_NAME_CHECK_LIST + " values(NULL, '" + Tdate + "', '" +hr +  "', '" + spo2 +  "', '" +bmi +  "',  '" +basicm +  "', '" + stress +  "', '" +musclem +  "', '" +fatm +  "', '" +fatp+  "', '" +etc +  "');";
        db.execSQL(sql);
    }

    public void checkInsert2(SQLiteDatabase db, String Tdate, String hr , String spo2, String bmi, String basicm , String stress, String musclem, String fatm , String fatp, String etc){
        String sql = "insert into " + TABLE_NAME_CHECK_LIST + " values(NULL, '" + Tdate + "', '" + hr +  "', '" + spo2 +  "', '" +bmi +  "',  '" +basicm +  "', '" + stress +  "', '" +musclem +  "', '" +fatm +  "', '" + fatp +  "', '" +etc +  "');";
        db.execSQL(sql);
    }


    public void select(SQLiteDatabase db) {
//       Cursor c = db.rawQuery("select * from table_main_list;", null);
//        while(c.moveToNext()) {
//            String id = c.getString(0);
//            String sensorname = c.getString(1);
//            String sensorvalue = c.getString(2);
//            Log.d("sqlite","sensorname:"+sensorname+",sensorvalue:"+sensorvalue);
//        }
//
//        Cursor u = db.rawQuery("select * from table_user_list;", null);
//        while(u.moveToNext()) {
//            String id = u.getString(0);
//            String name = u.getString(1);
//            int year = u.getInt(2);
//            int sex = u.getInt(3);
//            int tall = u.getInt(4);
//            int weight = u.getInt(5);
//            String etc = u.getString(6);
//            Log.d("sqlite","name:"+name+",year:"+year+",sex:"+sex+",tall:"+tall+",weight:"+weight+",etc:"+etc);
//        }
//        Cursor w = db.rawQuery("select * from table_working_list;", null);
//        while(w.moveToNext()) {
//            String id = w.getString(0);
//            String date = w.getString(1);
//            int count = w.getInt(2);
//            int kacl = w.getInt(3);
//            Log.d("sqlite", "working data ==== "+ date + "count == "+  count+  "kacl == " + kacl);
//        }

        Cursor ch = db.rawQuery("select * from table_check_list;", null);
        while(ch.moveToNext()) {
            String id = ch.getString(0);
            String date = ch.getString(1);
            String hr = ch.getString(2);
            String spo2 = ch.getString(3);
            String bmi  = ch.getString(4);
            String basicm = ch.getString(5);
            String etc = ch.getString(6);
            Log.d("sqlite", "working data ==== "+ date + "hr == "+  hr+  "spo2 == " + spo2 + bmi + basicm + etc);

        }

    }







}