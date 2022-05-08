package com.matescorp.soyu.farmkingapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.matescorp.soyu.farmkingapp.fcm.MyFirebaseInstanceIDService;
import com.matescorp.soyu.farmkingapp.util.DataPreference;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private Context context;

    private UserLoginTask mAuthTask = null;

    private EditText mIdView;
    private EditText mPasswordView;
    private CheckBox autologin;

    private String regid = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        context = getApplicationContext();

        DataPreference.PREF = PreferenceManager.getDefaultSharedPreferences(this);

        if ( DataPreference.getAutoLogin() && DataPreference.getId() != null ) {
            startActivity(new Intent(context, MainActivity.class));
            finish();
        }

        mIdView = (EditText) findViewById(R.id.mIdView);
        mPasswordView = (EditText) findViewById(R.id.mPasswordView);
        autologin = (CheckBox) findViewById(R.id.autologin);
        autologin.setChecked(DataPreference.getAutoLogin());

        Button mLoginButton = (Button) findViewById(R.id.mLoginButton);
        mLoginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

    }

    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        String id = mIdView.getText().toString();
        String pwd = mPasswordView.getText().toString();

        if ( !id.equals("") || !pwd.equals("") ) {
            mAuthTask = new UserLoginTask(id, pwd);
            mAuthTask.execute(Urls.farmking + "/api/login_post.php");
        }
    }
    /*
        로그인시 email, password, PushToken, serial, phoneNumber를 같이 넣어준다.

    */
    public class UserLoginTask extends AsyncTask<String, Void, String> {

        private String mId;
        private String mPassword;

        public UserLoginTask(String id, String password) {
            mId = id;
            mPassword = password;
        }

        @Override
        protected String doInBackground(String... args) {

            String returnValue = "";
            HttpURLConnection conn = null;
            try {
                String urlString = args[0];
                Log.e("!!!", "urlString = " + urlString);
                URL url = new URL(urlString);
                // open connection
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);            // 입력스트림 사용여부
                conn.setDoOutput(false);            // 출력스트림 사용여부
                conn.setUseCaches(false);        // 캐시사용 여부
                conn.setReadTimeout(20000);        // 타임아웃 설정 ms단위
                conn.setRequestMethod("POST");

                StringBuffer params = new StringBuffer("");
//                params.append("name=" + URLEncoder.encode(name)); //한글일 경우 URL인코딩
                params.append("id=" + mId + "&pwd=" + mPassword);
                PrintWriter output = new PrintWriter(conn.getOutputStream());
                output.print(params.toString());
                output.close();

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
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
            return returnValue;
        }

        @Override
        protected void onPostExecute(String result) {
            mAuthTask = null;
            result = result.trim();
            Log.e(TAG, "result =" + result);

            try {
                JSONArray jsona = new JSONArray(result);
                JSONObject jsono = jsona.getJSONObject(0);

                DataPreference.setFarmName(jsono.getString("farmname"));
                DataPreference.setSerial(jsono.getString("serial"));
                DataPreference.setId(jsono.getString("id"));
                DataPreference.setPwd(jsono.getString("pwd"));
                DataPreference.setName(jsono.getString("name"));
                DataPreference.setPhone(jsono.getString("phone"));
                DataPreference.setAddr(jsono.getString("addr"));

                loginResult(true);
            } catch (Exception e) {
                e.printStackTrace();
                loginResult(false);
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }

    private void loginResult(boolean b) {
        if ( b ) {
            DataPreference.setAutoLogin(autologin.isChecked());
            startActivity(new Intent(context, MainActivity.class));
            finish();
        }
    }
}

