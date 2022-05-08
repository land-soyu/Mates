package kr.co.netseason.myclebot.View;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.mozilla.javascript.tools.debugger.Main;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import kr.co.netseason.myclebot.API.ContactListData;
import kr.co.netseason.myclebot.API.MessageListData;
import kr.co.netseason.myclebot.Logger.Logger;
import kr.co.netseason.myclebot.MainActivity;
import kr.co.netseason.myclebot.MessageDelActivity;
import kr.co.netseason.myclebot.MessageDetailActivity;
import kr.co.netseason.myclebot.MessageSendActivity;
import kr.co.netseason.myclebot.Provider.SecureProvider;
import kr.co.netseason.myclebot.Provider.SecureSQLiteHelper;
import kr.co.netseason.myclebot.R;
import kr.co.netseason.myclebot.UTIL.OuiBotPreferences;
import kr.co.netseason.myclebot.openwebrtc.Config;

public class MessageView extends Fragment {
    private LinearLayout messageview;
    private ListView messageList;
    private ListViewAdapter mAdapter;
    private final String TAG = getClass().getName();
    private LinearLayout btn_message_send;
    private LinearLayout btn_message_del;
    private Context context;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Logger.e("!!!", "MessageView onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Logger.e("!!!", "MessageView onCreateView");
        if (MainActivity.CONTEXT.mFragments.containsKey(2)) {
            MainActivity.CONTEXT.mFragments.remove(2);
        }
        MainActivity.CONTEXT.mFragments.put(2, this);
        messageview = (LinearLayout) inflater.inflate(R.layout.view_message, container, false);

        TextView message_empty = (TextView) messageview.findViewById(R.id.message_empty);
        message_empty.setVisibility(View.VISIBLE);


        btn_message_send = (LinearLayout) messageview.findViewById(R.id.btn_message_send);
        btn_message_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.CONTEXT, MessageSendActivity.class);
                startActivity(intent);
            }
        });
        btn_message_del = (LinearLayout) messageview.findViewById(R.id.btn_message_del);
        btn_message_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent nextActivity = new Intent(MainActivity.CONTEXT, MessageDelActivity.class);
                startActivity(nextActivity);
            }
        });

        messageList = (ListView) messageview.findViewById(R.id.message_list);

        mAdapter = new ListViewAdapter(MainActivity.CONTEXT);
        messageList.setAdapter(mAdapter);
        messageList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                MessageListData data = mAdapter.getData().get(position);
                Intent intent = new Intent(MainActivity.CONTEXT, MessageDetailActivity.class);
                intent.putExtra("peer_rtcid", data.getPeerRtcid());
                startActivity(intent);
            }
        });
        LoadDBData();
        return messageview;
    }

    public void LoadDBData() {
        getMessageDBData();
        getContactListNameFromWeb();
        setMessageNoneTextView();
    }

    public void setMessageNoneTextView() {
        if (mAdapter == null) {
            return;
        }
        if (mAdapter.getCount() == 0) {
            if (messageview != null) {
                TextView history_empty = (TextView) messageview.findViewById(R.id.message_empty);
                history_empty.setVisibility(View.VISIBLE);
            }
        } else {
            if (messageview != null) {
                TextView history_empty = (TextView) messageview.findViewById(R.id.message_empty);
                history_empty.setVisibility(View.GONE);
            }
        }
    }

    public void getContactListNameFromWeb() {
        if (mAdapter == null) {
            Logger.d(TAG, "mAdapter = " + mAdapter);
            return;
        }
        new httpTask().execute("phonebook_list_post.php", "sindex=0&eindex=100&rtcid=" + OuiBotPreferences.getLoginId(MainActivity.CONTEXT));
    }

    private void getMessageDBData() {
        Logger.d(TAG, "getMessageDBData call mAdapter = " + mAdapter);
        if (mAdapter == null) {
            Logger.d(TAG, "mAdapter = " + mAdapter);
            return;
        }
        mAdapter.getData().clear();
        SecureSQLiteHelper mSecureHelper = new SecureSQLiteHelper(MainActivity.CONTEXT);
        SQLiteDatabase database = mSecureHelper.getReadableDatabase();
        Cursor c = database.query(SecureSQLiteHelper.TABLE_MESSAGE_LIST, SecureSQLiteHelper.TABLE_MESSAGE_ALL_COLUMNS, SecureSQLiteHelper.COL_RTCID + " = ? ", new String[]{OuiBotPreferences.getLoginId(MainActivity.CONTEXT)}, SecureSQLiteHelper.COL_PEER_RTCID, null, SecureSQLiteHelper.COL_TIME + " desc");
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
                c.close();
            }

        }
        mAdapter.notifyDataSetChanged();
    }



    private class ViewHolder {
        public ImageView mIcon;
        public TextView mName;
        public TextView mNumber;
        public TextView mDate;
        public TextView mData;
        public ImageView mNewMark;
        public LinearLayout mMarginLayout;
    }

    private class ListViewAdapter extends BaseAdapter {
        private Context mContext = null;
        private ContactListData mData;
        private ArrayList<MessageListData> mMessageListData = new ArrayList<MessageListData>();

        public ListViewAdapter(Context mContext) {
            super();
            this.mContext = mContext;
        }

        public ArrayList<MessageListData> getData() {
            return mMessageListData;
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

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.view_messagelistitem, null);
                holder.mIcon = (ImageView) convertView.findViewById(R.id.mImage);
                holder.mName = (TextView) convertView.findViewById(R.id.mName);
                holder.mNumber = (TextView) convertView.findViewById(R.id.mNumber);
                holder.mDate = (TextView) convertView.findViewById(R.id.mDate);
                holder.mData = (TextView) convertView.findViewById(R.id.mData);
                holder.mNewMark = (ImageView) convertView.findViewById(R.id.new_mark);
                holder.mMarginLayout = (LinearLayout) convertView.findViewById(R.id.margin_layout);
                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            MessageListData dataList = mMessageListData.get(position);

            if (position == getCount() - 1) {
                holder.mMarginLayout.setVisibility(View.VISIBLE);
            } else {
                holder.mMarginLayout.setVisibility(View.GONE);
            }
            holder.mIcon.setImageResource(R.drawable.img_contents_profile_no_bg);
            holder.mName.setText(dataList.getPeerRtcidName());
            holder.mNumber.setText(dataList.getPeerRtcid());
            holder.mDate.setText(dataList.getTimeStringFormat());

            Glide.with(MainActivity.CONTEXT).load(getImagePath(dataList.getPeerRtcid())).centerCrop().placeholder(R.drawable.img_contents_person_nopicture).into(holder.mIcon);
            String message;
            if (dataList.getType() == MessageListData.IMAGE_TYPE) {
                message = getResources().getString(R.string.file_image_mode);
            } else if (dataList.getType() == MessageListData.VIDEO_TYPE) {
                message = getResources().getString(R.string.file_video_mode);
            } else {
                message = dataList.getMessageData();
            }
            holder.mData.setText(message);
            if (dataList.getReadable() == MessageListData.UNREAD) {
                holder.mNewMark.setVisibility(View.VISIBLE);
            } else {
                holder.mNewMark.setVisibility(View.GONE);
            }

            return convertView;

        }

        public void addItem(String rtcid, String peerRtcid, String peerRtcidName, String messageData, int sendFlag, long time, int readable, int checked, int type, int sendState) {
            mMessageListData.add(new MessageListData(rtcid, peerRtcid, peerRtcidName, messageData, sendFlag, time, readable, checked, type, sendState));
        }

        private String getImagePath(String mNumber) {
            ContentResolver resolver = MainActivity.CONTEXT.getContentResolver();
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

//        private void setItemImage(ImageView view, String mNumber) {
//            ContentResolver resolver = mContext.getContentResolver();
//            Cursor c = resolver.query(Uri.parse("content://kr.co.netseason.myclebot.provider.secureProvider/profile_table"), new String[]{"profile"},
//                    "peer_rtcid" + " = ? ", new String[]{mNumber}, null);
//            if (c != null && c.moveToFirst()) {
//                try {
//                    do {
//                        byte[] profile = c.getBlob(c.getColumnIndex("profile"));
//                        Bitmap bitmap = BitmapFactory.decodeByteArray(profile, 0, profile.length);
//                        Log.e("!!!", "mNumber = " + mNumber);
//                        view.setImageBitmap(bitmap);
//                    } while (c.moveToNext());
//                } catch (Exception e) {
//                    e.printStackTrace();
//                } finally {
//                    if (c != null) {
//                        c.close();
//                    }
//                }
//
//            } else {
//                view.setImageResource(R.drawable.img_contents_person_nopicture);
//            }
//
//        }
    }

    private class httpTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... args) {

            String returnValue = "";
            HttpURLConnection conn = null;
            try {
                Logger.e("!!!", "args[0] = " + args[0]);
                Logger.e("!!!", "args[1] = " + args[1]);
                String urlString = Config.Server_IP + args[0];
                Logger.e("!!!", "urlString = " + urlString);
                URL url = new URL(urlString);

                // open connection
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);            // 입력스트림 사용여부
                conn.setDoOutput(false);            // 출력스트림 사용여부
                conn.setUseCaches(false);        // 캐시사용 여부
                conn.setReadTimeout(20000);        // 타임아웃 설정 ms단위
//                conn.setRequestMethod("GET");  // or GET
                conn.setRequestMethod("POST");

                // POST 값 전달 하기
                StringBuffer params = new StringBuffer("");
//                params.append("name=" + URLEncoder.encode(name)); //한글일 경우 URL인코딩
                params.append(args[1]);
                PrintWriter output = new PrintWriter(conn.getOutputStream());
                output.print(params.toString());
                output.close();

                // Response받기
                StringBuffer sb = new StringBuffer();
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                for (; ; ) {
                    String line = br.readLine();
                    if (line == null) break;
                    sb.append(line + "\n");
                }

                br.close();
                conn.disconnect();
                br = null;
                conn = null;

                returnValue = sb.toString();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (conn!=null) {
                    conn.disconnect();
                }
            }
            return returnValue;
        }

        @Override
        protected void onPostExecute(String result) {
            if (MessageView.this.getView() == null) {
                Logger.d("!!!","no refresh");
                return;
            }
            result = result.trim();
            Logger.e("!!!", "httpTask result = | " + result + " |");
            if (result.contains("NOT OK") || result.contains("true") || result.contains("false") || result.trim().equals("") || result.trim().equals("[]") || result.trim().contains("null")) {
                return;
            } else {
            }

            try {

                JSONArray json = new JSONArray(result);
                HashMap<String, String> hashMap = new HashMap<String, String>();
                if (json.length() > 0) {
                    for (int i = 0; i < json.length(); i++) {
                        hashMap.put(json.getJSONObject(i).getString("peer_rtcid"), json.getJSONObject(i).getString("peer_name"));
                    }
                }
                Logger.d(TAG, "mAdapter = " + mAdapter);
                Logger.d(TAG, "mAdapter.getData = " + mAdapter.getData());
                for (int i = 0; i < mAdapter.getData().size(); i++) {
                    String name = hashMap.get(mAdapter.getData().get(i).getPeerRtcid());
                    Logger.d("", "name = " + name);
                    mAdapter.getData().get(i).setPeerRtcidName(name);
                }
                mAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
