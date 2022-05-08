package kr.co.netseason.myclebot.View;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import kr.co.netseason.myclebot.R;

public class ContactModifyDialog extends Dialog {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e("!!!", "ContactModifyDialog is onCreate");

		WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
		lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
		lpWindow.dimAmount = 0.8f;
		getWindow().setAttributes(lpWindow);

		setContentView(R.layout.dialog_contect_modify);

		setLayout();
		setTitle(titleStr);
		setContent(noStr, centerStr, okStr);
		setClickListener(mLeftClickListener, mCenterClickListener, mRightClickListener);
	}

	public ContactModifyDialog(Context context, String title, String noStr, String centerStr, String okStr, View.OnClickListener leftListener, View.OnClickListener centerListener, View.OnClickListener rightListener) {
		super(context , android.R.style.Theme_Translucent_NoTitleBar);
		this.titleStr = title;
		this.noStr = noStr;
		this.centerStr = centerStr;
		this.okStr = okStr;
		this.mLeftClickListener = leftListener;
		this.mCenterClickListener = centerListener;
		this.mRightClickListener = rightListener;
	}

	private void setTitle(String titlestr){
		this.title.setText(titlestr);
	}

	private void setContent(String noStr, String centerStr, String okStr){
		popupno_text.setText(noStr);
		popupcenter_text.setText(centerStr);
		popupok_text.setText(okStr);
	}

	private void setClickListener(View.OnClickListener left, View.OnClickListener center, View.OnClickListener right){
		if ( left != null ) {
			popupno.setOnClickListener(left);
		} else {
			popupno.setVisibility(View.GONE);
		}
		if ( center != null ) {
			popupcenter.setOnClickListener(center);
		} else {
			popupcenter.setVisibility(View.GONE);
		}
		if ( right != null ) {
			popupok.setOnClickListener(right);
		} else {
			popupok.setVisibility(View.GONE);
		}
	}

	private TextView title;
	private TextView popupno_text;
	private TextView popupcenter_text;
	private TextView popupok_text;
	private String titleStr;
	private String noStr;
	private String centerStr;
	private String okStr;
	private LinearLayout popupno;
	private LinearLayout popupcenter;
	private LinearLayout popupok;

	private View.OnClickListener mLeftClickListener;
	private View.OnClickListener mCenterClickListener;
	private View.OnClickListener mRightClickListener;

	/*
     * Layout
     */
	private void setLayout(){
		title = (TextView) findViewById(R.id.data);
		popupno_text = (TextView) findViewById(R.id.popupno_text);
		popupcenter_text = (TextView) findViewById(R.id.popupcenter_text);
		popupok_text = (TextView) findViewById(R.id.popupok_text);
		popupno = (LinearLayout) findViewById(R.id.popupno);
		popupcenter = (LinearLayout) findViewById(R.id.popupcenter);
		popupok = (LinearLayout) findViewById(R.id.popupok);
	}

}