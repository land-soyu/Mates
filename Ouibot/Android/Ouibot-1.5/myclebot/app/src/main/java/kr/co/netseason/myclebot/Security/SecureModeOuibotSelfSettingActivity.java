package kr.co.netseason.myclebot.Security;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import org.json.JSONException;
import org.json.JSONObject;

import kr.co.netseason.myclebot.Logger.Logger;
import kr.co.netseason.myclebot.R;
import kr.co.netseason.myclebot.UTIL.OuiBotPreferences;
import kr.co.netseason.myclebot.View.LinkOuibotSelfSettingView;
import kr.co.netseason.myclebot.openwebrtc.Config;
import kr.co.netseason.myclebot.openwebrtc.SignalingChannel;

/**
 * Created by tbzm on 15. 11. 4.
 */
public class SecureModeOuibotSelfSettingActivity extends Activity implements View.OnClickListener {

    private final String TAG = getClass().getName();
    private LinearLayout mLayoutDetect;
    private LinearLayout mLayoutActivity;

    private LinearLayout mBtnLayoutDetect;
    private LinearLayout mBtnLayoutActivity;

    private TextView mTextDetect;
    private TextView mTextActivity;
    private ViewFlipper mViewFlipper;
    private ImageView mBtnBack;
    private final int DETECT_VIEW_INDEX = 0;
    private final int ACTIVITY_VIEW_INDEX = 1;

    private LinearLayout mSaveTimeLayout;
    private OptionRound5SelectView mRound5SeletView;
    private LinearLayout mNoneActivitySaveTimeLayout;
    private OptionRound5SelectView mNoneActivityRound5SeletView;

    private LinearLayout mDetectionSensitivityLayout;
    private OptionRound3SelectView mDetectionSensitivityView;
    private LinearLayout mNoneActivityDetectionSensitivityLayout;
    private OptionRound3SelectView mNoneActivityDetectionSensitivityView;

    private LinearLayout mTimeAfterDetectionSettingLayout;
    private OptionRound5SelectView mTimeAfterDetectionView;
    private LinearLayout mNoneActivityTimeAfterValueLayout;
    private OptionRound5SelectView mNoneActivityTimeAfterValueView;

    private TextView mSeletedTextView;
    private TextView mModeTextView;
    private ImageView mSelectedImageView;
    private TextView mNoneActivitySeletedTextView;
    private TextView mNoneActivityModeTextView;
    private ImageView mNoneActivitySelectedImageView;


    private ImageButton mVideoSwitch;
    private ImageButton mNoneActivityVideoSwitch;

    private ScrollView mScrollView;
    private UibotListData mSeletedItemTotalData;
    private IntentFilter mFilter;
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
        setContentView(R.layout.secure_mode_setting_ouibot_self_activity);
        mSeletedItemTotalData = (UibotListData) getIntent().getExtras().getParcelable(Config.SECURE_LIST_ITEM_INTENT_DATA);
        TextView ouibotId = (TextView) findViewById(R.id.ouibot_id);
        ouibotId.setText(mSeletedItemTotalData.getSlaveRtcid().toString());
        TextView noneActivityOuibotId = (TextView) findViewById(R.id.none_activity_ouibot_id);
        noneActivityOuibotId.setText(mSeletedItemTotalData.getSlaveRtcid().toString());
//        mScrollView = (ScrollView) findViewById(R.id.scroll_view);
        mLayoutDetect = (LinearLayout) findViewById(R.id.detect_layout_id);
        mLayoutActivity = (LinearLayout) findViewById(R.id.activity_layout_id);
        mTextDetect = (TextView) findViewById(R.id.tv_text_detect_mode);
        mTextActivity = (TextView) findViewById(R.id.tv_text_activity_mode);
        mBtnBack = (ImageView) findViewById(R.id.keypad_back);
        mBtnBack.setOnClickListener(this);
        mBtnLayoutDetect = (LinearLayout) findViewById(R.id.btn_layout_detect);
        mBtnLayoutDetect.setOnClickListener(this);
        mBtnLayoutActivity = (LinearLayout) findViewById(R.id.btn_layout_activity);
        mBtnLayoutActivity.setOnClickListener(this);
        mViewFlipper = (ViewFlipper) findViewById(R.id.view_flipper);
        mSeletedTextView = (TextView) findViewById(R.id.selected_textview);
        mSeletedTextView.setOnClickListener(this);
        mModeTextView = (TextView) findViewById(R.id.seleted_mode_text);
        mModeTextView.setOnClickListener(this);
        mSelectedImageView = (ImageView) findViewById(R.id.seleted_imageview);
        mSelectedImageView.setOnClickListener(this);
        mNoneActivitySeletedTextView = (TextView) findViewById(R.id.none_activity_selected_textview);
        mNoneActivitySeletedTextView.setOnClickListener(this);
        mNoneActivityModeTextView = (TextView) findViewById(R.id.none_activity_seleted_mode_text);
        mNoneActivityModeTextView.setOnClickListener(this);
        mNoneActivitySelectedImageView = (ImageView) findViewById(R.id.none_activity_seleted_imageview);
        mNoneActivitySelectedImageView.setOnClickListener(this);
        mVideoSwitch = (ImageButton) findViewById(R.id.video_onoff_switch);
        mVideoSwitch.setOnClickListener(mSecureVideoCheckChange);
        mNoneActivityVideoSwitch = (ImageButton) findViewById(R.id.none_activity_video_onoff_switch);
        mNoneActivityVideoSwitch.setOnClickListener(mNoneActivityVideoCheckChange);
        mFilter = new IntentFilter(Config.INTENT_ACTION_SECURE_OPTION_CHANGED);
        setDetectViewLayout();
        setNoneActivityViewLayout();

