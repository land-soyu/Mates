package com.matescorp.system.zaigle.detailView;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.matescorp.system.zaigle.R;
import com.matescorp.system.zaigle.adapter.BodyFatValueAdapter;
import com.matescorp.system.zaigle.adapter.MainSQLiteHelper;
import com.matescorp.system.zaigle.data.BodyValueData;
import com.matescorp.system.zaigle.data.ProfilePreferences;
import com.matescorp.system.zaigle.data.ValueData;
import com.matescorp.system.zaigle.settingView.RecentDataActivity;

import java.util.ArrayList;

/**
 * Created by Administrator on 2018-01-10.
 */



public class BodyFatDetailActivity extends AppCompatActivity {
    private ListView listView = null;
    private BodyFatValueAdapter adapter;
    private ArrayList<BodyValueData> mListData = new ArrayList<BodyValueData>();
    private String bmi_val, kal_val, water_val, minerals_val, muscle_val;
    private int new_age;
    SQLiteDatabase database;
    MainSQLiteHelper helper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.body_fat_detail);

        ImageView back = (ImageView)findViewById(R.id.icon_menu_view);
        back.setImageDrawable(getResources().getDrawable(R.drawable.back_icon));
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        listView = (ListView)findViewById(R.id.b_list);
        listView.setAdapter(adapter);
        helper = new MainSQLiteHelper(BodyFatDetailActivity.this);
        database = helper.getWritableDatabase();

        Intent intent = getIntent();
        new_age = intent.getIntExtra("age", 0 );

        makeList(database);
    }

    public void makeList(SQLiteDatabase db){

        mListData.clear();

        int HIGH = 0, STAND = 1, LOW = 2;
        Integer[] imageId = {
                R.drawable.above_standard,
                R.drawable.standard_icon,
                R.drawable.below_standard
        };

        Cursor ch = db.rawQuery("select * from table_check_list;", null);
        ch.moveToLast();

        bmi_val = ch.getString(4);
        kal_val = ch.getString(5);
        water_val = ch.getString(7);
        minerals_val = ch.getString(8);
        muscle_val = ch.getString(9);

        String gender = ProfilePreferences.getGender();
        //  기초대사량
        int kcal = 0;
        int k = Integer.parseInt(kal_val);
        if ( gender.equals("0") ) {    //  man
            if ( new_age < 8 ) {
                if ( k > 1200 ) { kcal = HIGH;
                } else if ( k < 1000 ) { kcal = LOW;
                } else { kcal = STAND; }
            } else if ( new_age < 14 ) {
                if ( k > 1400 ) { kcal = HIGH;
                } else if ( k < 1200 ) { kcal = LOW;
                } else { kcal = STAND; }
            } else if ( new_age < 18 ) {
                if ( k > 1710 ) { kcal = HIGH;
                } else if ( k < 1500 ) { kcal = LOW;
                } else { kcal = STAND; }
            } else if ( new_age < 30 ) {
                if ( k > 2000 ) { kcal = HIGH;
                } else if ( k < 1400 ) { kcal = LOW;
                } else { kcal = STAND; }
            } else if ( new_age < 50 ) {
                if ( k > 1800 ) { kcal = HIGH;
                } else if ( k < 1350 ) { kcal = LOW;
                } else { kcal = STAND; }
            } else if ( new_age < 60 ) {
                if ( k > 1700 ) { kcal = HIGH;
                } else if ( k < 1200 ) { kcal = LOW;
                } else { kcal = STAND; }
            } else {
                if ( k > 1300 ) { kcal = HIGH;
                } else if ( k < 1100 ) { kcal = LOW;
                } else { kcal = STAND; }
            }
        } else {    //  woman
            if ( new_age < 8 ) {
                if ( k > 1100 ) { kcal = HIGH;
                } else if ( k < 900 ) { kcal = LOW;
                } else { kcal = STAND; }
            } else if ( new_age < 14 ) {
                if ( k > 1280 ) { kcal = HIGH;
                } else if ( k < 1080 ) { kcal = LOW;
                } else { kcal = STAND; }
            } else if ( new_age < 18 ) {
                if ( k > 1400 ) { kcal = HIGH;
                } else if ( k < 1200 ) { kcal = LOW;
                } else { kcal = STAND; }
            } else if ( new_age < 30 ) {
                if ( k > 1500 ) { kcal = HIGH;
                } else if ( k < 1100 ) { kcal = LOW;
                } else { kcal = STAND; }
            } else if ( new_age < 50 ) {
                if ( k > 1450 ) { kcal = HIGH;
                } else if ( k < 1050 ) { kcal = LOW;
                } else { kcal = STAND; }
            } else if ( new_age < 60 ) {
                if ( k > 1350 ) { kcal = HIGH;
                } else if ( k < 1000 ) { kcal = LOW;
                } else { kcal = STAND; }
            } else {
                if ( k > 1100 ) { kcal = HIGH;
                } else if ( k < 900 ) { kcal = LOW;
                } else { kcal = STAND; }
            }
        }
        mListData.add(new BodyValueData(getResources().getDrawable(R.drawable.body_fat_icon),"기초대사량" ,kal_val ,  getResources().getDrawable(imageId[kcal])));

        // 비만도 ( BMI )
        int bmi = 0;
        int b = Integer.parseInt(bmi_val);
        if ( b < 18 ) { bmi = LOW;
        } else if( b > 23 ) { bmi = HIGH;
        } else { bmi = STAND;
        }
        mListData.add(new BodyValueData(getResources().getDrawable(R.drawable.body_fat_icon),"BMI" ,bmi_val ,  getResources().getDrawable(imageId[bmi])));
        mListData.add(new BodyValueData(getResources().getDrawable(R.drawable.body_fat_icon),"체수분" ,water_val ,  getResources().getDrawable(imageId[1])));
        mListData.add(new BodyValueData(getResources().getDrawable(R.drawable.body_fat_icon),"무기질" ,minerals_val ,  getResources().getDrawable(imageId[1])));
        mListData.add(new BodyValueData(getResources().getDrawable(R.drawable.body_fat_icon),"근육량" ,muscle_val ,  getResources().getDrawable(imageId[1])));

        listView.setAdapter(adapter = new BodyFatValueAdapter(this, mListData));

    }

}
