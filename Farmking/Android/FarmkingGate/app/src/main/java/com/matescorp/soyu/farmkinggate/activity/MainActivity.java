package com.matescorp.soyu.farmkinggate.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.HexDump;
import com.matescorp.soyu.farmkinggate.R;
import com.matescorp.soyu.farmkinggate.adapter.FarmkingListViewAdapter;
import com.matescorp.soyu.farmkinggate.adapter.ListViewItem;
import com.matescorp.soyu.farmkinggate.asynctask.CheckFarmSensorDataHTTPTask;
import com.matescorp.soyu.farmkinggate.asynctask.GetFarmSensorListDataHTTPTask;
import com.matescorp.soyu.farmkinggate.asynctask.SetFarmSensorDataHTTPTask;
import com.matescorp.soyu.farmkinggate.receiver.ReStartReceiver;
import com.matescorp.soyu.farmkinggate.util.Config;
import com.matescorp.soyu.farmkinggate.util.GyroData;
import com.matescorp.soyu.farmkinggate.util.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static MainActivity INSTANCE;
    public boolean isRunningReceiver = true;
    private static final String TAG = "MainActivity";
    private DataHandler mDataHandler;

    private UsbManager usbManager;
    private UsbReciever usbReciever;
    private static String ACTION_USB_PERMISSION_MATES = "com.matescorp.soyu.farmking";
    private static String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    private long mBackKeyPressedTime;

    private ScrollView logpanel;
    private TextView logtext;

    private ListView content_list;

    private static ArrayList<ListViewItem> listViewItemList;
    private FarmkingListViewAdapter farmkingAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        INSTANCE = MainActivity.this;
        listViewItemList = new ArrayList<ListViewItem>();

        SharedPreferences pref = getSharedPreferences("farmkinggate", MODE_PRIVATE);
        if ( pref.getBoolean("boot", false) ) {
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("boot", false);
            editor.commit();

            Intent intent = new Intent(this, ReStartReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 0, intent, 0);
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
                    + (5 * 1000), pendingIntent);

            Toast.makeText(this, "5??? ?????? ?????? ?????????.", Toast.LENGTH_LONG).show();
            finish();
        }

        Logger.logFileCreate();

        logpanel = (ScrollView)findViewById(R.id.logpanel);
        logtext = (TextView)findViewById(R.id.logtext);
        Button logbutton = (Button)findViewById(R.id.logbutton);
        logbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( logpanel.getVisibility() == View.GONE ) {
                    logpanel.setVisibility(View.VISIBLE);
                } else {
                    logpanel.setVisibility(View.GONE);
                }
            }
        });

        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        usbReciever = new UsbReciever();
        isRunningReceiver = true;
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(ACTION_USB_PERMISSION_MATES);
        registerReceiver(usbReciever, filter);
        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        HashMap<String, UsbDevice> devlist = usbManager.getDeviceList();
        Iterator<UsbDevice> deviter = devlist.values().iterator();
        Logger.d(TAG, "MainActivity onCreate call = "+deviter.hasNext());
        logtext.append("deviter.hasNext() is "+deviter.hasNext()+"\n");
        while (deviter.hasNext()) {
            UsbDevice d = deviter.next();
            String str = " VID["+d.getVendorId()+"], PID["+d.getProductId()+"]";
            logtext.append(str+" \n");
            Logger.e(TAG, "USB Driver = "+str+", "+d.getInterfaceCount());
            if (d.getVendorId() == 1027 && d.getProductId() == 24597) { // ASN
                usbManager.requestPermission(d, mPermissionIntent);
            }
            if (d.getVendorId() == 1027 && d.getProductId() == 24577) { // LED
                usbManager.requestPermission(d, mPermissionIntent);
            }
            if (d.getVendorId() == 1133 && d.getProductId() == 49958) { // ?????????
            }
            if (d.getVendorId() == 8584 && d.getProductId() == 2785) { // ?????????
            }
            if (d.getVendorId() == 4292 && d.getProductId() == 60000) { // farmking
                usbManager.requestPermission(d, mPermissionIntent);
            }
        }

        content_list = (ListView)findViewById(R.id.content_list);
        farmkingAdapter = new FarmkingListViewAdapter(listViewItemList);
        content_list.setAdapter(farmkingAdapter);

        Button additem = (Button)findViewById(R.id.additem);
        additem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                farmkingAdapter.addItem( new ListViewItem(farmkingAdapter.getCount()+"", "xxxxx", 0, "--???", 0) ); ;
                farmkingAdapter.notifyDataSetChanged() ;
            }
        });


