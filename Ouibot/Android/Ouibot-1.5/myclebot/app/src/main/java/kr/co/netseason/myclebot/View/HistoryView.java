package kr.co.netseason.myclebot.View;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import kr.co.netseason.myclebot.ContectAddActivity;
import kr.co.netseason.myclebot.HistoryDelActivity;
import kr.co.netseason.myclebot.Logger.Logger;
import kr.co.netseason.myclebot.MainActivity;
import kr.co.netseason.myclebot.Provider.SecureProvider;
import kr.co.netseason.myclebot.Provider.SecureSQLiteHelper;
import kr.co.netseason.myclebot.R;
import kr.co.netseason.myclebot.SpemAddActivity;
import kr.co.netseason.myclebot.UTIL.OuiBotPreferences;
import kr.co.netseason.myclebot.openwebrtc.Config;

public class HistoryView extends Fragment {
    private static LinearLayout historyview;
    private ListView historyList;
    private static ListViewAdapter mAdapter;

    private Activity mainActivity;

    private LinearLayout btn_control_long;

    private onHistoryCallClickListener mListener;
    public ContactModifyDialog mCustomDialog;

    public interface onHistoryCallClickListener {
        public void onHistoryCALLClick(String number);

        public void onHistoryCCTVClick(String number);

        public void onHistoryPETClick(String number);
    }

    @Override
    public void onAttach(Activity activity) {
        Logger.e("!!!", "HistoryView onAttach");
        super.onAttach(activity);
        mainActivity = activity;
        try {
            mListener = (onHistoryCallClickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnArticleSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Logger.e("!!!", "HistoryView onCreateView");
        if (MainActivity.CONTEXT.mFragments.containsKey(1)) {
            MainActivity.CONTEXT.mFragments.remove(1);
        }
        MainActivity.CONTEXT.mFragments.put(1, this);
        LoadDBData();
        historyview = (LinearLayout) inflater.inflate(R.layout.view_history, container, false);

        btn_control_long = (LinearLayout) historyview.findViewById(R.id.btn_control_long);
        btn_control_long.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent nextActivity = new Intent(MainActivity.CONTEXT, HistoryDelActivity.class);
                startActivity(nextActivity);
            }
        });

        historyList = (ListView) historyview.findViewById(R.id.history_list);

        mAdapter = new ListViewAdapter(MainActivity.CONTEXT);
        historyList.setAdapter(mAdapter);

