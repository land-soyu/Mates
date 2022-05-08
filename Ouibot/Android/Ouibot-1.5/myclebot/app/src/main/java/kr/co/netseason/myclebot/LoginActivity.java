package kr.co.netseason.myclebot;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

import kr.co.netseason.myclebot.Logger.Logger;
import kr.co.netseason.myclebot.UTIL.Installation;
import kr.co.netseason.myclebot.UTIL.OuiBotPreferences;
import kr.co.netseason.myclebot.openwebrtc.Config;


public class LoginActivity extends FragmentActivity {
    private Intent nextActivity;
    private EditText edittext;
    private EditText edittextpwd;

    private Context context;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        setContentView(R.layout.activity_login);
        initView();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
    }

    public void initView() {
        context = this;

        edittext = (EditText) findViewById(R.id.edittextid);
        edittextpwd = (EditText) findViewById(R.id.edittextpwd);
        edittextpwd.setFilters(new InputFilter[]{filterAlphaNum});
    }

    public void alwaysLogin(View view) {

        Logger.d("test","Config.getPhoneNumber = "+Config.getPhoneNumber(LoginActivity.this));
        Logger.d("test","Config.getAppVersionName = "+Config.getAppVersionName(LoginActivity.this));
        if (edittext.getText().toString().equals("")) {
            Toast.makeText(this, getString(R.string.input_ouibot_id), Toast.LENGTH_SHORT).show();
        } else if (edittextpwd.getText().toString().equals("")) {
            Toast.makeText(this, getString(R.string.enter_password), Toast.LENGTH_SHORT).show();
        } else {
            String str = "rtcid=" + edittext.getText().toString();
            str += "&password=" + edittextpwd.getText().toString();
            str += "&uuid=" + Installation.id(this);
            str += "&type=" + Config.Mode;
            str += "&os_version=" + Config.getAndroidVersion();
            str += "&app_version=" + Config.getAppVersionName(LoginActivity.this);
            str += "&device_name=" + Config.getDeviceName();
            str += "&phone_no=" + Config.getPhoneNumber(LoginActivity.this);
            new httpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "login_do_post.php", str);
        }
    }

    public void startMain() {
        OuiBotPreferences.setLoginId(this, edittext.getText().toString());
        OuiBotPreferences.setUUID(this, Installation.id(this));
        nextActivity = new Intent(this, MainActivity.class);
        startActivity(nextActivity);
        finish();
        //  service start
    }

    public void searchID(View view) {
        nextActivity = new Intent(this, SearchIDActivity.class);
        startActivity(nextActivity);
    }

    public void searchPWD(View view) {
        nextActivity = new Intent(this, SearchPWActivity.class);
        startActivity(nextActivity);
    }

    public void createID(View view) {
        nextActivity = new Intent(this, CreateIDActivity.class);
        startActivity(nextActivity);
    }

    protected InputFilter filterAlphaNum = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence charSequence, int i, int i1, Spanned spanned, int i2, int i3) {
            Pattern ps = Pattern.compile("^[a-zA-Z0-9]+$");
            if (!ps.matcher(charSequence).matches()) {
                return "";
            }
            return null;
        }
    };


    //AsyncTask<param,Progress,Result>
    private class httpTask extends android.os.AsyncTask<String, Void, String> {
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
                StringBuffer sb = new StringBuffer();
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                for (; ; ) {
                    String line = br.readLine();
                    if (line == null) break;
                    sb.append(line + "\n");
                }

                br.close();
                conn.disconnect();
                br = null;
                conn = null;

                returnValue = sb.toString();
            } catch (UnknownHostException e) {
                e.printStackTrace();
                return "UnknownHostException";
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

            if (result.contains("UnknownHostException")) {
                Toast.makeText(context, getString(R.string.login_connect_fail), Toast.LENGTH_SHORT).show();
                return;
            }
            switch (result) {
                case Config.PARAM_SUCCESS:
                    startMain();
                    break;
                case "another":
                    Toast.makeText(context, getString(R.string.login_info_fail), Toast.LENGTH_SHORT).show();
                    break;
                case "fail":
                    Toast.makeText(context, getString(R.string.login_fail), Toast.LENGTH_SHORT).show();
                    break;
            }

        }
    }
}
