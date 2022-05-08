package kr.co.netseason.myclebot.Service;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

import kr.co.netseason.myclebot.Logger.Logger;

/**
 * Created by tbzm on 15. 11. 18.
 */
public class BTReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        AudioManager audioM = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
            Logger.d("BTReceiver", "BluetoothDevice Connected!");
//            Set<BluetoothDevice> devices = btAdapter.getBondedDevices();
//            if (devices.size() > 0) {
//                audioM.setMode(AudioManager.MODE_IN_CALL);
//                audioM.setBluetoothScoOn(true);
//                audioM.startBluetoothSco();
//                audioM.setSpeakerphoneOn(false);
//            }
        } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
            Logger.d("BTReceiver", "BluetoothDevice Disconnected!");
//            audioM.setMode(AudioManager.MODE_NORMAL);
//            audioM.setBluetoothScoOn(false);
//            audioM.stopBluetoothSco();
//            audioM.setSpeakerphoneOn(true);
        }
    }

}
