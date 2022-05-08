package com.matescorp.system.zaigle.Bluetooth;

import android.content.Context;
import android.os.Messenger;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.matescorp.system.zaigle.BTCon;
import com.matescorp.system.zaigle.R;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;


//[Olive:130723]
public class DeviceMenuAdapter extends BaseAdapter implements View.OnClickListener {
	
	static final String TAG = "DeviceMenuAdapter";

	//static final String STR_REGISTER = "Register";
	static final String STR_FINDING = "Finding";
	static final String STR_FIND = "Find";
	static final String STR_STOP = "Stop";
	static final String STR_CONNECT = "Connect";

	Context context = null;
	List<BTDevice> mDevices;
	Messenger mainMessenger;
	Button find;

	static final String UNINITIALIZED = "Click on the \"Connect\" button ====>";

	Hashtable<Integer, DeviceView> Views = new Hashtable<Integer, DeviceView>();

	public DeviceMenuAdapter(Context c, List<BTDevice> devices, Messenger m)
	{
		context = c;
		mDevices = devices;
		mainMessenger = m;
	}
	
	public void setListItems(List<BTDevice> lit) {
		//Views.clear();
		mDevices = lit; 
	}

	@Override
	public int getCount() {
		return mDevices.size();
	}

	@Override
	public Object getItem(int position) {
		return mDevices.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		BTDevice tag = mDevices.get(position);
        if( tag == null) {
        	return convertView;
        }
		
		if( convertView == null) {
			LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = mInflater.inflate(R.layout.device_menu_row,  null);
		}else {
			int pos = (Integer) convertView.getTag();
			if( Views.containsKey(pos)) {
				Views.remove(pos);
			}
		}
 		
		Views.put(position, new DeviceView(tag.getDevice().getAddress(), convertView));
		find = (Button)convertView.findViewById(R.id.find);
//
		find.setTag(position);
		find.setOnClickListener(this);

		convertView.setTag(position);
		convertView.setOnClickListener(this);
		
		DrawTagRow(tag, convertView);

        return convertView;
	}

	@Override
	public void onClick(View v) {
		int position = Integer.parseInt(v.getTag().toString());
		BTDevice tag = mDevices.get(position);
		if( tag == null ) {
			return;
		}

		switch(v.getId()) {
			case R.id.find: {

				Button viFind = (Button) v;
				String text = viFind.getText().toString();

				Log.e(TAG, "text = "+text);
				Log.e(TAG, "tag.getBT_state() = "+tag.getBT_state());
				switch (tag.getBT_state()) {
					case BTDevice.BT_STATE_DISCONNECT:
						tag.setBT_state(BTDevice.BT_STATE_CONNECTING);
						BTConfig.sendMessage(mainMessenger, BTConfig.SP_BT_CONNECT, 1, 0, tag.getDevice());
						break;
					case BTDevice.BT_STATE_CONNECT:
						break;
					case BTDevice.BT_STATE_CONNECTING:
						break;
					case 3:
						break;

				}
				break;
			}

			default:
//				MsgDefine.sendMessageMain(MsgDefine.MID_OPEN_TAG_SET, 0, 0, tag);
			break;
		
		}
	}
	
	
	private DeviceView getDeviceView(String address) {
		DeviceView dView = null;
		
		Enumeration<Integer> enumerationKey = Views.keys();
		while (enumerationKey.hasMoreElements()) {
			Integer key = (Integer) enumerationKey.nextElement();
			dView = this.Views.get(key);
			if( dView.address.equals(address)) {
				return dView;
			}
		}
		
		return null;
	}

//	public void UpdateTagRow(String address) {
//
//		ProximityTag tag = bt.GetTagByAddress(address);
//		if(tag == null) {
//			return;
//		}
//
//		//Log.d(TAG, "===== Update_Tag_Row : " + tag.blueDevice.getName() + "=====");
//		DeviceView rowView = getDeviceView(tag.getAddress());
//
//    	if(rowView != null) {
//			DrawTagRow(tag, rowView.view);
//		}
//    }

	private void DrawTagRow(BTDevice tag, View v) {
		if( tag == null || v == null) return;

		ProgressBar viCircle = (ProgressBar) v.findViewById(R.id.circleBar);


		TextView viName = (TextView) v.findViewById(R.id.textDev);
		viName.setText(tag.getDevice().getName());

		TextView viStatus = (TextView) v.findViewById(R.id.textCon);
		switch (tag.getBT_state()) {
			case 0:
				viStatus.setText("click the button");
				break;
			case 1:
				viStatus.setText("success");
				viCircle.setVisibility(View.GONE);
				find.setVisibility(View.GONE);
				break;
			case 2:
				viStatus.setText("connecting");
				viCircle.setVisibility(View.VISIBLE);
				break;
			case 3:
				break;
		}

	}
//	public int GetSignalResouceID(int rssi) {
//		if(rssi == 0) {
//			return R.drawable.signal_0;
//		} else if(rssi>= -60) {
//			return R.drawable.signal_5;
//		}else if( rssi < -60 && rssi >= -70) {
//			return R.drawable.signal_4;
//		}else if( rssi < -70 && rssi >= -80) {
//			return R.drawable.signal_3;
//		}else if( rssi < -80 && rssi >= -90) {
//			return R.drawable.signal_2;
//		}else if( rssi < -90 && rssi >= -100) {
//			return R.drawable.signal_1;
//		}else {
//			return R.drawable.signal_0;
//		}
//	}

}

