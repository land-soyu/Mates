package kr.co.netseason.myclebot.ViewSetting;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

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
import kr.co.netseason.myclebot.Logger.Logger;
import kr.co.netseason.myclebot.MainActivity;
import kr.co.netseason.myclebot.R;
import kr.co.netseason.myclebot.UTIL.OuiBotPreferences;
import kr.co.netseason.myclebot.View.ContactModifyDialog;
import kr.co.netseason.myclebot.openwebrtc.Config;

public class ViewPermissionView extends Fragment {
    private Context context;
    private LinearLayout viewPermissionView;

    private ListView permissionList;
    private static ListViewAdapter mAdapter;

    private String selectname;
    private String selectNumber;

    private onPermissionClickListener mListener;

    public interface onPermissionClickListener {
        public void onPermissionCALLClick(String number);
    }

    @Override
    public void onAttach(Activity activity) {
        Logger.e("!!!", "ViewPermissionView onAttach");
        super.onAttach(activity);
        try {
            mListener = (onPermissionClickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnArticleSelectedListener");
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Logger.e("!!!", "ViewPermissionView onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Logger.e("!!!", "ViewPermissionView onCreateView");
        if (MainActivity.CONTEXT.settingFragments.containsKey(0)) {
            MainActivity.CONTEXT.settingFragments.remove(0);
        }
        MainActivity.CONTEXT.settingFragments.put(0, this);
        context = inflater.getContext();
        LoadDBData();

        viewPermissionView = (LinearLayout) inflater.inflate(R.layout.view_setting_viewpermission, container, false);

        permissionList = (ListView)viewPermissionView.findViewById(R.id.viewpermissionview_list);
        mAdapter = new ListViewAdapter(context);
        permissionList.setAdapter(mAdapter);
        permissionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                ContactListData mData = mAdapter.mContactListData.get(position);
            }
        });



        return viewPermissionView;
    }





    public void LoadDBData() {
        new httpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "setting.php", "rtcid=" + OuiBotPreferences.getLoginId(context));
    }
    public void LoadDBData(Context context) {
        mAdapter.removeAll();
        new httpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "setting.php", "rtcid=" + OuiBotPreferences.getLoginId(context));
    }

    private class ViewHolder {
        public TextView mFlag;
        public TextView mName;
        public TextView mNumber;
        public TextView mDELL;
    }

    public ContactModifyDialog mCustomDialog;
    private class ListViewAdapter extends BaseAdapter {
        private Context mContext = null;
        private ContactListData mData;
        // listdata init
        private List<ContactListData> mContactListData = new ArrayList<ContactListData>();

        public ListViewAdapter(Context mContext) {
            super();
            this.mContext = mContext;
        }

        @Override
        public int getCount() {
            return mContactListData.size();
        }

        @Override
        public Object getItem(int position) {
            return mContactListData.get(position);
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
                convertView = inflater.inflate(R.layout.view_setting_viewpermission_item, null);

                holder.mFlag = (TextView) convertView.findViewById(R.id.del_check);
                holder.mName = (TextView) convertView.findViewById(R.id.mName);
                holder.mNumber = (TextView) convertView.findViewById(R.id.mNumber);
                holder.mDELL = (TextView) convertView.findViewById(R.id.mDEL);

                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }

            mData = mContactListData.get(position);

            if ( mData.mCCTV == 1 ) {
                holder.mFlag.setBackgroundResource(R.drawable.btn_contents_checkbox_checked);
            } else {
                holder.mFlag.setBackgroundResource(R.drawable.btn_contents_checkbox_normal);
            }
            holder.mFlag.setHint(mData.mName + "|" + mData.mNumber + "|" + mData.mCCTV);
            holder.mFlag.setText(" ");
            holder.mFlag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setPermission(v);
                }
            });
            if ( mData.mName.equals("") ) {
                holder.mName.setTextColor(Color.parseColor("#F00000"));
                holder.mName.setText(getResources().getString(R.string.unlisted_number));
            } else {
                holder.mName.setTextColor(Color.parseColor("#4dc1d2"));
                holder.mName.setText(mData.mName);
            }
