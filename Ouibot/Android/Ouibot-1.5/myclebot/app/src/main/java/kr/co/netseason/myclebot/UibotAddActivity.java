package kr.co.netseason.myclebot;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import kr.co.netseason.myclebot.UTIL.OuiBotPreferences;
import kr.co.netseason.myclebot.openwebrtc.Config;
import kr.co.netseason.myclebot.openwebrtc.SignalingChannel;

/**
 * Created by tbzm on 15. 9. 24.
 */
public class UibotAddActivity extends Activity {
    private final String TAG = getClass().getName();
    public static Activity INSTANCE;
    private EditText edittextid;
    private Messenger mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        INSTANCE = this;
        setContentView(R.layout.activity_uibotadd);
        edittextid = (EditText) findViewById(R.id.edittextid);
        ImageView keypad_back = (ImageView) findViewById(R.id.keypad_back);
        keypad_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Button contentadd_button = (Button) findViewById(R.id.contentadd_button);
        contentadd_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edittextid.getText().toString().trim().equals("") || edittextid.getText().toString().trim().equals(OuiBotPreferences.getLoginId(UibotAddActivity.this))) {
                    Toast.makeText(UibotAddActivity.this, getResources().getString(R.string.input_id), Toast.LENGTH_SHORT).show();
                } else if (Config.isPhoneId(edittextid.getText().toString().trim())) {
                    if (Config.SECURE_TEST_MODE) {
                        requestMaster(edittextid.getText().toString().trim());
                    } else {
                        if(!edittextid.getText().toString().trim().equals(OuiBotPreferences.getLoginId(UibotAddActivity.this))){
                            Toast.makeText(UibotAddActivity.this, getResources().getString(R.string.please_input_this_matster_id_correctly), Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(UibotAddActivity.this, getResources().getString(R.string.you_can_not_save_your_id), Toast.LENGTH_SHORT).show();
                        }
                    }
                }else if(edittextid.getText().toString().length() != 8){
                    Toast.makeText(UibotAddActivity.this, getResources().getString(R.string.please_input_this_matster_id_correctly), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(UibotAddActivity.this, getResources().getString(R.string.wait_for_accept), Toast.LENGTH_SHORT).show();
                    requestMaster(edittextid.getText().toString().trim());
                }
            }
        });
    }

    public void requestMaster(String id) {
        if (mService == null) {
            return;
        }
        try {
            Message msg = Message.obtain(null, Config.REQUEST_MASTER, id);
            mService.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent service = new Intent(this, SignalingChannel.class);
        bindService(service, conn, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = new Messenger(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(conn);
    }


}
