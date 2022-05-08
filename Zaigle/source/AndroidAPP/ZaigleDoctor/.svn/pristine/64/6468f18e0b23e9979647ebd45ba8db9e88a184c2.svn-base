package com.matescorp.system.zaigle.settingView;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import android.widget.TextView;

import com.matescorp.system.zaigle.R;
import com.matescorp.system.zaigle.data.MarketVersionChecker;

/**
 * Created by sjkim on 17. 7. 12.
 */

public class VersionActivity extends AppCompatActivity {

    private BackThread mBackThread ;
    private String deviceVersion;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_version);


        TextView title = (TextView)findViewById(R.id.text_app_title);
        title.setText(R.string.vsersion);
        ImageView back = (ImageView)findViewById(R.id.icon_menu_view);
        back.setImageDrawable(getResources().getDrawable(R.drawable.back_icon));
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        TextView version = (TextView)findViewById(R.id.versiontext);
        try {
        deviceVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        }catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
        }
        version.setText(deviceVersion);


    }

    public class BackThread extends Thread {
        @Override
        public void run() {
            String storeVersion = MarketVersionChecker.getMarketVersion("kr.co.netseason.myclebot");

            try {
                deviceVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            }catch (PackageManager.NameNotFoundException e){
                e.printStackTrace();
            }

        }
    }


}
