package kr.co.netseason.myclebot.Security;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import kr.co.netseason.myclebot.R;

/**
 * Created by tbzm on 15. 10. 13.
 */
public class CustomDialog extends Dialog implements View.OnClickListener {
    private TextView mContentTextView;
    private Button mLeftButton;
    private Button mCenterButton;
    private Button mRightButton;
    private OnDialogListener mDialogListener;

    public static final int SINGLE_BUTTON_TYPE = 0;
    public static final int TWO_BUTTON_TYPE = 1;
    public static final int THREE_BUTTON_TYPE = 2;


    public interface OnDialogListener {
        void OnLeftClicked(View v);

        void OnCenterClicked(View v);

        void OnRightClicked(View v);

        void OnDismissListener();
    }

    public CustomDialog(Context context, String content, String leftBtnText, String centerBtnText, String rightBtnText,
                        OnDialogListener dialogListener, int type) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);
        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.8f;
        getWindow().setAttributes(lpWindow);
        setContentView(R.layout.popup_layout);
        mContentTextView = (TextView) findViewById(R.id.content_text);
        mContentTextView.setText(content);
        mLeftButton = (Button) findViewById(R.id.short_left);
        mLeftButton.setOnClickListener(this);
        mLeftButton.setText(leftBtnText);
        mCenterButton = (Button) findViewById(R.id.short_center);
        mCenterButton.setOnClickListener(this);
        mCenterButton.setText(centerBtnText);
        mRightButton = (Button) findViewById(R.id.short_right);
        mRightButton.setOnClickListener(this);
        mRightButton.setText(rightBtnText);
        if (type == SINGLE_BUTTON_TYPE) {
            mLeftButton.setVisibility(View.GONE);
            mCenterButton.setVisibility(View.GONE);
        } else if (type == TWO_BUTTON_TYPE) {
            mLeftButton.setVisibility(View.GONE);
        }
        setOnDismissListener(mDismissListener);
        mDialogListener = dialogListener;
        try {
            show();
        }catch (WindowManager.BadTokenException e){
            e.printStackTrace();
        }
    }

    OnDismissListener mDismissListener = new OnDismissListener() {
        @Override
        public void onDismiss(DialogInterface dialog) {
            mDialogListener.OnDismissListener();
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.short_left:
                mDialogListener.OnLeftClicked(v);
                break;
            case R.id.short_center:
                mDialogListener.OnCenterClicked(v);
                break;
            case R.id.short_right:
                mDialogListener.OnRightClicked(v);
                break;
        }
    }
}

