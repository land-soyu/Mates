package com.matescorp.soyu.farmkinggate.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.matescorp.soyu.farmkinggate.activity.IntroActivity;

/**
 * Created by tbzm on 16. 6. 2.
 */
public class ReStartReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Toast.makeText(context, "Don't panik but your time is up!!!!.", Toast.LENGTH_LONG).show();
        Intent i = new Intent(context, IntroActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }
}