        return historyview;
    }

    public void LoadDBData() {
        new httpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "recent_list_post.php", "sindex=0&eindex=100&rtcid=" + OuiBotPreferences.getLoginId(MainActivity.CONTEXT));
    }

    public void LoadDBData(Context context) {
        if ( mAdapter != null ) {
            mAdapter.removeAll();
        }
        new httpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "recent_list_post.php", "sindex=0&eindex=100&rtcid=" + OuiBotPreferences.getLoginId(context));
    }

    private class ViewHolder {
        public ImageView mIcon;
        public ImageView mFalgIcon;
        public TextView mName;
        public TextView mNumber;
        public TextView mDate;
        public LinearLayout mCALL_layout;
        public TextView mCALL;
        public LinearLayout mMarginLayout;
    }

    private class ListViewAdapter extends BaseAdapter {
        private Context mContext = null;
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
                convertView = inflater.inflate(R.layout.view_historylistitem, null);

                holder.mIcon = (ImageView) convertView.findViewById(R.id.mImage);
                holder.mFalgIcon = (ImageView) convertView.findViewById(R.id.mImage_callflag);
                holder.mName = (TextView) convertView.findViewById(R.id.mName);
                holder.mNumber = (TextView) convertView.findViewById(R.id.mNumber);
                holder.mDate = (TextView) convertView.findViewById(R.id.mDate);
                holder.mCALL = (TextView) convertView.findViewById(R.id.mCALL);
                holder.mCALL_layout = (LinearLayout) convertView.findViewById(R.id.mCALL_layout);
                holder.mMarginLayout = (LinearLayout) convertView.findViewById(R.id.margin_layout);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            HistoryListData mData = mHistoryListData.get(position);

            switch (mData.mIcon) {
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
            }
            if(position == getCount()-1){
                holder.mMarginLayout.setVisibility(View.VISIBLE);
            }else{
                holder.mMarginLayout.setVisibility(View.GONE);
            }
            if ( mData.mName.equals(getString(R.string.unlisted_number)) ) {
                holder.mName.setText(mData.mNumber);
                holder.mNumber.setText("");
            } else {
                holder.mName.setText(mData.mName);
                holder.mNumber.setText("");
            }
            holder.mDate.setText(mData.mDate);

            if ( Config.isPhoneId(mData.mNumber) ) {
                holder.mCALL_layout.setBackgroundResource(R.drawable.btn_contents_blue_normal);
                switch (mData.mMode) {
                    case "call" :
                        holder.mCALL.setBackgroundResource(R.drawable.ic_contents_phone);
                        holder.mCALL_layout.setTag(mData.mName + "|" + mData.mNumber + "|" + mData.app);
                        holder.mCALL_layout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String[] strs = ((LinearLayout) v).getTag().toString().split("\\|");
                                if ( strs.length < 3 || strs[2] == null || strs[2].equalsIgnoreCase("") ) {
                                    popup_Dialog(strs[1]);
                                } else {
                                    mListener.onHistoryCALLClick(((LinearLayout) v).getTag().toString());
                                }
                            }
                        });
                        holder.mCALL_layout.setBackgroundResource(R.drawable.btn_contents_blue_normal);
                        break;
                    case "cctv" :
                        holder.mCALL.setBackgroundResource(R.drawable.ic_contents_ouibot);
                        holder.mCALL_layout.setSoundEffectsEnabled(false);
                        holder.mCALL_layout.setOnClickListener(null);
                        holder.mCALL_layout.setBackgroundResource(R.drawable.btn_contents_short_normal);
                        break;
                    case "pet" :
                        holder.mCALL.setBackgroundResource(R.drawable.ic_contents_pet);
                        holder.mCALL_layout.setSoundEffectsEnabled(false);
                        holder.mCALL_layout.setOnClickListener(null);
                        holder.mCALL_layout.setBackgroundResource(R.drawable.btn_contents_short_normal);
                        break;

                }
            } else {
                holder.mCALL_layout.setBackgroundResource(R.drawable.btn_contents_blue_normal);
                holder.mCALL_layout.setTag(mData.mName + "|" + mData.mNumber + "|" + mData.app);
                switch (mData.mMode) {
                    case "call" :
                        holder.mCALL.setBackgroundResource(R.drawable.ic_contents_phone);
                        holder.mCALL_layout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String[] strs = ((LinearLayout) v).getTag().toString().split("\\|");
                                if ( strs.length < 3 || strs[2] == null || strs[2].equalsIgnoreCase("") ) {
                                    popup_Dialog(strs[1]);
                                } else {
                                    mListener.onHistoryCALLClick(((LinearLayout) v).getTag().toString());
                                }
                            }
                        });
                        break;
                    case "cctv" :
                        holder.mCALL.setBackgroundResource(R.drawable.ic_contents_ouibot);
                        holder.mCALL_layout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String[] strs = ((LinearLayout) v).getTag().toString().split("\\|");
                                if ( strs.length < 3 || strs[2] == null || strs[2].equalsIgnoreCase("") ) {
                                    popup_Dialog(strs[1]);
                                } else {
                                    mListener.onHistoryCCTVClick(((LinearLayout) v).getTag().toString());
                                }
                            }
                        });
                        break;
                    case "pet" :
                        holder.mCALL.setBackgroundResource(R.drawable.ic_contents_pet);
                        holder.mCALL_layout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String[] strs = ((LinearLayout) v).getTag().toString().split("\\|");
                                if ( strs.length < 3 || strs[2] == null || strs[2].equalsIgnoreCase("") ) {
                                    popup_Dialog(strs[1]);
                                } else {
                                    mListener.onHistoryPETClick(((LinearLayout) v).getTag().toString());
                                }
                            }
                        });
                        break;

                }
            }

            ImageView mSPAM = (ImageView)convertView.findViewById(R.id.mSPEM);
            ImageView mADDCONTECT = (ImageView)convertView.findViewById(R.id.mADDCONTECT);
            if ( mData.mName.equals(getString(R.string.unlisted_number)) ) {
                mSPAM.setTag(mData.mNumber);
                mSPAM.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ImageView v = (ImageView) view;
                        String number = (String) v.getTag();
                        Intent nextActivity = new Intent(mainActivity, SpemAddActivity.class);
                        nextActivity.putExtra("spemNumber", number);
                        startActivity(nextActivity);
                    }
                });
                mADDCONTECT.setTag(mData.mNumber);
                mADDCONTECT.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ImageView v = (ImageView) view;
                        String number = (String) v.getTag();
                        Intent nextActivity = new Intent(mainActivity, ContectAddActivity.class);
                        nextActivity.putExtra("addNumber", number);
                        startActivity(nextActivity);
                    }
                });
                mSPAM.setVisibility(View.VISIBLE);
                mADDCONTECT.setVisibility(View.VISIBLE);
            } else {
                mSPAM.setVisibility(View.GONE);
                mADDCONTECT.setVisibility(View.GONE);
            }

            ImageView content_profile_layout = (ImageView)convertView.findViewById(R.id.content_profile_layout);
            Glide.with(MainActivity.CONTEXT).load(getImagePath(mData.mNumber)).centerCrop().placeholder(R.drawable.img_contents_person_nopicture).into(content_profile_layout);




            Logger.w("!!!", "mData.certification = " + mData.certification);
            Logger.w("!!!", "mData.certification = "+mData.certification);
            Logger.w("!!!", "mData.certification = "+mData.certification);
            Logger.w("!!!", "mData.certification = "+mData.certification);

            if ( mData.certification.equals("0") || mData.certification.equals("") ) {
                switch (mData.mMode) {
                    case "pet":
                        holder.mCALL_layout.setSoundEffectsEnabled(false);
                        holder.mCALL_layout.setOnClickListener(null);
                        holder.mCALL_layout.setBackgroundResource(R.drawable.btn_contents_short_normal);
                        break;
                    case "cctv":
                        holder.mCALL_layout.setSoundEffectsEnabled(false);
                        holder.mCALL_layout.setOnClickListener(null);
                        holder.mCALL_layout.setBackgroundResource(R.drawable.btn_contents_short_normal);
                        break;
                }
            }

            return convertView;
        }

        public void addItem(String idx, String icon, String mName, String mNumber, String mDate, String mMode, String app, String certification) {
            HistoryListData addInfo = null;
            addInfo = new HistoryListData(idx, icon, mName, mNumber, mDate, mMode, app, certification);
            mHistoryListData.add(addInfo);
        }

        public void remove(int position) {
            mHistoryListData.remove(position);
            dataChange();
        }

        public void removeAll() {
            mHistoryListData.clear();
            dataChange();
        }

        public void sort() {
            Collections.sort(mHistoryListData, HistoryListData.ALPHA_COMPARATOR);
            dataChange();
        }

        public void dataChange() {
            mAdapter.notifyDataSetChanged();
        }
    }

    private void popup_Dialog(String str) {
        if ( Config.isPhoneId(str)) {
            mCustomDialog = new ContactModifyDialog(mainActivity,
                    getString(R.string.phone_update_message),
                    getString(R.string.confirm), "", "",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mCustomDialog.dismiss();
                        }
                    },
                    null,
                    null);
            mCustomDialog.show();
        } else {
            mCustomDialog = new ContactModifyDialog(mainActivity,
                    getString(R.string.ouibot_update_message),
                    getString(R.string.confirm), "", "",
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



    //AsyncTask<param,Progress,Result>
    private class httpTask extends android.os.AsyncTask<String, Void, String> {
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
            if (HistoryView.this.getView() == null) {
                Logger.d("!!!","no refresh");
                return;
            }
            result = result.trim();
            Logger.e("!!!", "httpTask result = | " + result + " |");
            if (result.contains("NOT OK") || result.contains("true") || result.contains("false") || result.trim().equals("") || result.trim().equals("null")) {
                if ( historyview != null ) {
                    TextView history_empty = (TextView)historyview.findViewById(R.id.history_empty);
                    history_empty.setVisibility(View.VISIBLE);
                }
                return;
            } else if (result.contains("ConnectException")) {
                String[] strs = result.split("\\|");
                TextView history_empty = (TextView) historyview.findViewById(R.id.history_empty);
                history_empty.setVisibility(View.VISIBLE);
                history_empty.setText(R.string.internet_not_connect);
                return;
            } else if (result.contains("SocketTimeoutException")) {
                String[] strs = result.split("\\|");
                TextView history_empty = (TextView) historyview.findViewById(R.id.history_empty);
                history_empty.setVisibility(View.VISIBLE);
                history_empty.setText(R.string.internet_restart);
                new httpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, strs[1], strs[2]);
                return;
            } else {
                TextView history_empty = (TextView) historyview.findViewById(R.id.history_empty);
                history_empty.setVisibility(View.GONE);
            }

            try {
                JSONArray json = new JSONArray(result);
                mAdapter.removeAll();
                if (json.length() > 0) {
                    for (int i = 0; i < json.length(); i++) {
                        if (json.getJSONObject(i).getString("name").equals(json.getJSONObject(i).getString("peer_rtcid"))) {
                            mAdapter.addItem(json.getJSONObject(i).getString("recentid"), json.getJSONObject(i).getString(Config.PARAM_TYPE), getResources().getString(R.string.unlisted_number), json.getJSONObject(i).getString("peer_rtcid"), json.getJSONObject(i).getString("register_date"), json.getJSONObject(i).getString("mode"), json.getJSONObject(i).getString("app"), json.getJSONObject(i).getString("certification"));
                        } else {
                            mAdapter.addItem(json.getJSONObject(i).getString("recentid"), json.getJSONObject(i).getString(Config.PARAM_TYPE), json.getJSONObject(i).getString("name"), json.getJSONObject(i).getString("peer_rtcid"), json.getJSONObject(i).getString("register_date"), json.getJSONObject(i).getString("mode"), json.getJSONObject(i).getString("app"), json.getJSONObject(i).getString("certification"));
                        }
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
