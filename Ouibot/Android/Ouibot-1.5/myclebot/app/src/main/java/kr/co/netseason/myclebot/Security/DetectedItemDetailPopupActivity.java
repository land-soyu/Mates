package kr.co.netseason.myclebot.Security;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;

import kr.co.netseason.myclebot.Logger.Logger;
import kr.co.netseason.myclebot.MainActivity;
import kr.co.netseason.myclebot.R;
import kr.co.netseason.myclebot.openwebrtc.Config;
import kr.co.netseason.myclebot.openwebrtc.SignalingChannel;

/**
 * Created by tbzm on 15. 10. 29.
 */
public class DetectedItemDetailPopupActivity extends Activity {
    private TextView mOuibotId;
    private ImageButton mBackKey;
    private TextView mModeEventText;
    private ImageView mDetailImageView;
    private TextView mDateText;
    private Button mBtnConnect112;
    private Button mBtnConnect119;
    private Button mOpenCctv;
    private Button mOpenApplication;
    private String id;
    private int mode;
    private Messenger mService;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        initUI();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUI();

    }

    public void initUI() {
        setContentView(R.layout.detected_item_detail_popup_activity);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        Intent intent = getIntent();
        String path = intent.getStringExtra("image_path_key");
        id = intent.getStringExtra("image_id_key");
        long eventDate = intent.getLongExtra("image_time_key", 0);
        mode = intent.getIntExtra("image_mode_key", Config.DETECT_SECURE_MODE);


        Logger.d("sunyung", "path = " + path);
        Logger.d("sunyung", "time = " + eventDate);
        Logger.d("sunyung", "id = " + id);
        Logger.d("sunyung", "mode = " + mode);

        mOuibotId = (TextView) findViewById(R.id.ouibot_id_text);
        mOuibotId.setText(getResources().getString(R.string.oui_bot_id) + " " + id);
        mBackKey = (ImageButton) findViewById(R.id.btn_back_key);
        mBackKey.setOnClickListener(mOnCLick);
        mModeEventText = (TextView) findViewById(R.id.event_mode_text);
        mDetailImageView = (ImageView) findViewById(R.id.main_image);
        mDateText = (TextView) findViewById(R.id.date_text);
        mDateText.setText(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(eventDate)));
        mBtnConnect112 = (Button) findViewById(R.id.btn_connect_112);
        mBtnConnect112.setOnClickListener(mOnCLick);
        mBtnConnect119 = (Button) findViewById(R.id.btn_connect_119);
        mBtnConnect119.setOnClickListener(mOnCLick);
        mOpenCctv = (Button) findViewById(R.id.btn_connect_cctv);
        mOpenCctv.setOnClickListener(mOnCLick);
        mOpenApplication = (Button) findViewById(R.id.btn_open_application);
        mOpenApplication.setOnClickListener(mOnCLick);
        if (mode == Config.DETECT_SECURE_MODE) {
            mModeEventText.setText(getResources().getString(R.string.noti_detected_event));
        } else {
            mModeEventText.setText(getResources().getString(R.string.noti_none_activity_event));
        }
        Glide.with(this)
                .load(path)
                .placeholder(R.drawable.no_image)
                .into(mDetailImageView);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String path = intent.getStringExtra("image_path_key");
        id = intent.getStringExtra("image_id_key");
        long eventDate = intent.getLongExtra("image_time_key", 0);
        mode = intent.getIntExtra("image_mode_key", Config.DETECT_SECURE_MODE);

        Logger.d("sunyung", "path = " + path);
        Logger.d("sunyung", "time = " + eventDate);
        Logger.d("sunyung", "id = " + id);
        Logger.d("sunyung", "mode = " + mode);
        if(mOuibotId == null){
            mOuibotId = (TextView) findViewById(R.id.ouibot_id_text);
        }
        mOuibotId.setText(getResources().getString(R.string.oui_bot_id) + " " + id);
        if(mDateText==null) {
            mDateText = (TextView) findViewById(R.id.date_text);
        }
        mDateText.setText(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(eventDate)));

        if (mode == Config.DETECT_SECURE_MODE) {
            mModeEventText.setText(getResources().getString(R.string.noti_detected_event));
        } else {
            mModeEventText.setText(getResources().getString(R.string.noti_none_activity_event));
        }
        Glide.with(this)
                .load(path)
                .placeholder(R.drawable.no_image)
                .into(mDetailImageView);

    }

    View.OnClickListener mOnCLick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.btn_connect_112: {
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:112"));
                    startActivity(intent);
                    break;
                }
                case R.id.btn_connect_119: {
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:119"));
                    startActivity(intent);
                    break;
                }
                case R.id.btn_back_key: {
                    finish();
                    break;
                }
                case R.id.btn_connect_cctv: {
                    onContactCCTVClick(id);
                    finish();
                    break;
                }
                case R.id.btn_open_application: {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.putExtra(Config.INTENT_MOVE_TO_LINK_SETTING_VIEW, true);
                    intent.putExtra(Config.INTENT_MOVE_TO_LINK_SETTING_OUIBOT_ID, id);
                    intent.putExtra(Config.INTENT_MOVE_TO_LINK_SETTING_SECURE_MODE, mode);
                    startActivity(intent);
                    finish();
                    break;
                }
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        Intent service = new Intent(this, SignalingChannel.class);
        bindService(service, conn, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(conn);
    }

    private void onContactCCTVClick(String number) {
        try {
            String peerName = "";
            Message msg = Message.obtain(null, Config.CCTV_ACTIVITY_START, peerName + "|" + number);
            msg.replyTo = mService;
            mService.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = new Messenger(service);

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };
}

