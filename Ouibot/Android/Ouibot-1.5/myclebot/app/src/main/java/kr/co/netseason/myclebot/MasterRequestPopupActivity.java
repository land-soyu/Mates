package kr.co.netseason.myclebot;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import kr.co.netseason.myclebot.Logger.Logger;
import kr.co.netseason.myclebot.Security.PushWakeLock;
import kr.co.netseason.myclebot.openwebrtc.Config;
import kr.co.netseason.myclebot.openwebrtc.SignalingChannel;

/**
 * Created by tbzm on 15. 9. 29.
 */
public class MasterRequestPopupActivity extends Activity implements View.OnClickListener {
    private Button mShortLeft, mShortOk;
    private String TAG = getClass().getName();
    private TextView mContentString;
    private Messenger mService;
    private Messenger messenger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_popup_layout);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        PushWakeLock.acquire(this, 3000);
        mContentString = (TextView) findViewById(R.id.content_text);
        mContentString.setText(getSenderID(getIntentData()) + " " + getResources().getString(R.string.do_you_allow_master));
        mShortLeft = (Button) findViewById(R.id.short_left);
        mShortLeft.setOnClickListener(this);
        this.setFinishOnTouchOutside(false);
        mShortOk = (Button) findViewById(R.id.short_right);
        mShortOk.setOnClickListener(this);
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
            messenger = new Messenger(new IncomingHandler());
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

    @Override
    public void onBackPressed() {
        notAllowMaster2Sever(getIntentData());
        super.onBackPressed();
    }

    public JSONObject getIntentData() {
        Intent intent = getIntent();
        JSONObject json = new JSONObject();
        try {
            json.put(Config.MASTER_REQUEST_INTENT_MY_ID, intent.getStringExtra(Config.PARAM_TO));
            json.put(Config.MASTER_REQUEST_INTENT_MASTER_ID, intent.getStringExtra(Config.PARAM_FROM));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Logger.d(TAG, "getIntentData json = " + json);
        return json;
    }

    public String getSenderID(JSONObject json) {
        String id = "";
        try {
            id = json.getString(Config.MASTER_REQUEST_INTENT_MASTER_ID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return id;
    }

    public void notAllowMaster2Sever(JSONObject data) {
        try {
            Message msg = Message.obtain(null, Config.NOT_ALLOW_MASTER, data);
            msg.replyTo = messenger;
            mService.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void allowMaster2Sever(JSONObject data) {
        try {
            Message msg = Message.obtain(null, Config.ALLOW_MASTER, data);
            msg.replyTo = messenger;
            mService.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.short_left:
                notAllowMaster2Sever(getIntentData());
                finish();
                break;
            case R.id.short_right:
                allowMaster2Sever(getIntentData());
                finish();
                break;

            default:
                break;
        }
    }

    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Config.ALREAY_MASTER:
                    Toast.makeText(MasterRequestPopupActivity.this, getResources().getString(R.string.already_registered_master), Toast.LENGTH_SHORT).show();
                    break;
                case Config.OVER_MAX_MASTER_COUNT:
                    Toast.makeText(MasterRequestPopupActivity.this, getResources().getString(R.string.over_master_max_count), Toast.LENGTH_SHORT).show();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

}
