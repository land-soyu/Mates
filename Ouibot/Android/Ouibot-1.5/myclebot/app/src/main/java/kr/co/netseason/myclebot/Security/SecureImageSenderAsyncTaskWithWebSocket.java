package kr.co.netseason.myclebot.Security;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import kr.co.netseason.myclebot.Logger.Logger;
import kr.co.netseason.myclebot.Provider.SecureSQLiteHelper;
import kr.co.netseason.myclebot.R;
import kr.co.netseason.myclebot.UTIL.OuiBotPreferences;
import kr.co.netseason.myclebot.openwebrtc.Config;

/**
 * Created by tbzm on 15. 12. 8.
 */
public class SecureImageSenderAsyncTaskWithWebSocket extends AsyncTask<Void, Void, String> {
    private final String TAG = getClass().getName();
    private Context mContext;
    private File mFile;
    private Messenger mService;
    private JSONObject mJson;
    private String mMasterId;

    //for secure Image
    public SecureImageSenderAsyncTaskWithWebSocket(Context context, JSONObject json, File file, String masterId, Messenger service) {
        mContext = context;
        mJson = json;
        mFile = file;
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

    @Override
    protected void onPostExecute(String result) {
        result = result.trim();
        String token =  result;
        if(token != null && result.length() > 0){
            String mode = "";
            String message = "";
            String title = OuiBotPreferences.getLoginId(mContext);
            String time = "";
            try{
                mode = mJson.getString(SecureSQLiteHelper.COL_MODE);
                time = mJson.getString(SecureSQLiteHelper.COL_TIME);
            }catch (JSONException e){
                e.printStackTrace();
            }
            if (Integer.parseInt(mode) == Config.DETECT_SECURE_MODE) {
                message = mContext.getResources().getString(R.string.notification_detected_success_string);
            }else {
                message = mContext.getResources().getString(R.string.notification_none_activity_success_string);
            }
            new ForIOSNotificationHttpAsyncTask(token, title , message, null,false, time, Config.SECURE_TYPE).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }else{
            Message msg = Message.obtain(null, Config.SEND_MESSAGE_TO_WEBSOCKET, makeJsonData());
            try {
                mService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public String makeJsonData() {
        try {
            JSONObject jsono = new JSONObject();
            jsono.put(Config.PARAM_TYPE, "event");
            jsono.put(Config.PARAM_SESSION_ID, Long.toString(System.currentTimeMillis()));
            jsono.put(Config.PARAM_FROM, OuiBotPreferences.getLoginId(mContext));
            jsono.put(Config.PARAM_TO, mMasterId);
            jsono.put(SecureSQLiteHelper.COL_FILE_PATH, mJson.get(SecureSQLiteHelper.COL_FILE_PATH));
            jsono.put(SecureSQLiteHelper.COL_MODE, mJson.get(SecureSQLiteHelper.COL_MODE));
            jsono.put(SecureSQLiteHelper.COL_TIME, mJson.get(SecureSQLiteHelper.COL_TIME));
            JSONObject json = new JSONObject();
            json.put("imgcut", Config.getByteStringForSecureImage(mFile));
            jsono.put("event", json);
            return jsono.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



}
