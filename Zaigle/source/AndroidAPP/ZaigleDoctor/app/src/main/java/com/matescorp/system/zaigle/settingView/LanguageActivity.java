package com.matescorp.system.zaigle.settingView;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.matescorp.system.zaigle.MainActivity;
import com.matescorp.system.zaigle.R;
import com.matescorp.system.zaigle.data.ProfilePreferences;

/**
 * Created by sjkim on 17. 8. 30.
 */

public class LanguageActivity extends AppCompatActivity {


    private LinearLayout choice_language;
    private TextView select_language;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.setting_language_two);
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


        if(ProfilePreferences.getLanguage() == null){

        }else {
            Log.e("LanguageActivity", "sex ===== " + ProfilePreferences.getGender().toString());
        }
        choice_language = (LinearLayout)findViewById(R.id.choice_language);
        select_language = (TextView)findViewById(R.id.select_language);

        choice_language.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LanguageActivity.this, LanguageChoiceActivity.class);
                startActivity(intent);
                finish();
            }
        });

        if (ProfilePreferences.getRadiomode() == null){
            select_language.setText(R.string.language_set);
        } else if(ProfilePreferences.getRadiomode().equals("ko")){
            select_language.setText("한국어");
        }else if(ProfilePreferences.getRadiomode().equals("en")){
            select_language.setText("English");
        }else if(ProfilePreferences.getRadiomode().equals("ja")){
            select_language.setText("日本語");
        }else if(ProfilePreferences.getRadiomode().equals("zh")){
            select_language.setText("汉语");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.e("sssssss", "ProfilePreferences.getLanguage().toString()); ==== "  + ProfilePreferences.getRadiomode());
    }
}
