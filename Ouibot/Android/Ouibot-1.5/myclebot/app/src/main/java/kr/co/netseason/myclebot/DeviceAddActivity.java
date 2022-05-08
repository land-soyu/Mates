package kr.co.netseason.myclebot;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

import kr.co.netseason.myclebot.API.CreateIDData;
import kr.co.netseason.myclebot.Logger.Logger;
import kr.co.netseason.myclebot.UTIL.Installation;
import kr.co.netseason.myclebot.UTIL.OuiBotPreferences;
import kr.co.netseason.myclebot.UTIL.UIUtil;
import kr.co.netseason.myclebot.openwebrtc.Config;


public class DeviceAddActivity extends FragmentActivity {
    private boolean privacy_flag = false;
    private boolean terms_flag = false;
    private int sex = -1;

    private String serial_no;
    private String uuid;

    private Context context;
    private LinearLayout device_add_layout;
    private LinearLayout loadpanel;

    private Spinner spinner_device_year;
    private Spinner spinner_device_month;
    private Spinner spinner_device_day;
    private Spinner spinner_email;
    private ArrayAdapter adapter_email;

    private ScrollView scrollview;
    private TextView privacytext;
    private TextView termstext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_add);
        device_add_layout = (LinearLayout)findViewById(R.id.device_add_layout);
        context = this;

        spinner_device_year = (Spinner)findViewById(R.id.spinner_device_year);
        ArrayAdapter adapter_year = ArrayAdapter.createFromResource(this, R.array.year, android.R.layout.simple_spinner_item);
        spinner_device_year.setAdapter(adapter_year);
        spinner_device_month = (Spinner)findViewById(R.id.spinner_device_month);
        ArrayAdapter adapter_month = ArrayAdapter.createFromResource(this, R.array.month, android.R.layout.simple_spinner_item);
        spinner_device_month.setAdapter(adapter_month);
        spinner_device_day = (Spinner)findViewById(R.id.spinner_device_day);
        ArrayAdapter adapter_day = ArrayAdapter.createFromResource(this, R.array.day, android.R.layout.simple_spinner_item);
        spinner_device_day.setAdapter(adapter_day);
        spinner_email = (Spinner)findViewById(R.id.spinner_email);
        adapter_email = ArrayAdapter.createFromResource(this, R.array.email, android.R.layout.simple_spinner_item);
        spinner_email.setAdapter(adapter_email);
        spinner_email.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                EditText useremail2 = (EditText)findViewById(R.id.useremail2);
                if ( adapter_email.getItem(i).equals("직접입력") ) {
                    Logger.w("!!!", "sethint is string and settext is null");
                    useremail2.setHint(adapter_email.getItem(i).toString());
                    useremail2.setText("");
                } else if ( adapter_email.getItem(i).equals("Direct input") ) {
                    useremail2.setHint(adapter_email.getItem(i).toString());
                    useremail2.setText("");
                } else {
                    useremail2.setText(adapter_email.getItem(i).toString());
                    useremail2.setHint("");
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        scrollview = (ScrollView)findViewById(R.id.scrollview);
        privacytext = (TextView)findViewById(R.id.privacytext);
        privacytext.setMovementMethod(new ScrollingMovementMethod());
        privacytext.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                scrollview.requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
        termstext = (TextView)findViewById(R.id.termstext);
        termstext.setMovementMethod(new ScrollingMovementMethod());
        termstext.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                scrollview.requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
    }


    public void onprivacyClicked(final View view) {
        Logger.d("!!!", "onprivacyClicked");
        LinearLayout privacylayout = (LinearLayout)findViewById(R.id.privacylayout);
        privacylayout.setVisibility(View.VISIBLE);
    }
    public void onprivacyokClicked(final View view) {
        Logger.d("!!!", "onprivacyokClicked");
        LinearLayout privacylayout = (LinearLayout)findViewById(R.id.privacylayout);
        privacylayout.setVisibility(View.GONE);

        ImageView privacyimage = (ImageView)findViewById(R.id.privacyimage);
        privacyimage.setImageResource(R.drawable.btn_contents_checkbox_checked);
        privacy_flag = true;
    }

    public void ontermsClicked(final View view) {
        Logger.d("!!!", "ontermsClicked");
        LinearLayout termslayout = (LinearLayout)findViewById(R.id.termslayout);
        termslayout.setVisibility(View.VISIBLE);
    }
    public void ontermsokClicked(final View view) {
        Logger.d("!!!", "ontermsokClicked");
        LinearLayout termslayout = (LinearLayout)findViewById(R.id.termslayout);
        termslayout.setVisibility(View.GONE);

        ImageView termsimage = (ImageView)findViewById(R.id.termsimage);
        termsimage.setImageResource(R.drawable.btn_contents_checkbox_checked);
        terms_flag = true;
    }


    public void onSexWomanClieckd(final View view) {
        Logger.d("!!!", "onSexWomanClieckd");
        ImageView sex_man = (ImageView)findViewById(R.id.sex_man);
        sex_man.setImageResource(R.drawable.btn_contents_checkbox_normal);
        ImageView sex_woman = (ImageView)findViewById(R.id.sex_woman);
        sex_woman.setImageResource(R.drawable.btn_contents_checkbox_checked);
        sex = 1;
    }
    public void onSexManClieckd(final View view) {
        Logger.d("!!!", "onSexManClieckd");
        ImageView sex_man = (ImageView)findViewById(R.id.sex_man);
        sex_man.setImageResource(R.drawable.btn_contents_checkbox_checked);
        ImageView sex_woman = (ImageView)findViewById(R.id.sex_woman);
        sex_woman.setImageResource(R.drawable.btn_contents_checkbox_normal);
        sex = 0;
    }

    public void onCreateIDClicked(final View view) {
        Logger.d("!!!", "onCreateIDClicked");

        CreateIDData user = new CreateIDData();
        if( !privacy_flag ) {
            Toast.makeText(this, getString(R.string.user_agreement), Toast.LENGTH_SHORT).show();
            return;
        }
        if( !terms_flag ) {
            Toast.makeText(this, getString(R.string.personal_information_collection), Toast.LENGTH_SHORT).show();
            return;
        }

        EditText username = (EditText)findViewById(R.id.username);
        if ( username.getText().toString().equals("") ) {
            Toast.makeText(this, getString(R.string.enter_name), Toast.LENGTH_SHORT).show();
            return;
        }
        user.setName(username.getText().toString());
        EditText useremail1 = (EditText)findViewById(R.id.useremail1);
        EditText useremail2 = (EditText)findViewById(R.id.useremail2);
        if ( useremail1.getText().toString().trim().equals("") || useremail2.getText().toString().trim().equals("") ) {
            Toast.makeText(this, getString(R.string.email_enter), Toast.LENGTH_SHORT).show();
            return;
        }
        user.setEmail(useremail1.getText().toString().trim()+"@"+useremail2.getText().toString().trim());

        if ( sex == -1 ) {
            Toast.makeText(this, getString(R.string.gender_check), Toast.LENGTH_SHORT).show();
            return;
        }
        user.setSex(""+sex);
        if ( spinner_device_year.getSelectedItem().toString().equals("") || spinner_device_month.getSelectedItem().toString().equals("") || spinner_device_day.getSelectedItem().toString().equals("") ) {
            Toast.makeText(this, getString(R.string.input_birtd_day), Toast.LENGTH_SHORT).show();
            return;
        }
        user.setBday(spinner_device_year.getSelectedItem().toString() + "-" + spinner_device_month.getSelectedItem().toString() + "-" + spinner_device_day.getSelectedItem().toString());


        loadpanel = UIUtil.createOuibotLoadingPanel(this, (int) (UIUtil.deviceWidth(this) * 0.1));
        device_add_layout.addView(loadpanel);

        serial_no = Build.SERIAL;
        serial_no = serial_no.substring(0, 4)+serial_no.substring(serial_no.length()-4, serial_no.length());
        uuid = Installation.id(this);
        String str = "rtcid="+serial_no;
        str += "&password="+serial_no;
        str += "&uuid="+ uuid;
        str += "&type="+Config.Mode;
        str += "&email="+user.getEmail();
        str += "&name="+user.getName();
        str += "&sex="+user.getSex();
        str += "&bday="+user.getBday();
        str += "&app_version="+Config.getAppVersionName(this);
        str += "&os_version="+Config.getAndroidVersion();
        str += "&device_name="+Config.getDeviceName();
        new httpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "device_add_do_post.php", str);

    }

    //AsyncTask<param,Progress,Result>
    private class httpTask extends AsyncTask<String,Void,String> {
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
                returnValue = "error";
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

            if (result.contains("fail") || result.contains("another") || result.contains("false") || result.trim().equals("") || result.trim().equals("[]") || result.trim().contains("null")) {
                Toast.makeText(context, getString(R.string.device_add_fail), Toast.LENGTH_SHORT).show();
                return;
            }

            if ( result.contains("error") ) {
                Toast.makeText(context, "[ "+getString(R.string.device_add_fail)+" ]", Toast.LENGTH_SHORT).show();
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
                                startActivity(new Intent(context, DeviceAddResultActivity.class));
                                finish();
                            }
                        }, 100);
                    }
                } else {
                    Toast.makeText(context, getString(R.string.device_add_fail), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
