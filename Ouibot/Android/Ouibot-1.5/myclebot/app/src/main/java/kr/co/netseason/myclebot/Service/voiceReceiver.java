package kr.co.netseason.myclebot.Service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import kr.co.netseason.myclebot.API.ContactListData;
import kr.co.netseason.myclebot.CctvActivity;
import kr.co.netseason.myclebot.Logger.Logger;
import kr.co.netseason.myclebot.MainActivity;
import kr.co.netseason.myclebot.PetActivity;
import kr.co.netseason.myclebot.R;
import kr.co.netseason.myclebot.Security.SecureActivity;
import kr.co.netseason.myclebot.Security.SecurePreference;
import kr.co.netseason.myclebot.Security.UibotListData;
import kr.co.netseason.myclebot.UTIL.OuiBotPreferences;
import kr.co.netseason.myclebot.View.ContactModifyDialog;
import kr.co.netseason.myclebot.openwebrtc.CallActivity;
import kr.co.netseason.myclebot.openwebrtc.Config;
import kr.co.netseason.myclebot.openwebrtc.SignalingChannel;

/**
 * Created by Administrator on 2015-07-17.
 */
public class voiceReceiver extends BroadcastReceiver {
    private Context context;
    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        String action = intent.getAction();
        Log.w("!!!", "onReceive broadcast = "+action);

        if (action.equals("signalvision.com.ouibot.voicerecognition"))
        {
            int num = intent.getIntExtra("number", -1);
            String str = intent.getStringExtra("result");

            switch (num) {
                case 0:
                    break;
                case 1: //  침입감지모드
                    if (!Config.isMountedSDcard()) {
                        Toast.makeText(MainActivity.CONTEXT, MainActivity.CONTEXT.getResources().getString(R.string.please_input_your_sdcard), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String previousDetectOnOff1 = SecurePreference.getDetectOnOff();
                    if ( previousDetectOnOff1.equalsIgnoreCase("on") ) {
                        Intent broadCastIntent = new Intent(Config.INTENT_ACTION_SECURE_ACTIVITY_FINISH);
                        context.sendBroadcast(broadCastIntent);
                    }
                    SecurePreference.setDetectMode(0);

                    Intent i1 = new Intent(context, SecureActivity.class);
                    i1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(i1);
                    break;
                case 2: //  무활동감지모드
                    if (!Config.isMountedSDcard()) {
                        Toast.makeText(MainActivity.CONTEXT, MainActivity.CONTEXT.getResources().getString(R.string.please_input_your_sdcard), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String previousDetectOnOff2 = SecurePreference.getDetectOnOff();
                    if ( previousDetectOnOff2.equalsIgnoreCase("on") ) {
                        Intent broadCastIntent = new Intent(Config.INTENT_ACTION_SECURE_ACTIVITY_FINISH);
                        context.sendBroadcast(broadCastIntent);
                    }
                    SecurePreference.setDetectMode(1);

                    Intent i2 = new Intent(context, SecureActivity.class);
                    i2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(i2);
                    break;
                case 3: //  음악실행
//                    Cursor cur = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, columns, MediaStore.Audio.Media.TITLE + "=" + "\"" + title + "\"", null, null);
//                    if (true == cur.moveToFirst()) {
//                        s_id = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media._ID));
//                    }
//                    cur.close();
                    String s_id = "";
                    Cursor cur = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            null, null, null, null);
                    if (true == cur.moveToFirst()) {
                        s_id = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media._ID));
                    }
                    cur.close();

                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.setDataAndType(Uri.parse("content://media/external/audio/media/"+s_id), "audio/mp3");
                    context.startActivity(i);

                    break;
                case 4: //  위봇통화
                    String strt = "sindex=0&eindex=1&rtcid=" + OuiBotPreferences.getLoginId(context);
                    new httpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "get_phonebook_data_post.php", strt);
                    break;

                default:
                    break;
            }
        }
    }










    //AsyncTask<param,Progress,Result>
    private class httpTask extends android.os.AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... args) {

            String returnValue = "";
            try {
                Logger.e("!!!", "args[0] = " + args[0]);
                Logger.e("!!!", "args[1] = " + args[1]);
                String urlString = Config.Server_IP + args[0];
                Logger.e("!!!", "urlString = " + urlString);
                URL url = new URL(urlString);

                // open connection
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
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
            Logger.e("!!!", "httpTask result = | " + result + " |");

            if (result.trim().equals("") || result.trim().equals("[]") || result.trim().contains("null")) {
                Toast.makeText(context, "등록되어 있는 연락처가 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                JSONArray json = new JSONArray(result);

                if (json.length() > 0) {
                    Intent i = new Intent(context, CallActivity.class);
                    i.putExtra("callSendNumber", json.getJSONObject(0).getString("peer_name")+"|"+json.getJSONObject(0).getString("peer_rtcid"));
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(i);
                } else {
                    Toast.makeText(context, "등록되어 있는 연락처가 없습니다.", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

}