package kr.co.netseason.myclebot;

import android.Manifest;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.tools.debugger.Main;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import kr.co.netseason.myclebot.API.MessageListData;
import kr.co.netseason.myclebot.Logger.Logger;
import kr.co.netseason.myclebot.Provider.SecureProvider;
import kr.co.netseason.myclebot.Provider.SecureSQLiteHelper;
import kr.co.netseason.myclebot.Security.ForIOSNotificationHttpAsyncTask;
import kr.co.netseason.myclebot.Security.SecureDetectedListActivity;
import kr.co.netseason.myclebot.Security.UibotListData;
import kr.co.netseason.myclebot.UTIL.OuiBotPreferences;
import kr.co.netseason.myclebot.UTIL.TabPagerAdapter;
import kr.co.netseason.myclebot.UTIL.UIUtil;
import kr.co.netseason.myclebot.View.ContactView;
import kr.co.netseason.myclebot.View.HistoryView;
import kr.co.netseason.myclebot.View.LinkOuibotSelfSettingView;
import kr.co.netseason.myclebot.View.LinkSettingView;
import kr.co.netseason.myclebot.View.MessageView;
import kr.co.netseason.myclebot.openwebrtc.CallActivity;
import kr.co.netseason.myclebot.openwebrtc.Config;
import kr.co.netseason.myclebot.openwebrtc.SignalingChannel;

