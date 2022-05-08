package kr.co.netseason.myclebot.Service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import kr.co.netseason.myclebot.Logger.Logger;

/**
 * Created by tbzm on 15. 11. 18.
 */
public class MediaButtonIntentReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String intentAction = intent.getAction();
        Logger.d("", "event == 성공"+intentAction);
        if (Intent.ACTION_VOICE_COMMAND.equals(intentAction)) {
            Logger.d("", "event == 성공");
        } else if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
            KeyEvent event = (KeyEvent)
                    intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (event == null) {
                Logger.d("", "event == " + event);
                return;
            }
            int keycode = event.getKeyCode();
            int action = event.getAction();
            long eventtime = event.getEventTime();
            if (action == KeyEvent.ACTION_DOWN) {
                Logger.d("", "event == ACTION_MEDIA_BUTTON" + " keycode = " + keycode + " action = " + action + " eventtime = " + eventtime);
            }
//                    if (mDown) {
//
//                    } else if (event.getRepeatCount() == 0) {
//                        Intent i = new Intent(context, SignalingChannel.class);
//                        i.setAction(SignalingChannel.SERVICECMD);
//                        if (keycode == KeyEvent.KEYCODE_HEADSETHOOK &&
//                                eventtime - mLastClickTime < 300) {
//                            i.putExtra(SignalingChannel.CMDNAME, SignalingChannel.CMDNEXT);
//                            context.startService(i);
//                            mLastClickTime = 0;
//                        } else {
//                            i.putExtra(SignalingChannel.CMDNAME, command);
//                            context.startService(i);
//                            mLastClickTime = eventtime;
//                        }
//
//                        mLaunched = false;
//                        mDown = true;
//                    }
        } else {
//                    mDown = false;
//                }
//                if (isOrderedBroadcast()) {
//                    abortBroadcast();
//                }
//            }
        }
    }
}
