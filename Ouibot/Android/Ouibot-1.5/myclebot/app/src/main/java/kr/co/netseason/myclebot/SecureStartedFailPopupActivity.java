package kr.co.netseason.myclebot;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by tbzm on 15. 11. 18.
 */
public class SecureStartedFailPopupActivity extends Activity implements View.OnClickListener {
    private Button mShortLeft, mShortOk;
    private TextView mContentString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_popup_layout);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mContentString = (TextView) findViewById(R.id.content_text);
        mContentString.setText(getResources().getString(R.string.sdcard_memory_limit_popop_content));
        mShortLeft = (Button) findViewById(R.id.short_left);
        mShortLeft.setOnClickListener(this);
        mShortLeft.setVisibility(View.GONE);
        mShortOk = (Button) findViewById(R.id.short_right);
        mShortOk.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.short_left:
                finish();
                break;
            case R.id.short_right:
                finish();
                break;
            default:
                break;
        }
    }
}