import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public class MainActivity extends FragmentActivity implements ContactView.onContactCallClickListener, HistoryView.onHistoryCallClickListener {
    public static MainActivity CONTEXT;
    private final String TAG = getClass().getName();
    private long backKeyPressedTime;
    private Toast toast;
    public static WeakHashMap<Integer, Fragment> mFragments = new WeakHashMap<Integer, Fragment>();
    public static WeakHashMap<Integer, Fragment> settingFragments = new WeakHashMap<Integer, Fragment>();

    private Messenger mService;
    private Messenger messenger;

    private TabPagerAdapter tabPagerAdapter;
    private ViewPager viewPager;
    private ViewPager viewPager2;

    private LinearLayout contactmenu;
    private LinearLayout historymenu;
    private LinearLayout messagemenu;
    private LinearLayout linksettingmenu;

    private ImageView contactmenu_icon;
    private ImageView historymenu_icon;
    private ImageView messagemenu_icon;
    private ImageView linksettingmenu_icon;

    private TextView contactmenu_text;
    private TextView historymenu_text;
    private TextView messagemenu_text;
    private TextView linksettingmenu_text;

    private LinearLayout contactmenu_line;
    private LinearLayout historymenu_line;
    private LinearLayout messagemenu_line;
    private LinearLayout linksettingmenu_line;

    private DrawerLayout dlDrawer;
    private View lv_activity_main_nav_list;
    private IntentFilter mSecureIntentFilter;
    private ImageView content_profile_layout;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        int currentItem = viewPager.getCurrentItem();
        Logger.e("!!!", "MainActivity onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
        initUI();
        viewPager.setCurrentItem(currentItem);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CONTEXT = this;
        mSecureIntentFilter = new IntentFilter(Config.INTENT_ACTION_REFESH_SECURE_PAGER_DATA);
        mSecureIntentFilter.addAction(Config.INTENT_ACTION_SET_SECURE_CONFIG_DATA_ACK);
        mSecureIntentFilter.addAction(Config.INTENT_ACTION_MICRO_SDCARD_ERROR);
        mSecureIntentFilter.addAction(Config.INTENT_ACTION_ALREADY_CONNECTED_PHONE_CALL);
        mSecureIntentFilter.addAction(Config.INTENT_ACTION_PEER_IS_NOT_LOGIN);
        mSecureIntentFilter.addAction(Config.INTENT_ACTION_MESSAGE_TOAST);
        mSecureIntentFilter.addAction(Config.INTENT_RECEIVE_MESSAGE_EVENT);
        switch (Config.Mode) {
            case 1:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                Logger.e("!!!", "error4");
                break;
            case 2:
                break;
            default:
                break;
        }

        registerReceiver(mSecureReceiver, mSecureIntentFilter);
        if (OuiBotPreferences.getLoginId(this) != null) {
            Intent service = new Intent(this, SignalingChannel.class);
            if (!Config.isServiceAlive(this)) {
                bindService(service, conn, Context.BIND_AUTO_CREATE);
                startService(service);
                Logger.e("!!!", "error2");
            } else {
                bindService(service, conn, Context.BIND_AUTO_CREATE);
                Logger.e("!!!", "error3");
            }
        }
        Logger.e("!!!", "oncreate == getIntent().getBooleanExtra(Config.INTENT_MOVE_TO_MESSAGE_VIEW, false) = " + getIntent().getBooleanExtra(Config.INTENT_MOVE_TO_MESSAGE_VIEW, false));
        initUI();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Logger.d(TAG, "onNewIntent call");
        setIntent(intent);
    }

    public void doActionWithIntentData() {
        Logger.e("!!!", "doActionWithIntentData cal");
        Logger.e("!!!", "getIntent().getBooleanExtra(Config.INTENT_MOVE_TO_MESSAGE_VIEW, false) = " + getIntent().getBooleanExtra(Config.INTENT_MOVE_TO_MESSAGE_VIEW, false));

        if (getIntent().getIntExtra("missedcall", -1) > -1) {
            viewPager.setCurrentItem(1);
            getIntent().putExtra("missedcall", -1);
        } else if (getIntent().getBooleanExtra(Config.INTENT_MOVE_TO_LINK_SETTING_VIEW, false)) {
            viewPager.setCurrentItem(3);
            Intent intent = new Intent(this, SecureDetectedListActivity.class);
            intent.putExtra(Config.INTENT_MOVE_TO_LINK_SETTING_OUIBOT_ID, getIntent().getStringExtra(Config.INTENT_MOVE_TO_LINK_SETTING_OUIBOT_ID));
            intent.putExtra(Config.INTENT_MOVE_TO_LINK_SETTING_SECURE_MODE, getIntent().getIntExtra(Config.INTENT_MOVE_TO_LINK_SETTING_SECURE_MODE, Config.DETECT_SECURE_MODE));
            startActivity(intent);
            getIntent().putExtra(Config.INTENT_MOVE_TO_LINK_SETTING_VIEW, false);
        } else if (getIntent().getBooleanExtra(Config.INTENT_MOVE_TO_MESSAGE_VIEW, false)) {
            viewPager.setCurrentItem(2);
            getIntent().putExtra(Config.INTENT_MOVE_TO_MESSAGE_VIEW, false);
        } else if (getIntent().getIntExtra("alram", -1) > -1) {
            Intent intent= null;
            if (Config.Mode == Config.COMPILE_Ouibot) {
                intent = (new Intent(CONTEXT, SettingActivity.class)).putExtra("message", mService);
            } else {
                intent = (new Intent(CONTEXT, SettingAndroidActivity.class)).putExtra("message", mService);
            }

            startActivity(intent.putExtra("alram", getIntent().getIntExtra("alram", -1)));
            getIntent().putExtra("alram", -1);
        } else {
            Logger.e("!!!", "doActionWithIntentData call else");
        }
    }

    public void addDBSpemItem(String id, String path) {
        ContentResolver resolver = getContentResolver();
        Cursor c = resolver.query(SecureProvider.PROFILE_TABLE_URI,
                new String[]{SecureSQLiteHelper.COL_PEER_RTCID},
                SecureSQLiteHelper.COL_PEER_RTCID + " = ? ",
                new String[]{id}, null);
        boolean isRTCID = false;
        if (c != null && c.moveToFirst()) {
            try {
                do {
                    isRTCID = true;
                } while (c.moveToNext());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                c.close();
            }
        }
        ContentValues values = new ContentValues();
        values.put(SecureSQLiteHelper.COL_PEER_RTCID, id);
        values.put(SecureSQLiteHelper.COL_PROFILE, path);
        if(isRTCID){
            resolver.update(SecureProvider.PROFILE_TABLE_URI,values,SecureSQLiteHelper.COL_PEER_RTCID + " = ? ",new String[]{id});
        }else {
            resolver.insert(SecureProvider.PROFILE_TABLE_URI, values);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
//        if (requestCode == PICK_FROM_GALLERY) {
//            Bundle extras2 = data.getExtras();
//            if (extras2 != null) {
//                Bitmap photo = extras2.getParcelable("data");
//                content_profile_layout.setImageBitmap(photo);
//                addDBSpemItem(OuiBotPreferences.getLoginId(getApplicationContext()), content_profile_layout);
//            }
//        }
        if (requestCode == PICK_FROM_GALLERY && resultCode == RESULT_OK && intent != null) {
            try {
                Uri uri = intent.getData();
                Logger.d(TAG, "result uri= " + uri);
                String result = null;
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
                    result = getPath(this, uri);
                }else {
                    result = getName(uri);
                }

                Logger.d(TAG, "result = " + result);
                if (result == null) {
                    Toast.makeText(this, getString(R.string.not_available_file_type), Toast.LENGTH_SHORT).show();
                    return;
                }
                File file = new File(result);
                if (!file.exists()) {
                    Toast.makeText(this, getString(R.string.not_available_file_type), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (file.length() > Config.MAX_FILE_SEND_SIZE) {
                    Toast.makeText(this, getString(R.string.send_file_size_limit), Toast.LENGTH_SHORT).show();
                    return;
                }
                Glide.with(this).load(result).centerCrop().into(content_profile_layout);
                addDBSpemItem(OuiBotPreferences.getLoginId(getApplicationContext()),result);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    public String getPath(final Context context, final Uri uri) {

        //check here to KITKAT or new version
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {

            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    private String getName(Uri uri) {
        String[] projection = {MediaStore.Images.ImageColumns.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private static final int PICK_FROM_GALLERY = 14;
    private void callProfileSetting() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
//        intent.putExtra("crop", "true");
//        intent.putExtra("aspectX", 0);
//        intent.putExtra("aspectY", 0);
//        intent.putExtra("outputX", 200);
//        intent.putExtra("outputY", 200);
        try {
//            intent.putExtra("return-data", true);
            startActivityForResult(Intent.createChooser(intent,
                    ""), PICK_FROM_GALLERY);
        } catch (ActivityNotFoundException e) {
        }
    }

    private void initUI() {
        Logger.e("!!!", "MainActivity initUI call");
        setContentView(R.layout.activity_myclebot);
        dlDrawer = (DrawerLayout) findViewById(R.id.dl_activity_main_drawer);
        lv_activity_main_nav_list = (View) findViewById(R.id.lv_activity_main_nav_list);
        lv_activity_main_nav_list.setSoundEffectsEnabled(false);
        lv_activity_main_nav_list.setOnClickListener(null);
        content_profile_layout = (ImageView) findViewById(R.id.content_profile_layout);

        Glide.with(MainActivity.CONTEXT).load(getImagePath(OuiBotPreferences.getLoginId(this))).centerCrop().placeholder(R.drawable.img_contents_person_nopicture).into(content_profile_layout);


        content_profile_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callProfileSetting();
            }
        });

        ImageView ic_action_menu = (ImageView) findViewById(R.id.ic_action_menu);
        ic_action_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dlDrawer.isDrawerOpen(lv_activity_main_nav_list)) {
                    dlDrawer.closeDrawer(lv_activity_main_nav_list);
                } else {
                    dlDrawer.openDrawer(lv_activity_main_nav_list);
                }
            }
        });
        ImageView ic_action_setting = (ImageView) findViewById(R.id.ic_action_setting);
        ic_action_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Config.Mode == Config.COMPILE_Ouibot) {
                    startActivity((new Intent(CONTEXT, SettingActivity.class)).putExtra("message", mService));
                } else {
                    startActivity((new Intent(CONTEXT, SettingAndroidActivity.class)).putExtra("message", mService));
                }
            }
        });
        ImageButton mBtnCallWebview = (ImageButton) findViewById(R.id.btn_call_webview);
        mBtnCallWebview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.CONTEXT, SensorWebViewActivity.class);
                startActivity(intent);
            }
        });
        if (Config.COMPILE_DEMO) {
            mBtnCallWebview.setVisibility(View.VISIBLE);
        } else {
            mBtnCallWebview.setVisibility(View.GONE);
        }
        TextView myphoneNumber = (TextView) findViewById(R.id.myphoneNumber);
        myphoneNumber.setText(OuiBotPreferences.getLoginId(getApplicationContext()));


        initTopMenu();
        initkeypad();
        initViewPager();

        LinearLayout logout = (LinearLayout)findViewById(R.id.logout);
        if ( Config.Mode == Config.COMPILE_Ouibot ) {
            logout.setVisibility(View.GONE);
        } else {
            logout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        Message msg = Message.obtain(null, Config.LOGOUT, null);
                        msg.replyTo = mService;
                        mService.send(msg);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });




        }
    }

    private JSONObject getJsonFormat(String token){
        JSONObject json = new JSONObject();
        JSONObject json2 = new JSONObject();
        try {
            json2.put("title","GCM을 통해서 푸시메시지 보내기");
            json2.put("body","GCM 으로 iOS !!!!!");
            json2.put("sound","default");

            json.put("to", token);
            json.put("content_available", true);
            json.put("priority", "high");
            json.putOpt("notification", json2);

        }catch (JSONException e){
            e.printStackTrace();
        }
        return json;
    }

    private void initViewPager(){
        tabPagerAdapter = new TabPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getFragmentItem(int position) {
                Logger.e("!!!", "tabPagerAdapter getFragmentItem position = " + position);
                if (position == 0) {
                    return new ContactView();
                } else if (position == 1) {
                    return new HistoryView();
                } else if (position == 2) {
                    return new MessageView();
                } else {
                    if (Config.Mode == 2) {
                        return new LinkSettingView();
                    } else {
                        return new LinkOuibotSelfSettingView();
                    }
                }
            }

            @Override
            public void updateFragmentItem(int position, Fragment fragment) {
                Logger.d("!!!", "updateFragmentItem position =" + position);
                if (position == 3) {
                    if (Config.Mode == 2) {
                        ((LinkSettingView) fragment).LoadDBData();
                    } else {
                        ((LinkOuibotSelfSettingView) fragment).LoadDBData();
                    }
                } else if (position == 2) {
                    ((MessageView) fragment).LoadDBData();
                } else if (position == 1) {
                    ((HistoryView) fragment).LoadDBData(getApplicationContext());
                } else if (position == 0) {
//                    Logger.d("!!!", "=================================     updateFragmentItem position =" + position);
//                    ((HistoryView) tabPagerAdapter.getItem(1)).LoadDBData(getApplicationContext());
                    try {
                        ((ContactView) fragment).LoadDBData(getApplicationContext());
                    } catch(Exception e) {
                    }
                }
            }

            @Override
            public int getCount() {
                return 4;
            }
        };
        viewPager = (ViewPager) findViewById(R.id.fl_activity_main_container);
        viewPager.setAdapter(tabPagerAdapter);
        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                setTopMenuBackGroundColor(position);
                if (position == 1) {
                    Intent intent = new Intent("kr.co.netseason.MISSDEL");
                    sendBroadcast(intent);
                    Logger.e("!!!", "Position Broad=" + intent);
                }
            }
        });
        viewPager.setOffscreenPageLimit(1);
    }

    private String getImagePath(String mNumber) {
        ContentResolver resolver = getContentResolver();
        Cursor c = resolver.query(SecureProvider.PROFILE_TABLE_URI, new String[]{SecureSQLiteHelper.COL_PROFILE},
                SecureSQLiteHelper.COL_PEER_RTCID + " = ? ", new String[]{mNumber}, null);
        String profile = "";
        if (c != null && c.moveToFirst()) {
            try {
                do {
                    profile = c.getString(c.getColumnIndex(SecureSQLiteHelper.COL_PROFILE));
                } while (c.moveToNext());
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                c.close();
            }
        }
        return profile;
    }

