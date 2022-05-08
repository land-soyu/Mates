package com.matescorp.system.zaigle.Bluetooth;

import android.view.View;

public class DeviceView {
	public String address;
	public View view;
	
	public DeviceView(String add, View v) {
		address = add;
		view = v;
	}
}
