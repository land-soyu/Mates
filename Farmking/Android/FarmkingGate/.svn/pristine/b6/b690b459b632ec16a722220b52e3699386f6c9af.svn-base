package com.matescorp.soyu.farmkinggate.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.matescorp.soyu.farmkinggate.R;


/**
 * Created by soyu on 17. 9. 25.
 */
public class IntroActivity extends Activity {

    private Handler mHandler;
    private int delay = 2000;

    private EditText input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_intro);




        if ( getSerialNum().equals("") ) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);

            alert.setTitle(getResources().getString(R.string.check_serial));

            input = new EditText(this);
            alert.setView(input);

            alert.setPositiveButton(getResources().getString(R.string.check_serial_ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String value = input.getText().toString();
                    Toast.makeText(IntroActivity.this, getResources().getString(R.string.check_serial_ok_message)+"("+value+")", Toast.LENGTH_SHORT).show();
                    setSerialNum(value);
                    mHandler = new Handler();
                    mHandler.postDelayed(mrun, delay);
                }
            });

            alert.setNegativeButton(getResources().getString(R.string.check_serial_cencal),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Toast.makeText(IntroActivity.this, getResources().getString(R.string.check_serial_cencal_message), Toast.LENGTH_SHORT).show();
                            finish();
                            IntroActivity.this.finish();
                        }
                    });
            alert.show();
        } else {
            mHandler = new Handler();
            mHandler.postDelayed(mrun, delay);
        }
    }

    Runnable mrun = new Runnable() {
        @Override
        public void run() {
            Intent i;
                i = new Intent(IntroActivity.this, MainActivity.class);
            startActivity(i);
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if ( mHandler != null ) {

        }
        mHandler.removeCallbacks(mrun);
    }


    private String getSerialNum() {
        SharedPreferences pref = getSharedPreferences("farmkinggate", MODE_PRIVATE);
        return pref.getString("serialnum", "");
    }
    private void setSerialNum(String serialnum){
        SharedPreferences pref = getSharedPreferences("farmkinggate", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("serialnum", serialnum);
        editor.commit();
    }
}
