package kr.co.netseason.myclebot.Service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import kr.co.netseason.myclebot.Logger.Logger;
import kr.co.netseason.myclebot.openwebrtc.SignalingChannel;

/**
 * Created by Administrator on 2015-07-17.
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.w("!!!", "ACTION_BOOT_COMPLETED ===   service start");
            Intent i = new Intent(context, SignalingChannel.class);
            context.startService(i);
            Log.w("!!!", "ACTION_BOOT_COMPLETED ===   service start end");
        }
        else if (intent.getAction().equals("ACTION.RESTART.PersistentService"))
        {
            Logger.e("!!!", "Service dead, but resurrection");
            Intent i = new Intent(context, SignalingChannel.class);
            context.startService(i);
        }
    }
}