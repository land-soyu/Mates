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

package kr.co.netseason.myclebot;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.ericsson.research.owr.Owr;
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
import kr.co.netseason.myclebot.Service.BootReceiver;
import kr.co.netseason.myclebot.UTIL.ImageSave;
import kr.co.netseason.myclebot.UTIL.OuiBotPreferences;
import kr.co.netseason.myclebot.UTIL.UIUtil;
import kr.co.netseason.myclebot.openwebrtc.Config;

public class CctvActivity extends Activity implements
        RtcSession.OnLocalCandidateListener,
        RtcSession.OnLocalDescriptionListener {
    private static final String TAG = "NativeCall";

    private static final String PREFERENCE_KEY_SERVER_URL = "url";

    private int threadTime = 400;
    private TextView call_screen_text;
    private FrameLayout video_layout;
    private LinearLayout loadpanel;

    private LinearLayout mEndButton;

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
    private JSONObject RecvCANDIDATE;
    private JSONObject SendSDP;

    private boolean CallImageView = false;
    private boolean CallButtonView = false;
    private boolean CallEndFlag = false;

    private AudioManager am;

    class CallActivityHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Config.CALL_OFFER_SDP_ACK_NOT_CONNECT:
                    video_layout.removeView(loadpanel);
                    video_layout.setVisibility(View.VISIBLE);
                    threadTime = 1000;
                    JSONObject json_peer = (JSONObject)msg.obj;
                    Logger.e("!!!", "Peer is not connect message = " + msg.obj);
                    String peer_is_not_login_message = getResources().getString(R.string.peer_is_not_login_message);
                    String end_call = getResources().getString(R.string.end_call);
                    call_screen_text.setText(peer_is_not_login_message + "\n" + end_call);

                    try {
                        String str = "rtcid=" + OuiBotPreferences.getLoginId(CctvActivity.this) +"&mode=2";
                        str += "&type=1&peer_rtcid=" + json_peer.getString(Config.PARAM_TO);
                        new httpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "recent_add_do_post.php", str);
                    }catch (Exception e) {
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
                    call_screen_text.setText(peer_is_not_login_message_calling+"\n"+end_call_calling);

                    try {
                        String str = "rtcid=" + OuiBotPreferences.getLoginId(CctvActivity.this) + "&mode=2";
                        str += "&type=1&peer_rtcid=" + json_peer_calling.getString(Config.PARAM_TO);
                        new httpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "recent_add_do_post.php", str);
                    } catch (Exception e) {
                    }
                    break;
                case Config.ANSWER_SDP_RECV:
                    Call_link_Flag = true;
                    callDelayFlag = true;
                    threadTime = 1000;

                    RecvANSWER = (JSONObject) msg.obj;
                    if (RecvANSWER.has(Config.PARAM_SDP)) {
                        try {
                            JSONObject sdp = new JSONObject();
                            sdp.put(Config.PARAM_TYPE, RecvANSWER.optString(Config.PARAM_TYPE));
                            sdp.put(Config.PARAM_SDP, RecvANSWER.optString(Config.PARAM_SDP));
                            Logger.v(TAG, "sdp: " + sdp);
                            SessionDescription sessionDescription = SessionDescriptions.fromJsep(sdp);
                            if (sessionDescription.getType() == SessionDescription.Type.OFFER) {
                                onInboundCall(sessionDescription);
                            } else {
                                onAnswer(sessionDescription);
                            }
                            call_screen_text.setText(getString(R.string.cctv_connected));
                            mEndButton.setVisibility(View.VISIBLE);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;

                case Config.RECV_CANDIDATE:
                    RecvCANDIDATE = (JSONObject) msg.obj;

                    if (RecvCANDIDATE.has(Config.PARAM_CANDIDATE)) {
                        JSONObject candidate = RecvCANDIDATE.optJSONObject(Config.PARAM_CANDIDATE);
                        Logger.v(TAG, "candidate: " + candidate);
                        RtcCandidate rtcCandidate = RtcCandidates.fromJsep(candidate);
                        if (rtcCandidate != null) {
                            mRtcSession.addRemoteCandidate(rtcCandidate);
                        } else {
                            Logger.w(TAG, "invalid candidate: " + candidate);
                        }
                    }
                    break;
                case Config.ANSWER_ACK_RECV:
                    callDelayFlag = true;
                    threadTime = 1000;
                    setLoadingView(video_layout);
                    break;

                case Config.HANGUP_RECV:
                    Logger.w("!!!", "cctv HANGUP_RECV");
                    setAudioSetting(true);
                    JSONObject json_hangup = (JSONObject) msg.obj;
                    call_screen_text.setText(R.string.cctvmode_end);
                    TextureView selfView = (TextureView) findViewById(R.id.self_view);
                    selfView.setVisibility(View.INVISIBLE);
                    TextureView remoteView = (TextureView) findViewById(R.id.remote_view);
                    remoteView.setVisibility(View.INVISIBLE);

                    try {
                        String str = "rtcid=" + OuiBotPreferences.getLoginId(CctvActivity.this) + "&mode=2";
                        if ( json_hangup.has(Config.PARAM_SUB_TYPE) ) {
                            if (json_hangup.getString(Config.PARAM_SUB_TYPE).equals("offer")) {
                                str += "&type=2&peer_rtcid=" + json_hangup.getString(Config.PARAM_FROM);
                            } else {
                                str += "&type=1&peer_rtcid=" + json_hangup.getString(Config.PARAM_TO);
                            }
                            new httpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "recent_add_do_post.php", str);
                        } else {
                            str += "&type=2&peer_rtcid=" + json_hangup.getString(Config.PARAM_FROM);
                            new httpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "recent_add_do_post.php", str);
                        }
                    } catch (Exception e) {
                    }
                    break;
                case Config.HANGUP_ACK_RECV:
                    CallEndFlag = true;
                    break;
                case Config.NETWORK_ERROR:
                    onEndClicked(null);
                    break;


                default:
                    super.handleMessage(msg);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "pause");
        sendBroadcast(i);

        Call_link_Flag = false;
        CallSendFlag = getIntent().getBooleanExtra("callSendFlag", true);
        mService = getIntent().getParcelableExtra("Messenger");
        recvMessenger = new Messenger(new CallActivityHandler());

        if ( mService != null ) {
            Message msg_main_start = Message.obtain(null, Config.CALL_START, OuiBotPreferences.getLoginId(getApplicationContext()));
            try {
                msg_main_start.replyTo = recvMessenger;
                mService.send(msg_main_start);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (Config.Mode == Config.COMPILE_Ouibot) {
            try {
                Logger.e("!!!", "CAM_MOVE_ONE..");
                Runtime runtime = Runtime.getRuntime();
                runtime.exec("/system/bin/motor_app -s 15 1 0 0");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        am.setMicrophoneMute(false);
        am.setMicrophoneMute(true);



//        if (Config.Mode == Config.COMPILE_Android) {
//        }
        if (Config.Mode == Config.COMPILE_Ouibot) {
            am.setSpeakerphoneOn(true);
//            am.stopBluetoothSco();
//            am.setMode(AudioManager.MODE_IN_CALL);
//            am.setStreamMute(AudioManager.STREAM_MUSIC, true);
//            am.setStreamMute(AudioManager.STREAM_VOICE_CALL, true);
            Logger.e("MYCLEBOT", "alex STREAM_MUSIC true ");
        }


        mRtcConfig = RtcConfigs.defaultConfig(Config.STUN_SERVER, Config.TURN_SERVER);

        setContentView(R.layout.activity_openwebrtc);
        video_layout = (FrameLayout)findViewById(R.id.video_layout);

        onJoinClicked(null);

        if (CallSendFlag) {
            loadpanel = UIUtil.createOuibotLoadingPanel(this, (int) (UIUtil.deviceWidth(this) * 0.1));
            video_layout.addView( loadpanel );

            CallSendNumber = getIntent().getStringExtra("callSendNumber");

            Logger.e("!!!", "CallSendNumber = " + CallSendNumber);
            String[] str = CallSendNumber.split("\\|");
            CallSendNumber = str[1];

//            initUi(CallSendNumber);
            initUi(str[0], CallSendNumber);
            call_screen_text.setText(getResources().getString(R.string.cctv_prepare_connection));
            mEndButton.setVisibility(View.INVISIBLE);

            TextureView selfView = (TextureView) findViewById(R.id.self_view);
            selfView.setVisibility(View.INVISIBLE);

            onCallClicked(null);

            if ( mService != null && CallSendFlag ) {
                Message ringToneStart = Message.obtain(null, Config.RINGTONE_START, null);
                try{
                    mService.send(ringToneStart);
                    Log.e("!", "ringToneStart");
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

        } else {
            try {
                RecvOffer = new JSONObject(getIntent().getStringExtra("RecvOffer"));
//                initUi(RecvOffer.getString(Config.PARAM_FROM));
                initUi("", RecvOffer.getString(Config.PARAM_FROM));
                call_screen_text.setText(getString(R.string.connection_cctv));

                FrameLayout removte_panel = (FrameLayout)findViewById(R.id.removte_panel);
                removte_panel.removeView(call_screen_text);
                removte_panel.addView(call_screen_text);

                RelativeLayout self_view_ = (RelativeLayout) findViewById(R.id.self_view_);
                self_view_.setVisibility(View.GONE);

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
                    } else {
                        onAnswer(sessionDescription);
                    }
                }


            } catch (Exception e) {
                e.printStackTrace();
            }

        }


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        );

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
                        sendMessageToService(Config.CCTV_OFFER_SDP_SEND, SendSDP);
                    } else {
                        sendMessageToService(Config.ANSWER_SDP_SEND, SendSDP);
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

    private void updateVideoView(boolean running) {
        if ( running ) {
            updateVideoView_remote(running);
            updateVideoView_self(running);
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
                mSelfView.setView(selfView, this);
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
                mRemoteView.setView(remoteView, this);
            } else {
                mRemoteView.stop();
            }
        }
    }

    public void initUi(String number) {
        setContentView(R.layout.activity_openwebrtc);
        FrameLayout video_layout = (FrameLayout) findViewById(R.id.video_layout);
        video_layout.setVisibility(View.VISIBLE);

        mEndButton = (LinearLayout) findViewById(R.id.end);
        call_screen_text = (TextView) findViewById(R.id.call_screen_text);

        TextureView self_view = (TextureView)findViewById(R.id.self_view);
        self_view.setVisibility(View.GONE);

        ImageView sendSound = (ImageView)findViewById(R.id.sendSound);
        sendSound.setVisibility(View.VISIBLE);
        sendSound.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Logger.e("!!!", "event.getAction() = " + event.getAction());
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    return false;
                }

                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        return false;
                    case MotionEvent.ACTION_DOWN:
                        if ( mService != null ) {
                            am.setMicrophoneMute(false);

                            Message msg_main_start = Message.obtain(null, Config.SEND_SOUND_FALSE, null);
                            try {
                                mService.send(msg_main_start);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        ((ImageView) v).setImageResource(R.drawable.btn_callscreen_walkietalkie_active);
                        break;
                    case MotionEvent.ACTION_UP:
                        if ( mService != null ) {
                            am.setMicrophoneMute(true);

                            Message msg_main_start = Message.obtain(null, Config.SEND_SOUND_TRUE, null);
                            try {
                                mService.send(msg_main_start);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        ((ImageView) v).setImageResource(R.drawable.btn_callscreen_walkietalkie_normal);
                        break;
                }
                return false;
            }
        });

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
            popup_title.setText(getResources().getString(R.string.connection_cctvmode));
            offer_layout_bottom.setVisibility(View.GONE);
            answer_layout_bottom.setVisibility(View.INVISIBLE);
        } else {    //offer
            offer_number.setText(OuiBotPreferences.getLoginId(getApplicationContext()));
            answer_number.setText(number);
            popup_title.setText(getResources().getString(R.string.cctvmode_connection));
            answer_layout_bottom.setVisibility(View.GONE);
        }

        mEndButton = (LinearLayout) findViewById(R.id.end);
        call_screen_text = (TextView) findViewById(R.id.call_screen_text);

        TextureView self_view = (TextureView)findViewById(R.id.self_view);
        self_view.setVisibility(View.GONE);

        ImageView sendSound = (ImageView)findViewById(R.id.sendSound);
        sendSound.setVisibility(View.VISIBLE);
        sendSound.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Logger.e("!!!", "event.getAction() = " + event.getAction());
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    return false;
                }

                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        return false;
                    case MotionEvent.ACTION_DOWN:
                        if (mService != null) {
                            am.setMicrophoneMute(false);


                            Message msg_main_start = Message.obtain(null, Config.SEND_SOUND_FALSE, null);
                            try {
                                mService.send(msg_main_start);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        ((ImageView) v).setImageResource(R.drawable.btn_callscreen_walkietalkie_active);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (mService != null) {
                            am.setMicrophoneMute(true);


                            Message msg_main_start = Message.obtain(null, Config.SEND_SOUND_TRUE, null);
                            try {
                                mService.send(msg_main_start);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        ((ImageView) v).setImageResource(R.drawable.btn_callscreen_walkietalkie_normal);
                        break;
                }
                return false;
            }
        });

        if ( CallSendFlag ) {
            FrameLayout movecam = (FrameLayout)findViewById(R.id.movecam);
            UIUtil.setCAMSIZE(this, 0.15, movecam);
            ImageView movecamimage = (ImageView)findViewById(R.id.movecamimage);
            UIUtil.setCAMSIZE(this, 0.15 * 0.75, movecamimage);

            movecam.setVisibility(View.VISIBLE);
            movecam.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    int middle = view.getWidth()/2;
                    int x = (int) motionEvent.getX();
                    int y = (int) motionEvent.getY();

                    Logger.e("!!!", "middle = " + middle);
                    Logger.e("!!!", "x = " + x);
                    Logger.e("!!!", "y = " + y);
                    if (x < middle) {
                        if (y < middle) {
                            if ((x - y) < 0) {
                                sendMessageToService(Config.CAM_MOVE_RIGHT);
                            } else if ((x - y) > 0) {
                                sendMessageToService(Config.CAM_MOVE_UP);
                            }
                            Logger.e("!!!", "x-y = " + (x - y));
                        } else if (y > middle) {
                            y = y - middle;
                            y = middle - y;
                            if ((x - y) < 0) {
                                sendMessageToService(Config.CAM_MOVE_RIGHT);
                            } else if ((x - y) > 0) {
                                sendMessageToService(Config.CAM_MOVE_DOWN);
                            }
                            Logger.e("!!!", "x-y = " + (x - y));
                        } else {
                            sendMessageToService(Config.CAM_MOVE_RIGHT);
                        }
                    } else if (x > middle) {
                        x = x - middle;
                        if (y < middle) {
                            y = middle - y;
                            if ((x - y) < 0) {
                                sendMessageToService(Config.CAM_MOVE_UP);
                            } else if ((x - y) > 0) {
                                sendMessageToService(Config.CAM_MOVE_LEFT);
                            }
                            Logger.e("!!!", "x-y = " + (x - y));
                        } else if (y > middle) {
                            y = y - middle;
                            if ((x - y) < 0) {
                                sendMessageToService(Config.CAM_MOVE_DOWN);
                            } else if ((x - y) > 0) {
                                sendMessageToService(Config.CAM_MOVE_LEFT);
                            }
                            Logger.e("!!!", "x-y = " + (x - y));
                        } else {
                            sendMessageToService(Config.CAM_MOVE_LEFT);
                       }
                    } else {
                        if (y < middle) {
                            sendMessageToService(Config.CAM_MOVE_UP);
                        } else if (y > middle) {
                            sendMessageToService(Config.CAM_MOVE_DOWN);
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
                if (Config.Mode == Config.COMPILE_Ouibot) {
                    if (Config.isMountedSDcard()) {
                        imagesave.SaveBitmapToFileCache(CctvActivity.this, mRemoteView.getView().getBitmap(), Config.getDirectory() + "screenshot/", imagesave.getPictureFileName());
                    } else {
                        Toast.makeText(CctvActivity.this, getResources().getString(R.string.check_sd_card), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    imagesave.SaveBitmapToFileCache(CctvActivity.this, mRemoteView.getView().getBitmap(), Config.getSaveImageFileExternalDirectory() + "screenshot/", imagesave.getPictureFileName());
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
                while ( flag ) {
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
                    } catch(Exception e) {
                    }


                    if ( callDelayFlag ) {
                        am.setMicrophoneMute(false);
                        am.setMicrophoneMute(true);

                        callDelayCount++;
                        Logger.e("!!!", "callDelayCount = "+callDelayCount);
                        Logger.e("!!!", "am.isMicrophoneMute() = "+am.isMicrophoneMute());
                        if ( callDelayCount > 30 ) {
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
                    if ( viewBackgroundFlag ) {
                        Logger.e("!!!", "threadTime = "+threadTime);
                        if (mRemoteView.checkVideoView() > 0) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    video_layout.removeView(loadpanel);
                                }
                            });
                            threadTime = 5000;
                            callDelayFlag = false;
                        }
                        if (threadTime == 5000) {
                            if (remoteViewTimestemp == mRemoteView.checkVideoView()) {
                                callEndCount = callEndCount + 1;
                            } else {
                                callEndCount = 0;
                            }
                            remoteViewTimestemp = mRemoteView.checkVideoView();
                        }

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
        if ( nm != null ) {
            nm.cancel(98765);
            nm = null;
        }
    }
    private void createNotification() {
        Intent intent = new Intent(this, CctvActivity.class);
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
        Logger.w("!!!!", "setAudioSetting flag = " + flag);
        am.setMicrophoneMute(flag);
        am.setStreamMute(AudioManager.STREAM_MUSIC, flag);
        am.setStreamMute(AudioManager.STREAM_SYSTEM, flag);
        am.setStreamMute(AudioManager.STREAM_VOICE_CALL, flag);
    }

    boolean onEndKeyflag = false;
    @Override
    protected void onDestroy() {
        Logger.d("!!!", "cctvActivity onDestroy");
//        if (Config.Mode == Config.COMPILE_Android) {
//
//            try {
//                Thread.sleep(1000);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            Owr.quit();
//
//            Owr.runInBackground();
//        }
        unregisterReceiver(buttonBroadcastReceiver);
        nm.cancel(98765);
        if ( !onEndKeyflag ) {
            sendMessageToService(Config.HANGUP_SEND, null);
            onActivityEnd(1000);
            onEndKeyflag = false;
        }
        super.onDestroy();
    }
    BroadcastReceiver buttonBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            nm.cancel(98765);
            Toast.makeText(context, context.getResources().getText(R.string.calling_end), Toast.LENGTH_LONG).show();
            onEndClicked(new View(context));
        }
    };

    public void onSelfViewClicked(final View view) {
        Logger.d(TAG, "onSelfViewClicked");
    }
    public void onRemoteViewClicked(final View view) {
        Logger.d(TAG, "onRemoteViewClicked");
    }

    public void onMissClicked(final View view) {
        Logger.d(TAG, "onMissClicked");

        sendMessageToService(Config.HANGUP_SEND, null);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadpanel = UIUtil.createOuibotLoadingPanel(CctvActivity.this, (int) (UIUtil.deviceWidth(CctvActivity.this) * 0.1));
                bg_landing = (FrameLayout) findViewById(R.id.bg_landing);
                bg_landing.addView(loadpanel);
            }
        });

        try {
            String str = "rtcid=" + OuiBotPreferences.getLoginId(CctvActivity.this) +"&mode=2";
            str += "&type=1&peer_rtcid=" + CallSendNumber;
            new httpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "recent_add_do_post.php", str);
        }catch (Exception e) {
        }
    }

    public void onJoinClicked(final View view) {
        Logger.d(TAG, "onJoinClicked");

//        mStreamSet = SimpleStreamSet.defaultConfig(wantAudio, wantVideo);
        mStreamSet = SimpleStreamSet.defaultConfig(true, true);
//        mSelfView = CameraSource.getInstance().createVideoView();
        mRemoteView = mStreamSet.createRemoteView();
        updateVideoView(true);
    }


    public void onCallClicked(final View view) {
        Logger.d(TAG, "onCallClicked");

        mRtcSession = RtcSessions.create(mRtcConfig);
        mRtcSession.setOnLocalCandidateListener(this);
        mRtcSession.setOnLocalDescriptionListener(this);

        mRtcSession.start(mStreamSet);
    }

    public void onEndClicked(final View view) {
        setAudioSetting(true);
        if ( view != null ) {
            try {
                String str = "rtcid=" + OuiBotPreferences.getLoginId(this) + "&mode=2&peer_rtcid=" + CallSendNumber;
                if (CallSendFlag) {
                    str += "&type=1";
                } else {
                    str += "&type=2";
                }
                new httpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "recent_add_do_post.php", str);
            } catch (Exception e) {
            }
            if ( nm != null ) {
                removeNotification();
            }
        }

        LinearLayout end = (LinearLayout)findViewById(R.id.end);
        end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onActivityEnd(10);
            }
        });

        Logger.d(TAG, "onEndClicked +++ [ " + mStreamSet.getStreamDataLink());
        switch (mStreamSet.getStreamDataLink()) {
            case 1:
                break;
            default:
                Toast.makeText(CctvActivity.this, getString(R.string.calling_not_terminated), Toast.LENGTH_SHORT).show();
                return;
        }

        if ( view == null ) {
            call_screen_text.setText(getResources().getString(R.string.communication_condition_unstable));
        } else {
            call_screen_text.setText(getResources().getString(R.string.cctvmode_end));
        }
        TextureView selfView = (TextureView) findViewById(R.id.self_view);
        selfView.setVisibility(View.INVISIBLE);
        TextureView remoteView = (TextureView) findViewById(R.id.remote_view);
        remoteView.setVisibility(View.INVISIBLE);
        if (view == null) {
            Logger.d(TAG, "onEndClicked 1 view = " + view);
            sendMessageToService(Config.HANGUP_SEND, null);
            sendCallRecent();
        }else {
            Logger.d(TAG, "onEndClicked 2 view = " + view);
            onEndKeyflag = true;
            sendMessageToService(Config.HANGUP_SEND, null);
        }
    }
    public void onEndClicked() {
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
        call_screen_text.setText(getResources().getString(R.string.communication_condition_unstable));
        TextureView selfView = (TextureView) findViewById(R.id.self_view);
        selfView.setVisibility(View.INVISIBLE);
        TextureView remoteView = (TextureView) findViewById(R.id.remote_view);
        remoteView.setVisibility(View.INVISIBLE);
        Logger.d(TAG, "onEndClicked 3");
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
                    if ( CallSendFlag ) {
                        type = 1;
                        number = CallSendNumber;
                    } else {
                        type = 2;
                        number = RecvOffer.getString(Config.PARAM_FROM);
                    }
                    String str = "rtcid=" + OuiBotPreferences.getLoginId(CctvActivity.this) + "&mode=2";
                    str += "&type="+type+"&peer_rtcid=" + number;
                    new httpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "recent_add_do_post.php", str);
                } catch (Exception e) {
                }
            }
        };
        handler.sendEmptyMessageDelayed(0, 3000);    // ms, 3초후 종료시킴
    }

    public void onActivityEnd(int sec) {
        Log.e("!!!", "onActivityEnd");
        updateVideoView(false);
        am.setMode(AudioManager.MODE_NORMAL);
        sendMessageToService(Config.ACTIVITY_END, null);

        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Logger.d(TAG, "handleMessage onEndClicked");
                mStreamSet = null;
                if ( mRtcSession != null ) {
                    mRtcSession.stopSEND();
//                    if ( CallSendFlag && !Call_link_Flag) {
//                        mRtcSession.stopSEND();
//                    } else {
//                        mRtcSession.stop();
//                    }
                    mRtcSession = null;
                }

                am.setMicrophoneMute(false);
                am.setSpeakerphoneOn(false);

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
    public void alex_finish(){
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
            JSONObject json = new JSONObject();
            json.putOpt(Config.PARAM_SDP, SessionDescriptions.toJsep(localDescription));
            Logger.d(TAG, "sending sdp: " + json);
            if ( CallImageView ) {
                if (CallSendFlag) {
                    sendMessageToService(Config.CCTV_OFFER_SDP_SEND, json);
                } else {
                    sendMessageToService(Config.ANSWER_SDP_SEND, json);
                }
            } else {
                SendSDP = json;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocalCandidate(final RtcCandidate candidate) {
        try {
            JSONObject json = new JSONObject();
            json.putOpt(Config.PARAM_CANDIDATE, RtcCandidates.toJsep(candidate));
            json.getJSONObject(Config.PARAM_CANDIDATE).put("sdpMid", "video");
            Logger.d(TAG, "saving candidate: " + json);
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
    private void sendMessageToService(int flag) {
        Message msg = Message.obtain(null, flag, flag);
        try {
            msg.replyTo = recvMessenger;
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


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
                conn.setDoInput(true);            // 입력스트림 사용여부
                conn.setDoOutput(false);            // 출력스트림 사용여부
                conn.setUseCaches(false);        // 캐시사용 여부
                conn.setReadTimeout(20000);        // 타임아웃 설정 ms단위
                conn.setConnectTimeout(3000);
//                conn.setRequestMethod("GET");  // or GET
                conn.setRequestMethod("POST");

                // POST 값 전달 하기
                StringBuffer params = new StringBuffer("");
//                params.append("name=" + URLEncoder.encode(name)); //한글일 경우 URL인코딩
                params.append(args[1]);
                PrintWriter output = new PrintWriter(conn.getOutputStream());
                output.print(params.toString());
                output.close();
                Logger.e("!!!", "result");
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
        intro = (LinearLayout)inflater.inflate(R.layout.activity_intro, null);
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
//                    am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamVolume(AudioManager.STREAM_MUSIC) - 1, AudioManager.FLAG_SHOW_UI);
                        am.setStreamVolume(AudioManager.STREAM_VOICE_CALL, am.getStreamVolume(AudioManager.STREAM_VOICE_CALL) - 1, AudioManager.FLAG_SHOW_UI);

                    }
                    break;

                case KeyEvent.KEYCODE_VOLUME_UP:
                    if (!Config.isBluetoothConnected(this)) {

//                    am.setStreamVolume(AudioManager.STREAM_MUSIC, am.getStreamVolume(AudioManager.STREAM_MUSIC) + 1, AudioManager.FLAG_SHOW_UI);
                        am.setStreamVolume(AudioManager.STREAM_VOICE_CALL, am.getStreamVolume(AudioManager.STREAM_VOICE_CALL) + 1, AudioManager.FLAG_SHOW_UI);
                    }
                    break;
            }
            return true;
        }
        return false;
    }
}
