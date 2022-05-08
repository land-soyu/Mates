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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.ericsson.research.owr.Owr;
import com.ericsson.research.owr.sdk.CameraSource;
import com.ericsson.research.owr.sdk.InvalidDescriptionException;
import com.ericsson.research.owr.sdk.RtcCandidate;
import com.ericsson.research.owr.sdk.RtcCandidates;
import com.ericsson.research.owr.sdk.RtcConfig;
import com.ericsson.research.owr.sdk.RtcConfigs;
import com.ericsson.research.owr.sdk.RtcSession;
import com.ericsson.research.owr.sdk.RtcSessions;
import com.ericsson.research.owr.sdk.SessionDescription;
import com.ericsson.research.owr.sdk.SessionDescriptions;
import com.ericsson.research.owr.sdk.SimpleStreamSet;
import com.ericsson.research.owr.sdk.VideoView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import kr.co.netseason.myclebot.Logger.Logger;
import kr.co.netseason.myclebot.MainActivity;
import kr.co.netseason.myclebot.R;
import kr.co.netseason.myclebot.Security.ForIOSNotificationHttpAsyncTask;
import kr.co.netseason.myclebot.Service.BootReceiver;
import kr.co.netseason.myclebot.UTIL.ImageSave;
import kr.co.netseason.myclebot.UTIL.OuiBotPreferences;
import kr.co.netseason.myclebot.UTIL.UIUtil;

