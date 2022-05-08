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
import android.text.method.ScrollingMovementMethod;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

import kr.co.netseason.myclebot.API.CreateIDData;
import kr.co.netseason.myclebot.Logger.Logger;
import kr.co.netseason.myclebot.UTIL.Installation;
import kr.co.netseason.myclebot.openwebrtc.Config;


public class CreateIDActivity extends FragmentActivity {
    private boolean id_check_flag = false;
    private boolean pwd_check_flag = false;
    private boolean privacy_flag = false;
    private boolean terms_flag = false;
    private int sex = -1;


    private EditText pwd;
    private EditText pwd_check;

    private ScrollView scrollview;
    private TextView privacytext;
    private TextView termstext;

    private Spinner spinner_year;
    private Spinner spinner_month;
    private Spinner spinner_day;
    private Spinner spinner_email;
    private ArrayAdapter adapter_email;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createid);

        spinner_year = (Spinner)findViewById(R.id.spinner_year);
        ArrayAdapter adapter_year = ArrayAdapter.createFromResource(this, R.array.year, android.R.layout.simple_spinner_item);
        spinner_year.setAdapter(adapter_year);
        spinner_month = (Spinner)findViewById(R.id.spinner_month);
        ArrayAdapter adapter_month = ArrayAdapter.createFromResource(this, R.array.month, android.R.layout.simple_spinner_item);
        spinner_month.setAdapter(adapter_month);
        spinner_day = (Spinner)findViewById(R.id.spinner_day);
        ArrayAdapter adapter_day = ArrayAdapter.createFromResource(this, R.array.day, android.R.layout.simple_spinner_item);
        spinner_day.setAdapter(adapter_day);
        spinner_email = (Spinner)findViewById(R.id.spinner_email);
        adapter_email = ArrayAdapter.createFromResource(this, R.array.email, android.R.layout.simple_spinner_item);
        spinner_email.setAdapter(adapter_email);
        spinner_email.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Logger.w("!!!", "adapter_email.getItem(i) = ["+adapter_email.getItem(i)+"]");
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

        pwd = (EditText)findViewById(R.id.pwd);
        pwd.setFilters(new InputFilter[]{filterAlphaNum});
        pwd_check = (EditText)findViewById(R.id.pwd_check);
        pwd_check.setFilters(new InputFilter[]{filterAlphaNum});
        pwd_check.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (pwd_check.getText().toString().equals(pwd.getText().toString())) {
                    TextView pwd_check_result = (TextView) findViewById(R.id.pwd_check_result);
                    pwd_check_result.setText(R.string.password_match);
                    pwd_check_result.setTextColor(Color.parseColor("#4dc1d2"));
                    pwd_check_flag = true;

                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(pwd_check.getWindowToken(), 0);
                } else {
                    TextView pwd_check_result = (TextView) findViewById(R.id.pwd_check_result);
                    pwd_check_result.setText(R.string.password_not_match);
                    pwd_check_result.setTextColor(Color.parseColor("#FF3333"));
                    pwd_check_flag = false;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
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











    public void onCheckIdClicked(final View view) {
        Logger.d("!!!", "onCheckIdClicked");
        EditText ouibotid = (EditText)findViewById(R.id.ouibotid);
        String id = ouibotid.getText().toString();
        if ( id.equals("") ) {
            Toast.makeText(this, R.string.enter_id, Toast.LENGTH_SHORT).show();
            return;
        }
        if ( id.length() != 8 ) {
            Toast.makeText(this, R.string.id_8_letter_re_enter, Toast.LENGTH_SHORT).show();
            return;
        }
        if ( Integer.parseInt(id.substring(0, 1)) < 5 ) {
            Toast.makeText(this, R.string.id_format_is_not, Toast.LENGTH_SHORT).show();
            return;
        }

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(ouibotid.getWindowToken(), 0);
        String str = "rtcid="+ id;
        new httpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "check_id_do_post.php", str);
    }
    private void onCheckIdResult(String str) {
        TextView id_check_result = (TextView)findViewById(R.id.id_check_result);
        switch (str) {
            case "Y":
                id_check_result.setText(R.string.id_available);
                id_check_result.setTextColor(Color.parseColor("#4dc1d2"));
                id_check_flag = true;
                break;
            case "N":
                id_check_result.setText(R.string.booked_id);
                id_check_result.setTextColor(Color.parseColor("#FF3333"));
                id_check_flag = false;
                break;
        }
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
        EditText ouibotid = (EditText)findViewById(R.id.ouibotid);
        if ( ouibotid.getText().toString().equals("") ) {
            Toast.makeText(this, getString(R.string.input_id2), Toast.LENGTH_SHORT).show();
            return;
        }
        if ( !id_check_flag ) {
            Toast.makeText(this, getString(R.string.id_overlap_check), Toast.LENGTH_SHORT).show();
            return;
        }
        user.setId(ouibotid.getText().toString());
        if ( pwd.getText().toString().equals("") ) {
            Toast.makeText(this, getString(R.string.enter_password), Toast.LENGTH_SHORT).show();
            return;
        }
        if ( !pwd_check_flag ) {
            Toast.makeText(this, getString(R.string.enter_password_check), Toast.LENGTH_SHORT).show();
            return;
        }
        user.setPwd(pwd.getText().toString());
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
        if ( spinner_year.getSelectedItem().toString().equals("") || spinner_month.getSelectedItem().toString().equals("") || spinner_day.getSelectedItem().toString().equals("") ) {
            Toast.makeText(this, getString(R.string.input_birtd_day), Toast.LENGTH_SHORT).show();
            return;
        }
        user.setBday(spinner_year.getSelectedItem().toString() + "-" + spinner_month.getSelectedItem().toString() + "-" + spinner_day.getSelectedItem().toString());
        String str = "rtcid="+ user.getId();
        str += "&type=2";
        str += "&uuid="+ Installation.id(this);
        str += "&password="+user.getPwd();
        str += "&email="+user.getEmail();
        str += "&name="+user.getName();
        str += "&sex="+user.getSex();
        str += "&bday="+user.getBday();
        new httpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "create_id_do_post.php", str);

    }
    private void onCreateIdResult(String str) {
        switch (str) {
            case "fail":
                Toast.makeText(this, getString(R.string.not_make_id)+"\n"+getString(R.string.retry), Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(this, getString(R.string.make_id), Toast.LENGTH_SHORT).show();
                finish();
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

            String[] strs = result.split("\\|");
            switch (strs[0]) {
                case "checkid":
                    onCheckIdResult(strs[1]);
                    break;
                case "createid":
                    onCreateIdResult(strs[1]);
                    break;
            }
        }
    }

}
