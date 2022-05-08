package com.matescorp.system.zaigle.detailView;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;

import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.matescorp.system.zaigle.R;
import com.matescorp.system.zaigle.adapter.DetailValueAdapter;
import com.matescorp.system.zaigle.adapter.MainSQLiteHelper;
import com.matescorp.system.zaigle.data.DetailValueData;
import com.matescorp.system.zaigle.data.ProfilePreferences;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


/**
 * Created by sjkim on 17. 9. 12.
 */

public class DetailInfoActivity extends AppCompatActivity implements View.OnClickListener{


    private ListView mListView;
    private ArrayList<DetailValueData> mListData = new ArrayList<DetailValueData>();
    private double bmi;
    private double Basic;
    private int weight = Integer.parseInt(ProfilePreferences.getWeight().toString());
    private int height = Integer.parseInt(ProfilePreferences.getHeight().toString());
    String year = ProfilePreferences.getBirth();
    int year2 = Integer.parseInt(year);
    String nowyear = getNowTime("yyyy");
    int new_age = (Integer.parseInt(nowyear)-year2);
    private String bmi_standard, checkingDate;

    private Button b_day, b_week_, b_month, b_year;
    private BarChart chart;
    private int val, hr2,o2;
    private int c_num = 1;
    SQLiteDatabase database;
    MainSQLiteHelper helper;
    private int getString;
    Cursor ch;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.detail_main);
        Intent intent = getIntent();
        TextView text_app_title = (TextView)findViewById(R.id.text_app_title);
        ImageView pro_confirm = (ImageView)findViewById(R.id.my_state);
        pro_confirm.setImageDrawable(getResources().getDrawable(R.drawable.edit_icon));
        pro_confirm.setVisibility(View.GONE);
        pro_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                helper.dropCheck(database);
                Toast.makeText(DetailInfoActivity.this, "삭제되었습니다", Toast.LENGTH_SHORT).show();
                chart.clear();
                chart.invalidate();
            }
        });
        b_day = (Button)findViewById(R.id.btn_day);
        b_week_ = (Button)findViewById(R.id.btn_week);
        b_month = (Button)findViewById(R.id.btn_month);
        b_year = (Button)findViewById(R.id.btn_year);

        b_day.setOnClickListener(this);
        b_week_.setOnClickListener(this);
        b_month.setOnClickListener(this);
        b_year.setOnClickListener(this);
        checkingDate = getNowTime("yyyy-MM-dd");

        text_app_title.setText(intent.getStringExtra("d_title"));
        getString = intent.getIntExtra("d_value", 1);

        chart = (BarChart) findViewById(R.id.chart1);

        chart.setDrawGridBackground(false);
        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(false);
        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);
        chart.setPinchZoom(false);
        chart.setScaleXEnabled(false);

        helper = new MainSQLiteHelper(DetailInfoActivity.this);
        database = helper.getWritableDatabase();
//        mListView = (ListView)findViewById(R.id.detail_list);

        ImageView back = (ImageView)findViewById(R.id.icon_menu_view);
        back.setImageDrawable(getResources().getDrawable(R.drawable.back_icon));
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
//        value();

