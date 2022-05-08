package kr.co.netseason.myclebot;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import kr.co.netseason.myclebot.API.MessageListData;
import kr.co.netseason.myclebot.Logger.Logger;
import kr.co.netseason.myclebot.Provider.SecureProvider;
import kr.co.netseason.myclebot.Provider.SecureSQLiteHelper;
import kr.co.netseason.myclebot.Security.CustomDialog;
import kr.co.netseason.myclebot.Security.DataSessionSenderAsyncTask;
import kr.co.netseason.myclebot.Security.ForIOSNotificationHttpAsyncTask;
import kr.co.netseason.myclebot.Security.MessageSenderAsyncTaskWithWebSocket;
import kr.co.netseason.myclebot.UTIL.OuiBotPreferences;
import kr.co.netseason.myclebot.openwebrtc.Config;
import kr.co.netseason.myclebot.openwebrtc.SignalingChannel;


public class MessageDetailActivity extends Activity implements View.OnClickListener {

    private Context context;
    private EditText mEditTextView;
    private String mPeerRtcid = "";
    private String TAG = getClass().getName();
    private ListView mListView;
    private ListViewAdapter mAdapter;
    private Messenger mService;
    private RelativeLayout dialview;
    private IntentFilter mFilter;
    private ImageButton mBtnMediaFileAdd;
    private CustomDialog mCustomDialog;
    private String mDialogPeerRticd;
    private long mDialogTime;
    private String mDialogMessage;
    private int SELECT_MEDIA = 0;
    private String mImagePath;
    public static DataSessionSenderAsyncTask mDataSessionTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messagesend_detail);
        context = this;
        Intent intent = getIntent();
        mPeerRtcid = intent.getStringExtra("peer_rtcid");
        Logger.d(TAG, "onCreate call mPeerRtcid= " + mPeerRtcid);
        if (mPeerRtcid == null) {
            finish();
            return;
        }
        mFilter = new IntentFilter(Config.INTENT_RECEIVE_MESSAGE_EVENT);
        mFilter.addAction(Config.INTENT_ACTION_THREAD_STOP);
        mBtnMediaFileAdd = (ImageButton) findViewById(R.id.btn_add_media);
        mBtnMediaFileAdd.setOnClickListener(this);
        TextView title = (TextView) findViewById(R.id.peer_rtcid_text);
        title.setText(mPeerRtcid);
        dialview = (RelativeLayout) findViewById(R.id.dialview);
        mListView = (ListView) findViewById(R.id.message_list);
