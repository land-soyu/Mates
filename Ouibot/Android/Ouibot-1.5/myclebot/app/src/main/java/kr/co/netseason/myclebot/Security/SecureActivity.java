package kr.co.netseason.myclebot.Security;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractorMOG2;
import org.opencv.video.Video;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import kr.co.netseason.myclebot.Logger.Logger;
import kr.co.netseason.myclebot.Provider.SecureProvider;
import kr.co.netseason.myclebot.Provider.SecureSQLiteHelper;
import kr.co.netseason.myclebot.R;
import kr.co.netseason.myclebot.UTIL.OuiBotPreferences;
import kr.co.netseason.myclebot.openwebrtc.Config;
import kr.co.netseason.myclebot.openwebrtc.SignalingChannel;

public class SecureActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private JavaCameraView mOpenCVCameraView;
    private static final String TAG = Activity.class.getName();
    private BackgroundSubtractorMOG2 mBgSubtractor;
    private Mat mGray;
    private Mat mRgb;
    private Mat mFGMask;
    private List<MatOfPoint> mContours;
    private double mLearningRate = 0.5;
    public static Context INSTANCE;
    private TextView mOverlayMainText;
    private TextView mOverlaySubText;
    private int mSecureStartCount = -1;
    private int START_TIME = 0;
    private int DEFAULT_TIME = -1;
    public long SDCARD_SIZE_LIMITED = 150;
