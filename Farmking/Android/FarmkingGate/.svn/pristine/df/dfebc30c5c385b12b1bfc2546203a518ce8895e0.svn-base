package com.matescorp.soyu.farmkinggate.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.matescorp.soyu.farmkinggate.R;
import com.matescorp.soyu.farmkinggate.adapter.ListViewItem;
import com.matescorp.soyu.farmkinggate.asynctask.GetFarmSensorItemListDataHTTPTask;
import com.matescorp.soyu.farmkinggate.asynctask.GetFarmSensorListDataHTTPTask;
import com.matescorp.soyu.farmkinggate.util.Config;
import com.matescorp.soyu.farmkinggate.util.Logger;
import com.matescorp.soyu.farmkinggate.util.MyMarkerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created by soyu on 17. 9. 25.
 */
public class SensorItemActivity extends Activity {
    private String id;
    private Context INSTANCE;
    private DataHandler mDataHandler;

    private TextView sensor_item_temp, sensor_item_move, sensor_item_date;

    private LineChart mChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_sensor_item);

        INSTANCE = this;
        id = getIntent().getStringExtra("id");

        Button back = (Button) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView sensor_item_title = (TextView)findViewById(R.id.sensor_item_title);
        sensor_item_title.setText(id);

        sensor_item_temp = (TextView)findViewById(R.id.sensor_item_temp);
        sensor_item_move = (TextView)findViewById(R.id.sensor_item_move);
        sensor_item_date = (TextView)findViewById(R.id.sensor_item_date);

        mChart = (LineChart) findViewById(R.id.chart);

        chartView();
        mDataHandler = new DataHandler();
        new GetFarmSensorItemListDataHTTPTask(INSTANCE, mDataHandler).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Config.PARAM_ID + Config.PARAM_EQUALS + id);

    }

    private void chartView() {
        mChart.setDrawGridBackground(false);
        mChart.getDescription().setEnabled(false);
        mChart.setTouchEnabled(false);
        mChart.setDragEnabled(false);
        mChart.setScaleEnabled(false);
        mChart.setPinchZoom(false);

        mChart.setScaleXEnabled(false);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setEnabled(false);
    }



    /*  AsyncTask에 대한 결과값을 받아온다.    */
    private class DataHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Config.MESSAGE_GET_FARM_SENSOR_ITEM_LIST_DATA :
                    if (msg.obj == null) {
                        Toast.makeText(INSTANCE, getResources().getString(R.string.check_network), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String result = msg.obj.toString();
                    Logger.e("!!!", "result = " + result);
                    JSONArray jsona;
                    try {
                        jsona = new JSONArray(result);
                        sensor_item_temp.setText(jsona.getJSONObject(jsona.length()-1).getString("temp")+"℃");
                        sensor_item_move.setText(jsona.getJSONObject(jsona.length()-1).getString("move")+"g");
                        sensor_item_date.setText(jsona.getJSONObject(jsona.length()-1).getString("date"));
                        ArrayList<Entry> valsComp1 = new ArrayList<Entry>();
                        ArrayList<Entry> valsComp2 = new ArrayList<Entry>();

                        for (int i = 0; i < jsona.length(); i++) {
                            JSONObject json = jsona.getJSONObject(i);

                            float move = Float.parseFloat(json.get("move").toString());
                            float temp = Float.parseFloat(json.get("temp").toString());

                            if ( move < 100 && move > -100 ) {
                                try {
                                    SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    Date to = transFormat.parse(json.getString("date").toString());
                                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH");
                                    int time = Integer.parseInt(timeFormat.format(to));
                                    SimpleDateFormat miniFormat = new SimpleDateFormat("mm");
                                    int mini = Integer.parseInt(miniFormat.format(to));

                                    int datetime = time * 60 + mini;

                                    valsComp1.add(new Entry(datetime, move));
                                    valsComp2.add(new Entry(datetime, temp));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        LineDataSet dataSets = new LineDataSet(valsComp1, "운동량");
                        dataSets.setAxisDependency(YAxis.AxisDependency.RIGHT);

                        LineDataSet dataSets2 = new LineDataSet(valsComp2, "온도");
                        dataSets2.setAxisDependency(YAxis.AxisDependency.RIGHT);
                        dataSets2.setColor(Color.RED);
                        dataSets2.setCircleColor(Color.RED);

                        List<ILineDataSet> list = new ArrayList<>();
                        list.add(dataSets);
                        list.add(dataSets2);

                        LineData data = new LineData(list);

                        mChart.setData(data);
                        mChart.invalidate();

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Logger.e("!!!", "excetion e = " + e);
                    }
                    break;

                default:
                    break;
            }
        }
    }
 }
