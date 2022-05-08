package kr.co.netseason.myclebot;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;

import kr.co.netseason.myclebot.UTIL.OuiBotPreferences;


public class DeviceAddResultActivity extends FragmentActivity {

    private Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_add_result);
        context = this;

        TextView mText = (TextView)findViewById(R.id.mText);

        String str = "";
        if ( OuiBotPreferences.getLoginId(context) == null ) {
            str += getResources().getString(R.string.device_add_result_data_4);
        } else {
            str += getResources().getString(R.string.device_add_result_data_1);
            str += OuiBotPreferences.getLoginId(context);
            str += getResources().getString(R.string.device_add_result_data_2);
        }

        mText.setText(str);
    }


    public void onCreateIDClicked(final View view) {

        if ( OuiBotPreferences.getLoginId(context) == null ) {
            startActivity(new Intent(context, DeviceAddInitActivity.class));
        } else {
            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra("init", true);
            startActivity(intent);
        }
        finish();

    }

}