public class CallActivity extends Activity implements
        RtcSession.OnLocalCandidateListener,
        RtcSession.OnLocalDescriptionListener {
    private static final String TAG = "NativeCall";

    private static final String PREFERENCE_KEY_SERVER_URL = "url";

    private int threadTime = 400;
    private TextView call_screen_text;
    private FrameLayout video_layout;
    private LinearLayout loadpanel;

    private RtcSession mRtcSession;
    private SimpleStreamSet mStreamSet;
    private VideoView mSelfView;
    private VideoView mRemoteView;
    private RtcConfig mRtcConfig;

    private String CallSendNumber;
    private Messenger mService;
    private Messenger recvMessenger;


    private boolean CallSendFlag = false;
    private boolean Call_link_Flag = false;
    private JSONObject RecvOffer;
    private JSONObject RecvANSWER;
    private JSONObject RecvANSWERACK;
    private JSONObject RecvCANDIDATE;
    private JSONObject SendAnswer;
    private JSONObject SendSDP;

    private boolean CallImageView = false;
    private boolean CallButtonView = false;
    private boolean CallEndFlag = false;

    class CallActivityHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Logger.e("!!!", "CallActivityHandler handleMessage receivce to service = "+msg.what);

            switch (msg.what) {
                case Config.CALL_START_IOS_AWAKE:
                    Logger.d("!!!","ios is login success");
                    sendMessageToService(Config.CALL_OFFER_SDP_SEND, jsonSdpOfferData);
                    break;
                case Config.CALL_OFFER_SDP_ACK_NOT_CONNECT:
                    video_layout.removeView(loadpanel);
                    video_layout.setVisibility(View.VISIBLE);
                    threadTime = 1000;
                    JSONObject json_peer = (JSONObject) msg.obj;
                    Logger.e("!!!", "Peer is not connect message = " + msg.obj);
                    String peer_is_not_login_message = getResources().getString(R.string.peer_is_not_login_message);
                    String end_call = getResources().getString(R.string.end_call);
                    call_screen_text.setText(peer_is_not_login_message + "\n" + end_call);

                    try {
                        String str = "rtcid=" + OuiBotPreferences.getLoginId(CallActivity.this) + "&mode=1";
                        str += "&type=1&peer_rtcid=" + json_peer.getString(Config.PARAM_TO);
                        new httpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "recent_add_do_post.php", str);
                    } catch (Exception e) {
                    }
                    send_timeCount = 60;
                    if ( nm != null ) {
                        removeNotification();
                    }
                    break;
                case Config.CALL_OFFER_SDP_ACK_CALLING:
                    video_layout.removeView(loadpanel);
                    video_layout.setVisibility(View.VISIBLE);
                    threadTime = 1000;
                    JSONObject json_peer_calling = (JSONObject) msg.obj;
                    Logger.e("!!!", "Peer is not connect message = " + msg.obj);
                    String peer_is_not_login_message_calling = getResources().getString(R.string.peer_is_calling_message);
                    String end_call_calling = getResources().getString(R.string.end_call);
                    call_screen_text.setText(peer_is_not_login_message_calling + "\n" + end_call_calling);

                    try {
                        String str = "rtcid=" + OuiBotPreferences.getLoginId(CallActivity.this) + "&mode=1";
                        str += "&type=1&peer_rtcid=" + json_peer_calling.getString(Config.PARAM_TO);
                        new httpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "recent_add_do_post.php", str);
                    } catch (Exception e) {
                    }
                    send_timeCount = 60;
                    if ( nm != null ) {
                        removeNotification();
                    }
                    break;
                case Config.ANSWER_SDP_RECV:
                    Call_link_Flag = true;

                    callDelayFlag = true;
                    threadTime = 1000;

                    RecvANSWER = (JSONObject) msg.obj;
                    try {
                        if ( !RecvANSWER.get(Config.PARAM_SUB_TYPE).toString().equals("reject") ) {
                            setLoadingView(video_layout);
                        }
                    } catch (Exception e ) { }
                    if (RecvANSWER.has(Config.PARAM_SDP)) {
                        try {
                            JSONObject sdp = new JSONObject();
                            sdp.put(Config.PARAM_TYPE, "answer");
                            sdp.put(Config.PARAM_SDP, RecvANSWER.optString(Config.PARAM_SDP));
                            Logger.v(TAG, "sdp: " + sdp);
                            SessionDescription sessionDescription = SessionDescriptions.fromJsep(sdp);
                            String connected = getResources().getString(R.string.connected);
                            String call_prepare = getResources().getString(R.string.call_prepare);
                            call_screen_text.setText(connected + "\n" + call_prepare);
                            onAnswer(sessionDescription);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    send_timeCount = 60;
                    break;


                case Config.RECV_CANDIDATE:
                    RecvCANDIDATE = (JSONObject) msg.obj;

                    if (RecvCANDIDATE.has(Config.PARAM_CANDIDATE)) {
                        JSONObject candidate = RecvCANDIDATE.optJSONObject(Config.PARAM_CANDIDATE);
                        Logger.e(TAG, "candidate: " + candidate);
                        RtcCandidate rtcCandidate = RtcCandidates.fromJsep(candidate);
                        if (rtcCandidate != null) {
                            mRtcSession.addRemoteCandidate(rtcCandidate);
                        } else {
                            Logger.e(TAG, "invalid candidate: " + candidate);
                        }
                    }
                    break;

                case Config.HANGUP_RECV:
                    JSONObject json_hangup = (JSONObject) msg.obj;
                    Logger.e("!!!", "HANGUP_RECV message = " + msg.obj);
                    call_screen_text.setText(R.string.end_call);
                    TextureView selfView = (TextureView) findViewById(R.id.self_view);
                    selfView.setVisibility(View.INVISIBLE);
                    TextureView remoteView = (TextureView) findViewById(R.id.remote_view);
                    remoteView.setVisibility(View.INVISIBLE);

                    try {
                        String str = "rtcid=" + OuiBotPreferences.getLoginId(CallActivity.this) + "&mode=1";
                        if ( json_hangup.has(Config.PARAM_SUB_TYPE) ) {
                            if (json_hangup.getString(Config.PARAM_SUB_TYPE).equals("offer")) {
                                if (Call_link_Flag) {
                                    setAudioSetting(true);

                                    str += "&type=2&peer_rtcid=" + json_hangup.getString(Config.PARAM_FROM);
                                } else {
                                    str += "&type=3&peer_rtcid=" + json_hangup.getString(Config.PARAM_FROM);

                                    //  sjkim   -   start
                                    Intent intent = new Intent("kr.co.netseason.MISS");
                                    intent.putExtra("MISS", 1);
                                    sendBroadcast(intent);
                                    Logger.d(TAG, "Broad" + intent);
                                    //  sjkim   -   end

                                    Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
                                    notificationIntent.putExtra("misscalled", 2);
                                    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                                    Notification.Builder builder = new Notification.Builder(getApplicationContext());
                                    builder.setSmallIcon(R.mipmap.ic_launcher);
                                    builder.setWhen(System.currentTimeMillis());
                                    builder.setContentTitle(getString(R.string.missed_call));
                                    builder.setContentText(json_hangup.getString(Config.PARAM_FROM));
                                    if (!Config.isBluetoothConnected(getApplicationContext())) {
                                        builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS);
                                    }
                                    builder.setContentIntent(pendingIntent);
                                    builder.setAutoCancel(true);
                                    builder.setPriority(Notification.PRIORITY_MAX);
                                    NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                    nm.notify(234567, builder.build());
                                }
                            } else {
                                setAudioSetting(true);

                                str += "&type=1&peer_rtcid=" + json_hangup.getString(Config.PARAM_TO);
                            }
                            new httpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "recent_add_do_post.php", str);
                        } else {
                            setAudioSetting(false);
                            str += "&type=3&peer_rtcid=" + json_hangup.getString(Config.PARAM_FROM);

                            //  sjkim   -   start
                            Intent intent = new Intent("kr.co.netseason.MISS");
                            intent.putExtra("MISS", 1);
                            sendBroadcast(intent);
                            Logger.d(TAG, "Broad" + intent);
                            //  sjkim   -   end

                            Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
                            notificationIntent.putExtra("misscalled", 2);
                            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                            Notification.Builder builder = new Notification.Builder(getApplicationContext());
                            builder.setSmallIcon(R.mipmap.ic_launcher);
                            builder.setWhen(System.currentTimeMillis());
                            builder.setContentTitle(getString(R.string.missed_call));
                            builder.setContentText(json_hangup.getString(Config.PARAM_FROM));
                            builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS);
                            builder.setContentIntent(pendingIntent);
                            builder.setAutoCancel(true);
                            builder.setPriority(Notification.PRIORITY_MAX);
                            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                            nm.notify(234567, builder.build());

                            new httpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "recent_add_do_post.php", str);
                        }
                    } catch (Exception e) {
                    }
                    send_timeCount = 60;
                    if ( nm != null ) {
                        removeNotification();
                    }
                    break;
                case Config.HANGUP_ACK_RECV:
                    JSONObject json_hangup_ack = (JSONObject) msg.obj;
                    Logger.e("!!!", "HANGUP_ACK_RECV message = " + msg.obj);

                    try {
                        String str = "rtcid=" + OuiBotPreferences.getLoginId(CallActivity.this) + "&mode=1";
                        if (json_hangup_ack.getString(Config.PARAM_SUB_TYPE).equals("offer")) {
                            str += "&type=1&peer_rtcid=" + json_hangup_ack.getString(Config.PARAM_TO);
                        } else {
                            if (Call_link_Flag) {
                                str += "&type=2&peer_rtcid=" + json_hangup_ack.getString(Config.PARAM_FROM);
                            } else {
                                str += "&type=3&peer_rtcid=" + json_hangup_ack.getString(Config.PARAM_FROM);

                                //  sjkim   -   start
                                Intent intent = new Intent("kr.co.netseason.MISS");
                                intent.putExtra("MISS", 1);
                                sendBroadcast(intent);
                                Logger.d(TAG, "Broad" + intent);
                                //  sjkim   -   end

                                Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
                                notificationIntent.putExtra("misscalled", 2);
                                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                                Notification.Builder builder = new Notification.Builder(getApplicationContext());
                                builder.setSmallIcon(R.mipmap.ic_launcher);
                                builder.setWhen(System.currentTimeMillis());
                                builder.setContentTitle(getString(R.string.missed_call));
                                builder.setContentText(json_hangup_ack.getString(Config.PARAM_FROM));
                                builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS);
                                builder.setContentIntent(pendingIntent);
                                builder.setAutoCancel(true);
                                builder.setPriority(Notification.PRIORITY_MAX);
                                NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                nm.notify(234567, builder.build());
                            }
                        }
                        Logger.e("!!!", "timeCount = " + timeCount);
                        new httpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "recent_add_do_post.php", str);
                    } catch (Exception e) {
                    }
                    send_timeCount = 60;
                    if ( nm != null ) {
                        removeNotification();
                    }
                    CallEndFlag = true;
                    break;

                case Config.REJECT_RECV:
                    video_layout.removeView(loadpanel);
                    video_layout.setVisibility(View.VISIBLE);
                    Call_link_Flag = false;
                    threadTime = 1000;
                    JSONObject json_reject = (JSONObject) msg.obj;
                    Logger.e("!!!", "REJECT_RECV message = " + msg.obj);
                    call_screen_text.setText(R.string.receipt_refusal);

                    try {
                        String str = "rtcid=" + OuiBotPreferences.getLoginId(CallActivity.this) + "&mode=1";
                        str += "&type=1&peer_rtcid=" + json_reject.getString(Config.PARAM_TO);
                        new httpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "recent_add_do_post.php", str);
                    } catch (Exception e) {
                    }
                    send_timeCount = 60;
                    if ( nm != null ) {
                        removeNotification();
                    }
                    break;
                case Config.NETWORK_ERROR:
                    video_layout.removeView(loadpanel);
                    video_layout.setVisibility(View.VISIBLE);
                    Logger.e("!!!", "NETWORK_ERROR receivce to service");
                    onEndClicked();
                    break;
                case Config.WEBSEND:
                    TextureView remoteview = (TextureView)findViewById(R.id.remote_view);
                    remoteview.setRotation(0.0f);
                    break;

                default:
                    super.handleMessage(msg);
            }
        }
    }

    private int timeCount = 0;
    private int send_timeCount = 0;

    private AudioManager mAudioManager;

    private ssvoiceReceiver mssvoiceReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mssvoiceReceiver == null) {
            IntentFilter subFilter = new IntentFilter();
            subFilter.addAction("signalvision.com.ouibot.voicerecognition.calling");
            mssvoiceReceiver = new ssvoiceReceiver();
            registerReceiver(mssvoiceReceiver, subFilter);
        }

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        if (Config.Mode == Config.COMPILE_Ouibot) {
            try {
                Logger.e("!!!", "CAM_MOVE_ONE..");
                Runtime runtime = Runtime.getRuntime();
                runtime.exec("/system/bin/motor_app -s 15 1 0 0");
            } catch (IOException e) {
            }
        }
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);


        if (Config.Mode == Config.COMPILE_Ouibot) {
            mAudioManager.setMicrophoneMute(true);
        }

        Call_link_Flag = false;
        mRtcConfig = RtcConfigs.defaultConfig(Config.STUN_SERVER, Config.TURN_SERVER);


        CallSendFlag = getIntent().getBooleanExtra("callSendFlag", true);
        mService = getIntent().getParcelableExtra("Messenger");
        recvMessenger = new Messenger(new CallActivityHandler());

        setContentView(R.layout.activity_openwebrtc);
        video_layout = (FrameLayout) findViewById(R.id.video_layout);

        loadpanel = UIUtil.createOuibotLoadingPanel(this, (int) (UIUtil.deviceWidth(this) * 0.1));
        video_layout.addView(loadpanel);

        onJoinClicked(null);

        if (CallSendFlag) {
            CallSendNumber = getIntent().getStringExtra("callSendNumber");

            Logger.e("!!!", "CallSendNumber = " + CallSendNumber);
            String[] str = CallSendNumber.split("\\|");
            CallSendNumber = str[1];
            Logger.e("!!!", "str[0] = " + str[0] + ", str[1] = " + str[1]);
            initUi(str[0], str[1]);

            onCallClicked(null);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    send_timeCount = 1;
                    while (send_timeCount < 60) {
                        Logger.e("!!!", "send_timeCount = " + send_timeCount);
                        handler.post(new Runnable() {
                            public void run() {
                                if (send_timeCount == 59) {
                                    popup_time.setText("");
                                    onEndClicked();
                                    new SendCallEndPushForIosTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "get_device_token_post.php", "rtcid=" + CallSendNumber);

                                } else {
                                    String waiting_connect = getResources().getString(R.string.waiting_connect);
                                    popup_time.setText(waiting_connect + " : " + send_timeCount);
                                }
                            }
                        });
                        SystemClock.sleep(1000);
                        send_timeCount++;
                    }
                }
            }).start();
        } else {
            try {
                RecvOffer = new JSONObject(getIntent().getStringExtra("RecvOffer"));
                initUi("", RecvOffer.getString(Config.PARAM_FROM));

                if (RecvOffer.has(Config.PARAM_SDP)) {
                    JSONObject sdp = new JSONObject();
                    sdp.put(Config.PARAM_TYPE, RecvOffer.optString(Config.PARAM_TYPE));
                    sdp.put(Config.PARAM_SDP, RecvOffer.optString(Config.PARAM_SDP));

                    //  soyu    20151120
                    Logger.v(TAG, "sdp: " + sdp);
                    SessionDescription sessionDescription = SessionDescriptions.fromJsep(sdp);
                    onInboundCall(sessionDescription);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            new Thread(new Runnable() {
                @Override
                public void run() {
                    timeCount = 1;
                    while (timeCount < 45) {
                        Logger.e("!!!", "timeCount = " + timeCount);
                        handler.post(new Runnable() {
                            public void run() {
                                if (timeCount == 44) {
                                    onMissClicked(null);
                                }
                                String waiting_connect = getResources().getString(R.string.waiting_connect);
                                popup_time.setText(waiting_connect + " : " + timeCount);
                            }
                        });
                        SystemClock.sleep(1000);
                        timeCount++;
                    }
                }
            }).start();
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        );

        if (mService == null) {
            if (OuiBotPreferences.getLoginId(this) != null) {
                Intent service = new Intent(this, SignalingChannel.class);
                if (!Config.isServiceAlive(this)) {
                    bindService(service, conn, Context.BIND_AUTO_CREATE);
                    startService(service);
                } else {
                    bindService(service, conn, Context.BIND_AUTO_CREATE);
                }
            }
        } else {
            Message msg_main_start = Message.obtain(null, Config.CALL_START, OuiBotPreferences.getLoginId(getApplicationContext()));
            try {
                msg_main_start.replyTo = recvMessenger;
                mService.send(msg_main_start);
                Log.e("!", "CALL_START");
            } catch (Exception e) {
                e.printStackTrace();
                alex_finish();
            }
            Message ringToneStart = Message.obtain(null, Config.RINGTONE_START, null);
            try{
                mService.send(ringToneStart);
                Log.e("!", "ringToneStart");
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("finish_call_activity");
        registerReceiver(finishBroadcastReceiver, intentFilter);
        startViewPanel();
    }

    private void startViewPanel() {
        final LinearLayout popup_panel = (LinearLayout)findViewById(R.id.popup_panel);
        AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(1500);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                popup_panel.setVisibility(View.VISIBLE);
                startViewImage();
                CallImageView = true;
                if ( CallButtonView ) {
                    startViewButton();
                }
                if ( SendSDP != null ) {
                    if (CallSendFlag) {
                        new GetDeviceTokenhttpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "get_device_token_post.php", "rtcid="+CallSendNumber);

//                        if(token != null){
//                            Logger.d(TAG, "getIphoneDeviceToken" + getIphoneDeviceToken());
//                            String title = OuiBotPreferences.getLoginId(CallActivity.this);
//                            String message = "전화가 왔습니다";
//                            new ForIOSNotificationHttpAsyncTask(token, title , message, null).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//                        }else {
//                            sendMessageToService(Config.CALL_OFFER_SDP_SEND, jsonSdpOfferData);
//                        }
                    } else {
                        call_screen_text.setText(R.string.connection_phone_call);

                        if ( Call_link_Flag ) {
                            sendMessageToService(Config.ANSWER_SDP_SEND, SendSDP);
                        } else {
                            SendAnswer = SendSDP;
                        }
                    }
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        popup_panel.startAnimation(anim);
    }

    private void startViewImage() {
        final LinearLayout popup_image = (LinearLayout)findViewById(R.id.popup_image);
        AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(1500);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                popup_image.setVisibility(View.VISIBLE);
                if (!CallSendFlag) {
                    startViewButton();
                }
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        popup_image.startAnimation(anim);
    }
    private void startViewButton() {
        final FrameLayout popup_button = (FrameLayout)findViewById(R.id.popup_button);
        AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(1500);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                popup_button.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        popup_button.startAnimation(anim);
    }


    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e("!","Ringtone_test");
            mService = new Messenger(service);
            Message msg_main_start = Message.obtain(null, Config.CALL_START, OuiBotPreferences.getLoginId(getApplicationContext()));
            try {
                msg_main_start.replyTo = recvMessenger;
                mService.send(msg_main_start);
            } catch (Exception e) {
                e.printStackTrace();
            }
            msg_main_start = Message.obtain(null, Config.CALLED_NUMBER, CallSendNumber);
            try {
                mService.send(msg_main_start);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Message ringToneStart = Message.obtain(null, Config.RINGTONE_START, null);
            try{
              mService.send(ringToneStart);
                Log.e("!", "ringToneStart");
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    private void updateVideoView(boolean running) {
        if (running) {
            updateVideoView_remote(running);
        } else {
            updateVideoView_remote(running);
            updateVideoView_self(running);
        }
    }
    private void updateVideoView_self(boolean running) {
        if (mStreamSet != null) {
            TextureView selfView = (TextureView) findViewById(R.id.self_view);
            selfView.setVisibility(running ? View.VISIBLE : View.INVISIBLE);
            if(mSelfView == null){
                return;
            }
            if (running) {
                mSelfView.setView(selfView);
                mSelfView.setMirrored(true);
            } else {
                mSelfView.stop();
            }
        }
    }
    private void updateVideoView_remote(boolean running) {
        if (mStreamSet != null) {
            TextureView remoteView = (TextureView) findViewById(R.id.remote_view);
//            remoteView.setRotation(180.0f);
            remoteView.setVisibility(running ? View.VISIBLE : View.INVISIBLE);
            if (running) {
                mRemoteView.setView(remoteView);
            } else {
                mRemoteView.stop();
            }
        }
    }

    private TextView popup_title;
    private TextView popup_time;

    public void initUi(String name, String number) {
        createProgress();
        call_screen_text = (TextView) findViewById(R.id.call_screen_text);

        popup_title = (TextView) findViewById(R.id.popup_title);
        popup_time = (TextView) findViewById(R.id.popup_time);
        FrameLayout offer_layout = (FrameLayout) findViewById(R.id.offer_layout);
        FrameLayout answer_layout = (FrameLayout) findViewById(R.id.answer_layout);

        int offernumber = Integer.parseInt(number.substring(0, 1));
        int answernumber = Integer.parseInt(OuiBotPreferences.getLoginId(getApplicationContext()).substring(0, 1));

        if (!name.equals("")) {
            offernumber = Integer.parseInt(OuiBotPreferences.getLoginId(getApplicationContext()).substring(0, 1));
            answernumber = Integer.parseInt(number.substring(0, 1));
        }
        if (offernumber < 5) {
            offer_layout.removeView((FrameLayout) findViewById(R.id.offer_phone));
            TextView offer_name = (TextView) findViewById(R.id.offer_name);
            offer_name.setText(R.string.app_name);
        } else {
            offer_layout.removeView((FrameLayout) findViewById(R.id.offer_ouibot));
            TextView offer_name = (TextView) findViewById(R.id.offer_name);
            offer_name.setText(R.string.cellphone);
        }
        if (answernumber < 5) {
            answer_layout.removeView((FrameLayout) findViewById(R.id.answer_phone));
            TextView answer_name = (TextView) findViewById(R.id.answer_name);
            if (name.equals("")) {    //answer
                answer_name.setText(R.string.app_name);
            } else {
                answer_name.setText(name);
            }
        } else {
            answer_layout.removeView((FrameLayout) findViewById(R.id.answer_ouibot));
            TextView answer_name = (TextView) findViewById(R.id.answer_name);
            if (name.equals("")) {    //answer
                answer_name.setText(R.string.cellphone);
            } else {
                answer_name.setText(name);
            }
        }

        TextView offer_number = (TextView) findViewById(R.id.offer_number);
        TextView answer_number = (TextView) findViewById(R.id.answer_number);

        LinearLayout offer_layout_bottom = (LinearLayout) findViewById(R.id.offer_layout_bottom);
        LinearLayout answer_layout_bottom = (LinearLayout) findViewById(R.id.answer_layout_bottom);
        if (name.equals("")) {    //answer
            offer_number.setText(number);
            answer_number.setText(OuiBotPreferences.getLoginId(getApplicationContext()));
            popup_title.setText(R.string.wanted_telephone);
            offer_layout_bottom.setVisibility(View.GONE);
        } else {    //offer
            offer_number.setText(OuiBotPreferences.getLoginId(getApplicationContext()));
            answer_number.setText(number);
            popup_title.setText(R.string.make_call);
            answer_layout_bottom.setVisibility(View.GONE);
        }

        if (Config.Mode == Config.COMPILE_Ouibot) {
            FrameLayout movecam = (FrameLayout) findViewById(R.id.movecam);
            UIUtil.setCAMSIZE(this, 0.15, movecam);
            ImageView movecamimage = (ImageView) findViewById(R.id.movecamimage);
            UIUtil.setCAMSIZE(this, 0.15 * 0.75, movecamimage);

            movecam.setVisibility(View.VISIBLE);
            movecam.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    int middle = view.getWidth() / 2;
                    int x = (int) motionEvent.getX();
                    int y = (int) motionEvent.getY();

                    Logger.e("!!!", "x = " + x);
                    Logger.e("!!!", "y = " + y);
                    if (x < middle) {
                        if (y < middle) {
                            if ((x - y) < 0) {
                                SignalingChannel.CAM_MOVE_LEFT();
                            } else if ((x - y) > 0) {
                                SignalingChannel.CAM_MOVE_UP();
                            }
                            Logger.e("!!!", "x-y = " + (x - y));
                        } else if (y > middle) {
                            y = y - middle;
                            y = middle - y;
                            if ((x - y) < 0) {
                                SignalingChannel.CAM_MOVE_LEFT();
                            } else if ((x - y) > 0) {
                                SignalingChannel.CAM_MOVE_DOWN();
                            }
                            Logger.e("!!!", "x-y = " + (x - y));
                        } else {
                            SignalingChannel.CAM_MOVE_LEFT();
                        }
                    } else if (x > middle) {
                        x = x - middle;
                        if (y < middle) {
                            y = middle - y;
                            if ((x - y) < 0) {
                                SignalingChannel.CAM_MOVE_UP();
                            } else if ((x - y) > 0) {
                                SignalingChannel.CAM_MOVE_RIGHT();
                            }
                            Logger.e("!!!", "x-y = " + (x - y));
                        } else if (y > middle) {
                            y = y - middle;
                            if ((x - y) < 0) {
                                SignalingChannel.CAM_MOVE_DOWN();
                            } else if ((x - y) > 0) {
                                SignalingChannel.CAM_MOVE_RIGHT();
                            }
                            Logger.e("!!!", "x-y = " + (x - y));
                        } else {
                            SignalingChannel.CAM_MOVE_RIGHT();
                        }
                    } else {
                        if (y < middle) {
                            SignalingChannel.CAM_MOVE_UP();
                        } else if (y > middle) {
                            SignalingChannel.CAM_MOVE_DOWN();
                        }
                    }
                    return false;
                }
            });
        }

        ImageView imageCapture = (ImageView)findViewById(R.id.imageCapture);
        imageCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageSave imagesave = new ImageSave();
                if ( Config.Mode == Config.COMPILE_Ouibot ) {
                    if ( Config.isMountedSDcard() ) {
                        imagesave.SaveBitmapToFileCache(CallActivity.this, mRemoteView.getView().getBitmap(), Config.getDirectory() + "screenshot/", imagesave.getPictureFileName());
                    } else {
                        Toast.makeText(CallActivity.this, getString(R.string.check_sd_card), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    imagesave.SaveBitmapToFileCache(CallActivity.this, mRemoteView.getView().getBitmap(), Config.getSaveImageFileExternalDirectory() + "screenshot/", imagesave.getPictureFileName());
                }
            }
        });
    }


    private ImageView offer_progress;
    private int progressCount = 0;
    private Handler handler = new Handler();

    private long remoteViewTimestemp = 0;
    private int callEndCount = 0;
    private int callDelayCount = 0;
    private boolean callDelayFlag = false;

    private void createProgress() {
        offer_progress = (ImageView) findViewById(R.id.offer_progress);
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean flag = true;
                while (flag) {
                    handler.post(new Runnable() {
                        public void run() {
                            progressCount++;
                            if (progressCount > 3) {
                                progressCount = 1;
                            }
                            switch (progressCount) {
                                case 1:
                                    offer_progress.setImageResource(R.drawable.img_popup_calling_progress01);
                                    break;
                                case 2:
                                    offer_progress.setImageResource(R.drawable.img_popup_calling_progress02);
                                    break;
                                case 3:
                                    offer_progress.setImageResource(R.drawable.img_popup_calling_progress03);
                                    break;
                            }
                        }
                    });
                    try {
                        Thread.sleep(threadTime);
                    } catch (Exception e) {
                    }

                    if (callDelayFlag) {
                        callDelayCount++;
                        Logger.e("!!!", "callDelayCount = " + callDelayCount);
                        if (callDelayCount > 30) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    onEndClicked();
                                }
                            });
                            flag = false;
                        }
                    }

