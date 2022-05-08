package com.matescorp.system.zaigle.settingView;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.matescorp.system.zaigle.R;
import com.matescorp.system.zaigle.data.ProfilePreferences;

/**
 * Created by sjkim on 17. 7. 12.
 */

public class UserInfoActivity extends AppCompatActivity{

    private String gender;
    private String birth;
    private String height;
    private String weight;
    private String TAG = "UserInfoActivity";
    private int sex = -1;

    private EditText pro_year;
    private EditText pro_weight;
    private EditText pro_height;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.setting_userinfo);
        ImageView back = (ImageView)findViewById(R.id.icon_menu_view_u);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                        finish();
            }
        });

        init();
    }

    private void init(){

        gender = ProfilePreferences.getGender().toString();
        birth = ProfilePreferences.getBirth().toString();
        height = ProfilePreferences.getHeight().toString();
        weight = ProfilePreferences.getWeight().toString();

        pro_year = (EditText)findViewById(R.id.pro_year_u);
        pro_year.setText(birth);
        pro_weight = (EditText)findViewById(R.id.pro_weight_u);
        pro_weight.setText(weight);
        pro_height = (EditText)findViewById(R.id.pro_height_u);
        pro_height.setText(height);

        if(gender.equals("0")){
            ImageView sex_man = (ImageView)findViewById(R.id.sex_man_u);
            sex_man.setImageResource(R.drawable.btn_contents_checkbox_checked);
            ImageView sex_woman = (ImageView)findViewById(R.id.sex_woman_u);
            sex_woman.setImageResource(R.drawable.btn_contents_checkbox_normal);
            sex = 0;
        }else {
            ImageView sex_man = (ImageView)findViewById(R.id.sex_man_u);
            sex_man.setImageResource(R.drawable.btn_contents_checkbox_normal);
            ImageView sex_woman = (ImageView)findViewById(R.id.sex_woman_u);
            sex_woman.setImageResource(R.drawable.btn_contents_checkbox_checked);
            sex = 1;
        }

        Button pro_confirm_u = (Button)findViewById(R.id.pro_confirm_u);
        pro_confirm_u.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String gender = String.valueOf(sex);

                ProfilePreferences.setHeight(pro_height.getText().toString());
                ProfilePreferences.setWeight(pro_weight.getText().toString());
                ProfilePreferences.setBirth(pro_year.getText().toString());
                ProfilePreferences.setGender(gender);
            }
        });

    }


    public void onSexWomanClieckd(final View view) {
        Log.d(TAG, "onSexWomanClieckd");
        ImageView sex_man = (ImageView)findViewById(R.id.sex_man_u);
        sex_man.setImageResource(R.drawable.btn_contents_checkbox_normal);
        ImageView sex_woman = (ImageView)findViewById(R.id.sex_woman_u);
        sex_woman.setImageResource(R.drawable.btn_contents_checkbox_checked);
        sex = 1;
    }
    public void onSexManClieckd(final View view) {
        Log.d(TAG, "onSexManClieckd");
        ImageView sex_man = (ImageView)findViewById(R.id.sex_man_u);
        sex_man.setImageResource(R.drawable.btn_contents_checkbox_checked);
        ImageView sex_woman = (ImageView)findViewById(R.id.sex_woman_u);
        sex_woman.setImageResource(R.drawable.btn_contents_checkbox_normal);
        sex = 0;
    }


}