//        mListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        setupUI(dialview);
//        setupUI(mListView);
        mImagePath = getImagePath(mPeerRtcid);
        mListView.setOnItemClickListener(mListViewOnLicked);
        mEditTextView = (EditText) findViewById(R.id.message_data);
        ImageView keypad_back = (ImageView) findViewById(R.id.keypad_back);
        keypad_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button message_send_button = (Button) findViewById(R.id.message_send_button);
        message_send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String data = mEditTextView.getText().toString();
                if (data != null) {
                    if (!data.equals("")) {
                        sendMessageOrFile(makeObjectWithMessage(data, MessageListData.MESSAGE_TYPE, MessageListData.SENDING));
                    }
                }
            }
        });

        mAdapter = new ListViewAdapter(context);
        mListView.setAdapter(mAdapter);

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
    AdapterView.OnItemClickListener mListViewOnLicked = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            hideSoftKeyboard();
        }
    };

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add_media:
                if(isSendingData()){
                    Toast.makeText(MessageDetailActivity.this, getResources().getString(R.string.already_message_receiving),Toast.LENGTH_SHORT).show();
                    return;
                }
                if(mDataSessionTask != null) {
                    if (mDataSessionTask.getStatus() != DataSessionSenderAsyncTask.Status.FINISHED) {
                        Toast.makeText(MessageDetailActivity.this, getResources().getString(R.string.already_message_receiving),Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                Intent intent = new Intent();
//                intent.setType("video/*");
                intent.setType("video/*, images/*");
//                intent.setType("*/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, ""), SELECT_MEDIA);
                break;
            default:
                break;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == SELECT_MEDIA && resultCode == RESULT_OK && intent != null) {
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
                    Toast.makeText(MessageDetailActivity.this, getString(R.string.not_available_file_type), Toast.LENGTH_SHORT).show();
                    return;
                }
                File file = new File(result);
                if (!file.exists()) {
                    Toast.makeText(MessageDetailActivity.this, getString(R.string.not_available_file_type), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (file.length() > Config.MAX_FILE_SEND_SIZE) {
                    Toast.makeText(MessageDetailActivity.this, getString(R.string.send_file_size_limit), Toast.LENGTH_SHORT).show();
                    return;
                }
                int type;
                if (Config.isVideoFile(result)) {
                    type = MessageListData.VIDEO_TYPE;
                } else if (Config.isImageFile(result)) {
                    type = MessageListData.IMAGE_TYPE;
                } else {
                    Toast.makeText(MessageDetailActivity.this, getString(R.string.not_available_file_type), Toast.LENGTH_SHORT).show();
                    return;
                }
                MessageListData data = makeObjectWithMessage(result, type, MessageListData.SENDING);
                sendMessageOrFile(data);
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

    public void sendMessageOrFile(MessageListData data) {
        inserMessage2database(data);
        addChatList(data);
        if (data.getType() == MessageListData.MESSAGE_TYPE) {
            messageSend(data);
        } else {
            new GetDeviceTokenhttpTask(data).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "get_device_token_post.php", "rtcid=" + data.getPeerRtcid());
        }
    }

    public static ThreadPoolExecutor mExec = new ThreadPoolExecutor(1, 999, 999, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(), new RejectedExecutionHandler() {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
//            Logger.e("!!!", "fileImageExec ThreadPoolExecutor rejectedExecution");
        }
    });

    private int getThreadTime(File file){
        int mb = 1024 * 1024;
        int defaultTime =25;
        int count = 4;
        if(file.length()/mb >0){
            count = (int)(file.length()/mb) * count;
        }
        Logger.d(TAG,"getThreadTime = "+(defaultTime+count));
        return defaultTime+count;
    }

    CustomDialog.OnDialogListener mDialogListner = new CustomDialog.OnDialogListener() {
        @Override
        public void OnLeftClicked(View v) {

        }

        @Override
        public void OnCenterClicked(View v) {
            mCustomDialog.cancel();
        }

        @Override
        public void OnRightClicked(View v) {
            deleteMessageDataInDatabase(mDialogMessage, mDialogPeerRticd, mDialogTime);
            getMessageDBData(mDialogPeerRticd);
            mCustomDialog.cancel();
        }

        @Override
        public void OnDismissListener() {
            mCustomDialog = null;
            mDialogPeerRticd = null;
            mDialogMessage = null;
            mDialogTime = 0;
        }

    };

    private void removeAllNotification() {
        NotificationManager notifiyMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notifiyMgr.cancelAll();
    }

    public void deleteMessageDataInDatabase(String message, String peerRtcid, long time) {
        getContentResolver().delete(SecureProvider.MESSAGE_TABLE_URI, SecureSQLiteHelper.COL_PEER_RTCID + " =? AND " + SecureSQLiteHelper.COL_MESSAGE_DATA + " =? AND " + SecureSQLiteHelper.COL_TIME + " =?", new String[]{peerRtcid, message, String.valueOf(time)});
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

    public void hideSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }

    public MessageListData makeObjectWithMessage(String data, int type, int sendState) {
        return new MessageListData(OuiBotPreferences.getLoginId(MessageDetailActivity.this),
                mPeerRtcid, mPeerRtcid, data, MessageListData.SEND_FLAG_ME, System.currentTimeMillis(), MessageListData.READED, MessageListData.UNCHECKED, type, sendState);
    }


    @Override
    protected void onResume() {
        super.onResume();
        removeAllNotification();
        Intent service = new Intent(this, SignalingChannel.class);
        bindService(service, conn, Context.BIND_AUTO_CREATE);
        registerReceiver(mReceiver, mFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
        unbindService(conn);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Config.INTENT_RECEIVE_MESSAGE_EVENT)) {
                getMessageDBData(mPeerRtcid);
                scrollMyListViewToBottom();
            }else if(intent.getAction().equals(Config.INTENT_ACTION_THREAD_STOP)){
                if(mDataSessionTask != null) {
                    mDataSessionTask.cancel(true);
                }
            }
        }
    };

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

    private void getMessageDBData(String peerRtcid) {
        Logger.d(TAG, "getMessageDBData call peerRtcid = " + peerRtcid);
        mAdapter.getData().clear();
        setAllDataReaded(peerRtcid);
        ContentResolver resolber = getContentResolver();
        Cursor c = resolber.query(SecureProvider.MESSAGE_TABLE_URI, SecureSQLiteHelper.TABLE_MESSAGE_ALL_COLUMNS, SecureSQLiteHelper.COL_RTCID + "= ? AND " + SecureSQLiteHelper.COL_PEER_RTCID + " = ? ", new String[]{OuiBotPreferences.getLoginId(context), peerRtcid}, SecureSQLiteHelper.COL_TIME);
        if (c != null && c.moveToFirst()) {
            try {
                do {
                    mAdapter.addItem(
                            c.getString(c.getColumnIndex(SecureSQLiteHelper.COL_RTCID)),
                            c.getString(c.getColumnIndex(SecureSQLiteHelper.COL_PEER_RTCID)),
                            c.getString(c.getColumnIndex(SecureSQLiteHelper.COL_PEER_RTCID_NAME)),
                            c.getString(c.getColumnIndex(SecureSQLiteHelper.COL_MESSAGE_DATA)),
                            c.getInt(c.getColumnIndex(SecureSQLiteHelper.COL_SEND_FLAG)),
                            c.getLong(c.getColumnIndex(SecureSQLiteHelper.COL_TIME)),
                            c.getInt(c.getColumnIndex(SecureSQLiteHelper.COL_READABLE)),
                            MessageListData.UNCHECKED,
                            c.getInt(c.getColumnIndex(SecureSQLiteHelper.COL_TYPE)),
                            c.getInt(c.getColumnIndex(SecureSQLiteHelper.COL_SEND_STATE)));
                } while (c.moveToNext());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (c != null) {
                    c.close();
                }
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    public void setAllDataReaded(String peerRtcid) {
        ContentValues values = new ContentValues();
        values.put(SecureSQLiteHelper.COL_READABLE, MessageListData.READED);
        ContentResolver resolver = getContentResolver();
        resolver.update(SecureProvider.MESSAGE_TABLE_URI, values, SecureSQLiteHelper.COL_PEER_RTCID + " =? AND " + SecureSQLiteHelper.COL_READABLE + " =? ", new String[]{peerRtcid, String.valueOf(MessageListData.UNREAD)});
    }

    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = new Messenger(service);
            getMessageDBData(mPeerRtcid);
            scrollMyListViewToBottom();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    private void scrollMyListViewToBottom() {
        mListView.post(new Runnable() {
            @Override
            public void run() {
                mListView.setSelection(mListView.getCount() - 1);
            }
        });
    }

    private void messageSend(MessageListData data) {
        mEditTextView.setText("");
        startSendTask(data);
    }

    public void startSendTask(final MessageListData data) {
        if (mAdapter.getData().size() > 0) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    MessageSenderAsyncTaskWithWebSocket mTask = new MessageSenderAsyncTaskWithWebSocket(MessageDetailActivity.this, data.getTime(), data.getMessageData(), mPeerRtcid, mService);
                    mTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, null);
                }
            }, 200);
        }
    }

    private void inserMessage2database(MessageListData data) {
        Logger.d("", "inserMessage2database call" + data.getPeerRtcid());
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
        resolver.delete(SecureProvider.MESSAGE_TABLE_URI, SecureSQLiteHelper.COL_PEER_RTCID + " =? AND "+SecureSQLiteHelper.COL_TIME+" =? ", new String[]{data.getPeerRtcid() ,String.valueOf(data.getTime())});
        getMessageDBData(data.getPeerRtcid());
    }

    private void addChatList(MessageListData data) {
        if (mAdapter != null) {
            mAdapter.addItem(data.getPeerRtcid(), data.getPeerRtcid(), data.getPeerRtcidName(), data.getMessageData(), data.getSendFlag(), data.getTime(), data.getReadable(), data.getChecked(), data.getType(), data.getSendState());
            mAdapter.notifyDataSetChanged();
            scrollMyListViewToBottom();
        }
    }

    private void removeChatList(MessageListData data) {
        if (mAdapter != null) {
            mAdapter.removeItem(data);
            mAdapter.notifyDataSetChanged();
            scrollMyListViewToBottom();
        }
    }

    private class ViewHolder {
        public ImageView mYouIcon;
        public TextView mYouName;
        public TextView mYouDate;
        public TextView mYouData;
        public TextView mMyDate;
        public TextView mMyData;
        public ImageView mYouImageData;
        public ImageView mMeImageData;
        public ImageView mLoadingMyImage;
        public RelativeLayout mYouHeaderLayout;
        public RelativeLayout mMyHeaderLayout;
        public TextView mYouHeaderText;
        public TextView mMyHeaderText;
        public ViewFlipper mViewFlipper;
        public ViewFlipper mMessageFileYouFlipper;
        public ViewFlipper mMessageFileMeFlipper;
    }

    private class ListViewAdapter extends BaseAdapter {
        private Context mContext = null;
        private ArrayList<MessageListData> mMessageDetailListData = new ArrayList<MessageListData>();

        public ListViewAdapter(Context mContext) {
            super();
            this.mContext = mContext;
        }

        @Override
        public int getCount() {
            return mMessageDetailListData.size();
        }

        @Override
        public Object getItem(int position) {
            if (position < 0 || mMessageDetailListData.size() < position + 1) {
                return null;
            }
            return mMessageDetailListData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.activity_messagedetailitem, null);
                holder.mYouIcon = (ImageView) convertView.findViewById(R.id.you_image);
                holder.mYouName = (TextView) convertView.findViewById(R.id.you_name);
                holder.mYouDate = (TextView) convertView.findViewById(R.id.you_date);
                holder.mYouData = (TextView) convertView.findViewById(R.id.you_data);
                holder.mMessageFileYouFlipper = (ViewFlipper) convertView.findViewById(R.id.you_message_file_flipper);
                holder.mMessageFileYouFlipper.setOnLongClickListener(onMessageLongClicked);
                holder.mMessageFileYouFlipper.setOnClickListener(onMessageClicked);
                holder.mMyDate = (TextView) convertView.findViewById(R.id.me_date);
                holder.mMyData = (TextView) convertView.findViewById(R.id.me_data);
                holder.mMessageFileMeFlipper = (ViewFlipper) convertView.findViewById(R.id.me_message_file_flipper);
                holder.mMessageFileMeFlipper.setOnClickListener(onMessageClicked);
                holder.mMessageFileMeFlipper.setOnLongClickListener(onMessageLongClicked);
                holder.mMeImageData = (ImageView) convertView.findViewById(R.id.me_image_data);
                holder.mYouImageData = (ImageView) convertView.findViewById(R.id.you_image_data);
                holder.mLoadingMyImage = (ImageView) convertView.findViewById(R.id.loading_me_img);
                holder.mLoadingMyImage.setOnClickListener(mRetryClicked);
                holder.mViewFlipper = (ViewFlipper) convertView.findViewById(R.id.view_flipper);
                holder.mYouHeaderLayout = (RelativeLayout) convertView.findViewById(R.id.you_header_layout);
                holder.mMyHeaderLayout = (RelativeLayout) convertView.findViewById(R.id.my_header_layout);
                holder.mYouHeaderText = (TextView) convertView.findViewById(R.id.you_header_layout_text);
                holder.mMyHeaderText = (TextView) convertView.findViewById(R.id.my_header_layout_text);
                holder.mMessageFileYouFlipper = (ViewFlipper) convertView.findViewById(R.id.you_message_file_flipper);
                holder.mMessageFileMeFlipper = (ViewFlipper) convertView.findViewById(R.id.me_message_file_flipper);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }


            MessageListData positionData = (MessageListData) getItem(position);
            MessageListData prePositionData = (MessageListData) getItem(position - 1);
            Logger.d(TAG, "getView call!! " + positionData.getTime());
            if (positionData.getSendFlag() == MessageListData.SEND_FLAG_ME) {
                holder.mViewFlipper.setDisplayedChild(1);
                holder.mMessageFileMeFlipper.setTag(positionData);
                holder.mMyDate.setText(positionData.getTimeStringFormat());
                if (positionData.getType() == MessageListData.IMAGE_TYPE) {
                    holder.mMessageFileMeFlipper.setDisplayedChild(1);
                    Glide.with(mContext)
                            .load(positionData.getMessageData()).override(getResources().getDimensionPixelOffset(R.dimen.message_image_size_width), getResources().getDimensionPixelOffset(R.dimen.message_image_size_height))
                            .placeholder(R.drawable.no_image)
                            .into(holder.mMeImageData);
                    holder.mMeImageData.setVisibility(View.VISIBLE);
                } else if (positionData.getType() == MessageListData.VIDEO_TYPE) {
                    holder.mMessageFileMeFlipper.setDisplayedChild(1);
                    Glide.with(mContext)
                            .load(positionData.getMessageData()).override(getResources().getDimensionPixelOffset(R.dimen.message_image_size_width), getResources().getDimensionPixelOffset(R.dimen.message_image_size_height))
                            .placeholder(R.drawable.no_image)
                            .into(holder.mMeImageData);
                } else {
                    holder.mMessageFileMeFlipper.setDisplayedChild(0);
                    holder.mMyData.setText(positionData.getMessageData());
                }

                holder.mMyHeaderText.setText(setHeaderTimeFormat(positionData.getTime()));
                if (changeDate(prePositionData, positionData)) {
                    holder.mMyHeaderLayout.setVisibility(View.VISIBLE);
                } else {
                    holder.mMyHeaderLayout.setVisibility(View.GONE);
                }
                holder.mLoadingMyImage.setTag(positionData);
                if (positionData.getSendState() == MessageListData.SENDING) {
                    holder.mLoadingMyImage.setImageResource(R.drawable.loading_01);
                    holder.mLoadingMyImage.setVisibility(View.VISIBLE);
                    startRotate(holder.mLoadingMyImage);
                } else if (positionData.getSendState() == MessageListData.SEND_FAIL) {
                    holder.mLoadingMyImage.setImageResource(R.drawable.message_retry);
                    holder.mLoadingMyImage.setVisibility(View.VISIBLE);
                    stopRotate(holder.mLoadingMyImage);
                } else {
                    holder.mLoadingMyImage.setVisibility(View.GONE);
                    stopRotate(holder.mLoadingMyImage);
                }
            } else {
                holder.mViewFlipper.setDisplayedChild(0);
                holder.mMessageFileYouFlipper.setTag(positionData);
                holder.mYouName.setText(positionData.getPeerRtcidName());
                holder.mYouDate.setText(positionData.getTimeStringFormat());
                Glide.with(mContext).load(mImagePath).centerCrop().placeholder(R.drawable.img_contents_person_nopicture).into(holder.mYouIcon);
                if (positionData.getType() == MessageListData.IMAGE_TYPE) {
                    holder.mMessageFileYouFlipper.setDisplayedChild(1);
                    Glide.with(mContext)
                            .load(positionData.getMessageData()).override(getResources().getDimensionPixelOffset(R.dimen.message_image_size_width), getResources().getDimensionPixelOffset(R.dimen.message_image_size_height))
                            .placeholder(R.drawable.no_image)
                            .into(holder.mYouImageData);
                } else if (positionData.getType() == MessageListData.VIDEO_TYPE) {
                    holder.mMessageFileYouFlipper.setDisplayedChild(1);
                    Glide.with(mContext)
                            .load(positionData.getMessageData()).override(getResources().getDimensionPixelOffset(R.dimen.message_image_size_width), getResources().getDimensionPixelOffset(R.dimen.message_image_size_height))
                            .placeholder(R.drawable.no_image)
                            .into(holder.mYouImageData);
                } else {
                    holder.mMessageFileYouFlipper.setDisplayedChild(0);
                    holder.mYouData.setText(positionData.getMessageData());
                }
                holder.mYouHeaderText.setText(setHeaderTimeFormat(positionData.getTime()));
                if (changeDate(prePositionData, positionData)) {
                    holder.mYouHeaderLayout.setVisibility(View.VISIBLE);
                } else {
                    holder.mYouHeaderLayout.setVisibility(View.GONE);
                }
            }
            return convertView;
        }

        View.OnClickListener onMessageClicked = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageListData positionData = (MessageListData) v.getTag();
                String data = positionData.getMessageData();
                Logger.d(TAG, "data = " + data);
                if (Config.isImageFile(data)) {
                    Intent intent = new Intent();
                    intent.setAction(android.content.Intent.ACTION_VIEW);
                    File file = new File(data);
                    intent.setDataAndType(Uri.fromFile(file), "image/*");
                    startActivity(intent);
                } else if (Config.isVideoFile(data)) {
                    Intent intent = new Intent();
                    intent.setAction(android.content.Intent.ACTION_VIEW);
                    File file = new File(data);
                    intent.setDataAndType(Uri.fromFile(file), "video/*");
                    startActivity(intent);
                } else {
                    //nothing
                }
            }

        };



        View.OnLongClickListener onMessageLongClicked = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                MessageListData data = (MessageListData) v.getTag();
                mDialogPeerRticd = data.getPeerRtcid();
                mDialogTime = data.getTime();
                mDialogMessage = data.getMessageData();

                if (mCustomDialog == null) {
                    mCustomDialog = new CustomDialog(MessageDetailActivity.this, getString(R.string.do_you_want_to_delete_message), "", getResources().getString(R.string.cancel), getResources().getString(R.string.confirm), mDialogListner, CustomDialog.TWO_BUTTON_TYPE);

                }
                return false;
            }
        };
        CustomDialog mRetryDialog;
        View.OnClickListener mRetryClicked = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final MessageListData positionData = (MessageListData) v.getTag();
                if (positionData.getSendState() == MessageListData.SEND_FAIL) {
                    if (mRetryDialog == null) {
                        mRetryDialog = new CustomDialog(MessageDetailActivity.this, getResources().getString(R.string.retry_send_message), "", getResources().getString(R.string.cancel), getResources().getString(R.string.confirm), new CustomDialog.OnDialogListener() {
                            @Override
                            public void OnLeftClicked(View v) {

                            }

                            @Override
                            public void OnCenterClicked(View v) {
                                finishRetryDialog();
                            }

                            @Override
                            public void OnRightClicked(View v) {
                                finishRetryDialog();
                                deleteMessageDataInDatabase(positionData.getMessageData(), positionData.getPeerRtcid(), positionData.getTime());
//                                for (int i = 0; i < mAdapter.getData().size(); i++) {
//                                    if (mAdapter.getData().get(i).getTime() == positionData.getTime()) {
//                                        mAdapter.getData().remove(i);
//                                        break;
//                                    }
//                                }
                                removeItem(positionData);
                                notifyDataSetChanged();
                                positionData.setTime(System.currentTimeMillis());
                                positionData.setSendState(MessageListData.SENDING);
                                sendMessageOrFile(positionData);
                            }

                            @Override
                            public void OnDismissListener() {
                                finishRetryDialog();
                            }
                        }, CustomDialog.TWO_BUTTON_TYPE);
                    }

                }
            }
        };

        private void finishRetryDialog() {
            if (mRetryDialog != null) {
                mRetryDialog.cancel();
                mRetryDialog = null;
            }
        }

        public void startRotate(View view) {
            Logger.d(TAG, "startRotate call view =" + view);
            if (view != null) {
                RotateAnimation anim = new RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                anim.setInterpolator(new LinearInterpolator());
                anim.setRepeatCount(Animation.INFINITE);
                anim.setDuration(700);
                view.startAnimation(anim);
            }
        }

        public void stopRotate(View view) {
            if (view != null) {
                view.clearAnimation();
            }
        }

        public boolean changeDate(MessageListData preData, MessageListData curData) {
            SimpleDateFormat formatterD = new SimpleDateFormat("dd");
            try {
                String pDay = formatterD.format(new Date(preData.getTime()));
                String dDay = formatterD.format(new Date(curData.getTime()));
                if (!pDay.equals(dDay)) {
                    return true;
                } else {
                    return false;
                }
            } catch (NullPointerException e) {
            }
            return true;
        }

        public String setHeaderTimeFormat(long timeMilli) {
            return new SimpleDateFormat("yyyy/MM/dd").format(new Date(timeMilli));
        }

        public void addItem(String rtcid, String peerRtcid, String peerRtcidName, String messageData, int sendFlag, long time, int readable, int checked, int type, int sendState) {
            if (mMessageDetailListData.size() > 100) {
                mMessageDetailListData.remove(0);
            }
            mMessageDetailListData.add(new MessageListData(rtcid, peerRtcid, peerRtcidName, messageData, sendFlag, time, readable, checked, type, sendState));
        }

        public void removeItem(MessageListData data){
            mMessageDetailListData.remove(data);
        }

        public ArrayList<MessageListData> getData() {
            return mMessageDetailListData;
        }
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
                    Toast.makeText(MessageDetailActivity.this, getString(R.string.can_not_use_video_file_transfer), Toast.LENGTH_SHORT).show();
                    deleteMessage2database(mData);
                    return;
                }
                String title = OuiBotPreferences.getLoginId(MessageDetailActivity.this);
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
                        json.put(Config.PARAM_FROM, OuiBotPreferences.getLoginId(MessageDetailActivity.this));
                        json.put(SecureSQLiteHelper.COL_FILE_PATH, Config.getFileName(path));
                        json.put(SecureSQLiteHelper.COL_MODE, Config.MESSAGE_FILE_MODE);
                        json.put(SecureSQLiteHelper.COL_TIME, mData.getTime());
                        json.put(Config.PARAM_THREAD_TIME, threadTime);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (MessageDetailActivity.mDataSessionTask != null) {
                        if (MessageDetailActivity.mDataSessionTask.getStatus() == DataSessionSenderAsyncTask.Status.FINISHED) {
                            MessageDetailActivity.mDataSessionTask = new DataSessionSenderAsyncTask(MessageDetailActivity.this, mData.getTime(), json, file, mPeerRtcid, mService, threadTime, Config.MESSAGE_FILE_CHANNEL_5);
                            MessageDetailActivity.mDataSessionTask.executeOnExecutor(MessageDetailActivity.mExec);
                        } else {
                            Toast.makeText(MessageDetailActivity.this, getResources().getString(R.string.already_message_receiving), Toast.LENGTH_SHORT).show();
                            deleteMessageDataInDatabase(mData.getMessageData(), mData.getPeerRtcid(), mData.getTime());
                            removeChatList(mData);
                        }
                    } else {
                        MessageDetailActivity.mDataSessionTask = new DataSessionSenderAsyncTask(MessageDetailActivity.this, mData.getTime(), json, file, mPeerRtcid, mService, threadTime, Config.MESSAGE_FILE_CHANNEL_5);
                        MessageDetailActivity.mDataSessionTask.executeOnExecutor(MessageDetailActivity.mExec);
                    }
                }else{
                    deleteMessageDataInDatabase(mData.getMessageData(), mData.getPeerRtcid(), mData.getTime());
                    removeChatList(mData);
                }
            }

        }
        public void recordMessageSendSucces2Database(long time) {
            if (time != 0) {
                ContentResolver resolver = getContentResolver();
                ContentValues values = new ContentValues();
                values.put(SecureSQLiteHelper.COL_SEND_STATE, MessageListData.SEND_COMPLETE);
                resolver.update(SecureProvider.MESSAGE_TABLE_URI, values, SecureSQLiteHelper.COL_RTCID + " =? AND " + SecureSQLiteHelper.COL_TIME + " =? ", new String[]{OuiBotPreferences.getLoginId(MessageDetailActivity.this), String.valueOf(time)});
            }
        }
    }
}
