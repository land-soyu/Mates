package kr.co.netseason.myclebot;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import kr.co.netseason.myclebot.Logger.Logger;
import kr.co.netseason.myclebot.Provider.SecureProvider;
import kr.co.netseason.myclebot.Provider.SecureSQLiteHelper;
import kr.co.netseason.myclebot.UTIL.OuiBotPreferences;
import kr.co.netseason.myclebot.View.ContactModifyDialog;
import kr.co.netseason.myclebot.openwebrtc.Config;
import kr.co.netseason.myclebot.openwebrtc.SignalingChannel;


public class ContectModifyActivity extends FragmentActivity {
    private Context context;
    private Messenger mService;

    private EditText edittextid;
    private EditText edittextpwd;
    private CheckBox camera_checkbox;

    private String requestView = "";

    private ImageView content_profile;
    private ImageView content_profile_layout;

    private static final int PICK_FROM_GALLERY = 14;
    private static final int PICK_FROM_CAMERA = 15;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        initView();
    }

    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = new Messenger(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

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
//
//        if (c != null && c.moveToFirst()) {
//            try {
//                do {
//                    byte[] profile = c.getBlob(c.getColumnIndex(SecureSQLiteHelper.COL_PROFILE));
//                    Bitmap bitmap = BitmapFactory.decodeByteArray(profile, 0, profile.length);
//                    view.setImageBitmap(bitmap);
//                } while (c.moveToNext());
//            } catch (Exception e) {
//                e.printStackTrace();
//            } finally {
//                if (c != null) {
//                    c.close();
//                }
//            }
//        }
//
//    }


    private void initView() {
        context = this;
        setContentView(R.layout.activity_contectmodify);

        edittextid = (EditText) findViewById(R.id.edittextid);
        edittextpwd = (EditText) findViewById(R.id.edittextpwd);
        camera_checkbox = (CheckBox) findViewById(R.id.camera_checkbox);

        content_profile_layout = (ImageView) findViewById(R.id.content_profile_layout);
        content_profile_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callProfileSetting();
            }
        });
        content_profile = (ImageView) findViewById(R.id.content_profile);
        content_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callProfileSetting();
            }
        });

        edittextid.setText(getIntent().getStringExtra("name"));
        edittextpwd.setText(getIntent().getStringExtra("number"));

        Glide.with(MainActivity.CONTEXT).load(getImagePath(edittextpwd.getText().toString())).centerCrop().placeholder(R.drawable.img_contents_person_nopicture).into(content_profile_layout);
        LinearLayout camera_check_layout = (LinearLayout) findViewById(R.id.camera_check_layout);
        camera_check_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (camera_checkbox.isChecked()) {
                    camera_checkbox.setChecked(false);
                } else {
                    camera_checkbox.setChecked(true);
                }
            }
        });
        if (Config.isPhoneId(edittextpwd.getText().toString().trim())) {
            camera_check_layout.setVisibility(View.INVISIBLE);
        }

        requestView = getIntent().getStringExtra("request");
        if (!requestView.equals("")) {
            camera_checkbox.setChecked(true);
        }

        edittextpwd.setFocusable(false);
        edittextpwd.setClickable(false);
        ImageView keypad_back = (ImageView) findViewById(R.id.keypad_back);
        keypad_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(-1);
                finish();
            }
        });

        Button contentadd_button = (Button) findViewById(R.id.contentadd_button);
        contentadd_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edittextid.getText().toString().trim().equals("")) {
                    Toast.makeText(ContectModifyActivity.this, getString(R.string.input_name), Toast.LENGTH_SHORT).show();
                } else if (edittextpwd.getText().toString().trim().equals("")) {
                    Toast.makeText(ContectModifyActivity.this, getString(R.string.input_ouibot_id), Toast.LENGTH_SHORT).show();
                } else if (edittextpwd.getText().toString().trim().equals(OuiBotPreferences.getLoginId(context))) {
                    Toast.makeText(ContectModifyActivity.this, getString(R.string.your_number_not_saved), Toast.LENGTH_SHORT).show();
                } else {
                    String str = "rtcid=" + OuiBotPreferences.getLoginId(context)
                            + "&peer_name=" + edittextid.getText()
                            + "&peer_rtcid=" + edittextpwd.getText();
                    if (camera_checkbox.isChecked()) {
                        if (requestView.equals("")) {
                            str += "&cctv=on";
                        } else {
                            str += "&cctv=empty";
                        }
                    } else {
                        if (requestView.equals("")) {
                            str += "&cctv=empty";
                        } else {
                            str += "&cctv=off";
                        }
                    }
                    new httpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "phonebook_edit_do_post.php", str);
                }
            }
        });

    }

    private void callProfileSetting() {
//                try {
//                    // ????????? ??????
//                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                    intent.putExtra(MediaStore.EXTRA_OUTPUT,
//                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString());
//
//// ????????? ???????????? ?????? ??????
//                    intent.putExtra("crop", "true");
//                    intent.putExtra("aspectX", 0);
//                    intent.putExtra("aspectY", 0);
//                    intent.putExtra("outputX", 200);
//                    intent.putExtra("outputY", 150);
//
//                    intent.putExtra("return-data", true);
//                    startActivityForResult(Intent.createChooser(intent, "Complete action using"), PICK_FROM_GALLERY);
//                } catch (ActivityNotFoundException e) {
//                    // Do nothing for now
//                }
        Intent intent = new Intent();
        // Gallery ??????
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        // ???????????? ??????
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
            // Do nothing for now
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent service = new Intent(this, SignalingChannel.class);
        bindService(service, conn, Context.BIND_AUTO_CREATE);

        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(conn);
    }

