package com.matescorp.system.zaigle.settingView;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.matescorp.system.zaigle.GuestMainActivity;
import com.matescorp.system.zaigle.MainActivity;
import com.matescorp.system.zaigle.Profile;
import com.matescorp.system.zaigle.R;
import com.matescorp.system.zaigle.data.ProfilePreferences;

/**
 * Created by sjkim on 17. 7. 12.
 */

public class GuestInfoActivity extends AppCompatActivity implements View.OnClickListener{
    private int sex = -1;
    private String gender, birth, height, weight, name;
    private EditText pro_year, pro_weight, pro_name, pro_height;
    private Button pro_confirm_g;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_guestinfo);

        TextView title = (TextView)findViewById(R.id.text_app_title);
        title.setText(R.string.guest_test);
        ImageView back = (ImageView)findViewById(R.id.icon_menu_view);
        back.setImageDrawable(getResources().getDrawable(R.drawable.back_icon));
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        pro_weight = (EditText)findViewById(R.id.pro_weight_g);
        pro_height= (EditText)findViewById(R.id.pro_height_g);
        pro_year= (EditText)findViewById(R.id.pro_year_g);
        pro_name= (EditText)findViewById(R.id.pro_name_g);
        pro_confirm_g = (Button)findViewById(R.id.pro_confirm_g);
        pro_confirm_g.setOnClickListener(this);

        if(ProfilePreferences.getG_birth()!=null){
            data_init();
        }

    }

    private void data_init(){
        Log.e("g_profile", " ProfilePreferences.getG_Gender().toString() === " + ProfilePreferences.getG_Gender().toString());
        Log.e("g_profile", " ProfilePreferences.getG_birth().toString() === " + ProfilePreferences.getG_birth().toString());
        Log.e("g_profile", " ProfilePreferences.getG_Height().toString() === " + ProfilePreferences.getG_Height().toString());
        Log.e("g_profile", " ProfilePreferences.getG_Weight().toString() === " + ProfilePreferences.getG_Weight().toString());
        Log.e("g_profile", " ProfilePreferences.getG_Name().toString() === " + ProfilePreferences.getG_Name().toString());

        gender = ProfilePreferences.getG_Gender().toString();
        birth = ProfilePreferences.getG_birth().toString();
        height = ProfilePreferences.getG_Height().toString();
        weight = ProfilePreferences.getG_Weight().toString();
        name = ProfilePreferences.getG_Name().toString();

        pro_year.setText(birth);
        pro_weight.setText(weight);
        pro_height.setText(height);
        pro_name.setText(name);

        if(gender.equals("0")){
            ImageView sex_man = (ImageView)findViewById(R.id.sex_man_g);
            sex_man.setImageResource(R.drawable.btn_contents_checkbox_checked);
            ImageView sex_woman = (ImageView)findViewById(R.id.sex_woman_g);
            sex_woman.setImageResource(R.drawable.btn_contents_checkbox_normal);
            sex = 0;
        }else {
            ImageView sex_man = (ImageView)findViewById(R.id.sex_man_g);
            sex_man.setImageResource(R.drawable.btn_contents_checkbox_normal);
            ImageView sex_woman = (ImageView)findViewById(R.id.sex_woman_g);
            sex_woman.setImageResource(R.drawable.btn_contents_checkbox_checked);
            sex = 1;
        }
    }

    public void onSexWomanClieckd(final View view) {
        ImageView sex_man = (ImageView)findViewById(R.id.sex_man_g);
        sex_man.setImageResource(R.drawable.btn_contents_checkbox_normal);
        ImageView sex_woman = (ImageView)findViewById(R.id.sex_woman_g);
        sex_woman.setImageResource(R.drawable.btn_contents_checkbox_checked);
        sex = 1;
    }
    public void onSexManClieckd(final View view) {
        ImageView sex_man = (ImageView)findViewById(R.id.sex_man_g);
        sex_man.setImageResource(R.drawable.btn_contents_checkbox_checked);
        ImageView sex_woman = (ImageView)findViewById(R.id.sex_woman_g);
        sex_woman.setImageResource(R.drawable.btn_contents_checkbox_normal);
        sex = 0;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.pro_confirm_g :

                String gender = String.valueOf(sex);

                if(pro_height.length() ==0 || pro_weight.length() == 0 || pro_year.length() ==0 ){
                    Toast.makeText(this, "빈칸을 입력해 주세요", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(GuestInfoActivity.this, MainActivity.class);

                ProfilePreferences.setGenderG(gender);
                ProfilePreferences.setWeightG(pro_weight.getText().toString());
                ProfilePreferences.setHeightG(pro_height.getText().toString());
                ProfilePreferences.setNameG(pro_name.getText().toString());
                ProfilePreferences.setBirthG(pro_year.getText().toString());

                setResult(1001, intent);
                finish();
                break;
        }
    }

}
