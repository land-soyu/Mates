package kr.co.netseason.myclebot;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.Toast;

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

import kr.co.netseason.myclebot.API.HistoryListData;
import kr.co.netseason.myclebot.Logger.Logger;
import kr.co.netseason.myclebot.Provider.SecureProvider;
import kr.co.netseason.myclebot.Provider.SecureSQLiteHelper;
import kr.co.netseason.myclebot.UTIL.OuiBotPreferences;
import kr.co.netseason.myclebot.openwebrtc.Config;


public class HistoryDelActivity extends FragmentActivity {
    private Context context;

    private ListView history_del_list;
    private static ListViewAdapter mAdapter;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.activity_historydel);

        initView();
    }

    private void initView() {
        context = this;

        history_del_list = (ListView)findViewById(R.id.history_del_list);

        mAdapter = new ListViewAdapter(context);
        history_del_list.setAdapter(mAdapter);
        history_del_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                mAdapter.setAllCheck(false);
                HistoryListData mData = mAdapter.mHistoryListData.get(position);

                LinearLayout panel = (LinearLayout) v;
                ImageView check = (ImageView) panel.findViewById(R.id.history_del_check);

                if (mData.check) {
                    mData.check = false;
                    check.setImageResource(R.drawable.btn_contents_checkbox_normal);
                } else {
                    mData.check = true;
                    check.setImageResource(R.drawable.btn_contents_checkbox_checked);
                }
            }
        });

        ImageView keypad_back = (ImageView)findViewById(R.id.keypad_back);
        keypad_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(-1);
                finish();
            }
        });

        LinearLayout btn_all_check = (LinearLayout)findViewById(R.id.btn_all_check);
        btn_all_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAdapter.allCheck();
            }
        });


        LinearLayout btn_history_del = (LinearLayout)findViewById(R.id.btn_history_del);
        btn_history_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                history_del_action();
            }
        });

        LoadDBData();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historydel);

        initView();
    }
    public void LoadDBData() {
        new httpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "recent_list_post.php", "sindex=0&eindex=100&rtcid=" + OuiBotPreferences.getLoginId(context));
    }
    public void history_del_action() {
        if ( mAdapter.allcheck ) {
            new httpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "recent_remove_all_do_post.php", "rtcid=" + OuiBotPreferences.getLoginId(context));
        } else {
            String recentid = "";
            for ( HistoryListData data : mAdapter.mHistoryListData) {
                if ( data.check ) {
                    recentid += data.mIidx+",";
                }
            }
            String str = "rtcid=" + OuiBotPreferences.getLoginId(context);
            if ( !recentid.equals("" ) ) {
                str += "&recentid="+recentid.substring(0, recentid.length()-1);
            }
            new httpTask().execute("recent_remove_item_do_post.php", str);
        }
    }






    private class ViewHolder {
        public ImageView mIcon;
        public ImageView mFalgIcon;
        public TextView mName;
        public TextView mNumber;
        public TextView mDate;
        public ImageView mProfile;
    }

    private class ListViewAdapter extends BaseAdapter {
        private Context mContext = null;
        private boolean allcheck = false;
        // listdata init
        private ArrayList<HistoryListData> mHistoryListData = new ArrayList<HistoryListData>();

        public ListViewAdapter(Context mContext) {
            super();
            this.mContext = mContext;
        }

        @Override
        public int getCount() {
            return mHistoryListData.size();
        }

        @Override
        public Object getItem(int position) {
            return mHistoryListData.get(position);
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
                convertView = inflater.inflate(R.layout.activity_historydellistitem, null);
                holder.mIcon = (ImageView) convertView.findViewById(R.id.mImage);
                holder.mFalgIcon = (ImageView) convertView.findViewById(R.id.mImage_callflag);
                holder.mName = (TextView) convertView.findViewById(R.id.mName);
                holder.mNumber = (TextView) convertView.findViewById(R.id.mNumber);
                holder.mDate = (TextView) convertView.findViewById(R.id.mDate);
                holder.mProfile = (ImageView)convertView.findViewById(R.id.content_profile_layout);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }

            HistoryListData mData = mHistoryListData.get(position);
            Glide.with(HistoryDelActivity.this).load(getImagePath(mData.mNumber)).centerCrop().placeholder(R.drawable.img_contents_person_nopicture).into(holder.mProfile);

            switch ( mData.mIcon ) {
                case "offer":
                    holder.mIcon.setImageResource(R.drawable.img_contents_person_picture);

                    holder.mFalgIcon.setImageResource(R.drawable.ic_contents_calltype_call_outgo);
                    holder.mName.setTextColor(Color.parseColor("#4dc1d2"));
                    break;
                case "answer":
                    holder.mIcon.setImageResource(R.drawable.img_contents_person_picture);

                    holder.mFalgIcon.setImageResource(R.drawable.ic_contents_calltype_call_income);
                    holder.mName.setTextColor(Color.parseColor("#4dc1d2"));
                    break;
                case "miss":
                    holder.mIcon.setImageResource(R.drawable.img_contents_person_picture_missed);

                    holder.mFalgIcon.setImageResource(R.drawable.ic_contents_calltype_call_missed);
                    holder.mName.setTextColor(Color.parseColor("#e00000"));
                    break;
                default:

                    break;
            }
            holder.mName.setText(mData.mName);
            holder.mNumber.setText(mData.mNumber);
            holder.mDate.setText(mData.mDate);

            ImageView check = (ImageView)convertView.findViewById(R.id.history_del_check);
            if ( mData.check ) {
                check.setImageResource(R.drawable.btn_contents_checkbox_checked);
            } else {
                check.setImageResource(R.drawable.btn_contents_checkbox_normal);
            }
            return convertView;
        }

        public void addItem(String idx, String icon, String mName, String mNumber, String mDate, String mMode, String app, String certification){
            HistoryListData addInfo = null;
            addInfo = new HistoryListData(idx, icon, mName, mNumber, mDate, mMode, app, certification);
            mHistoryListData.add(addInfo);
        }

        public void remove(int position){
            mHistoryListData.remove(position);
            dataChange();
        }
        public void removeAll(){
            mHistoryListData= new ArrayList<HistoryListData>();
            dataChange();
        }

        public void sort(){
            Collections.sort(mHistoryListData, HistoryListData.ALPHA_COMPARATOR);
            dataChange();
        }

        public void dataChange(){
            mAdapter.notifyDataSetChanged();
        }

        public void allCheck() {
            if ( allcheck ) {
                for ( HistoryListData data : mHistoryListData ) {
                    data.check = false;
                }
                allcheck = false;
            } else {
                for ( HistoryListData data : mHistoryListData ) {
                    data.check = true;
                }
                allcheck = true;
            }
            dataChange();
        }

        public void setAllCheck(boolean b){
            allcheck = b;
        }
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

