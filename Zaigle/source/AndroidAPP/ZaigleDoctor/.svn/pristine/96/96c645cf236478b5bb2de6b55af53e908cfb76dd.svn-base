package com.matescorp.system.zaigle;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.matescorp.system.zaigle.data.ProfilePreferences;

/**
 * Created by sjkim on 17. 7. 4.
 */

public class Profile extends AppCompatActivity implements View.OnClickListener{

    private String gender;
    private String birth;
    private String height;
    private String weight;
    private String name;
    private EditText pro_year;
    private EditText pro_weight;
    private EditText pro_name;
    private EditText pro_height;
    private Button pro_confirm;
    private int sex = -1;
    private String TAG = "Profile";

    private int page_flag = 0;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile);

        Intent intent = getIntent();
        page_flag = intent.getIntExtra("flag", 0);

        ProfilePreferences.PREF = PreferenceManager.getDefaultSharedPreferences(this);
        ImageView pro_confirm = (ImageView)findViewById(R.id.my_state);
        pro_confirm.setImageDrawable(getResources().getDrawable(R.drawable.edit_icon));
        pro_confirm.setVisibility(View.VISIBLE);
        pro_weight = (EditText)findViewById(R.id.pro_weight);
        pro_height= (EditText)findViewById(R.id.pro_height);
        pro_year= (EditText)findViewById(R.id.pro_year);
        pro_name= (EditText)findViewById(R.id.pro_name);

        TextView title = (TextView)findViewById(R.id.text_app_title);
        title.setText(R.string.user_info);
        ImageView back = (ImageView)findViewById(R.id.icon_menu_view);
        back.setImageDrawable(getResources().getDrawable(R.drawable.back_icon));
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        if(ProfilePreferences.getBirth() != null){
            data_init();
        }
        pro_confirm.setOnClickListener(this);

        Button bt_ok = (Button)findViewById(R.id.bt_ok);
        bt_ok.setOnClickListener(this);
        if ( page_flag == 1 ) {
            pro_confirm.setVisibility(View.INVISIBLE);
            back.setVisibility(View.INVISIBLE);
            bt_ok.setVisibility(View.VISIBLE);
        }

    }

    private void data_init(){
        gender = ProfilePreferences.getGender().toString();
        birth = ProfilePreferences.getBirth().toString();
        height = ProfilePreferences.getHeight().toString();
        weight = ProfilePreferences.getWeight().toString();
        name = ProfilePreferences.getName().toString();

        pro_year.setText(birth);
        pro_weight.setText(weight);
        pro_height.setText(height);
        pro_name.setText(name);

        if(gender.equals("0")){
            ImageView sex_man = (ImageView)findViewById(R.id.sex_man);
            sex_man.setImageResource(R.drawable.btn_contents_checkbox_checked);
            ImageView sex_woman = (ImageView)findViewById(R.id.sex_woman);
            sex_woman.setImageResource(R.drawable.btn_contents_checkbox_normal);
            sex = 0;
        }else {
            ImageView sex_man = (ImageView)findViewById(R.id.sex_man);
            sex_man.setImageResource(R.drawable.btn_contents_checkbox_normal);
            ImageView sex_woman = (ImageView)findViewById(R.id.sex_woman);
            sex_woman.setImageResource(R.drawable.btn_contents_checkbox_checked);
            sex = 1;
        }
    }

    public void onSexWomanClieckd(final View view) {
        Log.d(TAG, "onSexWomanClieckd");
        ImageView sex_man = (ImageView)findViewById(R.id.sex_man);
        sex_man.setImageResource(R.drawable.btn_contents_checkbox_normal);
        ImageView sex_woman = (ImageView)findViewById(R.id.sex_woman);
        sex_woman.setImageResource(R.drawable.btn_contents_checkbox_checked);
        sex = 1;
    }
    public void onSexManClieckd(final View view) {
        Log.d(TAG, "onSexManClieckd");
        ImageView sex_man = (ImageView)findViewById(R.id.sex_man);
        sex_man.setImageResource(R.drawable.btn_contents_checkbox_checked);
        ImageView sex_woman = (ImageView)findViewById(R.id.sex_woman);
        sex_woman.setImageResource(R.drawable.btn_contents_checkbox_normal);
        sex = 0;
    }

    @Override
    public void onClick(View view) {
        String gender = String.valueOf(sex);
        switch (view.getId()){
            case R.id.my_state :
                if(pro_height.length() ==0 || pro_weight.length() == 0 || pro_year.length() ==0 ){
                    Toast.makeText(this, "빈칸을 입력해 주세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                ProfilePreferences.setHeight(pro_height.getText().toString());
                ProfilePreferences.setWeight(pro_weight.getText().toString());
                ProfilePreferences.setBirth(pro_year.getText().toString());
                ProfilePreferences.setGender(gender);
                ProfilePreferences.setName(pro_name.getText().toString());

                if ( page_flag == 1 ) {
                    Intent intent = new Intent(Profile.this, MainActivity.class);
                    startActivity(intent);
                }
                finish();
                break;
            case R.id.bt_ok :
                if(pro_height.length() ==0 || pro_weight.length() == 0 || pro_year.length() ==0 ){
                    Toast.makeText(this, "빈칸을 입력해 주세요", Toast.LENGTH_SHORT).show();
                    return;
                }
                ProfilePreferences.setHeight(pro_height.getText().toString());
                ProfilePreferences.setWeight(pro_weight.getText().toString());
                ProfilePreferences.setBirth(pro_year.getText().toString());
                ProfilePreferences.setGender(gender);
                ProfilePreferences.setName(pro_name.getText().toString());
                if ( page_flag == 1 ) {
                    Intent intent = new Intent(Profile.this, MainActivity.class);
                    startActivity(intent);
                }
                finish();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent returnIntent = new Intent(Profile.this, MainActivity.class);
        setResult(Profile.RESULT_CANCELED, returnIntent);
        finish();

    }
}
