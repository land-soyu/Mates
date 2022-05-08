package com.matescorp.system.zaigle.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.matescorp.system.zaigle.R;
import com.matescorp.system.zaigle.data.ValueData;

import java.util.ArrayList;

/**
 * Created by sjkim on 17. 7. 11.
 */

public class ResultValueAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private Context mContext;
    private ArrayList<ValueData> mValueData;
    Animation growAnim;


    public ResultValueAdapter(Context context, ArrayList<ValueData> valuedata) {
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

        ValueData data = mValueData.get(i);
        Log.e("adapter ", " data.getmTitle() === " + data.getmTitle());
        holder.mImage.setImageDrawable(data.getmImage());
        holder.mTitle.setText(data.getmTitle());
        holder.mVlaue.setText(data.getmVlaue());
//        holder.mRImage.setText(data.getmG());

        holder.mTitle.setTextColor(Color.BLACK);
        holder.mVlaue.setTextColor(Color.BLACK);

        holder.mG.setImageDrawable(data.getmG());
//        holder.mRImage.setTextColor(Color.BLACK);



//        int a = 0;
//        try {
//            a = Integer.parseInt(data.getmVlaue().toString());
//        }catch ( Exception e ) {
//        }


//        LinearLayout itemLayout = new LinearLayout(mContext);
//        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
//
//
//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.WRAP_CONTENT,
//                LinearLayout.LayoutParams.WRAP_CONTENT);
//
//        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.WRAP_CONTENT,
//                LinearLayout.LayoutParams.WRAP_CONTENT);
//        LinearLayout.LayoutParams params3 = new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.WRAP_CONTENT,
//                LinearLayout.LayoutParams.WRAP_CONTENT);
//
//        // 텍스트뷰 추가
//
//        TextView textView = new TextView(mContext);
//        textView.setText(data.getmTitle());
//        params.width = 180;
//        params.setMargins(0, 4, 0, 4);
//        itemLayout.addView(textView, params);
//
//        // 프로그레스바 추가
//        ProgressBar proBar = new ProgressBar(mContext, null,
//                android.R.attr.progressBarStyleHorizontal);
//        proBar.setIndeterminate(false);
//        proBar.setMax(100);
//        proBar.setProgress(100);
//
//        proBar.setProgressDrawable(
//                mContext.getResources().getDrawable(R.drawable.progressbar_color_black));
//
//        params2.height = 10;
//        params2.width = a* 3;
//        params2.gravity = Gravity.CENTER_VERTICAL;
//        itemLayout.addView(proBar, params2);
//
//        holder.mGL.addView(itemLayout, params3);

        return view;
    }

    private void addItem(int value) {

    }
}
