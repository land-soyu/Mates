package kr.co.netseason.myclebot.Provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by tbzm on 15. 10. 19.
 */
public class SecureSQLiteHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "securedata.db";
    private static final int DATABASE_VERSION = 18;

    public static final String TABLE_SECURE_SLAVE_LIST = "table_secure_slave_list";
    public static final String TABLE_SECURE_MASTER_LIST = "table_secure_master_list";
    public static final String TABLE_MESSAGE_LIST = "message_list";
    public static final String TABLE_USER_INFO_LIST = "table_user_info_list";
    public static final String TABLE_SPEM_LIST = "spem_list";
    public static final String TABLE_PROFILE_LIST = "profile_list";
    public static final String COL_INEDEX = "_id";
    public static final String COL_ID = "ouibotid";
    public static final String COL_FILE_PATH = "imagepath";
    public static final String COL_MODE = "mode";
    public static final String COL_TIME = "time";
    public static final String COL_READABLE = "readable";
    public static final String COL_SEND_STATE = "send_state";
    public static final String COL_TYPE = "type";

    public static final String COL_RTCID = "rtcid";
    public static final String COL_PEER_RTCID = "peer_rtcid";
    public static final String COL_PEER_RTCID_NAME = "peer_rtcid_name";
    public static final String COL_MESSAGE_DATA = "messageData";
    public static final String COL_SEND_FLAG = "sendflag";
    public static final String COL_SLAVE_ID = "slave_id";
    public static final String COL_MASTER_ID = "master_id";
    public static final String COL_SECURE_ON_OFF_VALUE = "secure_on_off_value";
    public static final String COL_PROFILE = "profile";

    public static final String[] TABLE_SECURE_SLAVE_ALL_COLUMNS = {COL_INEDEX, COL_ID, COL_FILE_PATH, COL_MODE, COL_TIME};
    public static final String[] TABLE_SECURE_MASTER_ALL_COLUMNS = {COL_INEDEX, COL_ID, COL_FILE_PATH, COL_MODE, COL_TIME};
    public static final String[] TABLE_MESSAGE_ALL_COLUMNS = {COL_INEDEX, COL_RTCID, COL_PEER_RTCID, COL_PEER_RTCID_NAME, COL_MESSAGE_DATA, COL_SEND_FLAG, COL_TIME, COL_READABLE, COL_TYPE, COL_SEND_STATE};
    public static final String[] TABLE_USER_INFO_COLUMNS = {COL_INEDEX, COL_SLAVE_ID, COL_MASTER_ID, COL_SECURE_ON_OFF_VALUE, COL_TIME};
    public static final String[] TABLE_SPEM_ALL_COLUMNS = {COL_INEDEX, COL_PEER_RTCID};
    public static final String[] TABLE_PROFILE_ALL_COLUMNS = {COL_INEDEX, COL_PEER_RTCID, COL_PROFILE};

    private static final String DATABASE_CREATE_SECURE_SLAVE_LIST = "create table "
            + TABLE_SECURE_SLAVE_LIST + "(" + COL_INEDEX
            + " integer primary key autoincrement, "
            + COL_ID + " text not null , "
            + COL_FILE_PATH + " text not null , "
            + COL_MODE + " text not null , "
            + COL_TIME + " text not null );";
    private static final String DATABASE_CREATE_SECURE_MASTER_LIST = "create table "
            + TABLE_SECURE_MASTER_LIST + "(" + COL_INEDEX
            + " integer primary key autoincrement, "
            + COL_ID + " text not null , "
            + COL_FILE_PATH + " text not null , "
            + COL_MODE + " text not null , "
            + COL_TIME + " text not null );";

    private static final String DATABASE_CREATE_MESSAGE_LIST = "create table "
            + TABLE_MESSAGE_LIST + "(" + COL_INEDEX
            + " integer primary key autoincrement, "
            + COL_RTCID + " text not null , "
            + COL_PEER_RTCID + " text not null , "
            + COL_PEER_RTCID_NAME + " text not null, "
            + COL_MESSAGE_DATA + " text  not null , "
            + COL_SEND_FLAG + " text not null , "
            + COL_READABLE + " text not null , "
            + COL_TYPE + " text not null , "
            + COL_SEND_STATE + " text not null , "
            + COL_TIME + " text not null );";

    private static final String DATABASE_CREATE_USER_INFO_LIST = "create table "
            + TABLE_USER_INFO_LIST + "(" + COL_INEDEX
            + " integer primary key autoincrement, "
            + COL_SLAVE_ID + " text not null , "
            + COL_MASTER_ID + " text not null , "
            + COL_SECURE_ON_OFF_VALUE + " text not null , "
            + COL_TIME + " text not null );";

    private static final String DATABASE_CREATE_SPEM_LIST = "create table "
            + TABLE_SPEM_LIST + "(" + COL_INEDEX
            + " integer primary key autoincrement, "
            + COL_PEER_RTCID + " text not null ); ";

    private static final String DATABASE_CREATE_PROFILE_LIST = "create table "
            + TABLE_PROFILE_LIST + "(" + COL_INEDEX
            + " integer primary key autoincrement, "
            + COL_PEER_RTCID + " text not null , "
            + COL_PROFILE + " text not null ); ";

    public SecureSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE_SECURE_SLAVE_LIST);
        db.execSQL(DATABASE_CREATE_SECURE_MASTER_LIST);
        db.execSQL(DATABASE_CREATE_MESSAGE_LIST);
        db.execSQL(DATABASE_CREATE_USER_INFO_LIST);
        db.execSQL(DATABASE_CREATE_SPEM_LIST);
        db.execSQL(DATABASE_CREATE_PROFILE_LIST);
    }

    @Override
    protected void finalize() throws Throwable {
        this.close();
        super.finalize();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SECURE_SLAVE_LIST);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SECURE_MASTER_LIST);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGE_LIST);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_INFO_LIST);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SPEM_LIST);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROFILE_LIST);
        onCreate(db);
    }
}
