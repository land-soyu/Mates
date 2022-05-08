package com.matescorp.system.zaigle.settingView;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

public class RecentAllDataActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{

    private ListView listView = null;
    SQLiteDatabase database;
    MainSQLiteHelper helper;
    private int new_age ;
    private TextView text;
    private BodyFatValueAdapter adapter;
    private ArrayList<BodyValueData> mListData = new ArrayList<BodyValueData>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.recent_list);


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

        Intent intent = getIntent();
        new_age = intent.getIntExtra("age", 0 );
        listView.setOnItemClickListener(this);
        helper = new MainSQLiteHelper(RecentAllDataActivity.this);
        database = helper.getWritableDatabase();

        makeList(database);

    }


    public void makeList(SQLiteDatabase db){
        mListData.clear();
       Cursor ch = db.rawQuery("SELECT * FROM table_check_list ORDER BY Tdate DESC;",  null);
        if(ch.getCount()==0){
            text.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
            return;
        }
        while(ch.moveToNext()) {
            String date = ch.getString(1);
            Log.e("a" , "date ==== " + date) ;
            mListData.add(new BodyValueData(getResources().getDrawable(R.drawable.heart_icon),"" ,date ,  null));
        }
        listView.setAdapter(adapter = new BodyFatValueAdapter(this, mListData));
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        if (adapter != null) {
                Intent intent = new Intent(RecentAllDataActivity.this, RecentDataActivity.class);
                intent.putExtra("age" , new_age);
                intent.putExtra("position" , position);
                startActivity(intent);
            }
    }


}
