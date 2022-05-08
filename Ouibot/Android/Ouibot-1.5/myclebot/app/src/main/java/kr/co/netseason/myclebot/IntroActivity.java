package kr.co.netseason.myclebot;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

import kr.co.netseason.myclebot.Logger.Logger;
import kr.co.netseason.myclebot.UTIL.Installation;
import kr.co.netseason.myclebot.UTIL.OuiBotPreferences;
import kr.co.netseason.myclebot.openwebrtc.Config;

public class IntroActivity extends FragmentActivity {
    private Context context;

    private Intent nextActivity;

    private String serial_no;
    private String uuid;


    @Override
    public void onBackPressed() {
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.activity_intro);
        context = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        Logger.logFileCreate();
        Logger.d("intro activity", "onCreate call ");
        context = this;
        switch (Config.Mode) {
            case 1: //  Ouibot
                if ( OuiBotPreferences.getLoginId(this) == null ) {
//                    serial_no = Build.SERIAL;
//                    serial_no = serial_no.substring(0, 4)+serial_no.substring(serial_no.length()-4, serial_no.length());
//                    uuid = Installation.id(this);
//                    String str = "rtcid="+serial_no;
//                    str += "&password="+serial_no;
//                    str += "&uuid="+ uuid;
//                    str += "&type="+Config.Mode;
//
//                    new httpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "login_ouibot_do_post.php", str);

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(new Intent(context, DeviceAddInitActivity.class));
                            finish();
                        }
                    }, 2000);

                } else {
                    nextActivity = new Intent(this, MainActivity.class);
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            boolean messageFlag = getIntent().getBooleanExtra(Config.INTENT_MOVE_TO_MESSAGE_VIEW, false);
                            Logger.d("intro activity", "onStart call " + messageFlag);
                            if ( messageFlag ) {
                                nextActivity.putExtra(Config.INTENT_MOVE_TO_MESSAGE_VIEW, true);
                            } else {
                                Logger.e("!!!", "missedcall = " + getIntent().getIntExtra("missedcall", -1));
                                int num = getIntent().getIntExtra("missedcall", -1);
                                nextActivity.putExtra("missedcall", num);
                                Logger.e("!!!", "Intent miss num chack!"+num);

                                Logger.e("!!!", "misscalled = " + getIntent().getIntExtra("misscalled", -1));
                                int numed = getIntent().getIntExtra("misscalled", -1);
                                nextActivity.putExtra("misscalled", numed);

                            }

                            startActivity(nextActivity);
                            finish();
                        }
                    }, 2000);
                }
                break;
            case 2: //  Android
                if ( OuiBotPreferences.getLoginId(this) == null ) {
                    nextActivity = new Intent(this, LoginActivity.class);
                } else {
                    nextActivity = new Intent(this, MainActivity.class);
                }

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(nextActivity);
                        finish();
                    }
                }, 2000);
                break;
        }

    }

    //AsyncTask<param,Progress,Result>
    private class httpTask extends android.os.AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... args) {

            String returnValue = "";
            HttpURLConnection conn = null;
            try {
                Logger.e("!!!", "args[0] = " + args[0]);
                Logger.e("!!!", "args[1] = " + args[1]);
                String urlString = Config.Server_IP + args[0];
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
            } catch (Exception e) {
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

            if ( result.contains("NOT OK") ||  result.contains("true") ||  result.contains("false") ||  result.trim().equals("") ||  result.trim().contains("null") ) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(context, LoginActivity.class));
                        finish();
                    }
                }, 2000);
                return;
            }
            if ( result.contains("success") ) {
                OuiBotPreferences.setUUID(context, Installation.id(context));
                new httpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "ouibot_auto_login_do_post.php", "serial=" + serial_no);
                return;
            }

            try {
                JSONArray json = new JSONArray(result);

                if ( json.length() > 0 ) {
                    for (int i=0;i<json.length();i++) {
                        OuiBotPreferences.setLoginId(context, json.getJSONObject(i).getString("rtcid"));

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(context, MainActivity.class);
                                if ( getIntent().getIntExtra("missedcall", -1) > -1 ) {
                                    intent.putExtra("missedcall", getIntent().getIntExtra("missedcall", -1) );
                                }
                                startActivity(new Intent(context, MainActivity.class));
                                finish();
                            }
                        }, 2000);
                    }
                } else {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(new Intent(context, LoginActivity.class));
                            finish();
                        }
                    }, 2000);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

}
