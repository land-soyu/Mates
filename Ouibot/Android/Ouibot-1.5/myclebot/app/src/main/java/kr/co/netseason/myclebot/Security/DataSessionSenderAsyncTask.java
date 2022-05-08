package kr.co.netseason.myclebot.Security;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Messenger;

import org.json.JSONObject;

import java.io.File;

import kr.co.netseason.myclebot.API.MessageListData;
import kr.co.netseason.myclebot.Logger.Logger;
import kr.co.netseason.myclebot.Provider.SecureProvider;
import kr.co.netseason.myclebot.Provider.SecureSQLiteHelper;
import kr.co.netseason.myclebot.UTIL.OuiBotPreferences;
import kr.co.netseason.myclebot.openwebrtc.Config;

/**
 * Created by tbzm on 15. 12. 8.
 */
public class DataSessionSenderAsyncTask extends AsyncTask<Void, Void, Boolean> {
    private final String TAG = getClass().getName();
    private Context mContext;
    private File mFile;
    private Messenger mService;
    private JSONObject mJson;
    private String mMasterId;
    private long mTime = 0;
    private String mMessage;
    private SenderDataChannel channelLogin;
    private int count = 0;
    private int mThreadTime;
    private short mChannel;

    //for message
//    public DataSessionSenderAsyncTask(Context context, long time, String message, String masterId, Messenger service, int threadTime, short channel) {
//        mContext = context;
//        mTime = time;
//        mMessage = message;
//        mMasterId = masterId;
//        mService = service;
//        count = 0;
//        mThreadTime = threadTime;
//        mChannel = channel;
//    }

    //for file message and json
    public DataSessionSenderAsyncTask(Context context, long time, JSONObject json, File file, String masterId, Messenger service, int threadTime, short channel) {
        mContext = context;
        mTime = time;
        mJson = json;
        mFile = file;
        mMasterId = masterId;
        mService = service;
        count = 0;
        mThreadTime = threadTime;
        mChannel = channel;
    }

    //for secure Image
//    public DataSessionSenderAsyncTask(Context context, JSONObject json, File file, String masterId, Messenger service, int threadTime, short channel) {
//        mContext = context;
//        mJson = json;
//        mFile = file;
//        mMasterId = masterId;
//        mService = service;
//        count = 0;
//        mThreadTime = threadTime;
//        mChannel = channel;
//    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    public String getUniqueKey() {
        return Long.toString(System.currentTimeMillis());
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        String time = getUniqueKey();
        channelLogin = new SenderDataChannel(mContext, mMasterId, mService, time, mChannel);
        Config.THREAD_SENDER_CLASS.put(time, channelLogin);
        if (mJson != null && mFile != null) {
            channelLogin.offerSessionStart(mJson, mFile);
        }
        while(!isCancelled() && count != mThreadTime) {
            if (count == mThreadTime) {
                channelLogin.stopSession(time);
                return true;
            }
            try {
                Logger.d(TAG, " DataSessionSenderAsyncTask loading....." + count++);
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    @Override
    protected void onCancelled() {
        Logger.d(TAG,"onCancelled DataSessionSenderAsyncTask end !!!");
        recordMessageSendFail2Database(mTime);
        super.onCancelled();
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        Logger.d(TAG, "onPostExecute DataSessionSenderAsyncTask end !!!");
        recordMessageSendFail2Database(mTime);
        super.onPostExecute(aBoolean);
    }

    public void recordMessageSendFail2Database(long time) {
        ContentResolver resolver = mContext.getContentResolver();
        Cursor c = resolver.query(SecureProvider.MESSAGE_TABLE_URI, new String[]{SecureSQLiteHelper.COL_SEND_STATE},
                SecureSQLiteHelper.COL_RTCID + " =? AND " + SecureSQLiteHelper.COL_TIME + " =? ",
                new String[]{OuiBotPreferences.getLoginId(mContext), String.valueOf(time)}, null);
        boolean isSuccess = true;
        if (c != null && c.moveToFirst()) {
            try {
                do {
                    int data = c.getInt(c.getColumnIndex(SecureSQLiteHelper.COL_SEND_STATE));
                    if (data != MessageListData.SEND_COMPLETE) {
                        isSuccess = false;
                    }
                } while (c.moveToNext());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                c.close();
            }
        }

        if (!isSuccess) {
            ContentValues values = new ContentValues();
            values.put(SecureSQLiteHelper.COL_SEND_STATE, MessageListData.SEND_FAIL);
            resolver.update(SecureProvider.MESSAGE_TABLE_URI, values, SecureSQLiteHelper.COL_RTCID + " =? AND " + SecureSQLiteHelper.COL_TIME + " =? ",
                    new String[]{OuiBotPreferences.getLoginId(mContext), String.valueOf(time)});
        }
        try {
            Thread.sleep(500);
        }catch (Exception e){
            e.printStackTrace();
        }
        mContext.sendBroadcast(new Intent(Config.INTENT_RECEIVE_MESSAGE_EVENT));
    }
}
