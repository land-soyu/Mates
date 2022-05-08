package kr.co.netseason.myclebot.Security;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import kr.co.netseason.myclebot.API.MessageListData;
import kr.co.netseason.myclebot.Logger.Logger;
import kr.co.netseason.myclebot.Provider.SecureProvider;
import kr.co.netseason.myclebot.Provider.SecureSQLiteHelper;
import kr.co.netseason.myclebot.R;
import kr.co.netseason.myclebot.UTIL.OuiBotPreferences;
import kr.co.netseason.myclebot.openwebrtc.Config;

/**
 * Created by tbzm on 15. 12. 9.
 */
public class MessageSenderAsyncTaskWithWebSocket extends AsyncTask<Void, Void, String> {
    private final String TAG = getClass().getName();
    private Context mContext;
    private Messenger mService;
    private String mMasterId;
    private String mMessage;
    private long mTime = 0;

    //for message
    public MessageSenderAsyncTaskWithWebSocket(Context context, long time, String message, String masterId, Messenger service) {
        mContext = context;
        mTime = time;
        mMessage = message;
        mMasterId = masterId;
        mService = service;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(Void... params) {
        Logger.e("!!!", "SecureImageSenderAsyncTaskWithWebSocket doInBackground start call");
        String returnValue = "";
        try {
            String urlString = Config.Server_IP + "get_device_token_post.php";
            Logger.e("!!!", "urlString = " + urlString);
            URL url = new URL(urlString);

            // open connection
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);            // 입력스트림 사용여부
            conn.setDoOutput(false);            // 출력스트림 사용여부
            conn.setUseCaches(false);        // 캐시사용 여부
            conn.setReadTimeout(20000);        // 타임아웃 설정 ms단위
            conn.setRequestMethod("POST");

            // POST 값 전달 하기
            StringBuffer param = new StringBuffer("");
//                params.append("name=" + URLEncoder.encode(name)); //한글일 경우 URL인코딩
            param.append("rtcid="+mMasterId);
            PrintWriter output = new PrintWriter(conn.getOutputStream());
            output.print(param.toString());
            output.close();

            // Response받기
            StringBuffer sb = new StringBuffer();
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            for (; ; ) {
                String line = br.readLine();
                if (line == null) break;
                sb.append(line + "\n");
            }

            br.close();
            conn.disconnect();

            returnValue = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnValue;
    }

    public String makeJsonData() {
        try {
            JSONObject jsono = new JSONObject();
            jsono.put(Config.PARAM_TYPE, Config.PARAM_GET_CONFIG_ACK);
            jsono.put(Config.PARAM_SESSION_ID, Long.toString(System.currentTimeMillis()));
            jsono.put(Config.PARAM_FROM, mMasterId);
            jsono.put(Config.PARAM_TO, OuiBotPreferences.getLoginId(mContext));
            jsono.put(Config.PARAM_DESCRIPTION, "message");
            JSONObject json = new JSONObject();
            json.put("id", OuiBotPreferences.getLoginId(mContext));
            json.put("time", mTime);
            json.put("message", mMessage);
            jsono.put(Config.PARAM_CONFIG, json);
            return jsono.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    protected void onPostExecute(String result) {
        result = result.trim();
        String token =  result;
        if(token != null && result.length() > 0){
            String message = "";
            String title = OuiBotPreferences.getLoginId(mContext);
            String time = String.valueOf(mTime);
            message = mContext.getResources().getString(R.string.push_message_recieved);
            new ForIOSNotificationHttpAsyncTask(token, title , message, null, false, time, Config.MESSAGE_TYPE).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            recordMessageSendSucces2Database(mTime);
            if(mContext != null) {
                mContext.sendBroadcast(new Intent(Config.INTENT_RECEIVE_MESSAGE_EVENT));
            }

        }else{
            Message msg = Message.obtain(null, Config.SEND_MESSAGE_TO_WEBSOCKET, makeJsonData());
            try {
                mService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            recordMessageSendFail2Database(mTime);

        }
    }

    public void recordMessageSendFail2Database(long time) {
        if (mTime != 0) {
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
        }
    }

    public void recordMessageSendSucces2Database(long time) {
        if (time != 0) {
            ContentResolver resolver = mContext.getContentResolver();
            ContentValues values = new ContentValues();
            values.put(SecureSQLiteHelper.COL_SEND_STATE, MessageListData.SEND_COMPLETE);
            resolver.update(SecureProvider.MESSAGE_TABLE_URI, values, SecureSQLiteHelper.COL_RTCID + " =? AND " + SecureSQLiteHelper.COL_TIME + " =? ", new String[]{OuiBotPreferences.getLoginId(mContext), String.valueOf(time)});
        }
    }
}