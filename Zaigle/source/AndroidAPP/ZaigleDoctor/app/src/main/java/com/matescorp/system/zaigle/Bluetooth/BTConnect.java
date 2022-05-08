package com.matescorp.system.zaigle.Bluetooth;

import android.app.AlarmManager;

import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanFilter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.ParcelUuid;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class BTConnect extends Service {
    private static final String TAG = "BTConnect";
    private static Context context = null;

    private final static String LBS_UUID_SERVICE = "00001523-1212-efde-1523-785feabcd123";
    private UUID[] uuids = new UUID[1];





    private int getStatePreferences(String key){
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        return pref.getInt(key, 0);
    }
    private String getBTAddressPreferences(String key){
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        return pref.getString(key, null);
    }
    private void savePreferences(String key, int value){
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(key, value);
        editor.commit();
    }
    private void savePreferences(String key, String value){
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, value);
        editor.commit();
    }

    private static int bt_state = 0;
    public static boolean bt_sign = false;
    public static int isBt_search_state() {
        return bt_state;
    }
    public static List<BTDevice> bt_device = new ArrayList<>();

    public static Messenger messenger;
    private Messenger mainMessenger;

    private static BluetoothAdapter mBtAdapter;


    public HashMap<String, BluetoothGatt> mBluetoothGatt = new HashMap<String, BluetoothGatt>();
    public HashMap<String, BluetoothGattServer> mBluetoothGattServer = new HashMap<String, BluetoothGattServer>();
    public static BluetoothManager bluetoothManager = null;
    public int  alertint = 0;

    private static boolean serviceBindFlag = false;
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.e(TAG, "IncomingHandler handleMessage = "+msg.what);
            //

            switch (msg.what) {
                case BTConfig.MAIN_START:
                    context = (Context)msg.obj;
                    mainMessenger = msg.replyTo;
                    serviceBindFlag = true;
                    Log.e(TAG, "bt_device = "+bt_device.size());
                    if ( getStatePreferences("btstate") == 2 && bt_device.size() == 0 ) {
                        scan(true);
                    } else {
                        for ( BTDevice btdevice : bt_device ) {
                            BTConfig.sendMessage(mainMessenger, BTConfig.SP_BT_SERVICE_START, 0, 0, btdevice.getDevice());
                        }
                    }
                    break;
                case BTConfig.SP_BT_SEARCH:


                    Log.e("BTCONNECT", "BTConfig.SP_BT_SEARCH" ) ;
                    Log.e(TAG, " bt_device ===== + " + bt_device);
                    scan(true);
                    bt_state = 1;
                    break;
                case BTConfig.SP_BT_SEARCH_CANCEL:
                    Log.e("BTCONNECT", "BTConfig.SP_BT_SEARCH_CANCEL" ) ;
                    scan(false);
                    bt_state = 0;
                    break;
                case BTConfig.SP_BT_CONNECT:
                    int flag = msg.arg1;
                    Log.e(TAG, "SP_BT_CONNECT - SP_BT_CONNECT flag = "+flag);
                    BluetoothDevice device = (BluetoothDevice)msg.obj;
                    Log.e(TAG, "BTDevice btd ==== "+ device.getName());
                    if ( !searchDevice(device) ) {
                        BTDevice btd = new BTDevice(device, 0, BTDevice.BT_STATE_CONNECTING);
                        Log.e(TAG, "BTDevice btd ==== "+ btd);
                        Log.e(TAG, "BTDevice btd ==== "+ device);

                        bt_device.add(btd);
                    }
                    Bluetooth_Connect(device);

                    break;
                case BTConfig.SP_BT_ACTIVITY_END:
                    serviceBindFlag = false;
                    scan(false);
                    bt_state = 0;
                    break;
                case BTConfig.SP_BT_SEARCH_LINK:
                    scan(true);
                    break;

                default:
                    super.handleMessage(msg);
            }
        }
    }

    private boolean searchDevice(BluetoothDevice device) {
        boolean flag = false;
        for ( BTDevice btDevice : bt_device ) {
            Log.e(TAG, "btDevice = "+ btDevice.getDevice().getName() +", device = "+device.getName());

            if ( btDevice.getDevice().getName().equals(device.getName()) ) {
                flag = true;
            }
        }
        return flag;
    }

    private void Bluetooth_Connect(BluetoothDevice device) {
        final BluetoothDevice conn_device = mBtAdapter.getRemoteDevice(device.getAddress());
        if (conn_device == null) {
            Log.e(TAG, "Device not found.  Unable to connect.");
        } else {
            Log.e(TAG, device.getName()+"  ===== CONNECT : " + device.getBondState() + " =====");

            if(device.getBondState() == device.BOND_BONDING || device.getBondState() == device.BOND_BONDED) {
                removeBond(device);
            }

            Log.e(TAG, "===== Bluetooth_Connect : setupGattServer =====");
            setupGattServer(device.getName());

            Log.e(TAG, "===== Bluetooth_Connect : connectGatt =====");
            mBluetoothGatt.put(device.getName(), device.connectGatt(context, false, mGattCallbacks));
        }
    }


    public boolean reconnect(final String address) {
        Log.e(TAG, "===== PROXIMITY SERVICE - CONNECT : " + address + " =====");
        BluetoothDevice device = mBtAdapter.getRemoteDevice(address);

        if (mBluetoothGatt.get(device.getName()) != null && mBluetoothGattServer.get(device.getName()) != null) {
            Log.e(TAG, "========== Try to Reconnect ==========");
            if(mBluetoothGattServer.get(device.getName()).connect(device, false) == true) {
                Log.e(TAG, "========== Try to Reconnect OK1 ==========");
                if(mBluetoothGatt.get(device.getName()).connect() == true) {
                    Log.e(TAG, "========== Try to Reconnect OK2 ==========");
                    return true;
                } else {
                    Log.e(TAG, "========== Try to Reconnect ERROR1 ==========");
                    return false;
                }
            } else {
                Log.e(TAG, "========== Try to Reconnect ERROR2 ==========");
                return false;
            }
        }

        Log.e(TAG, "========== Try to Reconnect ERROR3 ==========");
        return false;
    }

    boolean remove_bond;
    public void removeBond(BluetoothDevice device) {
        Log.e(TAG, "===== removeBond =====");
        try {
            Method m = device.getClass().getMethod("removeBond", (Class[]) null);
            remove_bond = (Boolean) m.invoke(device, (Object[]) null);
            Log.e(TAG, "===== removeBond : " + remove_bond + " =====");
        } catch (Exception e) {
            Log.e(TAG, "===== removeBond - Exception : " + e.getMessage() + " =====");
        }
    }

    public void scan(boolean start) {

        if(mLeScanCallback == null) {
            Log.e(TAG, "===== scan callback is null =====");
            mBtAdapter.disable();
            return;
        }

        if (start) {
            mBtAdapter.startLeScan(uuids, mLeScanCallback);
        } else {
            mBtAdapter.stopLeScan(mLeScanCallback);
        }
    }
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            if(device == null) {
                Log.e(TAG, "===== scaning - device null =====");
                return;
            }

                if ( serviceBindFlag ) {
                    Log.e(TAG, "+++++++++++++++ scaning - device serviceBindFlag ++++++++++++++");
                    Log.e(TAG, "=++++++++++++++++ scaning - device  +++++++++++++++++++" + device.getName());
                    BTConfig.sendMessage(mainMessenger, BTConfig.SP_BT_SEARCH_DEVICE, rssi, 0, device);
                }

                if ( getStatePreferences("btstate") == 2 ) {
                    Log.e(TAG, "===== scaning - device  getStatePreferences =====");
                    if ( getBTAddressPreferences(device.getName()) != null ) {
                        BTConfig.sendMessage(mainMessenger, BTConfig.SP_BT_SEARCH_LINK_COMPLET, 0, 0, device);
                        scan(false);
                    }
                } else {
                    Log.e(TAG, "===== scaning - device else =====");


                    for ( int i=0;i< bt_device.size();i++ ) {
                        Log.e(TAG, "============================ int i=0;i< bt_device.size();i++=====" );
                        if ( bt_device.get(i).getDevice().getName().equals(device.getName()) ) {
                            if ( device.getAddress().equals(getBTAddressPreferences(device.getName())) && device.getBondState() == bt_device.get(i).getDevice().BOND_BONDED) {
                                Log.e(TAG, "===== scaning - device =====" + device);
                                scan(false);
                                BTConfig.sendMessage(mainMessenger, BTConfig.SP_BT_SEARCH_LINK_COMPLET, 0, 0, device);
                            }
                        }
                    }
                }
        }
    };


    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "onBind");
        messenger = new Messenger(new IncomingHandler());
        return messenger.getBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
