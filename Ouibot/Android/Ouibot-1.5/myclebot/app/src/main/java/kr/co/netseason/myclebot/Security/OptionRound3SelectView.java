package kr.co.netseason.myclebot.Security;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import kr.co.netseason.myclebot.R;
import kr.co.netseason.myclebot.openwebrtc.Config;

/**
 * Created by tbzm on 15. 10. 12.
 */
public class OptionRound3SelectView extends LinearLayout implements View.OnClickListener {
    private final String TAG = OptionRound3SelectView.this.getClass().getName();
    private Context mContext;
    private RelativeLayout mBtn01;
    private RelativeLayout mBtn02;
    private RelativeLayout mBtn03;

    private TextView mText01;
    private TextView mText02;
    private TextView mText03;

    private int VALUE_COUNT = 3;
    private ImageView mSelectImageView;

    private TextView[] mTextViewArray = {mText01, mText02, mText03};
    private int[] mTextViewResouceArray = {R.id.text_01, R.id.text_02, R.id.text_03};
    private int[] mSelectImageResourceArray = {R.drawable.bg_contents_handle3_1, R.drawable.bg_contents_handle3_2, R.drawable.bg_contents_handle3_3};
    private int[] mDataList = new int[VALUE_COUNT];
    private SelectedOnData mListner;
    private int mType;

    public interface SelectedOnData {
        public void dataSelected(int value);
    }

    public OptionRound3SelectView(Context context, SelectedOnData changed, int type) {
        super(context);
        mContext = context;
        mType = type;
        mListner = changed;
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.setting_round_3choice_bar, this, true);
        mBtn01 = (RelativeLayout) findViewById(R.id.btn_01);
        mBtn01.setOnClickListener(this);
        mBtn02 = (RelativeLayout) findViewById(R.id.btn_02);
        mBtn02.setOnClickListener(this);
        mBtn03 = (RelativeLayout) findViewById(R.id.btn_03);
        mBtn03.setOnClickListener(this);

        mSelectImageView = (ImageView) findViewById(R.id.button_image);

        for (int i = 0; i < mTextViewArray.length; i++) {
            mTextViewArray[i] = (TextView) findViewById(mTextViewResouceArray[i]);
        }
    }

    public void setDefalutValue(int[] data, int seletPosition) {
        if (data != null) {
            if (data.length == VALUE_COUNT) {
                for (int i = 0; i < data.length; i++) {
                    String timeStrin = "";
                    if (mType == Config.SECURE_ROUND_DATA_TYPE_TIME) {
                        int ours = data[i] / 3600;
                        int minutes = (data[i] % 3600) / 60;
                        int seconds = data[i] % 60;
                        if (ours > 0) {
                            timeStrin = String.valueOf(ours) + getResources().getString(R.string.hour);
                        } else if (minutes > 0) {
                            timeStrin = String.valueOf(minutes) + getResources().getString(R.string.minute);
                        } else {
                            timeStrin = String.valueOf(seconds) + getResources().getString(R.string.second);
                        }
                    } else {
                        if (data[i] == 0) {
                            timeStrin = getResources().getString(R.string.secure_middle);
                        } else if (data[i] == 1) {
                            timeStrin = getResources().getString(R.string.secure_strong);
                        } else {
                            timeStrin = getResources().getString(R.string.secure_weakness);
                        }
                    }
                    mTextViewArray[i].setText(timeStrin);
                    mDataList[i] = data[i];
                }
            }
        }
        setSelectImageRotate(seletPosition);
    }

    public void seValue(int position) {
        setSelectImageRotate(position);
    }

    public void setSelectImageRotate(int position) {
        if (mSelectImageView != null && mSelectImageResourceArray.length >= position) {
            mSelectImageView.setImageResource(mSelectImageResourceArray[position]);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_01:
                setSelectImageRotate(0);
                mListner.dataSelected(mDataList[0]);
                break;
            case R.id.btn_02:
                setSelectImageRotate(1);
                mListner.dataSelected(mDataList[1]);
                break;
            case R.id.btn_03:
                setSelectImageRotate(2);
                mListner.dataSelected(mDataList[2]);
                break;
        }
    }

}