//        if(intent.getStringExtra("d_title").toString().equals("체지방")){
//            bodyFatList();
//        }else {
//            list();
//        }
        settingChart();
        select(database, c_num);

        selectTo(database);
      //  bodyFatList();

    }

    @Override
    public void onClick(View view) {
       switch (view.getId()){
           case R.id.btn_day :
               select(database, 1);
               break;
           case R.id.btn_week :
               select(database, 2);
               break;
           case R.id.btn_month:
               select(database, 3);
               break;
           case R.id.btn_year :
               select(database, 4);
               break;

       }
    }

    public void settingChart(){
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f); // only intervals of 1 day
        xAxis.setLabelCount(7);

        YAxis leftAxis = chart.getAxisLeft();  //y축 설정
        leftAxis.setLabelCount(3, false);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setAxisMaximum(120);
        leftAxis.setSpaceTop(15f);

        //leftAxis.setAxisMaximum(100);
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setDrawAxisLine(false);
        rightAxis.setDrawLabels(false);

      // this replaces setStartAtZero(true)

        Legend l = chart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setForm(Legend.LegendForm.SQUARE);
        l.setFormSize(9f);
        l.setTextSize(11f);
        l.setXEntrySpace(4f);
        // l.setExtra(ColorTemplate.VORDIPLOM_COLORS, new String[] { "abc",
        // "def", "ghj", "ikl", "mno" });
        // l.setCustom(ColorTemplate.VORDIPLOM_COLORS, new String[] { "abc",
        // "def", "ghj", "ikl", "mno" });
    }

    public void select(SQLiteDatabase db, int choice) {
        ArrayList<BarEntry> list = new ArrayList<BarEntry>();
        ArrayList<BarEntry> values = new ArrayList<BarEntry>();
        ArrayList<String> timeArr = new ArrayList<String>();
        Log.e("tjdwnd" , "select --  " +choice );
        list.clear();
        values.clear();
        timeArr.clear();
        chart.clear();


        if(choice ==1 ){
            ch = db.rawQuery("SELECT * FROM table_check_list ;",  null);
        }else if(choice ==2 ){
            ch = db.rawQuery("SELECT * FROM table_check_list Where Tdate >= date('now','weekday 0', '-7 days', 'localtime') AND Tdate <= date('now','weekday 0', '-1 days', 'localtime');", null);
        }else if(choice ==3 ){
            ch = db.rawQuery("SELECT * FROM table_check_list WHERE Tdate >= date('now','start of month','localtime') AND Tdate <= date('now','start of month','+1 month','-1 day','localtime');", null);
        }else if(choice ==4 ){
            ch = db.rawQuery("SELECT * FROM table_check_list Where Tdate >= date('now','start of month', 'localtime') AND Tdate <= date('now','start of month','+12 month','-1 day','localtime');", null);
        }
        int i = 0;
        int a = 0;
        while(ch.moveToNext()) {
//            String date = ch.getString(1);
//            String[] S_month = date.split("-");
//            Log.e("sql" , "S_month 0=== " + S_month[0] )
            i++;
            String date = ch.getString(1);
            String hr = ch.getString(2);
            String spo2 = ch.getString(3);
            hr2  = Integer.parseInt(hr);
            o2 = Integer.parseInt(spo2);

//            String id = ch.getString(0);
//            String bmi  = ch.getString(4);
//            String basicm = ch.getString(5);
//            String etc = ch.getString(6);
            String S_H = date.substring(11, 13);
            String S_W = date.substring(8, 10);
            String S_M = date.substring(5, 7);
            String S_Y = date.substring(0, 4);
            String S_Today = checkingDate.substring(0, 10);

            Log.e("s_w" , "date.substring(8, 10) === " + date.substring(8, 10));

            int H = Integer.parseInt(S_H);
            int W = Integer.parseInt(S_W);
            int M = Integer.parseInt(S_M);
            int Y = Integer.parseInt(S_Y);

            if(choice ==1 ) {
                if(date!=null) {
                    if (date.contains(S_Today)) {
                        if (timeArr.size() == 0) {
                            list.add(new BarEntry(H, hr2));
                            values.add(new BarEntry(H, o2));
                            timeArr.add(S_H);
                        } else {
                            if (timeArr.get(a).equals(S_H)) {
                                list.set(a, new BarEntry(H, hr2));
                                values.set(a, new BarEntry(H, o2));
                                timeArr.set(a, S_H);
                            } else {
                                list.add(new BarEntry(H, hr2));
                                values.add(new BarEntry(H, o2));
                                timeArr.add(S_H);
                                a++;
                            }
                        }
                    }
                }
            }
            else if(choice == 2 ){
                if(date!=null) {
                    if(timeArr.size()==0){
                        list.add(new BarEntry(W , hr2));
                        values.add(new BarEntry(W, o2));
                        timeArr.add(S_W);
                    }else{
                        if (timeArr.get(a).equals(S_W)) {
                            list.set(a, new BarEntry(W, hr2));
                            values.set(a, new BarEntry(W, o2));
                            timeArr.set(a,S_W);
                        } else {
                        timeArr.add(S_W);
                        list.add(new BarEntry(W, hr2));
                        values.add(new BarEntry(W, o2));
                        a++;
                        }
                    }
                }
            }else if(choice == 3 ){
                if(timeArr.size()==0){
                    list.add(new BarEntry(M , hr2));
                    values.add(new BarEntry(M, o2));
                    timeArr.add(S_M);
                }else {
                    if (date != null) {
                        if (timeArr.get(a).equals(S_M)) {
                            list.set(a, new BarEntry(M, hr2));
                            values.set(a, new BarEntry(M, o2));
                            timeArr.set(a,S_M);
                        } else {
                            timeArr.add(S_M);
                            list.add(new BarEntry(M, hr2));
                            values.add(new BarEntry(M, o2));
                            a++;
                        }
                    }
                }
            } else if(choice == 4 ){
                if(timeArr.size()==0){
                    list.add(new BarEntry(Y , hr2));
                    values.add(new BarEntry(Y, o2));
                    timeArr.add(S_Y);
                }else{
                    if(date!=null) {
                        if (timeArr.get(a).equals(S_Y)) {
                            list.set(a, new BarEntry(Y, hr2));
                            values.set(a, new BarEntry(Y, o2));
                            timeArr.set(a,S_Y);
                        } else {
                            timeArr.add(S_Y);
                            list.add(new BarEntry(Y, hr2));
                            values.add(new BarEntry(Y, o2));
                            a++;
                        }
                    }
                }
            }
        }
        BarDataSet dataSets;

        String label = "";
        if(choice == 1){
            label = "시";
        }else if(choice ==2 ){
            label = "일";
        }else if(choice ==3 ){
            label = "월";
        }else if(choice ==4 ){
            label = "년";
        }

        if(getString==0) {
             dataSets = new BarDataSet(list, label);
        }else{
            dataSets = new BarDataSet(values, label);
        }
        dataSets.setColor(Color.rgb(0,162,154));
      //  dataSets.setAxisDependency(YAxis.AxisDependency.RIGHT);

        List<IBarDataSet> list2 = new ArrayList<>();
        list2.add(dataSets);

        BarData data = new BarData(list2);
        data.setBarWidth(0.1f);

        chart.invalidate();
        chart.setData(data);
        chart.notifyDataSetChanged();

        i = 0;
        a = 0;
    }


    public void selectTo(SQLiteDatabase db) {
        ArrayList<BarEntry> list = new ArrayList<BarEntry>();
        ArrayList<Entry> values = new ArrayList<Entry>();

        Cursor ch = db.rawQuery("SELECT * FROM table_check_list Where Tdate >= date('now','weekday 0', '-7 days', 'localtime') AND Tdate <= date('now','weekday 0', '-1 days', 'localtime');", null);
        while(ch.moveToNext()) {
            String date = ch.getString(1);
            String hr = ch.getString(2);
            String spo2 = ch.getString(3);
            Log.d("sqlite2", "working data ==== "+ date );
            Log.d("sqlite2", "working data ==== "+ hr);
            Log.d("sqlite2", "working data ==== "+ spo2);
        }
    }

    private void bodyFatList(){

        mListData.clear();
      // List<DetailValueData> list = new ArrayList<DetailValueData>();
        ArrayList<BarEntry> list = new ArrayList<BarEntry>();

        String v_bmi = ""+bmi;
        String v_basic =  ""+Basic;

        list.add(new BarEntry(0 ,val ));
//        list.add(new DetailValueData("체지방량" ,"60" ,"Graph"));
//        list.add(new DetailValueData("근육량" ,"92" ,"Graph"));
//        list.add(new DetailValueData("BMI" , v_bmi, bmi_standard));
//        list.add(new DetailValueData("기초대사량" ,v_basic ,"Kcal"));
//        list.add(new DetailValueData("비만도" ,"25" ,"Graph"));
//        list.add(new DetailValueData("신체나이" ,"25" ,"Graph"));
//        list.add(new DetailValueData("체형판정" ,"25" ,"Graph"));
//        list.add(new DetailValueData("나의목표" ,"25" ,"Graph"));
//
//       for (int i =0 ; i < list.size(); i++){
//            mListData.add(list.get(i));
//        }
//        mListView.setAdapter(new DetailValueAdapter(this, mListData));
        BarDataSet dataSets = new BarDataSet(list, "aaa");
        dataSets.setAxisDependency(YAxis.AxisDependency.RIGHT);

        List<IBarDataSet> list2 = new ArrayList<>();
        list2.add(dataSets);

        BarData data = new BarData(list2);
        data.setBarWidth(0.1f);
        chart.setData(data);
        chart.invalidate();

//        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//
//                ValueData data = mListData.get(i);
//                Intent intent = new Intent(MainActivity.this, DetailInfoActivity.class);
//                intent.putExtra("d_title", data.getmTitle());
//                startActivity(intent);
//            }
//        });


    }

    private void value(){
        bmi = weight/((height*0.01)*(height*0.01));
        if(bmi<18.4){
            bmi_standard = "저체중";
        }else if( bmi >= 18.5 && bmi <=24.9  ){
            bmi_standard = "정상";
        }else if (bmi >=25 && bmi <=29.9 ){
            bmi_standard = "과체중";
        }else if(bmi >= 30) {
            bmi_standard = "비만";
        }
        bmi = Double.parseDouble(String.format("%.1f", bmi));

        if (ProfilePreferences.getGender().equals("0")) {
            Basic = 66.47 + (13.75 * weight) + (5 * height) - (6.76 * new_age);
            Basic = Double.parseDouble(String.format("%.1f", Basic));
        }else {
            Basic = 665.1 + (9.56 * weight) + (1.85 * height) - (4.68 * new_age);
            Basic = Double.parseDouble(String.format("%.1f", Basic));
        }
    }


    private void list(){

        mListData.clear();
        List<DetailValueData> list = new ArrayList<DetailValueData>();

        list.add(new DetailValueData("평균" ,"123회"));


        for (int i =0 ; i < list.size(); i++){
            mListData.add(list.get(i));
        }
        mListView.setAdapter(new DetailValueAdapter(this, mListData));

    }


    public static String getNowTime(String formatType) {
        long time = System.currentTimeMillis();
        SimpleDateFormat dayTime = new SimpleDateFormat(formatType);
        String nowTime = dayTime.format(new Date(time));

        return nowTime;
    }


}
