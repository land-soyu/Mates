package kr.co.netseason.myclebot.View;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import java.util.List;

import kr.co.netseason.myclebot.API.ContactListData;
import kr.co.netseason.myclebot.ContectAddActivity;
import kr.co.netseason.myclebot.ContectModifyActivity;
import kr.co.netseason.myclebot.Logger.Logger;
import kr.co.netseason.myclebot.MainActivity;
import kr.co.netseason.myclebot.Provider.SecureProvider;
import kr.co.netseason.myclebot.Provider.SecureSQLiteHelper;
import kr.co.netseason.myclebot.R;
import kr.co.netseason.myclebot.UTIL.OuiBotPreferences;
import kr.co.netseason.myclebot.openwebrtc.Config;

public class ContactView extends Fragment {
    private static LinearLayout contactview;
    private ListView contactList;
    private static ListViewAdapter mAdapter;

    private Activity mainActivity;

    private LinearLayout btn_control_long;
    private String selectname;
    private String selectNumber;
    private String selectRequest;

    private EditText search_contect;
    public ContactModifyDialog mCustomDialog;

    private onContactCallClickListener mListener;

    public interface onContactCallClickListener {
        public void onContactCALLClick(String number);

        public void onContactCCTVClick(String number);

        public void onContactPETClick(String number);

        public void onContactMESSAGEClick(String number);
    }

    @Override
    public void onAttach(Activity activity) {
        Logger.e("!!!", "ContactView onAttach");
        super.onAttach(activity);
        mainActivity = activity;
        try {
            mListener = (onContactCallClickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnArticleSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (MainActivity.CONTEXT.mFragments.containsKey(0)) {
            MainActivity.CONTEXT.mFragments.remove(0);
        }
        MainActivity.CONTEXT.mFragments.put(0,this);
        Logger.e("!!!", "ContactView onCreateView");
        LoadDBData();
        contactview = (LinearLayout) inflater.inflate(R.layout.view_contact, container, false);

        btn_control_long = (LinearLayout) contactview.findViewById(R.id.btn_control_long);
        btn_control_long.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent nextActivity = new Intent(MainActivity.CONTEXT, ContectAddActivity.class);
                startActivity(nextActivity);
//                startActivityForResult(nextActivity, 100);
            }
        });
        contactList = (ListView) contactview.findViewById(R.id.contact_list);

