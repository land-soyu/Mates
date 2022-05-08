package kr.co.netseason.myclebot.Security;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import java.util.ArrayList;

import kr.co.netseason.myclebot.Logger.Logger;
import kr.co.netseason.myclebot.Provider.SecureProvider;
import kr.co.netseason.myclebot.Provider.SecureSQLiteHelper;
import kr.co.netseason.myclebot.R;
import kr.co.netseason.myclebot.openwebrtc.Config;

/**
 * Created by tbzm on 15. 10. 19.
 */
public class SecureDetectedListActivity extends Activity implements View.OnClickListener {
    private final String TAG = getClass().getName();
    private ViewFlipper mViewFlipper;
    private ImageView mBtnBack;
    private final int DETECT_LIST_VIEW_INDEX = 0;
    private final int ACTIVITY_LIST_VIEW_INDEX = 1;
    private LinearLayout mBtnLayoutDetect;
    private LinearLayout mBtnLayoutNoneActivity;
    private TextView mTextDetect;
    private TextView mTextActivity;
    private LinearLayout mLayoutDetect;
    private LinearLayout mLayoutActivity;
    private ListView mDetectedListView;
    private ListView mNoneActivityListView;
    private SecureDetectedListAdapter mDetectedAdapter;
    private SecureDetectedListAdapter mNoneActivityAdapter;
    private LinearLayout mCutButton;
    private ArrayList<SecureDetectedData> arrayDetectedData = new ArrayList<SecureDetectedData>();
    private ArrayList<SecureDetectedData> arrayNoneActivityData = new ArrayList<>();

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        initUI();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initUI();
    }
    @Override
    public void finish() {
        super.finish();
    }
    public void initUI() {
        setContentView(R.layout.activity_detected_list);
        mViewFlipper = (ViewFlipper) findViewById(R.id.view_flipper);
        mBtnLayoutDetect = (LinearLayout) findViewById(R.id.btn_layout_detect);
        mBtnLayoutDetect.setOnClickListener(this);
        mBtnLayoutNoneActivity = (LinearLayout) findViewById(R.id.btn_layout_activity);
        mBtnLayoutNoneActivity.setOnClickListener(this);
        mLayoutDetect = (LinearLayout) findViewById(R.id.detect_layout_id);
        mLayoutActivity = (LinearLayout) findViewById(R.id.activity_layout_id);
        mTextDetect = (TextView) findViewById(R.id.tv_text_detect_mode);
        mTextActivity = (TextView) findViewById(R.id.tv_text_activity_mode);
        mDetectedListView = (ListView) findViewById(R.id.detected_list);
        mNoneActivityListView = (ListView) findViewById(R.id.none_activity_list);
        mBtnBack = (ImageView) findViewById(R.id.keypad_back);
        mBtnBack.setOnClickListener(this);
        mDetectedAdapter = new SecureDetectedListAdapter(this, arrayDetectedData);
        mNoneActivityAdapter = new SecureDetectedListAdapter(this, arrayNoneActivityData);
        mCutButton = (LinearLayout) findViewById(R.id.btn_control_cut);
        mCutButton.setOnClickListener(this);
        mDetectedListView.setAdapter(mDetectedAdapter);
        mDetectedListView.setOnItemClickListener(mOnListItemClicked);
        mNoneActivityListView.setAdapter(mNoneActivityAdapter);
        mNoneActivityListView.setOnItemClickListener(mOnListItemClicked);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getIntent().getIntExtra(Config.INTENT_MOVE_TO_LINK_SETTING_SECURE_MODE, Config.DETECT_SECURE_MODE) == Config.DETECT_SECURE_MODE) {
            mBtnLayoutDetect.callOnClick();
        } else {
            mBtnLayoutNoneActivity.callOnClick();
        }
    }

    AdapterView.OnItemClickListener mOnListItemClicked = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            SecureDetectedData data = ((SecureDetectedListAdapter) parent.getAdapter()).getData().get(position);
            Intent intent = new Intent(SecureDetectedListActivity.this, DetectedItemDetailActivity.class);
            intent.putExtra("image_path_key", data.getImagePath());
            intent.putExtra("image_time_key", data.getTime());
            intent.putExtra("image_id_key", getIntent().getStringExtra(Config.INTENT_MOVE_TO_LINK_SETTING_OUIBOT_ID));

            startActivity(intent);

        }
    };

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.keypad_back:
                finish();
                break;
            case R.id.btn_layout_detect:
                setTopMenuBackGroundColor(v);
                moveFlipperWithIndex(DETECT_LIST_VIEW_INDEX);
                break;
            case R.id.btn_layout_activity:
                setTopMenuBackGroundColor(v);
                moveFlipperWithIndex(ACTIVITY_LIST_VIEW_INDEX);
                break;
            case R.id.btn_control_cut:
                Intent intent = new Intent(this, SecureDetectedListDeleteActivity.class);
                intent.putExtra(Config.INTENT_MOVE_TO_LINK_SETTING_OUIBOT_ID, getIntent().getStringExtra(Config.INTENT_MOVE_TO_LINK_SETTING_OUIBOT_ID));
                intent.putExtra(Config.INTENT_MOVE_TO_LINK_SETTING_SECURE_MODE, getCurrentMode());
                startActivity(intent);
                break;
        }
    }

    private int getCurrentMode() {
        if (mViewFlipper.getCurrentView() == mViewFlipper.getChildAt(0)) {
            return Config.DETECT_SECURE_MODE;
        } else {
            return Config.DETECT_MOVEMENT_MODE;
        }
    }

    private void getSecureDetectedData(String ouibotId, int mode) {
        arrayDetectedData.clear();
        ContentResolver resolver = getContentResolver();
        Cursor c = resolver.query(SecureProvider.SECURE_MASTER_TABLE_URI, SecureSQLiteHelper.TABLE_SECURE_MASTER_ALL_COLUMNS,
                SecureSQLiteHelper.COL_ID + " = ? AND " + SecureSQLiteHelper.COL_MODE + " = ? ", new String[]{ouibotId, String.valueOf(mode)}, SecureSQLiteHelper.COL_TIME + " desc");
        if (c != null && c.moveToFirst()) {
            try {
                do {
                    arrayDetectedData.add(new SecureDetectedData(c.getString(c.getColumnIndex(SecureSQLiteHelper.COL_INEDEX)),
                            c.getString(c.getColumnIndex(SecureSQLiteHelper.COL_ID)), c.getString(c.getColumnIndex(SecureSQLiteHelper.COL_FILE_PATH)),
                            c.getString(c.getColumnIndex(SecureSQLiteHelper.COL_MODE)), c.getLong(c.getColumnIndex(SecureSQLiteHelper.COL_TIME)), 0));
                    Logger.d(TAG, c.getString(c.getColumnIndex(SecureSQLiteHelper.COL_FILE_PATH)));

                } while (c.moveToNext());
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                c.close();
            }
        }
        mDetectedAdapter.notifyDataSetChanged();
    }

    private void getSecureNoneActivityData(String ouibotId, int mode) {
        arrayNoneActivityData.clear();
        ContentResolver resolver = getContentResolver();
        Cursor c = resolver.query(SecureProvider.SECURE_MASTER_TABLE_URI, SecureSQLiteHelper.TABLE_SECURE_MASTER_ALL_COLUMNS,
                SecureSQLiteHelper.COL_ID + " = ? AND " + SecureSQLiteHelper.COL_MODE + " = ? ", new String[]{ouibotId, String.valueOf(mode)}, SecureSQLiteHelper.COL_TIME + " desc");
        if (c != null && c.moveToFirst()) {
            try {
                do {
                    arrayNoneActivityData.add(new SecureDetectedData(c.getString(c.getColumnIndex(SecureSQLiteHelper.COL_INEDEX)),
                            c.getString(c.getColumnIndex(SecureSQLiteHelper.COL_ID)), c.getString(c.getColumnIndex(SecureSQLiteHelper.COL_FILE_PATH)),
                            c.getString(c.getColumnIndex(SecureSQLiteHelper.COL_MODE)), c.getLong(c.getColumnIndex(SecureSQLiteHelper.COL_TIME)), 0));
                    Logger.d(TAG, c.getString(c.getColumnIndex(SecureSQLiteHelper.COL_FILE_PATH)));
                } while (c.moveToNext());
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                c.close();
            }
        }
        mNoneActivityAdapter.notifyDataSetChanged();
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
                getSecureDetectedData(getIntent().getStringExtra(Config.INTENT_MOVE_TO_LINK_SETTING_OUIBOT_ID), Config.DETECT_SECURE_MODE);
                break;
            case R.id.btn_layout_activity:
                mTextActivity.setTextColor(getResources().getColor(R.color.top_menu_text_color_activity));
                mLayoutActivity.setBackgroundResource(R.color.top_menu_bg_activity);
                getSecureNoneActivityData(getIntent().getStringExtra(Config.INTENT_MOVE_TO_LINK_SETTING_OUIBOT_ID), Config.DETECT_MOVEMENT_MODE);
                break;
        }
    }

    public void moveFlipperWithIndex(int index) {
        if (mViewFlipper != null) {
            if (mViewFlipper.getCurrentView() != mViewFlipper.getChildAt(index)) {
                if (mViewFlipper.getChildCount() >= index) {
                    mViewFlipper.setDisplayedChild(index);
                }
            }
        }
    }
}
