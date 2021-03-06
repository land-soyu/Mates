package com.matescorp.soyu.farmkinggate.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.matescorp.soyu.farmkinggate.R;
import com.matescorp.soyu.farmkinggate.activity.SensorItemActivity;

import java.util.ArrayList;

/**
 * Created by soyu on 17. 9. 25.
 */

public class FarmkingListViewAdapter extends BaseAdapter {
    private ArrayList<ListViewItem> listViewItemList = new ArrayList<ListViewItem>() ;

    public FarmkingListViewAdapter() {
    }
    public FarmkingListViewAdapter(ArrayList<ListViewItem> list) {
        listViewItemList = list;
    }

    public void addItem(ListViewItem item) {
        listViewItemList.add(item);
    }

    @Override
    public int getCount() {
        return listViewItemList.size();
    }

    @Override
    public Object getItem(int position) {
        return listViewItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Context context = parent.getContext();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_content, parent, false);
        }

        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
        TextView list_text_no = (TextView) convertView.findViewById(R.id.list_text_no) ;
        TextView list_text_history_no = (TextView) convertView.findViewById(R.id.list_text_history_no) ;
        TextView list_text_ararm = (TextView) convertView.findViewById(R.id.list_text_ararm) ;
        TextView list_text_temp = (TextView) convertView.findViewById(R.id.list_text_temp) ;
        TextView list_text_move = (TextView) convertView.findViewById(R.id.list_text_move) ;

        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        ListViewItem listViewItem = listViewItemList.get(position);

        if ( listViewItem != null ) {
            // 아이템 내 각 위젯에 데이터 반영
            list_text_no.setText(listViewItem.getNo());
            if ( listViewItem.getHistory_no().equals("-") ) {
                list_text_no.setBackgroundColor(Color.RED);
            }
            list_text_history_no.setText(listViewItem.getHistory_no());
            list_text_ararm.setText(listViewItem.getArarm()+"건");
            list_text_temp.setText(listViewItem.getTemp()+"℃");
            list_text_move.setText(listViewItem.getMove()+"g");
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView list_text_no = (TextView) v.findViewById(R.id.list_text_no);
                String id = list_text_no.getText().toString();

                Intent intent = new Intent(context, SensorItemActivity.class);
                intent.putExtra("id", id);
                context.startActivity(intent);
            }
        });
        return convertView;
    }
}