//        unregisterRestartAlarm();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void finalize() throws Throwable {
        Log.e(TAG, "BT Connect finalize");
        super.finalize();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "BT Connect onCreate");
        if (bluetoothManager == null) {
            Log.e(TAG, "bluetoothManager is null");
            bluetoothManager = (BluetoothManager) getSystemService(context.BLUETOOTH_SERVICE);
            if (bluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
            }
            mBtAdapter = bluetoothManager.getAdapter();
        }
        if ( mBtAdapter == null ) {
            mBtAdapter = bluetoothManager.getAdapter();
        }

        prepareForScan();
    }
    private void prepareForScan() {
        UUID uuid = UUID.fromString(LBS_UUID_SERVICE);
        uuids[0] = uuid;
    }


    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
//        registerRestartAlarm();
        super.onDestroy();

    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
//        super.onTaskRemoved(rootIntent);
        Log.e(TAG, "onTaskRemoved");
//        registerRestartAlarm();
    }

//    public void registerRestartAlarm() {
//        Log.e(TAG, "registerRestartAlarm");
//        Intent intent = new Intent(this, BootReceiver.class);
//        intent.setAction("ACTION.RESTART.PersistentService");
//        PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, 0);
//        long firstTime = SystemClock.elapsedRealtime();
//        firstTime += 500;
//        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
//        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, 5 * 1000, sender);
//    }
//    public void unregisterRestartAlarm() {
//        Log.e(TAG, "unregisterRestartAlarm");
//        Intent intent = new Intent(this, BootReceiver.class);
//        intent.setAction("ACTION.RESTART.PersistentService");
//        PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, 0);
//        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
//        am.cancel(sender);
//
////        Log.e(TAG, "bt_device = "+bt_device);
////        Log.e(TAG, "bt_state = "+bt_state);
////        Log.e(TAG, "getPreferences(btstate) = "+getStatePreferences("btstate"));
////
////        if ( bt_state != getStatePreferences("btstate") ) {
////            bt_state = getStatePreferences("btstate");
////        }
////        if ( bt_device == null && bt_state == 2 ) {
////            Log.e(TAG, "bt search");
////            scan(true);
////        }
//    }




    public static final byte NO_ALERT = 0;
    public static final UUID ALERT_LEVEL_UUID = UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb");
    public static final UUID IMMEDIATE_ALERT_UUID = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb");
    public static final UUID LINK_LOSS_UUID = UUID.fromString("00001803-0000-1000-8000-00805f9b34fb");
    public static final UUID CCC = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private BluetoothGattService setupGattAdvertisingService() {

        Log.e(TAG, "Service Add");
        byte[] value = { NO_ALERT };

        BluetoothGattCharacteristic alertLevel = new BluetoothGattCharacteristic(
                ALERT_LEVEL_UUID, BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
                BluetoothGattCharacteristic.PERMISSION_WRITE);
        alertLevel.setValue(value);

        BluetoothGattCharacteristic linkalertLevel = new BluetoothGattCharacteristic(
                ALERT_LEVEL_UUID, BluetoothGattCharacteristic.PROPERTY_WRITE
                | BluetoothGattCharacteristic.PROPERTY_READ, BluetoothGattCharacteristic.PERMISSION_WRITE);
        linkalertLevel.setValue(value);

        BluetoothGattService immediateAlert =
                new BluetoothGattService(IMMEDIATE_ALERT_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY);

        BluetoothGattService linkloss =
                new BluetoothGattService(LINK_LOSS_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY);

        immediateAlert.addCharacteristic(alertLevel);
        immediateAlert.addCharacteristic(linkalertLevel);

        return immediateAlert;
    }
    //GATT client callbacks
    private BluetoothGattCallback mGattCallbacks = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            Log.e(TAG, "BluetoothCattCallback - onConnectionStateChange - " + "status : " + status + " new : " + newState);

            BluetoothDevice device = gatt.getDevice();

            if(gatt == null) {
                return;
            }

            switch(newState) {
                case BluetoothProfile.STATE_CONNECTED: {
                    Log.e(TAG, "BluetoothCattCallback - BLE CONNECTED");
                    for ( BTDevice btDevice : bt_device ) {
                        if ( btDevice.getDevice().getName().equals(device.getName()) ) {
                            btDevice.setBT_state(BTDevice.BT_STATE_CONNECT);
                        }
                    }
                    mBluetoothGatt.get(device.getName()).discoverServices();
                }break;

                case BluetoothProfile.STATE_DISCONNECTED: {
                    Log.e(TAG, "BluetoothCattCallback - BLE DISCONNECTED");
                    disconnect(device);

                    Log.e(TAG, "BluetoothCattCallback - serviceBindFlag = "+serviceBindFlag);
                    if ( serviceBindFlag ) {
                        BTConfig.sendMessage(mainMessenger, BTConfig.SP_BT_DEVICE_DISCONNECT, newState, 0, device);
                    } else {
                        for ( int i=0;i< bt_device.size();i++ ) {
                            if ( bt_device.get(i).getDevice().getName().equals(device.getName()) ) {
                                bt_device.remove(i);
                            }
                        }
                    }
                    if ( !reconnect(device.getAddress()) ) {
                        Bluetooth_Connect(device);
                    };

                }break;

                default:
                    return;
            }
        }

        @Override
        public void onCharacteristicChanged (BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.e(TAG, "BluetoothCattCallback - onCharacteristicChanged");
        }

        @Override
        public void onServicesDiscovered (BluetoothGatt gatt, int status) {
            Log.e(TAG, "BluetoothCattCallback - onServicesDiscovered");

            BluetoothDevice device = gatt.getDevice();

            if(status == gatt.GATT_SUCCESS) {
                Log.e(TAG, device.getName()+"  ============================GATT_SUCCESS=====================");

                if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    boolean create_bond = device.createBond();
                    Log.e(TAG, "===== SUCCESS create bond =====");
                }

                if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
//                    boolean pass_key = device.setPairingConfirmation(true);
//                    Log.e(TAG, "===== Write Characteristic - pass_key : " + pass_key + " =====");
                }

                BTConfig.sendMessage(mainMessenger, BTConfig.SP_BT_DEVICE_CONNECT, BTConfig.SP_BT_STATE_CONNECT, 0, device);  // 여기 풀기
                bt_state = 2;
                savePreferences("btstate", bt_state);
