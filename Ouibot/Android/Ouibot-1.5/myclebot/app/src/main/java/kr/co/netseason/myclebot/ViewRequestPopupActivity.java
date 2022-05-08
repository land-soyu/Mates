package kr.co.netseason.myclebot;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

import kr.co.netseason.myclebot.Logger.Logger;
import kr.co.netseason.myclebot.Security.PushWakeLock;
import kr.co.netseason.myclebot.UTIL.OuiBotPreferences;
import kr.co.netseason.myclebot.openwebrtc.Config;
import kr.co.netseason.myclebot.openwebrtc.SignalingChannel;

/**
 * Created by tbzm on 15. 9. 29.
 */
public class ViewRequestPopupActivity extends Activity implements View.OnClickListener {
    private Button mShortLeft, mShortRight;
    private String TAG = getClass().getName();
    private TextView mContentString;
    private Messenger mService;
    private Messenger messenger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_popup_layout);
        PushWakeLock.acquire(this, 3000);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mContentString = (TextView) findViewById(R.id.content_text);
        mContentString.setText(getSenderID(getIntentData()) + " " + getString(R.string.camera_permission));
        mShortLeft = (Button) findViewById(R.id.short_left);
        mShortLeft.setOnClickListener(this);
        this.setFinishOnTouchOutside(false);
        mShortRight = (Button) findViewById(R.id.short_right);
        mShortRight.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent service = new Intent(this, SignalingChannel.class);
        bindService(service, conn, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = new Messenger(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(conn);
    }
    public JSONObject getIntentData() {
        Intent intent = getIntent();
        JSONObject json = new JSONObject();
        try {
            json.put(Config.MASTER_REQUEST_INTENT_MY_ID, intent.getStringExtra(Config.PARAM_TO));
            json.put(Config.MASTER_REQUEST_INTENT_MASTER_ID, intent.getStringExtra(Config.PARAM_FROM));
        }catch (JSONException e){
            e.printStackTrace();
        }
        Logger.d(TAG, "getIntentData json = " + json);
        return json;
    }

    public String getSenderID(JSONObject json){
        String id = "";
        try {
           id= json.getString(Config.MASTER_REQUEST_INTENT_MY_ID);
        }catch (JSONException e){
            e.printStackTrace();
        }
        return id;
    }


    public void sendCertification(JSONObject data) {
        new httpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "setting_cctv_cert_do_post.php", "rtcid=" + getSenderID(data) + "&cert=1&myrtcid=" + OuiBotPreferences.getLoginId(this));
        try {
            Message msg = Message.obtain(null, Config.CERTIFICATION_ANSWER, getSenderID(data));
            mService.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void sendDelCertification(JSONObject data) {
        new httpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "phone_perm_del_do_post.php", "rtcid=" + getSenderID(data) + "&peer_rtcid=" + OuiBotPreferences.getLoginId(this));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.short_right:
                sendCertification(getIntentData());
                finish();
                break;
            case R.id.short_left:
                sendDelCertification(getIntentData());
                finish();
                break;
            default:
                break;
        }
    }




    private class httpTask extends android.os.AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... args) {

            String returnValue = "";
            HttpURLConnection conn = null;
            try {
                Logger.e("!!!", "args[0] = " + args[0]);
                Logger.e("!!!", "args[1] = " + args[1]);
                String urlString = Config.Server_IP+args[0];
                Logger.e("!!!", "urlString = " + urlString);
                URL url = new URL(urlString);

                // open connection
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);            // 입력스트림 사용여부
                conn.setDoOutput(false);            // 출력스트림 사용여부
                conn.setUseCaches(false);        // 캐시사용 여부
                conn.setReadTimeout(20000);        // 타임아웃 설정 ms단위
//                conn.setRequestMethod("GET");  // or GET
                conn.setRequestMethod("POST");

                // POST 값 전달 하기
                StringBuffer params = new StringBuffer("");
//                params.append("name=" + URLEncoder.encode(name)); //한글일 경우 URL인코딩
                params.append(args[1]);
                PrintWriter output = new PrintWriter(conn.getOutputStream());
                output.print(params.toString());
                output.close();

                // Response받기
                StringBuffer sb =  new StringBuffer();
                BufferedReader br = new BufferedReader( new InputStreamReader(conn.getInputStream()));

                for(;;){
                    String line =  br.readLine();
                    if(line == null) break;
                    sb.append(line+"\n");
                }

                br.close();
                conn.disconnect();
                br = null;
                conn = null;

                returnValue = sb.toString();
            } catch ( Exception e ) {
                e.printStackTrace();
            } finally {
                if (conn!=null) {
                    conn.disconnect();
                }
            }
            return returnValue;
        }
        @Override
        protected void onPostExecute(String result) {
            result = result.trim();
            Logger.e("!!!", "httpTask result = | " + result + " |");
            if ( result.contains("NOT OK") ||  result.contains("true") ||  result.contains("false") ||  result.trim().equals("") || result.trim().equals("[]") ||  result.trim().contains("null") ) {
                return;
            }

        }
    }}
