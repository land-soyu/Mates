package com.matescorp.system.zaigle;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.matescorp.system.zaigle.data.ProfilePreferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * Created by tbzm on 16. 5. 4.
 */
public class IntroActivity extends Activity {

    private Handler mHandler;
    private int delay = 3000;
    private LinearLayout main;
    private ImageView logo;
    private LinearLayout main2;
    private ImageView logo2;
    static final int ACTION_ENABLE_BT = 101;
    private BluetoothAdapter mBt = BluetoothAdapter.getDefaultAdapter();
    final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 102;



    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_intro);
        context = this;
        ProfilePreferences.PREF = PreferenceManager.getDefaultSharedPreferences(this);
        main = (LinearLayout)findViewById(R.id.main);
        logo = (ImageView)findViewById(R.id.logo);
        main2 = (LinearLayout)findViewById(R.id.main2);
        logo2 = (ImageView)findViewById(R.id.logo2);
        if (Build.VERSION.SDK_INT >= 23) {
            // Marshmallow+ Permission APIs
            Log.e("intro ", " Build.VERSION.SDK_INT >= 23 ");
            fuckMarshMallow();
            ProfilePreferences.setPermission("0");
        } else {
            Log.e("intro ", " Build.VERSION.SDK_INT >= 23  else");
            ProfilePreferences.setPermission("1");
            canUseBluetooth();
        }


    }







@Override
public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    switch (requestCode) {
        case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
            Map<String, Integer> perms = new HashMap<String, Integer>();
            // Initial
            perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);


            // Fill with results
//            for (int i = 0; i < permissions.length; i++)
//                perms.put(permissions[i], grantResults[i]);

            // Check for ACCESS_FINE_LOCATION
            if (perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // All Permissions Granted
                // Permission Denied
                Toast.makeText(IntroActivity.this, "????????? ?????? ???????????????.", Toast.LENGTH_SHORT)
                        .show();
                init();
                Log.e("intro ", " ????");
            } else {
                // Permission Denied
                Toast.makeText(IntroActivity.this, "????????? ??????????????????", Toast.LENGTH_SHORT)
                        .show();

                finish();
            }
        }
        break;
        default:
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}


    @TargetApi(Build.VERSION_CODES.M)
    private void fuckMarshMallow() {
        Log.e("intro ", " fuckMarshMallow");

        List<String> permissionsNeeded = new ArrayList<String>();

        final List<String> permissionsList = new ArrayList<String>();
        if (!addPermission(permissionsList, Manifest.permission.ACCESS_FINE_LOCATION))
            permissionsNeeded.add("Show Location");

        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {

//                if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
//                    Log.d("sjkim", "> Build.VERSION_CODES.LOLLIPOP");
//                    int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
//                    permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
//                    if (permissionCheck != 0) {
//
//                        this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS); //Any number
//                    }
//                }else{
//                    Log.d("sjkim", "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
//                }

                // Need Rationale
                String message = "?????????  " + permissionsNeeded.get(0) + " ????????? ???????????????.";

                for (int i = 1; i < permissionsNeeded.size(); i++)
                    message = message + ", " + permissionsNeeded.get(i);

                showMessageOKCancel(message,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                                        REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                            }
                        });
                return;
            }
            requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            return;
        }

//        Toast.makeText(IntroActivity.this, "No new Permission Required- Launching App .You are Awesome!!", Toast.LENGTH_SHORT)
//                .show();
               init();
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(IntroActivity.this)
                .setMessage(message)
                .setPositiveButton("??????", okListener)
                .setNegativeButton("??????", null)
                .create()
                .show();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean addPermission(List<String> permissionsList, String permission) {

        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!shouldShowRequestPermissionRationale(permission))
                return false;
        }
        return true;
    }







    public boolean canUseBluetooth(){
        Log.e("intro ", " canUseBluetooth ");
        mBt = BluetoothAdapter.getDefaultAdapter();
        if(mBt == null){
            Log.e("intro BT ", " BT NOT FOUND ");
            finish();
        }

        if(mBt.isEnabled()){
            Log.e("intro BT ", " BT ON ");
//            if(ProfilePreferences.getPermission().equals("0")){
//            }else{
                init();
//            }
            return true;
        }

        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent, ACTION_ENABLE_BT);
        return false;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("intro BT ", " onActivityResult ");
        if( requestCode == ACTION_ENABLE_BT ) {
            // ???????????? ???????????? ????????? ???????????????
            if( resultCode == RESULT_OK ) {
                // ???????????? ?????? ???????????? ?????? ?????????
//                if(ProfilePreferences.getPermission().equals("1")) {
                    init();
//                }
            }
            // ???????????? ???????????? ????????? ???????????????
            else {
                Toast.makeText(context, "???????????? ????????? ????????? ?????? ????????? ?????? ?????? ??? ??? ????????????.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }


    private void init(){
        ProfilePreferences.setPermission("1");
        if(ProfilePreferences.getRadiomode() == null){
            Log.e("intro" , "ProfilePreferences.getRadiomode() === " + ProfilePreferences.getRadiomode());
        }else {
            String A = ProfilePreferences.getRadiomode().toString();
            setLocale(A);
        }

        mHandler = new Handler();
        mHandler.postDelayed(mrun, delay);
    }

    Runnable mrun = new Runnable() {
        @Override
        public void run() {
            Intent i;

            Log.e("intro" , "ProfilePreferences.getBirth() ===== "+ ProfilePreferences.getBirth()) ;
            if (ProfilePreferences.getBirth()==null){
                i = new Intent(IntroActivity.this, Profile.class);
                i.putExtra("flag", 1);

            }else {
                i = new Intent(IntroActivity.this, MainActivity.class);

            }
            startActivity(i);
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        }
    };

    public void setLocale(String charicter) {   // 27. ?????? ???????????? ?????? ?????????
        Log.e("intro" , "setLocale === " + charicter);
        Locale locale = new Locale(charicter);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if ( mHandler != null ) {
            mHandler.removeCallbacks(mrun);
        }
    }
}