//        DataPreference.PREF = PreferenceManager.getDefaultSharedPreferences(this);
        mDataHandler = new DataHandler();
        new GetFarmSensorListDataHTTPTask(INSTANCE, mDataHandler).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Config.PARAM_GWIDX + Config.PARAM_EQUALS + Config.getSerialNum(this));
    }


    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() > mBackKeyPressedTime + 2000) {
            mBackKeyPressedTime = System.currentTimeMillis();
            Toast.makeText(INSTANCE, getResources().getString(R.string.backbutton_click_is_finish), Toast.LENGTH_SHORT).show();
            return;
        } else {
            super.onBackPressed();
        }
    }

    /*  AsyncTask??? ?????? ???????????? ????????????.    */
    private class DataHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Config.MESSAGE_GET_FARM_SENSOR_LIST_DATA :
                    if (msg.obj == null) {
                        Toast.makeText(INSTANCE, getResources().getString(R.string.check_network), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String result = msg.obj.toString();
                    JSONArray jsona;
                    try {
                        jsona = new JSONArray(result);
                        for (int i = 0; i < jsona.length(); i++) {
                            JSONObject json = jsona.getJSONObject(i);

                            listViewItemList.add(new ListViewItem(json.getString("id"), json.getString("name"), 0, json.getString("temp"), json.getInt("move")));
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                farmkingAdapter.notifyDataSetChanged();
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Logger.e("!!!", "excetion e = " + e);
                    }
                    break;

                case Config.MESSAGE_SET_FARM_SENSOR_DATA :
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            farmkingAdapter.notifyDataSetChanged();
                        }
                    });
                    break;

                case Config.MESSAGE_CHECK_FARM_SENSOR_DATA :
                    if (msg.obj == null) {
                        Toast.makeText(INSTANCE, getResources().getString(R.string.check_network), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    JSONArray jsonaa;
                    try {
                        jsonaa = new JSONArray(msg.obj.toString());
                        for (int i = 0; i < jsonaa.length(); i++) {
                            JSONObject json = jsonaa.getJSONObject(i);

                            for ( int j=0; j< listViewItemList.size(); j++ ) {
                                ListViewItem item = listViewItemList.get(j);
                                if ( json.getString("id").equals(item.getNo()) ) {
                                    item.setHistory_no(json.getString("name"));
                                }
                            }
                            listViewItemList.add(new ListViewItem(json.getString("id"), json.getString("name"), 0, json.getString("temp"), json.getInt("move")));
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                farmkingAdapter.notifyDataSetChanged();
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Logger.e("!!!", "excetion e = " + e);
                    }
                    break;

                default:
                    break;
            }
            if (msg.what == Config.MESSAGE_GET_FARM_SENSOR_LIST_DATA) {

            }
        }
    }












    /*  UsbReciever?????? ???????????? ????????? ???????????????.   */
    public class UsbReciever extends BroadcastReceiver {
        private int venderId = 0;
        private int productId = 0;
        private int finish_count = 0;

        private String log_str = "";
        private static final String TAG = "UsbReciever";
        private int SENSOR_ALIVE_CHECK_TIME_SECOND = 60 * 60 * 1; //1 ???????????? ????????? ??????????????????
        private int mIntervalCount = 1;

        private UsbEndpoint epIN = null;
        private UsbEndpoint epOUT = null;
        private byte[] send_buf = null;
        private byte[] recv_buf = null;

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Logger.e(TAG, "sun onReceive action = " + action);
            log_str = action;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    logtext.append(" onReceive action = "+log_str+"\n");
                }
            });

            if (ACTION_USB_PERMISSION.equals(action)) {

                final List<UsbSerialDriver> drivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager);

                Logger.e(TAG," drivers.size() is "+drivers.size());
                if (drivers.size() < 1) {
                    return;
                }
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    if (device != null) {

                        venderId = device.getVendorId();    //  1027 : donggl, 1659 : panel
                        productId = device.getProductId();  //  24597 : donngl, 8963 : panel

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                logtext.append(" venderId = "+venderId+", productId = "+productId+"\n");
                                Logger.e(TAG," venderId = "+venderId+", productId = "+productId+"\n");
                            }
                        });

                        if (venderId == 4292 && productId == 60000) {
                            Logger.e(TAG, "===============     usb donggl start");

                            final UsbDeviceConnection conn = usbManager.openDevice(device);
                            if (conn == null) {
                                Logger.e(TAG, "sun UsbDeviceConnection is null");
                            } else {
                                Logger.e(TAG, "sun UsbDeviceConnection is not null");

                                epIN = null;

                                UsbInterface usbIf = null;
                                if ( device.getInterfaceCount() < 1) {
                                    Logger.e(TAG, "sun device interface count = " + device.getInterfaceCount());
                                    return;
                                } else {
                                    usbIf = device.getInterface(0);
                                }

                                for (int i = 0; i < usbIf.getEndpointCount(); i++) {
                                    Logger.w(TAG, "sun usbIf.getEndpoint(i).getType() = " + usbIf.getEndpoint(i).getType());
                                    Logger.w(TAG, "sun usbIf.getEndpoint(i).getDirection() = " + usbIf.getEndpoint(i).getDirection());
                                    if (usbIf.getEndpoint(i).getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                                        if (usbIf.getEndpoint(i).getDirection() == UsbConstants.USB_DIR_IN) {
                                            epIN = usbIf.getEndpoint(i);
                                        }
                                    }
                                }

                                UsbSerialDriver usbSerialDriver = null;
                                usbSerialDriver = drivers.get(0);
//                                if (drivers.get(0).getDevice().getVendorId() == 1027) {
//                                    usbSerialDriver = drivers.get(0);
//                                } else {
//                                    usbSerialDriver = drivers.get(1);
//                                }
                                final List<UsbSerialPort> ports = usbSerialDriver.getPorts();
                                try {
                                    ports.get(0).open(conn);
                                    ports.get(0).setParameters(115200, UsbSerialPort.DATABITS_8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
                                } catch (IOException e) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            logtext.append("\n [Error setting baud rate...   please restart] \n");
                                        }
                                    });
                                    Logger.e(TAG, "sun Error setting up device: " + e.getMessage(), e);
                                    try {
                                        ports.get(0).close();
                                    } catch (IOException e2) {
                                        e2.printStackTrace();
                                    }
                                    return;
                                }
                                Logger.w(TAG, "sun Serial device: " + ports.get(0).getClass().getSimpleName());
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        for (; ; ) {
                                            if (!isRunningReceiver) {
                                                return;
                                            }
                                            final long now = System.currentTimeMillis();
                                            Date date = new Date(now);
                                            SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                            final String strNow = sdfNow.format(date);

                                            try {
                                                recv_buf = null;
                                                recv_buf = new byte[1024];
                                                Logger.e(TAG, "bulkTransfer is start");
                                                conn.bulkTransfer(epIN, recv_buf, recv_buf.length, 0);
                                                String receiveData = HexDump.dumpHexString(recv_buf);
                                                Logger.e(TAG, "receiveData: [" + receiveData+"]");
                                                if ( receiveData.equals("00") || receiveData.equals(" 00") ) {
                                                    finish_count++;
                                                    if ( finish_count > 10 ) {
                                                        restart();
                                                        finish_count = 0;
                                                    }
                                                    Logger.e(TAG, "finish_count: " + finish_count);
                                                }
                                                // listViewItemList ????????? ?????? ???????????? ????????? ????????? ??????.
                                                while (receiveData.contains("7C 45 4E 44 00")) {
                                                    String str = receiveData.substring(0, receiveData.indexOf("7C 45 4E 44 00"));
                                                    receiveData = receiveData.substring(receiveData.indexOf("7C 45 4E 44 00")+13, receiveData.length());

                                                    str = unHex(str);
                                                    Log.e(TAG, str);
                                                    log_str = str;
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            logtext.append("\n["+strNow+"] "+log_str+"\n");
                                                        }
                                                    });

                                                    //  str ????????? ????????? ???????????? ??????.
                                                    str = str.substring(str.indexOf(":")+1, str.length());

                                                    String[] strs = str.split("\\|");

                                                    String now_id = strs[1];
                                                    Logger.e(TAG, "========= now_id = " + now_id);
                                                    double temp = 0;
                                                    if ( strs[2].substring(0, 1).equals("+") ) {
                                                        temp = Double.parseDouble(strs[2].substring(1, strs[2].length()));
                                                    } else {
                                                        temp = 0 - Double.parseDouble(strs[2].substring(1, strs[2].length()));
                                                    }
                                                    Logger.e(TAG, "=========  temp = "+temp);


                                                    int move = (int)Math.sqrt(Math.pow(checkingMove(strs[3]), 2) + Math.pow(checkingMove(strs[4]), 2) + Math.pow(checkingMove(strs[5]), 2));
                                                    Logger.e(TAG, "=========  move = "+move);
                                                    move = move / 100;
                                                    Logger.e(TAG, "=========  move = "+move);

                                                    boolean addFlag = true;
                                                    String checkFlag = "-";
                                                    for (int j = 0; j < listViewItemList.size(); j++) {
                                                        ListViewItem data = listViewItemList.get(j);
                                                        String pre_id = data.getNo();
                                                        //?????????????????? ??????????????? ?????????
                                                        if (pre_id != null && pre_id.equals(now_id)) {
                                                            data.setNo(now_id);
                                                            data.setTemp(String.format("%.1f", temp));
                                                            data.setMove(move);
                                                            addFlag  =false;
                                                            checkFlag = data.getHistory_no();
                                                        }
                                                    }
                                                    if ( checkFlag.equals("-") ) {
                                                        String sendmsg = Config.PARAM_GWIDX + Config.PARAM_EQUALS + Config.getSerialNum(INSTANCE);
                                                        sendmsg = sendmsg + Config.PARAM_AND + Config.PARAM_ID + Config.PARAM_EQUALS + now_id;   // ?????? ?????????

                                                        Logger.v(TAG, "sendmsg = " + sendmsg);
                                                        new CheckFarmSensorDataHTTPTask(INSTANCE, mDataHandler).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, sendmsg);
                                                    }

                                                    if ( addFlag ) {
                                                        listViewItemList.add(new ListViewItem(now_id, "-", 0, String.format("%.1f", temp), move));
                                                    }

                                                    String sendmsg = Config.PARAM_GWIDX + Config.PARAM_EQUALS + Config.getSerialNum(INSTANCE);
                                                    sendmsg = sendmsg + Config.PARAM_AND + Config.PARAM_ID + Config.PARAM_EQUALS + now_id;   // ?????? ?????????
                                                    sendmsg = sendmsg + Config.PARAM_AND + Config.PARAM_DATE + Config.PARAM_EQUALS + strNow;
                                                    sendmsg = sendmsg + Config.PARAM_AND + Config.PARAM_TEMP + Config.PARAM_EQUALS + String.format("%.1f", temp); // ??????
                                                    sendmsg = sendmsg + Config.PARAM_AND + Config.PARAM_MOVE + Config.PARAM_EQUALS + move; // ?????????

                                                    Logger.v(TAG, "sendmsg = " + sendmsg);
                                                    new SetFarmSensorDataHTTPTask(INSTANCE, mDataHandler).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, sendmsg);
                                                }
                                            } catch (Exception e) {

                                            }
                                            if (mIntervalCount % SENSOR_ALIVE_CHECK_TIME_SECOND == 0) {
                                                //????????? ?????? ?????? ????????? ??? ??????????????? ????????????.
                                                checkAliveSensorOrNot(now);
                                                //????????? ?????? ?????? ???????????? ????????? ???????????? ????????????.
                                                deleteLocalDataBase(now);
                                                mIntervalCount = 1;
                                            }
                                            mIntervalCount++;

                                            if ( (mIntervalCount % 10) == 0 ) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        if ( logtext.getText().toString().length() > 1000000 ) {
                                                            logtext.setText("* ");
                                                        } else {
                                                            logtext.append("* ");
                                                        }
                                                    }
                                                });
                                            }
                                            try {
                                                Thread.sleep(1000);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                Logger.w(TAG, "e = "+e.getMessage());
                                            }
                                        }
                                    }
                                }).start();
                                Logger.w(TAG, "sun thread start!!!");

                            }
                        }
                    } else {
                        Logger.w(TAG, "device is not connect");
                    }
                } else {
                    Logger.e(TAG, "extra permission granted is false");
                }
            }
        }

    }

    private void restart() {
        Logger.e(TAG, "restart call ");

        Intent intent = new Intent(this, ReStartReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
                + (5 * 1000), pendingIntent);

        Toast.makeText(this, "5??? ?????? ?????? ?????????.", Toast.LENGTH_LONG).show();
        finish();
    }

    private int checkingMove(String str) {
        int move = 0;
        String[] strs = str.split(" ");

        for ( int i = 0; i<strs.length-1; i++) {
            move = move + getDouble(strs[i], strs[i+1]);
        }
        return move;
    }

    private int getDouble(String str, String str1) {
        int a = (int)(100*Double.parseDouble(str.substring(1, str.length())));
        int b = (int)(100*Double.parseDouble(str1.substring(1, str1.length())));

        if ( a > b ) {
            return a -b;
        } else {
            return b-a;
        }
    }

    public static String getStringSize(String str) {
        while (str.length() < 4 ) {
            str = "0"+str;
        }
        return str;
    }
    public static String unHex(String arg) {
        arg = arg.replace(" ", "");
        String str = "";
        for(int i=0;i<arg.length();i+=2)
        {
            String s = arg.substring(i, (i + 2));
            int decimal = Integer.parseInt(s, 16);
            str = str + (char) decimal;
        }
        return str;
    }

    public void checkAliveSensorOrNot(final long currentTime) {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                Date date = new Date(currentTime);
//                SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                final String curTime = sdfNow.format(date);
//                for (int i = 0; i < mFloorInfoDataList.size(); i++) {
//                    ArrayList<ParkingLotInfoData> lotInfoData = mFloorInfoDataList.get(i).getLotDataObject();
//                    for (int j = 0; j < lotInfoData.size(); j++) {
//                        ParkingLotInfoData data = lotInfoData.get(j);
//
//                        if (data.getRegistParkingDate() != null && data.getSensorId() != null && currentTime - time2MilliSecond(data.getRegistParkingDate()) >= (ALIVE_TIME_LIMIT)) {
//                            Logger.d(TAG, "sun " + data.getSensorId() + " error");
//                            insertLocalDataBase(data.getSensorId(), DataPreference.getGwidx(), curTime, curTime, Config.PARKING_STATE_ERROR, 0);
//                            insertServerDataBase(curTime, curTime, data.getSensorId(), Config.PARKING_STATE_ERROR, "0", data.getLotName());
//                            data.setParkingDate(curTime);
//                            data.setRegistParkingDate(curTime);
//                            data.setParkingState(Config.PARKING_STATE_ERROR);
//                            data.setBattery("0");
//                        }
//                    }
//                }
//
//                pagerAdapterDataChangeNotify();
//
//            }
//        }).start();
    }
    public void deleteLocalDataBase(long now) {
//        if (DataPreference.getGwidx() != null) {
//            Logger.d(TAG, "sun deleteLocalDataBaseTest call total database num = " + getTotalDataNum());
//            Logger.d(TAG, "sun deleteLocalDataBaseTest call total database maxIndexNum = " + maxIndexNum);
//            Logger.d(TAG, "sun deleteLocalDataBaseTest call total database minIndexNum = " + minIndexNum);
//
//            if ((maxIndexNum - minIndexNum) > SENSOR_DATA_CHECK_COUNT) {
//                ContentResolver resolver = getContentResolver();
//                resolver.delete(ParkingProvider.PARKING_TABLE_URI, ParkingSQLiteHelper.COL_INEDEX + " <  ? ", new String[]{"" + (maxIndexNum - SENSOR_DATA_CHECK_COUNT)});
//            }
//            Logger.d(TAG, "sun deleteLocalDataBaseTest call total database num after delete= " + getTotalDataNum());
//        } else {
//            Logger.d(TAG, "DataPreference.getGwidx() is null = " + DataPreference.getGwidx());
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRunningReceiver = false;
        unregisterReceiver(usbReciever);
    }
}
