package kr.co.netseason.myclebot;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Messenger;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import kr.co.netseason.myclebot.API.MessageListData;
import kr.co.netseason.myclebot.Logger.Logger;
import kr.co.netseason.myclebot.Provider.SecureProvider;
import kr.co.netseason.myclebot.Provider.SecureSQLiteHelper;
import kr.co.netseason.myclebot.Security.DataSessionSenderAsyncTask;
import kr.co.netseason.myclebot.Security.ForIOSNotificationHttpAsyncTask;
import kr.co.netseason.myclebot.Security.MessageSenderAsyncTaskWithWebSocket;
import kr.co.netseason.myclebot.UTIL.OuiBotPreferences;
import kr.co.netseason.myclebot.openwebrtc.Config;
import kr.co.netseason.myclebot.openwebrtc.SignalingChannel;


public class MessageSendActivity extends Activity implements View.OnClickListener {
    private Context context;
    private EditText message_to_number;
    private EditText message_data;
    private RelativeLayout dialview;
    private DataSessionSenderAsyncTask mDataSessionTask;
    private Messenger mService;
    private ImageButton mBtnMediaFileAdd;
    private int SELECT_MEDIA = 0;
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        initUI();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUI();
    }

    public void initUI() {
        context = this;
        setContentView(R.layout.activity_messagesend);
        String peerRtcid = getIntent().getStringExtra("peer_rtcid");
        dialview = (RelativeLayout) findViewById(R.id.dialview);
        mBtnMediaFileAdd = (ImageButton) findViewById(R.id.btn_add_media);
        mBtnMediaFileAdd.setOnClickListener(this);
        Button message_send_button = (Button) findViewById(R.id.message_send_button);
        message_send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessageOrFile(makeObjectWithMessage(message_data.getText().toString(), MessageListData.MESSAGE_TYPE, MessageListData.SENDING, message_to_number.getText().toString()));

            }
        });
        setupUI(dialview);
        message_to_number = (EditText) findViewById(R.id.message_to_number);
        message_data = (EditText) findViewById(R.id.message_data);
        if (peerRtcid != null) {
            message_to_number.setText(peerRtcid);
            message_data.requestFocus();
        }
        ImageView keypad_back = (ImageView) findViewById(R.id.keypad_back);
        keypad_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_add_media:
                if(isSendingData()){
                    Toast.makeText(MessageSendActivity.this, getResources().getString(R.string.already_message_receiving),Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent();
//                intent.setType("video/*");
                intent.setType("video/*, images/*");
//                intent.setType("*/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, ""), SELECT_MEDIA);
                break;
        }
    }

    private boolean isSendingData(){
        ContentResolver resolber = getContentResolver();
        Cursor c = resolber.query(SecureProvider.MESSAGE_TABLE_URI, SecureSQLiteHelper.TABLE_MESSAGE_ALL_COLUMNS, SecureSQLiteHelper.COL_RTCID + "= ? AND " + SecureSQLiteHelper.COL_SEND_STATE + " = ? ", new String[]{OuiBotPreferences.getLoginId(context), String.valueOf(MessageListData.SENDING)}, SecureSQLiteHelper.COL_TIME);
        boolean isSendingData = false;
        if (c != null && c.moveToFirst()) {
            try {
                do {
                    isSendingData = true;
                } while (c.moveToNext());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (c != null) {
                    c.close();
                }
            }
        }
        return isSendingData;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == SELECT_MEDIA && resultCode == RESULT_OK && intent != null) {
            try {
                Uri uri = intent.getData();
                Logger.d("!!!", "result uri= " + uri);
                String result = null;
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
                    result = getPath(this, uri);
                }else {
                    result = getName(uri);
                }

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
                int type;
                if (Config.isVideoFile(result)) {
                    type = MessageListData.VIDEO_TYPE;
                } else if (Config.isImageFile(result)) {
                    type = MessageListData.IMAGE_TYPE;
                } else {
                    Toast.makeText(this, getString(R.string.not_available_file_type), Toast.LENGTH_SHORT).show();
                    return;
                }
                sendMessageOrFile(makeObjectWithMessage(result, type, MessageListData.SENDING, message_to_number.getText().toString()));
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

    public void sendMessageOrFile(MessageListData data) {
        if (data.getPeerRtcid().equals("")) {
            Toast.makeText(this, getResources().getString(R.string.please_input_received_id), Toast.LENGTH_SHORT).show();
            return;
        }
        if (data.getMessageData().equals("")) {
            Toast.makeText(this, getResources().getString(R.string.input_your_message), Toast.LENGTH_SHORT).show();
            return;
        }
        inserMessage2database(data);
        if (data.getType() == MessageListData.MESSAGE_TYPE) {
            messageSend(data);
        } else {
            new GetDeviceTokenhttpTask(data).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "get_device_token_post.php", "rtcid=" + data.getPeerRtcid());
        }
        Intent nextActivity = new Intent(context, MessageDetailActivity.class);
        nextActivity.putExtra("peer_rtcid", message_to_number.getText().toString());
        startActivity(nextActivity);
        finish();
    }

    private int getThreadTime(File file){
        int mb = 1024 * 1024;
        int defaultTime =25;
        int count = 4;
        if(file.length()/mb >0){
            count = (int)(file.length()/mb) * count;
        }
        Logger.d("!!!","getThreadTime = "+(defaultTime+count));
        return defaultTime+count;
    }

    private String getName(Uri uri) {
        String[] projection = {MediaStore.Images.ImageColumns.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public void setupUI(View view) {
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {

                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard();
                    return false;
                }
            });
        }
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent service = new Intent(this, SignalingChannel.class);
        bindService(service, conn, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(conn);
        hideSoftKeyboard();
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

    private void messageSend(MessageListData data) {
        if (data == null) {
            return;
        }
        if (data.getMessageData().equals("")) {
            Toast.makeText(this, getResources().getString(R.string.input_your_message), Toast.LENGTH_SHORT).show();
            return;
        } else {
            MessageSenderAsyncTaskWithWebSocket mTask = new MessageSenderAsyncTaskWithWebSocket(this, data.getTime(), data.getMessageData(), data.getPeerRtcid(), mService);
            mTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, null);
        }
    }

    public MessageListData makeObjectWithMessage(String data, int type, int sendState, String peerRtcid) {
        return new MessageListData(OuiBotPreferences.getLoginId(this),
                peerRtcid, peerRtcid, data, MessageListData.SEND_FLAG_ME, System.currentTimeMillis(), MessageListData.READED, MessageListData.UNCHECKED, type, sendState);
    }

    public void hideSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }

    private void inserMessage2database(MessageListData data) {
        ContentValues values = new ContentValues();
        values.put(SecureSQLiteHelper.COL_RTCID, data.getRTCID());
        values.put(SecureSQLiteHelper.COL_PEER_RTCID, data.getPeerRtcid());
        values.put(SecureSQLiteHelper.COL_PEER_RTCID_NAME, data.getPeerRtcidName());
        values.put(SecureSQLiteHelper.COL_MESSAGE_DATA, data.getMessageData());
        values.put(SecureSQLiteHelper.COL_SEND_FLAG, data.getSendFlag());
        values.put(SecureSQLiteHelper.COL_TIME, data.getTime());
        values.put(SecureSQLiteHelper.COL_READABLE, data.getReadable());
        values.put(SecureSQLiteHelper.COL_TYPE, data.getType());
        values.put(SecureSQLiteHelper.COL_SEND_STATE, data.getSendState());
        ContentResolver resolver = getContentResolver();
        resolver.insert(SecureProvider.MESSAGE_TABLE_URI, values);
    }

    private void deleteMessage2database(MessageListData data) {
        ContentResolver resolver = getContentResolver();
        resolver.delete(SecureProvider.MESSAGE_TABLE_URI, SecureSQLiteHelper.COL_PEER_RTCID + " =? AND "+SecureSQLiteHelper.COL_TIME+" =? ", new String[]{data.getPeerRtcid(),String.valueOf(data.getTime())});
    }

    private class GetDeviceTokenhttpTask extends android.os.AsyncTask<String, Void, String> {
        MessageListData mData;
        public GetDeviceTokenhttpTask(MessageListData data){
            mData = data;
        }

        @Override
        protected String doInBackground(String... args) {
            Logger.e("!!!", "GetDeviceTokenhttpTask start call");
            String returnValue = "";
            try {
                Logger.e("!!!", "args[0] = " + args[0]);
                Logger.e("!!!", "args[1] = " + args[1]);
                String urlString = Config.Server_IP + args[0];
                Logger.e("!!!", "urlString = " + urlString);
                URL url = new URL(urlString);

                // open connection
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);            // 입력스트림 사용여부
                conn.setDoOutput(false);            // 출력스트림 사용여부
                conn.setUseCaches(false);        // 캐시사용 여부
                conn.setReadTimeout(20000);        // 타임아웃 설정 ms단위
//                conn.setRequestMethod("GET");  // or GET
                conn.setRequestMethod("POST");

                // POST 값 전달 하기
                StringBuffer params = new StringBuffer("");
//                params.append("name=" + URLEncoder.encode(name)); //한글일 경우 URL인코딩
                params.append(args[1]);
                PrintWriter output = new PrintWriter(conn.getOutputStream());
                output.print(params.toString());
                output.close();

                // Response받기
                StringBuffer sb = new StringBuffer();
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                for (; ; ) {
                    String line = br.readLine();
                    if (line == null) break;
                    sb.append(line + "\n");
                }

                br.close();
                conn.disconnect();

                returnValue = sb.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return returnValue;
        }

        @Override
        protected void onPostExecute(String result) {
            result = result.trim();
            String token = result;
            if (token != null && result.length() > 0) {
                if(mData.getType() == MessageListData.VIDEO_TYPE){
                    Toast.makeText(MessageSendActivity.this, getString(R.string.can_not_use_video_file_transfer), Toast.LENGTH_SHORT).show();
                    deleteMessage2database(mData);
                    return;
                }
                String title = OuiBotPreferences.getLoginId(MessageSendActivity.this);
                String message = getResources().getString(R.string.push_file_message_recieved);
                String key = String.valueOf(mData.getTime());
                new ForIOSNotificationHttpAsyncTask(token, title, message, null, false, key, Config.FILE_MESSAGE_TYPE).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                recordMessageSendSucces2Database(mData.getTime());
                sendBroadcast(new Intent(Config.INTENT_RECEIVE_MESSAGE_EVENT));
            } else {
                String path = mData.getMessageData();
                File file = new File(path);
                if(file.exists()) {
                    int threadTime = getThreadTime(file);
                    JSONObject json = new JSONObject();
                    try {
                        json.put(Config.COL_FILE_SLICE_COUNT, Config.getFileSliceNum(path));
                        json.put(Config.PARAM_TO, mData.getPeerRtcid());
                        json.put(Config.PARAM_FROM, OuiBotPreferences.getLoginId(MessageSendActivity.this));
                        json.put(SecureSQLiteHelper.COL_FILE_PATH, Config.getFileName(path));
                        json.put(SecureSQLiteHelper.COL_MODE, Config.MESSAGE_FILE_MODE);
                        json.put(SecureSQLiteHelper.COL_TIME, mData.getTime());
                        json.put(Config.PARAM_THREAD_TIME, threadTime);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (MessageDetailActivity.mDataSessionTask != null) {
                        if (MessageDetailActivity.mDataSessionTask.getStatus() == DataSessionSenderAsyncTask.Status.FINISHED) {
                            MessageDetailActivity.mDataSessionTask = new DataSessionSenderAsyncTask(MessageSendActivity.this, mData.getTime(), json, file, mData.getPeerRtcid(), mService, threadTime, Config.MESSAGE_FILE_CHANNEL_5);
                            MessageDetailActivity.mDataSessionTask.executeOnExecutor(MessageDetailActivity.mExec);
                        } else {
                            Toast.makeText(MessageSendActivity.this, getResources().getString(R.string.already_message_receiving), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        MessageDetailActivity.mDataSessionTask = new DataSessionSenderAsyncTask(MessageSendActivity.this, mData.getTime(), json, file, mData.getPeerRtcid(), mService, threadTime, Config.MESSAGE_FILE_CHANNEL_5);
                        MessageDetailActivity.mDataSessionTask.executeOnExecutor(MessageDetailActivity.mExec);
                    }
                }
            }

        }
        public void recordMessageSendSucces2Database(long time) {
            if (time != 0) {
                ContentResolver resolver = getContentResolver();
                ContentValues values = new ContentValues();
                values.put(SecureSQLiteHelper.COL_SEND_STATE, MessageListData.SEND_COMPLETE);
                resolver.update(SecureProvider.MESSAGE_TABLE_URI, values, SecureSQLiteHelper.COL_RTCID + " =? AND " + SecureSQLiteHelper.COL_TIME + " =? ", new String[]{OuiBotPreferences.getLoginId(MessageSendActivity.this), String.valueOf(time)});
            }
        }
    }
}