//                savePreferences("btaddress", device.getAddress());
                savePreferences(device.getName(), device.getAddress());
            } else {
                Log.e(TAG, "========== GATT_FAIL - " + "status : " + status + "==========");
            }
        }
        @Override
        public void onCharacteristicRead (BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.e(TAG, "BluetoothCattCallback - onCharacteristicRead - " + "status : " + status);
        }

        @Override
        public void onCharacteristicWrite (BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.e(TAG, "BluetoothCattCallback - onCharacteristicWrite - " + "status : " + status);
        }

        @Override
        public void onDescriptorRead (BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.e(TAG, "BluetoothCattCallback - onDescriptorRead - " + "status : " + status);
            BluetoothGattCharacteristic mTxPowerccc = descriptor.getCharacteristic();

            boolean isenabled = enableNotification(true, mTxPowerccc, gatt.getDevice().getName());

        }

        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            Log.e(TAG, "BluetoothCattCallback - onReadRemoteRssi - " + "rssi : " + rssi + " status : " + status);

            BluetoothDevice device = gatt.getDevice();

//            MsgDefine.sendMessageMain(MsgDefine.MID_BLE_RSSI, rssi, 0, device);
        }
    };
    public boolean enableNotification(boolean enable, BluetoothGattCharacteristic characteristic, String devicename) {
        Log.e(TAG, "enableNotification status=" + characteristic);

        if (mBluetoothGatt.get(devicename) == null)
            return false;
        if (!mBluetoothGatt.get(devicename).setCharacteristicNotification(characteristic, enable))
            return false;

        BluetoothGattDescriptor clientConfig = characteristic.getDescriptor(CCC);
        if (clientConfig == null)
            return false;

        if (enable) {
            clientConfig.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        } else {
            clientConfig.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        }
        return mBluetoothGatt.get(devicename).writeDescriptor(clientConfig);
    }

    private void setupGattServer(String devicename) {
        mBluetoothGattServer.put(devicename, bluetoothManager.openGattServer(context, new BluetoothGattServerCallback() {
            @Override
            public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
                Log.e(TAG, device.getName()+" BluetoothGattServerCallback - onConnectionStateChange - " + "status : " + status + " new : " + newState);

                switch(newState) {
                    case BluetoothProfile.STATE_CONNECTED: {
                        Log.e(TAG, "BluetoothGattServerCallback - BLE CONNECTED");
                        mBluetoothGatt.get(device.getName()).discoverServices();
                    }

                    case BluetoothProfile.STATE_DISCONNECTED: {
                        Log.e(TAG, "BluetoothGattServerCallback - BLE DISCONNECTED");
                        //MsgDefine.sendMessageMain(MsgDefine.MID_BLE_TAG_DISCONNECT, newState, 0, device);
                        disconnect(device);
//                        Log.e(TAG, "BluetoothGattServerCallback - serviceBindFlag = "+serviceBindFlag);
//                        if ( serviceBindFlag ) {
//                            BTConfig.sendMessage(mainMessenger, BTConfig.SP_BT_DEVICE_DISCONNECT, newState, 0, device);
//                        } else {
//                            bt_device = null;
//                            bt_device = new ArrayList<BluetoothDevice>();
//                        }
//                        reconnect(device.getAddress());
                    }break;

                    default:{
                    }return;
                }
            }

            @Override
            public void onServiceAdded(int status, BluetoothGattService service) {
                super.onServiceAdded(status, service);
                Log.e(TAG, "BluetoothGattServerCallback - onServiceAdded - " + "status : " + status + " service : " + service);
            }

            @Override
            public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId,
                                                     BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset,
                                                     byte[] value) {
                int alertLevel = 0x000000ff & value[0];

//                MsgDefine.sendMessageMain(MsgDefine.MID_BLE_CHAR_RX, alertLevel, 0, device);

                Log.e("bt_sign ", "alertLevel bt_sign  == " + alertLevel);
//                if ( Config.RE_BOOLEAN == false && alertint == 0 && alertLevel == 144) {
//                    Log.e("bt_sign ", " ( !bt_sign && (alertLevel == 144 || alertLevel == 164)   ");
//                    Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
//                    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//                    Notification.Builder builder = new Notification.Builder(getApplicationContext());
//                    builder.setSmallIcon(R.drawable.ic_launcher);
//                    builder.setWhen(System.currentTimeMillis());
//                    if (OuiBotPreferences.getEmergencyNumber(context) == null) {
//                        builder.setContentTitle("긴급전화번호 입력해주세");
//                    }else {
//                        builder.setContentTitle(device.getName() + ", Message alertlavel = " + alertLevel);
//                    }
//                        builder.setContentText("message");
//                    builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS);
//                    builder.setContentIntent(pendingIntent);
//                    builder.setAutoCancel(true);
//                    builder.setPriority(Notification.PRIORITY_MAX);
//                    NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//                    nm.notify(234567, builder.build());
//
////                    Intent in = new Intent(MainActivity.CONTEXT, SecureActivity.class);
////                    ((MainActivity)context).startActivityForResult(in, 2001);
//
//                    if (OuiBotPreferences.getEmergencyNumber(context) == null) {
//                        return;
//                    } else {
//                        Log.e("bt_sign" , "MAinActivity start");
//
//                        Log.e("bt_sign" , "serviceBindFlag = "+serviceBindFlag);
//                        if ( serviceBindFlag ) {
//                            Intent intent = new Intent();
//                            intent.setAction("bt_event_broad");
//                            sendBroadcast(intent);
//                            Log.e("bt_sign" , "sendBroadcast");
//                        } else {
//                            Intent intent = new Intent(context, MainActivity.class);
//                            intent.addFlags(intent.FLAG_ACTIVITY_NEW_TASK);
//                            intent.putExtra("scurestart", 1);
//                            startActivity(intent);
//                            Log.e("bt_sign" , "startActivity");
//                        }
//
//                    }
//                }
//                if(alertLevel == 144) {
//                    alertint++;
//                    Log.e("bt_sign" , "alertint == " +alertint);
//                    if(alertint == 6){
//                        alertint = 0;
//                    }
//                }


            }
        }));
        if(mBluetoothGattServer != null) {
            BluetoothGattService advertiseService = setupGattAdvertisingService();
            mBluetoothGattServer.get(devicename).addService(advertiseService);
        } else {
            Log.e(TAG, "========== Unable to GattServer Service ==========");
        }
    }



    public void disconnect(BluetoothDevice device) {
        Log.e(TAG, "===== ProximityService.disconnect() - all : " + device.getName() + " =====");

        if(mBluetoothGatt.get(device.getName()) != null) {
            mBluetoothGatt.get(device.getName()).disconnect();
        }
        if(mBluetoothGattServer.get(device.getName()) != null) {
            mBluetoothGattServer.get(device.getName()).cancelConnection(device);
        }
    }



}
