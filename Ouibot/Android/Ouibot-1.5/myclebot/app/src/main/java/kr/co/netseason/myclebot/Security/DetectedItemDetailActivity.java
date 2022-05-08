package kr.co.netseason.myclebot.Security;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import kr.co.netseason.myclebot.Logger.Logger;
import kr.co.netseason.myclebot.R;

/**
 * Created by tbzm on 15. 10. 19.
 */
public class DetectedItemDetailActivity extends Activity {
    Button mPolice;
    Button mFire;
    ImageView mDetailImageView;
    ImageView mDetailBack;
    TextView mOuibotText;
    TextView mEventData;
    TextView mSavePath;
    TextView mFileName;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        initUI();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUI();

    }

    private void initUI() {
        setContentView(R.layout.detected_item_detail_activity);
        Intent intent = getIntent();
        String path = intent.getStringExtra("image_path_key");
        String id = intent.getStringExtra("image_id_key");
        long eventDate = intent.getLongExtra("image_time_key", 0);

        Logger.d("sunyung", "path = " + path);
        Logger.d("sunyung", "time = " + eventDate);
        Logger.d("sunyung", "id = " + id);

        mOuibotText = (TextView) findViewById(R.id.ouibot_id_text);
        mOuibotText.setText(id);
        mEventData = (TextView) findViewById(R.id.event_date);
        mEventData.setText(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(eventDate)));
        mSavePath = (TextView) findViewById(R.id.save_path);
        mSavePath.setText(path);
        mFileName = (TextView) findViewById(R.id.file_name_tv);
        mFileName.setText(getFileName(path));
        mDetailBack = (ImageView) findViewById(R.id.keypad_back);
        mDetailBack.setOnClickListener(mOnCLick);
        mDetailImageView = (ImageView) findViewById(R.id.detail_image);
        mPolice = (Button) findViewById(R.id.btn_police);
        mPolice.setOnClickListener(mOnCLick);
        mFire = (Button) findViewById(R.id.btn_fire);
        mFire.setOnClickListener(mOnCLick);
        Glide.with(this)
                .load(path)
                .placeholder(R.drawable.no_image)
                .into(mDetailImageView);
    }

    private String getFileName(String path) {
        if (path.length() >= 2)
            return path.split(File.separator)[path.split(File.separator).length - 1];
        return null;
    }

    View.OnClickListener mOnCLick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.btn_police:
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:112"));
                    startActivity(intent);
                    break;
                case R.id.btn_fire:
                    Intent intent1 = new Intent(Intent.ACTION_CALL, Uri.parse("tel:119"));
                    startActivity(intent1);
                    break;
                case R.id.keypad_back:
                    finish();
                    break;

            }

        }
    };

}