        mAdapter = new ListViewAdapter(MainActivity.CONTEXT);
        contactList.setAdapter(mAdapter);
        contactList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                ContactListData mData = mAdapter.mContactListData.get(position);
            }
        });

        search_contect = (EditText) contactview.findViewById(R.id.search_contect);
        search_contect.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//				new httpTask().execute("phonebook_list_select_post.php", "sindex=0&eindex=100&rtcid=" + OuiBotPreferences.getLoginId(context)+"&peer_rtcid="+charSequence.toString());
                new httpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "phonebook_list_select_post.php", "sindex=0&eindex=100&rtcid=" + OuiBotPreferences.getLoginId(MainActivity.CONTEXT) + "&peer_rtcid=" + charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        return contactview;
    }

    public void LoadDBData() {
        new httpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "phonebook_list_post.php", "sindex=0&eindex=100&rtcid=" + OuiBotPreferences.getLoginId(MainActivity.CONTEXT));
    }

    public void LoadDBData(Context context) {
        if(context == null){
            context = MainActivity.CONTEXT;
        }
        if (mAdapter != null) {
            mAdapter.removeAll();
        }
        new httpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "phonebook_list_post.php", "sindex=0&eindex=100&rtcid=" + OuiBotPreferences.getLoginId(context));
    }

    private class ViewHolder {
        public TextView mName;
        public TextView mNumber;
        public LinearLayout mCCTV;
        public LinearLayout mPET;
        public LinearLayout mMessage;
        public LinearLayout mCALL;
        public TextView mContect_Modify;
        public LinearLayout mMarginLayout;
    }

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
                convertView = inflater.inflate(R.layout.view_contactlistitem, null);

                holder.mName = (TextView) convertView.findViewById(R.id.mName);
                holder.mNumber = (TextView) convertView.findViewById(R.id.mNumber);
                holder.mCCTV = (LinearLayout) convertView.findViewById(R.id.mCCTV_bg);
                holder.mPET = (LinearLayout) convertView.findViewById(R.id.mPET_bg);
                holder.mMessage = (LinearLayout) convertView.findViewById(R.id.mMessage_bg);
                holder.mCALL = (LinearLayout) convertView.findViewById(R.id.mCALL_bg);
                holder.mContect_Modify = (TextView) convertView.findViewById(R.id.contact_modify);
                holder.mMarginLayout = (LinearLayout) convertView.findViewById(R.id.margin_layout);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            mData = mContactListData.get(position);

            Logger.d("ContactView", "position" + position);
            if (position == getCount() - 1) {
                holder.mMarginLayout.setVisibility(View.VISIBLE);
            } else {
                holder.mMarginLayout.setVisibility(View.GONE);
            }

            holder.mName.setText(mData.mName);
            holder.mNumber.setText(mData.mNumber);
            switch (mData.mCCTV) {
                case 0:
                    holder.mCCTV.setVisibility(View.GONE);
                    break;
                case 1:
                    holder.mCCTV.setVisibility(View.VISIBLE);
                    holder.mCCTV.setTag(mData.mName + "|" + mData.mNumber + "|" + mData.app);
                    holder.mCCTV.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String[] strs = ((LinearLayout) v).getTag().toString().split("\\|");
                            if ( strs.length < 3 || strs[2] == null || strs[2].equalsIgnoreCase("") ) {
                                popup_Dialog(strs[1]);
                            } else {
                                mListener.onContactCCTVClick(((LinearLayout) v).getTag().toString());
                            }
                        }
                    });
                    break;
            }
            switch (mData.mPET) {
                case 0:
                    holder.mPET.setVisibility(View.GONE);
                    break;
                case 1:
                    holder.mPET.setVisibility(View.VISIBLE);
                    holder.mPET.setTag(mData.mName + "|" + mData.mNumber + "|" + mData.app);
                    holder.mPET.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String[] strs = ((LinearLayout) v).getTag().toString().split("\\|");
                            if ( strs.length < 3 || strs[2] == null || strs[2].equalsIgnoreCase("") ) {
                                popup_Dialog(strs[1]);
                            } else {
                                mListener.onContactPETClick(((LinearLayout) v).getTag().toString());
                            }
                        }
                    });
                    break;
            }
            holder.mMessage.setVisibility(View.VISIBLE);
            holder.mMessage.setTag(mData.mName + "|" + mData.mNumber + "|" + mData.app );
            holder.mMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String[] strs = ((LinearLayout) v).getTag().toString().split("\\|");
                    if ( strs.length < 3 || strs[2] == null || strs[2].equalsIgnoreCase("") ) {
                        popup_Dialog(strs[1]);
                    } else {
                        mListener.onContactMESSAGEClick(((LinearLayout) v).getTag().toString());
                    }
                }
            });
            holder.mCALL.setTag(mData.mName + "|" + mData.mNumber + "|" + mData.app);
            holder.mCALL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String[] strs = ((LinearLayout) v).getTag().toString().split("\\|");
                    if ( strs.length < 3 || strs[2] == null || strs[2].equalsIgnoreCase("") ) {
                        popup_Dialog(strs[1]);
                    } else {
                        mListener.onContactCALLClick(((LinearLayout) v).getTag().toString());
                    }
                }
            });
            holder.mContect_Modify.setHint(mData.mName + "|" + mData.mNumber + "|" + mData.request);
            holder.mContect_Modify.setText(" ");
            holder.mContect_Modify.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Logger.w("!!!", "((TextView) v).getHint().toString() = " + ((TextView) v).getHint().toString());
                    String[] strs = ((TextView) v).getHint().toString().split("\\|");
                    selectname = strs[0];
                    selectNumber = strs[1];
                    if (strs.length < 3) {
                        selectRequest = "";
                    } else {
                        selectRequest = strs[2];
                    }
                    mCustomDialog = new ContactModifyDialog(MainActivity.CONTEXT,
                            getString(R.string.contact_modify_and_del),
                            getString(R.string.amend), getString(R.string.delete), getString(R.string.cancel),
                            leftClickListener,
                            rightClickListener,
                            centerClickListener);
                    mCustomDialog.show();
                }
            });

            ImageView content_profile_layout = (ImageView) convertView.findViewById(R.id.content_profile_layout);
            Glide.with(MainActivity.CONTEXT).load(getImagePath(mData.mNumber)).centerCrop().placeholder(R.drawable.img_contents_person_nopicture).into(content_profile_layout);
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
        public void addItem(String mTitle, String mDate, int mCCTV, int mPET, int mMessage, String request, String app) {
            ContactListData addInfo = null;
            addInfo = new ContactListData(mTitle, mDate, mCCTV, mPET, mMessage, request, app);
            mContactListData.add(addInfo);
        }

        public void remove(int position) {
            mContactListData.remove(position);
            dataChange();
        }

        public void removeAll() {
            mContactListData.clear();
            dataChange();
        }

        public void sort() {
            Collections.sort(mContactListData, ContactListData.ALPHA_COMPARATOR);
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

//    private void setItemImage(ImageView view, String mNumber) {
//        ContentResolver resolver = mainActivity.getContentResolver();
//        Cursor c = resolver.query(SecureProvider.PROFILE_TABLE_URI, new String[]{SecureSQLiteHelper.COL_PROFILE},
//                SecureSQLiteHelper.COL_PEER_RTCID + " = ? ", new String[]{mNumber}, null);
//        if (c != null && c.moveToFirst()) {
//            try {
//                do {
//                    byte[] profile = c.getBlob(c.getColumnIndex(SecureSQLiteHelper.COL_PROFILE));
//                    Bitmap bitmap = BitmapFactory.decodeByteArray(profile, 0, profile.length);
//                    view.setImageBitmap(bitmap);
//                } while (c.moveToNext());
//            } catch (Exception e) {
//
//            } finally {
//                c.close();
//            }
//        } else {
//            view.setImageResource(R.drawable.img_contents_person_nopicture);
//        }
//    }


    private View.OnClickListener leftClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mCustomDialog.dismiss();
            Intent nextActivity = new Intent(MainActivity.CONTEXT, ContectModifyActivity.class);
            nextActivity.putExtra("name", selectname);
            nextActivity.putExtra("number", selectNumber);
            nextActivity.putExtra("request", selectRequest);
            startActivity(nextActivity);
        }
    };

    private View.OnClickListener rightClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mCustomDialog.dismiss();
            mCustomDialog = new ContactModifyDialog(MainActivity.CONTEXT,
                    getString(R.string.the_contact_del),
                            getString(R.string.cancel), getString(R.string.confirm), "",
                    cancelClickListener,
                    okClickListener,
                    null);
            mCustomDialog.show();
        }
    };
    private View.OnClickListener centerClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
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

            new httpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "phonebook_remove_rtcid_do_post.php", "peer_rtcid=" + selectNumber + "&rtcid=" + OuiBotPreferences.getLoginId(MainActivity.CONTEXT));

            mCustomDialog.dismiss();
            mCustomDialog = new ContactModifyDialog(MainActivity.CONTEXT,
                    getString(R.string.be_eliminated),
                    getString(R.string.confirm), "", "",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mainActivity.sendBroadcast(new Intent(Config.INTENT_ACTION_REFESH_SECURE_PAGER_DATA));
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
            if (ContactView.this.getView() == null) {
                Logger.d("!!!","no refresh");
                return;
            }
            result = result.trim();
            Logger.e("!!!", "httpTask result = | " + result + " |");
            if (result.contains("NOT OK") || result.contains("true") || result.contains("false") || result.trim().equals("") || result.trim().equals("[]") || result.trim().contains("null")) {
                TextView contact_empty = (TextView) contactview.findViewById(R.id.contact_empty);
                contact_empty.setVisibility(View.VISIBLE);
                return;
            } else if (result.contains("ConnectException")) {
                String[] strs = result.split("\\|");
                TextView contact_empty = (TextView) contactview.findViewById(R.id.contact_empty);
                contact_empty.setVisibility(View.VISIBLE);
                contact_empty.setText(R.string.internet_not_connect);
                return;
            } else if (result.contains("SocketTimeoutException")) {
                String[] strs = result.split("\\|");
                TextView contact_empty = (TextView) contactview.findViewById(R.id.contact_empty);
                contact_empty.setVisibility(View.VISIBLE);
                contact_empty.setText(R.string.internet_restart);
                new httpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, strs[1], strs[2]);
                return;
            } else {
                TextView contact_empty = (TextView) contactview.findViewById(R.id.contact_empty);
                contact_empty.setVisibility(View.GONE);
            }

            if (result.contains("remove")) {
                LoadDBData(MainActivity.CONTEXT);
            } else {
                try {
                    JSONArray json = new JSONArray(result);
                    mAdapter.removeAll();
                    if (json.length() > 0) {
                        for (int i = 0; i < json.length(); i++) {
                            mAdapter.addItem(json.getJSONObject(i).getString("peer_name"), json.getJSONObject(i).getString("peer_rtcid"), json.getJSONObject(i).getInt("cert"), json.getJSONObject(i).getInt("cert"), json.getJSONObject(i).getInt("cert"), json.getJSONObject(i).getString("request"), json.getJSONObject(i).getString("app"));
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

}
