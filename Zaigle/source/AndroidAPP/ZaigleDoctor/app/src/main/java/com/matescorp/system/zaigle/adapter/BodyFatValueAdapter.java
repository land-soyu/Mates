package com.matescorp.system.zaigle.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.matescorp.system.zaigle.R;
import com.matescorp.system.zaigle.data.BodyValueData;
import com.matescorp.system.zaigle.data.ValueData;

import java.util.ArrayList;

/**
 * Created by sjkim on 17. 7. 11.
 */

public class BodyFatValueAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private Context mContext;
    private ArrayList<BodyValueData> mValueData;
    Animation growAnim;


    public BodyFatValueAdapter(Context context, ArrayList<BodyValueData> valuedata) {
        mContext = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mValueData = valuedata;
        growAnim = AnimationUtils.loadAnimation(mContext, R.anim.grow);

    }

    private static class InfoViewHolder {
        public ImageView mImage;
        public TextView mTitle;
        public TextView mVlaue;
        public ImageView  mG;
      //  public ImageView mRImage;
    }

    @Override
    public int getCount() {
        return mValueData.size();
    }

    @Override
    public Object getItem(int i) {
        return mValueData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        InfoViewHolder holder;
        if(view == null){
            holder = new InfoViewHolder();
            view = mInflater.inflate(R.layout.main_list_item, viewGroup ,false);
            holder.mImage = (ImageView) view.findViewById(R.id.list_img);
            holder.mTitle = (TextView) view.findViewById(R.id.lv_title);
            holder.mVlaue = (TextView) view.findViewById(R.id.lv_result_value);
//            holder.mG = (TextView) view.findViewById(R.id.lv_graph);
            holder.mG= (ImageView) view.findViewById(R.id.list_Rimg);
            view.setTag(holder);
        } else {
            holder = (InfoViewHolder) view.getTag();
        }

        BodyValueData data = mValueData.get(i);
        Log.e("adapter ", " data.getmTitle() === " + data.getmTitle());
        holder.mImage.setImageDrawable(data.getmImage());
        holder.mTitle.setText(data.getmTitle());
        holder.mVlaue.setText(data.getmVlaue());
//        holder.mRImage.setText(data.getmG());

        holder.mTitle.setTextColor(Color.BLACK);
        holder.mVlaue.setTextColor(Color.BLACK);

        holder.mG.setImageDrawable(data.getmG());
//        holder.mRImage.setTextColor(Color.BLACK);

        return view;
    }

    private void addItem(int value) {

    }
}
