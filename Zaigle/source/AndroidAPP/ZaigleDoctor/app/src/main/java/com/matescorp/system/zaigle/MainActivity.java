package com.matescorp.system.zaigle;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import android.os.ParcelUuid;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.matescorp.system.zaigle.Bluetooth.BTDevice;
import com.matescorp.system.zaigle.adapter.BleDeviceAdapter;
import com.matescorp.system.zaigle.adapter.ExtendedBluetoothDevice;
import com.matescorp.system.zaigle.adapter.MainSQLiteHelper;
import com.matescorp.system.zaigle.adapter.ResultValueAdapter;
import com.matescorp.system.zaigle.data.ProfilePreferences;
import com.matescorp.system.zaigle.data.ValueData;
import com.matescorp.system.zaigle.detailView.BodyFatDetailActivity;
import com.matescorp.system.zaigle.detailView.DetailInfoActivity;
import com.matescorp.system.zaigle.settingView.AboutDeviceActivity;
import com.matescorp.system.zaigle.settingView.GuestInfoActivity;
import com.matescorp.system.zaigle.settingView.LanguageActivity;
import com.matescorp.system.zaigle.settingView.QnAActivity;
import com.matescorp.system.zaigle.settingView.RecentAllDataActivity;
import com.matescorp.system.zaigle.settingView.RecentDataActivity;
import com.matescorp.system.zaigle.settingView.VersionActivity;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private View setting_view;
    private DrawerLayout dl_main;
    public static final String TAG = "MainActivity";
    private ListView listBleDevices = null;
    private ArrayList<ValueData> mListData = new ArrayList<ValueData>();
    private LinearLayout no_data_info;
    private LinearLayout data_info;
    private LinearLayout bt_state;
    private TextView tv_bt_state;
    private boolean yes;
    private String year, name, height, weight, gender, bmi_val, kal_val, water_val, minerals_val, muscle_val;
    private int new_age;

    MainSQLiteHelper helper;
    SQLiteDatabase database;

    Resources res;
    Animation growAnim;

    private Messenger messenger;
    private Messenger mBTService;
    private static List<BTDevice> btDevices = new ArrayList<>();
    private static final int REQUEST_ENABLE_BT = 0;
    private static final long SCAN_PERIOD = 10000; // [ms]

    private BleDeviceAdapter mBleDeviceListAdapter;
    private ResultValueAdapter mResultValueAdapter;
    //    private final static String LBS_UUID_SERVICE = "00001523-1212-efde-1523-785feabcd123";
    private final static String LBS_UUID_SERVICE = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";

    private BluetoothLeScannerCompat mScanner;
    private ArrayList<ScanFilter> scanFilterList;
    private Handler mScannerHandler;

    private boolean mScanning;
    private boolean service_bind = false;

    ProgressBar progressBar;

    private Context context;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private boolean guest_bool = false;
    private boolean insert_value = false;

    private int stepcount = 0;
    private boolean bt_connect_complete = false;
    boolean dubleValue = false;
    private BackPressCloseSystem backPressCloseSystem;
    boolean startbool = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        res = getResources();
        listBleDevices = (ListView) findViewById(R.id.main_list);
        listBleDevices.setAdapter(mBleDeviceListAdapter = new BleDeviceAdapter());
        listBleDevices.setOnItemClickListener(this);

        tv_bt_state = (TextView) findViewById(R.id.tv_bt_state);
        tv_bt_state.setText("????????? ??????????????? ????????????.");

        dl_main = (DrawerLayout) findViewById(R.id.dl_main);
        setting_view = findViewById(R.id.setting_view);
        setting_view.setOnClickListener(null);

        growAnim = AnimationUtils.loadAnimation(this, R.anim.grow);
        no_data_info = (LinearLayout) findViewById(R.id.no_data_info);
        data_info = (LinearLayout) findViewById(R.id.data_info);
        data_info.setVisibility(View.VISIBLE);

        ImageView my_state = (ImageView) findViewById(R.id.my_state);
        my_state.setVisibility(View.VISIBLE);
        progressBar = (ProgressBar) findViewById(R.id.progressBar1);


        helper = new MainSQLiteHelper(MainActivity.this);
        database = helper.getWritableDatabase();

        backPressCloseSystem = new BackPressCloseSystem(this);
        init();
        list();

        my_state.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Profile.class);
                startActivityForResult(intent, 100);
            }
        });

        ImageView ic_action_menu = (ImageView) findViewById(R.id.icon_menu_view);
        ic_action_menu.setImageDrawable(getResources().getDrawable(R.drawable.icon_menu_view));
        ic_action_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dl_main.isDrawerOpen(setting_view)) {
                    dl_main.closeDrawer(setting_view);
                } else {
                    dl_main.openDrawer(setting_view);
                }
            }
        });
        settingInit();

        mScannerHandler = new Handler();
        prepareForScan();

        if (!isBleEnabled()) {
            final Intent bluetoothEnable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(bluetoothEnable, REQUEST_ENABLE_BT);
        } else {
            if(ProfilePreferences.getSearch() == 1 && ProfilePreferences.getBleCon() == 1){
                startLeScan(1);
            }else{
                startLeScan(0);
               // ProfilePreferences.setSearch(0);
            }
        }

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                stopLeScan();
                startLeScan(0);
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

    }

    private void prepareForScan() {
        if (isBleSupported()) {
            final ParcelUuid uuid = ParcelUuid.fromString(LBS_UUID_SERVICE);
            scanFilterList = new ArrayList<>();
            scanFilterList.add(new ScanFilter.Builder().setServiceUuid(uuid).build());
            ScanSettings mScan =  new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_BALANCED).setReportDelay(0)
                    .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES).build();
            mScanner = BluetoothLeScannerCompat.getScanner();
        } else {
            Log.e(TAG, getString(R.string.ble_not_supported));
        }
    }

    private boolean isBleSupported() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    private boolean isBleEnabled() {
        final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        adapter.startDiscovery();
        return adapter != null && adapter.isEnabled();
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    private void startLeScan(int start) {
        if (isBleEnabled()) {
            final ScanSettings settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                    .setReportDelay(1000)
                    .setUseHardwareFilteringIfSupported(false)
                    .build();

            // Clear the devices list
            mBleDeviceListAdapter.clear();
            mBleDeviceListAdapter.notifyDataSetChanged();
            Log.e("sjkim ", " -----------startLeScan---------");
            mScannerHandler.postDelayed(mStopScanningTask, SCAN_PERIOD);
            if(start==0) {
                mScanner.startScan(scanFilterList, settings, scanCallback);
            }else{
                mScanner.startScan(scanFilterList, settings, scanCallback2);
            }
//            mScanner.startScan(scanCallback);
            mScanning = true;
            invalidateOptionsMenu();

            Intent bindIntent = new Intent(this, UartService.class);
            bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

            LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
        } else {
            Toast.makeText(context , " ??????????????? ???????????? " , Toast.LENGTH_SHORT).show();
        }
    }

    private void stopLeScan() {
        mScannerHandler.removeCallbacks(mStopScanningTask);
        mScanning = false;
        mScanner.stopScan(scanCallback);
        mScanner.stopScan(scanCallback2);
        invalidateOptionsMenu();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mScanning)
            stopLeScan();

    }

    private Runnable mStopScanningTask = new Runnable() {
        @Override
        public void run() {
            stopLeScan();
        }
    };

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
        if (mBleDeviceListAdapter == null) {
            if(insert_value == true){
                return;
            }

            if (position == 3) {
                Intent intent = new Intent(context, BodyFatDetailActivity.class);
                intent.putExtra("bmi", bmi_val);
                intent.putExtra("kal", kal_val);
                intent.putExtra("water", water_val);
                intent.putExtra("mine", minerals_val);
                intent.putExtra("mus", muscle_val);
                intent.putExtra("age", new_age);
                ValueData data = mListData.get(position);
                intent.putExtra("d_title", data.getmTitle());
                startActivity(intent);
            } else if (position==0) {
                Log.e(TAG, "mBleDeviceListAdapter == null == " + position);
                ValueData data = mListData.get(position);
                Intent intent = new Intent(context, DetailInfoActivity.class);
                intent.putExtra("d_title", data.getmTitle());
                intent.putExtra("d_value", position);
                Log.e("sjkim ", "position === " + position);
                startActivity(intent);
            }else if (position==1) {
                Log.e(TAG, "mBleDeviceListAdapter == null == " + position);
                ValueData data = mListData.get(position);
                Intent intent = new Intent(context, DetailInfoActivity.class);
                intent.putExtra("d_title", data.getmTitle());
                intent.putExtra("d_value", position);
                Log.e("sjkim ", "position === " + position);
                startActivity(intent);
            }
        } else {
            stopLeScan();

            ExtendedBluetoothDevice device = mBleDeviceListAdapter.getItem(position);
            Log.e(TAG, "device = " + device.getName());

            mUartService.connect(device.getAddress());
            mUartService.setDeviceName(device.getName());

            ProfilePreferences.setDeviceName(device.getName());
            ProfilePreferences.setDeviceAddr(device.getAddress());
            ProfilePreferences.setSearch(1);

            mBleDeviceListAdapter.clear();
            mBleDeviceListAdapter.notifyDataSetChanged();
            tv_bt_state.setText(mUartService.getDeviceName() +" "+ getResources().getString(R.string.connecting_device));
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
        intentFilter.addAction(UartService.ACTION_GATT_ON_DESCRIPTOR_WRITE);
        intentFilter.addAction(UartService.ACTION_LANGUAGE_CHANGE);
        intentFilter.addAction(UartService.BLE_STATE_CHANGE);
        return intentFilter;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (service_bind) {
            unbindService(mServiceConnection);
        }
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
        } catch (Exception ignore) {
            Log.e(TAG, ignore.toString());
        }
        mUartService.stopSelf();
        mUartService = null;
        mServiceConnection = null;

    }


    private void init() {

        TextView main_age = (TextView) findViewById(R.id.main_age);
        TextView main_sex = (TextView) findViewById(R.id.main_sex);
        TextView main_height = (TextView) findViewById(R.id.main_height);
        TextView main_weight = (TextView) findViewById(R.id.main_weight);
        TextView main_name = (TextView) findViewById(R.id.main_name);
        name = ProfilePreferences.getName();
        height = ProfilePreferences.getHeight();
        weight = ProfilePreferences.getWeight();
        gender = ProfilePreferences.getGender();
        year = ProfilePreferences.getBirth();
        int year2 = Integer.parseInt(year);
        String nowyear = getNowTime("yyyy");
        new_age = (Integer.parseInt(nowyear) - year2);


        main_name.setText(ProfilePreferences.getName());
        main_age.setText(String.valueOf(new_age));
        main_height.setText(ProfilePreferences.getHeight());
        main_weight.setText(ProfilePreferences.getWeight());

        if (ProfilePreferences.getGender().equals("0")) {
            main_sex.setText(R.string.man);

        } else {
            main_sex.setText(R.string.woman);
        }


    }

    public static String getNowTime(String formatType) {
        long time = System.currentTimeMillis();
        SimpleDateFormat dayTime = new SimpleDateFormat(formatType);
        String nowTime = dayTime.format(new Date(time));

        return nowTime;

    }

    private void list() {

        mListData.clear();
        List<ValueData> list = new ArrayList<ValueData>();

        list.add(new ValueData(getResources().getDrawable(R.drawable.heart_icon), "?????????", "80", getResources().getDrawable(R.drawable.standard_icon)));
        list.add(new ValueData(getResources().getDrawable(R.drawable.oxygen_icon), "60", "Graph", getResources().getDrawable(R.drawable.standard_icon)));
//        list.add(new ValueData("????????????" ,"92" ,"Graph"));
//        list.add(new ValueData("?????????" ,"25" ,"Graph"));
//        list.add(new ValueData("?????????" ,"25" ,"Graph"));

        for (int i = 0; i < list.size(); i++) {
            mListData.add(list.get(i));
            Log.e(TAG, "Log.e(TAG, mListDat) ===== " + mListData.get(i).toString());

        }
//        helper.insert(database, "?????????",  "90");
//        helper.userInsert(database, "?????????",  1992, 0 , 182, 58, "");
//        helper.workingInsert(database, "2016-09-20",  1992, 623 );
//        helper.checkInsert(database, "?????????",  1992, 0 , 182, "??????" , 12, 14, 534, 32, null);
//        mListView.setAdapter(new ResultValueAdapter(this, mListData));
//
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

    private void settingInit() {

//        LinearLayout life = (LinearLayout)findViewById(R.id.life);
//        life.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this, LifeActivity.class);  //???????????? ????????????
//                startActivity(intent);
//            }
//        });

        LinearLayout qna = (LinearLayout) findViewById(R.id.qna);
        qna.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, QnAActivity.class);
                startActivity(intent);
            }
        });

        LinearLayout guest = (LinearLayout) findViewById(R.id.test_guest);
        guest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mState == UART_PROFILE_ON_DESCRIPTOR_WRITED) {
                    Intent intent = new Intent(MainActivity.this, GuestInfoActivity.class);
                    startActivityForResult(intent, 1001);
                } else {
                    Toast.makeText(context, "???????????? ????????? ???????????????.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        LinearLayout help = (LinearLayout) findViewById(R.id.help);
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AboutDeviceActivity.class);
                startActivity(intent);
            }
        });
        LinearLayout version = (LinearLayout) findViewById(R.id.version);
        version.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, VersionActivity.class);
                startActivity(intent);
            }
        });
        LinearLayout language = (LinearLayout) findViewById(R.id.language);
        language.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, LanguageActivity.class);
                startActivity(intent);
            }
        });

        LinearLayout recent = (LinearLayout) findViewById(R.id.recent);
        recent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, RecentAllDataActivity.class);
                intent.putExtra("age", new_age);
                startActivity(intent);
            }
        });

        LinearLayout start_test = (LinearLayout) findViewById(R.id.start_test);
        start_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                insert_value = false;
                if (progressBar.getVisibility() == View.VISIBLE) {
                    Toast.makeText(context, "??????????????? ????????? ?????? ????????????.", Toast.LENGTH_SHORT).show();
                } else {
                    if (mState == UART_PROFILE_ON_DESCRIPTOR_WRITED) {
                        measureBody(year, height, weight, gender, name);
                    }else {
                        Toast.makeText(context, "???????????? ????????? ???????????????.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 100:
                init();
                if (mState == UART_PROFILE_ON_DESCRIPTOR_WRITED) {
                    Log.e("sjkim", "requestCode 100    ==== " + data);
                    String sendMessage = "";
                    sendMessage = sendMessage + (Integer.parseInt(getNowTime("yyyy")) - Integer.parseInt(ProfilePreferences.getBirth()) + 1) + "|";
                    if (ProfilePreferences.getGender().equals("0")) {
                        sendMessage = sendMessage + "0|";
                    } else {
                        sendMessage = sendMessage + "1|";
                    }
                    sendMessage = sendMessage + ProfilePreferences.getHeight() + "|";
                    sendMessage = sendMessage + ProfilePreferences.getWeight();
                    sendMessage = "ORDER|" + sendMessage;
                    Log.e(TAG, "sendMessage = " + sendMessage);
                    byte[] value;
                    try {
                        //send data to service
                        value = sendMessage.getBytes("UTF-8");
                        mUartService.writeRXCharacteristic(value);
                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                break;
            case 1001:
                Log.e("sjkim", "requestCode 1001    ==== " + data);

                if(resultCode == RESULT_CANCELED){
                    Log.e("sjkim", "=========== RESULT_CANCELED ========" );
                 return;
                }else {
                    if (ProfilePreferences.getG_Height() == null) {
                        return;
                    } else {
                        String n = ProfilePreferences.getG_Name();
                        String w = ProfilePreferences.getG_Height();
                        String b = ProfilePreferences.getG_birth();
                        String h = ProfilePreferences.getG_Weight();
                        String s = ProfilePreferences.getG_Gender();

                        guest_bool = true;
                        insert_value = true;
                        measureBody(b, h, w, s, n);
                    }
                }
                break;
        }

    }


    private void measureBody(String age, String h, String w, String sex, String name) {

        progressBar.setVisibility(View.VISIBLE);
        progressBar.bringToFront();
        String sendMessage = "";

        sendMessage = sendMessage + (Integer.parseInt(getNowTime("yyyy")) - Integer.parseInt(age) + 1) + "|";
        if (sex.equals("0")) {
            sendMessage = sendMessage + "0|";
        } else {
            sendMessage = sendMessage + "1|";
        }
        sendMessage = sendMessage + h + "|";
        sendMessage = sendMessage + w;
        sendMessage = "MYDAT|" + sendMessage;

        Log.e("tst" , "sendMessage == " + sendMessage) ;

        byte[] value;
        try {
            //send data to service
            if (!service_bind) {
                return;
            }
            value = sendMessage.getBytes("UTF-8");
            Log.e("tst" , "value == " + value) ;
            mUartService.writeRXCharacteristic(value);

        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        listBleDevices.setAdapter(null);
    }


    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(final int callbackType, final ScanResult result) {
            Log.e(TAG, "onScanResult = "+result.getDevice().getName());
            // We scan with report delay > 0. This will never be called.
            if ( mScanning ) {
                boolean newDeviceFound = false;
                Log.e(TAG, "onBatchScanResults " + result.getDevice().getName());
                Log.e(TAG, "onBatchScanResults " + result.getScanRecord().getDeviceName());
                if (!mBleDeviceListAdapter.hasDevice(result)) {
                    newDeviceFound = true;
                    mBleDeviceListAdapter.addDevice(new ExtendedBluetoothDevice(result));
                }

                if (newDeviceFound)
                    mBleDeviceListAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onBatchScanResults(final List<ScanResult> results) {
            Log.e(TAG, "onBatchScanResults1111111111111 " + results.size());
            if (mScanning) {
                boolean newDeviceFound = false;
                for (final ScanResult result : results) {
                    Log.e(TAG, "onBatchScanResults " + result.getDevice().getName());
                    Log.e(TAG, "onBatchScanResults " + result.getScanRecord().getDeviceName());
                    String name = result.getScanRecord().getDeviceName();
                    if (name.contains("Zaigle") || name.contains("Nordic")) {
                        if (!mBleDeviceListAdapter.hasDevice(result)) {
                            newDeviceFound = true;
                            mBleDeviceListAdapter.addDevice(new ExtendedBluetoothDevice(result));
                        }
                    }
                }

                if (newDeviceFound)
                    mBleDeviceListAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onScanFailed(final int errorCode) {
            // This should be handled
        }
    };

    private ScanCallback scanCallback2 = new ScanCallback() {

        @Override
        public void onBatchScanResults(final List<ScanResult> results) {
            Log.e(TAG, "onBatchScanResults " + results.size());
            if (mScanning) {
                boolean newDeviceFound = false;
                for (final ScanResult result : results) {
                    Log.e(TAG, "onBatchScanResults " + result.getDevice().getName());
                    Log.e(TAG, "onBatchScanResults " + result.getScanRecord().getDeviceName());
                    String name = result.getScanRecord().getDeviceName();
                    if (name.contains("Zaigle") || name.contains("Nordic")) {
                        if (!mBleDeviceListAdapter.hasDevice(result)) {
                            newDeviceFound = true;
                            mBleDeviceListAdapter.addDevice(new ExtendedBluetoothDevice(result));
                        }
                        Log.e(TAG, "????????? ++++++++ ?");
                        if(ProfilePreferences.getDeviceAddr() != null){
                            Log.e(TAG, "??????????" + ProfilePreferences.getDeviceName());
                            if(result.getDevice().getName().equals(ProfilePreferences.getDeviceName())) {
                                Log.e(TAG, "?????????2?" +  ProfilePreferences.getDeviceAddr());
                                stopLeScan();
                                mUartService.connect(ProfilePreferences.getDeviceAddr());
                                mUartService.setDeviceName(ProfilePreferences.getDeviceName());
                                tv_bt_state.setText(mUartService.getDeviceName() +" "+ getResources().getString(R.string.connecting_device));
                                ProfilePreferences.setBleCon(0);

                            }

                        }
                        mBleDeviceListAdapter.clear();
                        mBleDeviceListAdapter.notifyDataSetChanged();
                    }
                }

                if (newDeviceFound)
                    mBleDeviceListAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onScanFailed(final int errorCode) {
            // This should be handled
        }
    };



    private UartService mUartService = null;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            service_bind = true;
            mUartService = ((UartService.LocalBinder) service).getService();
            Log.d(TAG, "onServiceConnected mService= " + mUartService);
            if (!mUartService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            service_bind = false;
            mUartService = null;
        }
    };
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_ON_DESCRIPTOR_WRITED = 22;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private int mState = UART_PROFILE_DISCONNECTED;
    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            final Intent mIntent = intent;
            //*********************//
            if (action.equals(UartService.ACTION_GATT_CONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.d(TAG, "UART_CONNECT_MSG");
                        mState = UART_PROFILE_CONNECTED;
                        mSwipeRefreshLayout.setEnabled(false);
                    }
                });
            }

            //*********************//
            if (action.equals(UartService.ACTION_GATT_DISCONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.d(TAG, "UART_DISCONNECT_MSG");
                        mState = UART_PROFILE_DISCONNECTED;
                        mUartService.close();
                        mScanner.stopScan(scanCallback);
                        mScanner.stopScan(scanCallback2);

                        bleDisconected();
                        bt_connect_complete = false;
                    }
                });
            }

            //*********************//
            if (action.equals(UartService.ACTION_GATT_ON_DESCRIPTOR_WRITE)) {
                if (mState != UART_PROFILE_ON_DESCRIPTOR_WRITED) {
                    mState = UART_PROFILE_ON_DESCRIPTOR_WRITED;
                    String sendMessage = "";
                    sendMessage = sendMessage + (Integer.parseInt(getNowTime("yyyy")) - Integer.parseInt(ProfilePreferences.getBirth()) + 1) + "|";
                    if (ProfilePreferences.getGender().equals("0")) {
                        sendMessage = sendMessage + "0|";
                    } else {
                        sendMessage = sendMessage + "1|";
                    }
                    sendMessage = sendMessage + ProfilePreferences.getHeight() + "|";
                    sendMessage = sendMessage + ProfilePreferences.getWeight();
                    sendMessage = "ORDER|" + sendMessage;
                    Log.e(TAG, "sendMessage = " + sendMessage);
                    byte[] value;
                    try {
                        //send data to service
                        value = sendMessage.getBytes("UTF-8");
                        mUartService.writeRXCharacteristic(value);
                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    dubleValue = false;

                }
            }

            //*********************//
            if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED)) {
                mUartService.enableTXNotification();
            }
            //*********************//
            if (action.equals(UartService.ACTION_DATA_AVAILABLE)) {

                final byte[] txValue = intent.getByteArrayExtra(UartService.EXTRA_DATA);
                runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            String text = new String(txValue, "UTF-8");
                            String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                            String data = "[" + currentDateTimeString + "] RX: " + text;
                            Log.d(TAG, "ACTION_DATA_AVAILABLE data = " + data);
                            if(data.contains("START")) {
                                Log.d(TAG, " =====START===== " );
                                startbool = true;
                            }
                            viewValue(text);
                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                        }
                    }
                });
                if(startbool == true){
                    progressinit();
                }

            }
            //*********************//
            if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART)) {
                Log.e(TAG, "Device doesn't support UART. Disconnecting");
                mUartService.disconnect();
            }
            //*********************//
            if (action.equals(UartService.ACTION_LANGUAGE_CHANGE)) {
                Log.e(TAG, "ACTION_LANGUAGE_CHANGE lang = " + intent.getIntExtra("lang", 0));
                String lang = intent.getStringExtra("lang");

                Intent ia = new Intent(MainActivity.this, MainActivity.class);
                startActivity(ia);
                finish();
                setLocale(MainActivity.this, lang);
                stopLeScan();

                Toast.makeText(context, "?????????????????? ?????? ????????? ?????????" , Toast.LENGTH_SHORT).show();

            }
            if (action.equals(UartService.BLE_STATE_CHANGE)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.e(TAG, "Bluetooth off");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.e(TAG, "Turning Bluetooth off...");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.e(TAG, "Bluetooth on");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.e(TAG, "Turning Bluetooth on...");
                        break;
                }
            }


        }
    };
    private void progressinit(){
        progressBar.setVisibility(View.VISIBLE);
        progressBar.bringToFront();
    }

    private void viewValue(String data) {
        progressBar.setVisibility(View.GONE);

        if (data.startsWith("00|00|00|00")) {
            if(guest_bool == true) {
                Toast.makeText(context, "????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, GuestInfoActivity.class);
                startActivityForResult(intent, 1001);
                guest_bool = false;
                insert_value = true;

                return;
            }else{
                startbool = false;
                Toast.makeText(context, "????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
                return;
            }

        } else if ( data.startsWith("ORDER|OK") ) {

            if(dubleValue == true){
                return;
            }
            if ( !bt_connect_complete ) {
                String[] order_return = data.split("\\|");

                // order_return[2] <- step count
                stepcount = Integer.parseInt(order_return[2]);

                byte[] value;
                Date date = new Date(System.currentTimeMillis());
                SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy:MM:dd:HH:mm:ss");
                String strNow = sdfNow.format(date);
                Log.e(TAG, "strNow = "+strNow);
                String sendMessage = "TIME|" + strNow;
                Log.e(TAG, "sendMessage time = "+sendMessage);
                try {
                    //send data to service
                    value = sendMessage.getBytes("UTF-8");
                    mUartService.writeRXCharacteristic(value);
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                tv_bt_state.setText(mUartService.getDeviceName() +" "+ getResources().getString(R.string.connect_device));
                bt_connect_complete = true;
            }

            return;
        }



        String[] strs = data.split("\\|");
        /*
        zaigle data
        strs[0] - heart rate
        strs[1] - sp02
        strs[2] - BMI
        strs[3] - ???????????????
        strs[4] - ????????????
        strs[5] - ?????????????
        strs[6] - ??????????
        strs[7] - ????????????
         */
        //*/
        listBleDevices = (ListView) findViewById(R.id.main_list);

        bmi_val = strs[2];
        kal_val = strs[3];
        water_val = strs[4];
        minerals_val = strs[5];
        muscle_val = strs[6];

        int HIGH = 0, STAND = 1, LOW = 2;
        Integer[] imageId = {
                R.drawable.above_standard,
                R.drawable.standard_icon,
                R.drawable.below_standard
        };
        mListData.clear();

        //????????? https://m.blog.naver.com/PostView.nhn?blogId=findyourself&logNo=220039028744&proxyReferer=https%3A%2F%2Fwww.google.co.kr%2F
        int heart = 0;
        int h = Integer.parseInt(strs[0]);
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
        mListData.add(new ValueData(getResources().getDrawable(R.drawable.heart_icon), "?????????", strs[0], getResources().getDrawable(imageId[heart])));

        //  ?????? ?????????
        int oxygen = 0;
        int o = Integer.parseInt(strs[1]);
        if (o == 100) {
            oxygen = HIGH;
        } else if (o < 85) {
            oxygen = LOW;
        } else {
            oxygen = STAND;
        }
        mListData.add(new ValueData(getResources().getDrawable(R.drawable.oxygen_icon), "???????????????", strs[1], getResources().getDrawable(imageId[oxygen])));

        // ????????? ( BMI )
        int bmi = 0;
        int b = Integer.parseInt(strs[2]);
        if (b < 18) {
            bmi = LOW;
        } else if (b > 23) {
            bmi = HIGH;
        } else {
            bmi = STAND;
        }
//        mListData.add(new ValueData(getResources().getDrawable(R.drawable.body_fat_icon),"BMI" ,strs[6] ,  getResources().getDrawable(imageId[bmi])));

        //  ???????????????
        int kcal = 0;
        int k = Integer.parseInt(strs[3]);
        if (gender.equals("0")) {    //  man
            if (new_age < 8) {
                if (k > 1200) {
                    kcal = HIGH;
                } else if (k < 1000) {
                    kcal = LOW;
                } else {
                    kcal = STAND;
                }
            } else if (new_age < 14) {
                if (k > 1400) {
                    kcal = HIGH;
                } else if (k < 1200) {
                    kcal = LOW;
                } else {
                    kcal = STAND;
                }
            } else if (new_age < 18) {
                if (k > 1710) {
                    kcal = HIGH;
                } else if (k < 1500) {
                    kcal = LOW;
                } else {
                    kcal = STAND;
                }
            } else if (new_age < 30) {
                if (k > 2000) {
                    kcal = HIGH;
                } else if (k < 1400) {
                    kcal = LOW;
                } else {
                    kcal = STAND;
                }
            } else if (new_age < 50) {
                if (k > 1800) {
                    kcal = HIGH;
                } else if (k < 1350) {
                    kcal = LOW;
                } else {
                    kcal = STAND;
                }
            } else if (new_age < 60) {
                if (k > 1700) {
                    kcal = HIGH;
                } else if (k < 1200) {
                    kcal = LOW;
                } else {
                    kcal = STAND;
                }
            } else {
                if (k > 1300) {
                    kcal = HIGH;
                } else if (k < 1100) {
                    kcal = LOW;
                } else {
                    kcal = STAND;
                }
            }
        } else {    //  woman
            if (new_age < 8) {
                if (k > 1100) {
                    kcal = HIGH;
                } else if (k < 900) {
                    kcal = LOW;
                } else {
                    kcal = STAND;
                }
            } else if (new_age < 14) {
                if (k > 1280) {
                    kcal = HIGH;
                } else if (k < 1080) {
                    kcal = LOW;
                } else {
                    kcal = STAND;
                }
            } else if (new_age < 18) {
                if (k > 1400) {
                    kcal = HIGH;
                } else if (k < 1200) {
                    kcal = LOW;
                } else {
                    kcal = STAND;
                }
            } else if (new_age < 30) {
                if (k > 1500) {
                    kcal = HIGH;
                } else if (k < 1100) {
                    kcal = LOW;
                } else {
                    kcal = STAND;
                }
            } else if (new_age < 50) {
                if (k > 1450) {
                    kcal = HIGH;
                } else if (k < 1050) {
                    kcal = LOW;
                } else {
                    kcal = STAND;
                }
            } else if (new_age < 60) {
                if (k > 1350) {
                    kcal = HIGH;
                } else if (k < 1000) {
                    kcal = LOW;
                } else {
                    kcal = STAND;
                }
            } else {
                if (k > 1100) {
                    kcal = HIGH;
                } else if (k < 900) {
                    kcal = LOW;
                } else {
                    kcal = STAND;
                }
            }
        }
//        mListData.add(new ValueData(getResources().getDrawable(R.drawable.body_fat_icon),"???????????????" ,strs[7] ,  getResources().getDrawable(imageId[kcal])));

        //  ????????????
        int percent = 0;
        int p = Integer.parseInt(strs[7]);
        if (gender.equals("0")) {    //  man
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
        mListData.add(new ValueData(getResources().getDrawable(R.drawable.stress_icon), "????????????", null, getResources().getDrawable(imageId[percent])));
        mListData.add(new ValueData(getResources().getDrawable(R.drawable.body_fat_icon), "????????????", strs[7], getResources().getDrawable(imageId[percent])));
        mListData.add(new ValueData(getResources().getDrawable(R.drawable.walking_icon), "?????????", stepcount+"", null));



        SimpleDateFormat fm1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String date = fm1.format(new Date());

             /*
        zaigle data
        strs[0] - heart rate
        strs[1] - sp02
        strs[2] - BMI
        strs[3] - ???????????????
        strs[4] - ????????????
        strs[5] - ?????????????
        strs[6] - ??????????
        strs[7] - ????????????
         */


            if(guest_bool != true) {
                helper.checkInsert(database, date, strs[0], strs[1], strs[2], strs[3], null, strs[4], strs[5], strs[6], strs[7]);
            }

//        helper.checkInsert(database, "2018-01-08 12", strs[0], strs[1] , strs[2], strs[3],   null);

        listBleDevices.setAdapter(mResultValueAdapter = new ResultValueAdapter(this, mListData));
        Log.e(TAG, "viewValue");
        mBleDeviceListAdapter = null;
        dubleValue = true;
        guest_bool = false;
        startbool = false;
    }

    private void bleDisconected() {
        mSwipeRefreshLayout.setEnabled(true);

        tv_bt_state.setText("????????? ??????????????? ????????????.");
        listBleDevices.setAdapter(mBleDeviceListAdapter = new BleDeviceAdapter());
        mResultValueAdapter = null;

        if(ProfilePreferences.getSearch() == 1 && ProfilePreferences.getBleCon() == 1){
            startLeScan(1);
        }else{
            startLeScan(0);
            // ProfilePreferences.setSearch(0);
        }
    }

    public static int getResourceId(Context context, Object tag) {
        return context.getResources().getIdentifier((String) tag, "string", context.getPackageName());
    }

    public static void setRefreshViewGroup(Context context, ViewGroup root) throws Exception {
        Log.e("a", "root.getChildCount() == " + root.getChildCount()) ;
        for (int i = 0; i < root.getChildCount(); i++) {
            View child = root.getChildAt(i);

            if (child instanceof TextView) {
                Log.d(TAG, "setRefreshViewGroup" );
                if (child.getTag() != null) {
                    Log.d(TAG, "child.getTag() != null" );
                    if (((TextView) child).getText() != null && ((TextView) child).getText().toString().length() > 0) {
                        int stringId = getResourceId(context, child.getTag());
                        ((TextView) child).setText(stringId);
                        Log.d(TAG, "getTag" );
                        // child).getText());
                    }

                    if (((TextView) child).getHint() != null && ((TextView) child).getHint().toString().length() > 0) {
                        int hintId = getResourceId(context, child.getTag());
                        ((TextView) child).setHint(hintId);
                        Log.d(TAG, "getTag1" );

                        // Log.i(TAG, "getHint:" + ((TextView)
                        // child).getHint());
                    }
                }
            } else if (child instanceof ViewGroup)
                Log.d(TAG, "else if (child instanceof ViewGroup)" );
            setRefreshViewGroup(context, (ViewGroup) child);
        }
    }



    // ?????? ?????? ?????????
    public static void setLocale(final Activity activity, String character) {
        Locale locale = new Locale(character);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        activity.getResources().updateConfiguration(config, activity.getResources().getDisplayMetrics());

        Log.i(TAG, "getLocale : " + Locale.getDefault().toString());
    }


    @Override
    public void onBackPressed() {
        Log.e("a" , "onBackPressed ");
//        super.onBackPressed();
        if (dl_main.isDrawerOpen(setting_view)) {
            dl_main.closeDrawer(setting_view);
            return;
        }

        backPressCloseSystem.onBackPressed();

    }
}
