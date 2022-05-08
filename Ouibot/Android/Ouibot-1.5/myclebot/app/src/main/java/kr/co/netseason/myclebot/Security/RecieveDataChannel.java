package kr.co.netseason.myclebot.Security;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.util.Log;

import com.ericsson.research.owr.DataChannel;
import com.ericsson.research.owr.DataChannelReadyState;
import com.ericsson.research.owr.MediaSource;
import com.ericsson.research.owr.MediaType;
import com.ericsson.research.owr.sdk.InvalidDescriptionException;
import com.ericsson.research.owr.sdk.RtcCandidate;
import com.ericsson.research.owr.sdk.RtcCandidates;
import com.ericsson.research.owr.sdk.RtcConfig;
import com.ericsson.research.owr.sdk.RtcConfigs;
import com.ericsson.research.owr.sdk.RtcSession;
import com.ericsson.research.owr.sdk.RtcSessions;
import com.ericsson.research.owr.sdk.SessionDescription;
import com.ericsson.research.owr.sdk.SessionDescriptions;
import com.ericsson.research.owr.sdk.StreamMode;
import com.ericsson.research.owr.sdk.StreamSet;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import kr.co.netseason.myclebot.API.MessageListData;
import kr.co.netseason.myclebot.Logger.Logger;
import kr.co.netseason.myclebot.MainActivity;
import kr.co.netseason.myclebot.Provider.SecureProvider;
import kr.co.netseason.myclebot.Provider.SecureSQLiteHelper;
import kr.co.netseason.myclebot.R;
import kr.co.netseason.myclebot.UTIL.OuiBotPreferences;
import kr.co.netseason.myclebot.UTIL.TestsUtils;
import kr.co.netseason.myclebot.openwebrtc.Config;