//    private int DETECT_SENSITIVITY_0 = 70; // 중
//    private int DETECT_SENSITIVITY_1 = 30; // 강
//    private int DETECT_SENSITIVITY_2 = 250; // 약

    private int DETECT_SENSITIVITY_0 = 40; // 중
    private int DETECT_SENSITIVITY_1 = 1; // 강
    private int DETECT_SENSITIVITY_2 = 80; // 약

    private int NONE_ACTIVITY_SENSITIVITY_0 = 40; // 중
    private int NONE_ACTIVITY_SENSITIVITY_1 = 1; // 강
    private int NONE_ACTIVITY_SENSITIVITY_2 = 80; // 약
    private ArrayList<Integer> mSaveContoursData;
    private Messenger mService;
    private int mDetectMode = SecurePreference.getDetectMode();
    private int mRecordingOption = SecurePreference.getRecordingOption();
    private int mRecordingTime = SecurePreference.getRecordingTime();
    private int mDetectSensitivity = SecurePreference.getDetectSensitivity();
    private int mSecuritySettingTime = SecurePreference.getSecuritySettingTime();
    private int mNoneActivityDetectSensitivity = SecurePreference.getNoneActivitySensitivity();
    private int mNoneActivityCheckTime = SecurePreference.getNoneActivityCheckTime();
    private int mNoneActivityRecordingOption = SecurePreference.getNoneActivityRecordingOption();
    private int mNoneActivityRecordingTime = SecurePreference.getNoneActivityRecordingTime();
    private int RETRY_DETECT_TIME = 10;
    private int mCurrentMode = SecurePreference.getDetectMode();
    private boolean isRecording = false;
    private TimerHandler mTimeHandler;
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    Logger.i(TAG, "OpenCV Manager Connected");
                    initCamera();
                    break;
                case LoaderCallbackInterface.INIT_FAILED:
                    Logger.i(TAG, "Init Failed");
                    break;
                case LoaderCallbackInterface.INSTALL_CANCELED:
                    Logger.i(TAG, "Install Cancelled");
                    break;
                case LoaderCallbackInterface.INCOMPATIBLE_MANAGER_VERSION:
                    Logger.i(TAG, "Incompatible Version");
                    break;
                case LoaderCallbackInterface.MARKET_ERROR:
                    Logger.i(TAG, "Market Error");
                    break;
                default:
                    Logger.i(TAG, "OpenCV Manager Install");
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    @Override
    public void onCameraViewStarted(int width, int height) {
        Logger.i(TAG, "onCameraViewStarted");
        mBgSubtractor = Video.createBackgroundSubtractorMOG2();
        mRgb = new Mat();
        mFGMask = new Mat();
        mGray = new Mat();
        mContours = new ArrayList<MatOfPoint>();
        mSaveContoursData = new ArrayList<>();
        startSecureTimer(mDetectMode);
    }

    @Override
    public void finish() {
        super.finish();
        SecurePreference.setDetectForceOnOff(Config.DETECT_OFF);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        deleteModeTime();
    }

    private void saveContoursSize(int data){
        if(mSaveContoursData != null){
            if(mSaveContoursData.size() > 5){
                mSaveContoursData.remove(0);
            }
            mSaveContoursData.add(data);
        }
    }

    private int getPreviousData(){
        if(mSaveContoursData != null){
            if(mSaveContoursData.size() > 4){
                return mSaveContoursData.get(mSaveContoursData.size()-4);
            }
        }
        return 0;
    }

    private int getPrePreviousData(){
        if(mSaveContoursData != null){
            if(mSaveContoursData.size() > 4){
                return mSaveContoursData.get(mSaveContoursData.size()-5);
            }
        }
        return 0;
    }

    @Override
    public void onCameraViewStopped() {
        Logger.i(TAG, "onCameraViewStopped");
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mContours.clear();
        mGray = inputFrame.gray();
        Imgproc.cvtColor(mGray, mRgb, Imgproc.COLOR_GRAY2RGB);
        mBgSubtractor.apply(mRgb, mFGMask, mLearningRate);
        Imgproc.erode(mFGMask, mFGMask, new Mat());
        Imgproc.dilate(mFGMask, mFGMask, new Mat());
        Imgproc.findContours(mFGMask, mContours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
//        Logger.i(TAG, "mContours.size = " + mContours.size() + " , mSecureStartCount = " + mSecureStartCount);

//        Imgproc.drawContours(mRgb, mContours, -1, new Scalar(255, 0, 0), 3);
        if(mContours.size() > 800){
            return mRgb;
        }
        if (mSecureStartCount == DEFAULT_TIME) {
            Logger.i(TAG, "idle state = " + mSecureStartCount);
            return mRgb;
        }

        if (!checkSDCardAvailable()) {
            finish();
            return mRgb;
        }
        saveContoursSize(mContours.size());
        if (getCurrentMode() == Config.DETECT_SECURE_MODE) {
            if (mSecureStartCount == START_TIME) {
                if (mContours.size() > getDetectSensitivityValue()) {
                    if(getPreviousData() > 0 && getPrePreviousData() > 0) {
                        mSecureStartCount = DEFAULT_TIME;
                        showToast(getResources().getString(R.string.found_move_detection));
                        startTextColorBlink();
                        takePicture();
                    }
                }
            }
        } else {
            if (mSecureStartCount > START_TIME) {
                if (mContours.size() > getNoneActivityDetectSensitivityValue()) {
                    if(getPreviousData() > 0 && getPrePreviousData() > 0) {
                        mSecureStartCount = mNoneActivityCheckTime;
                    }
                }
            } else {
                mSecureStartCount = DEFAULT_TIME;
                showToast(getResources().getString(R.string.no_found_move_detection));
                startTextColorBlink();
                takePicture();

            }

        }
        return mRgb;
    }

    private boolean checkSDCardAvailable() {
        if (Config.getSDCardAvailableSpaceInMB() < SDCARD_SIZE_LIMITED && !Config.SECURE_TEST_MODE) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (Config.getSDCardAvailableSpaceInMB() == 0) {
                        Toast.makeText(SecureActivity.this, getString(R.string.check_sd_card), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SecureActivity.this, getString(R.string.not_enough_to_make_video), Toast.LENGTH_SHORT).show();
                        notifyAvailableMemoryNotEnough2Masters();
                    }
                }
            });
            return false;

        }
        return true;
    }

    private synchronized void sendFile2Master(String path, int mode, String time) {
        ArrayList<UibotListData> data = getMasterList(OuiBotPreferences.getLoginId(this));
        File file = new File(path);
        if (file.exists()) {
            for (int i = 0; i < data.size(); i++) {
                String masterIds = data.get(i).getMasterRtcid();
                JSONObject json = new JSONObject();
                try {
//                    json.put(Config.COL_FILE_SLICE_COUNT, Config.getFileSliceNum(path));
//                    json.put(Config.PARAM_TO, masterIds);
//                    json.put(Config.PARAM_FROM, OuiBotPreferences.getLoginId(INSTANCE));
                    json.put(SecureSQLiteHelper.COL_FILE_PATH, Config.getFileName(path));
                    json.put(SecureSQLiteHelper.COL_MODE, mode);
                    json.put(SecureSQLiteHelper.COL_TIME, time);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                SecureImageSenderAsyncTaskWithWebSocket mDataSessionTask = new SecureImageSenderAsyncTaskWithWebSocket(this, json, file, masterIds, mService);
                mDataSessionTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                //slepp very important that is Unique Key
                try {
                    Thread.sleep(10);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

//    public ThreadPoolExecutor secureExec = new ThreadPoolExecutor(1, 999, 999, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(), new RejectedExecutionHandler() {
//        @Override
//        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
//        }
//    });

//    private synchronized void sendTestMessageForChannelOpenSpeead() {
//        ArrayList<UibotListData> data = getMasterList(OuiBotPreferences.getLoginId(this));
//        for (int i = 0; i < data.size(); i++) {
//            DataSessionSenderAsyncTask mDataSessionTask = new DataSessionSenderAsyncTask(this, 0, "0", data.get(i).getMasterRtcid(), mService, Config.THREAD_MAX_TIME, Config.MESSAGE_CHANNEL_1);
//            mDataSessionTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//            try {
//                Thread.sleep(10);
//            }catch (Exception e){
//                e.printStackTrace();
//            }
//        }
//    }


    public int getDetectSensitivityValue() {
        if (mDetectSensitivity == Config.DETECTION_SENSITIVITY_0) {
            return DETECT_SENSITIVITY_0;
        } else if (mDetectSensitivity == Config.DETECTION_SENSITIVITY_1) {
            return DETECT_SENSITIVITY_1;
        } else {
            return DETECT_SENSITIVITY_2;
        }
    }

    public int getNoneActivityDetectSensitivityValue() {
        if (mNoneActivityDetectSensitivity == Config.NONE_ACTIVITY_DETECTION_SENSITIVITY_0) {
            return NONE_ACTIVITY_SENSITIVITY_0;
        } else if (mDetectSensitivity == Config.NONE_ACTIVITY_DETECTION_SENSITIVITY_1) {
            return NONE_ACTIVITY_SENSITIVITY_1;
        } else {
            return NONE_ACTIVITY_SENSITIVITY_2;
        }
    }

    private void reStartCameraORVideoRecording() {
        if (mOpenCVCameraView == null) {
            return;
        }
        if (getCurrentMode() == Config.DETECT_SECURE_MODE) {
            if (mRecordingOption == Config.VIDEO_SAVE_MODE_ON) {
                setOverLaySubText(getResources().getString(R.string.it_is_recording_can_not_use_detect_mode));
                isRecording = true;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mOpenCVCameraView.beginRecording(getApplicationContext(), Config.getDirectory() + getVideoFileName(getCurrentTimeMilli()), mRecordingTime);
                    }
                }).start();
            } else {
                reStartSecureTimer(mDetectMode, RETRY_DETECT_TIME, -9999);
            }
        } else {
            if (mNoneActivityRecordingOption == Config.VIDEO_SAVE_MODE_ON) {
                setOverLaySubText(getResources().getString(R.string.it_is_recording_can_not_use_none_activity_mode));
                isRecording = true;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mOpenCVCameraView.beginRecording(getApplicationContext(), Config.getDirectory() + getVideoFileName(getCurrentTimeMilli()), mNoneActivityRecordingTime);
                    }
                }).start();
            } else {
                reStartSecureTimer(mDetectMode, -9999, mNoneActivityCheckTime);
            }
        }

    }

    private void takePicture() {

        if (mOpenCVCameraView == null) {
            return;
        }
        final long time = getCurrentTimeMilli();
        new Thread(new Runnable() {
            @Override
            public void run() {
                mOpenCVCameraView.pictureTaken(Config.getDirectory() + getPictureFileName(time), null, String.valueOf(time));
            }
        }).start();
    }

    private synchronized void startMediaScanner(final String path) {
        Logger.i(TAG, "startMediaScanner call = " + path);
        if (path == null) {
            return;
        }
        File file = new File(path);
        if (file == null) {
            return;
        }
        if (file.exists()) {
            new MediaScanner(getApplicationContext(), new File(path));
        }
    }

    private void setData2Database(String id, String fileName, String mode, String time) {
        try {
            ContentValues values = new ContentValues();
            values.put(SecureSQLiteHelper.COL_ID, id);
            values.put(SecureSQLiteHelper.COL_FILE_PATH, fileName);
            values.put(SecureSQLiteHelper.COL_MODE, mode);
            values.put(SecureSQLiteHelper.COL_TIME, time);
            ContentResolver resolver = getContentResolver();
            resolver.insert(SecureProvider.SECURE_MASTER_TABLE_URI, values);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JavaCameraView.OnJavaCamViewListener onMediaListner = new JavaCameraView.OnJavaCamViewListener() {
        @Override
        public void onPictureTaken(final String file, final String time, final String err) {
            if (err.equals("sdcard_error")) {
                Logger.i(TAG, "sdcard_error == ");
                finish();
                return;
            }
            if (file == null || file.equals("")) {
                Logger.i(TAG, "file == " + file);
                finish();
                return;
            }
            Logger.i(TAG, "photo file == " + file);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    setData2Database(OuiBotPreferences.getLoginId(SecureActivity.this), file, String.valueOf(getCurrentMode()), time);
                    startMediaScanner(file);
                    sendFile2Master(file, getCurrentMode(), time);
                }
            }).start();
            reStartCameraORVideoRecording();
        }

        //영상촬영 후 다시 시작하기 위함(영상 촬영중 SecureAcitivty 가 종료된 경우도 호출됨)
        //시작 카운트 다운 시 onRecordingStop이 호출 될 수 있음에 유의
        @Override
        public void onRecordingStop(final String file) {
            Logger.i(TAG, "onRecordingStop call == " + file);
            isRecording = false;
            startMediaScanner(file);
            setOverLaySubText("");
            if (SecurePreference.getDetectOnOff().equals(Config.DETECT_ON)) {
                mOpenCVCameraView.reconnectCamera();
                reStartSecureTimer(mDetectMode, RETRY_DETECT_TIME, mNoneActivityCheckTime);
            }
        }

        @Override
        public void onRecordingError() {
            Logger.i(TAG, "onRecordingError call !!!!");
            finish();
        }
    };

    private int getCurrentMode() {
        return mCurrentMode;
    }

    private void setCurrentMode(int mode) {
        mCurrentMode = mode;
    }

    private String getVideoFileName(long time) {
        Date now = new Date(time);
        SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        String formattedDate = df.format(now);
        String fileExtention = Config.VIDEO_FILE_EXTENTION;
        return formattedDate + fileExtention;
    }

    private long getCurrentTimeMilli() {
        return System.currentTimeMillis();
    }

    private String getPictureFileName(long time) {
        Date now = new Date(time);
        SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        String formattedDate = df.format(now);
        String fileExtention = Config.IMAGE_FILE_EXTENTION;
        return formattedDate + fileExtention;
    }

    private void showToast(final String data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(INSTANCE, data, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logger.d(TAG, "secureActivity oncreate call ");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_secure);
        SecurePreference.PREF = PreferenceManager.getDefaultSharedPreferences(this);
//        mDetectMode = SecurePreference.getDetectMode();
//        mRecordingOption = SecurePreference.getRecordingOption();
//        mRecordingTime = SecurePreference.getRecordingTime();
//        mDetectSensitivity = SecurePreference.getDetectSensitivity();
//        mSecuritySettingTime = SecurePreference.getSecuritySettingTime();
//        mNoneActivityDetectSensitivity = SecurePreference.getNoneActivitySensitivity();
//        mNoneActivityCheckTime = SecurePreference.getNoneActivityCheckTime();
//        mNoneActivityRecordingOption = SecurePreference.getNoneActivityRecordingOption();
//        mNoneActivityRecordingTime = SecurePreference.getNoneActivityRecordingTime();

        INSTANCE = SecureActivity.this;
        mOverlayMainText = (TextView) findViewById(R.id.overlay_main_text);
        mOverlaySubText = (TextView) findViewById(R.id.overlay_sub_text);
        setData(getIntent().getStringExtra(Config.INTENT_DATA_JSON_KEY));
        SecurePreference.setDetectForceOnOff(Config.DETECT_ON);
    }

    public void setData(String data) {
        Logger.d(TAG, "setData call" + data);
        if (data == null) {
            return;
        }
        try {
            JSONObject jsono = new JSONObject(data);
            JSONObject childJson = jsono.getJSONObject(Config.PARAM_CONFIG);
            mDetectMode = childJson.getInt(Config.PARAM_DETECT_MODE);
            mRecordingOption = childJson.getInt(Config.PARAM_RECORDING_OPTION);
            mRecordingTime = childJson.getInt(Config.PARAM_RECORDING_TIME);
            mDetectSensitivity = childJson.getInt(Config.PARAM_DETECT_SENSITIVITY);
            mSecuritySettingTime = childJson.getInt(Config.PARAM_SECURITY_SETTING_TIME);
            mNoneActivityDetectSensitivity = childJson.getInt(Config.PARAM_NONE_ACTIVITY_SENSITIVITY);
            mNoneActivityCheckTime = childJson.getInt(Config.PARAM_NONE_ACTIVITY_CHECK_TIME);
            mNoneActivityRecordingOption = childJson.getInt(Config.PARAM_NONE_ACTIVITY_RECORDING_OPTION);
            mNoneActivityRecordingTime = childJson.getInt(Config.PARAM_NONE_ACTIVITY_RECORDING_TIME);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setOverLayMainText(final String data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mOverlayMainText != null) {
                    mOverlayMainText.setText(data);
                }
            }
        });
    }

    public void startTextColorBlink() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mOverlayMainText != null) {
                    mOverlayMainText.setTextColor(Color.YELLOW);
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mOverlayMainText.setTextColor(Color.WHITE);
                        }
                    }, 1000);
                }
            }
        });
    }

    private void initCamera() {
        Logger.i(TAG, "initCamera mOpenCVCameraView = " + mOpenCVCameraView);
        if (mOpenCVCameraView == null) {
            mOpenCVCameraView = (JavaCameraView) findViewById(R.id.surface_camera);
            mOpenCVCameraView.setCvCameraViewListener(this);
            mOpenCVCameraView.setJavaCamViewListener(onMediaListner);
            mOpenCVCameraView.enableView();
        }
    }

    @Override
    protected void onResume() {
        Logger.d(TAG, "secure activity onresume call ");
        super.onResume();
        setResigterIntent();
        Intent service = new Intent(this, SignalingChannel.class);
        bindService(service, conn, Context.BIND_AUTO_CREATE);
        willUseMicSendBroadCast();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mSecureReceiver);
        SecurePreference.setDetectOnOff(Config.DETECT_OFF);
        saveModeTime();
        notifyChangeSecureData2MastersWithNotification();
        unbindService(conn);
        stopTimerHandler();
        release();
        endUseMicSendBroadCast();
    }

    public void saveModeTime() {
        SecurePreference.saveDetecteTime(mSecureStartCount);
        SecurePreference.saveNoneActivityTime(mSecureStartCount);
    }

    public void deleteModeTime() {
        SecurePreference.saveDetecteTime(DEFAULT_TIME);
        SecurePreference.saveNoneActivityTime(DEFAULT_TIME);
    }

    public int getSavedSecureTime() {
        return SecurePreference.getSavedDetecteTime();
    }

    public int getSavedNoneActivityTime() {
        return SecurePreference.getSavedNoneActivityTime();
    }

    public void notifyAvailableMemoryNotEnough2Masters() {
        ArrayList<UibotListData> masterData = getMasterList(OuiBotPreferences.getLoginId(this));
        for (int i = 0; i < masterData.size(); i++) {
            try {
                Message msg = Message.obtain(null, Config.SET_CONFIG_ARK_FAIL_BROADCAST, masterData.get(i).getMasterRtcid());
                mService.send(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void notifyChangeSecureData2MastersWithNotification() {
        ArrayList<UibotListData> masterData = getMasterList(OuiBotPreferences.getLoginId(this));
        for (int i = 0; i < masterData.size(); i++) {
            try {
                Message msg = Message.obtain(null, Config.SET_CONFIG_ARK_BROADCAST_WITH_NOTIFICATION, masterData.get(i).getMasterRtcid());
                mService.send(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
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
                c.close();
            }
        }
        return masterData;
    }


    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, final IBinder service) {
            mService = new Messenger(service);
            SecurePreference.setDetectOnOff(Config.DETECT_ON);
            notifyChangeSecureData2MastersWithNotification();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Logger.d(TAG, "start secure mode memory check = " + Config.getSDCardAvailableSpaceInMB());
                    if (!Config.SECURE_TEST_MODE) {
                        if (Config.getSDCardAvailableSpaceInMB() < SDCARD_SIZE_LIMITED) {
//                    if (Config.getSDCardAvailableSpaceInMB() > SDCARD_SIZE_LIMITED) {
                            showDeleteDataDialog();
                            notifyAvailableMemoryNotEnough2Masters();
                            return;
                        }
                    }
                    mDetectMode = SecurePreference.getDetectMode();
                    mRecordingOption = SecurePreference.getRecordingOption();
                    mRecordingTime = SecurePreference.getRecordingTime();
                    mDetectSensitivity = SecurePreference.getDetectSensitivity();
                    mSecuritySettingTime = SecurePreference.getSecuritySettingTime();
                    mNoneActivityDetectSensitivity = SecurePreference.getNoneActivitySensitivity();
                    mNoneActivityCheckTime = SecurePreference.getNoneActivityCheckTime();
                    mNoneActivityRecordingOption = SecurePreference.getNoneActivityRecordingOption();
                    mNoneActivityRecordingTime = SecurePreference.getNoneActivityRecordingTime();

                    if (!OpenCVLoader.initDebug()) {
                        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, INSTANCE, mLoaderCallback);
                    } else {
                        mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
                    }
                    Message msg_main_start = Message.obtain(null, Config.MAIN_START, OuiBotPreferences.getLoginId(getApplicationContext()));
                    try {
                        mService.send(msg_main_start);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 1000);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };
    CustomDialog mFileDeleteFailDialog;
    CustomDialog mConfirmDialog;

    private void showDeleteDataDialog() {
        if (mFileDeleteFailDialog == null) {
            mFileDeleteFailDialog = new CustomDialog(this, getResources().getString(R.string.sdcard_file_delete_popop_content), getResources().getString(R.string.popup_btn_total_delete), getResources().getString(R.string.popup_btn_select_delete), getResources().getString(R.string.popup_btn_do_later), mDialogListener, CustomDialog.THREE_BUTTON_TYPE);
        }
    }

    private void showConfirmDialog() {
        if (mConfirmDialog == null) {
            mConfirmDialog = new CustomDialog(this, getResources().getString(R.string.really_want_to_delete), "", getResources().getString(R.string.cancel), getResources().getString(R.string.confirm), mFileDeleteDialogListener, CustomDialog.TWO_BUTTON_TYPE);
        }
    }

    private CustomDialog.OnDialogListener mDialogListener = new CustomDialog.OnDialogListener() {


        @Override
        public void OnLeftClicked(View v) {
            showConfirmDialog();
        }

        @Override
        public void OnCenterClicked(View v) {
            finishFileDeleteDialog();
            Intent galleryIntent = new Intent(Intent.ACTION_VIEW, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivity(galleryIntent);
        }

        @Override
        public void OnRightClicked(View v) {
            finishFileDeleteDialog();
            finish();
        }

        @Override
        public void OnDismissListener() {
            finishConFirmPopup();

        }
    };

    private void finishConFirmPopup() {
        if (mConfirmDialog != null) {
            mConfirmDialog.cancel();
            mConfirmDialog = null;
        }
    }

    private void finishFileDeleteDialog() {
        if (mFileDeleteFailDialog != null) {
            mFileDeleteFailDialog.cancel();
            mFileDeleteFailDialog = null;
        }
    }

    private CustomDialog.OnDialogListener mFileDeleteDialogListener = new CustomDialog.OnDialogListener() {
        @Override
        public void OnLeftClicked(View v) {
        }

        @Override
        public void OnCenterClicked(View v) {
            finishFileDeleteDialog();
            finishConFirmPopup();
            finish();
        }

        @Override
        public void OnRightClicked(View v) {
            finishConFirmPopup();
            finishFileDeleteDialog();
            String rootPath = Config.SDCARD_PATH + File.separator + "Ouibot";
            deleteFiles(rootPath);
//            deletePathFilesAndStartMediaScan(rootPath);
//            sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri
//                    .parse("file://"
//                            + Environment.getExternalStorageDirectory())));
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri
                    .parse("file:///storage/sdcard1")));

            if (!OpenCVLoader.initDebug()) {
                OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, INSTANCE, mLoaderCallback);
            } else {
                mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
            }
            Message msg_main_start = Message.obtain(null, Config.MAIN_START, OuiBotPreferences.getLoginId(getApplicationContext()));
            try {
                mService.send(msg_main_start);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        @Override
        public void OnDismissListener() {
            finishFileDeleteDialog();
        }
    };

    public void deleteFiles(String path) {

        File file = new File(path);

        if (file.exists()) {
            String deleteCmd = "rm -r " + path;
            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec(deleteCmd);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void deletePathFilesAndStartMediaScan(String path){
        File file = new File(path);
        if (file.exists()) {
            File[] fileNames =  file.listFiles();
            for(int i=0; i<fileNames.length; i++){
                fileNames[i].delete();
                startMediaScanner(fileNames[i].getAbsolutePath());
            }
        }

    }

    private void setResigterIntent() {
        IntentFilter intentFilter = new IntentFilter(Config.INTENT_ACTION_SECURE_ACTIVITY_FINISH);
        intentFilter.addAction(Config.INTENT_ACTION_SET_SECURE_CONFIG_DATA);
        intentFilter.addAction(Config.INTENT_ACTION_SECURE_ACTIVITY_MOVE_TO_HOME);
        registerReceiver(mSecureReceiver, intentFilter);
    }

    private void release() {
        if (mOpenCVCameraView != null) {
            mOpenCVCameraView.stopRecording();
            mOpenCVCameraView.disableView();
            mOpenCVCameraView = null;
        }
    }

    private BroadcastReceiver mSecureReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Config.INTENT_ACTION_SET_SECURE_CONFIG_DATA)) {
                try {
                    deleteModeTime();
                    String newSettingData = intent.getStringExtra(Config.INTENT_DATA_JSON_KEY);
                    JSONObject json = new JSONObject(newSettingData);
                    JSONObject childJson = json.getJSONObject(Config.PARAM_CONFIG);
                    int detectMode = childJson.getInt(Config.PARAM_DETECT_MODE);
                    int noneActivityDetectSensitivity = childJson.getInt(Config.PARAM_NONE_ACTIVITY_SENSITIVITY);
                    int noneActivityCheckTime = childJson.getInt(Config.PARAM_NONE_ACTIVITY_CHECK_TIME);
                    int noneActivityRecordingOption = childJson.getInt(Config.PARAM_NONE_ACTIVITY_RECORDING_OPTION);
                    int noneActivityRecordingTime = childJson.getInt(Config.PARAM_NONE_ACTIVITY_RECORDING_TIME);
                    if (mDetectMode != detectMode) {
                        setData(intent.getStringExtra(Config.INTENT_DATA_JSON_KEY));
                        if(!isRecording) {
                            startSecureTimer(mDetectMode);
                        }
                    }else {
                        if (mNoneActivityDetectSensitivity == noneActivityDetectSensitivity && mNoneActivityCheckTime == noneActivityCheckTime && mNoneActivityRecordingOption == noneActivityRecordingOption && mNoneActivityRecordingTime == noneActivityRecordingTime) {
                            if (mDetectMode == Config.DETECT_SECURE_MODE) {
                                setData(intent.getStringExtra(Config.INTENT_DATA_JSON_KEY));
                                if(!isRecording) {
                                    startSecureTimer(mDetectMode);
                                }
                            }
                        } else {
                            if (mDetectMode != Config.DETECT_SECURE_MODE) {
                                setData(intent.getStringExtra(Config.INTENT_DATA_JSON_KEY));
                                if(!isRecording) {
                                    startSecureTimer(mDetectMode);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (intent.getAction().equals(Config.INTENT_ACTION_SECURE_ACTIVITY_FINISH)) {
                finish();
            } else if(intent.getAction().equals(Config.INTENT_ACTION_SECURE_ACTIVITY_MOVE_TO_HOME)){
//                Intent i = new Intent();
//                i.setAction(Intent.ACTION_MAIN);
//                i.addCategory(Intent.CATEGORY_HOME);
//                startActivity(i);
//                finish();
            }
        }
    };

    private void stopTimerHandler() {
        setOverLaySubText("");
        if (mTimeHandler != null) {
            mTimeHandler.removeCallbacksAndMessages(null);
            mTimeHandler = null;
        }
    }

    private void startSecureTimer(final int mode) {
        setCurrentMode(mode);
        if (mode == Config.DETECT_SECURE_MODE) {
            setOverLayMainText(getResources().getString(R.string.secure_mode_text));
            if (getSavedSecureTime() == DEFAULT_TIME) {
                mSecureStartCount = mSecuritySettingTime;
            } else {
                mSecureStartCount = getSavedSecureTime();
            }
        } else {
            setOverLayMainText(getResources().getString(R.string.movement_mode_text));
            if (getSavedNoneActivityTime() == DEFAULT_TIME) {
                mSecureStartCount = mNoneActivityCheckTime;
            } else {
                mSecureStartCount = getSavedNoneActivityTime();
            }
        }
        if (mSecureStartCount <= START_TIME) {
            return;
        }
        stopTimerHandler();
        startTimerHandler();
    }

    private void willUseMicSendBroadCast(){
        Intent intent = new Intent("com.ouibot.call.flag");
        intent.putExtra("flag", 0);
        sendBroadcast(intent);
    }

    private void endUseMicSendBroadCast(){
        Intent intent = new Intent("com.ouibot.call.flag");
        intent.putExtra("flag", 1);
        sendBroadcast(intent);
    }

    private void reStartSecureTimer(final int mode, final int secureTime, final int noneActivityTime) {
        setCurrentMode(mode);
        if (mode == Config.DETECT_SECURE_MODE) {
            setOverLayMainText(getResources().getString(R.string.secure_mode_text));
            mSecureStartCount = secureTime;
        } else {
            setOverLayMainText(getResources().getString(R.string.movement_mode_text));
            mSecureStartCount = noneActivityTime;
        }
        if (mSecureStartCount <= START_TIME) {
            return;
        }
        stopTimerHandler();
        startTimerHandler();
    }

    private void startTimerHandler() {
        if (mTimeHandler == null) {
            mTimeHandler = new TimerHandler();
        }
        mTimeHandler.sendMessageDelayed(new Message(), 1000);

    }

    public class TimerHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            setOverLaySubText(mSecureStartCount, getCurrentMode());
            if (mSecureStartCount <= START_TIME) {
                stopTimerHandler();
                return;
            }
            mSecureStartCount--;
            startTimerHandler();
            super.handleMessage(msg);
        }

    }

    public void setOverLaySubText(final int data, final int mode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isRecording) {
                    if (mOverlaySubText != null) {
                        if (mode == Config.DETECT_SECURE_MODE) {
                            mOverlaySubText.setText(setTimeStringFormat(data) + getResources().getString(R.string.start_detection_mode_with_second));
                        } else {
                            mOverlaySubText.setText(setTimeStringFormat(data) + getResources().getString(R.string.start_none_activity_mode_with_second));
                        }
                    }
                }
            }
        });
    }

    public void setOverLaySubText(final String data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isRecording) {
                    if (mOverlaySubText != null) {
                        mOverlaySubText.setText(data);
                    }
                }
            }
        });
    }

    public String setTimeStringFormat(int miliSecond) {
        String timeString = "";
        String minuteString = "";
        String secondString = "";
        int ours = miliSecond / 3600;
        int minutes = (miliSecond % 3600) / 60;
        int seconds = miliSecond % 60;
        if (ours > 0) {
            timeString = String.valueOf(ours) + getString(R.string.hour) + " ";
        }
        if (minutes > 0) {
            minuteString = String.valueOf(minutes) + getString(R.string.minute) + " ";
        }
        if (seconds > 0) {
            secondString = String.valueOf(seconds) + getString(R.string.second) + " ";
        }
        return timeString + minuteString + secondString;
    }
}