        if (mSeletedItemTotalData.getDetectMode() == Config.DETECT_SECURE_MODE) {
            mBtnLayoutDetect.callOnClick();
        } else {
            mBtnLayoutActivity.callOnClick();
        }
        setViewFromData();
    }

    private View.OnClickListener mSecureVideoCheckChange = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mSeletedItemTotalData.getRecordingOption() != Config.VIDEO_SAVE_MODE_ON) {
                mSeletedItemTotalData.setSecureVideoSaveMode(Config.VIDEO_SAVE_MODE_ON);
            } else {
                mSeletedItemTotalData.setSecureVideoSaveMode(Config.VIDEO_SAVE_MODE_OFF);
            }
            setVideoSaveMode(mSeletedItemTotalData.getRecordingOption());
            changeSettingOption();
        }
    };

    private View.OnClickListener mNoneActivityVideoCheckChange = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mSeletedItemTotalData.getNoneActivityRecordingOption() != Config.VIDEO_SAVE_MODE_ON) {
                mSeletedItemTotalData.setNoneActivityVideoSaveMode(Config.VIDEO_SAVE_MODE_ON);
            } else {
                mSeletedItemTotalData.setNoneActivityVideoSaveMode(Config.VIDEO_SAVE_MODE_OFF);
            }
            setNoneActivityVideoSaveMode(mSeletedItemTotalData.getNoneActivityRecordingOption());
            changeSettingOption();
        }
    };

    @Override
    protected void onResume() {
        registerReceiver(mIntentReceiver, mFilter);
        Intent service = new Intent(this, SignalingChannel.class);
        bindService(service, conn, Context.BIND_AUTO_CREATE);
        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(mIntentReceiver);
        unbindService(conn);
        super.onPause();
    }

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(Config.INTENT_ACTION_SECURE_OPTION_CHANGED)) {
                for (int i = 0; i < LinkOuibotSelfSettingView.mSlaveListViewAdapter.getData().size(); i++) {
                    if (LinkOuibotSelfSettingView.mSlaveListViewAdapter.getData().get(i).getSlaveRtcid().equals(mSeletedItemTotalData.getSlaveRtcid())) {
                        mSeletedItemTotalData = LinkOuibotSelfSettingView.mSlaveListViewAdapter.getData().get(i);
                        setViewFromData();
                        return;
                    }
                }

            }
        }
    };

    public void notifyChangeSecureData2Masters() {
//        try {
//            if (LinkSettingView.mMasterListViewAdapter != null) {
//                for (int i = 0; i < LinkSettingView.mMasterListViewAdapter.getData().size(); i++) {
//                    try {
//                        Message msg = Message.obtain(null, Config.SET_CONFIG_ARK_BROADCAST_SELF, LinkSettingView.mMasterListViewAdapter.getData().get(i).getMasterRtcid());
//                        mService.send(msg);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        try {
            if (LinkOuibotSelfSettingView.mMasterListViewAdapter != null) {
                for (int i = 0; i < LinkOuibotSelfSettingView.mMasterListViewAdapter.getData().size(); i++) {
                    try {
                        Message msg = Message.obtain(null, Config.SET_CONFIG_ARK_BROADCAST, LinkOuibotSelfSettingView.mMasterListViewAdapter.getData().get(i).getMasterRtcid());
                        mService.send(msg);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, final IBinder service) {
            mService = new Messenger(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    public void setDetectViewLayout() {
        mSaveTimeLayout = (LinearLayout) findViewById(R.id.video_save_round_layout);
        mRound5SeletView = new OptionRound5SelectView(this, new OptionRound5SelectView.SelectedOnData() {
            @Override
            public void dataSelected(int value) {
                Logger.d(TAG, "video save time" + value);
                mSeletedItemTotalData.setVideoSaveTime(value);
                Logger.d(TAG, "mSeletedItemTotalData.getRecordingTime()" + mSeletedItemTotalData.getRecordingTime());

                changeSettingOption();

            }
        }, Config.SECURE_ROUND_DATA_TYPE_TIME);
        mRound5SeletView.setDefalutValue(Config.VIDEO_SAVE_TIME_VALUE, Config.VIDEO_SAVE_TIME_DEFAULT);
        mSaveTimeLayout.addView(mRound5SeletView);

        mDetectionSensitivityLayout = (LinearLayout) findViewById(R.id.detect_sensitivity_layout);
        mDetectionSensitivityView = new OptionRound3SelectView(this, new OptionRound3SelectView.SelectedOnData() {
            @Override
            public void dataSelected(int value) {
                Logger.d(TAG, "DetectSensitivity" + value);
                mSeletedItemTotalData.setDetectSensitivity(value);
                changeSettingOption();
            }
        }, Config.SECURE_ROUND_DATA_TYPE_STRENGTH);
        mDetectionSensitivityView.setDefalutValue(Config.DETECTION_SENSITIVITY_VALUE, Config.DETECTION_SENSITIVITY_DEFAULT);
        mDetectionSensitivityLayout.addView(mDetectionSensitivityView);

        mTimeAfterDetectionSettingLayout = (LinearLayout) findViewById(R.id.do_after_setting_time);
        mTimeAfterDetectionView = new OptionRound5SelectView(this, new OptionRound5SelectView.SelectedOnData() {
            @Override
            public void dataSelected(int value) {
                Logger.d(TAG, "doTimeAfterSetting " + value);
                mSeletedItemTotalData.setDoTimeAfterSetting(value);
                changeSettingOption();
            }
        }, Config.SECURE_ROUND_DATA_TYPE_TIME);
        mTimeAfterDetectionView.setDefalutValue(Config.DO_AFTER_SETTING_TIME, Config.DO_AFTER_SETTING_TIME_DEFAULT);
        mTimeAfterDetectionView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        mTimeAfterDetectionSettingLayout.addView(mTimeAfterDetectionView);
    }

    public void setNoneActivityViewLayout() {
        mNoneActivitySaveTimeLayout = (LinearLayout) findViewById(R.id.none_activity_video_save_round_layout);
        mNoneActivityRound5SeletView = new OptionRound5SelectView(this, new OptionRound5SelectView.SelectedOnData() {
            @Override
            public void dataSelected(int value) {
                Logger.d(TAG, "NoneActivityVideoSaveTime " + value);
                mSeletedItemTotalData.setNoneActivityVideoSaveTime(value);
                changeSettingOption();

            }
        }, Config.SECURE_ROUND_DATA_TYPE_TIME);
        mNoneActivityRound5SeletView.setDefalutValue(Config.NONE_ACTIVITY_VIDEO_SAVE_TIME_VALUE, Config.NONE_ACTIVITY_VIDEO_SAVE_TIME_DEFAULT);
        mNoneActivitySaveTimeLayout.addView(mNoneActivityRound5SeletView);

        mNoneActivityDetectionSensitivityLayout = (LinearLayout) findViewById(R.id.none_activity_detect_sensitivity_layout);
        mNoneActivityDetectionSensitivityView = new OptionRound3SelectView(this, new OptionRound3SelectView.SelectedOnData() {
            @Override
            public void dataSelected(int value) {
                Logger.d(TAG, "NoneActivityDetectSensitivity " + value);
                mSeletedItemTotalData.setNoneActivityDetectSensitivity(value);
                changeSettingOption();
            }
        }, Config.SECURE_ROUND_DATA_TYPE_STRENGTH);
        mNoneActivityDetectionSensitivityView.setDefalutValue(Config.NONE_ACTIVITY_DETECTION_SENSITIVITY_VALUE, Config.NONE_ACTIVITY_DETECTION_SENSITIVITY_DEFAULT);
        mNoneActivityDetectionSensitivityLayout.addView(mNoneActivityDetectionSensitivityView);

        mNoneActivityTimeAfterValueLayout = (LinearLayout) findViewById(R.id.none_activity_do_after_setting_time);
        mNoneActivityTimeAfterValueView = new OptionRound5SelectView(this, new OptionRound5SelectView.SelectedOnData() {
            @Override
            public void dataSelected(int value) {
                Logger.d(TAG, "NoneActivityDetectTime " + value);
                mSeletedItemTotalData.setNoneActivityDetectTime(value);
                changeSettingOption();
            }
        }, Config.SECURE_ROUND_DATA_TYPE_TIME);
        mNoneActivityTimeAfterValueView.setDefalutValue(Config.NONE_ACTIVITY_SETTING_TIME, Config.NONE_ACTIVITY_SETTING_TIME_DEFAULT);
        mNoneActivityTimeAfterValueView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        mNoneActivityTimeAfterValueLayout.addView(mNoneActivityTimeAfterValueView);
    }

    private void setViewFromData() {
        Logger.i(TAG, "setViewFromData call mSeletedItemTotalData.getRecordingTime() ="+mSeletedItemTotalData.getRecordingTime());
        if (mSeletedItemTotalData != null) {
            setImageChoiceOrNot(mSeletedItemTotalData.getDetectMode());
            setTextChoiceOrNot(mSeletedItemTotalData.getDetectMode());
            setVideoSaveMode(mSeletedItemTotalData.getRecordingOption());
            setVideoSaveTime(mSeletedItemTotalData.getRecordingTime());
            setDetectSensitivity(mSeletedItemTotalData.getDetectSensitivity());
            setDoTimeAfterSetting(mSeletedItemTotalData.getSecuritySettingTime());
            setNoneActivityImageChoiceOrNot(mSeletedItemTotalData.getDetectMode());
            setNoneActivityTextChoiceOrNot(mSeletedItemTotalData.getDetectMode());
            setNoneActivityVideoSaveMode(mSeletedItemTotalData.getNoneActivityRecordingOption());
            setNoneActivityVideoSaveTime(mSeletedItemTotalData.getNoneActivityRecordingTime());
            setNoneActivityDetectSensitivity(mSeletedItemTotalData.getNoneActivitySensitivity());
            setNoneActivityDoTimeSettingValue(mSeletedItemTotalData.getNoneActivityCheckTime());
        }
    }

    public void setNoneActivityDoTimeSettingValue(int doTime) {
        for (int i = 0; i < Config.NONE_ACTIVITY_SETTING_TIME.length; i++) {
            if (doTime == Config.NONE_ACTIVITY_SETTING_TIME[i]) {
                mNoneActivityTimeAfterValueView.seValue(i);
                return;
            }

        }
    }

    public void setNoneActivityDetectSensitivity(int sensitivity) {
        for (int i = 0; i < Config.NONE_ACTIVITY_DETECTION_SENSITIVITY_VALUE.length; i++) {
            if (sensitivity == Config.NONE_ACTIVITY_DETECTION_SENSITIVITY_VALUE[i]) {
                mNoneActivityDetectionSensitivityView.seValue(i);
                return;
            }

        }
    }

    public void setNoneActivityVideoSaveTime(int videoSaveTime) {
        for (int i = 0; i < Config.NONE_ACTIVITY_VIDEO_SAVE_TIME_VALUE.length; i++) {
            if (videoSaveTime == Config.NONE_ACTIVITY_VIDEO_SAVE_TIME_VALUE[i]) {
                mNoneActivityRound5SeletView.seValue(i);
                return;
            }

        }
    }

    public void setNoneActivityVideoSaveMode(int videoSaveMode) {
        if (videoSaveMode == Config.VIDEO_SAVE_MODE_ON) {
            mNoneActivityVideoSwitch.setImageResource(setSwitchImage(true));
        } else {
            mNoneActivityVideoSwitch.setImageResource(setSwitchImage(false));
        }
    }

    public void setNoneActivityTextChoiceOrNot(int value) {
        if (value == Config.DETECT_MOVEMENT_MODE) {
            mNoneActivitySeletedTextView.setText(getResources().getString(R.string.selected));
        } else {
            mNoneActivitySeletedTextView.setText(getResources().getString(R.string.none_selected));

        }
    }

    public void setNoneActivityImageChoiceOrNot(int value) {
        if (value == Config.DETECT_MOVEMENT_MODE) {
            mNoneActivitySelectedImageView.setImageResource(R.drawable.btn_contents_checkbox_checked);
        } else {
            mNoneActivitySelectedImageView.setImageResource(R.drawable.btn_contents_checkbox);

        }
    }

    public void setDoTimeAfterSetting(int doTime) {
        for (int i = 0; i < Config.DO_AFTER_SETTING_TIME.length; i++) {
            if (doTime == Config.DO_AFTER_SETTING_TIME[i]) {
                mTimeAfterDetectionView.seValue(i);
                return;
            }

        }
    }

    public void setDetectSensitivity(int sensitivity) {
        for (int i = 0; i < Config.DETECTION_SENSITIVITY_VALUE.length; i++) {
            if (sensitivity == Config.DETECTION_SENSITIVITY_VALUE[i]) {
                mDetectionSensitivityView.seValue(i);
                return;
            }

        }
    }

    public void setVideoSaveTime(int videoSaveTime) {
        for (int i = 0; i < Config.VIDEO_SAVE_TIME_VALUE.length; i++) {
            Logger.d(TAG,"videoSaveTime = "+videoSaveTime);
            Logger.d(TAG,"Config.VIDEO_SAVE_TIME_VALUE[i] = "+Config.VIDEO_SAVE_TIME_VALUE[i]);
            if (videoSaveTime == Config.VIDEO_SAVE_TIME_VALUE[i]) {
                mRound5SeletView.seValue(i);
                return;
            }

        }
    }

    public void setVideoSaveMode(int videoSaveMode) {
        Logger.d(TAG, "videoSaveMode = " + videoSaveMode);
        Logger.d(TAG, "Config.VIDEO_SAVE_MODE_ON = " + Config.VIDEO_SAVE_MODE_ON);
        if (videoSaveMode == Config.VIDEO_SAVE_MODE_ON) {
            mVideoSwitch.setImageResource(setSwitchImage(true));
        } else {
            mVideoSwitch.setImageResource(setSwitchImage(false));
        }
    }

    public int setSwitchImage(boolean b) {
        if (b) {
            return R.drawable.btn_on;
        } else {
            return R.drawable.btn_off;
        }
    }

    private void setTextChoiceOrNot(int value) {
        if (value == Config.DETECT_SECURE_MODE) {
            mSeletedTextView.setText(getResources().getString(R.string.selected));
        } else {
            mSeletedTextView.setText(getResources().getString(R.string.none_selected));

        }
    }

    private void setImageChoiceOrNot(int value) {
        if (value == Config.DETECT_SECURE_MODE) {
            mSelectedImageView.setImageResource(R.drawable.btn_contents_checkbox_checked);
        } else {
            mSelectedImageView.setImageResource(R.drawable.btn_contents_checkbox);

        }
    }

    public void moveFlipperWithIndex(int index) {
        if (mViewFlipper != null) {
            if (mViewFlipper.getCurrentView() != mViewFlipper.getChildAt(index)) {
                if (mViewFlipper.getChildCount() >= index) {
                    mViewFlipper.setDisplayedChild(index);
//                    mScrollView.setScrollY(0);
                }
            }
        }
    }

    public void saveSharedPrefSettingData(JSONObject json) {
        try {
            JSONObject kobj = json.getJSONObject(Config.PARAM_CONFIG);
            SecurePreference.setDetectMode(kobj.getInt(Config.PARAM_DETECT_MODE));
            SecurePreference.setDetectOnOff(kobj.getString(Config.PARAM_DETECT_ONOFF));
            SecurePreference.setRecordingOption(kobj.getInt(Config.PARAM_RECORDING_OPTION));
            SecurePreference.setRecordingTime(kobj.getInt(Config.PARAM_RECORDING_TIME));
            SecurePreference.setDetectSensitivity(kobj.getInt(Config.PARAM_DETECT_SENSITIVITY));
            SecurePreference.setSucuritySettingTime(kobj.getInt(Config.PARAM_SECURITY_SETTING_TIME));
            SecurePreference.setNoneActivityDetectSensitivity(kobj.getInt(Config.PARAM_NONE_ACTIVITY_SENSITIVITY));
            SecurePreference.setNoneActivityCheckTime(kobj.getInt(Config.PARAM_NONE_ACTIVITY_CHECK_TIME));
            SecurePreference.setNoneActivityRecordingOption(kobj.getInt(Config.PARAM_NONE_ACTIVITY_RECORDING_OPTION));
            SecurePreference.setNoneActivityRecordingTime(kobj.getInt(Config.PARAM_NONE_ACTIVITY_RECORDING_TIME));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void changeSettingOption() {
        saveSharedPrefSettingData(makeJsonObjectWithData());
        notifyChangeSecureData2Masters();
    }

    public JSONObject makeJsonObjectWithData() {
        JSONObject kobj = new JSONObject();
        JSONObject jobj = new JSONObject();
        try {
            kobj.put(Config.PARAM_DETECT_MODE, mSeletedItemTotalData.getDetectMode());
            kobj.put(Config.PARAM_DETECT_ONOFF, mSeletedItemTotalData.getDetectOnOff());
            kobj.put(Config.PARAM_RECORDING_OPTION, mSeletedItemTotalData.getRecordingOption());
            kobj.put(Config.PARAM_RECORDING_TIME, mSeletedItemTotalData.getRecordingTime());
            kobj.put(Config.PARAM_DETECT_SENSITIVITY, mSeletedItemTotalData.getDetectSensitivity());
            kobj.put(Config.PARAM_SECURITY_SETTING_TIME, mSeletedItemTotalData.getSecuritySettingTime());
            kobj.put(Config.PARAM_NONE_ACTIVITY_SENSITIVITY, mSeletedItemTotalData.getNoneActivitySensitivity());
            kobj.put(Config.PARAM_NONE_ACTIVITY_CHECK_TIME, mSeletedItemTotalData.getNoneActivityCheckTime());
            kobj.put(Config.PARAM_NONE_ACTIVITY_RECORDING_OPTION, mSeletedItemTotalData.getNoneActivityRecordingOption());
            kobj.put(Config.PARAM_NONE_ACTIVITY_RECORDING_TIME, mSeletedItemTotalData.getNoneActivityRecordingTime());
            jobj.put(Config.PARAM_TYPE, Config.PARAM_SET_CONFIG);
            jobj.put(Config.PARAM_SESSION_ID, Long.toString(System.currentTimeMillis()));
            jobj.put(Config.PARAM_FROM, OuiBotPreferences.getLoginId(this));
            jobj.put(Config.PARAM_TO, mSeletedItemTotalData.getSlaveRtcid());
            jobj.put(Config.PARAM_CONFIG, kobj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jobj;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.seleted_mode_text:
            case R.id.none_activity_seleted_mode_text:
            case R.id.selected_textview:
            case R.id.none_activity_selected_textview:
            case R.id.seleted_imageview:
            case R.id.none_activity_seleted_imageview:
                if (mSeletedItemTotalData.getDetectMode() == Config.DETECT_SECURE_MODE) {
                    mSeletedItemTotalData.setDetectMode(Config.DETECT_MOVEMENT_MODE);
                } else {
                    mSeletedItemTotalData.setDetectMode(Config.DETECT_SECURE_MODE);
                }
                changeSettingOption();
                setTextChoiceOrNot(mSeletedItemTotalData.getDetectMode());
                setNoneActivityTextChoiceOrNot(mSeletedItemTotalData.getDetectMode());
                setImageChoiceOrNot(mSeletedItemTotalData.getDetectMode());
                setNoneActivityImageChoiceOrNot(mSeletedItemTotalData.getDetectMode());
                break;
            case R.id.keypad_back:
                finish();
                break;
            case R.id.btn_layout_detect:
                setTopMenuBackGroundColor(v);
                moveFlipperWithIndex(DETECT_VIEW_INDEX);
                break;
            case R.id.btn_layout_activity:
                setTopMenuBackGroundColor(v);
                moveFlipperWithIndex(ACTIVITY_VIEW_INDEX);
                break;
        }
    }


    private void setTopMenuBackGroundColor(View v) {
        mTextDetect.setTextColor(getResources().getColor(R.color.top_menu_text_color));
        mLayoutDetect.setBackgroundResource(R.color.top_menu_bg);
        mTextActivity.setTextColor(getResources().getColor(R.color.top_menu_text_color));
        mLayoutActivity.setBackgroundResource(R.color.top_menu_bg);
        switch (v.getId()) {
            case R.id.btn_layout_detect:
                mTextDetect.setTextColor(getResources().getColor(R.color.top_menu_text_color_activity));
                mLayoutDetect.setBackgroundResource(R.color.top_menu_bg_activity);
                break;
            case R.id.btn_layout_activity:
                mTextActivity.setTextColor(getResources().getColor(R.color.top_menu_text_color_activity));
                mLayoutActivity.setBackgroundResource(R.color.top_menu_bg_activity);
                break;
        }
    }
}