//            holder.mName.setHint(mData.mName + "|" + mData.mNumber + "|" + mData.mCCTV);
//            holder.mName.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    setPermission(v);
//                }
//            });

            holder.mNumber.setText(mData.mNumber);

            holder.mDELL.setHint(mData.mName + "|" + mData.mNumber);
            holder.mDELL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String[] strs = ((TextView) v).getHint().toString().split("\\|");
                    selectname = strs[0];
                    selectNumber = strs[1];
                    mCustomDialog = new ContactModifyDialog(context,
                            getResources().getString(R.string.the_contact_del),
                            getResources().getString(R.string.delete),  getResources().getString(R.string.cancel), "",
                            okClickListener,
                            cancelClickListener,
                            null);
                    mCustomDialog.show();
                }
            });

            return convertView;
        }

        private void setPermission(View v) {
            String[] strs = ((TextView) v).getHint().toString().split("\\|");
            selectname = strs[0];
            selectNumber = strs[1];
            if ( strs[2].equals("1") ) {
                mCustomDialog = new ContactModifyDialog(context,
                        getResources().getString(R.string.delete_permission),
                        getResources().getString(R.string.delete),  getResources().getString(R.string.cancel), "",
                        permissionDelListener,
                        cancelClickListener,
                        null);
                mCustomDialog.show();
            } else {
                mCustomDialog = new ContactModifyDialog(context,
                        getResources().getString(R.string.want_to_view_permissions),
                        getResources().getString(R.string.confirm),  getResources().getString(R.string.cancel), "",
                        permissionAddListener,
                        cancelClickListener,
                        null);
                mCustomDialog.show();
            }
        }

        public void addItem(String mTitle, String mDate, int mCertification){
            ContactListData addInfo = null;
            addInfo = new ContactListData(mTitle, mDate, mCertification, 0, 0, "", "");
            mContactListData.add(addInfo);
        }

        public void remove(int position){
            mContactListData.remove(position);
            dataChange();
        }
        public void removeAll(){
            mContactListData.clear();
            dataChange();
        }

        public void sort(){
            Collections.sort(mContactListData, ContactListData.ALPHA_COMPARATOR);
            dataChange();
        }

        public void dataChange(){
            mAdapter.notifyDataSetChanged();
        }
    }

    private View.OnClickListener permissionDelListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mListener.onPermissionCALLClick(selectNumber);
            new httpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "setting_cctv_cert_do_post.php", "rtcid=" + selectNumber + "&cert=0&myrtcid=" + OuiBotPreferences.getLoginId(context));
            mCustomDialog.dismiss();
        }
    };
    private View.OnClickListener permissionAddListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mListener.onPermissionCALLClick(selectNumber);
            new httpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "setting_cctv_cert_do_post.php", "rtcid=" + selectNumber + "&cert=1&myrtcid=" + OuiBotPreferences.getLoginId(context));
            mCustomDialog.dismiss();
        }
    };
    private View.OnClickListener cancelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mCustomDialog.dismiss();
        }
    };
    private View.OnClickListener okClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mListener.onPermissionCALLClick(selectNumber);

            new httpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "setting_cctv_cert_del_do_post.php", "rtcid=" + selectNumber + "&myrtcid=" + OuiBotPreferences.getLoginId(context));

            mCustomDialog.dismiss();
            mCustomDialog = new ContactModifyDialog(context,
                    getResources().getString(R.string.be_eliminated),
                    getResources().getString(R.string.confirm), "", "",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mCustomDialog.dismiss();
                        }
                    },
                    null,
                    null);
            mCustomDialog.show();
        }
    };







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
            if ( result.contains("NOT OK") ||  result.contains("true") ||  result.contains("false") ||  result.trim().equals("") || result.trim().equals("[]") ||  result.trim().contains("null") ) {
                TextView viewpermissionview_empty = (TextView)viewPermissionView.findViewById(R.id.viewpermissionview_empty);
                viewpermissionview_empty.setVisibility(View.VISIBLE);
                return;
            } else {
                TextView viewpermissionview_empty = (TextView)viewPermissionView.findViewById(R.id.viewpermissionview_empty);
                viewpermissionview_empty.setVisibility(View.GONE);
            }

            if ( result.contains("remove") ) {
                LoadDBData(context);
            } else if ( result.contains("success") ){
                LoadDBData(context);
            } else {
                try {
                    mAdapter.removeAll();

                    JSONArray json = new JSONArray(result);
                    if ( json.length() > 0 ) {
                        for (int i=0;i<json.length();i++) {
                            mAdapter.addItem(json.getJSONObject(i).getString("name"), json.getJSONObject(i).getString("rtcid"), json.getJSONObject(i).getInt("certification"));
                        }
                        mAdapter.dataChange();
                    } else {
                        //	Data impty
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
