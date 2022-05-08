package kr.co.netseason.myclebot.View;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import kr.co.netseason.myclebot.Logger.Logger;
import kr.co.netseason.myclebot.MainActivity;
import kr.co.netseason.myclebot.Provider.SecureProvider;
import kr.co.netseason.myclebot.Provider.SecureSQLiteHelper;
import kr.co.netseason.myclebot.R;
import kr.co.netseason.myclebot.Security.CustomDialog;
import kr.co.netseason.myclebot.Security.SecureActivity;
import kr.co.netseason.myclebot.Security.SecureDetectedListActivity;
import kr.co.netseason.myclebot.Security.SecureModeOuibotSelfSettingActivity;
import kr.co.netseason.myclebot.Security.SecurePreference;
import kr.co.netseason.myclebot.Security.UibotListData;
import kr.co.netseason.myclebot.UTIL.OuiBotPreferences;
import kr.co.netseason.myclebot.UibotAddActivity;
import kr.co.netseason.myclebot.openwebrtc.Config;

/**
 * Created by tbzm on 15. 11. 4.
 */
public class LinkOuibotSelfSettingView extends Fragment implements View.OnClickListener {
    private ListView mSlaveList;
    private ListView mMasterList;
    private static CustomDialog mDialog;
    public static SlaveListViewAdapter mSlaveListViewAdapter;
    public static MasterListViewAdapter mMasterListViewAdapter;
    private LinearLayout mUibotAddLayout;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (MainActivity.CONTEXT.mFragments.containsKey(3)) {
            MainActivity.CONTEXT.mFragments.remove(3);
        }
        MainActivity.CONTEXT.mFragments.put(3,this);
        mSlaveListViewAdapter = new SlaveListViewAdapter(getActivity().getApplicationContext());
        mMasterListViewAdapter = new MasterListViewAdapter(getActivity().getApplicationContext());
        LoadDBData();
        LinearLayout securityview = (LinearLayout) inflater.inflate(R.layout.view_linksetting, container, false);
        TextView linkmyphoneNumber = (TextView) securityview.findViewById(R.id.linkmyphoneNumber);
        linkmyphoneNumber.setText(" " + OuiBotPreferences.getLoginId(MainActivity.CONTEXT));
        mUibotAddLayout = (LinearLayout) securityview.findViewById(R.id.uibot_add_layout);
        mUibotAddLayout.setOnClickListener(this);
        if (Config.Mode == 1) {
            mUibotAddLayout.setVisibility(View.GONE);
        }
        mSlaveList = (ListView) securityview.findViewById(R.id.ouibot_list);
        mMasterList = (ListView) securityview.findViewById(R.id.master_list);
        mSlaveList.setAdapter(mSlaveListViewAdapter);
        mMasterList.setAdapter(mMasterListViewAdapter);
        return securityview;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.uibot_add_layout:
                Intent intent = new Intent(MainActivity.CONTEXT, UibotAddActivity.class);
                startActivity(intent);
                break;
        }
    }

    public static void LoadDBData() {
        Logger.d("LinkOuibotSelfSettingView", "LoadDBData call mSlaveListViewAdapter == " + mSlaveListViewAdapter);
        if (mSlaveListViewAdapter == null) {
            return;
        }
        if (mMasterListViewAdapter == null) {
            return;
        }
        mSlaveListViewAdapter.removeAll();
        mMasterListViewAdapter.removeAll();

        ArrayList<UibotListData> masterDataList = getMasterList(OuiBotPreferences.getLoginId(MainActivity.CONTEXT));
        for (int i = 0; i < masterDataList.size(); i++) {
            mMasterListViewAdapter.addItem(masterDataList.get(i));
        }
        ArrayList<UibotListData> slaveDataList = getSlaveList(OuiBotPreferences.getLoginId(MainActivity.CONTEXT));
        for (int i = 0; i < slaveDataList.size(); i++) {
            mSlaveListViewAdapter.addItem(slaveDataList.get(i));
        }
        mSlaveListViewAdapter.dataChange();
        mMasterListViewAdapter.dataChange();
    }

    private static ArrayList<UibotListData> getMasterList(String id) {
        ContentResolver resolver = MainActivity.CONTEXT.getContentResolver();
        Cursor c = resolver.query(SecureProvider.USER_INFO_TABLE_URI, new String[]{SecureSQLiteHelper.COL_MASTER_ID},
                SecureSQLiteHelper.COL_SLAVE_ID + " = ? ", new String[]{id}, SecureSQLiteHelper.COL_TIME + " desc");
        ArrayList<UibotListData> masterData = new ArrayList<UibotListData>();
        if (c != null && c.moveToFirst()) {
            try {
                do {
                    String master = c.getString(c.getColumnIndex(SecureSQLiteHelper.COL_MASTER_ID));
                    masterData.add(new UibotListData(master, null, SecurePreference.getDetectMode(), SecurePreference.getDetectOnOff(), SecurePreference.getRecordingOption(), SecurePreference.getRecordingTime(), SecurePreference.getDetectSensitivity(), SecurePreference.getSecuritySettingTime()
                            , SecurePreference.getNoneActivityRecordingOption(), SecurePreference.getNoneActivityRecordingTime(), SecurePreference.getNoneActivitySensitivity(), SecurePreference.getNoneActivityCheckTime()));
                } while (c.moveToNext());
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                c.close();
            }
        }
        return masterData;
    }

    private static ArrayList<UibotListData> getSlaveList(String id) {
        ArrayList<UibotListData> slaveData = new ArrayList<UibotListData>();
        slaveData.add(new UibotListData(null, id, SecurePreference.getDetectMode(), SecurePreference.getDetectOnOff(), SecurePreference.getRecordingOption(), SecurePreference.getRecordingTime(), SecurePreference.getDetectSensitivity(), SecurePreference.getSecuritySettingTime()
                , SecurePreference.getNoneActivityRecordingOption(), SecurePreference.getNoneActivityRecordingTime(), SecurePreference.getNoneActivitySensitivity(), SecurePreference.getNoneActivityCheckTime()));
        return slaveData;
    }

    public static void deleteMasterData(String masterID, String slaveID) {
        ContentResolver resolver = MainActivity.CONTEXT.getContentResolver();
        resolver.delete(SecureProvider.USER_INFO_TABLE_URI, SecureSQLiteHelper.COL_SLAVE_ID + " =? AND " + SecureSQLiteHelper.COL_MASTER_ID + " =? ", new String[]{slaveID, masterID});
        LoadDBData();
        MainActivity.CONTEXT.sendDeleteSlaveWithConfigAck(masterID);
    }


    private static class MasterViewHolder {
        public TextView mMasterId;
    }

    public static class MasterListViewAdapter extends BaseAdapter {
        private Context mContext = null;
        public UibotListData mData;
        public static ArrayList<UibotListData> mMasterListData = new ArrayList<UibotListData>();

        public MasterListViewAdapter(Context mContext) {
            super();
            this.mContext = mContext;
        }

        @Override
        public int getCount() {
            return mMasterListData.size();
        }

        @Override
        public Object getItem(int position) {
            return mMasterListData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MasterViewHolder holder;
            if (convertView == null) {
                holder = new MasterViewHolder();
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.view_linksettingitem_master, null);
                holder.mMasterId = (TextView) convertView.findViewById(R.id.master_id);
                convertView.setTag(holder);
            } else {
                holder = (MasterViewHolder) convertView.getTag();
            }

            mData = mMasterListData.get(position);
            holder.mMasterId.setTag(mData);
            holder.mMasterId.setText(" " + mData.getMasterRtcid());
            holder.mMasterId.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showMasterDeleteDialog((UibotListData) v.getTag());
                }
            });
            return convertView;
        }

        public void showMasterDeleteDialog(final UibotListData data) {
            if (mDialog == null) {
                mDialog = new CustomDialog(MainActivity.CONTEXT, data.getMasterRtcid() + " " +
                        mContext.getResources().getString(R.string.delete_master), "", MainActivity.CONTEXT.getResources().getString(R.string.cancel), MainActivity.CONTEXT.getResources().getString(R.string.confirm), new CustomDialog.OnDialogListener() {
                    @Override
                    public void OnLeftClicked(View v) {

                    }

                    @Override
                    public void OnCenterClicked(View v) {
                        mDialog.cancel();
                    }

                    @Override
                    public void OnRightClicked(View v) {
                        deleteMasterData(data.getMasterRtcid(), OuiBotPreferences.getLoginId(MainActivity.CONTEXT));
                        mDialog.cancel();
                    }

                    @Override
                    public void OnDismissListener() {
                        mDialog = null;
                    }
                }, CustomDialog.TWO_BUTTON_TYPE);
            }
        }

        public void addItem(UibotListData data) {
            mMasterListData.add(data);
        }

        public ArrayList<UibotListData> getData() {
            return mMasterListData;
        }

        public void removeAll() {
            if (mMasterListData != null) {
                mMasterListData.clear();
            }
        }

        public void dataChange() {
            if (mMasterListViewAdapter != null)
                mMasterListViewAdapter.notifyDataSetChanged();
        }
    }

    private static class SlaveViewHolder {
        public TextView mOuibotId;
        public ImageButton mSwitch;
        public TextView mSecureModeText;
        public TextView mSecureModeSubText;
        public Button mModeSetting;
        public Button mNotificationConfirm;
    }

    public static class SlaveListViewAdapter extends BaseAdapter {
        private Context mContext = null;
        public UibotListData mData;
        public static ArrayList<UibotListData> mUibotListData = new ArrayList<UibotListData>();

        public SlaveListViewAdapter(Context mContext) {
            super();
            this.mContext = mContext;
        }

        @Override
        public int getCount() {
            return mUibotListData.size();
        }

        @Override
        public Object getItem(int position) {
            return mUibotListData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            SlaveViewHolder holder;
            if (convertView == null) {
                holder = new SlaveViewHolder();
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.view_linksettingitem, null);
                holder.mOuibotId = (TextView) convertView.findViewById(R.id.ouibot_id);
                holder.mSwitch = (ImageButton) convertView.findViewById(R.id.secure_mode_switch);
                holder.mSecureModeText = (TextView) convertView.findViewById(R.id.secure_mode_text);
                holder.mSecureModeSubText = (TextView) convertView.findViewById(R.id.secure_mode_sub_text);
                holder.mModeSetting = (Button) convertView.findViewById(R.id.btn_mode_setting);
                holder.mModeSetting.setOnClickListener(mItemButtonClicked);
                holder.mNotificationConfirm = (Button) convertView.findViewById(R.id.btn_notification_confirm);
                holder.mNotificationConfirm.setOnClickListener(mItemButtonClicked);
                holder.mSwitch.setOnClickListener(mSwitchClicked);
                convertView.setTag(holder);
            } else {
                holder = (SlaveViewHolder) convertView.getTag();
            }

            mData = mUibotListData.get(position);
            holder.mModeSetting.setTag(mData);
            holder.mNotificationConfirm.setTag(mData);
            holder.mOuibotId.setText(mData.getSlaveRtcid());
            holder.mSecureModeText.setText(getDetectModeString(mData.getDetectMode()));
            holder.mSecureModeSubText.setText(getDetectModeSubString(mData.getDetectMode()));
            holder.mSwitch.setTag(mData);
            holder.mSwitch.setImageResource(setToggleButtonImage(getSecureOnOffValue(mData.getDetectOnOff())));
            return convertView;
        }

        public int setToggleButtonImage(boolean b) {
            if (b) {
                return R.drawable.btn_on;
            } else {
                return R.drawable.btn_off;
            }
        }

        View.OnClickListener mSwitchClicked = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!getSecureOnOffValue(mData.getDetectOnOff())) {
                    if (!Config.isMountedSDcard()) {
                        Toast.makeText(MainActivity.CONTEXT, MainActivity.CONTEXT.getResources().getString(R.string.please_input_your_sdcard), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent intent = new Intent(mContext, SecureActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    MainActivity.CONTEXT.startActivity(intent);
                }
            }
        };

        View.OnClickListener mItemButtonClicked = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UibotListData data = ((UibotListData) v.getTag());
                switch (v.getId()) {
                    case R.id.btn_notification_confirm: {
                        Intent intent = new Intent(MainActivity.CONTEXT, SecureDetectedListActivity.class);
                        intent.putExtra(Config.INTENT_MOVE_TO_LINK_SETTING_OUIBOT_ID, data.getSlaveRtcid());
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent);
                        break;
                    }
                    case R.id.btn_mode_setting: {
                        if (mContext != null) {
                            Intent intent = new Intent(mContext, SecureModeOuibotSelfSettingActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra(Config.SECURE_LIST_ITEM_INTENT_DATA, data);
                            mContext.startActivity(intent);
                        }
                        break;
                    }
                }
            }
        };

        public String getDetectModeString(int mode) {
            if (mode == Config.DETECT_SECURE_MODE) {
                return MainActivity.CONTEXT.getResources().getString(R.string.secure_mode_text);
            } else {
                return MainActivity.CONTEXT.getResources().getString(R.string.movement_mode_text);
            }
        }

        public String getDetectModeSubString(int mode) {
            if (mode == Config.DETECT_SECURE_MODE) {
                return MainActivity.CONTEXT.getResources().getString(R.string.secure_mode_sub_text);
            } else {
                return MainActivity.CONTEXT.getResources().getString(R.string.movement_mode_sub_text);
            }
        }

        public boolean getSecureOnOffValue(String value) {
            if (value.equals(Config.DETECT_ON)) {
                return true;
            } else {
                return false;
            }
        }

        public void addItem(UibotListData data) {
            mUibotListData.add(data);
        }

        public static ArrayList<UibotListData> getData() {
            return mUibotListData;
        }

        public void removeAll() {
            if (mUibotListData != null) {
                mUibotListData.clear();
            }
        }

        public void dataChange() {
            if (mSlaveListViewAdapter != null)
                mSlaveListViewAdapter.notifyDataSetChanged();
        }
    }
}