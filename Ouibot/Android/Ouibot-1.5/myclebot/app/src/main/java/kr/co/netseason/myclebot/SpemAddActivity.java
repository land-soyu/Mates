package kr.co.netseason.myclebot;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONObject;

import kr.co.netseason.myclebot.Logger.Logger;
import kr.co.netseason.myclebot.Provider.SecureProvider;
import kr.co.netseason.myclebot.Provider.SecureSQLiteHelper;
import kr.co.netseason.myclebot.openwebrtc.Config;

/**
 * Created by tbzm on 15. 9. 24.
 */
public class SpemAddActivity extends Activity {

    private EditText edittextid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_spemadd);
        edittextid = (EditText) findViewById(R.id.edittextid);
        String str = getIntent().getStringExtra("spemNumber");
        if ( str == null ) {
            str = "";
        }
        edittextid.setText(str);
        ImageView keypad_back = (ImageView) findViewById(R.id.keypad_back);
        keypad_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(-1);
                finish();
            }
        });
        Button spemadd_button = (Button) findViewById(R.id.spemadd_button);
        spemadd_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String number = edittextid.getText().toString().trim();
                if (number.equals("")) {
                    Toast.makeText(SpemAddActivity.this, getResources().getString(R.string.input_id), Toast.LENGTH_SHORT).show();
                } else if ( number.length() != 8 ) {
                    Toast.makeText(SpemAddActivity.this, getResources().getString(R.string.id_8_letter_re_enter), Toast.LENGTH_SHORT).show();
                } else {
                    if ( spemNumberCatch(number) ) {
                        Toast.makeText(SpemAddActivity.this, getResources().getString(R.string.registration_contact), Toast.LENGTH_SHORT).show();
                    } else {
                        addDBSpemItem(number);
                    }
                }
            }
        });
    }

    public void addDBSpemItem(String id) {
        ContentValues values = new ContentValues();
        values.put(SecureSQLiteHelper.COL_PEER_RTCID, id);
        ContentResolver resolver = getContentResolver();
        Uri uri = resolver.insert(SecureProvider.SPEM_TABLE_URI, values);



        setResult(222);
        Toast.makeText(SpemAddActivity.this, getResources().getString(R.string.number_register), Toast.LENGTH_SHORT).show();
        finish();
    }

    private boolean spemNumberCatch(String number) {
        Logger.e("!!!", "spemNumberCatch");
        boolean returnFlag = false;
        try {
            Logger.e("!!!", "number = " + number);

            ContentResolver resolber = getContentResolver();
            Cursor c = resolber.query(SecureProvider.SPEM_TABLE_URI,
                    SecureSQLiteHelper.TABLE_SPEM_ALL_COLUMNS,
                    SecureSQLiteHelper.COL_PEER_RTCID + " = ? ",
                    new String[]{number},
                    "");

            if (c != null && c.moveToFirst()) {
                try {
                    do {
                        Logger.e("!!!", "number = " + c.getString(c.getColumnIndex(SecureSQLiteHelper.COL_PEER_RTCID)));
                        if (number.equals(c.getString(c.getColumnIndex(SecureSQLiteHelper.COL_PEER_RTCID)))) {
                            returnFlag = true;
                        }
                    } while (c.moveToNext());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    c.close();

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return returnFlag;
    }

}
