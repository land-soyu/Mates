package kr.co.netseason.myclebot;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;

import kr.co.netseason.myclebot.API.CreateIDData;
import kr.co.netseason.myclebot.Logger.Logger;
import kr.co.netseason.myclebot.openwebrtc.Config;


public class SearchIDActivity extends FragmentActivity {
    private int sex = -1;

    private Spinner spinner_id_year;
    private Spinner spinner_id_month;
    private Spinner spinner_id_day;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchid);

        spinner_id_year = (Spinner)findViewById(R.id.spinner_id_year);
        ArrayAdapter adapter_year = ArrayAdapter.createFromResource(this, R.array.year, android.R.layout.simple_spinner_item);
        spinner_id_year.setAdapter(adapter_year);
        spinner_id_month = (Spinner)findViewById(R.id.spinner_id_month);
        ArrayAdapter adapter_month = ArrayAdapter.createFromResource(this, R.array.month, android.R.layout.simple_spinner_item);
        spinner_id_month.setAdapter(adapter_month);
        spinner_id_day = (Spinner)findViewById(R.id.spinner_id_day);
        ArrayAdapter adapter_day = ArrayAdapter.createFromResource(this, R.array.day, android.R.layout.simple_spinner_item);
        spinner_id_day.setAdapter(adapter_day);
    }


    public void onSexWomanClieckd(final View view) {
        Logger.d("!!!", "onSexWomanClieckd");
        ImageView id_sex_man = (ImageView) findViewById(R.id.id_sex_man);
        id_sex_man.setImageResource(R.drawable.btn_contents_checkbox_normal);
        ImageView id_sex_woman = (ImageView) findViewById(R.id.id_sex_woman);
        id_sex_woman.setImageResource(R.drawable.btn_contents_checkbox_checked);
        sex = 1;
    }

    public void onSexManClieckd(final View view) {
        Logger.d("!!!", "onSexManClieckd");
        ImageView id_sex_man = (ImageView) findViewById(R.id.id_sex_man);
        id_sex_man.setImageResource(R.drawable.btn_contents_checkbox_checked);
        ImageView id_sex_woman = (ImageView) findViewById(R.id.id_sex_woman);
        id_sex_woman.setImageResource(R.drawable.btn_contents_checkbox_normal);
        sex = 0;
    }
    public void onsearchidendClieckd(final View view) {
        finish();
    }


    public void onSearchIDClicked(final View view) {
        Logger.d("!!!", "onSearchIDClicked");

        CreateIDData user = new CreateIDData();
        EditText id_username = (EditText)findViewById(R.id.id_username);
        if ( id_username.getText().toString().equals("") ) {
            Toast.makeText(this, getString(R.string.enter_name), Toast.LENGTH_SHORT).show();
            return;
        }
        user.setName(id_username.getText().toString());
        if ( sex == -1 ) {
            Toast.makeText(this, getString(R.string.gender_check), Toast.LENGTH_SHORT).show();
            return;
        }
        user.setSex(""+sex);

        if ( spinner_id_year.getSelectedItem().toString().equals("") || spinner_id_month.getSelectedItem().toString().equals("") || spinner_id_day.getSelectedItem().toString().equals("") ) {
            Toast.makeText(this, getString(R.string.input_birtd_day), Toast.LENGTH_SHORT).show();
            return;
        }
        user.setBday(spinner_id_year.getSelectedItem().toString()+"-"+spinner_id_month.getSelectedItem().toString()+"-"+spinner_id_day.getSelectedItem().toString());

        String str = "name="+ URLEncoder.encode(user.getName());
        str += "&sex="+user.getSex();
        str += "&bday="+user.getBday();
        str += "&type="+Config.Mode;


        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(id_username.getWindowToken(), 0);

        new httpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "search_ids_do_post.php", str);
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
            Logger.e("!!!", "httpTask result = | " + result + " |");
            startSearchResultView(result);
        }
    }

    private void startSearchResultView(String str) {
        setContentView(R.layout.activity_searchidresult);
        str = str.trim();
        Logger.e("!!!", "httpTask result = | " + str + " |");

        TextView search_id_result = (TextView)findViewById(R.id.search_id_result);
        if ( str.trim().equals("") || str.trim().equals("[]") || str.trim().contains("null")) {
            TextView search_id_result_text = (TextView)findViewById(R.id.search_id_result_text);
            search_id_result_text.setText("");
            search_id_result.setText(R.string.ouibot_id_nonexistent);
            return;
        }

        try {
            JSONArray json = new JSONArray(str);
            if (json.length() > 0) {
                search_id_result.setText("");
                for (int i = 0; i < json.length(); i++) {
                    search_id_result.append(json.getJSONObject(i).getString("rtcid")+"\n");
                }
            } else {
                Logger.e("!!!", "Data impty");
                //	Data impty
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}