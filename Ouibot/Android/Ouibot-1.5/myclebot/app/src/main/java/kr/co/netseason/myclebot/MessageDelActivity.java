package kr.co.netseason.myclebot;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import kr.co.netseason.myclebot.API.MessageListData;
import kr.co.netseason.myclebot.Provider.SecureProvider;
import kr.co.netseason.myclebot.Provider.SecureSQLiteHelper;
import kr.co.netseason.myclebot.UTIL.OuiBotPreferences;


public class MessageDelActivity extends FragmentActivity {
    private Context context;

    private ListView mListView;
    private ListViewAdapter mAdapter;

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

    @Override
    public void finish() {
        super.finish();
    }

    public void initUI() {
        setContentView(R.layout.activity_messagedel);
        context = this;
        mListView = (ListView) findViewById(R.id.history_del_list);
        mAdapter = new ListViewAdapter(context);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                MessageListData positionData = mAdapter.getData().get(position);
                if (positionData.getChecked() == MessageListData.CHECKED) {
                    positionData.setChecked(MessageListData.UNCHECKED);
                } else {
                    positionData.setChecked(MessageListData.CHECKED);
                }
                mAdapter.notifyDataSetChanged();
            }
        });
        ImageView keypad_back = (ImageView) findViewById(R.id.keypad_back);
        keypad_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        LinearLayout btn_all_check = (LinearLayout) findViewById(R.id.btn_all_check);
        btn_all_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getUnCheckItem()) {
                    setAllCheck();
                } else {
                    setAllUnCheck();
                }
                mAdapter.notifyDataSetChanged();
            }
        });


        LinearLayout btn_history_del = (LinearLayout) findViewById(R.id.btn_history_del);
        btn_history_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteSelectedItems();
                getMessageDBData();
                finish();
            }
        });
    }

    public boolean getUnCheckItem() {
        for (int i = 0; i < mAdapter.getData().size(); i++) {
            if (mAdapter.getData().get(i).getChecked() == MessageListData.UNCHECKED) {
                return true;
            }
        }
        return false;
    }

    public void setAllCheck() {
        for (int i = 0; i < mAdapter.getData().size(); i++) {
            if (mAdapter.getData().get(i).getChecked() == MessageListData.UNCHECKED) {
                mAdapter.getData().get(i).setChecked(MessageListData.CHECKED);
            }
        }
    }

    public void setAllUnCheck() {
        for (int i = 0; i < mAdapter.getData().size(); i++) {
            if (mAdapter.getData().get(i).getChecked() == MessageListData.CHECKED) {
                mAdapter.getData().get(i).setChecked(MessageListData.UNCHECKED);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getMessageDBData();
    }

    public void deleteSelectedItems() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
        ContentProviderOperation operation;
        for (int i = 0; i < mAdapter.getData().size(); i++) {
            if (mAdapter.getData().get(i).getChecked() == MessageListData.CHECKED) {
                operation = ContentProviderOperation
                        .newDelete(SecureProvider.MESSAGE_TABLE_URI)
                        .withSelection(SecureSQLiteHelper.COL_RTCID + " = ? AND " + SecureSQLiteHelper.COL_PEER_RTCID + " =? ", new String[]{mAdapter.getData().get(i).getRTCID(), mAdapter.getData().get(i).getPeerRtcid()})
                        .build();
                operations.add(operation);
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

    private class ViewHolder {
        public ImageView mIcon;
        public TextView mName;
        public TextView mNumber;
        public TextView mDate;
        public TextView mData;
        public ImageView mDelCheck;
    }

    private class ListViewAdapter extends BaseAdapter {
        private Context mContext = null;
        private ArrayList<MessageListData> mMessageListData = new ArrayList<MessageListData>();

        public ListViewAdapter(Context mContext) {
            super();
            this.mContext = mContext;
        }

        @Override
        public int getCount() {
            return mMessageListData.size();
        }

        @Override
        public Object getItem(int position) {
            return mMessageListData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public ArrayList<MessageListData> getData() {
            return mMessageListData;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.view_message_dellistitem, null);
                holder.mIcon = (ImageView) convertView.findViewById(R.id.mImage);
                holder.mName = (TextView) convertView.findViewById(R.id.mName);
                holder.mNumber = (TextView) convertView.findViewById(R.id.mNumber);
                holder.mDate = (TextView) convertView.findViewById(R.id.mDate);
                holder.mData = (TextView) convertView.findViewById(R.id.mData);
                holder.mDelCheck = (ImageView) convertView.findViewById(R.id.del_check);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            MessageListData dataList = mMessageListData.get(position);
            holder.mName.setText(dataList.getPeerRtcidName());
            holder.mNumber.setText(dataList.getPeerRtcid());
            holder.mDate.setText(dataList.getTimeStringFormat());
            Glide.with(MessageDelActivity.this).load(getImagePath(dataList.getPeerRtcid())).centerCrop().placeholder(R.drawable.img_contents_person_nopicture).into(holder.mIcon);

            String message;
            if (dataList.getType() == MessageListData.IMAGE_TYPE) {
                message = getResources().getString(R.string.file_image_mode);
            } else if (dataList.getType() == MessageListData.VIDEO_TYPE) {
                message = getResources().getString(R.string.file_video_mode);
            } else {
                message = dataList.getMessageData();
            }
            holder.mData.setText(message);

            if (dataList.getChecked() == MessageListData.CHECKED) {
                holder.mDelCheck.setImageResource(R.drawable.btn_contents_checkbox_checked);
            } else {
                holder.mDelCheck.setImageResource(R.drawable.btn_contents_checkbox_normal);
            }
            return convertView;
        }

        public void addItem(String rtcid, String peerRtcid, String peerRtcidName, String messageData, int sendFlag, long time, int readable, int checked, int type, int sendState) {
            mMessageListData.add(new MessageListData(rtcid, peerRtcid, peerRtcidName, messageData, sendFlag, time, readable, checked, type, sendState));
        }

        private String getImagePath(String mNumber) {
            ContentResolver resolver = getContentResolver();
            Cursor c = resolver.query(SecureProvider.PROFILE_TABLE_URI, new String[]{SecureSQLiteHelper.COL_PROFILE},
                    SecureSQLiteHelper.COL_PEER_RTCID + " = ? ", new String[]{mNumber}, null);
            String profile = "";
            if (c != null && c.moveToFirst()) {
                try {
                    do {
                        profile = c.getString(c.getColumnIndex(SecureSQLiteHelper.COL_PROFILE));

                    } while (c.moveToNext());
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    c.close();
                }
            }
            return profile;
        }
    }

    private void getMessageDBData() {
        mAdapter.getData().clear();
        SecureSQLiteHelper mSecureHelper = new SecureSQLiteHelper(MainActivity.CONTEXT);
        SQLiteDatabase database = mSecureHelper.getReadableDatabase();
        Cursor c = database.query(SecureSQLiteHelper.TABLE_MESSAGE_LIST, SecureSQLiteHelper.TABLE_MESSAGE_ALL_COLUMNS, SecureSQLiteHelper.COL_RTCID + " = ? ", new String[]{OuiBotPreferences.getLoginId(context)}, SecureSQLiteHelper.COL_PEER_RTCID, null, SecureSQLiteHelper.COL_TIME + " desc");

        if (c != null && c.moveToFirst()) {
            try {
                do {
                    mAdapter.addItem(
                            c.getString(c.getColumnIndex(SecureSQLiteHelper.COL_RTCID)),
                            c.getString(c.getColumnIndex(SecureSQLiteHelper.COL_PEER_RTCID)),
                            c.getString(c.getColumnIndex(SecureSQLiteHelper.COL_PEER_RTCID_NAME)),
                            c.getString(c.getColumnIndex(SecureSQLiteHelper.COL_MESSAGE_DATA)),
                            c.getInt(c.getColumnIndex(SecureSQLiteHelper.COL_SEND_FLAG)),
                            c.getLong(c.getColumnIndex(SecureSQLiteHelper.COL_TIME)),
                            c.getInt(c.getColumnIndex(SecureSQLiteHelper.COL_READABLE)),
                            MessageListData.UNCHECKED,
                            c.getInt(c.getColumnIndex(SecureSQLiteHelper.COL_TYPE)),
                            c.getInt(c.getColumnIndex(SecureSQLiteHelper.COL_SEND_STATE)));

                } while (c.moveToNext());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (c != null) {
                    c.close();
                }
            }
        }

        mAdapter.notifyDataSetChanged();
    }

}
