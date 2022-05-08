package com.matescorp.soyu.farmkinggate.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

import com.matescorp.soyu.farmkinggate.activity.IntroActivity;
import com.matescorp.soyu.farmkinggate.util.Logger;


/**
 * Created by tbzm on 16. 4. 18.
 */
public class DataService extends Service {

    public static final String INTENT_ACTION = "intent.action.bhc.test.service";
    private static final int MSG_WORK = 1;

    final RemoteCallbackList<IDataServiceCallback> callbacks = new RemoteCallbackList();

    private final IDataService.Stub mBinder = new IDataService.Stub() {

        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public boolean unregisterCallback(IDataServiceCallback callback)
                throws RemoteException {
            boolean flag = false;

            if (callback != null) {
                flag = unregisterCallback(callback);
            }

            return flag;
        }

        @Override
        public boolean registerCallback(IDataServiceCallback callback)
                throws RemoteException {


            boolean flag = false;

            if (callback != null) {
                flag = callbacks.register(callback);
            }

            return flag;
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;

    }


    @Override
    public void onCreate() {
        Logger.d("Service", "sun Service is onCreate..");
        Intent intent = new Intent(this, IntroActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        handler.sendEmptyMessage(MSG_WORK);

        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Logger.d("Service", "sun Service is onDestory..");

        handler.removeMessages(MSG_WORK);

        super.onDestroy();
    }

    private Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {

            switch (msg.what) {
                case MSG_WORK:
                    int N = callbacks.beginBroadcast();
                    for (int i = 0; i < N; i++) {
                        try {
                            IDataServiceCallback cb = callbacks.getBroadcastItem(i);
                            cb.valueChanged(System.currentTimeMillis());
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    Logger.d("BHC_TEST", "Handler work.. :: callbacks clients count is " + N);
                    callbacks.finishBroadcast();
                    break;

                default:
                    break;
            }
            return false;
        }
    });

}