//    private boolean profileflag = false;

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
    private String mResult="";
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == PICK_FROM_GALLERY && resultCode == RESULT_OK && intent != null) {
            try {
                Uri uri = intent.getData();
                Logger.d("", "result uri= " + uri);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
                    mResult = getPath(this, uri);
                }else {
                    mResult = getName(uri);
                }

                Logger.d("", "result = " + mResult);
                if (mResult == null) {
                    Toast.makeText(this, getString(R.string.not_available_file_type), Toast.LENGTH_SHORT).show();
                    return;
                }
                File file = new File(mResult);
                if (!file.exists()) {
                    Toast.makeText(this, getString(R.string.not_available_file_type), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (file.length() > Config.MAX_FILE_SEND_SIZE) {
                    Toast.makeText(this, getString(R.string.send_file_size_limit), Toast.LENGTH_SHORT).show();
                    return;
                }
                Glide.with(this).load(mResult).centerCrop().into(content_profile_layout);
                addDBSpemItem(OuiBotPreferences.getLoginId(getApplicationContext()), mResult);
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
    private ContactModifyDialog mCustomDialog;

    //AsyncTask<param,Progress,Result>
    private class httpTask extends android.os.AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... args) {

            String returnValue = "";
            HttpURLConnection conn = null;
            try {
                Logger.e("!!!", "args[0] = " + args[0]);
                Logger.e("!!!", "args[1] = " + args[1]);
                String urlString = Config.Server_IP + args[0];
                Logger.e("!!!", "urlString = " + urlString);
                URL url = new URL(urlString);

                // open connection
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);            // ??????????????? ????????????
                conn.setDoOutput(false);            // ??????????????? ????????????
                conn.setUseCaches(false);        // ???????????? ??????
                conn.setReadTimeout(20000);        // ???????????? ?????? ms??????
//                conn.setRequestMethod("GET");  // or GET
                conn.setRequestMethod("POST");

                // POST ??? ?????? ??????
                StringBuffer params = new StringBuffer("");
//                params.append("name=" + URLEncoder.encode(name)); //????????? ?????? URL?????????
                params.append(args[1]);
                PrintWriter output = new PrintWriter(conn.getOutputStream());
                output.print(params.toString());
                output.close();

                // Response??????
                StringBuffer sb = new StringBuffer();
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                for (; ; ) {
                    String line = br.readLine();
                    if (line == null) break;
                    sb.append(line + "\n");
                }

                br.close();
                conn.disconnect();
                br = null;
                conn = null;

                returnValue = sb.toString();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (conn!=null) {
                    conn.disconnect();
                }
            }
            return returnValue;
        }

        @Override
        protected void onPostExecute(String result) {
            result = result.trim();
            Logger.e("!!!", "httpTask result = | " + result + " |");
            if (result.contains(Config.PARAM_SUCCESS)) {
                Logger.e("!!!", "getIntent().getStringExtra(request) = " + getIntent().getStringExtra("request"));
                if (getIntent().getStringExtra("request").equals("")) {
                    Logger.e("!!!", "camera_checkbox.isChecked() = " + camera_checkbox.isChecked() + ", mService = " + mService);
                    if (camera_checkbox.isChecked() && mService != null) {
                        try {
                            Message msg = Message.obtain(null, Config.CERTIFICATION_SEND, edittextpwd.getText().toString());
                            mService.send(msg);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

//                if (profileflag)
                    addDBSpemItem(edittextpwd.getText().toString(), mResult);

                mCustomDialog = new ContactModifyDialog(context,
                        getString(R.string.contact_modified),
                        getString(R.string.confirm), "", "",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                mCustomDialog.dismiss();
                                setResult(100);
                                finish();
                            }
                        },
                        null,
                        null);
                mCustomDialog.show();
            } else {
                mCustomDialog = new ContactModifyDialog(context,
                        getString(R.string.contact_not_modified),
                        getString(R.string.confirm), "", "",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                mCustomDialog.dismiss();
                            }
                        },
                        null,
                        null);
                mCustomDialog.show();
            }

        }
    }
}
