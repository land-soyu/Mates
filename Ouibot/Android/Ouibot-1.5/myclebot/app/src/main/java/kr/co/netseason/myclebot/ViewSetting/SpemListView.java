package kr.co.netseason.myclebot.ViewSetting;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
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

import java.util.ArrayList;
import java.util.List;

import kr.co.netseason.myclebot.Logger.Logger;
import kr.co.netseason.myclebot.MainActivity;
import kr.co.netseason.myclebot.Provider.SecureProvider;
import kr.co.netseason.myclebot.Provider.SecureSQLiteHelper;
import kr.co.netseason.myclebot.R;
import kr.co.netseason.myclebot.SpemAddActivity;
import kr.co.netseason.myclebot.View.ContactModifyDialog;
import kr.co.netseason.myclebot.openwebrtc.Config;

public class SpemListView extends Fragment {
    private Context context;
    private LinearLayout spemListView;
    private LinearLayout btn_control_long;

    private ListView spemList;
    private static ListViewAdapter mAdapter;

    private String selectNumber;

    public ContactModifyDialog mCustomDialog;

    private static TextView spemlistview_empty;
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Logger.e("!!!", "SpemListView onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Logger.e("!!!", "SpemListView onCreateView");
        if(Config.Mode == Config.COMPILE_Ouibot){
            if (MainActivity.CONTEXT.settingFragments.containsKey(1)) {
                MainActivity.CONTEXT.settingFragments.remove(1);
            }
            MainActivity.CONTEXT.settingFragments.put(1, this);
        }else {
            if (MainActivity.CONTEXT.settingFragments.containsKey(0)) {
                MainActivity.CONTEXT.settingFragments.remove(0);
            }
            MainActivity.CONTEXT.settingFragments.put(0, this);
        }
        context = inflater.getContext();
        spemListView = (LinearLayout) inflater.inflate(R.layout.view_setting_spemlist, container, false);

        spemlistview_empty = (TextView)spemListView.findViewById(R.id.spemlistview_empty);

        btn_control_long = (LinearLayout)spemListView.findViewById(R.id.btn_control_long);
        btn_control_long.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent nextActivity = new Intent(context, SpemAddActivity.class);
                startActivityForResult(nextActivity, 300);
            }
        });
        spemList = (ListView)spemListView.findViewById(R.id.spemlistview_list);

        mAdapter = new ListViewAdapter(context);
        spemList.setAdapter(mAdapter);
        spemList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                String mData = mAdapter.mListData.get(position);
            }
        });

        getSpemDBData();
        return spemListView;
    }



    public void getSpemDBData() {
        Logger.e("!!!", "getSpemDBData");
        mAdapter.removeAll();
        ContentResolver resolber = context.getContentResolver();
        Cursor c = resolber.query(SecureProvider.SPEM_TABLE_URI, SecureSQLiteHelper.TABLE_SPEM_ALL_COLUMNS,
                "",
                null, "");

        if (c != null && c.moveToFirst()) {
            try {
                spemlistview_empty.setVisibility(View.GONE);
                do {
                    mAdapter.addItem(c.getString(c.getColumnIndex(SecureSQLiteHelper.COL_PEER_RTCID)));
                } while (c.moveToNext());
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                c.close();
            }
        } else {
            spemlistview_empty.setVisibility(View.VISIBLE);
        }
        mAdapter.dataChange();
    }
    public void getSpemDBData(Context con) {
        mAdapter.removeAll();
        ContentResolver resolber = con.getContentResolver();
        Cursor c = resolber.query(SecureProvider.SPEM_TABLE_URI, SecureSQLiteHelper.TABLE_SPEM_ALL_COLUMNS, "", null, "");

        if (c != null && c.moveToFirst()) {
            try {
                spemlistview_empty.setVisibility(View.GONE);
                do {
                    mAdapter.addItem(c.getString(c.getColumnIndex(SecureSQLiteHelper.COL_PEER_RTCID)));
                } while (c.moveToNext());
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                c.close();
            }
        } else {
            spemlistview_empty.setVisibility(View.VISIBLE);
        }
        mAdapter.dataChange();
    }







    private class ViewHolder {
        public TextView mNumber;
        public TextView mDEL;
    }

    private class ListViewAdapter extends BaseAdapter {
        private Context mContext = null;
        private String mData;
        // listdata init
        private List<String> mListData = new ArrayList<String>();

        public ListViewAdapter(Context mContext) {
            super();
            this.mContext = mContext;
        }

        @Override
        public int getCount() {
            return mListData.size();
        }

        @Override
        public Object getItem(int position) {
            return mListData.get(position);
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
                convertView = inflater.inflate(R.layout.view_setting_spemlist_item, null);

                holder.mNumber = (TextView) convertView.findViewById(R.id.mNumber);
                holder.mDEL = (TextView) convertView.findViewById(R.id.mDEL);

                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }

            mData = mListData.get(position);

            holder.mNumber.setText(mData);

            holder.mDEL.setHint(mData);
            holder.mDEL.setText(getResources().getString(R.string.spam_remove));
            holder.mDEL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectNumber = ((TextView) v).getHint().toString();
                    mCustomDialog = new ContactModifyDialog(context,
                            getResources().getString(R.string.spem_unlock),
                            getResources().getString(R.string.cancel), getResources().getString(R.string.confirm), "",
                            cancelClickListener,
                            okClickListener,
                            null);
                    mCustomDialog.show();
                }
            });

            return convertView;
        }

        public void addItem(String number){
            Logger.e("!!!", "addItem = " + number);
            mListData.add(number);
        }

        public void remove(int position){
            mListData.remove(position);
            dataChange();
        }
        public void removeAll(){
            mListData.clear();
            dataChange();
        }

        public void dataChange(){
            mAdapter.notifyDataSetChanged();
        }
    }




    private View.OnClickListener cancelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mCustomDialog.dismiss();
        }
    };
    private View.OnClickListener okClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            deleteItem();

            mCustomDialog.dismiss();
            mCustomDialog = new ContactModifyDialog(context,
                    getResources().getString(R.string.release),
                    getResources().getString(R.string.confirm), "", "",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            getSpemDBData();
                            mCustomDialog.dismiss();
                        }
                    },
                    null,
                    null);
            mCustomDialog.show();
        }
    };
    private void deleteItem() {
        ContentResolver resolver = context.getContentResolver();
        resolver.delete(SecureProvider.SPEM_TABLE_URI,
                SecureSQLiteHelper.COL_PEER_RTCID + " =? ",
                new String[]{selectNumber});
    }

}
