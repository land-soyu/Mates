package com.matescorp.system.zaigle;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.matescorp.system.zaigle.Bluetooth.BTConfig;
import com.matescorp.system.zaigle.Bluetooth.BTConnect;
import com.matescorp.system.zaigle.Bluetooth.BTDevice;
import com.matescorp.system.zaigle.Bluetooth.DeviceMenuAdapter;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by sjkim on 17. 11. 27.
 */

public class BTCon extends Activity{
    public Context context;
    public Messenger mBTService;
    private Messenger messenger;
    private static final String TAG = "BTCon";
    private static List<BTDevice> btDevices = new ArrayList<>();

    private int getStatePreferences(String key){
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        return pref.getInt(key, 0);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bt_test);
        context = this;
        Intent btservice = new Intent(this, BTConnect.class);
        bindService(btservice, btconn, Context.BIND_AUTO_CREATE);
        init();

        if (!Config.isBTServiceAlive(this)) {
            bindService(btservice, btconn, Context.BIND_AUTO_CREATE);
            startService(btservice);
            Log.e("!!!", "btconn error2");

        } else {
            bindService(btservice, btconn, Context.BIND_AUTO_CREATE);
            Log.e("!!!", "btconn error3");
        }



    }

    public void init(){
        Button scanBT = (Button)findViewById(R.id.scanBT);
        switch (BTConnect.isBt_search_state() ) {
            case 0:
                scanBT.setText("검색하기");
                break;
            case 1:
                scanBT.setText("검색중(취소)");
                break;
            case 2:
                scanBT.setVisibility(View.GONE);
                break;
        }
            scanBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button bt = (Button)v;
                String btnName = (bt).getText().toString();
                if( btnName.equals("검색중(취소)")) {
                    Log.e(TAG, "btnName.equals(검색중(취서ㅗ)") ;
                    UpdateScanButton(bt, true);
                }else if( btnName.equals("검색하기")) {
                    Log.e(TAG, "btnName.equals(검색하기))") ;
                    UpdateScanButton(bt, false);
                }
            }
        });




        messenger = new Messenger(new IncomingHandler());
        viDeviceList = (ListView) this.findViewById(R.id.scanDevices);
        scanAdapter = new DeviceMenuAdapter(context, btDevices, messenger);
    }


    ServiceConnection btconn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e(TAG, "BT ServiceConnection onServiceConnected name = " + name.toString());
            mBTService = new Messenger(service);

            try {
                Message msg_main_start = Message.obtain(null, BTConfig.MAIN_START,"");
                msg_main_start.replyTo = messenger;
                msg_main_start.obj = context;
                mBTService.send(msg_main_start);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Log.e(TAG, "bt_state = " + BTConnect.isBt_search_state());
            Log.e(TAG, "getPreferences(btstate) = " + getStatePreferences("btstate"));

            if (BTConnect.bt_device == null && getStatePreferences("btstate") == 2) {
                Log.e(TAG, "bt search");
//                ShowWaitDialog("Connect BT Searching......");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(TAG, "BT ServiceConnection onServiceDisconnected name = "+name.toString());
            mBTService = null;
        }
    };




    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("!!!!!", "MainActivity onDestroy");

        Button scanBT = (Button)findViewById(R.id.scanBT);
        scanBT.setText("검색하기");
        BTConfig.sendMessage(mBTService, BTConfig.SP_BT_ACTIVITY_END, 0, 0, null);

        unbindService(btconn);
        btDevices.clear();
    }

    public void UpdateScanButton(Button bt, boolean bScanning) {
        if( bScanning) {
            bt.setText("검색하기");
            try {
                Message msg_bt_search = Message.obtain(null, BTConfig.SP_BT_SEARCH_CANCEL, null);
                mBTService.send(msg_bt_search);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            bt.setText("검색중(취소)");
            try {
                Message msg_bt_search = Message.obtain(null, BTConfig.SP_BT_SEARCH, null);
                mBTService.send(msg_bt_search);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    ListView viDeviceList = null;
    DeviceMenuAdapter scanAdapter = null;
    /** 서비스로 부터 message를 받음 **/
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            BluetoothDevice device;
            Log.e(TAG, "+++++++++++++ msg.what +++++++++++ " + msg.what);
            switch (msg.what) {


                case BTConfig.SP_BT_SERVICE_START:
                    device = (BluetoothDevice)msg.obj;

                    if(device == null) {
                        Log.e(TAG, "===== SP_BT_SERVICE_START - Device is null =====");
                    } else {
                        Log.e(TAG, "===== SP_BT_SERVICE_START - Device is "+device.getName()+" =====");

                        if ( btDevices.size() == 0 ) {
                            BTDevice btd = new BTDevice(device, -1, 0);
                            btDevices.add(btd);
                        } else {
                            for ( int i=0;i<btDevices.size();i++ ) {
                                if ( device.getAddress().equals(btDevices.get(i).getDevice().getName()) ) {
                                    btDevices.get(i).setRssi(-1);
                                    btDevices.get(i).setBT_state(BTDevice.BT_STATE_CONNECT);
                                }
                            }
                        }
                        UpdateDeviceList();
                    }

                    break;
                case BTConfig.SP_BT_SEARCH_DEVICE:
                    int rssi = msg.arg1;
                    device = (BluetoothDevice)msg.obj;
                    Log.e(TAG, "++++++++++++ BTConfig.SP_BT_SEARCH_DEVICE ++++++++++++++" + device);
                    if(device == null) {
                        Log.e(TAG, "===== MID_BLE_SCAN_DEVICE - Device is null =====");
                        return;
                    }

                    if ( btDevices.size() == 0 ) {
                        Log.e(TAG , "btDevices.size() == 0");
                        Log.e(TAG , "device == " +device);
                        Log.e(TAG , "rssi == " + rssi);
                        BTDevice btd = new BTDevice(device, rssi, 0);
                        btDevices.add(btd);
                        Log.e(TAG, "++++++++++++ btDevices.add(btd)+++++++++++++ " + btDevices.size());
                    } else {
                        boolean eflag = false;
                        for ( int i=0;i<btDevices.size();i++ ) {
                            if ( device.getName().equals(btDevices.get(i).getDevice().getName()) ) {
                                btDevices.get(i).setRssi(rssi);
//                                btDevices.get(i).setBT_state(BTDevice.BT_STATE_CONNECT);
                                eflag = true;
                            }
                        }
                        if ( !eflag ) {
                            BTDevice btd = new BTDevice(device, rssi, 0);
                            btDevices.add(btd);
                        }
                    }
                    UpdateDeviceList();
                    break;
                case BTConfig.SP_BT_CONNECT:
                    Log.e(TAG, "SP_BT_CONNECT - SP_BT_CONNECT");
                    int flag = msg.arg1;
                    device = (BluetoothDevice)msg.obj;
                    BTConfig.sendMessage(mBTService, BTConfig.SP_BT_CONNECT, flag, 0, device);

                    UpdateScanButton((Button)findViewById(R.id.scanBT), true);
                    break;
                case BTConfig.SP_BT_DEVICE_CONNECT:
                    HideWaitDialog();
                    device = (BluetoothDevice)msg.obj;

                    Log.e(TAG, "SP_BT_DEVICE_CONNECT - SP_BT_DEVICE_CONNECT = "+device.getName());
                    for ( BTDevice bt : btDevices ) {
                        if ( bt.getDevice().getAddress().equals(device.getAddress()) ) {
                            bt.setBT_state(BTDevice.BT_STATE_CONNECT);
                        }
                    }
                    for ( BTDevice bt : btDevices ) {
                        Log.e(TAG, "bt state = "+bt.getBT_state()+", bt name= "+bt.getDevice().getName());
                    }

                    Log.e(TAG , "sjkim btDevices  ==== " + btDevices.size());

                    scanAdapter.setListItems(btDevices);
                    viDeviceList.setAdapter(scanAdapter);
                    break;
                case BTConfig.SP_BT_DEVICE_DISCONNECT:
                    device = (BluetoothDevice)msg.obj;
                    Log.e(TAG, "SP_BT_DEVICE_DISCONNECT - SP_BT_DEVICE_DISCONNECT device.getName() = "+device.getName());
//                    BTConfig.sendMessage(mBTService, BTConfig.SP_BT_CONNECT, msg.arg1, 0, device);
                    break;

                case BTConfig.SP_BT_SEARCH_LINK_COMPLET:
                    device = (BluetoothDevice)msg.obj;
                    BTConfig.sendMessage(mBTService, BTConfig.SP_BT_CONNECT, 2, 0, device);

                    break;

                default:
                    super.handleMessage(msg);
            }
        }
    }

    boolean reloadFlag = false;
    public void UpdateDeviceList() {
        if(reloadFlag) return;

        reloadFlag = true;
        Log.e(TAG, "++++++++++++ btDevices.add(btd)+++++++++++++ " + btDevices.get(0).toString());

        scanAdapter.setListItems(btDevices);
        viDeviceList.setAdapter(scanAdapter);
        reloadFlag = false;
    }
    ProgressDialog waitDialog = null;

    private void HideWaitDialog() {
        if( waitDialog != null) {
            waitDialog.dismiss();
            waitDialog = null;
        }
    }
}