//                    if ( mStreamSet.getStreamDataLink() == 1 ) {
                    if (viewBackgroundFlag) {
                        if (mRemoteView.checkVideoView() > 0) {
                            if (callDelayFlag) {


                                if (Config.Mode == Config.COMPILE_Ouibot) {

                                    mAudioManager.setMicrophoneMute(false);
                                    mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);

                                    Logger.e("!!!", "alex call mute off");

                                }

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        video_layout.removeView(loadpanel);

                                        if (mService != null) {
                                            Message msg_main_start = Message.obtain(null, Config.RINGTONE_END, "call");
                                            try {
                                                msg_main_start.replyTo = recvMessenger;
                                                mService.send(msg_main_start);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }

                                    }
                                });
                            }
                            callDelayFlag = false;
                            threadTime = 5000;
                        }

                        if (threadTime == 5000) {
//
                            if (remoteViewTimestemp == mRemoteView.checkVideoView()) {
                                callEndCount = callEndCount + 1;
                            } else {
                                callEndCount = 0;
                            }
                            remoteViewTimestemp = mRemoteView.checkVideoView();
                        }
//
                        if (callEndCount > 2) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    onEndClicked(null);
                                }
                            });
                            flag = false;
                        }
                    }
                }
            }
        }).start();
    }

    private boolean viewBackgroundFlag;

    @Override
    protected void onResume() {
        Logger.d("!!!", "onResume");
        super.onResume();
        viewBackgroundFlag = true;

        removeNotification();
    }

    private NotificationManager nm;
    private void removeNotification() {
        Log.d(TAG, "removeNotification");
        if ( nm != null ) {
            Log.d(TAG, "removeNotification nm.cancel 1 = ");
            nm.cancel(98765);
            Log.d(TAG, "removeNotification nm.cancel 2");
            nm = null;
        } else {
            Log.d(TAG, "removeNotification nm.cancel 3");
        }
    }
    private void createNotification() {
        Intent intent = new Intent(this, CallActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("calling");
        builder.setContentIntent(pendingIntent);

        Notification noti = builder.build();
        noti.flags |= Notification.FLAG_NO_CLEAR;
        Intent intent_ = new Intent("com.myclebot.call.end");
        pendingIntent = PendingIntent.getBroadcast(this, 0, intent_, PendingIntent.FLAG_UPDATE_CURRENT);

        Logger.e("!!!", "createNotification callDelayFlag = "+callDelayFlag);
        if ( threadTime != 5000 ) {
            RemoteViews contentiew = new RemoteViews(getPackageName(), R.layout.noti_call_layout);
            noti.contentView = contentiew;
        } else {
            RemoteViews contentiew = new RemoteViews(getPackageName(), R.layout.noti_layout);
            contentiew.setOnClickPendingIntent(R.id.mButton, pendingIntent);
            noti.contentView = contentiew;
        }

        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(98765, noti);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.myclebot.call.end");
        registerReceiver(buttonBroadcastReceiver, intentFilter);
    }
    @Override
    protected void onPause() {
        Logger.d("!!!", "onPause");
        super.onPause();
        viewBackgroundFlag = false;

        if ( CallEndFlag ) {
            onActivityEnd(2000);
        } else {
            createNotification();
        }
    }

    private void setAudioSetting(boolean flag) {
        Logger.w("!!!!", "setAudioSetting flag = "+flag);
        mAudioManager.setMicrophoneMute(flag);
        mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, flag);
        mAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, flag);
        mAudioManager.setStreamMute(AudioManager.STREAM_VOICE_CALL, flag);
    }

    boolean onEndKeyflag = false;
    @Override
    protected void onDestroy() {
        Logger.d("!!!", "callActivity onDestroy");
        super.onDestroy();
        if (Config.Mode == Config.COMPILE_Android) {
            Owr.quit();

            Owr.runInBackground();
        }
        unregisterReceiver(buttonBroadcastReceiver);
        unregisterReceiver(finishBroadcastReceiver);
        nm.cancel(98765);
        if ( !onEndKeyflag ) {
            sendMessageToService(Config.HANGUP_SEND, null);
            onActivityEnd(1000);
            onEndKeyflag = false;
        }

        if (mssvoiceReceiver != null) {
            unregisterReceiver(mssvoiceReceiver);
            mssvoiceReceiver = null;
        }
    }

    BroadcastReceiver finishBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "CallActivity.this.finish();");
                    alex_finish();
                }
            }, 2000);
        }
    };

    BroadcastReceiver buttonBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            nm.cancel(98765);
            Toast.makeText(context, context.getResources().getText(R.string.calling_end), Toast.LENGTH_LONG).show();
            onEndClicked(new View(context));
        }
    };

    public void onSelfViewClicked(final View view) {
        Logger.d(TAG, "onRemoteViewClicked");
//        if (mStreamSet != null) {
//            if (mSelfView != null) {
//                mSelfView.setRotation((mSelfView.getRotation() + 1) % 4);
//            }
//        }
//        mStreamSet.toggleCamera();
    }

    public void onRemoteViewClicked(final View view) {
        Logger.d(TAG, "onRemoteViewClicked");
    }

    public void onJoinClicked(final View view) {
        Logger.e(TAG, "onJoinClicked");

        mStreamSet = SimpleStreamSet.defaultConfig(true, true);
        mSelfView = CameraSource.getInstance().createVideoView();
        mRemoteView = mStreamSet.createRemoteView();
        updateVideoView(true);

        Logger.e(TAG, "onJoinClicked end");
    }


    public void onCallClicked(final View view) {
        Logger.d(TAG, "onCallClicked");
        timeCount = 45;

        mRtcSession = RtcSessions.create(mRtcConfig);
        mRtcSession.setOnLocalCandidateListener(this);
        mRtcSession.setOnLocalDescriptionListener(this);

        mRtcSession.start(mStreamSet);
    }

    public void onEndClicked(final View view) {
        Logger.d(TAG, "onEndClicked final View vi");
        setAudioSetting(true);

        LinearLayout end = (LinearLayout)findViewById(R.id.end);
        end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onActivityEnd(10);
            }
        });

        Logger.e("!!!", "++++++++++++++++++++++++ [ " + mStreamSet.getStreamDataLink());

        switch (mStreamSet.getStreamDataLink()) {
            case 1:
                break;
            default:
                Toast.makeText(CallActivity.this, getString(R.string.calling_not_terminated), Toast.LENGTH_SHORT).show();
                return;

        }

        if (view == null) {
            call_screen_text.setText(getString(R.string.communication_condition_unstable));
        } else {
            call_screen_text.setText(getString(R.string.end_call));
        }

        TextureView selfView = (TextureView) findViewById(R.id.self_view);
        selfView.setVisibility(View.INVISIBLE);
        TextureView remoteView = (TextureView) findViewById(R.id.remote_view);
        remoteView.setVisibility(View.INVISIBLE);
        if (view == null) {
            sendMessageToService(Config.HANGUP_SEND, null);
            sendCallRecent();
        }else {
            onEndKeyflag = true;
            sendMessageToService(Config.HANGUP_SEND, null);
        }
    }

    public void onEndClicked() {
        Logger.d(TAG, "onEndClicked");
        setAudioSetting(true);
        if ( nm != null ) {
            removeNotification();
        }
        LinearLayout end = (LinearLayout)findViewById(R.id.end);
        end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onActivityEnd(10);
            }
        });

        video_layout.removeView(loadpanel);
        call_screen_text.setText(getString(R.string.communication_condition_unstable));
        TextureView selfView = (TextureView) findViewById(R.id.self_view);
        selfView.setVisibility(View.INVISIBLE);
        TextureView remoteView = (TextureView) findViewById(R.id.remote_view);
        remoteView.setVisibility(View.INVISIBLE);
        sendMessageToService(Config.HANGUP_SEND, null);
        sendCallRecent();
    }

    private void sendCallRecent() {
        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                try {
                    int type = 0;
                    String number = "";
                    if (CallSendFlag) {
                        type = 1;
                        number = CallSendNumber;
                    } else {
                        type = 2;
                        number = RecvOffer.getString(Config.PARAM_FROM);
                    }
                    String str = "rtcid=" + OuiBotPreferences.getLoginId(CallActivity.this) + "&mode=1";
                    str += "&type=" + type + "&peer_rtcid=" + number;
                    new httpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "recent_add_do_post.php", str);
                } catch (Exception e) {
                }
            }
        };
        handler.sendEmptyMessageDelayed(0, 1000);    // ms, 3초후 종료시킴
    }

    public void onRejectClicked(final View view) {
        Logger.d(TAG, "onRejectClicked");
        timeCount = 45;

        sendMessageToService(Config.REJECT_SEND, null);

        try {
            String str = "rtcid=" + OuiBotPreferences.getLoginId(CallActivity.this) + "&mode=1";
            str += "&type=2&peer_rtcid=" + RecvOffer.getString(Config.PARAM_FROM);
            new httpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "recent_add_do_post.php", str);
        } catch (Exception e) {
        }
    }

    public void onMissClicked(final View view) {
        Logger.d(TAG, "onMissClicked");

        timeCount = 45;
        sendMessageToService(Config.HANGUP_SEND, null);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadpanel = UIUtil.createOuibotLoadingPanel(CallActivity.this, (int) (UIUtil.deviceWidth(CallActivity.this) * 0.1));
                bg_landing = (FrameLayout) findViewById(R.id.bg_landing);
                bg_landing.addView(loadpanel);
            }
        });
        new SendCallEndPushForIosTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "get_device_token_post.php", "rtcid=" + CallSendNumber);
