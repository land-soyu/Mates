package com.matescorp.system.zaigle;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by sjkim on 17. 7. 13.
 */

public class GuestMainActivity extends AppCompatActivity {

    private Handler mHandler;
    private int delay = 2000;
    private TextView ggg;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guest);

        ImageView back = (ImageView)findViewById(R.id.icon_menu_view_u);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        ggg =(TextView)findViewById(R.id.gggg);

        mHandler = new Handler();
        mHandler.postDelayed(mrun, delay);

        ggg.setText("측정중....");

    }

    Runnable mrun = new Runnable() {
        @Override
        public void run() {
            ggg.setText("측정완료 결과화면");
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mHandler.removeCallbacks(mrun);
    }
}


