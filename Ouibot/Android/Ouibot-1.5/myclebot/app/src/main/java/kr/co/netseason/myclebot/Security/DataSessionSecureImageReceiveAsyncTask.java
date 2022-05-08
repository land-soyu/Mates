package kr.co.netseason.myclebot.Security;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Messenger;

import org.json.JSONException;
import org.json.JSONObject;

import kr.co.netseason.myclebot.Logger.Logger;
import kr.co.netseason.myclebot.openwebrtc.Config;

/**
 * Created by tbzm on 15. 11. 30.
 */
public class DataSessionSecureImageReceiveAsyncTask extends AsyncTask<Void, Void, Boolean> {
    private final String TAG = getClass().getName();
    private Messenger mService;
    private JSONObject json;
    private int count = 0;
    private int mThreadTime;
    private Context mContext;

    public DataSessionSecureImageReceiveAsyncTask(Context context, Messenger service, JSONObject json, int threadTime) {
        mService = service;
        this.json = json;
        mThreadTime = threadTime;
        mContext = context;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        String key = null;
        try {
            key = json.getString(Config.PARAM_SESSION_ID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RecieveDataChannel recieveDataChannel = new RecieveDataChannel(mContext, mService, json, key);
        Config.THREAD_RECIEVER_CLASS.put(key, recieveDataChannel);

        while (recieveDataChannel.getRtcSession() != null) {
            if (count == mThreadTime) {
                recieveDataChannel.stopSession(key);
                return true;
            }
            try {
                Logger.d(TAG, " DataSessionReceiveAsyncTask loading....." + count++);
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        Logger.d(TAG, " DataSessionReceiveAsyncTask end !!!");
        super.onPostExecute(aBoolean);
    }
}
