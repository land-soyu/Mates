package com.matescorp.system.zaigle.settingView;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.matescorp.system.zaigle.MainActivity;
import com.matescorp.system.zaigle.R;
import com.matescorp.system.zaigle.adapter.NoticeListData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sjkim on 17. 7. 12.
 */

public class AboutDeviceActivity extends AppCompatActivity {

    private ListView noticeList = null;
    private static   ListViewAdapter mAdapter = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_custom);

        noticeList = (ListView)findViewById(R.id.noticeview_list);
        mAdapter = new ListViewAdapter(this);
        noticeList.setAdapter(mAdapter);
        noticeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                NoticeListData mData = mAdapter.mNoticeListData.get(position);
            }
        });

        TextView title = (TextView)findViewById(R.id.text_app_title);
        title.setText(R.string.about);
        ImageView back = (ImageView)findViewById(R.id.icon_menu_view);
        back.setImageDrawable(getResources().getDrawable(R.drawable.back_icon));
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });



        mAdapter.removeAll();
        mAdapter.addItem("?????? ??????" , "????????? ????????? ????????? ???????????? ???????????? ??????????????? ???????????? ????????? ????????? ???????????? ??????????????? ?????????.....");
        mAdapter.addItem("?????? ??????" , "????????? ????????? ????????? ???????????? ???????????? ??????????????? ???????????? ????????? ????????? ???????????? ??????????????? ?????????.....");
        mAdapter.addItem("?????? ??????" , "????????? ????????? ????????? ???????????? ???????????? ??????????????? ???????????? ????????? ????????? ???????????? ??????????????? ?????????.....");
    }


    private class ViewHolder {
        public TextView mNoticeTitle;
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
                convertView = inflater.inflate(R.layout.view_setting_about_item, null);

                holder.mNoticeTitle = (TextView) convertView.findViewById(R.id.mNoticeTitle);
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


            holder.mNoticeTitle.setText(mData.mNoticeTitle);
            holder.mNoticeData.setText(mData.mNoticeData);

            return convertView;
        }
        public void addItem(String mTitle, String app) {
            NoticeListData addInfo = null;
            addInfo = new NoticeListData(mTitle, app);
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


        public void dataChange() {
            mAdapter.notifyDataSetChanged();
        }
    }

}