public class RecieveDataChannel implements RtcSession.OnLocalCandidateListener,
        RtcSession.OnLocalDescriptionListener {
    private String mOuibotID;
    private String TAG = getClass().getName();
    private DataChannel outChannel;
    private RtcSession mRtcSession;
    private RtcConfig mRtcConfig;
    private StreamSetMock streamSet;
    private Messenger mService;
    private JSONObject dataChannelRecvOfferData;
    private List<JSONObject> dataChannelReceiverCandidates = new ArrayList<>();
    private String mDataClassKey;
    private Context mContext;
    public RecieveDataChannel(Context context, Messenger service, JSONObject RecvOffer, String key) {
        mContext = context;
        mDataClassKey = key;
        mService = service;
        try {
            JSONObject jsono = new JSONObject();
            jsono.put(Config.PARAM_TYPE, "offer_ack");
            jsono.put(Config.PARAM_SESSION_ID, RecvOffer.get(Config.PARAM_SESSION_ID));
            jsono.put(Config.PARAM_FROM, RecvOffer.get(Config.PARAM_FROM));
            jsono.put(Config.PARAM_TO, RecvOffer.get(Config.PARAM_TO));
            jsono.put(Config.PARAM_CODE, Config.PARAM_SUCCESS_CODE);
            jsono.put(Config.PARAM_MODE, "file");
            dataChannelRecvOfferData = jsono;
            sendMessageToService(jsono);

            mRtcConfig = RtcConfigs.defaultConfig(Config.STUN_SERVER, Config.TURN_SERVER);
            streamSet = new StreamSetMock("ouibot", Collections.singletonList(data()));
            try {
                mOuibotID = RecvOffer.getString("from");
                mRtcSession = RtcSessions.create(mRtcConfig);
                mRtcSession.setOnLocalCandidateListener(this);
                mRtcSession.setOnLocalDescriptionListener(this);
                if (RecvOffer.has(Config.PARAM_SDP)) {
                    JSONObject sdp = new JSONObject();
                    sdp.put(Config.PARAM_TYPE, RecvOffer.optString(Config.PARAM_TYPE));
                    sdp.put(Config.PARAM_SDP, RecvOffer.optString(Config.PARAM_SDP));

                    SessionDescription sessionDescription = SessionDescriptions.fromJsep(sdp);
                    if (sessionDescription.getType() == SessionDescription.Type.OFFER) {
                        onInboundCall(sessionDescription);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void onInboundCall(final SessionDescription sessionDescription) {
        try {
            mRtcSession.setRemoteDescription(sessionDescription);
            mRtcSession.start(streamSet);
        } catch (InvalidDescriptionException e) {
            e.printStackTrace();
        }
    }

    public void stopSession(String session) {
        Logger.w("!!!!", "soyu stopSession");
        startFlag = false;
        mOuibotID = null;
        streamSet = null;
        mRtcConfig = null;
//        if(outChannel != null){
//            outChannel.close();
//            outChannel = null;
//        }
        if (mRtcSession != null) {
            mRtcSession.stop();
            mRtcSession = null;
        }
        Config.THREAD_RECIEVER_CLASS.remove(session);
        if (Config.Mode == Config.COMPILE_Android) {
            PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "alex");
            Logger.e("!!!", "alex wakelock release");
            if (wakeLock.isHeld()) {
                wakeLock.release();
            }
        }
    }

    public RtcSession getRtcSession() {
        return mRtcSession;
    }

    @Override
    public void onLocalDescription(SessionDescription localDescription) {
        try {
            JSONObject json = new JSONObject();
            json.putOpt(Config.PARAM_SDP, SessionDescriptions.toJsep(localDescription));
            json = json.getJSONObject(Config.PARAM_SDP);
            json.put(Config.PARAM_TYPE, "answer");
            json.put(Config.PARAM_SUB_TYPE, "accept");
            json.put(Config.PARAM_SESSION_ID, dataChannelRecvOfferData.getString(Config.PARAM_SESSION_ID));
            json.put(Config.PARAM_FROM, dataChannelRecvOfferData.getString(Config.PARAM_FROM));
            json.put(Config.PARAM_TO, dataChannelRecvOfferData.getString(Config.PARAM_TO));
            json.put(Config.PARAM_MODE, dataChannelRecvOfferData.getString(Config.PARAM_MODE));
            callAnswerSdpRecv(json);
            sendMessageToService(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocalCandidate(RtcCandidate candidate) {
        Logger.w("!!!!", "RecieveDataChannel candidate = " + candidate.toString());
        try {
            JSONObject json = new JSONObject();
            json.putOpt(Config.PARAM_CANDIDATE_DATA_CHANNEL, RtcCandidates.toJsep(candidate));
            json.putOpt(Config.PARAM_SESSION_ID, dataChannelRecvOfferData.getString(Config.PARAM_SESSION_ID));

            Logger.w("!!!!", startFlag + "] RecieveDataChannel candidate = " + candidate.toString());
            if (!startFlag) {
                dataChannelReceiverCandidates.add(json);
            } else {

                json.put(Config.PARAM_TYPE, Config.PARAM_CANDIDATE);
                json.put(Config.PARAM_SUB_TYPE, "answer");
                json.put(Config.PARAM_SESSION_ID, dataChannelRecvOfferData.getString(Config.PARAM_SESSION_ID));
                json.put(Config.PARAM_TO, dataChannelRecvOfferData.getString(Config.PARAM_TO));
                json.put(Config.PARAM_FROM, dataChannelRecvOfferData.getString(Config.PARAM_FROM));
                sendMessageToService(json);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendCandidate() {
        startFlag = true;
        for (JSONObject candidate : dataChannelReceiverCandidates) {
            try {
                candidate.put(Config.PARAM_TYPE, Config.PARAM_CANDIDATE);
                candidate.put(Config.PARAM_SUB_TYPE, "answer");
                candidate.put(Config.PARAM_SESSION_ID, dataChannelRecvOfferData.getString(Config.PARAM_SESSION_ID));
                candidate.put(Config.PARAM_TO, dataChannelRecvOfferData.getString(Config.PARAM_TO));
                candidate.put(Config.PARAM_FROM, dataChannelRecvOfferData.getString(Config.PARAM_FROM));
                sendMessageToService(candidate);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        dataChannelReceiverCandidates.clear();
    }

    private void sendMessageToService(JSONObject jsono) {
        Message msg = Message.obtain(null, Config.SEND_MESSAGE_TO_WEBSOCKET, jsono);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void callAnswerSdpRecv(JSONObject json) {
        if (json.has(Config.PARAM_SDP)) {
            try {
                JSONObject sdp = new JSONObject();
                sdp.put(Config.PARAM_TYPE, json.optString(Config.PARAM_TYPE));
                sdp.put(Config.PARAM_SDP, json.optString(Config.PARAM_SDP));
                SessionDescription sessionDescription = SessionDescriptions.fromJsep(sdp);
                onInboundCall(sessionDescription);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private int byteRecieveCount = 0;
    private int mFileSliceCount;
    private String detectedFileName;
    private int mFileModeData;
    private String mFileOuibotId;
    private long mFileSaveTime;
    private FileOutputStream outputStream;

    private boolean startFlag = false;

    public void startChannel() {
        Logger.w("!!!!", "soyu startChannel");
        TestsUtils.synchronous().latchCount(1).run(new TestsUtils.SynchronousBlock() {
            @Override
            public void run(final CountDownLatch latch) {
                streamSet.getDataStream().waitForDataChannels(latch);
            }
        });
        if (outChannel == null && streamSet != null) {
            List<DataChannel> data = streamSet.getDataStream().getReceivedDataChannels();
            for (int i = 0; i < data.size(); i++) {
                Logger.d(TAG, "DataChannel data == " + data.get(i).toString());
            }
            try {
                outChannel = data.get(0);
                outChannel.addReadyStateChangeListener(readyStateChangeListener);
                outChannel.addOnDataListener(new DataChannel.OnDataListener() {
                    @Override
                    public void onData(String s) {
                        Logger.d(TAG, "onData data == " + s);
//                        if (s.startsWith(Config.JSON_STRING_FORMAT)) {
//                            s = s.replace(Config.JSON_STRING_FORMAT, "");
                            Logger.d(TAG, "onJsonStringReceive = " + s);
                            byteRecieveCount = 0;
                            try {
                                JSONObject json = new JSONObject(s);
                                mFileSliceCount = json.getInt(Config.COL_FILE_SLICE_COUNT);
                                detectedFileName = json.get(SecureSQLiteHelper.COL_FILE_PATH).toString();
                                mFileModeData = json.getInt(SecureSQLiteHelper.COL_MODE);
                                mFileOuibotId = json.get(Config.PARAM_FROM).toString();
                                mFileSaveTime = json.getLong(SecureSQLiteHelper.COL_TIME);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                outputStream = new FileOutputStream(Config.getSaveImageFileExternalDirectory() + detectedFileName);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (mFileModeData == Config.DETECT_SECURE_MODE || mFileModeData == Config.DETECT_MOVEMENT_MODE) {
                                setSecureData2Database(s);
                            }
//                        }
//                        else {
//
//                            String peerId = s.split(":")[0];
//                            String time = s.split(":")[1];
//                            String message = s.split(":")[2];
//                            if (time.equals("0") && message.equals("0")) {
//                                return;
//                            }
//                            inserMessage2database(makeObjectWithMessage(peerId, message, MessageListData.MESSAGE_TYPE, MessageListData.SEND_COMPLETE));
//                            requestNotificationOnlyChat(peerId, message);
//                            try {
//                                Thread.sleep(300);
//                            }catch (Exception e){
//                                e.printStackTrace();
//                            }
//                            mContext.sendBroadcast(new Intent(Config.INTENT_RECEIVE_MESSAGE_EVENT));
//                            sendMessageSuccessReceive(peerId, time);
//                            stopSession(mDataClassKey);
//                        }
                    }
                });
                outChannel.addOnBinaryDataListener(new DataChannel.OnBinaryDataListener() {
                    @Override
                    public void onBinaryData(byte[] bytes) {
                        try {
                            outputStream.write(bytes);
                            if (byteRecieveCount == mFileSliceCount - 1) {
                                outputStream.flush();
                                outputStream.close();
                                outputStream = null;
                                if (mFileModeData == Config.DETECT_SECURE_MODE || mFileModeData == Config.DETECT_MOVEMENT_MODE) {
                                    setSecureFileData2Database(detectedFileName, Config.getSaveImageFileExternalDirectory() + detectedFileName);
                                    showNotificationORDetectImageView(mFileSaveTime, Config.getSaveImageFileExternalDirectory() + detectedFileName, mFileModeData, mFileOuibotId);
                                } else {
                                    inserMessage2database(makeObjectWithMessage(mFileOuibotId, Config.getSaveImageFileExternalDirectory() + detectedFileName, Config.getFileType(detectedFileName), MessageListData.SEND_COMPLETE));
                                    new MediaScanner(mContext, new File(Config.getSaveImageFileExternalDirectory() + detectedFileName));
                                    requestNotificationOnlyChat(mFileOuibotId, Config.getSaveImageFileExternalDirectory() + detectedFileName, Config.getFileType(detectedFileName));
                                    try {
                                        Thread.sleep(300);
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                    mContext.sendBroadcast(new Intent(Config.INTENT_RECEIVE_MESSAGE_EVENT));
                                    sendFileSuccessReceive(mFileOuibotId, String.valueOf(mFileSaveTime));
                                }
                                stopSession(mDataClassKey);
                            } else {
                                byteRecieveCount++;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }

                    }
                });
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void showNotificationORDetectImageView(long time, String filePath, int mode, String id) {
        Logger.d(TAG, "showNotificationORDetectImageView call " + filePath);
        if (filePath == null) {
            return;
        }
        Intent intent = new Intent(mContext, DetectedItemDetailPopupActivity.class);
        intent.putExtra("image_time_key", time);
        intent.putExtra("image_path_key", filePath);
        intent.putExtra("image_mode_key", mode);
        intent.putExtra("image_id_key", id);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
        requestNotification(mode, id);
        PushWakeLock.acquire(mContext, 3000);

    }

    private synchronized void requestNotification(int mode, String id) {
//        Intent intent = new Intent(mContext, MainActivity.class);
//        intent.putExtra(Config.INTENT_MOVE_TO_LINK_SETTING_VIEW, true);
//        intent.putExtra(Config.INTENT_MOVE_TO_LINK_SETTING_OUIBOT_ID, id);
//        intent.putExtra(Config.INTENT_MOVE_TO_LINK_SETTING_SECURE_MODE, mode);
//        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(mContext);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setWhen(System.currentTimeMillis());
        if (mode == Config.DETECT_SECURE_MODE) {
            builder.setContentTitle(mContext.getResources().getString(R.string.notification_detected_success_string));
        } else {
            builder.setContentTitle(mContext.getResources().getString(R.string.notification_none_activity_success_string));
        }
        builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS);
//        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);
        builder.setPriority(Notification.PRIORITY_MAX);
        NotificationManager nm = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(123456, builder.build());
        PushWakeLock.acquire(mContext,3000);
    }

    public synchronized void sendMessageSuccessReceive(String to, String time) {
        try {
            JSONObject jobj = new JSONObject();
            JSONObject kobj = new JSONObject();
            jobj.put(Config.PARAM_TYPE, Config.PARAM_GET_CONFIG_ACK);
            jobj.put(Config.PARAM_SESSION_ID, time);
            jobj.put(Config.PARAM_FROM, to);
            jobj.put(Config.PARAM_TO, OuiBotPreferences.getLoginId(mContext));
            jobj.put(Config.PARAM_CODE, Config.PARAM_SUCCESS_CODE);
            jobj.put(Config.PARAM_DESCRIPTION, "message_success");
            jobj.put(Config.PARAM_CONFIG, kobj);
            sendMessageToService(jobj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public synchronized void sendFileSuccessReceive(String to, String time) {
        try {
            JSONObject jobj = new JSONObject();
            JSONObject kobj = new JSONObject();
            jobj.put(Config.PARAM_TYPE, Config.PARAM_GET_CONFIG_ACK);
            jobj.put(Config.PARAM_SESSION_ID, time);
            jobj.put(Config.PARAM_FROM, to);
            jobj.put(Config.PARAM_TO, OuiBotPreferences.getLoginId(mContext));
            jobj.put(Config.PARAM_CODE, Config.PARAM_SUCCESS_CODE);
            jobj.put(Config.PARAM_DESCRIPTION, "file_success");
            jobj.put(Config.PARAM_CONFIG, kobj);
            sendMessageToService(jobj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private synchronized void setSecureFileData2Database(String oldFileName, String newPath) {
        try {
            ContentValues values = new ContentValues();
            values.put(SecureSQLiteHelper.COL_FILE_PATH, newPath);
            ContentResolver resolver = mContext.getContentResolver();
            resolver.update(SecureProvider.SECURE_MASTER_TABLE_URI, values, SecureSQLiteHelper.COL_FILE_PATH + "=? ", new String[]{oldFileName});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//
//    private synchronized void requestNotificationOnlyChat(String OuibotId, String chat) {
//        Intent intent = new Intent(mContext, MainActivity.class);
//        intent.putExtra(Config.INTENT_MOVE_TO_MESSAGE_VIEW, true);
//        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        Notification.Builder builder = new Notification.Builder(mContext);
//        builder.setSmallIcon(R.mipmap.ic_launcher);
//        builder.setWhen(System.currentTimeMillis());
//        builder.setContentTitle(OuibotId + " : " + chat);
//        builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS);
//        builder.setContentIntent(pendingIntent);
//        builder.setAutoCancel(true);
//        builder.setPriority(Notification.PRIORITY_MAX);
//        NotificationManager nm = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
//        nm.notify(123456, builder.build());
//    }


    private synchronized void requestNotificationOnlyChat(String OuibotId, String chat, int type) {
        Intent intent = new Intent(mContext, MainActivity.class);
        intent.putExtra(Config.INTENT_MOVE_TO_MESSAGE_VIEW, true);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(mContext);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setWhen(System.currentTimeMillis());
        String data;
        if (type == MessageListData.IMAGE_TYPE) {
            data = mContext.getResources().getString(R.string.file_image_mode);
        } else {
            data = mContext.getResources().getString(R.string.file_video_mode);
        }
        builder.setContentTitle(OuibotId + " : " + data);
        builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);
        builder.setPriority(Notification.PRIORITY_MAX);
        NotificationManager nm = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(123456, builder.build());
        PushWakeLock.acquire(mContext, 3000);

    }

    private synchronized void inserMessage2database(MessageListData data) {
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
        ContentResolver resolver = mContext.getContentResolver();
        Uri uri = resolver.insert(SecureProvider.MESSAGE_TABLE_URI, values);
    }

    public synchronized MessageListData makeObjectWithMessage(String peerId, String data, int type, int sendState) {
        return new MessageListData(OuiBotPreferences.getLoginId(mContext),
                peerId, peerId, data, MessageListData.SEND_FLAG_YOU, System.currentTimeMillis(), MessageListData.UNREAD, MessageListData.UNCHECKED, type, sendState);
    }

    private synchronized void setSecureData2Database(String data) {
        try {
            JSONObject json = new JSONObject(data);
            ContentValues values = new ContentValues();
            values.put(SecureSQLiteHelper.COL_ID, json.get(Config.PARAM_FROM).toString());
            values.put(SecureSQLiteHelper.COL_FILE_PATH, json.get(SecureSQLiteHelper.COL_FILE_PATH).toString());
            values.put(SecureSQLiteHelper.COL_MODE, json.get(SecureSQLiteHelper.COL_MODE).toString());
            values.put(SecureSQLiteHelper.COL_TIME, json.get(SecureSQLiteHelper.COL_TIME).toString());
            ContentResolver resolver = mContext.getContentResolver();
            resolver.insert(SecureProvider.SECURE_MASTER_TABLE_URI, values);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void callRecvCandidate(JSONObject jsono) {
        try {
            if (jsono.has(Config.PARAM_CANDIDATE_DATA_CHANNEL)) {
                JSONObject candidate = jsono.optJSONObject(Config.PARAM_CANDIDATE_DATA_CHANNEL);
                RtcCandidate rtcCandidate = RtcCandidates.fromJsep(candidate);
                if (rtcCandidate != null) {

                    Logger.d(TAG, "callRecvCandidate call mRtcSession = " + mRtcSession);
                    Logger.d(TAG, "callRecvCandidate call rtcCandidate = " + rtcCandidate.toString());
                    mRtcSession.addRemoteCandidate(rtcCandidate);

                } else {
                    Log.e(TAG, "invalid candidate: " + candidate);
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    DataChannel.ReadyStateChangeListener readyStateChangeListener = new DataChannel.ReadyStateChangeListener() {
        @Override
        public void onReadyStateChanged(final DataChannelReadyState dataChannelReadyState) {
            Log.d("!!!", "DATACHANNEL: ready state changed: " + dataChannelReadyState);
        }
    };

    private class StreamSetMock extends StreamSet {
        private final ArrayList<Stream> mStreams;
        private final String mLabel;
        private final boolean mAcceptUnusedStreams;

        public StreamSetMock(final String label, boolean acceptUnusedStreams, List<StreamConfig> configs) {
            mLabel = label;
            mAcceptUnusedStreams = acceptUnusedStreams;
            mStreams = new ArrayList<>(configs.size());
            for (StreamConfig config : configs) {
                if (config.mediaType != null) {
                    mStreams.add(new MediaStreamMock(config));
                } else {
                    mStreams.add(new DataStreamMock());
                }
            }
        }

        public StreamSetMock(final String label, List<StreamConfig> configs) {
            this(label, false, configs);
        }

        public MediaStreamMock getMediaStream(String id) {
            for (Stream stream : mStreams) {
                if (stream instanceof MediaStreamMock) {
                    MediaStreamMock mock = (MediaStreamMock) stream;
                    if (id.equals(mock.getId())) {
                        return mock;
                    }
                }
            }
            return null;
        }

        public DataStreamMock getDataStream() {
            for (Stream stream : mStreams) {
                if (stream instanceof DataStreamMock) {
                    return (DataStreamMock) stream;
                }
            }
            return null;
        }

        @Override
        protected List<Stream> getStreams() {
            return mStreams;
        }

        private class DataStreamMock extends DataStream {
            private StreamMode mStreamMode = null;
            private List<DataChannel> mReceivedDataChannels = new LinkedList<>();
            private CountDownLatch mDataChannelLatch = null;
            private CountDownLatch mModeSetLatch = null;
            private DataChannelDelegate mDataChannelDelegate;

            public synchronized void waitForDataChannels(CountDownLatch latch) {
                mDataChannelLatch = latch;
                for (DataChannel ignored : mReceivedDataChannels) {
                    latch.countDown();
                }
            }

            public StreamMode getStreamMode() {
                return mStreamMode;
            }

            public List<DataChannel> getReceivedDataChannels() {
                return mReceivedDataChannels;
            }

            public void addDataChannel(DataChannel dataChannel) {
                mDataChannelDelegate.addDataChannel(dataChannel);
            }

            @Override
            protected synchronized boolean onDataChannelReceived(final DataChannel dataChannel) {
                Log.e(TAG, "[" + mLabel + "] data channel received: " + dataChannel);
                mReceivedDataChannels.add(dataChannel);
                if (mDataChannelLatch != null) {
                    mDataChannelLatch.countDown();
                }
                return true;
            }

            @Override
            protected void setDataChannelDelegate(final DataChannelDelegate dataChannelDelegate) {
                mDataChannelDelegate = dataChannelDelegate;
                Log.e(TAG, "[" + mLabel + "] data channel delegate set: " + dataChannelDelegate);
            }

            @Override
            public void setStreamMode(final StreamMode mode) {
                mStreamMode = mode;
                if (mModeSetLatch != null) {
                    mModeSetLatch.countDown();
                    mModeSetLatch = null;
                }
            }

            public void waitUntilActive(final CountDownLatch latch) {
                mModeSetLatch = latch;
                if (mStreamMode != null) {
                    mModeSetLatch.countDown();
                    mModeSetLatch = null;
                }
            }
        }

        private class MediaStreamMock extends MediaStream {
            private MediaSource mRemoteSource;
            private MediaSourceDelegate mMediaSourceDelegate;
            private StreamMode mStreamMode = null;
            private MediaSource mLocalSource;

            private final StreamConfig mConfig;
            private CountDownLatch mCountDownLatch = null;

            public MediaStreamMock(final StreamConfig config) {
                mConfig = config;
            }

            public boolean haveRemoteSource() {
                return mRemoteSource != null;
            }

            public boolean haveMediaSourceDelegate() {
                return mMediaSourceDelegate != null;
            }

            public MediaSourceDelegate getMediaSourceDelegate() {
                return mMediaSourceDelegate;
            }

            public MediaSource getRemoteSource() {
                return mRemoteSource;
            }

            public StreamMode getStreamMode() {
                return mStreamMode;
            }

            public synchronized void waitForRemoteSource(final CountDownLatch remoteSourceLatch) {
                if (haveRemoteSource()) {
                    Log.d(TAG, "[" + mLabel + "] already had remote source for " + getId());
                    remoteSourceLatch.countDown();
                    return;
                }
                Log.d(TAG, "[" + mLabel + "] waiting for remote source for " + getId());
                mCountDownLatch = remoteSourceLatch;
            }

            @Override
            protected String getId() {
                return mConfig.id;
            }

            @Override
            protected MediaType getMediaType() {
                return mConfig.mediaType;
            }

            @Override
            protected boolean wantSend() {
                return mConfig.wantSend;
            }

            @Override
            protected boolean wantReceive() {
                return mConfig.wantReceive;
            }

            @Override
            protected boolean getMediaSourceData() {
                if (mRemoteSource == null) {
                    return false;
                } else {
                    return true;
                }

            }

            @Override
            protected synchronized void onRemoteMediaSource(final MediaSource mediaSource) {
                Log.v(TAG, "[" + mLabel + "] got remote source for " + getId() + " : " + mediaSource);
                mRemoteSource = mediaSource;
                if (mCountDownLatch != null) {
                    mCountDownLatch.countDown();
                }
            }

            @Override
            protected void setMediaSourceDelegate(final MediaSourceDelegate mediaSourceDelegate) {
                mMediaSourceDelegate = mediaSourceDelegate;
                if (mLocalSource != null && mediaSourceDelegate != null) {
                    Log.v(TAG, "[" + mLabel + "] local source set for " + getId() + " : " + mLocalSource);
                    mediaSourceDelegate.setMediaSource(mLocalSource);
                } else {
                    Log.v(TAG, "[" + mLabel + "] local source not set for " + getId() + " : " + mediaSourceDelegate);
                }
            }

            @Override
            public void setStreamMode(final StreamMode mode) {
                mStreamMode = mode;
            }

            public void setMediaSource(final MediaSource mediaSource) {
                Log.v(TAG, "[" + mLabel + "] local source stored for " + getId() + " : " + mediaSource);
                mLocalSource = mediaSource;
            }
        }
    }

    private static StreamConfig video(String id, boolean wantSend, boolean wantReceive) {
        return new StreamConfig(id, wantSend, wantReceive, MediaType.VIDEO);
    }

    private static StreamConfig audio(String id, boolean wantSend, boolean wantReceive) {
        return new StreamConfig(id, wantSend, wantReceive, MediaType.AUDIO);
    }

    private static StreamConfig data() {
        return new StreamConfig();
    }

    private static class StreamConfig {
        private String id;
        private boolean wantSend;
        private boolean wantReceive;
        private MediaType mediaType;

        private StreamConfig(String id, boolean wantSend, boolean wantReceive, MediaType mediaType) {
            this.id = id;
            this.wantSend = wantSend;
            this.wantReceive = wantReceive;
            this.mediaType = mediaType;
        }

        private StreamConfig() {
            this.mediaType = null;
        }
    }
}
