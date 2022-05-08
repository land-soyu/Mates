package kr.co.netseason.myclebot;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;


public class DeviceAddInitActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_add_init);
    }

    public void onCheckIdClicked(final View view) {
        startActivity(new Intent(this, DeviceAddActivity.class));
        finish();
    }

}
