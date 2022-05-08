package kr.co.netseason.myclebot;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.regex.Pattern;

import kr.co.netseason.myclebot.API.CreateIDData;
import kr.co.netseason.myclebot.Logger.Logger;
import kr.co.netseason.myclebot.openwebrtc.Config;


public class SearchPWActivity extends FragmentActivity {
    private int sex = -1;

    private Spinner spinner_pwd_year;
    private Spinner spinner_pwd_month;
    private Spinner spinner_pwd_day;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchpwd);

        spinner_pwd_year = (Spinner)findViewById(R.id.spinner_pwd_year);
        ArrayAdapter adapter_year = ArrayAdapter.createFromResource(this, R.array.year, android.R.layout.simple_spinner_item);
        spinner_pwd_year.setAdapter(adapter_year);
        spinner_pwd_month = (Spinner)findViewById(R.id.spinner_pwd_month);
        ArrayAdapter adapter_month = ArrayAdapter.createFromResource(this, R.array.month, android.R.layout.simple_spinner_item);
        spinner_pwd_month.setAdapter(adapter_month);
        spinner_pwd_day = (Spinner)findViewById(R.id.spinner_pwd_day);
        ArrayAdapter adapter_day = ArrayAdapter.createFromResource(this, R.array.day, android.R.layout.simple_spinner_item);
        spinner_pwd_day.setAdapter(adapter_day);
    }
    protected InputFilter filterAlphaNum = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence charSequence, int i, int i1, Spanned spanned, int i2, int i3) {
            Pattern ps = Pattern.compile("^[a-zA-Z0-9]+$");
            if( !ps.matcher(charSequence).matches() ) {
                return "";
            }
            return null;
        }
    };




    public void onSexWomanClieckd(final View view) {
        Logger.d("!!!", "onSexWomanClieckd");
        ImageView pwd_sex_man = (ImageView) findViewById(R.id.pwd_sex_man);
        pwd_sex_man.setImageResource(R.drawable.btn_contents_checkbox_normal);
        ImageView pwd_sex_woman = (ImageView) findViewById(R.id.pwd_sex_woman);
        pwd_sex_woman.setImageResource(R.drawable.btn_contents_checkbox_checked);
        sex = 1;
    }
    public void onSexManClieckd(final View view) {
        Logger.d("!!!", "onSexManClieckd");
        ImageView pwd_sex_man = (ImageView) findViewById(R.id.pwd_sex_man);
        pwd_sex_man.setImageResource(R.drawable.btn_contents_checkbox_checked);
        ImageView pwd_sex_woman = (ImageView) findViewById(R.id.pwd_sex_woman);
        pwd_sex_woman.setImageResource(R.drawable.btn_contents_checkbox_normal);
        sex = 0;
    }
    public void onModifyPWDClicked(final View view) {
        if ( !pwd_check_flag ) {
            Toast.makeText(this, getString(R.string.password_check), Toast.LENGTH_SHORT).show();
        }
        CreateIDData user = new CreateIDData();
        TextView search_pwd_result_ouibotid = (TextView)findViewById(R.id.search_pwd_result_ouibotid);
        user.setId(search_pwd_result_ouibotid.getText().toString());

        EditText modify_pwd = (EditText)findViewById(R.id.modify_pwd);
        if ( modify_pwd.getText().toString().equals("") ) {
            Toast.makeText(this,  getString(R.string.password_enter), Toast.LENGTH_SHORT).show();
            return;
        }
        user.setPwd(modify_pwd.getText().toString());

        String str = "rtcid="+user.getId();
        str += "&password="+user.getPwd();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(modify_pwd.getWindowToken(), 0);

        new httpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "update_pwd_do_post.php", str);
    }
    public void onSearchPWDFailClicked(final View view) {
        setContentView(R.layout.activity_searchpwd);
    }

    public void onSearchPWDClicked(final View view) {
        Logger.d("!!!", "onSearchPWDClicked");

        CreateIDData user = new CreateIDData();
        EditText pwd_ouibotid = (EditText)findViewById(R.id.pwd_ouibotid);
        if ( pwd_ouibotid.getText().toString().equals("") ) {
            Toast.makeText(this, getString(R.string.enter_id), Toast.LENGTH_SHORT).show();
            return;
        }
        user.setId(pwd_ouibotid.getText().toString());
        EditText pwd_username = (EditText)findViewById(R.id.pwd_username);
        if ( pwd_username.getText().toString().equals("") ) {
            Toast.makeText(this, getString(R.string.enter_name), Toast.LENGTH_SHORT).show();
            return;
        }
        user.setName(pwd_username.getText().toString());
        if ( sex == -1 ) {
            Toast.makeText(this, getString(R.string.gender_check), Toast.LENGTH_SHORT).show();
            return;
        }
        user.setSex(""+sex);

        if ( spinner_pwd_year.getSelectedItem().toString().equals("") || spinner_pwd_month.getSelectedItem().toString().equals("") || spinner_pwd_day.getSelectedItem().toString().equals("") ) {
            Toast.makeText(this, getString(R.string.input_birtd_day), Toast.LENGTH_SHORT).show();
            return;
        }
        user.setBday(spinner_pwd_year.getSelectedItem().toString()+"-"+spinner_pwd_month.getSelectedItem().toString()+"-"+spinner_pwd_day.getSelectedItem().toString());

        String str = "name="+ URLEncoder.encode(user.getName());
        str += "&rtcid="+user.getId();
        str += "&sex="+user.getSex();
        str += "&bday="+user.getBday();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(pwd_username.getWindowToken(), 0);

        new httpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "search_pwd_do_post.php", str);
    }











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
            String[] strs = result.split("\\|");
            Logger.e("!!!", "httpTask result = | " + result + " |");
            switch (strs[0]) {
                case "search":
                    switch (strs[1]) {
                        case "fail":
                            startSearchResultfailView(strs[1]);
                            break;
                        default:
                            startSearchResultokView(strs[1]);
                            break;
                    }
                    break;
                case "update":
                    startUpdatePWDResultView(strs[1]);
                    break;
            }
        }
    }

    private void startUpdatePWDResultView(String str) {
        switch (str) {
            case Config.PARAM_SUCCESS:
                Toast.makeText(this, getString(R.string.passwd_modified), Toast.LENGTH_SHORT).show();
                finish();
                break;
            case "fail":
                Toast.makeText(this, getString(R.string.password_reset_fail), Toast.LENGTH_SHORT).show();
                break;
            case "empty":
                Toast.makeText(this, getString(R.string.id_not_find), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private EditText modify_pwd_check;
    private EditText modify_pwd;
    private boolean pwd_check_flag;
    private void startSearchResultokView(String str) {
        setContentView(R.layout.activity_searchpwdresultok);
        TextView search_pwd_result_ouibotid = (TextView)findViewById(R.id.search_pwd_result_ouibotid);
        search_pwd_result_ouibotid.setText(str);

        modify_pwd = (EditText)findViewById(R.id.modify_pwd);
        modify_pwd.setFilters(new InputFilter[]{filterAlphaNum});
        modify_pwd_check = (EditText)findViewById(R.id.modify_pwd_check);
        modify_pwd_check.setFilters(new InputFilter[]{filterAlphaNum});
        modify_pwd_check.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (modify_pwd_check.getText().toString().equals(modify_pwd.getText().toString())) {
                    TextView search_pwd_check_result = (TextView) findViewById(R.id.search_pwd_check_result);
                    search_pwd_check_result.setText(R.string.password_match);
                    search_pwd_check_result.setTextColor(Color.parseColor("#4dc1d2"));
                    pwd_check_flag = true;
                } else {
                    TextView search_pwd_check_result = (TextView) findViewById(R.id.search_pwd_check_result);
                    search_pwd_check_result.setText(R.string.password_not_match);
                    search_pwd_check_result.setTextColor(Color.parseColor("#FF3333"));
                    pwd_check_flag = false;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }
    private void startSearchResultfailView(String str) {
        setContentView(R.layout.activity_searchpwdresultfail);
    }
}
