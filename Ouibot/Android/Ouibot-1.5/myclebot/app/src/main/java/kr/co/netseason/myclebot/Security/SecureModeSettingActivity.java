package kr.co.netseason.myclebot.Security;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import org.json.JSONException;
import org.json.JSONObject;

import kr.co.netseason.myclebot.Logger.Logger;
import kr.co.netseason.myclebot.MainActivity;
import kr.co.netseason.myclebot.Provider.SecureProvider;
import kr.co.netseason.myclebot.Provider.SecureSQLiteHelper;
import kr.co.netseason.myclebot.R;
import kr.co.netseason.myclebot.UTIL.OuiBotPreferences;
import kr.co.netseason.myclebot.View.LinkSettingView;
import kr.co.netseason.myclebot.openwebrtc.Config;

/**
 * Created by tbzm on 15. 10. 8.
 */
public class SecureModeSettingActivity extends Activity implements View.OnClickListener {

    private final String TAG = getClass().getName();
    private static Context mContext;
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

    private LinearLayout mRemoveMaster;
    private LinearLayout mNoneActivityRemoveMaster;

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
    private CustomDialog mDialog;
    private TextView mSecureSave;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        initUI();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = SecureModeSettingActivity.this;
        initUI();

    }

    @Override
    public void finish() {
        super.finish();
    }

    public void initUI() {
        setContentView(R.layout.secure_mode_setting_activity);
        mSeletedItemTotalData = (UibotListData) getIntent().getExtras().getParcelable(Config.SECURE_LIST_ITEM_INTENT_DATA);
        TextView ouibotId = (TextView) findViewById(R.id.ouibot_id);
        ouibotId.setText(mSeletedItemTotalData.getSlaveRtcid().toString());
        TextView noneActivityOuibotId = (TextView) findViewById(R.id.none_activity_ouibot_id);
        noneActivityOuibotId.setText(mSeletedItemTotalData.getSlaveRtcid().toString());
        mScrollView = (ScrollView) findViewById(R.id.scroll_view);
        mSecureSave = (TextView) findViewById(R.id.secure_save);
        mSecureSave.setOnClickListener(this);
        mLayoutDetect = (LinearLayout) findViewById(R.id.detect_layout_id);
        mLayoutActivity = (LinearLayout) findViewById(R.id.activity_layout_id);
        mTextDetect = (TextView) findViewById(R.id.tv_text_detect_mode);
        mTextActivity = (TextView) findViewById(R.id.tv_text_activity_mode);
        mBtnBack = (ImageView) findViewById(R.id.keypad_back);
        mBtnBack.setOnClickListener(this);
        mRemoveMaster = (LinearLayout) findViewById(R.id.uibot_disconnect);
        mRemoveMaster.setOnClickListener(this);
        mNoneActivityRemoveMaster = (LinearLayout) findViewById(R.id.none_activity_uibot_disconnect);
        mNoneActivityRemoveMaster.setOnClickListener(this);
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
//            changeSettingOption();
            setViewFromData();
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
//            changeSettingOption();
            setViewFromData();
        }
    };

    @Override
    protected void onResume() {
        registerReceiver(mIntentReceiver, mFilter);
        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(mIntentReceiver);
        super.onPause();
    }

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(Config.INTENT_ACTION_SECURE_OPTION_CHANGED)) {
                for (int i = 0; i < LinkSettingView.mSlaveListViewAdapter.getData().size(); i++) {
                    if (LinkSettingView.mSlaveListViewAdapter.getData().get(i).getSlaveRtcid().equals(mSeletedItemTotalData.getSlaveRtcid())) {
                        mSeletedItemTotalData = LinkSettingView.mSlaveListViewAdapter.getData().get(i);
                        setViewFromData();
                        return;
                    }
                }

            }
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
//                changeSettingOption();

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
//                changeSettingOption();
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
//                changeSettingOption();
            }
        }, Config.SECURE_ROUND_DATA_TYPE_TIME);
        mTimeAfterDetectionView.setDefalutValue(Config.DO_AFTER_SETTING_TIME, Config.DO_AFTER_SETTING_TIME_DEFAULT);
        mTimeAfterDetectionSettingLayout.addView(mTimeAfterDetectionView);
    }

    public void setNoneActivityViewLayout() {
        mNoneActivitySaveTimeLayout = (LinearLayout) findViewById(R.id.none_activity_video_save_round_layout);
        mNoneActivityRound5SeletView = new OptionRound5SelectView(this, new OptionRound5SelectView.SelectedOnData() {
            @Override
            public void dataSelected(int value) {
                Logger.d(TAG, "NoneActivityVideoSaveTime " + value);
                mSeletedItemTotalData.setNoneActivityVideoSaveTime(value);
//                changeSettingOption();

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
//                changeSettingOption();
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
//                changeSettingOption();
            }
        }, Config.SECURE_ROUND_DATA_TYPE_TIME);
        if (Config.NONE_ACTIVITY_TEST_MODE) {
            mNoneActivityTimeAfterValueView.setDefalutValue(Config.NONE_ACTIVITY_SETTING_TIME_TEST, Config.NONE_ACTIVITY_SETTING_TIME_DEFAULT);
        } else {
            mNoneActivityTimeAfterValueView.setDefalutValue(Config.NONE_ACTIVITY_SETTING_TIME, Config.NONE_ACTIVITY_SETTING_TIME_DEFAULT);
        }
        mNoneActivityTimeAfterValueLayout.addView(mNoneActivityTimeAfterValueView);
    }

    private void setViewFromData() {
        Logger.i(TAG, "setViewFromData call");
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
        if (Config.NONE_ACTIVITY_TEST_MODE) {
            for (int i = 0; i < Config.NONE_ACTIVITY_SETTING_TIME_TEST.length; i++) {
                if (doTime == Config.NONE_ACTIVITY_SETTING_TIME_TEST[i]) {
                    mNoneActivityTimeAfterValueView.seValue(i);
                    return;
                }

            }
        } else {
            for (int i = 0; i < Config.NONE_ACTIVITY_SETTING_TIME.length; i++) {
                if (doTime == Config.NONE_ACTIVITY_SETTING_TIME[i]) {
                    mNoneActivityTimeAfterValueView.seValue(i);
                    return;
                }

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
                    mScrollView.setScrollY(0);
                }
            }
        }
    }

    public void changeSettingOption() {
//        final ProgressDialog dialog = new ProgressDialog(SecureModeSettingActivity.this);
//        dialog.setCancelable(false);
//        dialog.setMessage(getResources().getString(R.string.please_wait));
//        dialog.show();
//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                dialog.dismiss();
//            }
//        }, 2000);
        //프로세서 킬 이후 아래 컨텍스트 값이 날라감
        if(MainActivity.CONTEXT != null) {
            MainActivity.CONTEXT.requestConfigChange(makeJsonObjectWithData());

        }else{
            Logger.d(TAG,"error MainActivity.CONTEXT == null");
        }
        finish();
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
//                changeSettingOption();
                setViewFromData();
                break;
            case R.id.uibot_disconnect:
            case R.id.none_activity_uibot_disconnect:
                showMasterDeleteDialog();
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
            case R.id.secure_save:
                changeSettingOption();
                break;
        }
    }

    public void showMasterDeleteDialog() {

        if (mDialog == null) {
            mDialog = new CustomDialog(this, mSeletedItemTotalData.getSlaveRtcid() + " " +
                    mContext.getResources().getString(R.string.delete_master), "", getResources().getString(R.string.cancel), getResources().getString(R.string.confirm), mDialogListner, CustomDialog.TWO_BUTTON_TYPE);
        }
    }

    private CustomDialog.OnDialogListener mDialogListner = new CustomDialog.OnDialogListener() {
        @Override
        public void OnLeftClicked(View v) {

        }

        @Override
        public void OnCenterClicked(View v) {
            mDialog.cancel();
        }

        @Override
        public void OnRightClicked(View v) {
            deleteSlaveData(OuiBotPreferences.getLoginId(SecureModeSettingActivity.this), mSeletedItemTotalData.getSlaveRtcid());
            mDialog.cancel();
        }

        @Override
        public void OnDismissListener() {
            mDialog = null;
        }
    };

    public void deleteSlaveData(String masterID, String slaveID) {
        ContentResolver resolver = getContentResolver();
        resolver.delete(SecureProvider.USER_INFO_TABLE_URI, SecureSQLiteHelper.COL_SLAVE_ID + " =? AND " + SecureSQLiteHelper.COL_MASTER_ID + " =? ", new String[]{slaveID, masterID});
        if(MainActivity.CONTEXT != null) {
            MainActivity.CONTEXT.sendDeleteMasterWithConfigAck(mSeletedItemTotalData.getSlaveRtcid());

        }else{
            Logger.d(TAG,"error MainActivity.CONTEXT == null");
        }
        finish();
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