//        try {
//            String str = "rtcid=" + OuiBotPreferences.getLoginId(CallActivity.this) +"&mode=1";
//            str += "&type=1&peer_rtcid=" + CallSendNumber;
//            new httpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "recent_add_do_post.php", str);
//        }catch (Exception e) {
//        }
    }

    public void onOKClicked(final View view) {
        Intent intent = new Intent("com.ouibot.call.flag");
        intent.putExtra("flag", 0);
        sendBroadcast(intent);

        Logger.d(TAG, "onOKClicked");
//        sendMessageToService(Config.RING_END, null);

        timeCount = 45;
        Call_link_Flag = true;

        setLoadingView(video_layout);
        callDelayFlag = true;
        threadTime = 1000;
        call_screen_text.setText(getString(R.string.call_prepare));

        //  soyu 20151120
        if ( SendAnswer != null ) {
            sendMessageToService(Config.ANSWER_SDP_SEND, SendAnswer);
        }

//        if (RecvOffer.has(Config.PARAM_SDP)) {
//            try {
//                JSONObject sdp = new JSONObject();
//                sdp.put(Config.PARAM_TYPE, "offer");
//                sdp.put(Config.PARAM_SDP, RecvOffer.optString(Config.PARAM_SDP));
//                Logger.v(TAG, "sdp: " + sdp);
//                SessionDescription sessionDescription = SessionDescriptions.fromJsep(sdp);
//                onInboundCall(sessionDescription);
//            } catch (Exception e)
//                e.printStackTrace();
//            }
//        }
    }



    public void onActivityEnd(int sec) {
        Log.e("!!!","onActivityEnd");
        timeCount = 45;
        updateVideoView(false);


        mAudioManager.setSpeakerphoneOn(false);
        mAudioManager.setMode(AudioManager.MODE_NORMAL);
        sendMessageToService(Config.ACTIVITY_END, null);

        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Logger.d("!!!", "handleMessage onEndClicked");

                mStreamSet = null;
                if (mRtcSession != null) {
                    mRtcSession.stopSEND();
//                    if (CallSendFlag && !Call_link_Flag) {
//                        mRtcSession.stopSEND();
//                    } else {
//                        mRtcSession.stop();
//                    }
                    mRtcSession = null;
                }

                setResult(200);
                alex_finish();

            }
        };
        if (Config.Mode == Config.COMPILE_Ouibot) {
            handler.sendEmptyMessageDelayed(0, 2000);    // ms, 3초후 종료시킴
        } else {
            handler.sendEmptyMessageDelayed(0, 1000);    // ms, 3초후 종료시킴
        }
    }

    public void alex_finish() {
        Logger.e("!!!", "alex_finish");
        setAudioSetting(false);
        registerRestartAlarm();
//        System.exit(0);
//        moveTaskToBack(false);
//        setResult(1000);
        finish();
//        if (Config.Mode == Config.COMPILE_Ouibot) {
        android.os.Process.killProcess((android.os.Process.myPid()));
//        }
    }
    public void registerRestartAlarm() {
        Logger.e("!!!", "registerRestartAlarm");
        Intent intent = new Intent(this, BootReceiver.class);
        intent.setAction("ACTION.RESTART.PersistentService");
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, 0);
        long firstTime = SystemClock.elapsedRealtime();
        firstTime += 500;
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, sender);
    }


    private void onInboundCall(final SessionDescription sessionDescription) {
        try {
            mRtcSession = RtcSessions.create(mRtcConfig);
            mRtcSession.setOnLocalCandidateListener(this);
            mRtcSession.setOnLocalDescriptionListener(this);

            mRtcSession.setRemoteDescription(sessionDescription);
            mRtcSession.start(mStreamSet);
        } catch (InvalidDescriptionException e) {
            e.printStackTrace();
        }
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

    private JSONObject jsonSdpOfferData;

    @Override
    public void onLocalDescription(final SessionDescription localDescription) {
        try {
            if (CallSendFlag) {
                if ( !CallImageView ) {
                    CallButtonView = true;
                } else {
                    startViewButton();
                }
            }
            jsonSdpOfferData = new JSONObject();
            jsonSdpOfferData.putOpt(Config.PARAM_SDP, SessionDescriptions.toJsep(localDescription));
            Logger.d(TAG, "sending sdp: " + jsonSdpOfferData);
            if ( CallImageView ) {
                if (CallSendFlag) {

                    new GetDeviceTokenhttpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "get_device_token_post.php", "rtcid="+CallSendNumber);
//                    String token =  getIphoneDeviceToken();
//                    if(token != null){
//                        Logger.d(TAG, "getIphoneDeviceToken" + getIphoneDeviceToken());
//                        String title = OuiBotPreferences.getLoginId(this);
//                        String message = "전화가 왔습니다";
//                        new ForIOSNotificationHttpAsyncTask(token, title , message, null).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//                    }else {
//                        sendMessageToService(Config.CALL_OFFER_SDP_SEND, jsonSdpOfferData);
//                    }
                } else {
                    call_screen_text.setText(R.string.connection_phone_call);

                    if ( Call_link_Flag ) {
                        sendMessageToService(Config.ANSWER_SDP_SEND, jsonSdpOfferData);
                    } else {
                        SendAnswer = jsonSdpOfferData;
                    }
                }
            } else {
                SendSDP = jsonSdpOfferData;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        updateVideoView_self(true);
    }

    @Override
    public void onLocalCandidate(final RtcCandidate candidate) {
        try {
            JSONObject json = new JSONObject();
            json.putOpt(Config.PARAM_CANDIDATE, RtcCandidates.toJsep(candidate));
            json.getJSONObject(Config.PARAM_CANDIDATE).put("sdpMid", "video");
            Logger.e(TAG, "[SAVE] candidate: " + json);
            if (CallSendFlag) {
                sendMessageToService(Config.OFFER_CANDIDATE_SEND, json);
            } else {
                sendMessageToService(Config.ANSWER_CANDIDATE_SEND, json);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void sendMessageToService(int flag, JSONObject jsono) {
        Message msg = Message.obtain(null, flag, jsono);
        try {
            msg.replyTo = recvMessenger;
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private class SendCallEndPushForIosTask extends android.os.AsyncTask<String, Void, String> {
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
            String token =  result;
            if(token != null && result.length() > 0){
                String title = OuiBotPreferences.getLoginId(CallActivity.this);
                String message = getResources().getString(R.string.missed_call_for_ios_push);
                new ForIOSNotificationHttpAsyncTask(token, title , message, null, false, OuiBotPreferences.getLoginId(CallActivity.this),Config.CALL_END_TYPE).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }

        }
    }

    private class GetDeviceTokenhttpTask extends android.os.AsyncTask<String, Void, String> {
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
            String token =  result;
            if(token != null && result.length() > 0){
                String title = OuiBotPreferences.getLoginId(CallActivity.this);
                String message = getResources().getString(R.string.wanted_telephone);
                new ForIOSNotificationHttpAsyncTask(token, title , message, null, true, OuiBotPreferences.getLoginId(CallActivity.this),Config.CALL_TYPE).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }else{
                sendMessageToService(Config.CALL_OFFER_SDP_SEND, jsonSdpOfferData);
            }

        }
    }

    //AsyncTask<param,Progress,Result>
    private class httpTask extends android.os.AsyncTask<String, Void, String> {
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
            if (result.contains(Config.PARAM_SUCCESS)) {
                Logger.e("!!!", "result is contains true");
            } else {
                Logger.e("!!!", "result is contains false");
            }
            onActivityEnd(2000);
        }
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }


    private LinearLayout intro;
    private FrameLayout bg_landing;

    private void setLoadingView(final FrameLayout layout) {
        bg_landing = (FrameLayout)findViewById(R.id.bg_landing);
        //  loading
        LayoutInflater inflater = getLayoutInflater();
        intro = (LinearLayout) inflater.inflate(R.layout.activity_intro, null);
        AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(1000);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) { }
            @Override
            public void onAnimationEnd(Animation animation) {
                layout.setVisibility(View.VISIBLE);
            }
            @Override
            public void onAnimationRepeat(Animation animation) { }
        });
        intro.startAnimation(anim);

        bg_landing.addView(intro);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Logger.w("!!!!", "setLoadingView runOnUiThread");
                intro.animate().setDuration(1000).setStartDelay(3000).alpha(0).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        intro.setVisibility(View.GONE);
                    }
                });
            }
        });
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ( Config.Mode == Config.COMPILE_Android ) {
            switch(keyCode)
            {
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    if (!Config.isBluetoothConnected(this)) {
    //                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) - 1, AudioManager.FLAG_SHOW_UI);
                        mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, mAudioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL) - 1, AudioManager.FLAG_SHOW_UI);
                    }
                    break;

                case KeyEvent.KEYCODE_VOLUME_UP:
                    if (!Config.isBluetoothConnected(this)) {
    //                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) + 1, AudioManager.FLAG_SHOW_UI);
                        mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, mAudioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL) + 1, AudioManager.FLAG_SHOW_UI);
                    }

                    break;
            }
            return true;
        }
        return false;
    }









    class ssvoiceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("signalvision.com.ouibot.voicerecognition.calling")) {
                Logger.e("!!!", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                Logger.e("!!!", "signalvision.com.ouibot.voicerecognition.calling");

                int num = intent.getIntExtra("number", -1);

                Logger.e("!!!", "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ "+num);
                switch (num) {
                    case 5:
                        onOKClicked(null);
                        break;

                }
            }
        }
    }

}
