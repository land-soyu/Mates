package kr.co.netseason.myclebot.Security;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import java.io.File;
import java.util.ArrayList;

import kr.co.netseason.myclebot.Logger.Logger;
import kr.co.netseason.myclebot.Provider.SecureProvider;
import kr.co.netseason.myclebot.Provider.SecureSQLiteHelper;
import kr.co.netseason.myclebot.R;
import kr.co.netseason.myclebot.openwebrtc.Config;

/**
 * Created by tbzm on 15. 10. 28.
 */
public class SecureDetectedListDeleteActivity extends Activity implements View.OnClickListener {
    private final String TAG = getClass().getName();
    private static Context mContext;
    private ViewFlipper mViewFlipper;
    private ImageView mBtnBack;
    private final int DETECT_LIST_VIEW_INDEX = 0;
    private final int ACTIVITY_LIST_VIEW_INDEX = 1;
    private LinearLayout mBtnLayoutDetect;
    private LinearLayout mNoneBtnLayoutActivity;
    private TextView mTextDetect;
    private TextView mTextActivity;
    private LinearLayout mLayoutDetect;
    private LinearLayout mLayoutActivity;
    private ListView mDetectedListView;
    private ListView mNoneActivityListView;
    private SecureDetectedDeleteListAdapter mDetectedAdapter;
    private SecureDetectedDeleteListAdapter mNoneActivityAdapter;
    private LinearLayout mTotalDeleteButton;
    private LinearLayout mSeletedDeleteButton;
    private ArrayList<SecureDetectedData> arrayDetectedData = new ArrayList<SecureDetectedData>();
    private ArrayList<SecureDetectedData> arrayNoneActivityData = new ArrayList<SecureDetectedData>();

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
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_detected_delete_list);
        mViewFlipper = (ViewFlipper) findViewById(R.id.view_flipper);
        mBtnLayoutDetect = (LinearLayout) findViewById(R.id.btn_layout_detect);
        mBtnLayoutDetect.setOnClickListener(this);
        mNoneBtnLayoutActivity = (LinearLayout) findViewById(R.id.btn_layout_activity);
        mNoneBtnLayoutActivity.setOnClickListener(this);
        mLayoutDetect = (LinearLayout) findViewById(R.id.detect_layout_id);
        mLayoutActivity = (LinearLayout) findViewById(R.id.activity_layout_id);
        mTextDetect = (TextView) findViewById(R.id.tv_text_detect_mode);
        mTextActivity = (TextView) findViewById(R.id.tv_text_activity_mode);
        mDetectedListView = (ListView) findViewById(R.id.detected_list);
        mNoneActivityListView = (ListView) findViewById(R.id.none_activity_list);
        mBtnBack = (ImageView) findViewById(R.id.keypad_back);
        mBtnBack.setOnClickListener(this);
        mDetectedAdapter = new SecureDetectedDeleteListAdapter(this, arrayDetectedData);
        mNoneActivityAdapter = new SecureDetectedDeleteListAdapter(this, arrayNoneActivityData);
        mSeletedDeleteButton = (LinearLayout) findViewById(R.id.btn_selected_delete);
        mSeletedDeleteButton.setOnClickListener(this);
        mTotalDeleteButton = (LinearLayout) findViewById(R.id.btn_all_check);
        mTotalDeleteButton.setOnClickListener(this);
        mDetectedListView.setAdapter(mDetectedAdapter);
        mDetectedListView.setOnItemClickListener(mOnListItemClicked);
        mNoneActivityListView.setAdapter(mNoneActivityAdapter);
        mNoneActivityListView.setOnItemClickListener(mOnListItemClicked);
        if (getIntent().getIntExtra(Config.INTENT_MOVE_TO_LINK_SETTING_SECURE_MODE, Config.DETECT_SECURE_MODE) == Config.DETECT_SECURE_MODE) {
            mBtnLayoutDetect.callOnClick();
        } else {
            mNoneBtnLayoutActivity.callOnClick();
        }
    }

    AdapterView.OnItemClickListener mOnListItemClicked = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            SecureDetectedData data = ((SecureDetectedDeleteListAdapter) parent.getAdapter()).getData().get(position);
            if (data.getSelected() == 0) {
                data.setSelected(1);
            } else {
                data.setSelected(0);
            }
            ((SecureDetectedDeleteListAdapter) parent.getAdapter()).notifyDataSetChanged();
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
            case R.id.btn_all_check:
                if (getCheckedItem(getCurrentMode()).size() != getCurrentData(getCurrentMode()).size()) {
                    setItemTotalChecked(getCurrentMode());
                } else {
                    setItemTotalUnChecked(getCurrentMode());
                }
                requestNotifyDataChanged(getCurrentMode());
                break;
            case R.id.btn_selected_delete:
                deleteData(getCheckedItem(getCurrentMode()));
                requestNotifyDataChanged(getCurrentMode());
                finish();
                break;
        }
    }

    private void deleteData(ArrayList<SecureDetectedData> data) {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
        ContentProviderOperation operation;
        for (SecureDetectedData item : data) {

            operation = ContentProviderOperation
                    .newDelete(SecureProvider.SECURE_MASTER_TABLE_URI)
                    .withSelection(SecureSQLiteHelper.COL_FILE_PATH + " = ?", new String[]{item.getImagePath()})
                    .build();

            operations.add(operation);
            File file = new File(item.getImagePath());
            boolean b = file.delete();
            if(b){
                Logger.d(TAG, "파일이 삭제되었습니다. ");
            }else{
                Logger.d(TAG, "file delete fail ");
            }
        }
        try {
            getContentResolver().applyBatch(SecureProvider.AUTHORITY, operations);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }

    private void requestNotifyDataChanged(int mode) {
        if (mode == Config.DETECT_SECURE_MODE) {
            mDetectedAdapter.notifyDataSetChanged();
        } else {
            mNoneActivityAdapter.notifyDataSetChanged();
        }
    }

    private void setItemTotalChecked(int mode) {
        ArrayList<SecureDetectedData> data = new ArrayList<SecureDetectedData>();
        if (mode == Config.DETECT_SECURE_MODE) {
            for (int i = 0; i < arrayDetectedData.size(); i++) {
                if (arrayDetectedData.get(i).getSelected() != 1) {
                    arrayDetectedData.get(i).setSelected(1);
                }
            }
        } else {
            for (int i = 0; i < arrayNoneActivityData.size(); i++) {
                if (arrayNoneActivityData.get(i).getSelected() != 1) {
                    arrayNoneActivityData.get(i).setSelected(1);
                }
            }
        }
    }

    private void setItemTotalUnChecked(int mode) {
        ArrayList<SecureDetectedData> data = new ArrayList<SecureDetectedData>();
        if (mode == Config.DETECT_SECURE_MODE) {
            for (int i = 0; i < arrayDetectedData.size(); i++) {
                if (arrayDetectedData.get(i).getSelected() != 0) {
                    arrayDetectedData.get(i).setSelected(0);
                }
            }
        } else {
            for (int i = 0; i < arrayNoneActivityData.size(); i++) {
                if (arrayNoneActivityData.get(i).getSelected() != 0) {
                    arrayNoneActivityData.get(i).setSelected(0);
                }
            }
        }
    }

    private ArrayList<SecureDetectedData> getCurrentData(int mode) {
        if (mode == Config.DETECT_SECURE_MODE) {
            return arrayDetectedData;
        } else {
            return arrayNoneActivityData;
        }
    }

    private ArrayList<SecureDetectedData> getCheckedItem(int mode) {
        ArrayList<SecureDetectedData> data = new ArrayList<SecureDetectedData>();
        if (mode == Config.DETECT_SECURE_MODE) {
            for (int i = 0; i < arrayDetectedData.size(); i++) {
                if (arrayDetectedData.get(i).getSelected() == 1) {
                    data.add(arrayDetectedData.get(i));
                }
            }
        } else {
            for (int i = 0; i < arrayNoneActivityData.size(); i++) {
                if (arrayNoneActivityData.get(i).getSelected() == 1) {
                    data.add(arrayNoneActivityData.get(i));
                }
            }
        }
        return data;
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
                c.close();
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