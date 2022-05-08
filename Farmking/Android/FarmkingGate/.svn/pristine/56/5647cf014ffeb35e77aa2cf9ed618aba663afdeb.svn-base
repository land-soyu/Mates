package com.matescorp.soyu.farmkinggate.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.matescorp.soyu.farmkinggate.service.DataService;

/**
 * Created by tbzm on 16. 6. 2.
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            setBootFlag(context);

            Intent i = new Intent(context, DataService.class);
            context.startService(i);
        }
    }

    private void setBootFlag(Context context){
        SharedPreferences pref = context.getSharedPreferences("farmkinggate", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("boot", true);
        editor.commit();
    }
}
