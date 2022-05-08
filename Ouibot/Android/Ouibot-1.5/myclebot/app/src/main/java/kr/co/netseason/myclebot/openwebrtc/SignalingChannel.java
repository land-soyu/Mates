/*
 * Copyright (c) 2014, Ericsson AB. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 */

package kr.co.netseason.myclebot.openwebrtc;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.audiofx.*;
import android.media.audiofx.AudioEffect;
import android.media.audiofx.AutomaticGainControl;
import android.media.audiofx.NoiseSuppressor;
import android.media.effect.Effect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.widget.Toast;

import com.ericsson.research.owr.Owr;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import kr.co.netseason.myclebot.API.MessageListData;
import kr.co.netseason.myclebot.CctvActivity;
import kr.co.netseason.myclebot.DeviceAddInitActivity;
import kr.co.netseason.myclebot.IntroActivity;
import kr.co.netseason.myclebot.Logger.Logger;
import kr.co.netseason.myclebot.MainActivity;
import kr.co.netseason.myclebot.MasterRequestPopupActivity;
import kr.co.netseason.myclebot.MasterSettingResultActivity;
import kr.co.netseason.myclebot.MessageDetailActivity;
import kr.co.netseason.myclebot.PetActivity;
import kr.co.netseason.myclebot.Provider.SecureProvider;
import kr.co.netseason.myclebot.Provider.SecureSQLiteHelper;
import kr.co.netseason.myclebot.R;
import kr.co.netseason.myclebot.SecureStartedFailPopupActivity;
import kr.co.netseason.myclebot.Security.DataSessionFileReceiveAsyncTask;
import kr.co.netseason.myclebot.Security.DataSessionMessageReceiveAsyncTask;
import kr.co.netseason.myclebot.Security.DataSessionSenderAsyncTask;
import kr.co.netseason.myclebot.Security.DetectedItemDetailPopupActivity;
import kr.co.netseason.myclebot.Security.ForIOSNotificationHttpAsyncTask;
import kr.co.netseason.myclebot.Security.MediaScanner;
import kr.co.netseason.myclebot.Security.MessageSenderAsyncTaskWithWebSocket;
import kr.co.netseason.myclebot.Security.PushWakeLock;
import kr.co.netseason.myclebot.Security.SecureActivity;
import kr.co.netseason.myclebot.Security.SecureDetectedData;
import kr.co.netseason.myclebot.Security.SecureImageSenderAsyncTaskWithWebSocket;
import kr.co.netseason.myclebot.Security.SecurePreference;
import kr.co.netseason.myclebot.Security.SenderDataChannel;
import kr.co.netseason.myclebot.Security.UibotListData;
import kr.co.netseason.myclebot.Service.BootReceiver;
import kr.co.netseason.myclebot.UTIL.Installation;
import kr.co.netseason.myclebot.UTIL.OuiBotPreferences;
import kr.co.netseason.myclebot.ViewRequestPopupActivity;

public class SignalingChannel extends Service {
    public static final int FLAG_WIFI_CONNECTED = 1;
    public static final int FLAG_WIFI_DISCONNECTED = 2;
    public static final int FLAG_ETHERNET_CONNECTED = 4;
    public static final int FLAG_ETHERNET_DISCONNECTED = 8;
    public static final int FLAG_MOBILE_CONNECTED = 4;
    public static final int FLAG_MOBILE_DISCONNECTED = 8;
    public static int cur_net_stat = 10;
    private DataSessionFileReceiveAsyncTask mDataSessionReceiveTask;
    public final String led_blue = "ioctl /dev/led_ctrl 0";
    public final String led_red = "ioctl /dev/led_ctrl 1";
    public static Messenger messenger;
    private WebSocketClient mWebSocketClient;
    private Messenger mainMessenger;
    private Messenger callMessenger;
    private final String TAG = SignalingChannel.this.getClass().getName();
    private String myPhoneNumber = "";
    private String CalledNumber = "";
    private JSONObject SendOfferData;
    private NetworkReceiver mNetworkReceiver;
    private JSONObject RecvOfferData;
    //    private int byteRecieveCount = 0;
    private JSONObject HangupData;
    private boolean callendFlag;
    private List<JSONObject> Candidates = new ArrayList<>();
    private int CALL_MODE = 0;
    //    private String detectedFileName;
//    private int mFileModeData;
//    private String mFileOuibotId;
//    private long mFileSaveTime;
    private PowerManager.WakeLock wakeLock = null;


