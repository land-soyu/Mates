package com.matescorp.system.zaigle.settingView;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.matescorp.system.zaigle.R;
import com.matescorp.system.zaigle.adapter.BodyFatValueAdapter;
import com.matescorp.system.zaigle.adapter.MainSQLiteHelper;
import com.matescorp.system.zaigle.data.BodyValueData;
import com.matescorp.system.zaigle.data.ProfilePreferences;
import com.matescorp.system.zaigle.detailView.BodyFatDetailActivity;
import com.matescorp.system.zaigle.detailView.DetailInfoActivity;

import java.util.ArrayList;

/**
 * Created by Administrator on 2018-01-15.
 */

public class RecentDataActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{

    private ListView listView = null;
    private BodyFatValueAdapter adapter;
    private ArrayList<BodyValueData> mListData = new ArrayList<BodyValueData>();
    SQLiteDatabase database;
    MainSQLiteHelper helper;
    private int new_age ;
    private int position ;
    private TextView text;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.recent_data);


        ImageView back = (ImageView)findViewById(R.id.icon_menu_view);
        back.setImageDrawable(getResources().getDrawable(R.drawable.back_icon));
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        text = (TextView)findViewById(R.id.recent_text);

        listView = (ListView)findViewById(R.id.b_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        helper = new MainSQLiteHelper(RecentDataActivity.this);
        database = helper.getWritableDatabase();

        Intent intent = getIntent();
        new_age = intent.getIntExtra("age", 0 );
        position = intent.getIntExtra("position", 0 );

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
        if(ch.getCount()==0){
            text.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
            return;
        }
        ch.moveToPosition(position);


            String hr = ch.getString(2);
            String spo2 = ch.getString(3);
            String fatp= ch.getString(10);

        int heart = 0;
        int h = Integer.parseInt(hr);
        if (new_age <= 1) {
            if (h > 180) {
                heart = HIGH;
            } else if (h < 101) {
                heart = LOW;
            } else {
                heart = STAND;
            }
        } else if (new_age < 4) {
            if (h > 150) {
                heart = HIGH;
            } else if (h < 90) {
                heart = LOW;
            } else {
                heart = STAND;
            }
        } else if (new_age < 18) {
            if (h > 110) {
                heart = HIGH;
            } else if (h < 60) {
                heart = LOW;
            } else {
                heart = STAND;
            }
        } else if (new_age < 60) {
            if (h > 80) {
                heart = HIGH;
            } else if (h < 65) {
                heart = LOW;
            } else {
                heart = STAND;
            }
        } else {
            if (h > 85) {
                heart = HIGH;
            } else if (h < 62) {
                heart = LOW;
            } else {
                heart = STAND;
            }
        }
        mListData.add(new BodyValueData(getResources().getDrawable(R.drawable.body_fat_icon),"?????????" ,hr ,  getResources().getDrawable(imageId[heart])));

        //  ?????? ?????????
        int oxygen = 0;
        int o = Integer.parseInt(spo2);
        if (o == 100) {
            oxygen = HIGH;
        } else if (o < 85) {
            oxygen = LOW;
        } else {
            oxygen = STAND;
        }
        mListData.add(new BodyValueData(getResources().getDrawable(R.drawable.body_fat_icon),"???????????????" ,spo2 ,  getResources().getDrawable(imageId[oxygen])));

        //  ????????????
        int percent = 0;
        int p = Integer.parseInt(fatp);
        if (ProfilePreferences.getGender().equals("0")) {    //  man
            if (new_age < 18) {
                if (p > 20) {
                    percent = HIGH;
                } else if (p < 8) {
                    percent = LOW;
                } else {
                    percent = STAND;
                }
            } else if (new_age < 40) {
                if (p > 22) {
                    percent = HIGH;
                } else if (p < 11) {
                    percent = LOW;
                } else {
                    percent = STAND;
                }
            } else if (new_age < 60) {
                if (p > 25) {
                    percent = HIGH;
                } else if (p < 13) {
                    percent = LOW;
                } else {
                    percent = STAND;
                }
            }
        } else {    //  woman
            if (new_age < 18) {
                if (p > 33) {
                    percent = HIGH;
                } else if (p < 20) {
                    percent = LOW;
                } else {
                    percent = STAND;
                }
            } else if (new_age < 40) {
                if (p > 34) {
                    percent = HIGH;
                } else if (p < 22) {
                    percent = LOW;
                } else {
                    percent = STAND;
                }
            } else if (new_age < 60) {
                if (p > 36) {
                    percent = HIGH;
                } else if (p < 23) {
                    percent = LOW;
                } else {
                    percent = STAND;
                }
            }
        }
        mListData.add(new BodyValueData(getResources().getDrawable(R.drawable.body_fat_icon),"????????????" ,null ,  getResources().getDrawable(imageId[1])));
        mListData.add(new BodyValueData(getResources().getDrawable(R.drawable.body_fat_icon),"????????????" , fatp ,  getResources().getDrawable(imageId[percent])));
        mListData.add(new BodyValueData(getResources().getDrawable(R.drawable.body_fat_icon),"?????????" ,null ,  getResources().getDrawable(imageId[1])));

        listView.setAdapter(adapter = new BodyFatValueAdapter(this, mListData));

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (adapter != null) {
            if (i == 3) {
                Intent intent = new Intent(RecentDataActivity.this, BodyFatDetailActivity.class);
                intent.putExtra("age" , new_age);
                startActivity(intent);
            }else if (i ==0) {
                BodyValueData data = mListData.get(i);
                Intent intent = new Intent(RecentDataActivity.this, DetailInfoActivity.class);
                intent.putExtra("d_title", data.getmTitle());
                intent.putExtra("d_value", i);
                startActivity(intent);
            }else if (i ==1) {
                BodyValueData data = mListData.get(i);
                Intent intent = new Intent(RecentDataActivity.this, DetailInfoActivity.class);
                intent.putExtra("d_title", data.getmTitle());
                intent.putExtra("d_value", i);
                startActivity(intent);
            }

        }

    }
}
