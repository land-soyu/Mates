package kr.co.netseason.myclebot.Security;

import android.content.Context;
import android.os.Message;
import android.os.Messenger;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import kr.co.netseason.myclebot.Logger.Logger;
import kr.co.netseason.myclebot.UTIL.OuiBotPreferences;
import kr.co.netseason.myclebot.openwebrtc.Config;

import static junit.framework.Assert.assertSame;

/**
 * Created by tbzm on 15. 10. 21.
 */
public class SenderDataChannel implements RtcSession.OnLocalCandidateListener,
        RtcSession.OnLocalDescriptionListener {
    private Context mContext;
    private String mOuibotID;
    private String TAG = getClass().getName();
    private DataChannel outChannel;
    private RtcSession mRtcSession;
    private RtcConfig mRtcConfig;
    private StreamSetMock streamSet;
    private Messenger mService;
    private JSONObject mMessage;
    private File mFileByte;
    private JSONObject dataChannelSendOfferData;
    private List<JSONObject> dataChannelOfferCandidates = new ArrayList<>();
    private String mDataClassKey;
    private short mChannel;

    private boolean startFlag = false;

    public SenderDataChannel(Context context, String ouibotId, Messenger service, String key, short channel) {
        mContext = context;
        mService = service;
        mOuibotID = ouibotId;
        mDataClassKey = key;
        mChannel = channel;
    }

//    public void offerSessionStart(String message, String time) {
//        mTime = time;
//        mMessage = message;
//        mFileByte = null;
//        startSession();
//    }

    public void offerSessionStart(JSONObject message, File file) {
        mMessage = message;
        mFileByte = file;
        startSession();
    }

    public void startSession() {
        streamSet = new StreamSetMock("ouibot", Collections.singletonList(data()));
        mRtcConfig = RtcConfigs.defaultConfig(Config.STUN_SERVER, Config.TURN_SERVER);
        mRtcSession = RtcSessions.create(mRtcConfig);
        mRtcSession.setOnLocalCandidateListener(this);
        mRtcSession.setOnLocalDescriptionListener(this);
        mRtcSession.start(streamSet);
    }

    public void stopSession(String session) {
        startFlag = false;
        mOuibotID = null;
        streamSet = null;
        mRtcConfig = null;
        mMessage = null;
//        if(outChannel != null){
//            outChannel.close();
//            outChannel = null;
//        }
        if (mRtcSession != null) {
            mRtcSession.stop();
            mRtcSession = null;
        }
        Config.THREAD_SENDER_CLASS.remove(session);
    }

//    public short getRandomChannel() {
//        Random random = new Random();
//        int result = random.nextInt(9) + 1;
//        short data = 1;
//        if (result == 2 || result == 4 || result == 6) {
//            data = (short) (result + 1);
//        }
//        if (result == 7 || result == 8) {
//            data = (short) (9);
//        }
//        Logger.d(TAG, "data == " + data);
//        return data;
//    }

    public RtcSession getRtcSession() {
        return mRtcSession;
    }

    private void createDataChannel() {
        Logger.d(TAG, "createDataChannel call");
        outChannel = new DataChannel(true, -1, 10, "UTPE", false, mChannel, "test");
        outChannel.addReadyStateChangeListener(new DataChannel.ReadyStateChangeListener() {
            @Override
            public void onReadyStateChanged(DataChannelReadyState dataChannelReadyState) {
                Logger.d(TAG, "onReadyStateChanged call");
                if (mMessage != null && mFileByte != null) {
                    outChannel.send(mMessage.toString());
                    try {
                        Thread.sleep(200);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Logger.d(TAG, "original file size  = " + mFileByte.length());
                    sendFile(mFileByte);
//                } else if (mMessage != null) {
//                    Logger.d(TAG, "message send =" + mMessage);
//                    outChannel.send(OuiBotPreferences.getLoginId(mContext) + ":" + mTime + ":" + mMessage);
                } else {
                    sendFile(mFileByte);
                }
                stopSession(mDataClassKey);
            }
        });
        outChannel.addOnDataListener(new DataChannel.OnDataListener() {
            @Override
            public void onData(String s) {
                Log.e(TAG, "outChannel addOnDataListener s = " + s);
            }
        });
        outChannel.addOnBinaryDataListener(new DataChannel.OnBinaryDataListener() {
            @Override
            public void onBinaryData(byte[] bytes) {
                Log.e(TAG, "outChannel addOnBinaryDataListener s = " + new String(bytes));

            }
        });
        streamSet.getDataStream().addDataChannel(outChannel);
    }

    private void sendFile(File file) {
        if (outChannel != null) {
            try {
                int sliceNum = Config.getFileSliceNum(file.getPath());
                byte[] totalByte = Config.readBytesFromFile(file);
                for (int i = 0; i < sliceNum; i++) {
                    byte[] slice;
                    int startIndexByte = i * Config.MAX_BYTE_NUM;
                    int endIndexByte;
                    if (i == sliceNum - 1) {
                        endIndexByte = (int) file.length();
                    } else {
                        endIndexByte = startIndexByte + Config.MAX_BYTE_NUM;
                    }
                    Logger.d(TAG, startIndexByte + " 부터 = " + endIndexByte + " 까지 ");
                    slice = Arrays.copyOfRange(totalByte, startIndexByte, endIndexByte);
                    outChannel.sendBinary(slice);
                }
                totalByte = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onLocalDescription(SessionDescription localDescription) {
        try {
            JSONObject json = new JSONObject();
            json.putOpt(Config.PARAM_SDP, SessionDescriptions.toJsep(localDescription));
            json = json.getJSONObject(Config.PARAM_SDP);
            json.put(Config.PARAM_TYPE, "offer");
            json.put(Config.PARAM_SESSION_ID, mDataClassKey);
            json.put(Config.PARAM_FROM, OuiBotPreferences.getLoginId(mContext));
            json.put(Config.PARAM_TO, mOuibotID);
            json.put(Config.PARAM_THREAD_TIME, mMessage.getString(Config.PARAM_THREAD_TIME));
//            if (mChannel == Config.MESSAGE_CHANNEL_1) {
//                json.put(Config.PARAM_MODE, "message");
//            } else if (mChannel == Config.SECURE_IMAGE_CHANNEL_3) {
//                json.put(Config.PARAM_MODE, "secure_image");
//            } else {
                json.put(Config.PARAM_MODE, "file");
//            }
            dataChannelSendOfferData = json;
            sendMessageToService(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocalCandidate(RtcCandidate candidate) {
        Logger.w("!!!!", "SenderDataChannel candidate = " + candidate.toString());
        try {
            JSONObject json = new JSONObject();
            json.putOpt(Config.PARAM_CANDIDATE_DATA_CHANNEL, RtcCandidates.toJsep(candidate));
            json.putOpt(Config.PARAM_SESSION_ID, dataChannelSendOfferData.getString(Config.PARAM_SESSION_ID));
            if ( !startFlag ) {
                dataChannelOfferCandidates.add(json);
            } else {
                json.put(Config.PARAM_TYPE, Config.PARAM_CANDIDATE);
                json.put(Config.PARAM_SUB_TYPE, "offer");
                json.put(Config.PARAM_SESSION_ID, dataChannelSendOfferData.getString(Config.PARAM_SESSION_ID));
                json.put(Config.PARAM_TO, dataChannelSendOfferData.getString(Config.PARAM_TO));
                json.put(Config.PARAM_FROM, dataChannelSendOfferData.getString(Config.PARAM_FROM));
                sendMessageToService(json);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendCandidate() {
        startFlag = true;
        for (JSONObject candidate : dataChannelOfferCandidates) {
            try {
                candidate.put(Config.PARAM_TYPE, Config.PARAM_CANDIDATE);
                candidate.put(Config.PARAM_SUB_TYPE, "offer");
                candidate.put(Config.PARAM_SESSION_ID, dataChannelSendOfferData.getString(Config.PARAM_SESSION_ID));
                candidate.put(Config.PARAM_TO, dataChannelSendOfferData.getString(Config.PARAM_TO));
                candidate.put(Config.PARAM_FROM, dataChannelSendOfferData.getString(Config.PARAM_FROM));
                sendMessageToService(candidate);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        try {
            Thread.sleep(1000);
        }catch (Exception e){
            e.printStackTrace();
        }
        startDataChannel();
        JSONObject newCandidate = new JSONObject();
        try {
            JSONObject candidate = dataChannelOfferCandidates.get(0);
            newCandidate.put(Config.PARAM_TO, candidate.getString(Config.PARAM_FROM));
            newCandidate.put(Config.PARAM_FROM, candidate.getString(Config.PARAM_TO));
            newCandidate.put(Config.PARAM_SESSION_ID, candidate.getString(Config.PARAM_SESSION_ID));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendMessageSetCofigAck(newCandidate);
        dataChannelOfferCandidates.clear();
    }

    private synchronized void sendMessageSetCofigAck(JSONObject json) {
        try {
            JSONObject jobj = new JSONObject();
            jobj.put(Config.PARAM_TYPE, Config.PARAM_SET_CONFIG_ACK);
            jobj.put(Config.PARAM_SESSION_ID, json.get(Config.PARAM_SESSION_ID));
            jobj.put(Config.PARAM_FROM, json.get(Config.PARAM_FROM));
            jobj.put(Config.PARAM_TO, json.get(Config.PARAM_TO));
            jobj.put(Config.PARAM_CODE, Config.PARAM_SUCCESS_CODE);
            jobj.put(Config.PARAM_DESCRIPTION, Config.END_OFFER_CANDIDATA);
            sendMessageToService(jobj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
                onAnswer(sessionDescription);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void callRecvCandidate(JSONObject jsono) {
        if (jsono.has(Config.PARAM_CANDIDATE_DATA_CHANNEL)) {
            JSONObject candidate = jsono.optJSONObject(Config.PARAM_CANDIDATE_DATA_CHANNEL);
            RtcCandidate rtcCandidate = RtcCandidates.fromJsep(candidate);
            if (rtcCandidate != null) {
                mRtcSession.addRemoteCandidate(rtcCandidate);
                Logger.d(TAG, "call Send addRemoteCandidate call");
            } else {
                Log.e(TAG, "invalid candidate: " + candidate);
            }
        }
    }

    public void startDataChannel() {
        assertSame(StreamMode.SEND_RECEIVE, streamSet.getDataStream().getStreamMode());
        createDataChannel();
    }

    private void onAnswer(final SessionDescription sessionDescription) {
        if (mRtcSession != null) {
            try {
                mRtcSession.setRemoteDescription(sessionDescription);
            } catch (InvalidDescriptionException e) {
                e.printStackTrace();
            }
        }
    }

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

//        public MediaStreamMock getMediaStream(String id) {
//            for (Stream stream : mStreams) {
//                if (stream instanceof MediaStreamMock) {
//                    MediaStreamMock mock = (MediaStreamMock) stream;
//                    if (id.equals(mock.getId())) {
//                        return mock;
//                    }
//                }
//            }
//            return null;
//        }

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

//            public synchronized void waitForDataChannels(CountDownLatch latch) {
//                mDataChannelLatch = latch;
//                for (DataChannel ignored : mReceivedDataChannels) {
//                    latch.countDown();
//                }
//            }

            public StreamMode getStreamMode() {
                return mStreamMode;
            }

//            public List<DataChannel> getReceivedDataChannels() {
//                return mReceivedDataChannels;
//            }

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
//
//    private static StreamConfig video(String id, boolean wantSend, boolean wantReceive) {
//        return new StreamConfig(id, wantSend, wantReceive, MediaType.VIDEO);
//    }
//
//    private static StreamConfig audio(String id, boolean wantSend, boolean wantReceive) {
//        return new StreamConfig(id, wantSend, wantReceive, MediaType.AUDIO);
//    }

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
