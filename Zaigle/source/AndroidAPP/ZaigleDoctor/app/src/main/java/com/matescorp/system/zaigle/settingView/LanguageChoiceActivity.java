package com.matescorp.system.zaigle.settingView;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.matescorp.system.zaigle.MainActivity;
import com.matescorp.system.zaigle.R;
import com.matescorp.system.zaigle.UartService;
import com.matescorp.system.zaigle.data.ProfilePreferences;

import java.lang.reflect.Method;
import java.util.Locale;

import static com.matescorp.system.zaigle.data.ProfilePreferences.setLanguagesj;

/**
 * Created by sjkim on 17. 7. 27.
 */

public class LanguageChoiceActivity extends AppCompatActivity {

    private RadioButton option1;
    private RadioButton option2;
    private RadioButton option3;
    private RadioButton option4;
    private Handler mHandler = null;
    private final String KEY_SCROLL_X = "scroll_x";
    private HorizontalScrollView svLanguageChooser;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_language);
        ProfilePreferences.PREF = PreferenceManager.getDefaultSharedPreferences(this);

        TextView title = (TextView)findViewById(R.id.text_app_title);
        title.setText(R.string.language);
        ImageView back = (ImageView)findViewById(R.id.icon_menu_view);
        back.setImageDrawable(getResources().getDrawable(R.drawable.back_icon));
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        mHandler = new Handler();
        option1 = (RadioButton) findViewById(R.id.option1);
        option2 = (RadioButton) findViewById(R.id.option2);
        option3 = (RadioButton) findViewById(R.id.option3);
        option4 = (RadioButton) findViewById(R.id.option4);
        option1.setOnClickListener(optionOnClickListener);
        option2.setOnClickListener(optionOnClickListener);
        option3.setOnClickListener(optionOnClickListener);
        option4.setOnClickListener(optionOnClickListener);

        Log.e("ssssssss" , "ProfilePreferences.getRadio() === " + ProfilePreferences.getRadiomode());
        radiocheck();
    }


    private void radiocheck(){

        if (ProfilePreferences.getRadiomode()==null){
            option1.setChecked(true);

        }else if(ProfilePreferences.getRadiomode().equals("ko")){
            Log.e("language", "ProfilePreferences.getLanguage().equals(1)");
            option1.setChecked(true);
            option1.setText("한글");
            option2.setText("영어");
            option3.setText("일본어");
            option4.setText("중국어");
        }else if(ProfilePreferences.getRadiomode().equals("en")){
            Log.e("language", "ProfilePreferences.getLanguage().equals(2)");
            option2.setChecked(true);
            option1.setText("Korea");
            option2.setText("Englisg");
            option3.setText("Japanese");
            option4.setText("Chinese");

        }else if(ProfilePreferences.getRadiomode().equals("ja")){
            Log.e("language", "ProfilePreferences.getLanguage().equals(3)");
            option3.setChecked(true);
            option1.setText("韓国語");
            option2.setText("英語");
            option3.setText("日本語");
            option4.setText("中国語");

        }else if(ProfilePreferences.getRadiomode().equals("zh")){
            Log.e("language", "ProfilePreferences.getLanguage().equals(4)");
            option4.setChecked(true);
            option1.setText("韩国语");
            option2.setText("英语");
            option3.setText("日本");
            option4.setText("汉语");

        }

    }

    public void setLocale(String charicter) {   // 27. 전체 엑티비티 설정 바꾸기
        Log.e("language" , "setLocale === " + charicter);
        Locale locale = new Locale(charicter);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Save x-position of horizontal scroll view.
        outState.putInt(KEY_SCROLL_X, svLanguageChooser.getScrollX());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore x-position of horizontal scroll view.
        svLanguageChooser.scrollTo(savedInstanceState.getInt(KEY_SCROLL_X), 0);
    }

    private void updateLanguage(Locale locale) {
        Log.d("ANDROID_LAB", locale.toString());
        try {
            Object objIActMag, objActMagNative;
            Class clzIActMag = Class.forName("android.app.IActivityManager");
            Class clzActMagNative = Class.forName("android.app.ActivityManagerNative");
            Method getDefault = clzActMagNative.getDeclaredMethod("getDefault");
// IActivityManager iActMag = ActivityManagerNative.getDefault();
            objIActMag = getDefault.invoke(clzActMagNative);
// Configuration config = iActMag.getConfiguration();
            Method getConfiguration = clzIActMag.getDeclaredMethod("getConfiguration");
            Configuration config = (Configuration) getConfiguration.invoke(objIActMag);
            config.locale = locale;
            Class[] clzParams = { Configuration.class };
            Method updateConfiguration = clzIActMag.getDeclaredMethod(
                    "updateConfiguration", clzParams);
            updateConfiguration.invoke(objIActMag, config);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    RadioButton.OnClickListener optionOnClickListener
            = new RadioButton.OnClickListener() {

        public void onClick(View v) {

            switch (v.getId()){

                case R.id.option1:
                    setLocale("ko");
                    ProfilePreferences.setRadiomode("ko");
                    mainIntent("ko");
                    ProfilePreferences.setBleCon(1);

                    break;
                case R.id.option2:
                    setLocale("en");
              //      return view -> setLanguage("en");
                    ProfilePreferences.setRadiomode("en");
                    ProfilePreferences.setBleCon(1);

                    mainIntent("en");
                    break;
                case R.id.option3:
                    ProfilePreferences.setRadiomode("ja");
                    setLocale("ja");
                    mainIntent("ja");
                    ProfilePreferences.setBleCon(1);
                    break;
                case R.id.option4:
                    ProfilePreferences.setRadiomode("zh");
                    setLocale("zh");
                    mainIntent("zh");
                    ProfilePreferences.setBleCon(1);
                    break;
            }

        }
    };

    private void mainIntent(String lang){

        String intentAction = UartService.ACTION_LANGUAGE_CHANGE;
        final Intent intent = new Intent(intentAction);
        intent.putExtra("lang", lang);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        finish();
    }
}
