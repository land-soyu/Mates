package kr.co.netseason.myclebot.ViewSetting;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kr.co.netseason.myclebot.API.ContactListData;
import kr.co.netseason.myclebot.API.NoticeListData;
import kr.co.netseason.myclebot.ContectAddActivity;
import kr.co.netseason.myclebot.ContectModifyActivity;
import kr.co.netseason.myclebot.Logger.Logger;
import kr.co.netseason.myclebot.MainActivity;
import kr.co.netseason.myclebot.Provider.SecureProvider;
import kr.co.netseason.myclebot.Provider.SecureSQLiteHelper;
import kr.co.netseason.myclebot.R;
import kr.co.netseason.myclebot.UTIL.OuiBotPreferences;
import kr.co.netseason.myclebot.View.ContactModifyDialog;
import kr.co.netseason.myclebot.openwebrtc.Config;

public class NoticeView extends Fragment {
    private static LinearLayout noticeview;
    private ListView noticeList;
    private static ListViewAdapter mAdapter;

    private Activity mainActivity;

    @Override
    public void onAttach(Activity activity) {
        Logger.e("!!!", "NoticeView onAttach");
        super.onAttach(activity);
        mainActivity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (MainActivity.CONTEXT.mFragments.containsKey(0)) {
            MainActivity.CONTEXT.mFragments.remove(0);
        }
        MainActivity.CONTEXT.mFragments.put(0, this);
        Logger.e("!!!", "ContactView onCreateView");
        LoadDBData();
        noticeview = (LinearLayout) inflater.inflate(R.layout.view_setting_notice, container, false);

        noticeList = (ListView) noticeview.findViewById(R.id.noticeview_list);

        mAdapter = new ListViewAdapter(MainActivity.CONTEXT);
        noticeList.setAdapter(mAdapter);
        noticeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                NoticeListData mData = mAdapter.mNoticeListData.get(position);
            }
        });

        return noticeview;
    }

    public void LoadDBData() {
        new httpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "webrtc_mobile_getnotice_json.php", "");
    }

    private class ViewHolder {
        public TextView mNoticeTitle;
        public TextView mNoticeDate;
        public LinearLayout mNoticeDataBg;
        public TextView mNoticeData;
        public ImageView mNoticeDataButton;
    }

    private class ListViewAdapter extends BaseAdapter {
        private Context mContext = null;
        private NoticeListData mData;
        // listdata init
        private List<NoticeListData> mNoticeListData = new ArrayList<NoticeListData>();

        public ListViewAdapter(Context mContext) {
            super();
            this.mContext = mContext;
        }

        @Override
        public int getCount() {
            return mNoticeListData.size();
        }

        @Override
        public Object getItem(int position) {
            return mNoticeListData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();

                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.view_setting_notice_item, null);

                holder.mNoticeTitle = (TextView) convertView.findViewById(R.id.mNoticeTitle);
                holder.mNoticeDate = (TextView) convertView.findViewById(R.id.mNoticeDate);
                holder.mNoticeDataBg = (LinearLayout) convertView.findViewById(R.id.mNoticeDataBg);
                holder.mNoticeData = (TextView) convertView.findViewById(R.id.mNoticeData);
                holder.mNoticeDataButton = (ImageView) convertView.findViewById(R.id.mNoticeDataButton);

                holder.mNoticeDataButton.setImageResource(R.drawable.btn_contents_expand);
                holder.mNoticeDataButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (holder.mNoticeDataBg.getVisibility() == View.GONE) {
                            holder.mNoticeDataButton.setImageResource(R.drawable.btn_contents_expanded);
                            holder.mNoticeDataBg.setVisibility(View.VISIBLE);
                        } else {
                            holder.mNoticeDataButton.setImageResource(R.drawable.btn_contents_expand);
                            holder.mNoticeDataBg.setVisibility(View.GONE);
                        }
                    }
                });

                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (holder.mNoticeDataBg.getVisibility() == View.GONE) {
                            holder.mNoticeDataButton.setImageResource(R.drawable.btn_contents_expanded);
                            holder.mNoticeDataBg.setVisibility(View.VISIBLE);
                        } else {
                            holder.mNoticeDataButton.setImageResource(R.drawable.btn_contents_expand);
                            holder.mNoticeDataBg.setVisibility(View.GONE);
                        }
                    }
                });

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            mData = mNoticeListData.get(position);

            Logger.d("ContactView", "position" + position);

            holder.mNoticeTitle.setText(mData.mNoticeTitle);
            holder.mNoticeDate.setText(mData.mNoticeDate);
            holder.mNoticeData.setText(mData.mNoticeData);

            return convertView;
        }
        private String getImagePath(String mNumber) {
            ContentResolver resolver = mainActivity.getContentResolver();
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
        public void addItem(String mTitle, String mDate, String app) {
            NoticeListData addInfo = null;
            addInfo = new NoticeListData(mTitle, mDate, app);
            mNoticeListData.add(addInfo);
        }

        public void remove(int position) {
            mNoticeListData.remove(position);
            dataChange();
        }

        public void removeAll() {
            mNoticeListData.clear();
            dataChange();
        }

        public void sort() {
            Collections.sort(mNoticeListData, NoticeListData.ALPHA_COMPARATOR);
            dataChange();
        }

        public void dataChange() {
            mAdapter.notifyDataSetChanged();
        }
    }






    //AsyncTask<param,Progress,Result>
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
                conn.setReadTimeout(3000);        // 타임아웃 설정 ms단위
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
            } catch (ConnectException e) {
                e.printStackTrace();
                return "ConnectException|" + args[0] + "|" + args[1];
            } catch (SocketTimeoutException e) {
                e.printStackTrace();
                return "SocketTimeoutException|" + args[0] + "|" + args[1];
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
            if (NoticeView.this.getView() == null) {
                Logger.d("!!!","no refresh");
                return;
            }
            result = result.trim();
            Logger.e("!!!", "httpTask result = | " + result + " |");
            if (result.contains("NOT OK") || result.contains("true") || result.contains("false") || result.trim().equals("") || result.trim().equals("[]") || result.trim().contains("null")) {
                TextView contact_empty = (TextView) noticeview.findViewById(R.id.noticeview_empty);
                contact_empty.setVisibility(View.VISIBLE);
                return;
            } else if (result.contains("ConnectException")) {
                String[] strs = result.split("\\|");
                TextView contact_empty = (TextView) noticeview.findViewById(R.id.noticeview_empty);
                contact_empty.setVisibility(View.VISIBLE);
                contact_empty.setText(R.string.internet_not_connect);
                return;
            } else if (result.contains("SocketTimeoutException")) {
                String[] strs = result.split("\\|");
                TextView contact_empty = (TextView) noticeview.findViewById(R.id.noticeview_empty);
                contact_empty.setVisibility(View.VISIBLE);
                contact_empty.setText(R.string.internet_restart);
                new httpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, strs[1], strs[2]);
                return;
            } else {
                TextView contact_empty = (TextView) noticeview.findViewById(R.id.noticeview_empty);
                contact_empty.setVisibility(View.GONE);
            }

            try {
                JSONArray json = new JSONArray(result);
                mAdapter.removeAll();
                if (json.length() > 0) {
                    for (int i = 0; i < json.length(); i++) {
                        mAdapter.addItem(json.getJSONObject(i).getString("notice_title"), json.getJSONObject(i).getString("reg_date"), json.getJSONObject(i).getString("notice_content"));
                    }
                    mAdapter.dataChange();
                } else {
                    Logger.e("!!!", "Data impty");
                    //	Data impty
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


}
