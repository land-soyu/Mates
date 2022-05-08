package com.matescorp.system.zaigle;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

/**
 * Created by sjkim on 17. 11. 27.
 */

public class Config {

    public static boolean isBTServiceAlive(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.matescorp.system.zaigle.Bluetooth.BTConnect".equals(service.service.getClassName())) {
                Log.e("!!!", "isBTServiceAlive true");
                return true;
            }
        }
        return false;
    }
}