    private boolean isForceStartSecureMode;
    private FileOutputStream outputStream;
    private Handler checkackHandler = new Handler();
    private Runnable checkackRunnable = new Runnable() {
        @Override
        public void run() {
            Logger.e("!!!", "checkackRunnable run");
            try {
                JSONObject jsono = new JSONObject();
                jsono.put(Config.PARAM_TYPE, "check");
                jsono.put(Config.PARAM_SESSION_ID, Long.toString(System.currentTimeMillis()));
                jsono.put(Config.PARAM_RTCID, OuiBotPreferences.getLoginId(getApplicationContext()));
                jsono.put(Config.PARAM_UUID, OuiBotPreferences.getUUID(getApplicationContext()));
                sendMessageToService(jsono.toString());
                Logger.e("!!!", "***** alex checkackRunnable *****");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    private void run_check() {
        Logger.e("!!!", "run_check");
        try {
            JSONObject jsono = new JSONObject();
            jsono.put(Config.PARAM_TYPE, "check");
            jsono.put(Config.PARAM_SESSION_ID, Long.toString(System.currentTimeMillis()));
            jsono.put(Config.PARAM_RTCID, OuiBotPreferences.getLoginId(getApplicationContext()));
            jsono.put(Config.PARAM_UUID, OuiBotPreferences.getUUID(getApplicationContext()));
            sendMessageToService(jsono.toString());
            Logger.e("!!!", "***** alex run_check *****");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //    private void setAlarm(Context context, long second) {
    private void setAlarm() {
        Logger.e("!!!", "setAlarm");
        if ( OuiBotPreferences.getLoginId(getApplicationContext()) != null ) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

            PendingIntent sender = PendingIntent.getBroadcast(this, 0, new Intent("ACTION.SEND.CHECK"), 0);


//        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + second, sender);
//        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + second, second, sender);
//        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, second, second, sender);
//        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, second, second, sender);
//        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 15 * 60 * 1000, 15 * 60 * 1000, sender);
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, AlarmManager.INTERVAL_HOUR, AlarmManager.INTERVAL_HOUR, sender);
            Logger.e("!!!", "alex setAlarm");
        }
    }

    private void send_check_ack() {
        Logger.e("!!!", "send_check_ack");

//        checkackHandler.removeCallbacks(checkackRunnable);
//        if (Config.Mode == Config.COMPILE_Ouibot) {
//            checkackHandler.postDelayed(checkackRunnable, 1000 * 60 * 4);
//        } else {
//            checkackHandler.postDelayed(checkackRunnable, 1000 * 60 * 60);
//        }

        setAlarm();
    }


    /**
     * Initialize OpenWebRTC at startup
     */
    static {
        Owr.init();
        Owr.runInBackground();
    }

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Logger.e("!!!", "IncomingHandler handleMessage = "+msg.what);
            switch (msg.what) {
                case Config.START_APP:
                    if(mWebSocketClient == null) {
                        connectingWebSocket();
                    }else{
                        //연결을 끊었다가 다시 연결하는 이슈가 있으므로 위와같이 처리
                        Logger.d(TAG,"mWebSocketClient == "+mWebSocketClient+"do not connect WebSocket ");
                        Logger.d(TAG,"mWebSocketClient == "+mWebSocketClient.getConnection().isClosed());
                        if(Config.Mode ==Config.COMPILE_Ouibot){
                            sendMessageForMasterExistInDatabase(getMasterList(OuiBotPreferences.getLoginId(SignalingChannel.this)));
                        }
                        setAllMessageFailIfSeding(OuiBotPreferences.getLoginId(getApplicationContext()));
                    }
                    break;
                case Config.MAIN_START:
                    mainMessenger = msg.replyTo;
                    myPhoneNumber = msg.obj.toString();
                    break;
                case Config.CHECK_CONNECTED_CALL:
                    try {
                        if (checkConnectedPhoneCall()) {
                            Message call = Message.obtain(null, Config.CHECK_CONNECTED_CALL, null);
                            msg.replyTo.send(call);
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case Config.CALL_START:
                    Logger.e("!!!", "*****     CALL_START get Messenger     *****");
                    callMessenger = msg.replyTo;
                    Logger.e("!!!", "*****     CALL_START get Messenger     *****     callMessenger = " + callMessenger);
                    myPhoneNumber = msg.obj.toString();
                    if ( callendFlag ) {
                        callendFlag = false;
                        if ( HangupData != null ) {
                            sendMessage_hangup_ack(HangupData);
                            try {
                                if (callMessenger != null) {
                                    Message msg_hangup = Message.obtain(null, Config.HANGUP_RECV, HangupData);
                                    callMessenger.send(msg_hangup);
                                    CalledNumber = "";
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            HangupData = null;
                        }
                    }
                    break;
                case Config.CALLED_NUMBER:
                    callMessenger = msg.replyTo;
                    CalledNumber = msg.obj.toString();
                    break;
                case Config.MESSAGE_ACTIVITY_STARTED:
                    Logger.e("!!!", "recevi MESSAGE_START");
                    myPhoneNumber = msg.obj.toString();
                    break;
                case Config.CALL_ACTIVITY_START:
                    startActivity(Config.MODE_CALL, msg.obj.toString());
                    break;
                case Config.CCTV_ACTIVITY_START:
                    startActivity(Config.MODE_CCTV, msg.obj.toString());
                    break;
                case Config.PET_ACTIVITY_START:
                    startActivity(Config.MODE_PET, msg.obj.toString());
                    break;
                case Config.CALL_OFFER_SDP_SEND:
                    callMessenger = msg.replyTo;
                    offerSDPSend("call", (JSONObject) msg.obj);
                    break;
                case Config.CCTV_OFFER_SDP_SEND:
                    callMessenger = msg.replyTo;
                    offerSDPSend("cctv", (JSONObject) msg.obj);
                    break;
                case Config.PET_OFFER_SDP_SEND:
                    callMessenger = msg.replyTo;
                    offerSDPSend("pet", (JSONObject) msg.obj);
                    break;
                case Config.ANSWER_SDP_SEND:
                    callMessenger = msg.replyTo;
                    answerSDPSend((JSONObject) msg.obj);
                    break;
                case Config.OFFER_CANDIDATE_SEND:
                    answerCandidateSave((JSONObject) msg.obj);
                    break;
                case Config.ANSWER_CANDIDATE_SEND:
                    answerCandidateSave((JSONObject) msg.obj);
                    break;
                case Config.HANGUP_SEND:
                    HangupSend();
                    break;
                case Config.REJECT_SEND:
                    RejectSend();
                    break;
                case Config.REQUEST_MASTER:
                    requestMaster(msg.obj.toString());
                    break;
                case Config.ALLOW_MASTER:
                    if (registerMaster2Databse(msg.replyTo, (JSONObject) msg.obj)) {
                        allow2Master((JSONObject) msg.obj);
                    }
                    break;
                case Config.NOT_ALLOW_MASTER:
                    notAllow2Master((JSONObject) msg.obj);
                    break;
                case Config.GET_SECURE_CONFIG:
                    getSecureConfig(msg.obj.toString());
                    break;
                case Config.START_SECURE_ACTIVITY:
                    startSecureActivityRequest2Server(msg.obj.toString());
                    break;
                case Config.FINISH_SECURE_ACTIVITY:
                    finishSecureActivityRequest2Server(msg.obj.toString());
                    break;
                case Config.DELETE_MASTER_NOTIFY:
                    sendMasterDeleteWithCofigAck(msg.obj.toString());
                    break;
                case Config.DELETE_SLAVE_NOTIFY:
                    sendSlaveDeleteWithCofigAck(msg.obj.toString());
                    break;
                case Config.REQUEST_CONFIG_CHANGE:
                    requestConfigChange((JSONObject) msg.obj);
                    break;
                case Config.ACTIVITY_END:
                    sendMessage_activityend();
                    break;
                case Config.GET_MASTER_ID: {
                    Message dataMessage = Message.obtain(null, Config.GET_MASTER_LIST_DATA, getMasterList(msg.obj.toString()));
                    try {
                        msg.replyTo.send(dataMessage);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case Config.GET_SLAVE_ID: {
                    Message dataMessage = Message.obtain(null, Config.GET_SLAVE_LIST_DATA, getSlaveList(msg.obj.toString()));
                    try {
                        msg.replyTo.send(dataMessage);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case Config.CAM_MOVE_LEFT: {
                    sendCamMove(CalledNumber, "CAM_MOVE_LEFT");
                    break;
                }
                case Config.CAM_MOVE_RIGHT: {
                    sendCamMove(CalledNumber, "CAM_MOVE_RIGHT");
                    break;
                }
                case Config.CAM_MOVE_UP: {
                    sendCamMove(CalledNumber, "CAM_MOVE_UP");
                    break;
                }
                case Config.CAM_MOVE_DOWN: {
                    sendCamMove(CalledNumber, "CAM_MOVE_DOWN");
                    break;
                }
                case Config.SEND_SOUND_FALSE: {
                    sendCamMove(CalledNumber, "SEND_SOUND_FALSE");
                    break;
                }
                case Config.SEND_SOUND_TRUE: {
                    sendCamMove(CalledNumber, "SEND_SOUND_TRUE");
                    break;
                }
                case Config.RING_END: {
                    ringtone(false);
                    break;
                }
                case Config.RINGTONE_END: {
                    ringtone_end();
                    break;
                }
                case Config.RINGTONE_START: {
                    ringtone(true);
                    break;
                }

                case Config.CERTIFICATION_SEND: {
                    sendCamMove(msg.obj.toString(), "CERTIFICATION_SEND");
                    break;
                }
                case Config.CERTIFICATION_ANSWER: {
                    sendCamMove(msg.obj.toString(), "CERTIFICATION_ANSWER");
                    break;
                }
                case Config.SET_CONFIG_ARK_BROADCAST: {
                    sendMessageSetCofigAckBroasdCast(msg.obj.toString());
                    break;
                }
                case Config.SET_CONFIG_ARK_BROADCAST_WITH_NOTIFICATION: {
                    sendMessageSetCofigAckBroasdCastWithNotification(msg.obj.toString());
                    break;
                }
                case Config.SET_CONFIG_ARK_FAIL_BROADCAST: {
                    sendMessageSetCofigFailAckBroasdCast(msg.obj.toString());
                    break;
                }
                case Config.SEND_MESSAGE_TO_WEBSOCKET: {
                    sendMessageToService(msg.obj.toString());
                    break;
                }
                case Config.LOGOUT: {
                    sendMessage_Logout();
                    break;
                }
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void sendMessage_activityend() {
        Logger.e("!!!", "sendMessage_activityend");
        link_flag = 0;
        callMessenger = null;
        SendOfferData = null;
        RecvOfferData = null;
        Candidates.clear();
//        if (mainMessenger != null) {
//            try {
//                Message msg_hangup = Message.obtain(null, Config.ACTIVITY_END, null);
//                mainMessenger.send(msg_hangup);
//            } catch (RemoteException e) {
//                e.printStackTrace();
//            }
//        }

        Intent intent = new Intent("com.ouibot.call.flag");
        intent.putExtra("flag", 1);
        sendBroadcast(intent);
    }

    private void startSecureActivityRequest2Server(String to) {
        Logger.e("!!!", "startSecureActivityRequest2Server");
        try {
            JSONObject jsono = new JSONObject();
            jsono.put(Config.PARAM_TYPE, Config.PARAM_SET_CONFIG);
            jsono.put(Config.PARAM_SESSION_ID, Long.toString(System.currentTimeMillis()));
            jsono.put(Config.PARAM_FROM, myPhoneNumber);
            jsono.put(Config.PARAM_TO, to);
            jsono.put(Config.PARAM_CONFIG, new JSONObject().put(Config.PARAM_DETECT_ONOFF, Config.DETECT_ON));
            sendMessageToService(jsono.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void finishSecureActivityRequest2Server(String to) {
        Logger.e("!!!", "finishSecureActivityRequest2Server");
        try {
            JSONObject jsono = new JSONObject();
            jsono.put(Config.PARAM_TYPE, Config.PARAM_SET_CONFIG);
            jsono.put(Config.PARAM_SESSION_ID, Long.toString(System.currentTimeMillis()));
            jsono.put(Config.PARAM_FROM, myPhoneNumber);
            jsono.put(Config.PARAM_TO, to);
            jsono.put(Config.PARAM_CONFIG, new JSONObject().put(Config.PARAM_DETECT_ONOFF, Config.DETECT_OFF));
            sendMessageToService(jsono.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void RejectSend() {
        Logger.e("!!!", "RejectSend");
        try {
            JSONObject jsono = new JSONObject();
            jsono.put(Config.PARAM_TYPE, "answer");

            jsono.put(Config.PARAM_SESSION_ID, RecvOfferData.getString(Config.PARAM_SESSION_ID));
            jsono.put(Config.PARAM_SUB_TYPE, "reject");
            jsono.put(Config.PARAM_FROM, RecvOfferData.getString(Config.PARAM_FROM));
            jsono.put(Config.PARAM_TO, RecvOfferData.getString(Config.PARAM_TO));
            jsono.put(Config.PARAM_MODE, RecvOfferData.getString(Config.PARAM_MODE));

            sendMessageToService(jsono.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void HangupSend() {
        Logger.e("!!!", "HangupSend");
        try {
            JSONObject jsono = new JSONObject();
            jsono.put(Config.PARAM_TYPE, "hangup");

            Logger.e("!!!", "HangupSend");
            Logger.e("!!!", "SendOfferData = " + SendOfferData);
            Logger.e("!!!", "RecvOfferData = " + RecvOfferData);

            if (RecvOfferData != null) {
                jsono.put(Config.PARAM_SESSION_ID, RecvOfferData.getString(Config.PARAM_SESSION_ID));
                jsono.put(Config.PARAM_SUB_TYPE, "answer");
                jsono.put(Config.PARAM_FROM, RecvOfferData.getString(Config.PARAM_FROM));
                jsono.put(Config.PARAM_TO, RecvOfferData.getString(Config.PARAM_TO));
            } else if(SendOfferData != null){
                jsono.put(Config.PARAM_SESSION_ID, SendOfferData.getString(Config.PARAM_SESSION_ID));
                jsono.put(Config.PARAM_SUB_TYPE, "offer");
                jsono.put(Config.PARAM_FROM, SendOfferData.getString(Config.PARAM_FROM));
                jsono.put(Config.PARAM_TO, SendOfferData.getString(Config.PARAM_TO));
            }else{
                //2초뒤에 종료 액티비티 종료만 해야함
                //아이폰에 Push만 전달한 상태
                Intent i = new Intent("finish_call_activity");
                sendBroadcast(i);
                return;
            }

            sendMessageToService(jsono.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void answerCandidateSave(JSONObject obj) {
        Logger.e("!!!", "answerCandidateSave link_flag = "+link_flag);
        switch (link_flag) {
            case 1:
                try {
                    obj.put(Config.PARAM_TYPE, Config.PARAM_CANDIDATE);
                    obj.put(Config.PARAM_SUB_TYPE, "offer");
                    obj.put(Config.PARAM_SESSION_ID, SendOfferData.getString(Config.PARAM_SESSION_ID));
                    obj.put(Config.PARAM_TO, SendOfferData.getString(Config.PARAM_TO));
                    obj.put(Config.PARAM_FROM, SendOfferData.getString(Config.PARAM_FROM));
                    sendMessageToService(obj.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case 2:
                try {
                    obj.put(Config.PARAM_TYPE, Config.PARAM_CANDIDATE);
                    obj.put(Config.PARAM_SUB_TYPE, "answer");
                    obj.put(Config.PARAM_SESSION_ID, RecvOfferData.getString(Config.PARAM_SESSION_ID));
                    obj.put(Config.PARAM_TO, RecvOfferData.getString(Config.PARAM_TO));
                    obj.put(Config.PARAM_FROM, RecvOfferData.getString(Config.PARAM_FROM));
                    sendMessageToService(obj.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            default:
                Candidates.add(obj);
                break;
        }
    }

    private void answerSDPSend(JSONObject jsono) {
        Logger.e("!!!", "answerSDPSend");
        try {
            jsono = jsono.getJSONObject(Config.PARAM_SDP);
            jsono.put(Config.PARAM_TYPE, "answer");
            jsono.put(Config.PARAM_SUB_TYPE, "accept");
            jsono.put(Config.PARAM_SESSION_ID, RecvOfferData.getString(Config.PARAM_SESSION_ID));
            jsono.put(Config.PARAM_FROM, RecvOfferData.getString(Config.PARAM_FROM));
            jsono.put(Config.PARAM_TO, RecvOfferData.getString(Config.PARAM_TO));
            jsono.put(Config.PARAM_MODE, RecvOfferData.getString(Config.PARAM_MODE));
            sendMessageToService(jsono.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void offerSDPSend(String mode, JSONObject jsono) {
        Logger.e("!!!", "offerSDPSend");
        try {
            jsono = jsono.getJSONObject(Config.PARAM_SDP);
            jsono.put(Config.PARAM_TYPE, "offer");
            jsono.put(Config.PARAM_SESSION_ID, Long.toString(System.currentTimeMillis()));
            jsono.put(Config.PARAM_FROM, myPhoneNumber);
            jsono.put(Config.PARAM_TO, CalledNumber);
            jsono.put(Config.PARAM_MODE, mode);
            SendOfferData = jsono;
            sendMessageToService(jsono.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected byte[] appendData(byte[] firstObject, byte[] secondObject) {
        Logger.e("!!!", "appendData");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            if (firstObject != null && firstObject.length != 0)
                outputStream.write(firstObject);
            if (secondObject != null && secondObject.length != 0)
                outputStream.write(secondObject);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputStream.toByteArray();
    }

    public boolean isApplicationAlive(Context context) {
        String strPackage = "kr.co.netseason.myclebot";
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> proceses = am.getRunningAppProcesses();

        for (ActivityManager.RunningAppProcessInfo process : proceses) {
            if (process.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                Logger.d(TAG, "process.processName = " + process.processName);
                if (strPackage.equals(process.processName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void requestMaster(String to) {
        Logger.e("!!!", "requestMaster");
        try {
            JSONObject jsono = new JSONObject();
            jsono.put(Config.PARAM_TYPE, "register_master");
            jsono.put(Config.PARAM_SESSION_ID, Long.toString(System.currentTimeMillis()));
            jsono.put(Config.PARAM_FROM, myPhoneNumber);
            jsono.put("fromName", myPhoneNumber);
            jsono.put(Config.PARAM_TO, to);
            jsono.put("toName", to);
            sendMessageToService(jsono.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean registerMaster2Databse(Messenger msg, JSONObject obj) {
        Logger.d(TAG, "registerMaster2Databse call " + obj.toString());
        String id = "";
        String masterId = "";
        try {
            id = obj.getString(Config.MASTER_REQUEST_INTENT_MY_ID);
            masterId = obj.getString(Config.MASTER_REQUEST_INTENT_MASTER_ID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ContentResolver resolver = getContentResolver();
        Cursor c = resolver.query(SecureProvider.USER_INFO_TABLE_URI, new String[]{SecureSQLiteHelper.COL_MASTER_ID},
                SecureSQLiteHelper.COL_SLAVE_ID + " = ? ", new String[]{OuiBotPreferences.getLoginId(this)}, SecureSQLiteHelper.COL_TIME + " desc");
        ArrayList<String> masterData = new ArrayList<String>();
        boolean isAlreadyMaster = false;
        if (c != null && c.moveToFirst()) {
            try {
                do {
                    String master = c.getString(c.getColumnIndex(SecureSQLiteHelper.COL_MASTER_ID));
                    if (master.equals(masterId)) {
                        isAlreadyMaster = true;
                    }
                    masterData.add(master);
                } while (c.moveToNext());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                c.close();

            }
        }
        if (masterData.size() == Config.MAX_MASTER_REGISTER_NUM && msg != null) {
            Message data = Message.obtain(null, Config.OVER_MAX_MASTER_COUNT, null);
            try {
                msg.send(data);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Logger.d(TAG, "registerMaster2Databse call max size return");
            return false;
        }

        if (isAlreadyMaster && msg != null) {
            Message data = Message.obtain(null, Config.ALREAY_MASTER, null);
            try {
                msg.send(data);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Logger.d(TAG, "registerMaster2Databse call isAleadyMaster return");
            return false;
        }
        try {
            ContentValues values = new ContentValues();
            values.put(SecureSQLiteHelper.COL_SLAVE_ID, id);
            values.put(SecureSQLiteHelper.COL_MASTER_ID, masterId);
            values.put(SecureSQLiteHelper.COL_SECURE_ON_OFF_VALUE, Config.DETECT_ONOFF_DEFAULT);
            values.put(SecureSQLiteHelper.COL_TIME, System.currentTimeMillis());
            Uri uri = resolver.insert(SecureProvider.USER_INFO_TABLE_URI, values);
            Logger.d(TAG, "insert complete uri " + uri);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private void allow2Master(JSONObject json) {
        Logger.d(TAG, "allow2Master call" + json.toString());
        try {
            JSONObject jsono = new JSONObject();
            jsono.put(Config.PARAM_TYPE, "register_master_ack");
            jsono.put(Config.PARAM_SESSION_ID, Long.toString(System.currentTimeMillis()));
            jsono.put(Config.PARAM_FROM, json.getString(Config.MASTER_REQUEST_INTENT_MASTER_ID));
            jsono.put("fromName", json.getString(Config.MASTER_REQUEST_INTENT_MASTER_ID));
            jsono.put(Config.PARAM_TO, OuiBotPreferences.getLoginId(this));
            jsono.put("toName", OuiBotPreferences.getLoginId(this));
            jsono.put(Config.PARAM_CODE, Config.PARAM_SUCCESS_CODE);
            jsono.put(Config.PARAM_DESCRIPTION, Config.PARAM_SUCCESS);
            sendMessageToService(jsono.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteMasterInDataBase(String master) {
        Logger.e("!!!", "deleteMasterInDataBase");
        ContentResolver resolver = getContentResolver();
        resolver.delete(SecureProvider.USER_INFO_TABLE_URI, SecureSQLiteHelper.COL_MASTER_ID + " =? AND " + SecureSQLiteHelper.COL_SLAVE_ID + " =? ", new String[]{master, OuiBotPreferences.getLoginId(this)});
    }

    private void deleteSlave(String slave) {
        Logger.e("!!!", "deleteSlave");
        ContentResolver resolver = getContentResolver();
        resolver.delete(SecureProvider.USER_INFO_TABLE_URI, SecureSQLiteHelper.COL_MASTER_ID + " =? AND " + SecureSQLiteHelper.COL_SLAVE_ID + " =? ", new String[]{OuiBotPreferences.getLoginId(this), slave});
    }

    private void notAllow2Master(JSONObject json) {
        Logger.e("!!!", "notAllow2Master");
        try {
            JSONObject jsono = new JSONObject();
            jsono.put(Config.PARAM_TYPE, "register_master_ack");
            jsono.put(Config.PARAM_SESSION_ID, Long.toString(System.currentTimeMillis()));
            jsono.put(Config.PARAM_FROM, json.getString(Config.MASTER_REQUEST_INTENT_MASTER_ID));
            jsono.put("fromName", json.getString(Config.MASTER_REQUEST_INTENT_MASTER_ID));
            jsono.put(Config.PARAM_TO, OuiBotPreferences.getLoginId(this));
            jsono.put("toName", OuiBotPreferences.getLoginId(this));
            jsono.put(Config.PARAM_CODE, Config.PARAM_SUCCESS_CODE);
            jsono.put(Config.PARAM_DESCRIPTION, "reject");
            sendMessageToService(jsono.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getSecureConfig(String id) {
        Logger.e("!!!", "getSecureConfig");
        try {
            JSONObject jsono = new JSONObject();
            jsono.put(Config.PARAM_TYPE, Config.PARAM_GET_CONFIG);
            jsono.put(Config.PARAM_SESSION_ID, Long.toString(System.currentTimeMillis()));
            jsono.put(Config.PARAM_FROM, OuiBotPreferences.getLoginId(this));
            jsono.put(Config.PARAM_TO, id);
            sendMessageToService(jsono.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startActivity(int flag, String callSendNumber) {
        Logger.e("!!!", "startActivity"+callSendNumber);

        if (mWebSocketClient != null && mWebSocketClient.getConnection() != null) {
            if (!mWebSocketClient.getConnection().isOpen()) {
                Toast.makeText(SignalingChannel.this, getString(R.string.connection_faile_please_retry), Toast.LENGTH_SHORT).show();
                Logger.e("!!!", "alex startactivity");
                return;
            }
        }
        CALL_MODE = flag;
        CalledNumber = callSendNumber.split("\\|")[1];
        Intent intent = null;
        switch (CALL_MODE) {
            case Config.MODE_CALL:
                intent = new Intent(getApplicationContext(), CallActivity.class);
                break;
            case Config.MODE_CCTV:
                intent = new Intent(getApplicationContext(), CctvActivity.class);
                break;
            case Config.MODE_PET:
                intent = new Intent(getApplicationContext(), PetActivity.class);
                break;
        }
        intent.putExtra("callSendFlag", true);
        Logger.e("!!!", "callSendFlag true");
        intent.putExtra("callSendNumber", callSendNumber);
        Logger.e("!!!", "callSendNumber is " + callSendNumber);
        intent.putExtra("Messenger", messenger);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
//                ringtone(true);
    }

    MediaPlayer mMediaPlayer = null;
    Vibrator vibrator = null;
    private void ringtone(boolean flag) {
        Logger.e("!!!", "====================   ringtone  ======================");
        Logger.d("service", "RINGING.....R....R.... flag = " + flag);

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        if ( !flag ) {
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.cancel();

            if (mMediaPlayer != null) {
                if (Config.Mode == Config.COMPILE_Ouibot) {
                    mMediaPlayer.setVolume(0, 0);
//                    audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
                    audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
                    audioManager.setStreamMute(AudioManager.STREAM_VOICE_CALL, true);
                } else {
                    mMediaPlayer.stop();
                    mMediaPlayer.release();
                    mMediaPlayer = null;
                }
            }
        }
        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "pause");
        sendBroadcast(i);

        if (Config.isBluetoothConnected(this)) {
            audioManager.startBluetoothSco();
            audioManager.setMode(AudioManager.MODE_IN_CALL);
            Logger.e("MYCLEBOT", "alex BT ON ");
        } else {
            set_sound();
        }

        if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {

            Logger.d("MYCLEBOT", "ringtone - AudioManager.RINGER_MODE_SILENT ");
            return;
        }

        if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
            Logger.d("MYCLEBOT", "ringtone - AudioManager.RINGER_MODE_VIBRATE ");
            long[] pattern = new long[]{1000, 1000};
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (flag) {
                vibrator.vibrate(pattern, 0);
            }

            return;
        }
        try {
            if (flag) {
                if (mMediaPlayer != null) {
                    mMediaPlayer.release();
                    mMediaPlayer = null;
                }
                mMediaPlayer = new MediaPlayer();

                if (!Config.isBluetoothConnected(this)) {
                    audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
                }
//                try {
//                    Thread.sleep(500);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                if (Config.Mode == Config.COMPILE_Ouibot) {
                    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
//                }
                mMediaPlayer.setDataSource(getApplicationContext(), Uri.parse("android.resource://"
                        + getBaseContext().getPackageName() + "/" + R.raw.ring_bell));
                mMediaPlayer.setLooping(true);
                mMediaPlayer.prepare();
                mMediaPlayer.start();
            }
        } catch (Exception e) {
        }
    }

    private void ringtone_end() {
        Logger.e("!!!", "ringtone_end");
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        if (Config.Mode == Config.COMPILE_Ouibot) {
            if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                mMediaPlayer.stop();
                if (mMediaPlayer != null) {
                    mMediaPlayer.release();
                    mMediaPlayer = null;
                }
            }
        }
    }

    private void set_sound() {

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setSpeakerphoneOn(true);
//        if (Config.Mode == Config.COMPILE_Android) {
//        audioManager.setMode(AudioManager.STREAM_MUSIC);
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);

//        } else {
//            Logger.e("!!!", "OUIBOT set_sound");
//        }
//        audioManager.setMode(AudioManager.STREAM_MUSIC);

    }
//    private void set_sound() {
//        Logger.e("!!!", "set_sound");
//        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
//// note4 mode_in_call 상태 양호
//
//
//            audioManager.setMode(AudioManager.MODE_IN_CALL);
//            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
//            audioManager.setSpeakerphoneOn(true);
//
////            if ( AcousticEchoCanceler.isAvailable() ) {
////                Logger.w("!!!", "AcousticEchoCanceler.isAvailable() is true");
////
////           audioManager.setParameters("acoustic_echocanceler=on");
////////                AcousticEchoCanceler aec = AcousticEchoCanceler.create(audioManager.generateAudioSessionId());
//////               AcousticEchoCanceler aec = AcousticEchoCanceler.create(audioManager.getMode());
//////                aec.setEnabled(true);
//////
////            } else {
////                Logger.w("!!!", "AcousticEchoCanceler.isAvailable() is false");
////            }
//////
////            if (NoiseSuppressor.isAvailable())
////            {
////                audioManager.setParameters("noise_suppression=on");
////                Logger.w("!!!", "NoiseSuppressor.isAvailable() is true");
////            }
////            if (AutomaticGainControl.isAvailable())
////            {
////                Logger.w("!!!", "AutomaticGainControl.isAvailable() is true");
////            }
//
//            Logger.e("!!!", "alex LOLLIPOP++");
//
//
//        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            // G3 : MODE_IN_CALL echo 심함 VOICE_CALL 하울링 심함 에코 캔슬러 동작안함
//
////            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
//            audioManager.setMode(AudioManager.MODE_IN_CALL);
//            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
//
//            audioManager.setSpeakerphoneOn(true);
//
////            Logger.e("!!!", "alex LOLLIPOP");
////
////            if ( AcousticEchoCanceler.isAvailable() ) {
////                Logger.w("!!!", "AcousticEchoCanceler.isAvailable() is true");
////            } else {
////                Logger.w("!!!", "AcousticEchoCanceler.isAvailable() is false");
////            }
////            if (NoiseSuppressor.isAvailable())
////            {
////                Logger.w("!!!", "NoiseSuppressor.isAvailable() is true");
////            }
////            audioManager.setSpeakerphoneOn(true);
////            if (AutomaticGainControl.isAvailable())
////            {
////                Logger.w("!!!", "AutomaticGainControl.isAvailable() is true");
//////            }
////            if (NoiseSuppressor.isAvailable())
////            {
////                audioManager.setParameters("noise_suppression=on");
////                Logger.w("!!!", "NoiseSuppressor.isAvailable() is true");
////            }
////
//
//        } else if (Config.Mode == Config.COMPILE_Ouibot) {
////            audioManager.setMode(AudioManager.MODE_NORMAL);
////            audioManager.setSpeakerphoneOn(true);
//            audioManager.setSpeakerphoneOn(true);
//            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
////            audioManager.setMode(AudioManager.MODE_NORMAL);
////            if ( AcousticEchoCanceler.isAvailable() ) {
////                Logger.w("!!!", "AcousticEchoCanceler.isAvailable() is true");
////            } else {
////                Logger.w("!!!", "AcousticEchoCanceler.isAvailable() is false");
////            }
////            if (NoiseSuppressor.isAvailable())
////            {
////                Logger.w("!!!", "NoiseSuppressor.isAvailable() is true");
////            }
////            if (AutomaticGainControl.isAvailable())
////            {
////                Logger.w("!!!", "AutomaticGainControl.isAvailable() is true");
////            }
//        } else {
//
//            audioManager.setMode(AudioManager.MODE_IN_CALL);
//            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
//
//            audioManager.setSpeakerphoneOn(true);
//
////            audioManager.setMicrophoneMute(true);
//            Logger.e("!!!", "alex LOLLIPOP--");
//
////            if ( AcousticEchoCanceler.isAvailable() ) {
////                Logger.w("!!!", "AcousticEchoCanceler.isAvailable() is true");
////                audioManager.setParameters("acoustic_echocanceler=on");
////            } else {
////                Logger.w("!!!", "AcousticEchoCanceler.isAvailable() is false");
////            }
////            if (NoiseSuppressor.isAvailable())
////            {
////                Logger.w("!!!", "NoiseSuppressor.isAvailable() is true");
////            }
////            if (AutomaticGainControl.isAvailable())
////            {
////                Logger.w("!!!", "AutomaticGainControl.isAvailable() is true");
////            }
////            if (NoiseSuppressor.isAvailable())
////            {
////                audioManager.setParameters("noise_suppression=on");
////                Logger.w("!!!", "NoiseSuppressor.isAvailable() is true");
////            }
//////
//        }
//
////            Logger.e("!!!", "alex note4 / 5");
////        }  else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//        // note 3 STREAM_VOICE_CALL 로 동작
//
//
//
////    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//        //
//
//    }




    private void startActivity(int flag, JSONObject jsono) {
        Logger.e("!!!", "startActivity");

        Logger.w("!!!!", "jsono = " + jsono);
        try {
            CalledNumber = jsono.getString("from");
        } catch (Exception e) {
        }
        Intent intent = null;
        switch (flag) {
            case Config.MODE_CALL:
//                ringtone(true);
                intent = new Intent(getApplicationContext(), CallActivity.class);
                break;
            case Config.MODE_CCTV:
                intent = new Intent(getApplicationContext(), CctvActivity.class);
                break;
            case Config.MODE_PET:
//                ringtone(true);
                intent = new Intent(getApplicationContext(), PetActivity.class);
                break;
//            case Config.MODE_MESSAGE:
//                intent = new Intent(getApplicationContext(), MessageDetailActivity.class);
//                break;
        }
        intent.putExtra("callSendFlag", false);
        Logger.e("!!!", "callSendFlag false");
        intent.putExtra("RecvOffer", jsono.toString());
        Logger.e("!!!", "RecvOffer is " + jsono.toString());
        intent.putExtra("Messenger", messenger);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Logger.e("!!!", "onBind");

        networkReceiverSet();

        return messenger.getBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.e("!!!", "onStartCommand");

        unregisterRestartAlarm();
        networkReceiverSet();


//        return super.onStartCommand(intent, flags, startId);
        return START_REDELIVER_INTENT;
    }

    @Override
    protected void finalize() throws Throwable {
        Logger.e("!!!!!", "Signaling channel finalize");
        super.finalize();
    }

    private NotificationManager nm;
    @Override
    public void onCreate() {
        super.onCreate();
        Logger.e("!!!", "Signaling channel onCreate");
        messenger = new Messenger(new IncomingHandler());
        isForceStartSecureMode = false;
        SecurePreference.PREF = PreferenceManager.getDefaultSharedPreferences(this);
        if ((Build.BRAND.equals("Samsung") || Build.BRAND.equals("samsung") || Config.Mode == Config.COMPILE_Ouibot)) {
            Logger.e("!!!", "alex Brand equal Samsung");
            startForeground(1, new Notification());
        }
        networkReceiverSet();

//        startForeground(1, new Notification());
//        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        Notification notification = new Notification(0, "hello", System.currentTimeMillis());
//        notification.setLatestEventInfo(getApplicationContext(), "title", "text", null);
//        nm.notify(1, notification);
//        try {
//            Thread.sleep(100);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        nm.cancel(1);
    }

    public boolean checkDatabaseMasterIDExist(String slaveID) {
        Logger.e("!!!", "checkDatabaseMasterIDExist");
        if (slaveID == null) {
            return true;
        }
        boolean checkSlaveExist = false;
        ArrayList<UibotListData> data = getSlaveList(OuiBotPreferences.getLoginId(this));

        try {
            for (int i = 0; i < data.size(); i++) {
                if (slaveID.equals(data.get(i).getSlaveRtcid())) {
                    checkSlaveExist = true;
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return checkSlaveExist;
    }

    public void setAllMessageFailIfSeding(String peerRtcid) {
        Logger.d(TAG,"setAllMessageFailIfSeding call "+peerRtcid);
        if(peerRtcid != null) {
            ContentValues values = new ContentValues();
            values.put(SecureSQLiteHelper.COL_SEND_STATE, MessageListData.SEND_FAIL);
            ContentResolver resolver = getContentResolver();
            resolver.update(SecureProvider.MESSAGE_TABLE_URI, values, SecureSQLiteHelper.COL_RTCID + " =? AND " + SecureSQLiteHelper.COL_SEND_STATE + " =? ", new String[]{peerRtcid, String.valueOf(MessageListData.SENDING)});
        }
    }

    public void sendMessageForMasterExistInDatabase(ArrayList<UibotListData> materIDArray) {
        Logger.e("!!!", "sendMessageForMasterExistInDatabase");
        if (materIDArray == null) {
            return;
        }
        for (int i = 0; i < materIDArray.size(); i++) {
            String materID = materIDArray.get(i).getMasterRtcid();
            try {
                JSONObject jobj = new JSONObject();
                JSONObject kobj = new JSONObject();
                jobj.put(Config.PARAM_TYPE, Config.PARAM_GET_CONFIG_ACK);
                jobj.put(Config.PARAM_SESSION_ID, System.currentTimeMillis());
                jobj.put(Config.PARAM_FROM, materID);
                jobj.put(Config.PARAM_TO, OuiBotPreferences.getLoginId(this));
                jobj.put(Config.PARAM_CODE, Config.PARAM_SUCCESS_CODE);
                jobj.put(Config.PARAM_DESCRIPTION, "check_master_exist");
                jobj.put(Config.PARAM_CONFIG, kobj);
                sendMessageToService(jobj.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void networkReceiverSet() {
        Logger.e("!!!", "networkReceiverSet");
        if (mNetworkReceiver == null) {
            IntentFilter subFilter = new IntentFilter();
            subFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            subFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
            subFilter.addAction("ACTION.SEND.CHECK");
            mNetworkReceiver = new NetworkReceiver();
            Logger.e("!!!", "registerReceiver(mNetworkReceiver, subFilter) ");
            cur_net_stat = 10;
            registerReceiver(mNetworkReceiver, subFilter);
        }


        Logger.w("!!!", "noti reset");
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification = new Notification(0, "hello", System.currentTimeMillis());
        notification.setLatestEventInfo(getApplicationContext(), "title", "text", null);
        nm.notify(1, notification);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(300);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                nm.cancel(1);
                try {
                    Thread.sleep(300);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                nm.cancel(1);
            }
        }).start();

    }

    private void insertDatabaseMessageSuccesReceive(String id, String time) {
        Logger.e("!!!", "insertDatabaseMessageSuccesReceive");
        ContentValues values = new ContentValues();
        values.put(SecureSQLiteHelper.COL_SEND_STATE, MessageListData.SEND_COMPLETE);
        ContentResolver resolver = getContentResolver();
        resolver.update(SecureProvider.MESSAGE_TABLE_URI, values, SecureSQLiteHelper.COL_PEER_RTCID + " =? AND " + SecureSQLiteHelper.COL_TIME + " =? ", new String[]{id, time});
    }

    void TURN_ON_BLUE() // log-on
    {
        Logger.e("!!!", "TURN_ON_BLUE");
        if (Config.Mode == Config.COMPILE_Ouibot) {
            try {
//                Thread.sleep(30);
                Logger.e("!!!", "TURN_ON_BLUE..");
                Runtime runtime = Runtime.getRuntime();
                runtime.exec(led_blue);
            } catch (Exception e) {
            }
        }
    }

    void TURN_ON_RED() // log-off
    {
        Logger.e("!!!", "TURN_ON_RED");
        if (Config.Mode == Config.COMPILE_Ouibot) {
            try {
                Logger.e("!!!", "TURN_ON_RED..");
                Runtime runtime = Runtime.getRuntime();
                runtime.exec(led_red);
            } catch (IOException e) {
            }
            cur_net_stat = 10;
        }
    }

    private void notifyDataChangeForViewRefreshExceptNoti(JSONObject json) {
        Logger.e("!!!", "notifyDataChangeForViewRefreshExceptNoti");
        Intent secureConfig = new Intent(Config.INTENT_ACTION_SET_SECURE_CONFIG_DATA_ACK);
        secureConfig.putExtra(Config.INTENT_ACTION_SET_SECURE_CONFIG_DATA_ACK_KEY, json.toString());
        sendBroadcast(secureConfig);
    }


    private void notifyDataChangeForViewRefresh(JSONObject json) {
        Logger.e("!!!", "notifyDataChangeForViewRefresh");
        Intent secureConfig = new Intent(Config.INTENT_ACTION_SET_SECURE_CONFIG_DATA_ACK);
        secureConfig.putExtra(Config.INTENT_ACTION_SET_SECURE_CONFIG_DATA_ACK_KEY, json.toString());
        sendBroadcast(secureConfig);
        Logger.d(TAG, "json == " + json);

        try {
            JSONObject childJson = json.getJSONObject(Config.PARAM_CONFIG);
            if (childJson.getString(Config.PARAM_DETECT_ONOFF).equals(Config.DETECT_OFF)) {
                if (childJson.getInt(Config.PARAM_DETECT_MODE) == Config.DETECT_MOVEMENT_MODE) {
                    requestNotificationOnly(getResources().getString(R.string.notification_none_activity_finish));
                } else {
                    requestNotificationNotthing(getResources().getString(R.string.notification_detected_finish));
                }
            } else {
                if (childJson.getInt(Config.PARAM_DETECT_MODE) == Config.DETECT_SECURE_MODE) {
                    requestNotificationOnly(getResources().getString(R.string.notification_detected_start));
                } else {
                    requestNotificationOnly(getResources().getString(R.string.notification_none_activity_start));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean logoutFlag = false;
    private boolean reconnectFlag = false;

    private int connectErrorCount = 0;
    private PowerManager.WakeLock wl = null;
    private void connectingWebSocket() {
        Logger.e("!!!", "connectingWebSocket");
        Logger.e("!!!", "reconnectFlag = " + reconnectFlag);
        if ( reconnectFlag ) {
            reconnectFlag = false;
            return;
        } else {
            reconnectFlag = true;
            if (OuiBotPreferences.getLoginId(SignalingChannel.this) == null) {
                if (Config.Mode == Config.COMPILE_Ouibot) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(getApplicationContext(), DeviceAddInitActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    }, 2000);

                }
                reconnectFlag = false;
                return;
            }
            URI uri;
            try {
                uri = new URI(Config.OUIBOT_SERVER_ADDRESS);
            } catch (URISyntaxException e) {
                e.printStackTrace();
                return;
            }

            HashMap<String, String> httpHeaders = new HashMap<String, String>();
            httpHeaders.put("Sec-WebSocket-Protocol", "ns-rtc");

            Logger.e("!!!", "mWebSocketClient = " + mWebSocketClient);

            if (mWebSocketClient != null) {
                Logger.e("!!!", "mWebSocketClient is close = "+mWebSocketClient.getReadyState().name());
                connectErrorCount = 0;
                mWebSocketClient.close();

                Logger.e("!!!", "alex websocket not null");
                mWebSocketClient = null;
                System.gc();
            } else {
                Logger.e("!!!", "alex websocket is null");
            }

            mWebSocketClient = new WebSocketClient(uri, new Draft_17(), httpHeaders, 0) {
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    Logger.e("!!!", "mWebSocketClient onOpen");
                    sendMessage_Login();

                    String str = "rtcid=" + OuiBotPreferences.getLoginId(getApplicationContext());
                    str += "&uuid=" + Installation.id(getApplicationContext());
                    str += "&type=" + Config.Mode;
                    str += "&os_version=" + Config.getAndroidVersion();
                    str += "&app_version=" + Config.getAppVersionName(getApplicationContext());
                    str += "&device_name=" + Config.getDeviceName();
                    str += "&phone_no=" + Config.getPhoneNumber(getApplicationContext());
                    new httpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "login_db_do_post.php", str);
                }

                @Override
                public void onMessage(ByteBuffer bytes) {
                    Logger.e("!!!", "bytes ="+bytes.toString());
                    super.onMessage(bytes);
                }

                @Override
                public void onMessage(String s) {
                    Logger.w("!!!", "onMessage s = " + s);
                    connectErrorCount = 0;
                    try {
                        final JSONObject jsono = new JSONObject(s);
                        switch (jsono.get(Config.PARAM_TYPE).toString()) {
                            case "login_ack":  //  login ack
                                if (jsono.get(Config.PARAM_CODE).equals(Config.PARAM_SUCCESS_CODE)) {    //  login OK
                                    logoutFlag = false;
                                    if (Config.Mode == Config.COMPILE_Ouibot) {
                                        TURN_ON_BLUE();
                                    }

                                    send_check_ack();
                                } else if (jsono.get(Config.PARAM_CODE).equals(Config.PARAM_WARRING_CODE)) {
                                    if ( OuiBotPreferences.getLoginId(getApplicationContext()) != null ) {
                                        logoutFlag = true;
                                        Intent notificationIntent = new Intent(getApplicationContext(), IntroActivity.class);
                                        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                                        Notification.Builder builder = new Notification.Builder(getApplicationContext());
                                        builder.setSmallIcon(R.mipmap.ic_launcher);
                                        builder.setWhen(System.currentTimeMillis());
                                        builder.setContentTitle(getResources().getString(R.string.ws_connect_close_title));
                                        builder.setContentText(getResources().getString(R.string.ws_connect_close_message));
                                        builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS);
                                        builder.setContentIntent(pendingIntent);
                                        builder.setAutoCancel(true);
                                        builder.setPriority(Notification.PRIORITY_MAX);
                                        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                        nm.notify(234567, builder.build());

                                        GO_LOGINPAGE();

                                    }
                                } else {    //  login fail
                                    GO_LOGINPAGE();
                                }
                                break;
                            case "logout_ack":  //  logout ack
                                if (jsono.get(Config.PARAM_CODE).equals(Config.PARAM_SUCCESS_CODE)) {    //  logout OK
                                    Logger.e("!!!", "LogOUT OK");
                                    checkackHandler.removeCallbacks(checkackRunnable);

                                    GO_LOGINPAGE();
                                } else {    //  logout fail
                                }
                                break;

                            case "offer":  //  offer ack
                                if (Config.Mode == Config.COMPILE_Android) {
                                    KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
                                    KeyguardManager.KeyguardLock kl = km.newKeyguardLock("TAG");
                                    kl.disableKeyguard();
                                    wl = ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, TAG);
                                    wl.acquire();

//                                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
                                }

                                Logger.e("!!!", "alex wakelock");
                                HangupData = null;

                                if (spemNumberCatch(jsono)) {
                                    sendMessage_offer_ack(jsono, 1);
                                } else {
                                    if (Config.Mode == Config.COMPILE_Android) {
                                        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                                        if (tm.getCallState() != TelephonyManager.CALL_STATE_IDLE) {
                                            sendMessage_offer_ack(jsono, 2);
                                            return;
                                        }
                                    }
                                    switch (jsono.getString(Config.PARAM_MODE)) {
                                        case "call":
                                            if (checkConnectedPhoneCall()) {
                                                sendMessage_offer_ack(jsono, 2);
                                            } else {
                                                sendMessage_offer_ack(jsono);
                                                startActivity(Config.MODE_CALL, jsono);
                                            }
                                            break;
                                        case "cctv":
                                            if (checkConnectedPhoneCall()) {
                                                sendMessage_offer_ack(jsono, 2);
                                            } else {
                                                sendMessage_offer_ack(jsono);
                                                startActivity(Config.MODE_CCTV, jsono);
                                            }
                                            break;
                                        case "pet":
                                            if (checkConnectedPhoneCall()) {
                                                sendMessage_offer_ack(jsono, 2);
                                            } else {
                                                sendMessage_offer_ack(jsono);
                                                startActivity(Config.MODE_PET, jsono);
                                            }
                                            break;
                                        case "message":
                                            startMessageAsyncReceiveTask(jsono);
                                            break;
//                                    case "secure_image":
//                                        startSecureImageAsyncReceiveTask(jsono);
//                                        break;
                                        case "file":
                                            startFileImageAsyncReceiveTask(jsono);
                                            break;
                                    }
                                }
                                break;
                            case "offer_ack": {
                                if (!jsono.has(Config.PARAM_MODE)) {
                                    switch (jsono.getString(Config.PARAM_CODE)) {
                                        case Config.PARAM_PEER_IS_NOT_LOGIN_CODE: {
                                            Logger.e(TAG, "C PARAM_CODE 111 succes" + jsono);
                                            if (callMessenger != null) {
                                                try {
                                                    Message msg_hangup = Message.obtain(null, Config.CALL_OFFER_SDP_ACK_NOT_CONNECT, jsono);
                                                    callMessenger.send(msg_hangup);
                                                } catch (RemoteException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            SenderDataChannel senderDataChannel = Config.THREAD_SENDER_CLASS.get(jsono.get(Config.PARAM_SESSION_ID));
                                            if (senderDataChannel != null) {
                                                Logger.w("!!!!", "jsono.getString(Config.PARAM_SESSION_ID) = " + jsono.getString(Config.PARAM_SESSION_ID));
                                                senderDataChannel.stopSession(jsono.getString(Config.PARAM_SESSION_ID));
                                                Intent intent = new Intent(Config.INTENT_ACTION_MESSAGE_TOAST);
                                                intent.putExtra(Config.INTENT_ACTION_MESSAGE_TOAST_MESSAGE, jsono.getString(Config.PARAM_TO) + " " + getResources().getString(R.string.peer_is_not_login_message));
                                                sendBroadcast(intent);
                                                sendBroadcast(new Intent(Config.INTENT_ACTION_THREAD_STOP));
                                            }
                                            break;
                                        }
                                    }
                                    break;
                                }
                                switch (jsono.getString(Config.PARAM_MODE)) {
                                    case "call":
                                    case "pet":
                                    case "cctv":
                                        switch (jsono.getString(Config.PARAM_CODE)) {
                                            case "100": {
                                                Logger.e(TAG, "offer_ack PARAM_CODE 100 return" + jsono);
                                                break;
                                            }
                                            case Config.PARAM_PEER_IS_CALLING_CODE: {
                                                try {
                                                    Message msg_hangup = Message.obtain(null, Config.CALL_OFFER_SDP_ACK_CALLING, jsono);
                                                    callMessenger.send(msg_hangup);
                                                } catch (RemoteException e) {
                                                    e.printStackTrace();
                                                }
                                                break;
                                            }
                                        }
                                        break;
                                    case "file":
                                        switch (jsono.getString(Config.PARAM_CODE)) {
                                            case Config.PARAM_ALREADY_DATA_CHANNEL_OPEN: {
                                                SenderDataChannel senderDataChannel = Config.THREAD_SENDER_CLASS.get(jsono.get(Config.PARAM_SESSION_ID));
                                                if(senderDataChannel != null) {
                                                    senderDataChannel.stopSession(jsono.getString(Config.PARAM_SESSION_ID));
                                                    Intent intent = new Intent(Config.INTENT_ACTION_MESSAGE_TOAST);
                                                    intent.putExtra(Config.INTENT_ACTION_MESSAGE_TOAST_MESSAGE, getResources().getString(R.string.already_message_receiving));
                                                    sendBroadcast(intent);
                                                    sendBroadcast(new Intent(Config.INTENT_ACTION_THREAD_STOP));
                                                }
                                                break;
                                            }
                                        }
                                        break;
                                }
                                break;
                            }
                            case "answer":  //  answer ack
                                switch (jsono.getString(Config.PARAM_MODE)) {
                                    case "call":
                                    case "pet":
                                    case "cctv":
                                        ringtone(false);
                                        sendMessage_answer_ack(jsono);
                                        switch (jsono.getString(Config.PARAM_SUB_TYPE)) {
                                            case "accept":
                                                sendMessage_candidate(true);
                                                break;
                                            case "reject":
                                                try {
                                                    Message msg_hangup = Message.obtain(null, Config.REJECT_RECV, jsono);
                                                    callMessenger.send(msg_hangup);
                                                } catch (RemoteException e) {
                                                    e.printStackTrace();
                                                }
                                                break;
                                        }
                                        break;
                                    case "file":
                                        sendMessage_data_channel_secure_answer_ack(jsono);
                                        switch (jsono.getString(Config.PARAM_SUB_TYPE)) {
                                            case "accept":
                                                try {
                                                    Thread.sleep(500);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                try {
                                                    Config.THREAD_SENDER_CLASS.get(jsono.getString(Config.PARAM_SESSION_ID)).sendCandidate();
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                                break;
                                            case "reject":
                                                break;
                                        }
                                        break;
                                }
                                break;

                            case Config.PARAM_CANDIDATE: {  //  answer ack offer 랑 sender 둘다 수신한다.
                                if (jsono.has(Config.PARAM_CANDIDATE_DATA_CHANNEL)) {
                                    switch (jsono.getString(Config.PARAM_SUB_TYPE)) {
                                        case "answer": {
                                            Logger.d(TAG, "Config.THREAD_SENDER_CLASS.size = " + Config.THREAD_SENDER_CLASS.size());
                                            Config.THREAD_SENDER_CLASS.get(jsono.getString(Config.PARAM_SESSION_ID)).callRecvCandidate(jsono);
                                            break;
                                        }
                                        case "offer": {
                                            Logger.d(TAG, "Config.THREAD_SENDER_CLASS.size = " + Config.THREAD_SENDER_CLASS.size());
                                            Config.THREAD_RECIEVER_CLASS.get(jsono.getString(Config.PARAM_SESSION_ID)).callRecvCandidate(jsono);
                                            break;
                                        }
                                    }
                                } else {
                                    if (callMessenger != null) {
                                        Logger.d(TAG, "callMessenger = " + callMessenger + " msg_candidate = " + jsono);
                                        Message msg_candidate = Message.obtain(null, Config.RECV_CANDIDATE, jsono);
                                        try {
                                            callMessenger.send(msg_candidate);
                                        } catch (RemoteException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                                break;
                            }
                            case "answer_ack":  //  answer ack
                                if ( jsono.has(Config.PARAM_MODE) ) {
                                    switch (jsono.getString(Config.PARAM_MODE)) {
                                        case "call":
                                        case "pet":
                                            ringtone(false);
                                        case "cctv":
                                            switch (jsono.getString(Config.PARAM_SUB_TYPE)) {
                                                case "accept":
                                                    sendMessage_candidate(false);
                                                    break;
                                                case "reject":
                                                    break;
                                            }
                                            break;
                                        case "file":
                                            try {
                                                Config.THREAD_RECIEVER_CLASS.get(jsono.getString(Config.PARAM_SESSION_ID)).sendCandidate();
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            break;
                                    }
                                } else {
                                    Logger.w("!!!", "jsono = "+jsono);
                                    try {
                                        if (callMessenger != null) {
                                            Logger.w("!!!", "send HANGUP_RECV");
                                            Message msg_hangup = Message.obtain(null, Config.HANGUP_RECV, jsono);
                                            callMessenger.send(msg_hangup);
                                            CalledNumber = "";
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                break;

                            case "hangup":    // receive hangup
                                Logger.w("!!!", "hangup jsono = " + jsono);
                                if (wl != null && wl.isHeld()) {
                                    Logger.w("!!!", "wl is held() and release");
                                    wl.release();
                                } else {
                                    Logger.w("!!!", "wl is null or not held wl = "+wl);
                                }

                                ringtone(false);
                                try {
                                    Logger.w("!!!", "hangup callMessenger = " + callMessenger);
                                    if (callMessenger != null) {
                                        Message msg_hangup = Message.obtain(null, Config.HANGUP_RECV, jsono);
                                        callMessenger.send(msg_hangup);
                                        CalledNumber = "";
                                        sendMessage_hangup_ack(jsono);
                                    } else {
                                        callendFlag = true;
                                        HangupData = jsono;
                                    }
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                break;
                            case "hangup_ack":    // receive hangup
                                if (wl != null && wl.isHeld()) {
                                    Logger.w("!!!", "wl is held() and release");
                                    wl.release();
                                } else {
                                    Logger.w("!!!", "wl is null or not held wl = " + wl);
                                }

                                try {
                                    Message msg_hangup = Message.obtain(null, Config.HANGUP_ACK_RECV, jsono);
                                    CalledNumber = "";
                                    if (callMessenger != null) {
                                        callMessenger.send(msg_hangup);
                                    }

                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                                break;

                            case "register_master_ack":
                                switch (jsono.getString(Config.PARAM_DESCRIPTION)) {
                                    case Config.PARAM_SUCCESS: {
                                        setMasterResult(jsono);
                                        break;
                                    }
                                    case "reject": {
                                        Intent intent = new Intent(Config.INTENT_ACTION_MESSAGE_TOAST);
                                        intent.putExtra(Config.INTENT_ACTION_MESSAGE_TOAST_MESSAGE, getResources().getString(R.string.reject_request_master));
                                        sendBroadcast(intent);
                                        break;
                                    }
                                    case Config.PARAM_PEER_IS_NOT_LOGIN: {
                                        Intent intent = new Intent(Config.INTENT_ACTION_MESSAGE_TOAST);
                                        intent.putExtra(Config.INTENT_ACTION_MESSAGE_TOAST_MESSAGE, getResources().getString(R.string.peer_is_not_login_request_master));
                                        sendBroadcast(intent);
                                        break;
                                    }
                                }
                                break;
                            case Config.PARAM_GET_CONFIG:
                                if (checkMasterAvailable(jsono)) {
                                    sendMessageGetCofigAck(jsono.toString());
                                } else {
                                    sendSlaveDeleteWithCofigAck(jsono.getString(Config.PARAM_FROM), jsono.getString(Config.PARAM_SESSION_ID));
                                }
                                break;
                            case Config.PARAM_GET_CONFIG_ACK:
                                switch (jsono.getString(Config.PARAM_DESCRIPTION)) {
                                    case "WEBSEND": {
                                        try {
                                            Message msg_hangup = Message.obtain(null, Config.WEBSEND, jsono);
                                            callMessenger.send(msg_hangup);
                                        } catch (RemoteException e) {
                                            e.printStackTrace();
                                        }
                                        break;
                                    }
                                    case "get_total_message_data":{
                                        sendTotalMessageData(jsono);
                                        break;
                                    }
                                    case "get_total_secure_image_data":{
                                        sendTotalSecureImageData(jsono);
                                        break;
                                    }
                                    case "ios_device_is_login":{
                                        JSONObject childJson = jsono.getJSONObject(Config.PARAM_CONFIG);
                                        String id = jsono.getString(Config.PARAM_TO);
                                        String notification_mode = childJson.getString("notification_mode");
                                        String notification_data = childJson.getString("notification_data");
                                        Logger.d(TAG,"id = "+id);
                                        Logger.d(TAG,"notification_mode = "+notification_mode);
                                        Logger.d(TAG,"notification_data = "+notification_data);
                                        if(notification_mode.equals(Config.CALL_TYPE)){
                                            if(callMessenger == null){
                                                Logger.d(TAG,"이미 전화를 종료한 경우  = ");
                                                return;
                                            }
                                            try {
                                                Message msg_hangup = Message.obtain(null, Config.CALL_START_IOS_AWAKE, jsono);
                                                callMessenger.send(msg_hangup);
                                            } catch (RemoteException e) {
                                                e.printStackTrace();
                                            }
                                        }else if(notification_mode.equals(Config.SECURE_TYPE)){
                                            String path = getSecureFile(notification_data);
                                            String mode = getSecureMode(notification_data);
                                            sendMessageToService(makeSecureImageJsonDataForIOS(id,new File(path),path,mode,notification_data,"popup"));
                                        }else if(notification_mode.equals(Config.MESSAGE_TYPE)){
                                            String message = getMessageFromDatabase(id, notification_data);
                                            String message_send_flag = getMessageSendFlag(id, notification_data);
                                            String message_type = getMessageType(id, notification_data);
                                            JSONObject j = new JSONObject();
                                            j.put(Config.PARAM_TYPE, Config.PARAM_GET_CONFIG_ACK);
                                            j.put(Config.PARAM_SESSION_ID, Long.toString(System.currentTimeMillis()));
                                            j.put(Config.PARAM_FROM, id);
                                            j.put(Config.PARAM_TO, OuiBotPreferences.getLoginId(SignalingChannel.this));
                                            j.put(Config.PARAM_DESCRIPTION, "message");
                                            JSONObject json = new JSONObject();
                                            json.put("id", OuiBotPreferences.getLoginId(SignalingChannel.this));
                                            json.put("time", notification_data);
                                            json.put("message", message);
                                            json.put("message_send_flag",message_send_flag);
                                            json.put("message_type", message_type);
                                            j.put(Config.PARAM_CONFIG, json);
                                            sendMessageToService(j.toString());
                                        }else if(notification_mode.equals(Config.FILE_MESSAGE_TYPE)){
                                            String path = getMessageFromDatabase(id, notification_data);
                                            File file = new File(path);
                                            if(file.exists()) {
                                                sendMessageToService(makeFileMessageImageJsonDataForIOS(id,new File(path),path,notification_data,"file_message"));
//                                                int threadTime = getThreadTime(file);
//                                                JSONObject json = new JSONObject();
//                                                try {
//                                                    json.put(Config.COL_FILE_SLICE_COUNT, Config.getFileSliceNum(path));
//                                                    json.put(Config.PARAM_TO, id);
//                                                    json.put(Config.PARAM_FROM, OuiBotPreferences.getLoginId(SignalingChannel.this));
//                                                    json.put(SecureSQLiteHelper.COL_FILE_PATH, Config.getFileName(path));
//                                                    json.put(SecureSQLiteHelper.COL_MODE, Config.MESSAGE_FILE_MODE);
//                                                    json.put(SecureSQLiteHelper.COL_TIME, notification_data);
//                                                    json.put(Config.PARAM_THREAD_TIME, threadTime);
//                                                } catch (JSONException e) {
//                                                    e.printStackTrace();
//                                                }
//                                                if (MessageDetailActivity.mDataSessionTask != null) {
//                                                    if (MessageDetailActivity.mDataSessionTask.getStatus() == DataSessionSenderAsyncTask.Status.FINISHED) {
//                                                        MessageDetailActivity.mDataSessionTask = new DataSessionSenderAsyncTask(SignalingChannel.this, Long.valueOf(notification_data), json, file, id, messenger, threadTime, Config.MESSAGE_FILE_CHANNEL_5);
//                                                        MessageDetailActivity.mDataSessionTask.executeOnExecutor(MessageDetailActivity.mExec);
//                                                    }
//                                                } else {
//                                                    MessageDetailActivity.mDataSessionTask = new DataSessionSenderAsyncTask(SignalingChannel.this, Long.valueOf(notification_data), json, file, id, messenger, threadTime, Config.MESSAGE_FILE_CHANNEL_5);
//                                                    MessageDetailActivity.mDataSessionTask.executeOnExecutor(MessageDetailActivity.mExec);
//                                                }
                                            }
                                        }else if(notification_mode.equals(Config.CALL_END_TYPE)){

                                        }
                                        break;
                                    }
                                    case "get_single_secure_image_data":{
                                        JSONObject childJson = jsono.getJSONObject(Config.PARAM_CONFIG);
                                        String id = jsono.getString(Config.PARAM_TO);
                                        String notification_data = childJson.getString("notification_data");
                                        String path = getSecureFile(notification_data);
                                        String mode = getSecureMode(notification_data);
                                        sendMessageToService(makeSecureImageJsonDataForIOS(id, new File(path), path, mode, notification_data,"list"));
                                        break;
                                    }

                                    case "get_single_message_data":{
                                        JSONObject childJson = jsono.getJSONObject(Config.PARAM_CONFIG);
                                        String id = jsono.getString(Config.PARAM_TO);
                                        String notification_data = childJson.getString("notification_data");
                                        String message = getMessageFromDatabase(id, notification_data);
                                        String message_send_flag = getMessageSendFlag(id, notification_data);
                                        String message_type = getMessageType(id, notification_data);
                                        JSONObject j = new JSONObject();
                                        j.put(Config.PARAM_TYPE, Config.PARAM_GET_CONFIG_ACK);
                                        j.put(Config.PARAM_SESSION_ID, Long.toString(System.currentTimeMillis()));
                                        j.put(Config.PARAM_FROM, id);
                                        j.put(Config.PARAM_TO, OuiBotPreferences.getLoginId(SignalingChannel.this));
                                        j.put(Config.PARAM_DESCRIPTION, "message");
                                        JSONObject json = new JSONObject();
                                        json.put("id", OuiBotPreferences.getLoginId(SignalingChannel.this));
                                        json.put("time", notification_data);
                                        json.put("message", message);
                                        json.put("message_send_flag", message_send_flag);
                                        json.put("message_type", message_type);
                                        j.put(Config.PARAM_CONFIG, json);
                                        sendMessageToService(j.toString());
                                        break;
                                    }
                                    case "check_master_exist": {
                                        boolean existSlaveID = checkDatabaseMasterIDExist(jsono.getString(Config.PARAM_TO));
                                        if (!existSlaveID) {
                                            sendMasterDeleteWithCofigAck(jsono.getString(Config.PARAM_TO));
                                        }
                                        break;
                                    }
                                    case "fail_secure_start_because_momory": {
                                        Intent intent = new Intent(getApplicationContext(), SecureStartedFailPopupActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        sendBroadcast(new Intent(Config.INTENT_ACTION_REFESH_SECURE_PAGER_DATA));
                                        break;
                                    }
                                    case "master_request_fail": {
                                        Intent intent = new Intent(Config.INTENT_ACTION_MESSAGE_TOAST);
                                        intent.putExtra(Config.INTENT_ACTION_MESSAGE_TOAST_MESSAGE, getResources().getString(R.string.you_can_not_request_master_because_mode_in_secure));
                                        sendBroadcast(intent);
                                        break;
                                    }
                                    case "message_success": {
                                        String id = jsono.getString(Config.PARAM_TO);
                                        String time = jsono.getString(Config.PARAM_SESSION_ID);
                                        insertDatabaseMessageSuccesReceive(id, time);
                                        sendBroadcast(new Intent(Config.INTENT_RECEIVE_MESSAGE_EVENT));
                                        break;
                                    }

                                    case "file_success": {
                                        String id = jsono.getString(Config.PARAM_TO);
                                        String time = jsono.getString(Config.PARAM_SESSION_ID);
                                        insertDatabaseMessageSuccesReceive(id, time);
                                        sendBroadcast(new Intent(Config.INTENT_ACTION_THREAD_STOP));
                                        break;
                                    }
                                    case "view_refresh": {
                                        notifyDataChangeForViewRefreshExceptNoti(jsono);
                                        break;
                                    }
                                    case "delete_master": {
                                        deleteMasterInDataBase(jsono.getString(Config.PARAM_TO));
                                        sendBroadcast(new Intent(Config.INTENT_ACTION_REFESH_SECURE_PAGER_DATA));
                                        break;
                                    }
                                    case "delete_slave": {
                                        deleteSlave(jsono.getString(Config.PARAM_TO));
                                        sendBroadcast(new Intent(Config.INTENT_ACTION_REFESH_SECURE_PAGER_DATA));
                                        break;
                                    }
                                    case "CAM_MOVE_LEFT": {
                                        CAM_MOVE_LEFT();
                                        break;
                                    }
                                    case "CAM_MOVE_RIGHT": {
                                        CAM_MOVE_RIGHT();
                                        break;
                                    }
                                    case "CAM_MOVE_UP": {
                                        CAM_MOVE_UP();
                                        break;
                                    }
                                    case "CAM_MOVE_DOWN": {
                                        CAM_MOVE_DOWN();
                                        break;
                                    }
                                    case "SEND_SOUND_FALSE": {
                                        SEND_SOUND_FALSE();
                                        break;
                                    }
                                    case "SEND_SOUND_TRUE": {
                                        SEND_SOUND_TRUE();
                                        break;
                                    }
                                    case "CERTIFICATION_SEND": {
                                        if (SecurePreference.getDetectOnOff().equals(Config.DETECT_ON)) {
                                            sendMessageGetCofigAckCameraViewRequestFail(jsono);
                                            return;
                                        }
                                        CERTIFICATION_SEND(jsono);
                                        break;
                                    }
                                    case "CERTIFICATION_ANSWER": {
                                        CERTIFICATION_ANSWER();
                                        break;
                                    }
                                    case "message": {
                                        if (spemNumberCatch(jsono)) {
                                            break;
                                        }
                                        JSONObject jsonChild = jsono.getJSONObject(Config.PARAM_CONFIG);
                                        String peerId = jsonChild.getString("id");
                                        String time = jsonChild.getString("time");
                                        String message = jsonChild.getString("message");
                                        inserMessage2database(makeObjectWithMessage(Long.parseLong(time),peerId, message, MessageListData.MESSAGE_TYPE, MessageListData.SEND_COMPLETE));
                                        requestNotificationOnlyChat(peerId, message);
                                        sendBroadcast(new Intent(Config.INTENT_RECEIVE_MESSAGE_EVENT));
                                        sendMessageSuccessReceive(peerId, time);
                                        break;
                                    }
                                    case Config.PARAM_PEER_IS_NOT_LOGIN: {
                                        ContentValues values = new ContentValues();
                                        values.put(SecureSQLiteHelper.COL_SECURE_ON_OFF_VALUE, Config.DETECT_OFF);
                                        ContentResolver resolver = getContentResolver();
                                        resolver.update(SecureProvider.USER_INFO_TABLE_URI, values, SecureSQLiteHelper.COL_SLAVE_ID + " =? ", new String[]{jsono.getString(Config.PARAM_TO)});
                                        break;
                                    }
                                    case "ALRAM": {
                                        Logger.w("!!!", "jsono = "+jsono);
                                        if ( Config.Mode == Config.COMPILE_Ouibot ) {
                                            //  sjkim   -   start
                                            Intent intent = new Intent("kr.co.mates.ALRAM");
                                            intent.putExtra("ALRAM", 1);
                                            sendBroadcast(intent);
                                            Logger.d(TAG, "Broad" + intent);
                                            //  sjkim   -   end
                                        }

                                        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
                                        notificationIntent.putExtra("alram", 1);
                                        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                                        Notification.Builder builder = new Notification.Builder(getApplicationContext());
                                        builder.setSmallIcon(R.mipmap.ic_launcher);
                                        builder.setWhen(System.currentTimeMillis());
                                        builder.setContentTitle(getString(R.string.notice));
                                        builder.setContentText(getString(R.string.notice_add));
                                        builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS);
                                        if ( callMessenger == null ) {
                                            builder.setContentIntent(pendingIntent);
                                        }
//                                            builder.setAutoCancel(true);
                                        builder.setPriority(Notification.PRIORITY_MAX);
                                        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                        nm.notify(888888, builder.build());
                                        break;
                                    }
                                }
                                break;
                            case "check_ack":
                                if (jsono.getString(Config.PARAM_CODE).equals(Config.PARAM_SUCCESS_CODE)) {
//                                    Logger.e("!!!", "alex check_ack");
//                                    send_check_ack();

                                }
                                break;
                            case "register_master":
                                if (SecurePreference.getDetectOnOff().equals(Config.DETECT_ON)) {
                                    sendMessageGetCofigAckMasterRequestFail(jsono);
                                    return;
                                }
                                setMasterPopup(jsono);
                                break;
                            case "event":
                                if (jsono.isNull(Config.PARAM_DESCRIPTION) || !jsono.getString(Config.PARAM_DESCRIPTION).equals("file_message")) {
                                    startSecureImageAsyncReceiveTask(jsono);
                                }else{
                                    if(spemNumberCatch(jsono)){
                                        return;
                                    }
                                    String detectedFileName = Config.getFileName(jsono.get(SecureSQLiteHelper.COL_FILE_PATH).toString());
                                    JSONObject event = jsono.getJSONObject("event");
                                    String fileOuibotId = jsono.get(Config.PARAM_FROM).toString();
                                    String base64ImageData = event.get("imgcut").toString();
                                    String tm = jsono.get("time").toString();
                                    FileOutputStream fos;
                                    try {
                                        if (base64ImageData != null) {
                                            fos = new FileOutputStream(Config.getSaveImageFileExternalDirectory() + detectedFileName);
                                            byte[] decodedString = android.util.Base64.decode(base64ImageData, android.util.Base64.DEFAULT);
                                            fos.write(decodedString);
                                            fos.flush();
                                            fos.close();
                                        }

                                    }catch(Exception e){
                                        e.printStackTrace();
                                    }

                                    inserMessage2database(makeFileObjectWithMessage(fileOuibotId, Config.getSaveImageFileExternalDirectory() + detectedFileName, Config.getFileType(detectedFileName), Long.parseLong(tm),MessageListData.SEND_COMPLETE));
                                    new MediaScanner(SignalingChannel.this, new File(Config.getSaveImageFileExternalDirectory() + detectedFileName));
                                    requestNotificationOnlyChat(fileOuibotId, Config.getSaveImageFileExternalDirectory() + detectedFileName, Config.getFileType(detectedFileName));
                                    try {
                                        Thread.sleep(300);
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                    sendBroadcast(new Intent(Config.INTENT_RECEIVE_MESSAGE_EVENT));
                                }
                                break;
                            case Config.PARAM_SET_CONFIG:
                                //setConfig 요청이 들어오면 데이타를 저장하고 그에 해당하는 동작을 수행한다..
                                if (!checkMasterAvailable(jsono)) {
                                    sendMessageSetCofigAck(jsono, Config.DO_NOT_HAVE_MATER_ABILITY);
                                    return;
                                }
                                if (!Config.isMountedSDcard()) {
                                    if (!Config.SECURE_TEST_MODE) {
                                        sendMessageSDcardNotAvailable(jsono);
                                        return;
                                    }
                                }
                                if (checkConnectedPhoneCall()) {
                                    sendMessageConnectedPhoneCall(jsono);
                                    return;
                                }
                                String previousDetectOnOff = SecurePreference.getDetectOnOff();
                                save2ConfigData(jsono);
                                Thread.sleep(200);
                                ArrayList<UibotListData> masterListData = getMasterList(OuiBotPreferences.getLoginId(SignalingChannel.this));
                                for (int i = 0; i < masterListData.size(); i++) {
                                    sendMessageSetCofigAck(jsono, masterListData.get(i));
                                }
                                doStartSecureActivityOrNot(jsono, previousDetectOnOff);
                                notifyDataChangeForViewRefreshExceptNoti(jsono);
                                break;
                            case Config.PARAM_SET_CONFIG_ACK:
                                switch (jsono.getString(Config.PARAM_DESCRIPTION)) {
                                    case Config.PARAM_SUCCESS: {
                                        //NOT THING
                                        break;
                                    }
                                    case "fail_secure_start_because_momory": {
                                        Intent intent = new Intent(getApplicationContext(), SecureStartedFailPopupActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        sendBroadcast(new Intent(Config.INTENT_ACTION_REFESH_SECURE_PAGER_DATA));
                                        break;
                                    }
                                    case Config.DO_NOT_HAVE_MATER_ABILITY: {
                                        Intent intent = new Intent(Config.INTENT_ACTION_MESSAGE_TOAST);
                                        intent.putExtra(Config.INTENT_ACTION_MESSAGE_TOAST_MESSAGE, getResources().getString(R.string.you_do_not_have_master_power));
                                        sendBroadcast(intent);
                                        break;
                                    }
                                    case "view_refresh": {
                                        notifyDataChangeForViewRefreshExceptNoti(jsono);
                                        break;
                                    }
                                    case "view_refresh_with_notification": {
                                        notifyDataChangeForViewRefresh(jsono);
                                        break;
                                    }
                                    case "camera_view_request_fail": {
                                        Intent intent = new Intent(Config.INTENT_ACTION_MESSAGE_TOAST);
                                        intent.putExtra(Config.INTENT_ACTION_MESSAGE_TOAST_MESSAGE, getResources().getString(R.string.you_can_not_request_camera_request_because_mode_in_secure));
                                        sendBroadcast(intent);
                                        break;
                                    }
                                    case Config.PARAM_MICRO_SD_EMPTY: {
                                        Intent intent = new Intent(Config.INTENT_ACTION_MICRO_SDCARD_ERROR);
                                        sendBroadcast(intent);
                                        break;
                                    }
                                    case Config.PARAM_ALREADY_CONNECTED_PHONE_CALL: {
                                        Intent intent = new Intent(Config.INTENT_ACTION_ALREADY_CONNECTED_PHONE_CALL);
                                        sendBroadcast(intent);
                                        break;
                                    }
                                    case Config.PARAM_PEER_IS_NOT_LOGIN: {
                                        Intent intent = new Intent(Config.INTENT_ACTION_PEER_IS_NOT_LOGIN);
                                        sendBroadcast(intent);
                                        break;
                                    }
                                    case Config.END_OFFER_CANDIDATA: {
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Logger.w("!!!!", "soyu END_OFFER_CANDIDATA");
                                                try {
                                                    Config.THREAD_RECIEVER_CLASS.get(jsono.getString(Config.PARAM_SESSION_ID)).startChannel();
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }).start();

                                        break;
                                    }
                                    default: {
                                        Intent intent = new Intent(Config.INTENT_ACTION_MESSAGE_TOAST);
                                        intent.putExtra(Config.INTENT_ACTION_MESSAGE_TOAST_MESSAGE, getResources().getString(R.string.peer_is_not_login));
                                        sendBroadcast(intent);
                                        break;
                                    }
                                }
                                break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onClose(int i, String s, boolean b) {
                    Logger.e("!!!", "onClose i = " + i);
                    Logger.e("!!!", "onClose s = " + s);
                    Logger.e("!!!", "onClose b = " + b);

                    if (callMessenger != null) {
                        Message msg_error = Message.obtain(null, Config.NETWORK_ERROR, null);
                        try {
                            callMessenger.send(msg_error);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }

                    if ( b ) {
                        switch (i) {
                            case 1006:
                                TURN_ON_RED();
                                Logger.e("!!!", "connectErrorCount = " + connectErrorCount);
                                if ( connectErrorCount == 0 ) {
                                    connectingWebSocket();
                                    connectErrorCount++;
                                } else {
                                    connectErrorCount = 0;
                                }
                                break;
                        }
                    }
                }

                @Override
                public void onError(Exception e) {
                    e.printStackTrace();
                    Logger.i("!!!", "Error " + e.getMessage());

//                    TURN_ON_RED();
//                    try {
//                        Thread.sleep(3000);
//                    } catch (Exception exception) {}
//                    connectingWebSocket();
                }
            };
            try {
                if (mWebSocketClient != null) {
                    Logger.e("!!!", "mWebSocketClient.connect : try ..");
                    mWebSocketClient.connect();
                    Logger.e("!!!", "mWebSocketClient.connect : try finished ");
                }
                Logger.e("!!!", "mWebSocketClient = " + mWebSocketClient);
            } catch (IllegalStateException e) {
                Logger.e("!!!", "connection err msg : " + e.getMessage());
            }

            reconnectFlag = false;
        }
    }

    private String getMessageFromDatabase(String id, String time){
        ContentResolver resolver = getContentResolver();
        Cursor c = resolver.query(SecureProvider.MESSAGE_TABLE_URI, new String[]{SecureSQLiteHelper.COL_MESSAGE_DATA},
                SecureSQLiteHelper.COL_PEER_RTCID + " = ? AND " + SecureSQLiteHelper.COL_TIME + " = ? ", new String[]{id, time}, SecureSQLiteHelper.COL_TIME + " DESC");
        String message = "";
        if (c != null && c.moveToFirst()) {
            try {
                do {
                    message =  c.getString(c.getColumnIndex(SecureSQLiteHelper.COL_MESSAGE_DATA));
                } while (c.moveToNext());
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                c.close();
            }
        }
        return message;
    }

    private String getMessageSendFlag(String id, String time){
        ContentResolver resolver = getContentResolver();
        Cursor c = resolver.query(SecureProvider.MESSAGE_TABLE_URI, new String[]{SecureSQLiteHelper.COL_SEND_FLAG},
                SecureSQLiteHelper.COL_PEER_RTCID + " = ? AND " + SecureSQLiteHelper.COL_TIME + " = ? ", new String[]{id, time}, SecureSQLiteHelper.COL_TIME + " DESC");
        String flag = "";
        if (c != null && c.moveToFirst()) {
            try {
                do {
                    flag =  c.getString(c.getColumnIndex(SecureSQLiteHelper.COL_SEND_FLAG));
                } while (c.moveToNext());
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                c.close();
            }
        }
        if(flag.equals(String.valueOf(MessageListData.SEND_FLAG_YOU))){
            return String.valueOf(MessageListData.SEND_FLAG_ME);
        }else {
            return String.valueOf(MessageListData.SEND_FLAG_YOU);
        }
    }

    private String getMessageType(String id, String time){
        ContentResolver resolver = getContentResolver();
        Cursor c = resolver.query(SecureProvider.MESSAGE_TABLE_URI, new String[]{SecureSQLiteHelper.COL_TYPE},
                SecureSQLiteHelper.COL_PEER_RTCID + " = ? AND " + SecureSQLiteHelper.COL_TIME + " = ? ", new String[]{id, time}, SecureSQLiteHelper.COL_TIME + " DESC");
        String flag = String.valueOf(MessageListData.MESSAGE_TYPE);
        if (c != null && c.moveToFirst()) {
            try {
                do {
                    flag =  c.getString(c.getColumnIndex(SecureSQLiteHelper.COL_TYPE));
                } while (c.moveToNext());
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                c.close();
            }
        }
        return flag;
    }

    private void sendTotalMessageData(JSONObject json){
        try {
            JSONObject jobj = new JSONObject();
            JSONObject kobj = new JSONObject();
            String from = json.getString(Config.PARAM_FROM);
            String to = json.getString(Config.PARAM_TO);
            ContentResolver resolver = getContentResolver();
            Cursor c = resolver.query(SecureProvider.MESSAGE_TABLE_URI, new String[]{SecureSQLiteHelper.COL_TIME},
                    SecureSQLiteHelper.COL_PEER_RTCID + " = ? ", new String[]{to}, SecureSQLiteHelper.COL_TIME + " DESC");
            if (c != null && c.moveToFirst()) {
                try {
                    kobj.put("size", c.getCount());
                    int num = 0;
                    do {
                        if(num == 99){
                            break;
                        }
                        kobj.put(String.valueOf(num), c.getString(c.getColumnIndex(SecureSQLiteHelper.COL_TIME)));
                        num++;
                    } while (c.moveToNext());
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    c.close();
                }
            }

            jobj.put(Config.PARAM_TYPE, Config.PARAM_GET_CONFIG_ACK);
            jobj.put(Config.PARAM_SESSION_ID, Long.toString(System.currentTimeMillis()));
            jobj.put(Config.PARAM_FROM, to);
            jobj.put(Config.PARAM_TO, from);
            jobj.put(Config.PARAM_CODE, Config.PARAM_SUCCESS_CODE);
            jobj.put(Config.PARAM_DESCRIPTION, "get_total_message_data_ack");
            jobj.put(Config.PARAM_CONFIG, kobj);
            sendMessageToService(jobj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendTotalSecureImageData(JSONObject json){
        try {
            JSONObject jobj = new JSONObject();
            JSONObject kobj = new JSONObject();

            JSONObject childJson = json.getJSONObject(Config.PARAM_CONFIG);
            String mode = childJson.getString(Config.PARAM_MODE);
            String from = json.getString(Config.PARAM_FROM);
            String to = json.getString(Config.PARAM_TO);

            ContentResolver resolver = getContentResolver();
            Cursor c = resolver.query(SecureProvider.SECURE_MASTER_TABLE_URI, SecureSQLiteHelper.TABLE_SECURE_MASTER_ALL_COLUMNS,
                    SecureSQLiteHelper.COL_ID + " = ? AND " + SecureSQLiteHelper.COL_MODE + " = ? ", new String[]{from, mode}, SecureSQLiteHelper.COL_TIME + " DESC");
            if (c != null && c.moveToFirst()) {
                try {
                    kobj.put("size", c.getCount());
                    int num=0;
                    do {
                        kobj.put(String.valueOf(num), c.getString(c.getColumnIndex(SecureSQLiteHelper.COL_TIME)));
                        num++;
                    } while (c.moveToNext());
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    c.close();
                }
            }

            jobj.put(Config.PARAM_TYPE, Config.PARAM_GET_CONFIG_ACK);
            jobj.put(Config.PARAM_SESSION_ID, Long.toString(System.currentTimeMillis()));
            jobj.put(Config.PARAM_FROM, to);
            jobj.put(Config.PARAM_TO, from);
            jobj.put(Config.PARAM_CODE, Config.PARAM_SUCCESS_CODE);
            jobj.put(Config.PARAM_DESCRIPTION, "get_total_secure_image_data_ack");
            jobj.put(Config.PARAM_CONFIG, kobj);
            sendMessageToService(jobj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
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
    private String makeSecureImageJsonDataForIOS(String masterId, File file, String fileName, String mode, String time, String description){
        JSONObject jsono = new JSONObject();
        try {
            jsono.put(Config.PARAM_TYPE, "event");
            jsono.put(Config.PARAM_SESSION_ID, Long.toString(System.currentTimeMillis()));
            jsono.put(Config.PARAM_FROM, OuiBotPreferences.getLoginId(this));
            jsono.put(Config.PARAM_TO, masterId);
            jsono.put(SecureSQLiteHelper.COL_FILE_PATH, fileName);
            jsono.put(SecureSQLiteHelper.COL_MODE, mode);
            jsono.put(SecureSQLiteHelper.COL_TIME, time);
            jsono.put(Config.PARAM_DESCRIPTION, description);
            JSONObject json = new JSONObject();
            String data;
            try{
                data = Config.getByteStringForSecureImage(file);
            }catch (Exception e){
                e.printStackTrace();
                data = "file_deleted";
            }
            json.put("imgcut", data);
            jsono.put("event", json);
            return jsono.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String makeFileMessageImageJsonDataForIOS(String masterId, File file, String fileName, String time, String description){
        JSONObject jsono = new JSONObject();
        try {
            jsono.put(Config.PARAM_TYPE, "event");
            jsono.put(Config.PARAM_SESSION_ID, Long.toString(System.currentTimeMillis()));
            jsono.put(Config.PARAM_FROM, OuiBotPreferences.getLoginId(this));
            jsono.put(Config.PARAM_TO, masterId);
            jsono.put(SecureSQLiteHelper.COL_FILE_PATH, fileName);
            jsono.put(SecureSQLiteHelper.COL_TIME, time);
            jsono.put(Config.PARAM_DESCRIPTION, description);
            JSONObject json = new JSONObject();
            String data;
            try{
                data = Config.getByteStringForFileMessage(file);
            }catch (Exception e){
                e.printStackTrace();
                data = "file_deleted";
            }
            json.put("imgcut", data);
            jsono.put("event", json);
            return jsono.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
//    public String getByteString(File file) {
////        Logger.d(TAG, "file size = "+file.length());
//        return Base64.encodeToString(getBytesFromBitmap(resizeBitmapImageFn(BitmapFactory.decodeFile(file.getPath()), MAX_BITMAP_RESOLUTION)),
//                Base64.NO_WRAP);
//    }

//    public Bitmap resizeBitmapImageFn(
//            Bitmap bmpSource, int maxResolution){
//        int iWidth = bmpSource.getWidth();      //비트맵이미지의 넓이
//        int iHeight = bmpSource.getHeight();     //비트맵이미지의 높이
//        int newWidth = iWidth ;
//        int newHeight = iHeight ;
//        float rate;
//
//        //이미지의 가로 세로 비율에 맞게 조절
//        if(iWidth > iHeight ){
//            if(maxResolution < iWidth ){
//                rate = maxResolution / (float) iWidth ;
//                newHeight = (int) (iHeight * rate);
//                newWidth = maxResolution;
//            }
//        }else{
//            if(maxResolution < iHeight ){
//                rate = maxResolution / (float) iHeight ;
//                newWidth = (int) (iWidth * rate);
//                newHeight = maxResolution;
//            }
//        }
//
//        return Bitmap.createScaledBitmap(
//                bmpSource, newWidth, newHeight, true);
//    }

//    public byte[] getBytesFromBitmap(Bitmap bitmap) {
//        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
//        return stream.toByteArray();
//    }

    private void GO_LOGINPAGE() {
        Logger.e("!!!", "GO_LOGINPAGE");
        OuiBotPreferences.delLoginId(getApplicationContext());
        if (mainMessenger != null) {
            try {
                Message msg_hangup = Message.obtain(null, Config.GO_LOGINPAGE, null);
                mainMessenger.send(msg_hangup);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            if (mWebSocketClient != null) {
                Logger.e("!!!", "mWebSocketClient is close = "+mWebSocketClient.getReadyState().name());
                connectErrorCount = 0;
                mWebSocketClient.close();
                mWebSocketClient = null;
                System.gc();
            }
            if (mNetworkReceiver != null) {
                unregisterReceiver(mNetworkReceiver);
                mNetworkReceiver = null;
            }

        }
    }

    public synchronized void sendMessageSuccessReceive(String to, String time) {
        Logger.e("!!!", "sendMessageSuccessReceive");
        try {
            JSONObject jobj = new JSONObject();
            JSONObject kobj = new JSONObject();
            jobj.put(Config.PARAM_TYPE, Config.PARAM_GET_CONFIG_ACK);
            jobj.put(Config.PARAM_SESSION_ID, time);
            jobj.put(Config.PARAM_FROM, to);
            jobj.put(Config.PARAM_TO, OuiBotPreferences.getLoginId(this));
            jobj.put(Config.PARAM_CODE, Config.PARAM_SUCCESS_CODE);
            jobj.put(Config.PARAM_DESCRIPTION, "message_success");
            jobj.put(Config.PARAM_CONFIG, kobj);
            sendMessageToService(jobj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private synchronized void inserMessage2database(MessageListData data) {
        Logger.e("!!!", "inserMessage2database");
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

    private synchronized void requestNotificationOnlyChat(String OuibotId, String chat) {
        Logger.e("!!!", "requestNotificationOnlyChat");
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(Config.INTENT_MOVE_TO_MESSAGE_VIEW, true);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setWhen(System.currentTimeMillis());
        builder.setContentTitle(OuibotId + " : " + chat);
        builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);
        builder.setPriority(Notification.PRIORITY_MAX);
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(123456, builder.build());
        PushWakeLock.acquire(this, 3000);
    }

    private synchronized void requestNotificationOnlyChat(String OuibotId, String chat, int type) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(Config.INTENT_MOVE_TO_MESSAGE_VIEW, true);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setWhen(System.currentTimeMillis());
        String data;
        if (type == MessageListData.IMAGE_TYPE) {
            data = getResources().getString(R.string.file_image_mode);
        } else {
            data = getResources().getString(R.string.file_video_mode);
        }
        builder.setContentTitle(OuibotId + " : " + data);
        builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);
        builder.setPriority(Notification.PRIORITY_MAX);
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(123456, builder.build());
        PushWakeLock.acquire(this, 3000);

    }

    public synchronized MessageListData makeObjectWithMessage(long time, String peerId, String data, int type, int sendState) {
        return new MessageListData(OuiBotPreferences.getLoginId(this),
                peerId, peerId, data, MessageListData.SEND_FLAG_YOU, time, MessageListData.UNREAD, MessageListData.UNCHECKED, type, sendState);
    }
    public synchronized MessageListData makeFileObjectWithMessage(String peerId, String data, int type, long time,int sendState) {
        return new MessageListData(OuiBotPreferences.getLoginId(this),
                peerId, peerId, data, MessageListData.SEND_FLAG_YOU, time, MessageListData.UNREAD, MessageListData.UNCHECKED, type, sendState);
    }
    private void SEND_SOUND_FALSE() {
        Logger.e("!!!", "SEND_SOUND_FALSE");
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

//        audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
        audioManager.setStreamMute(AudioManager.STREAM_VOICE_CALL, false);

    }

    private void SEND_SOUND_TRUE() {
        Logger.e("!!!", "SEND_SOUND_TRUE");
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

//        audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
        audioManager.setStreamMute(AudioManager.STREAM_VOICE_CALL, true);
    }


    private String getSecureFile(String time){
        String path="";
        ContentResolver resolver = getContentResolver();
        Cursor c = resolver.query(SecureProvider.SECURE_MASTER_TABLE_URI, new String[]{SecureSQLiteHelper.COL_FILE_PATH},
                SecureSQLiteHelper.COL_TIME + " = ? ", new String[]{time}, SecureSQLiteHelper.COL_TIME + " desc");
        if (c != null && c.moveToFirst()) {
            try {
                do {
                    path = c.getString(c.getColumnIndex(SecureSQLiteHelper.COL_FILE_PATH));

                } while (c.moveToNext());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                c.close();
            }
        }
        return path;
    }

    private String getSecureMode(String time){
        String mode="";
        ContentResolver resolver = getContentResolver();
        Cursor c = resolver.query(SecureProvider.SECURE_MASTER_TABLE_URI, new String[]{SecureSQLiteHelper.COL_MODE},
                SecureSQLiteHelper.COL_TIME + " = ? ", new String[]{time}, SecureSQLiteHelper.COL_TIME + " desc");
        if (c != null && c.moveToFirst()) {
            try {
                do {
                    mode = c.getString(c.getColumnIndex(SecureSQLiteHelper.COL_MODE));

                } while (c.moveToNext());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                c.close();
            }
        }
        return mode;
    }

    private void startSecureImageAsyncReceiveTask(JSONObject jsono) {
        String detectedFileName = null;
        int mFileModeData = 0;
        String mFileOuibotId = null;
        String base64ImageData = null;
        long mFileSaveTime = 0;
        try {
            mFileModeData = jsono.getInt(SecureSQLiteHelper.COL_MODE);
            mFileOuibotId = jsono.get(Config.PARAM_FROM).toString();
            detectedFileName = jsono.get(SecureSQLiteHelper.COL_FILE_PATH).toString();
            mFileSaveTime = jsono.getLong(SecureSQLiteHelper.COL_TIME);
            JSONObject event = jsono.getJSONObject("event");
            base64ImageData = event.get("imgcut").toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        FileOutputStream fos;
        try {
            if (base64ImageData != null) {
                fos = new FileOutputStream(Config.getSaveImageFileExternalDirectory() + detectedFileName);
                byte[] decodedString = android.util.Base64.decode(base64ImageData, android.util.Base64.DEFAULT);
                fos.write(decodedString);
                fos.flush();
                fos.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        setSecureData2Database(mFileOuibotId, Config.getSaveImageFileExternalDirectory() + detectedFileName, String.valueOf(mFileModeData), String.valueOf(mFileSaveTime));
        showNotificationORDetectImageView(mFileSaveTime, Config.getSaveImageFileExternalDirectory() + detectedFileName, mFileModeData, mFileOuibotId);
    }

    private synchronized void setSecureData2Database(String id, String filePath, String mode, String time) {
        Logger.e("!!!", "setSecureData2Database");
        ContentValues values = new ContentValues();
        values.put(SecureSQLiteHelper.COL_ID, id);
        values.put(SecureSQLiteHelper.COL_FILE_PATH, filePath);
        values.put(SecureSQLiteHelper.COL_MODE, mode);
        values.put(SecureSQLiteHelper.COL_TIME, time);
        ContentResolver resolver = getContentResolver();
        resolver.insert(SecureProvider.SECURE_MASTER_TABLE_URI, values);
    }

    private synchronized void showNotificationORDetectImageView(long time, String filePath, int mode, String id) {
        Logger.d(TAG, "showNotificationORDetectImageView call " + filePath);
        if (filePath == null) {
            return;
        }
        Intent intent = new Intent(this, DetectedItemDetailPopupActivity.class);
        intent.putExtra("image_time_key", time);
        intent.putExtra("image_path_key", filePath);
        intent.putExtra("image_mode_key", mode);
        intent.putExtra("image_id_key", id);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        requestNotification(mode, id);
        PushWakeLock.acquire(this, 3000);

    }

    private synchronized void requestNotification(int mode, String id) {
        Logger.e("!!!", "requestNotification");
//        Intent intent = new Intent(mContext, MainActivity.class);
//        intent.putExtra(Config.INTENT_MOVE_TO_LINK_SETTING_VIEW, true);
//        intent.putExtra(Config.INTENT_MOVE_TO_LINK_SETTING_OUIBOT_ID, id);
//        intent.putExtra(Config.INTENT_MOVE_TO_LINK_SETTING_SECURE_MODE, mode);
//        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setWhen(System.currentTimeMillis());
        if (mode == Config.DETECT_SECURE_MODE) {
            builder.setContentTitle(getResources().getString(R.string.notification_detected_success_string));
        } else {
            builder.setContentTitle(getResources().getString(R.string.notification_none_activity_success_string));
        }
        builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS);
//        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);
        builder.setPriority(Notification.PRIORITY_MAX);
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(123456, builder.build());
        PushWakeLock.acquire(this, 3000);
    }

    public ThreadPoolExecutor secureExec = new ThreadPoolExecutor(1, 999, 999, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(), new RejectedExecutionHandler() {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            Logger.e("!!!", "secureExec ThreadPoolExecutor rejectedExecution");
        }
    });

    private void startMessageAsyncReceiveTask(JSONObject jsono) {
        Logger.e("!!!", "startMessageAsyncReceiveTask");
        DataSessionMessageReceiveAsyncTask mDataSessionReceiveTask = new DataSessionMessageReceiveAsyncTask(getApplicationContext(), messenger, jsono, Config.THREAD_MAX_TIME);
        mDataSessionReceiveTask.executeOnExecutor(messageExec);
    }

    public ThreadPoolExecutor messageExec = new ThreadPoolExecutor(1, 999, 999, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(), new RejectedExecutionHandler() {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            Logger.e("!!!", "messageExec ThreadPoolExecutor rejectedExecution");
        }
    });

    private void startFileImageAsyncReceiveTask(JSONObject jsono) {
        Logger.e("!!!", "startFileImageAsyncReceiveTask");
        int threadTime = 25;
        try {
            threadTime = jsono.getInt(Config.PARAM_THREAD_TIME);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(mDataSessionReceiveTask != null){
            if(mDataSessionReceiveTask.getStatus() == DataSessionFileReceiveAsyncTask.Status.RUNNING){
                try {
                    JSONObject json = new JSONObject();
                    json.put(Config.PARAM_TYPE, "offer_ack");
                    json.put(Config.PARAM_SESSION_ID, jsono.get(Config.PARAM_SESSION_ID));
                    json.put(Config.PARAM_FROM, jsono.get(Config.PARAM_FROM));
                    json.put(Config.PARAM_TO, jsono.get(Config.PARAM_TO));
                    json.put(Config.PARAM_CODE, Config.PARAM_ALREADY_DATA_CHANNEL_OPEN);
                    json.put(Config.PARAM_MODE, "file");
                    sendMessageToService(json.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return;
            }
        }

        mDataSessionReceiveTask = new DataSessionFileReceiveAsyncTask(getApplicationContext(), messenger, jsono, threadTime*2);
        mDataSessionReceiveTask.executeOnExecutor(fileImageExec);
    }

    public ThreadPoolExecutor fileImageExec = new ThreadPoolExecutor(1, 999, 999, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(), new RejectedExecutionHandler() {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            Logger.e("!!!", "fileImageExec ThreadPoolExecutor rejectedExecution");
        }
    });

    private boolean spemNumberCatch(JSONObject jsono) {
        Logger.e("!!!", "spemNumberCatch");
        boolean returnFlag = false;
        try {
            String number = jsono.getString(Config.PARAM_FROM);
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

    public void save2ConfigData(JSONObject value) {
        Logger.e("!!!", "save2ConfigData");
        try {
            JSONObject config = value.getJSONObject(Config.PARAM_CONFIG);
            int detectMode = SecurePreference.getDetectMode();
            int recordingOption = SecurePreference.getRecordingOption();
            String detectOnOff = SecurePreference.getDetectOnOff();
            int recordingTime = SecurePreference.getRecordingTime();
            int detectSensitivity = SecurePreference.getDetectSensitivity();
            int securitySettingTime = SecurePreference.getSecuritySettingTime();
            int noneActivityDetectSensitivity = SecurePreference.getNoneActivitySensitivity();
            int noneActivityCheckTime = SecurePreference.getNoneActivityCheckTime();
            int noneActivityRecordingOption = SecurePreference.getNoneActivityRecordingOption();
            int noneActivityRecordingTime = SecurePreference.getNoneActivityRecordingTime();
            if (!config.isNull(Config.PARAM_DETECT_MODE)) {
                detectMode = config.getInt(Config.PARAM_DETECT_MODE);
            }
            if (!config.isNull(Config.PARAM_DETECT_ONOFF)) {
                detectOnOff = config.getString(Config.PARAM_DETECT_ONOFF);
            }
            if (!config.isNull(Config.PARAM_RECORDING_OPTION)) {
                recordingOption = config.getInt(Config.PARAM_RECORDING_OPTION);
            }
            if (!config.isNull(Config.PARAM_RECORDING_TIME)) {
                recordingTime = config.getInt(Config.PARAM_RECORDING_TIME);
            }
            if (!config.isNull(Config.PARAM_DETECT_SENSITIVITY)) {
                detectSensitivity = config.getInt(Config.PARAM_DETECT_SENSITIVITY);
            }
            if (!config.isNull(Config.PARAM_SECURITY_SETTING_TIME)) {
                securitySettingTime = config.getInt(Config.PARAM_SECURITY_SETTING_TIME);
            }
            if (!config.isNull(Config.PARAM_NONE_ACTIVITY_SENSITIVITY)) {
                noneActivityDetectSensitivity = config.getInt(Config.PARAM_NONE_ACTIVITY_SENSITIVITY);
            }
            if (!config.isNull(Config.PARAM_NONE_ACTIVITY_CHECK_TIME)) {
                noneActivityCheckTime = config.getInt(Config.PARAM_NONE_ACTIVITY_CHECK_TIME);
            }
            if (!config.isNull(Config.PARAM_NONE_ACTIVITY_RECORDING_OPTION)) {
                noneActivityRecordingOption = config.getInt(Config.PARAM_NONE_ACTIVITY_RECORDING_OPTION);
            }
            if (!config.isNull(Config.PARAM_NONE_ACTIVITY_RECORDING_TIME)) {
                noneActivityRecordingTime = config.getInt(Config.PARAM_NONE_ACTIVITY_RECORDING_TIME);
            }
            SecurePreference.setDetectMode(detectMode);
            SecurePreference.setDetectOnOff(detectOnOff);
            SecurePreference.setRecordingOption(recordingOption);
            SecurePreference.setRecordingTime(recordingTime);
            SecurePreference.setDetectSensitivity(detectSensitivity);
            SecurePreference.setSucuritySettingTime(securitySettingTime);
            SecurePreference.setNoneActivityDetectSensitivity(noneActivityDetectSensitivity);
            SecurePreference.setNoneActivityCheckTime(noneActivityCheckTime);
            SecurePreference.setNoneActivityRecordingOption(noneActivityRecordingOption);
            SecurePreference.setNoneActivityRecordingTime(noneActivityRecordingTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doStartSecureActivityOrNot(JSONObject jsono, String previousDetectedOnOffValue) {
        Logger.d(TAG, "doStartSecureActivityOrNot call");
        try {
            JSONObject config = jsono.getJSONObject(Config.PARAM_CONFIG);
            String detectOnOff = config.getString(Config.PARAM_DETECT_ONOFF);

            if (previousDetectedOnOffValue.equals(Config.DETECT_ON)) {
                if (detectOnOff.equals(Config.DETECT_ON)) {
                    Intent broadCastIntent = new Intent(Config.INTENT_ACTION_SET_SECURE_CONFIG_DATA);
                    broadCastIntent.putExtra(Config.INTENT_DATA_JSON_KEY, jsono.toString());
                    sendBroadcast(broadCastIntent);
                } else {
                    Intent broadCastIntent = new Intent(Config.INTENT_ACTION_SECURE_ACTIVITY_FINISH);
                    sendBroadcast(broadCastIntent);
                }
            } else {
                if (detectOnOff.equals(Config.DETECT_ON)) {
                    startSecureActivity(jsono.toString(), 0);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    public boolean checkMasterAvailable(JSONObject json) {
        Logger.e("!!!", "checkMasterAvailable");

        boolean checkMasterExist = false;
        ArrayList<UibotListData> data = getMasterList(OuiBotPreferences.getLoginId(this));

        try {
            if (json.get(Config.PARAM_FROM).equals(OuiBotPreferences.getLoginId(SignalingChannel.this))) {
                checkMasterExist = true;
            }

            for (int i = 0; i < data.size(); i++) {
                if (checkMasterExist || json.get(Config.PARAM_FROM).equals(data.get(i).getMasterRtcid())) {
                    checkMasterExist = true;
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return checkMasterExist;
    }

    public boolean checkConnectedPhoneCall() {
        if (callMessenger != null) {
            Logger.d(TAG, "통화 중입니다. ");
            return true;
        } else {
            Logger.d(TAG, "통화 중이 아닙니다");
            return false;
        }
    }

    public void startSecureActivity(final String value, int delayTime) {
        Logger.e("!!!", "startSecureActivity");
        try {
            Thread.sleep(delayTime);
        }catch (Exception e){
            e.printStackTrace();
        }
        Intent intent = new Intent(getApplicationContext(), SecureActivity.class);
        intent.putExtra(Config.INTENT_DATA_JSON_KEY, value);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void sendMessageSDcardNotAvailable(JSONObject json) {
        Logger.e("!!!", "sendMessageSDcardNotAvailable");
        try {
            JSONObject jobj = new JSONObject();
            jobj.put(Config.PARAM_TYPE, Config.PARAM_SET_CONFIG_ACK);
            jobj.put(Config.PARAM_SESSION_ID, json.get(Config.PARAM_SESSION_ID));
            jobj.put(Config.PARAM_FROM, json.get(Config.PARAM_FROM));
            jobj.put(Config.PARAM_TO, json.get(Config.PARAM_TO));
            jobj.put(Config.PARAM_CODE, Config.PARAM_SDCARD_ERROR_CODE);
            jobj.put(Config.PARAM_DESCRIPTION, Config.PARAM_MICRO_SD_EMPTY);
            sendMessageToService(jobj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendMessageConnectedPhoneCall(JSONObject json) {
        Logger.e("!!!", "sendMessageConnectedPhoneCall");
        try {
            JSONObject jobj = new JSONObject();
            jobj.put(Config.PARAM_TYPE, Config.PARAM_SET_CONFIG_ACK);
            jobj.put(Config.PARAM_SESSION_ID, json.get(Config.PARAM_SESSION_ID));
            jobj.put(Config.PARAM_FROM, json.get(Config.PARAM_FROM));
            jobj.put(Config.PARAM_TO, json.get(Config.PARAM_TO));
            jobj.put(Config.PARAM_CODE, Config.PARAM_SDCARD_ERROR_CODE);
            jobj.put(Config.PARAM_DESCRIPTION, Config.PARAM_ALREADY_CONNECTED_PHONE_CALL);
            sendMessageToService(jobj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setMasterResult(JSONObject json) {
        Logger.e("!!!", "setMasterResult");
        String slaveID = "";
        String masterID = "";
        try {
            slaveID = json.getString(Config.PARAM_FROM);
            masterID = json.getString(Config.PARAM_TO);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Logger.d(TAG, "setMasterResult call id = " + slaveID);
        Logger.d(TAG, "setMasterResult call fromID = " + masterID);
        ContentResolver resolver = getContentResolver();
        Cursor c = resolver.query(SecureProvider.USER_INFO_TABLE_URI, new String[]{SecureSQLiteHelper.COL_SLAVE_ID},
                SecureSQLiteHelper.COL_SLAVE_ID + " = ? ", new String[]{masterID}, SecureSQLiteHelper.COL_TIME + " desc");
        boolean isAleadyMaster = false;
        if (c != null && c.moveToFirst()) {
            try {
                do {
                    isAleadyMaster = true;
                } while (c.moveToNext());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                c.close();

            }
        }

        if (isAleadyMaster) {
            Logger.d(TAG, "It is already master");
            return;
        }
        try {
            ContentValues values = new ContentValues();
            values.put(SecureSQLiteHelper.COL_SLAVE_ID, masterID);
            values.put(SecureSQLiteHelper.COL_MASTER_ID, slaveID);
            values.put(SecureSQLiteHelper.COL_SECURE_ON_OFF_VALUE, Config.DETECT_ONOFF_DEFAULT);
            values.put(SecureSQLiteHelper.COL_TIME, System.currentTimeMillis());
            Uri uri = resolver.insert(SecureProvider.USER_INFO_TABLE_URI, values);
            Logger.d(TAG, "insert compete " + uri);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(getApplicationContext(), MasterSettingResultActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Config.MASTER_SETTING_RESULT, masterID);
        startActivity(intent);
    }

    private ArrayList<UibotListData> getMasterList(String id) {
        Logger.e("!!!", "getMasterList");
        if (id == null) {
            return null;
        }
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
                c.close();
            }
        }
        return masterData;
    }

    private ArrayList<UibotListData> getSlaveList(String masterId) {
        Logger.e("!!!", "getSlaveList");
        if (masterId == null) {
            return null;
        }
        ContentResolver resolver = getContentResolver();
        Cursor c = resolver.query(SecureProvider.USER_INFO_TABLE_URI, new String[]{SecureSQLiteHelper.COL_SLAVE_ID},
                SecureSQLiteHelper.COL_MASTER_ID + " = ? ", new String[]{masterId}, SecureSQLiteHelper.COL_TIME + " desc");
        ArrayList<UibotListData> slaveData = new ArrayList<UibotListData>();
        if (c != null && c.moveToFirst()) {
            try {
                do {
                    String slave = c.getString(c.getColumnIndex(SecureSQLiteHelper.COL_SLAVE_ID));
                    slaveData.add(new UibotListData(null, slave, Config.DETECT_MODE_DEFAULT, Config.DETECT_ONOFF_DEFAULT, Config.VIDEO_SAVE_MODE_DEAFULT, Config.VIDEO_SAVE_TIME_DEFAULT, Config.SENSITIVITY_DEFAULT, Config.DO_AFTER_SETTING_TIME_DEFAULT
                            , Config.VIDEO_SAVE_MODE_DEAFULT, Config.NONE_ACTIVITY_VIDEO_SAVE_TIME_DEFAULT, Config.NONE_ACTIVITY_DETECTION_SENSITIVITY_DEFAULT, Config.NONE_ACTIVITY_SETTING_TIME_DEFAULT));
                } while (c.moveToNext());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                c.close();

            }
        }
        return slaveData;
    }

    private void setMasterPopup(JSONObject data) {
        Logger.e("!!!", "setMasterPopup");

        Logger.d(TAG, "setMasterPopup call = " + data.toString());
        String myId = null;
        String masterId = null;
        try {
            myId = data.getString(Config.PARAM_TO);
            masterId = data.getString(Config.PARAM_FROM);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(getApplicationContext(), MasterRequestPopupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Config.PARAM_TO, myId);
        intent.putExtra(Config.PARAM_FROM, masterId);
        startActivity(intent);
    }

    public void sendMessage_Login() {
        Logger.e("!!!", "sendMessage_Login");
        try {
            JSONObject jsono = new JSONObject();

            String loginId = OuiBotPreferences.getLoginId(this);
            if (Integer.parseInt(loginId.substring(0, 1)) < 5) {
                jsono.put("devicetype", "1");
                jsono.put(Config.PARAM_UUID, OuiBotPreferences.getUUID(getApplicationContext()));
            } else {
                jsono.put("devicetype", "2");
                jsono.put(Config.PARAM_UUID, OuiBotPreferences.getUUID(getApplicationContext()));
            }
            jsono.put("version", Config.getAppVersionName(getApplicationContext()));
            jsono.put(Config.PARAM_RTCID, loginId);
            jsono.put(Config.PARAM_TYPE, "login");
            jsono.put("password", "");
            sendMessageToService(jsono.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public void sendMessage_Logout() {
        Logger.e("!!!", "sendMessage_Logout");
        try {
            JSONObject jsono = new JSONObject();
            jsono.put(Config.PARAM_UUID, OuiBotPreferences.getUUID(getApplicationContext()));
            jsono.put(Config.PARAM_RTCID, OuiBotPreferences.getLoginId(this));
            jsono.put(Config.PARAM_TYPE, "logout");
            sendMessageToService(jsono.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage_offer_ack(JSONObject json) {
        Logger.e("!!!", "sendMessage_offer_ack 1");
        RecvOfferData = json;
        try {
            JSONObject jsono = new JSONObject();
            jsono.put(Config.PARAM_TYPE, "offer_ack");
            jsono.put(Config.PARAM_SESSION_ID, json.get(Config.PARAM_SESSION_ID));
            jsono.put(Config.PARAM_FROM, json.get(Config.PARAM_FROM));
            jsono.put(Config.PARAM_TO, json.get(Config.PARAM_TO));
            jsono.put(Config.PARAM_CODE, Config.PARAM_SUCCESS_CODE);
            jsono.put(Config.PARAM_MODE, json.get(Config.PARAM_MODE));
            sendMessageToService(jsono.toString());
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private synchronized void sendMessage_offer_ack(JSONObject json, int flag) {
        Logger.e("!!!", "sendMessage_offer_ack 2");
        RecvOfferData = json;
        try {
            JSONObject jsono = new JSONObject();
            jsono.put(Config.PARAM_TYPE, "offer_ack");
            jsono.put(Config.PARAM_SESSION_ID, json.get(Config.PARAM_SESSION_ID));
            jsono.put(Config.PARAM_FROM, json.get(Config.PARAM_FROM));
            jsono.put(Config.PARAM_TO, json.get(Config.PARAM_TO));
            jsono.put(Config.PARAM_MODE, json.get(Config.PARAM_MODE));
            switch (flag) {
                case 1:
                    jsono.put(Config.PARAM_CODE, Config.PARAM_PEER_IS_NOT_LOGIN_CODE);
                    jsono.put(Config.PARAM_DESCRIPTION, Config.PARAM_PEER_IS_NOT_LOGIN);
                    break;
                case 2:
                    jsono.put(Config.PARAM_CODE, Config.PARAM_PEER_IS_CALLING_CODE);
                    jsono.put(Config.PARAM_DESCRIPTION, Config.PARAM_PEER_IS_CALLING);
                    break;
            }
            sendMessageToService(jsono.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage_answer_ack(JSONObject json) {
        Logger.e("!!!", "sendMessage_answer_ack");
        try {
            JSONObject jsono = new JSONObject();
            jsono.put(Config.PARAM_TYPE, "answer_ack");
            jsono.put(Config.PARAM_SESSION_ID, json.get(Config.PARAM_SESSION_ID));
            jsono.put(Config.PARAM_FROM, json.get(Config.PARAM_FROM));
            jsono.put(Config.PARAM_TO, json.get(Config.PARAM_TO));
            Logger.e("!!!", "*****     sendMessage_answer_ack     *****     callMessenger = " + callMessenger);
            if (callMessenger != null) {
                jsono.put(Config.PARAM_SUB_TYPE, json.get(Config.PARAM_SUB_TYPE));
                jsono.put(Config.PARAM_CODE, Config.PARAM_SUCCESS_CODE);
                jsono.put(Config.PARAM_MODE, json.get(Config.PARAM_MODE));
                sendMessageToService(jsono.toString());
                Logger.e("!!!", "*****     SignalingChannel send ANSWER_SDP_RECV     *****");
                Message msg_finish = Message.obtain(null, Config.ANSWER_SDP_RECV, json);
                callMessenger.send(msg_finish);
            } else {
                jsono.put(Config.PARAM_CODE, Config.PARAM_PEER_IS_NOT_LOGIN_CODE);
                jsono.put(Config.PARAM_DESCRIPTION, Config.PARAM_PEER_IS_NOT_LOGIN);
                sendMessageToService(jsono.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMessage_data_channel_secure_answer_ack(JSONObject json) {
        Logger.e("!!!", "sendMessage_data_channel_secure_answer_ack");
        try {
            JSONObject jsono = new JSONObject();
            jsono.put(Config.PARAM_TYPE, "answer_ack");
            jsono.put(Config.PARAM_SESSION_ID, json.get(Config.PARAM_SESSION_ID));
            jsono.put(Config.PARAM_FROM, json.get(Config.PARAM_FROM));
            jsono.put(Config.PARAM_TO, json.get(Config.PARAM_TO));
            jsono.put(Config.PARAM_CODE, Config.PARAM_SUCCESS_CODE);
            jsono.put(Config.PARAM_MODE, json.get(Config.PARAM_MODE));
            sendMessageToService(jsono.toString());
            Config.THREAD_SENDER_CLASS.get(json.get(Config.PARAM_SESSION_ID)).callAnswerSdpRecv(json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMessage_hangup_ack(JSONObject json) {
        Logger.e("!!!", "sendMessage_hangup_ack");
        try {
            JSONObject jsono = new JSONObject();
            jsono.put(Config.PARAM_TYPE, "hangup_ack");
            jsono.put(Config.PARAM_SUB_TYPE, json.get(Config.PARAM_SUB_TYPE));
            jsono.put(Config.PARAM_SESSION_ID, json.get(Config.PARAM_SESSION_ID));
            jsono.put(Config.PARAM_FROM, json.get(Config.PARAM_FROM));
            jsono.put(Config.PARAM_TO, json.get(Config.PARAM_TO));
            jsono.put(Config.PARAM_CODE, Config.PARAM_SUCCESS_CODE);
            jsono.put(Config.PARAM_DESCRIPTION, "");
            sendMessageToService(jsono.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void requestConfigChange(JSONObject json) {
        Logger.e("!!!", "requestConfigChange");
        sendMessageToService(json.toString());
    }

    private synchronized void sendMessageSetCofigAck(JSONObject json, String message) {
        Logger.e("!!!", "sendMessageSetCofigAck");
        try {
            JSONObject jobj = new JSONObject();
            JSONObject kobj = new JSONObject();
            kobj.put(Config.PARAM_DETECT_MODE, SecurePreference.getDetectMode());
            kobj.put(Config.PARAM_DETECT_ONOFF, SecurePreference.getDetectOnOff());
            kobj.put(Config.PARAM_RECORDING_OPTION, SecurePreference.getRecordingOption());
            kobj.put(Config.PARAM_RECORDING_TIME, SecurePreference.getRecordingTime());
            kobj.put(Config.PARAM_DETECT_SENSITIVITY, SecurePreference.getDetectSensitivity());
            kobj.put(Config.PARAM_SECURITY_SETTING_TIME, SecurePreference.getSecuritySettingTime());
            kobj.put(Config.PARAM_NONE_ACTIVITY_SENSITIVITY, SecurePreference.getNoneActivitySensitivity());
            kobj.put(Config.PARAM_NONE_ACTIVITY_CHECK_TIME, SecurePreference.getNoneActivityCheckTime());
            kobj.put(Config.PARAM_NONE_ACTIVITY_RECORDING_OPTION, SecurePreference.getNoneActivityRecordingOption());
            kobj.put(Config.PARAM_NONE_ACTIVITY_RECORDING_TIME, SecurePreference.getNoneActivityRecordingTime());
            jobj.put(Config.PARAM_TYPE, Config.PARAM_SET_CONFIG_ACK);
            jobj.put(Config.PARAM_SESSION_ID, json.get(Config.PARAM_SESSION_ID));
            jobj.put(Config.PARAM_FROM, json.get(Config.PARAM_FROM));
            jobj.put(Config.PARAM_TO, json.get(Config.PARAM_TO));
            jobj.put(Config.PARAM_CODE, Config.PARAM_SUCCESS_CODE);
            jobj.put(Config.PARAM_DESCRIPTION, message);
            jobj.put(Config.PARAM_CONFIG, kobj);
            sendMessageToService(jobj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendMessageSetCofigAck(JSONObject json, UibotListData masterListData) {
        Logger.e("!!!", "sendMessageSetCofigAck");
        try {
            JSONObject jobj = new JSONObject();
            JSONObject kobj = new JSONObject();
            kobj.put(Config.PARAM_DETECT_MODE, SecurePreference.getDetectMode());
            kobj.put(Config.PARAM_DETECT_ONOFF, SecurePreference.getDetectOnOff());
            kobj.put(Config.PARAM_RECORDING_OPTION, SecurePreference.getRecordingOption());
            kobj.put(Config.PARAM_RECORDING_TIME, SecurePreference.getRecordingTime());
            kobj.put(Config.PARAM_DETECT_SENSITIVITY, SecurePreference.getDetectSensitivity());
            kobj.put(Config.PARAM_SECURITY_SETTING_TIME, SecurePreference.getSecuritySettingTime());
            kobj.put(Config.PARAM_NONE_ACTIVITY_SENSITIVITY, SecurePreference.getNoneActivitySensitivity());
            kobj.put(Config.PARAM_NONE_ACTIVITY_CHECK_TIME, SecurePreference.getNoneActivityCheckTime());
            kobj.put(Config.PARAM_NONE_ACTIVITY_RECORDING_OPTION, SecurePreference.getNoneActivityRecordingOption());
            kobj.put(Config.PARAM_NONE_ACTIVITY_RECORDING_TIME, SecurePreference.getNoneActivityRecordingTime());
            jobj.put(Config.PARAM_TYPE, Config.PARAM_SET_CONFIG_ACK);
            jobj.put(Config.PARAM_SESSION_ID, json.get(Config.PARAM_SESSION_ID));
            jobj.put(Config.PARAM_FROM, masterListData.getMasterRtcid());
            jobj.put(Config.PARAM_TO, json.get(Config.PARAM_TO));
            jobj.put(Config.PARAM_CODE, Config.PARAM_SUCCESS_CODE);
            jobj.put(Config.PARAM_DESCRIPTION, Config.PARAM_SUCCESS);
            jobj.put(Config.PARAM_CONFIG, kobj);
            sendMessageToService(jobj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendMessageGetCofigAck(String data) {
        Logger.e("!!!", "sendMessageGetCofigAck");
        try {
            JSONObject json = new JSONObject(data);
            JSONObject jobj = new JSONObject();
            JSONObject kobj = new JSONObject();
            kobj.put(Config.PARAM_DETECT_MODE, SecurePreference.getDetectMode());
            kobj.put(Config.PARAM_DETECT_ONOFF, SecurePreference.getDetectOnOff());
            kobj.put(Config.PARAM_RECORDING_OPTION, SecurePreference.getRecordingOption());
            kobj.put(Config.PARAM_RECORDING_TIME, SecurePreference.getRecordingTime());
            kobj.put(Config.PARAM_DETECT_SENSITIVITY, SecurePreference.getDetectSensitivity());
            kobj.put(Config.PARAM_SECURITY_SETTING_TIME, SecurePreference.getSecuritySettingTime());
            kobj.put(Config.PARAM_NONE_ACTIVITY_SENSITIVITY, SecurePreference.getNoneActivitySensitivity());
            kobj.put(Config.PARAM_NONE_ACTIVITY_CHECK_TIME, SecurePreference.getNoneActivityCheckTime());
            kobj.put(Config.PARAM_NONE_ACTIVITY_RECORDING_OPTION, SecurePreference.getNoneActivityRecordingOption());
            kobj.put(Config.PARAM_NONE_ACTIVITY_RECORDING_TIME, SecurePreference.getNoneActivityRecordingTime());
            jobj.put(Config.PARAM_TYPE, Config.PARAM_GET_CONFIG_ACK);
            jobj.put(Config.PARAM_SESSION_ID, json.get(Config.PARAM_SESSION_ID));
            jobj.put(Config.PARAM_FROM, json.get(Config.PARAM_FROM));
            jobj.put(Config.PARAM_TO, json.get(Config.PARAM_TO));
            jobj.put(Config.PARAM_CODE, Config.PARAM_SUCCESS_CODE);
            jobj.put(Config.PARAM_DESCRIPTION, "view_refresh");
            jobj.put(Config.PARAM_CONFIG, kobj);
            sendMessageToService(jobj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendMessageSetCofigAckBroasdCast(String data) {
        Logger.e("!!!", "sendMessageSetCofigAckBroasdCast");
        try {
            JSONObject jobj = new JSONObject();
            JSONObject kobj = new JSONObject();
            kobj.put(Config.PARAM_DETECT_MODE, SecurePreference.getDetectMode());
            kobj.put(Config.PARAM_DETECT_ONOFF, SecurePreference.getDetectOnOff());
            kobj.put(Config.PARAM_RECORDING_OPTION, SecurePreference.getRecordingOption());
            kobj.put(Config.PARAM_RECORDING_TIME, SecurePreference.getRecordingTime());
            kobj.put(Config.PARAM_DETECT_SENSITIVITY, SecurePreference.getDetectSensitivity());
            kobj.put(Config.PARAM_SECURITY_SETTING_TIME, SecurePreference.getSecuritySettingTime());
            kobj.put(Config.PARAM_NONE_ACTIVITY_SENSITIVITY, SecurePreference.getNoneActivitySensitivity());
            kobj.put(Config.PARAM_NONE_ACTIVITY_CHECK_TIME, SecurePreference.getNoneActivityCheckTime());
            kobj.put(Config.PARAM_NONE_ACTIVITY_RECORDING_OPTION, SecurePreference.getNoneActivityRecordingOption());
            kobj.put(Config.PARAM_NONE_ACTIVITY_RECORDING_TIME, SecurePreference.getNoneActivityRecordingTime());
            jobj.put(Config.PARAM_TYPE, Config.PARAM_SET_CONFIG_ACK);
            jobj.put(Config.PARAM_SESSION_ID, System.currentTimeMillis());
            jobj.put(Config.PARAM_FROM, data);
            jobj.put(Config.PARAM_TO, OuiBotPreferences.getLoginId(this));
            jobj.put(Config.PARAM_CODE, Config.PARAM_SUCCESS_CODE);
            jobj.put(Config.PARAM_DESCRIPTION, "view_refresh");
            jobj.put(Config.PARAM_CONFIG, kobj);
            sendMessageToService(jobj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendMessageSetCofigAckBroasdCastWithNotification(String data) {
        Logger.e("!!!", "sendMessageSetCofigAckBroasdCastWithNotification");
        try {
            JSONObject jobj = new JSONObject();
            JSONObject kobj = new JSONObject();
            kobj.put(Config.PARAM_DETECT_MODE, SecurePreference.getDetectMode());
            kobj.put(Config.PARAM_DETECT_ONOFF, SecurePreference.getDetectOnOff());
            kobj.put(Config.PARAM_RECORDING_OPTION, SecurePreference.getRecordingOption());
            kobj.put(Config.PARAM_RECORDING_TIME, SecurePreference.getRecordingTime());
            kobj.put(Config.PARAM_DETECT_SENSITIVITY, SecurePreference.getDetectSensitivity());
            kobj.put(Config.PARAM_SECURITY_SETTING_TIME, SecurePreference.getSecuritySettingTime());
            kobj.put(Config.PARAM_NONE_ACTIVITY_SENSITIVITY, SecurePreference.getNoneActivitySensitivity());
            kobj.put(Config.PARAM_NONE_ACTIVITY_CHECK_TIME, SecurePreference.getNoneActivityCheckTime());
            kobj.put(Config.PARAM_NONE_ACTIVITY_RECORDING_OPTION, SecurePreference.getNoneActivityRecordingOption());
            kobj.put(Config.PARAM_NONE_ACTIVITY_RECORDING_TIME, SecurePreference.getNoneActivityRecordingTime());
            jobj.put(Config.PARAM_TYPE, Config.PARAM_SET_CONFIG_ACK);
            jobj.put(Config.PARAM_SESSION_ID, System.currentTimeMillis());
            jobj.put(Config.PARAM_FROM, data);
            jobj.put(Config.PARAM_TO, OuiBotPreferences.getLoginId(this));
            jobj.put(Config.PARAM_CODE, Config.PARAM_SUCCESS_CODE);
            jobj.put(Config.PARAM_DESCRIPTION, "view_refresh_with_notification");
            jobj.put(Config.PARAM_CONFIG, kobj);
            new GetDeviceTokenhttpTask(jobj).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "get_device_token_post.php", "rtcid=" + data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private class GetDeviceTokenhttpTask extends android.os.AsyncTask<String, Void, String> {
        private JSONObject mJsonData;
        public GetDeviceTokenhttpTask(JSONObject data){
            mJsonData = data;
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
                String title = OuiBotPreferences.getLoginId(SignalingChannel.this);
                String message = "";

                try {
                    JSONObject childJson = mJsonData.getJSONObject(Config.PARAM_CONFIG);
                    if (childJson.getString(Config.PARAM_DETECT_ONOFF).equals(Config.DETECT_OFF)) {
                        if (childJson.getInt(Config.PARAM_DETECT_MODE) == Config.DETECT_MOVEMENT_MODE) {
                            message = getResources().getString(R.string.notification_none_activity_finish);
                        } else {
                            message = getResources().getString(R.string.notification_detected_finish);
                        }
                    } else {
                        if (childJson.getInt(Config.PARAM_DETECT_MODE) == Config.DETECT_SECURE_MODE) {
                            message = getResources().getString(R.string.notification_detected_start);
                        } else {
                            message = getResources().getString(R.string.notification_none_activity_start);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                new ForIOSNotificationHttpAsyncTask(token, title, message, null, false, null, Config.SECURE_GET_CONFIG_TYPE).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                sendMessageToService(mJsonData.toString());
            }

        }
    }

    private void sendMessageSetCofigFailAckBroasdCast(String data) {
        Logger.e("!!!", "sendMessageSetCofigFailAckBroasdCast");
        try {
            JSONObject jobj = new JSONObject();
            JSONObject kobj = new JSONObject();
            kobj.put(Config.PARAM_DETECT_MODE, SecurePreference.getDetectMode());
            kobj.put(Config.PARAM_DETECT_ONOFF, SecurePreference.getDetectOnOff());
            kobj.put(Config.PARAM_RECORDING_OPTION, SecurePreference.getRecordingOption());
            kobj.put(Config.PARAM_RECORDING_TIME, SecurePreference.getRecordingTime());
            kobj.put(Config.PARAM_DETECT_SENSITIVITY, SecurePreference.getDetectSensitivity());
            kobj.put(Config.PARAM_SECURITY_SETTING_TIME, SecurePreference.getSecuritySettingTime());
            kobj.put(Config.PARAM_NONE_ACTIVITY_SENSITIVITY, SecurePreference.getNoneActivitySensitivity());
            kobj.put(Config.PARAM_NONE_ACTIVITY_CHECK_TIME, SecurePreference.getNoneActivityCheckTime());
            kobj.put(Config.PARAM_NONE_ACTIVITY_RECORDING_OPTION, SecurePreference.getNoneActivityRecordingOption());
            kobj.put(Config.PARAM_NONE_ACTIVITY_RECORDING_TIME, SecurePreference.getNoneActivityRecordingTime());
            jobj.put(Config.PARAM_TYPE, Config.PARAM_SET_CONFIG_ACK);
            jobj.put(Config.PARAM_SESSION_ID, System.currentTimeMillis());
            jobj.put(Config.PARAM_FROM, data);
            jobj.put(Config.PARAM_TO, OuiBotPreferences.getLoginId(this));
            jobj.put(Config.PARAM_CODE, Config.PARAM_SUCCESS_CODE);
            jobj.put(Config.PARAM_DESCRIPTION, "fail_secure_start_because_momory");
            jobj.put(Config.PARAM_CONFIG, kobj);
            sendMessageToService(jobj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendMessageGetCofigAckMasterRequestFail(JSONObject json) {
        Logger.e("!!!", "sendMessageGetCofigAckMasterRequestFail");
        try {
            JSONObject jobj = new JSONObject();
            JSONObject kobj = new JSONObject();
            kobj.put(Config.PARAM_DETECT_MODE, SecurePreference.getDetectMode());
            kobj.put(Config.PARAM_DETECT_ONOFF, SecurePreference.getDetectOnOff());
            kobj.put(Config.PARAM_RECORDING_OPTION, SecurePreference.getRecordingOption());
            kobj.put(Config.PARAM_RECORDING_TIME, SecurePreference.getRecordingTime());
            kobj.put(Config.PARAM_DETECT_SENSITIVITY, SecurePreference.getDetectSensitivity());
            kobj.put(Config.PARAM_SECURITY_SETTING_TIME, SecurePreference.getSecuritySettingTime());
            kobj.put(Config.PARAM_NONE_ACTIVITY_SENSITIVITY, SecurePreference.getNoneActivitySensitivity());
            kobj.put(Config.PARAM_NONE_ACTIVITY_CHECK_TIME, SecurePreference.getNoneActivityCheckTime());
            kobj.put(Config.PARAM_NONE_ACTIVITY_RECORDING_OPTION, SecurePreference.getNoneActivityRecordingOption());
            kobj.put(Config.PARAM_NONE_ACTIVITY_RECORDING_TIME, SecurePreference.getNoneActivityRecordingTime());
            jobj.put(Config.PARAM_TYPE, Config.PARAM_GET_CONFIG_ACK);
            jobj.put(Config.PARAM_SESSION_ID, json.get(Config.PARAM_SESSION_ID));
            jobj.put(Config.PARAM_FROM, json.get(Config.PARAM_FROM));
            jobj.put(Config.PARAM_TO, json.get(Config.PARAM_TO));
            jobj.put(Config.PARAM_CODE, Config.PARAM_SUCCESS_CODE);
            jobj.put(Config.PARAM_DESCRIPTION, "master_request_fail");
            jobj.put(Config.PARAM_CONFIG, kobj);
            sendMessageToService(jobj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendMessageGetCofigAckCameraViewRequestFail(JSONObject json) {
        Logger.e("!!!", "sendMessageGetCofigAckCameraViewRequestFail");
        try {
            JSONObject jobj = new JSONObject();
            JSONObject kobj = new JSONObject();
            kobj.put(Config.PARAM_DETECT_MODE, SecurePreference.getDetectMode());
            kobj.put(Config.PARAM_DETECT_ONOFF, SecurePreference.getDetectOnOff());
            kobj.put(Config.PARAM_RECORDING_OPTION, SecurePreference.getRecordingOption());
            kobj.put(Config.PARAM_RECORDING_TIME, SecurePreference.getRecordingTime());
            kobj.put(Config.PARAM_DETECT_SENSITIVITY, SecurePreference.getDetectSensitivity());
            kobj.put(Config.PARAM_SECURITY_SETTING_TIME, SecurePreference.getSecuritySettingTime());
            kobj.put(Config.PARAM_NONE_ACTIVITY_SENSITIVITY, SecurePreference.getNoneActivitySensitivity());
            kobj.put(Config.PARAM_NONE_ACTIVITY_CHECK_TIME, SecurePreference.getNoneActivityCheckTime());
            kobj.put(Config.PARAM_NONE_ACTIVITY_RECORDING_OPTION, SecurePreference.getNoneActivityRecordingOption());
            kobj.put(Config.PARAM_NONE_ACTIVITY_RECORDING_TIME, SecurePreference.getNoneActivityRecordingTime());
            jobj.put(Config.PARAM_TYPE, Config.PARAM_GET_CONFIG_ACK);
            jobj.put(Config.PARAM_SESSION_ID, json.get(Config.PARAM_SESSION_ID));
            jobj.put(Config.PARAM_FROM, json.get(Config.PARAM_FROM));
            jobj.put(Config.PARAM_TO, json.get(Config.PARAM_TO));
            jobj.put(Config.PARAM_CODE, Config.PARAM_SUCCESS_CODE);
            jobj.put(Config.PARAM_DESCRIPTION, "camera_view_request_fail");
            jobj.put(Config.PARAM_CONFIG, kobj);
            sendMessageToService(jobj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void sendCamMove(String data, String message) {
        Logger.e("!!!", "sendCamMove");
        try {
            JSONObject json = new JSONObject();
            JSONObject jobj = new JSONObject();
            JSONObject kobj = new JSONObject();
            kobj.put(Config.PARAM_DETECT_MODE, SecurePreference.getDetectMode());
            jobj.put(Config.PARAM_TYPE, Config.PARAM_GET_CONFIG_ACK);
            jobj.put(Config.PARAM_SESSION_ID, System.currentTimeMillis());
            jobj.put(Config.PARAM_FROM, data);
            jobj.put(Config.PARAM_TO, OuiBotPreferences.getLoginId(this));
            jobj.put(Config.PARAM_CODE, Config.PARAM_SUCCESS_CODE);
            jobj.put(Config.PARAM_DESCRIPTION, message);
            jobj.put(Config.PARAM_CONFIG, kobj);
            sendMessageToService(jobj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendMasterDeleteWithCofigAck(String data) {
        Logger.e("!!!", "sendMasterDeleteWithCofigAck");
        try {
            JSONObject jobj = new JSONObject();
            JSONObject kobj = new JSONObject();
            kobj.put(Config.PARAM_DETECT_MODE, SecurePreference.getDetectMode());
            jobj.put(Config.PARAM_TYPE, Config.PARAM_GET_CONFIG_ACK);
            jobj.put(Config.PARAM_SESSION_ID, System.currentTimeMillis());
            jobj.put(Config.PARAM_FROM, data);
            jobj.put(Config.PARAM_TO, OuiBotPreferences.getLoginId(this));
            jobj.put(Config.PARAM_CODE, Config.PARAM_SUCCESS_CODE);
            jobj.put(Config.PARAM_DESCRIPTION, "delete_master");
            jobj.put(Config.PARAM_CONFIG, kobj);
            sendMessageToService(jobj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendSlaveDeleteWithCofigAck(String data) {
        Logger.e("!!!", "sendSlaveDeleteWithCofigAck");
        try {
            JSONObject jobj = new JSONObject();
            JSONObject kobj = new JSONObject();
            kobj.put(Config.PARAM_DETECT_MODE, SecurePreference.getDetectMode());
            jobj.put(Config.PARAM_TYPE, Config.PARAM_GET_CONFIG_ACK);
            jobj.put(Config.PARAM_SESSION_ID, System.currentTimeMillis());
            jobj.put(Config.PARAM_FROM, data);
            jobj.put(Config.PARAM_TO, OuiBotPreferences.getLoginId(this));
            jobj.put(Config.PARAM_CODE, Config.PARAM_SUCCESS_CODE);
            jobj.put(Config.PARAM_DESCRIPTION, "delete_slave");
            jobj.put(Config.PARAM_CONFIG, kobj);
            sendMessageToService(jobj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendSlaveDeleteWithCofigAck(String data, String sessionId) {
        Logger.e("!!!", "sendSlaveDeleteWithCofigAck");
        try {
            JSONObject jobj = new JSONObject();
            JSONObject kobj = new JSONObject();
            kobj.put(Config.PARAM_DETECT_MODE, SecurePreference.getDetectMode());
            jobj.put(Config.PARAM_TYPE, Config.PARAM_GET_CONFIG_ACK);
            jobj.put(Config.PARAM_SESSION_ID, sessionId);
            jobj.put(Config.PARAM_FROM, data);
            jobj.put(Config.PARAM_TO, OuiBotPreferences.getLoginId(this));
            jobj.put(Config.PARAM_CODE, Config.PARAM_SUCCESS_CODE);
            jobj.put(Config.PARAM_DESCRIPTION, "delete_slave");
            jobj.put(Config.PARAM_CONFIG, kobj);
            sendMessageToService(jobj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private int link_flag = 0;

    private void sendMessage_candidate(boolean flag) {
        Logger.e("!!!", "sendMessage_candidate");
        if (flag) {
            link_flag = 1;
            for (JSONObject candidate : Candidates) {
                try {
                    candidate.put(Config.PARAM_TYPE, Config.PARAM_CANDIDATE);
                    candidate.put(Config.PARAM_SUB_TYPE, "offer");
                    candidate.put(Config.PARAM_SESSION_ID, SendOfferData.getString(Config.PARAM_SESSION_ID));
                    candidate.put(Config.PARAM_TO, SendOfferData.getString(Config.PARAM_TO));
                    candidate.put(Config.PARAM_FROM, SendOfferData.getString(Config.PARAM_FROM));
                    Logger.e(TAG, "Candidates offer send call " + candidate.toString());
                    sendMessageToService(candidate.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else {
            link_flag = 2;
            for (JSONObject candidate : Candidates) {
                try {
                    candidate.put(Config.PARAM_TYPE, Config.PARAM_CANDIDATE);
                    candidate.put(Config.PARAM_SUB_TYPE, "answer");
                    candidate.put(Config.PARAM_SESSION_ID, RecvOfferData.getString(Config.PARAM_SESSION_ID));
                    candidate.put(Config.PARAM_TO, RecvOfferData.getString(Config.PARAM_TO));
                    candidate.put(Config.PARAM_FROM, RecvOfferData.getString(Config.PARAM_FROM));
                    Logger.e(TAG, "Candidates answer send call " + candidate.toString());
                    sendMessageToService(candidate.toString());

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        Candidates.clear();

        try {
            if (callMessenger != null) {
                Message msg_finish = Message.obtain(null, Config.ANSWER_ACK_RECV, null);
                callMessenger.send(msg_finish);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMessageToService(String json) {
        Logger.w("!!!", "sendMessageToService json = " + json);
        try {
            mWebSocketClient.send(json);
        } catch (WebsocketNotConnectedException e) {
            e.printStackTrace();
            if(isRunningProcess()) {
                Toast.makeText(this, getString(R.string.connection_faile_please_retry), Toast.LENGTH_SHORT).show();
                Logger.e("!!!", "alex retry");
            }
            connectingWebSocket();
        } catch (NullPointerException e) {
            e.printStackTrace();
            if(isRunningProcess()) {
                Toast.makeText(this, getString(R.string.connection_faile_please_retry), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public boolean isRunningProcess() {
        Logger.e("!!!", "isRunningProcess");

        boolean isRunning = false;

        ActivityManager actMng = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);

        List<ActivityManager.RunningAppProcessInfo> list = actMng.getRunningAppProcesses();

        for(ActivityManager.RunningAppProcessInfo rap : list)
        {
            if(rap.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND)
            {
                String strPackage = rap.processName; //package이름과 동일함.
                Logger.d(TAG,"strPackage = "+strPackage);
                if(strPackage.equals(getPackageName())) {
                    isRunning = true;
                }
                break;
            }
        }

        return isRunning;
    }

    private void requestNotificationOnly(String message) {
        Logger.e("!!!", "requestNotificationOnly");
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setWhen(System.currentTimeMillis());
        builder.setContentTitle(message);
        builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);
        builder.setPriority(Notification.PRIORITY_MAX);
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(123456, builder.build());
    }

    private void requestNotificationNotthing(String message) {
        Logger.e("!!!", "requestNotificationNotthing");
        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setWhen(System.currentTimeMillis());
        builder.setContentTitle(message);
        builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS);
        builder.setAutoCancel(true);
        builder.setPriority(Notification.PRIORITY_MAX);
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(123456, builder.build());
    }

    class NetworkReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Logger.e("!!!", "NetworkReceiver onReceive");
            if ( OuiBotPreferences.getLoginId(getApplicationContext()) == null ) {
                return;
            }
            String action = intent.getAction();
            Logger.e("!!!", "~~~~~~~~~~~ NetworkReceiver action = "+action);
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                Logger.e("!!!", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                Logger.e("!!!", "network connect");
                ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

                if (Config.Mode == Config.COMPILE_Ouibot) {
                    NetworkInfo ethernet = manager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
                    NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                    int internet_conn_flag = 0;

                    if (wifi != null && wifi.isConnected()) {
                        internet_conn_flag |= FLAG_WIFI_CONNECTED;
                    } else {
                        internet_conn_flag |= FLAG_WIFI_DISCONNECTED;
                    }

                    if (ethernet != null && ethernet.isConnected()) {
                        internet_conn_flag |= FLAG_ETHERNET_CONNECTED;
                    } else {
                        internet_conn_flag |= FLAG_ETHERNET_DISCONNECTED;
                    }

                    if (internet_conn_flag == 6) {
                        Logger.w("!!!", "ethernet connect");
                        internet_conn_flag = 1;
                    } else if (internet_conn_flag == 9) {
                        Logger.w("!!!", "wifi connect");
                        internet_conn_flag = 1;
                    } else if (internet_conn_flag == 5) {
                        Logger.w("!!!", "all connect");
                        internet_conn_flag = 1;
                    }
                    Logger.w("!!!", "cur_net_stat = "+cur_net_stat+", internet_conn_flag = "+internet_conn_flag);
                    if (cur_net_stat != internet_conn_flag) {
                        if (internet_conn_flag == 10) {
                            Logger.w("!!!", "not connect");
                        } else {
                            CERTIFICATION_ANSWER();
                            connectingWebSocket();
                            if (SecurePreference.getDetectOnOff().equals(Config.DETECT_ON) || SecurePreference.getDetectForceOnOff().equals(Config.DETECT_ON)) {
                                if (!isForceStartSecureMode) {
                                    startSecureActivity(null, 3000);
                                    isForceStartSecureMode = true;
                                }
                            }
                        }
                    }
                    cur_net_stat = internet_conn_flag;
                    Logger.w("!!!", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~   Config.COMPILE_Ouibot");
                } else {
                    NetworkInfo network = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                    NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                    int internet_conn_flag = 0;

                    if (wifi != null && wifi.isConnected()) {
                        internet_conn_flag |= FLAG_WIFI_CONNECTED;
                    } else {
                        internet_conn_flag |= FLAG_WIFI_DISCONNECTED;
                    }

                    if (network != null && network.isConnected()) {
                        internet_conn_flag |= FLAG_ETHERNET_CONNECTED;
                    } else {
                        internet_conn_flag |= FLAG_ETHERNET_DISCONNECTED;
                    }

                    if (internet_conn_flag == 6) {
                        Logger.w("!!!", "network connect");
                    } else if (internet_conn_flag == 9) {
                        Logger.w("!!!", "wifi connect");
                    } else if (internet_conn_flag == 5) {
                        Logger.w("!!!", "all connect");
                    }

                    Logger.w("!!!", "cur_net_stat = "+cur_net_stat+", internet_conn_flag = "+internet_conn_flag);
                    if (cur_net_stat != internet_conn_flag) {
                        if (internet_conn_flag == 10) {
                            Logger.w("!!!", "not connect");
                        } else {
                            CERTIFICATION_ANSWER();
                            connectingWebSocket();
                            if (SecurePreference.getDetectOnOff().equals(Config.DETECT_ON) || SecurePreference.getDetectForceOnOff().equals(Config.DETECT_ON)) {
                                if (!isForceStartSecureMode) {
                                    startSecureActivity(null, 2000);
                                    isForceStartSecureMode = true;
                                }
                            }
                        }
                    }
                    cur_net_stat = internet_conn_flag;
//                    android.os.Process.killProcess((android.os.Process.myPid()));
                    Logger.e("!!!", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~   Config.COMPILE_Android");
                }
                reconnectFlag = false;
            } else if (action.equals("ACTION.SEND.CHECK")) {
                Logger.e("!!!", "+++++++++++++++++++++++++++++");
//                send_check_ack();
                run_check();
//                checkackHandler.post(checkackRunnable);

            } else if (action.equals("android.intent.action.PHONE_STATE")) {
                String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
                String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
                if (stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                    if (number != null) {
                        if (!CalledNumber.equals("") && callMessenger != null) {
                            HangupSend();
                        }
                    }
                }
            }
        }
    }


    public static void CAM_MOVE_LEFT() {
        try {
            Logger.e("!!!", "CAM_MOVE_LEFT..");
            Runtime runtime = Runtime.getRuntime();
            runtime.exec("/system/bin/motor_app -s 15 1 0 15");
        } catch (IOException e) {
        }
    }

    public static void CAM_MOVE_RIGHT() {
        try {
            Logger.e("!!!", "CAM_MOVE_RIGHT..");
            Runtime runtime = Runtime.getRuntime();
            runtime.exec("/system/bin/motor_app -s 15 0 0 15");
        } catch (IOException e) {
        }
    }

    public static void CAM_MOVE_UP() {
        try {
            Logger.e("!!!", "CAM_MOVE_UP..");
            Runtime runtime = Runtime.getRuntime();
            runtime.exec("/system/bin/motor_app -s 240 0 0 10");
        } catch (IOException e) {
        }
    }

    public static void CAM_MOVE_DOWN() {
        try {
            Logger.e("!!!", "CAM_MOVE_DOWN..");
            Runtime runtime = Runtime.getRuntime();
            runtime.exec("/system/bin/motor_app -s 240 0 1 10");
        } catch (IOException e) {
        }
    }


    private void CERTIFICATION_SEND(JSONObject data) {
        Logger.e("!!!", "CERTIFICATION_SEND");
        String myId = null;
        String masterId = null;
        try {
            myId = data.getString(Config.PARAM_TO);
            masterId = data.getString(Config.PARAM_FROM);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(getApplicationContext(), ViewRequestPopupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Config.PARAM_TO, myId);
        intent.putExtra(Config.PARAM_FROM, masterId);
        startActivity(intent);
    }

    private void CERTIFICATION_ANSWER() {
        Logger.e("!!!", "CERTIFICATION_ANSWER");
        if (mainMessenger != null) {
            try {
                Message msg_hangup = Message.obtain(null, Config.CONTACT_RELOAD, null);
                mainMessenger.send(msg_hangup);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        Logger.e("!!!", "onDestroy");


//        Logger.e("!!!!!", "SignalingChannel onDestroy");
//        if (mWebSocketClient != null) {
//            mWebSocketClient.close();
//            mWebSocketClient = null;
//        }
        if (mNetworkReceiver != null) {
            unregisterReceiver(mNetworkReceiver);
            mNetworkReceiver = null;
        }
        if ( !logoutFlag ) {
            registerRestartAlarm();
        }
        super.onDestroy();
//        unregisterReceiver(mMediaButtonReceiver);

    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
//        super.onTaskRemoved(rootIntent);
        Logger.e("!!!", "onTaskRemoved");
        if (mNetworkReceiver != null) {
            unregisterReceiver(mNetworkReceiver);
            mNetworkReceiver = null;
        }
        registerRestartAlarm();
//        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        nm.cancelAll();
//        registerRestartAlarm();
//        android.os.Process.killProcess((android.os.Process.myPid()));
    }


    public void registerRestartAlarm() {
        Logger.e("!!!", "registerRestartAlarm");
        Intent intent = new Intent(this, BootReceiver.class);
        intent.setAction("ACTION.RESTART.PersistentService");
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, 0);
        long firstTime = SystemClock.elapsedRealtime();
        firstTime += 500;
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, 5 * 1000, sender);
    }

    public void unregisterRestartAlarm() {
        Logger.e("!!!", "unregisterRestartAlarm");
        Intent intent = new Intent(this, BootReceiver.class);
        intent.setAction("ACTION.RESTART.PersistentService");
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, 0);
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.cancel(sender);
    }


    //AsyncTask<param,Progress,Result>
    private class httpTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... args) {

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
            Logger.e("!!!", "httpTask result = | " + result + " |");

            if (result.contains("fail") || result.contains("another") || result.trim().equals("") || result.trim().contains("null")) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        String serial_no = Build.SERIAL;
                        serial_no = serial_no.substring(0, 4) + serial_no.substring(serial_no.length() - 4, serial_no.length());
                        String uuid = Installation.id(getApplicationContext());
                        String str = "rtcid=" + serial_no;
                        str += "&password=" + serial_no;
                        str += "&uuid=" + uuid;
                        str += "&type=" + Config.Mode;

                        new httpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "login_ouibot_do_post.php", str);
                    }
                }, 2000);
                return;
            }
            if (result.contains("success")) {
                OuiBotPreferences.setUUID(getApplicationContext(), Installation.id(getApplicationContext()));
                String serial_no = Build.SERIAL;
                serial_no = serial_no.substring(0, 4) + serial_no.substring(serial_no.length() - 4, serial_no.length());
                new httpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "ouibot_auto_login_do_post.php", "serial=" + serial_no);
                return;
            }
            if (result.contains("db")) {
                return;
            }

            try {
                JSONArray json = new JSONArray(result);

                if (json.length() > 0) {
                    for (int i = 0; i < json.length(); i++) {
                        OuiBotPreferences.setLoginId(getApplicationContext(), json.getJSONObject(i).getString("rtcid"));
                    }
                }
                connectingWebSocket();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
