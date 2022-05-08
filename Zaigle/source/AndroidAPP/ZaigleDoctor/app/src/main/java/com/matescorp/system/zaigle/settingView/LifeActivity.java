package com.matescorp.system.zaigle.settingView;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.matescorp.system.zaigle.MainActivity;
import com.matescorp.system.zaigle.R;
import com.matescorp.system.zaigle.data.ProfilePreferences;

import static com.matescorp.system.zaigle.MainActivity.getNowTime;

/**
 * Created by sjkim on 17. 8. 31.
 */

public class LifeActivity extends AppCompatActivity {


    private TextView name;
    private TextView age;
    private TextView gender;
    private TextView height;
    private TextView weight;
    private LinearLayout no_data_info;
    private LinearLayout data_info;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.setting_life);
        no_data_info = (LinearLayout)findViewById(R.id.no_data_info);
        data_info = (LinearLayout)findViewById(R.id.data_info);

        ImageView back = (ImageView)findViewById(R.id.icon_menu_view);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        ImageView home = (ImageView)findViewById(R.id.icon_history);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent  = new Intent(LifeActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });


        if (ProfilePreferences.getBirth()==null){
            no_data_info.setVisibility(View.VISIBLE);
            data_info.setVisibility(View.GONE);
        }else {
            no_data_info.setVisibility(View.GONE);
            data_info.setVisibility(View.VISIBLE);
            myInfo();
        }


    }



    private void myInfo(){

        TextView life_age = (TextView)findViewById(R.id.life_age);
        TextView life_sex = (TextView)findViewById(R.id.life_gender);
        TextView life_height = (TextView)findViewById(R.id.life_height);
        TextView life_weight = (TextView)findViewById(R.id.life_weight);
        TextView main_name = (TextView)findViewById(R.id.life_name);

        String year = ProfilePreferences.getBirth();
        int year2 = Integer.parseInt(year);
        String nowyear = getNowTime("yyyy");
        int new_age = (Integer.parseInt(nowyear)-year2);



        life_age.setText(String.valueOf(new_age));
        life_height.setText(ProfilePreferences.getHeight());
        life_weight.setText(ProfilePreferences.getWeight());
        if(ProfilePreferences.getGender().equals("0")){
            life_sex.setText(R.string.man);

        }else {
            life_sex.setText(R.string.woman);
        }



    }

}
