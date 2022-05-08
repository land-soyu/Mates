package kr.co.netseason.myclebot;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import kr.co.netseason.myclebot.openwebrtc.Config;

/**
 * Created by tbzm on 15. 9. 29.
 */
public class MasterSettingResultActivity extends Activity implements View.OnClickListener {

    private Button mShortLeft, mShortRight;
    private String TAG = getClass().getName();
    private TextView mContentString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_popup_layout);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mContentString = (TextView) findViewById(R.id.content_text);
        mContentString.setText(getIntentData() + " " + getResources().getString(R.string.confirm_master));
        mShortLeft = (Button) findViewById(R.id.short_left);
        mShortLeft.setOnClickListener(this);
        mShortLeft.setVisibility(View.GONE);
        this.setFinishOnTouchOutside(false);
        mShortRight = (Button) findViewById(R.id.short_right);
        mShortRight.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.short_right:
                finish();
                try {
                    UibotAddActivity.INSTANCE.finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }

    public String getIntentData() {
        Intent intent = getIntent();
        return intent.getStringExtra(Config.MASTER_SETTING_RESULT);
    }

}