//    private void setItemImage(ImageView view, String mNumber) {
//        ContentResolver resolver = getContentResolver();
//        Cursor c = resolver.query(SecureProvider.PROFILE_TABLE_URI, new String[]{SecureSQLiteHelper.COL_PROFILE},
//                SecureSQLiteHelper.COL_PEER_RTCID + " = ? ", new String[]{mNumber}, null);
//        if (c != null && c.moveToFirst()) {
//            try {
//                do {
//                    byte[] profile = c.getBlob(c.getColumnIndex(SecureSQLiteHelper.COL_PROFILE));
//                    Bitmap bitmap = BitmapFactory.decodeByteArray(profile, 0, profile.length);
//                    view.setImageBitmap(bitmap);
//                } while (c.moveToNext());
//            }catch (Exception e){
//                e.printStackTrace();
//            }finally {
//                c.close();
//            }
//        } else {
//            view.setImageResource(R.drawable.img_contents_person_nopicture);
//        }
//    }

    //AsyncTask<param,Progress,Result>
    private class httpTask extends android.os.AsyncTask<String,Void,String> {
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
                StringBuffer sb =  new StringBuffer();
                BufferedReader br = new BufferedReader( new InputStreamReader(conn.getInputStream()));

                for(;;){
                    String line =  br.readLine();
                    if(line == null) break;
                    sb.append(line+"\n");
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
            result = result.trim();
            Logger.e("!!!", "httpTask result = | " + result + " |");
            if ( result.contains("success") ) {
                Toast.makeText(context, getString(R.string.item_deleted_successfuly), Toast.LENGTH_SHORT).show();
                setResult(200);
                finish();
                return;
            }
            if ( result.contains("fail") ) {
                Toast.makeText(context, getString(R.string.item_deleted_fail)+"\n"+getString(R.string.retry), Toast.LENGTH_SHORT).show();
                return;
            }
            if ( result.contains("NOT OK") ||  result.contains("true") ||  result.contains("false") ||  result.trim().equals("") ) {
                return;
            }

            try {
                JSONArray json = new JSONArray(result);

                if ( json.length() > 0 ) {
                    Logger.e("!!!", "json = | " + json + " |");
                    for (int i=0;i<json.length();i++) {
                        if ( json.getJSONObject(i).getString("name").equals(json.getJSONObject(i).getString("peer_rtcid")) ) {
                            mAdapter.addItem(json.getJSONObject(i).getString("recentid"), json.getJSONObject(i).getString("type"), "미등록 번호", json.getJSONObject(i).getString("peer_rtcid"), json.getJSONObject(i).getString("register_date"), json.getJSONObject(i).getString("mode"), json.getJSONObject(i).getString("app"), json.getJSONObject(i).getString("certification"));
                        } else {
                            mAdapter.addItem(json.getJSONObject(i).getString("recentid"), json.getJSONObject(i).getString("type"), json.getJSONObject(i).getString("name"), json.getJSONObject(i).getString("peer_rtcid"), json.getJSONObject(i).getString("register_date"), json.getJSONObject(i).getString("mode"), json.getJSONObject(i).getString("app"), json.getJSONObject(i).getString("certification"));
                        }
                    }
                    mAdapter.dataChange();
                } else {
                    //	Data impty
                }
            } catch (JSONException e) {
                mAdapter.removeAll();
                e.printStackTrace();
            }
        }
    }
}