//    private void setItemImage(ImageView view, String mNumber) {
//        ContentResolver resolver = getContentResolver();
//        Cursor c = resolver.query(SecureProvider.PROFILE_TABLE_URI, new String[]{SecureSQLiteHelper.COL_PROFILE},
//                SecureSQLiteHelper.COL_PEER_RTCID + " = ? ", new String[]{mNumber}, null);
//        if (c != null && c.moveToFirst()) {
//            try {
//                do {
//                    byte[] profile = c.getBlob(c.getColumnIndex(SecureSQLiteHelper.COL_PROFILE));
//                    Logger.e(TAG, "profile = " + profile.length);
//                    Glide.with(this).load(profile).centerCrop().into(view);
////                    Bitmap bitmap = BitmapFactory.decodeByteArray(profile, 0, profile.length);
////                    view.setImageBitmap(bitmap);
//                } while (c.moveToNext());
//            } catch (Exception e) {
//                e.printStackTrace();
//            } finally {
//                c.close();
//            }
//        } else {
//            view.setImageResource(R.drawable.img_contents_person_nopicture);
//        }
//    }

//    private void uiTopSetting() {
//        LinearLayout ic_action_menu_Panel = (LinearLayout)findViewById(R.id.ic_action_menu_Panel);
//        UIUtil.setIMAGESIZE(this, 60, ic_action_menu_Panel, 1, Gravity.CENTER);
//        ImageView ic_action_menu = (ImageView)findViewById(R.id.ic_action_menu);
//        UIUtil.setIMAGESIZE(this, 30, ic_action_menu, 1, Gravity.CENTER);
//
//        LinearLayout ic_action_logo_Panel = (LinearLayout)findViewById(R.id.ic_action_logo_Panel);
//        UIUtil.setIMAGESIZE(this, 633, 60, ic_action_logo_Panel, 1, Gravity.CENTER);
//        ImageView ic_action_logo = (ImageView)findViewById(R.id.ic_action_logo);
//        UIUtil.setIMAGESIZE(this, 160, 30, ic_action_logo, 1, Gravity.CENTER);
//
//        LinearLayout ic_action_setting_Panel = (LinearLayout)findViewById(R.id.ic_action_setting_Panel);
//        UIUtil.setIMAGESIZE(this, 60, ic_action_setting_Panel, 1, Gravity.CENTER);
//        ImageView ic_action_setting = (ImageView)findViewById(R.id.ic_action_setting);
//        UIUtil.setIMAGESIZE(this, 30, ic_action_setting, 1, Gravity.CENTER);
//    }

    private void initkeypad() {
        ImageButton keypad = (ImageButton) findViewById(R.id.keypad_start);
//        UIUtil.setIMAGESIZE(this, 75,keypad, 2, Gravity.RIGHT|Gravity.BOTTOM);
        keypad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CONTEXT, DialActivity.class);
                startActivity(intent.putExtra("message", mService));
            }
        });
    }

    private void initTopMenu() {
        contactmenu = (LinearLayout) findViewById(R.id.top_menu_contact);
        contactmenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(0);
            }
        });
        contactmenu_icon = (ImageView) findViewById(R.id.top_menu_contact_icon);
        contactmenu_text = (TextView) findViewById(R.id.top_menu_contact_text);
        contactmenu_line = (LinearLayout) findViewById(R.id.top_menu_contact_line);
        historymenu = (LinearLayout) findViewById(R.id.top_menu_history);
        historymenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(1);
            }
        });
        historymenu_icon = (ImageView) findViewById(R.id.top_menu_history_icon);
        historymenu_text = (TextView) findViewById(R.id.top_menu_history_text);
        historymenu_line = (LinearLayout) findViewById(R.id.top_menu_history_line);
        messagemenu = (LinearLayout) findViewById(R.id.top_menu_message);
        messagemenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(2);
            }
        });
        messagemenu_icon = (ImageView) findViewById(R.id.top_menu_message_icon);
        messagemenu_text = (TextView) findViewById(R.id.top_menu_message_text);
        messagemenu_line = (LinearLayout) findViewById(R.id.top_menu_message_line);
        linksettingmenu = (LinearLayout) findViewById(R.id.top_menu_link);
        linksettingmenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(3);
            }
        });
        linksettingmenu_icon = (ImageView) findViewById(R.id.top_menu_link_icon);
        linksettingmenu_text = (TextView) findViewById(R.id.top_menu_link_text);
        linksettingmenu_line = (LinearLayout) findViewById(R.id.top_menu_link_line);
    }

    private void uimenuSetting() {
        LinearLayout topMenu = (LinearLayout) findViewById(R.id.topMenu);
        UIUtil.setIMAGESIZE(this, LinearLayout.LayoutParams.MATCH_PARENT, 45, topMenu, 1, 0);
        UIUtil.setIMAGESIZE(this, 23, contactmenu_icon, 1, Gravity.CENTER);
        UIUtil.setIMAGESIZE(this, 23, historymenu_icon, 1, Gravity.CENTER);
        UIUtil.setIMAGESIZE(this, 23, messagemenu_icon, 1, Gravity.CENTER);
        UIUtil.setIMAGESIZE(this, 23, linksettingmenu_icon, 1, Gravity.CENTER);
    }

    private void setTopMenuBackGroundColor(int index) {
        contactmenu_icon.setImageResource(R.drawable.ic_tap_contact_normal);
        contactmenu_text.setTextColor(getResources().getColor(R.color.top_menu_text_color));
        contactmenu_line.setBackgroundResource(R.color.top_menu_bg);
        historymenu_icon.setImageResource(R.drawable.ic_tap_recent_normal);
        historymenu_text.setTextColor(getResources().getColor(R.color.top_menu_text_color));
        historymenu_line.setBackgroundResource(R.color.top_menu_bg);
        messagemenu_icon.setImageResource(R.drawable.ic_tap_message_normal);
        messagemenu_text.setTextColor(getResources().getColor(R.color.top_menu_text_color));
        messagemenu_line.setBackgroundResource(R.color.top_menu_bg);
        linksettingmenu_icon.setImageResource(R.drawable.ic_tap_connection_normal);
        linksettingmenu_text.setTextColor(getResources().getColor(R.color.top_menu_text_color));
        linksettingmenu_line.setBackgroundResource(R.color.top_menu_bg);
        switch (index) {
            case 0:
                contactmenu_icon.setImageResource(R.drawable.ic_tap_contact_selected);
                contactmenu_text.setTextColor(getResources().getColor(R.color.top_menu_text_color_activity));
                contactmenu_line.setBackgroundResource(R.color.top_menu_bg_activity);
                break;
            case 1:
                historymenu_icon.setImageResource(R.drawable.ic_tap_recent_selected);
                historymenu_text.setTextColor(getResources().getColor(R.color.top_menu_text_color_activity));
                historymenu_line.setBackgroundResource(R.color.top_menu_bg_activity);
                break;
            case 2:
                messagemenu_icon.setImageResource(R.drawable.ic_tap_message_selected);
                messagemenu_text.setTextColor(getResources().getColor(R.color.top_menu_text_color_activity));
                messagemenu_line.setBackgroundResource(R.color.top_menu_bg_activity);
                break;
            case 3:
                linksettingmenu_icon.setImageResource(R.drawable.ic_tap_connection_selected);
                linksettingmenu_text.setTextColor(getResources().getColor(R.color.top_menu_text_color_activity));
                linksettingmenu_line.setBackgroundResource(R.color.top_menu_bg_activity);
                break;
        }
    }


    public void callClinck(View view) {
        try {
            Message msg = Message.obtain(null, Config.CALL_ACTIVITY_START, ((Button) view).getHint().toString());
            msg.replyTo = mService;
            mService.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cctvClinck(View view) {
        try {
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA}, 1);
//            }

            Message msg = Message.obtain(null, Config.CCTV_ACTIVITY_START, ((Button) view).getHint().toString());
            msg.replyTo = mService;
            mService.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //    public void logoutClinck(View view) {
//        try {
//            Message msg = Message.obtain(null, Config.OUIBOT_LOGOUT, OuiBotPreferences.getLoginId(this));
//            msg.replyTo = mService;
//            mService.send(msg);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        OuiBotPreferences.setLoginId(this, null);
//        Intent intent = new Intent(context, LoginActivity.class);
//        startActivity(intent);
//        finish();
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        Logger.e("!!!", "requestCode = " + requestCode);
//        Logger.e("!!!", "resultCode = " + resultCode);
//
//        switch (resultCode) {
//            case 100:
//                ((ContactView) tabPagerAdapter.getItem(0)).LoadDBData(getApplicationContext());
//                ((HistoryView) tabPagerAdapter.getItem(1)).LoadDBData(getApplicationContext());
//                break;
//            case 200:
//                ((HistoryView) tabPagerAdapter.getItem(1)).LoadDBData(getApplicationContext());
//                break;
//        }
//    }


    @Override
    protected void onResume() {
        super.onResume();
        refreshViewPaagerData();
        removeAllNotification();
        doActionWithIntentData();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                moveToCallActivityIfConnected();
            }
        }, 500);
    }

    private void moveToCallActivityIfConnected() {
        if (mService != null) {
            messenger = new Messenger(new IncomingHandler());
            Message msg = Message.obtain(null, Config.CHECK_CONNECTED_CALL);
            try {
                msg.replyTo = messenger;
                mService.send(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private BroadcastReceiver mSecureReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Logger.i("", "intent.getAction() = " + intent.getAction());
            if (intent.getAction().equals(Config.INTENT_ACTION_REFESH_SECURE_PAGER_DATA)) {
                refreshViewPaagerData();
            } else if (intent.getAction().equals(Config.INTENT_ACTION_SET_SECURE_CONFIG_DATA_ACK)) {
                setSecureConfigData(intent.getStringExtra(Config.INTENT_ACTION_SET_SECURE_CONFIG_DATA_ACK_KEY));
            } else if (intent.getAction().equals((Config.INTENT_ACTION_MICRO_SDCARD_ERROR))) {
                Toast.makeText(CONTEXT, getResources().getString(R.string.please_input_your_sdcard), Toast.LENGTH_SHORT).show();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (LinkSettingView.mSlaveListViewAdapter != null)
                            LinkSettingView.mSlaveListViewAdapter.notifyDataSetChanged();
                        sendBroadcast(new Intent(Config.INTENT_ACTION_SECURE_OPTION_CHANGED));

                    }
                });
            } else if (intent.getAction().equals((Config.INTENT_ACTION_ALREADY_CONNECTED_PHONE_CALL))) {
                Toast.makeText(CONTEXT, getResources().getString(R.string.can_not_any_action_in_connect_phone_call), Toast.LENGTH_SHORT).show();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (LinkSettingView.mSlaveListViewAdapter != null)
                            LinkSettingView.mSlaveListViewAdapter.notifyDataSetChanged();
                        sendBroadcast(new Intent(Config.INTENT_ACTION_SECURE_OPTION_CHANGED));

                    }
                });
            } else if (intent.getAction().equals((Config.INTENT_ACTION_PEER_IS_NOT_LOGIN))) {
                Toast.makeText(CONTEXT, getResources().getString(R.string.peer_is_not_login), Toast.LENGTH_SHORT).show();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (LinkSettingView.mSlaveListViewAdapter != null)
                            LinkSettingView.mSlaveListViewAdapter.notifyDataSetChanged();
                        sendBroadcast(new Intent(Config.INTENT_ACTION_SECURE_OPTION_CHANGED));

                    }
                });
            } else if (intent.getAction().equals((Config.INTENT_ACTION_MESSAGE_TOAST))) {
                Toast.makeText(CONTEXT, intent.getStringExtra(Config.INTENT_ACTION_MESSAGE_TOAST_MESSAGE), Toast.LENGTH_SHORT).show();
            } else if (intent.getAction().equals(Config.INTENT_RECEIVE_MESSAGE_EVENT)) {
                refreshViewPaagerData();
            }
        }
    };


    public synchronized void refreshViewPaagerData() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (tabPagerAdapter != null)
                    tabPagerAdapter.notifyDataSetChanged();
            }
        });
    }

    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = new Messenger(service);
            messenger = new Messenger(new IncomingHandler());

            Message msg_main_start = Message.obtain(null, Config.MAIN_START, OuiBotPreferences.getLoginId(getApplicationContext()));
            try {
                msg_main_start.replyTo = messenger;
                mService.send(msg_main_start);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                Message msg = Message.obtain(null, Config.START_APP, null);
                mService.send(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    public void onBackPressed() {
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            toast = Toast.makeText(CONTEXT, getResources().getString(R.string.backbutton_click_is_finish), Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            finish();
            toast.cancel();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.e("!!!!!", "MainActivity onDestroy");
        if (Config.isServiceAlive(this)) {
            unbindService(conn);
        }
        unregisterReceiver(mSecureReceiver);
    }

    public void onContactCALLClick(String number) {
        try {
            Message msg = Message.obtain(null, Config.CALL_ACTIVITY_START, number);
            msg.replyTo = mService;
            mService.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onContactCCTVClick(String number) {
        try {
            Message msg = Message.obtain(null, Config.CCTV_ACTIVITY_START, number);
            msg.replyTo = mService;
            mService.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onContactPETClick(String number) {
        try {
            Message msg = Message.obtain(null, Config.PET_ACTIVITY_START, number);
            msg.replyTo = mService;
            mService.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onContactMESSAGEClick(String number) {
        Intent nextActivity = new Intent(this, MessageSendActivity.class);
        Logger.d("", "onContactMESSAGEClick call " + number);
        nextActivity.putExtra("peer_rtcid", number.split("\\|")[1]);
        startActivity(nextActivity);
    }

    public void onHistoryCALLClick(String number) {
        try {
            Message msg = Message.obtain(null, Config.CALL_ACTIVITY_START, number);
            msg.replyTo = mService;
            mService.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onHistoryCCTVClick(String number) {
        try {
            Message msg = Message.obtain(null, Config.CCTV_ACTIVITY_START, number);
            msg.replyTo = mService;
            mService.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onHistoryPETClick(String number) {
        try {
            Message msg = Message.obtain(null, Config.PET_ACTIVITY_START, number);
            msg.replyTo = mService;
            mService.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getSecureConfig(String data) {
        if (data == null) {
            return;
        }
        try {
            Message msg = Message.obtain(null, Config.GET_SECURE_CONFIG, data);
            msg.replyTo = mService;
            mService.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<UibotListData> getMasterList(String id) {
        ContentResolver resolver = getContentResolver();
        Cursor c = resolver.query(SecureProvider.USER_INFO_TABLE_URI, new String[]{SecureSQLiteHelper.COL_MASTER_ID},
                SecureSQLiteHelper.COL_SLAVE_ID + " = ? ", new String[]{id}, SecureSQLiteHelper.COL_TIME + " desc");
        ArrayList<UibotListData> masterData = new ArrayList<UibotListData>();

        if (c != null && c.moveToFirst()) {
            try {
                do {
                    String master = c.getString(c.getColumnIndex(SecureSQLiteHelper.COL_MASTER_ID));
                    masterData.add(new UibotListData(master, null, Config.DETECT_MODE_DEFAULT, Config.DETECT_ONOFF_DEFAULT, Config.VIDEO_SAVE_MODE_DEAFULT, Config.VIDEO_SAVE_TIME_DEFAULT, Config.SENSITIVITY_DEFAULT, Config.DO_AFTER_SETTING_TIME_DEFAULT
                            , Config.VIDEO_SAVE_MODE_DEAFULT, Config.NONE_ACTIVITY_VIDEO_SAVE_TIME_DEFAULT, Config.NONE_ACTIVITY_DETECTION_SENSITIVITY_DEFAULT, Config.NONE_ACTIVITY_SETTING_TIME_DEFAULT));
                } while (c.moveToNext());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (c != null) {
                    c.close();
                }
            }
        }

        return masterData;
    }

    public void setSecureConfigData(String value) {
        if (LinkSettingView.mSlaveListViewAdapter.getData() != null) {
            for (int i = 0; i < LinkSettingView.mSlaveListViewAdapter.getData().size(); i++) {
                try {
                    JSONObject jsono = new JSONObject(value);
                    if (LinkSettingView.mSlaveListViewAdapter.getData().get(i).getSlaveRtcid().equals(jsono.get(Config.PARAM_TO))) {
                        JSONObject childJson = jsono.getJSONObject(Config.PARAM_CONFIG);
                        UibotListData data = LinkSettingView.mSlaveListViewAdapter.getData().get(i);
                        data.setDetectMode(childJson.getInt(Config.PARAM_DETECT_MODE));
                        data.setDetectOnOff(childJson.getString(Config.PARAM_DETECT_ONOFF));
                        insertOnOffValueInSecureDatabase(LinkSettingView.mSlaveListViewAdapter.getData().get(i).getSlaveRtcid(), childJson.getString(Config.PARAM_DETECT_ONOFF));
                        data.setSecureVideoSaveMode(childJson.getInt(Config.PARAM_RECORDING_OPTION));
                        data.setVideoSaveTime(childJson.getInt(Config.PARAM_RECORDING_TIME));
                        data.setDetectSensitivity(childJson.getInt(Config.PARAM_DETECT_SENSITIVITY));
                        data.setDoTimeAfterSetting(childJson.getInt(Config.PARAM_SECURITY_SETTING_TIME));
                        data.setNoneActivityDetectSensitivity(childJson.getInt(Config.PARAM_NONE_ACTIVITY_SENSITIVITY));
                        data.setNoneActivityDetectTime(childJson.getInt(Config.PARAM_NONE_ACTIVITY_CHECK_TIME));
                        data.setNoneActivityVideoSaveMode(childJson.getInt(Config.PARAM_NONE_ACTIVITY_RECORDING_OPTION));
                        data.setNoneActivityVideoSaveTime(childJson.getInt(Config.PARAM_NONE_ACTIVITY_RECORDING_TIME));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (LinkSettingView.mSlaveListViewAdapter != null) {
                        LinkSettingView.mSlaveListViewAdapter.dataChange();
                        sendBroadcast(new Intent(Config.INTENT_ACTION_SECURE_OPTION_CHANGED));
                    }
                }
            });

        }
        if (LinkOuibotSelfSettingView.mSlaveListViewAdapter.getData() != null) {
            for (int i = 0; i < LinkOuibotSelfSettingView.mSlaveListViewAdapter.getData().size(); i++) {
                try {
                    JSONObject jsono = new JSONObject(value);
                    if (LinkOuibotSelfSettingView.mSlaveListViewAdapter.getData().get(i).getSlaveRtcid().equals(jsono.get(Config.PARAM_TO))) {
                        JSONObject childJson = jsono.getJSONObject(Config.PARAM_CONFIG);
                        UibotListData data = LinkOuibotSelfSettingView.mSlaveListViewAdapter.getData().get(i);
                        data.setDetectMode(childJson.getInt(Config.PARAM_DETECT_MODE));
                        data.setDetectOnOff(childJson.getString(Config.PARAM_DETECT_ONOFF));
                        insertOnOffValueInSecureDatabase(LinkOuibotSelfSettingView.mSlaveListViewAdapter.getData().get(i).getSlaveRtcid(), childJson.getString(Config.PARAM_DETECT_ONOFF));
                        data.setSecureVideoSaveMode(childJson.getInt(Config.PARAM_RECORDING_OPTION));
                        data.setVideoSaveTime(childJson.getInt(Config.PARAM_RECORDING_TIME));
                        data.setDetectSensitivity(childJson.getInt(Config.PARAM_DETECT_SENSITIVITY));
                        data.setDoTimeAfterSetting(childJson.getInt(Config.PARAM_SECURITY_SETTING_TIME));
                        data.setNoneActivityDetectSensitivity(childJson.getInt(Config.PARAM_NONE_ACTIVITY_SENSITIVITY));
                        data.setNoneActivityDetectTime(childJson.getInt(Config.PARAM_NONE_ACTIVITY_CHECK_TIME));
                        data.setNoneActivityVideoSaveMode(childJson.getInt(Config.PARAM_NONE_ACTIVITY_RECORDING_OPTION));
                        data.setNoneActivityVideoSaveTime(childJson.getInt(Config.PARAM_NONE_ACTIVITY_RECORDING_TIME));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (LinkOuibotSelfSettingView.mSlaveListViewAdapter != null) {
                        LinkOuibotSelfSettingView.mSlaveListViewAdapter.dataChange();
                        sendBroadcast(new Intent(Config.INTENT_ACTION_SECURE_OPTION_CHANGED));
                    }
                }
            });
        }
    }

    public void insertOnOffValueInSecureDatabase(String slaveId, String value) {
        ContentValues values = new ContentValues();
        values.put(SecureSQLiteHelper.COL_SECURE_ON_OFF_VALUE, value);
        ContentResolver resolver = getContentResolver();
        resolver.update(SecureProvider.USER_INFO_TABLE_URI, values, SecureSQLiteHelper.COL_SLAVE_ID + " =? ", new String[]{slaveId});
    }

    public void startSecureMode(String data) {
        try {
            Message msg = Message.obtain(null, Config.START_SECURE_ACTIVITY, data);
            msg.replyTo = mService;
            mService.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void removeAllNotification() {
        NotificationManager notifiyMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notifiyMgr.cancelAll();
    }

    public void stopSecureMode(String data) {
        try {
            Message msg = Message.obtain(null, Config.FINISH_SECURE_ACTIVITY, data);
            msg.replyTo = mService;
            mService.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendDeleteMasterWithConfigAck(String data) {
        try {
            Message msg = Message.obtain(null, Config.DELETE_MASTER_NOTIFY, data);
            msg.replyTo = mService;
            mService.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendDeleteSlaveWithConfigAck(String data) {
        try {
            Message msg = Message.obtain(null, Config.DELETE_SLAVE_NOTIFY, data);
            msg.replyTo = mService;
            mService.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void requestConfigChange(JSONObject data) {
        try {
            Message msg = Message.obtain(null, Config.REQUEST_CONFIG_CHANGE, data);
            msg.replyTo = mService;
            mService.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Config.CONTACT_RELOAD:
//                    ((ContactView) tabPagerAdapter.getItem(0)).LoadDBData(getApplicationContext());
//                    ((HistoryView) tabPagerAdapter.getItem(1)).LoadDBData(getApplicationContext());
                    refreshViewPaagerData();
                    break;
                case Config.CHECK_CONNECTED_CALL:
                    Logger.d(TAG, "CHECK_CONNECTED_CALL ");
                    Intent intent = new Intent(CONTEXT, CallActivity.class);
                    startActivity(intent);
                    break;
                case Config.GO_LOGINPAGE:
                    OuiBotPreferences.delLoginId(CONTEXT);
                    Logger.e(TAG, "GO_LOGINPAGE ");
                    Intent loginIntent = new Intent(CONTEXT, LoginActivity.class);
                    startActivity(loginIntent);
                    finish();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
}
