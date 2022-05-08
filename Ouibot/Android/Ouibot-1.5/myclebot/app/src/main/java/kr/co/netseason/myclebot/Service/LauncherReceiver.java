package kr.co.netseason.myclebot.Service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Messenger;

import kr.co.netseason.myclebot.openwebrtc.CallActivity;

/**
 * Created by sjkim on 15. 10. 13.
 */
public class LauncherReceiver extends BroadcastReceiver {
    private Messenger mService;

    private String number;
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action.equals("kr.co.netseason.MYACTION")){
            number = intent.getExtras().getString("DATA");

//            mService = new Messenger(peekService(context, new Intent(context, SignalingChannel.class)));
//            Message msg_main_start = Message.obtain(null, Config.CALL_ACTIVITY_START, number);
//            try {
//                mService.send(msg_main_start);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }

            Intent i = new Intent(context, CallActivity.class);
            i.putExtra("callSendNumber", number);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);

        }
    }

}
