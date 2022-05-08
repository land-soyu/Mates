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
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Build;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import kr.co.netseason.myclebot.API.MessageListData;
import kr.co.netseason.myclebot.Logger.Logger;
import kr.co.netseason.myclebot.Security.RecieveDataChannel;
import kr.co.netseason.myclebot.Security.SenderDataChannel;

public class Config {
    public static final int Mode = 2;   //  1: Ouibot, 2: Android
    public static final int COMPILE_Ouibot = 1;   //  1: Ouibot, 2: Android
    public static final int COMPILE_Android = 2;   //  1: Ouibot, 2: Android
    public static final boolean COMPILE_DEMO = false;
    public static boolean NONE_ACTIVITY_TEST_MODE = false;
    public static boolean SECURE_TEST_MODE = false;
    public static final int MAX_FILE_SEND_SIZE = 1024 * 1024 * 100;
    public static final short MESSAGE_CHANNEL_1 = 1;
    public static final short SECURE_IMAGE_CHANNEL_3 = 3;
    public static final short MESSAGE_FILE_CHANNEL_5 = 5;

    public static final int THREAD_MAX_TIME = 25;
    public static final int THREAD_FILE_MAX_TIME = 300;

    public static final String GCM_SEVER_API_KEY = "AIzaSyCc-_eBJzgJzts49zXKVGf_qmupv12rgNo";
    public static final String GCM_SEVER_URL = "https://android.googleapis.com/gcm/send";

    /*
        Changing this does not take effect until app user data is cleared or the app is uninstalled.
        The server address can be configured inside the application by pressing the icon in the top right.
     */
//    public static final String OUIBOT_SERVER_ADDRESS = "ws://update.ouibot.com:7450";
    public static final String OUIBOT_SERVER_ADDRESS = "ws://pc.ouibot.com:7450"; // test
//    public static final String OUIBOT_SERVER_ADDRESS = "ws://call.ouibot.com:7450"; // real

    //  3479 - DTLS NO
    public static final String STUN_SERVER = "pc.ouibot.com:3478";
    public static final String TURN_SERVER = "pc.ouibot.com:3478:gorst:hero";

    //  5349 - DTLS YES
//    public static final String STUN_SERVER = "pc.ouibot.com:5349";
//    public static final String TURN_SERVER = "pc.ouibot.com:5349:gorst:hero";

    //  7460 - DTLS NO
//    public static final String STUN_SERVER = "turn.ouibot.com:7460";
//    public static final String TURN_SERVER = "turn.ouibot.com:7460:nstrunuser:spttlwms";

    public static final String VERSION_NAME = "";
//    public static final String VERSION_NAME = "0.1.0.16";

    // Server Data URL
    public static final String Server_IP = "http://sql.ouibot.com/ouibot/";

    //  Ouibot Signal
    public static final int MODE_CALL = 1;
    public static final int MODE_CCTV = 2;
    public static final int MODE_PET = 3;
    public static final int MODE_MESSAGE = 4;
    public static final int MODE_SECURITY = 5;

    public static final int CAM_MOVE_LEFT = 6;
    public static final int CAM_MOVE_RIGHT = 7;
    public static final int CAM_MOVE_UP = 8;
    public static final int CAM_MOVE_DOWN = 9;

    public static final int MAIN_START = 10;
    public static final int CALL_START = 11;
    public static final int CALLED_NUMBER = 12;
    public static final int CHECK_CONNECTED_CALL = 13;

    public static final int WEBSEND = 86;
    public static final int START_APP = 87;
    public static final int RINGTONE_START = 88;
    public static final int SEND_SOUND_FALSE = 90;
    public static final int SEND_SOUND_TRUE = 91;
    public static final int GO_LOGINPAGE = 92;
    public static final int RINGTONE_END = 93;
    public static final int NETWORK_ERROR = 94;
    public static final int CONTACT_RELOAD = 95;
    public static final int CERTIFICATION_SEND = 96;
    public static final int CERTIFICATION_ANSWER = 97;
    public static final int RING_END = 98;
    public static final int ACTIVITY_END = 99;


    public static final int CALL_ACTIVITY_START = 101;
    public static final int CALL_OFFER_SDP_SEND = 111;
    public static final int CALL_OFFER_SDP_ACK_NOT_CONNECT = 112;
    public static final int CALL_OFFER_SDP_ACK_CALLING = 113;
    public static final int CALL_START_IOS_AWAKE = 114;
    public static final int ANSWER_SDP_SEND = 121;
    public static final int ANSWER_SDP_RECV = 122;
    public static final int ANSWER_ACK_RECV = 123;
    public static final int ANSWER_ACK_SDP_SEND = 124;
    public static final int OFFER_CANDIDATE_SEND = 131;
    public static final int ANSWER_CANDIDATE_SEND = 132;
    public static final int RECV_CANDIDATE = 141;
    public static final int HANGUP_SEND = 151;
    public static final int HANGUP_RECV = 152;
    public static final int HANGUP_ACK_RECV = 153;
    public static final int REJECT_SEND = 161;
    public static final int REJECT_RECV = 162;

    public static final int LOGOUT = 199;

    public static final int CCTV_ACTIVITY_START = 201;
    public static final int CCTV_OFFER_SDP_SEND = 211;

    public static final int PET_ACTIVITY_START = 301;
    public static final int PET_OFFER_SDP_SEND = 311;

    public static final int REQUEST_MASTER = 401;
    public static final int ALLOW_MASTER = 402;
    public static final int NOT_ALLOW_MASTER = 403;
    public static final int GET_SECURE_CONFIG = 404;
    public static final int START_SECURE_ACTIVITY = 405;
    public static final int FINISH_SECURE_ACTIVITY = 406;
    public static final int DELETE_MASTER_NOTIFY = 407;
    public static final int REQUEST_CONFIG_CHANGE = 408;
    public static final int DELETE_SLAVE_NOTIFY = 409;

    public static final int SET_CONFIG_ARK_BROADCAST = 504;
    public static final int SET_CONFIG_ARK_FAIL_BROADCAST = 505;
    public static final int SET_CONFIG_ARK_BROADCAST_WITH_NOTIFICATION = 506;

    public static final int SEND_MESSAGE_TO_WEBSOCKET = 600;

    public static final int MESSAGE_ACTIVITY_STARTED = 802;

    public static final int OVER_MAX_MASTER_COUNT = 1101;
    public static final int ALREAY_MASTER = 1102;

    public static final int GET_MASTER_ID = 1103;
    public static final int GET_SLAVE_ID = 1104;

    public static final int GET_MASTER_LIST_DATA = 1105;
    public static final int GET_SLAVE_LIST_DATA = 1106;


    public static final int MAX_BYTE_NUM = 48 * 1024;

    public static final int MAX_MASTER_REGISTER_NUM = 2;

//    public static final String JSON_STRING_FORMAT = "json:";
    public static final String VIDEO_FILE_EXTENTION = ".3gp";
    public static final String IMAGE_FILE_EXTENTION = ".jpg";
    public static final String COL_FILE_SLICE_COUNT = "col_file_slice_count";
    public static final String SECURE_LIST_ITEM_INTENT_DATA = "secure_list_item_intent_data";
    public static final String INTENT_MOVE_TO_LINK_SETTING_VIEW = "intent_move_to_link_setting_view";
    public static final String INTENT_MOVE_TO_LINK_SETTING_SECURE_MODE = "intent_move_to_link_setting_secure_mode";
    public static final String INTENT_MOVE_TO_LINK_SETTING_OUIBOT_ID = "intent_move_to_link_setting_ouibot_id";
    //    public static final String INTENT_RECEIVE_MESSAGE_REFRESH = "intent_receive_message_refresh";
    public static final String INTENT_MOVE_TO_MESSAGE_VIEW = "intent_move_to_message_view";
    public static final String PARAM_MASTER_ADD_MY_ID = "master_add_my_id";
    public static final String PARAM_MASTER_ADD_MASTER_ID = "master_add_master_id";
    public static final String INTENT_RECEIVE_MESSAGE_EVENT = "intent_receive_message_event";
    public static final String INTENT_ACTION_THREAD_STOP = "intent_action_thread_stop";
    public static final int DETECT_SECURE_MODE = 0;
    public static final int DETECT_MOVEMENT_MODE = 1;
    public static final int MESSAGE_FILE_MODE = 2;
    public static final int DETECT_MODE_DEFAULT = DETECT_SECURE_MODE;
    public static final String DETECT_MODE_KEY = "detect_mode_key";
    public static final String SAVE_DETECT_TIME = "save_detect_time";
    public static final String SAVE_NONE_ACTIVITY_TIME = "save_none_activity_time";
    public static final String DETECT_OFF = "off";
    public static final String DETECT_ON = "on";
    public static final String DETECT_ONOFF_DEFAULT = DETECT_OFF;
    public static final String DETECT_FORCE_ONOFF_DEFAULT = DETECT_OFF;
    public static final String DETECT_FORCE_ONOFF_KEY = "detect_force_onoff_key";
    public static final String DETECT_ONOFF_KEY = "detect_onoff_key";
    public static final int SENSITIVITY_DEFAULT = 0;
    public static final String SENSITIVITY_KEY = "detect_sensitivity_key";

    public static final int NONE_ACTIVITY_SENSITIVITY_DEFAULT = 0;
    public static final String NONE_ACTIVITY_SENSITIVITY_KEY = "none_activity_sensitivity_key";

    public static final int SECURE_ROUND_DATA_TYPE_TIME = 0;
    public static final int SECURE_ROUND_DATA_TYPE_STRENGTH = 1;

    public static final int VIDEO_SAVE_TIME_60 = 60;
    public static final int VIDEO_SAVE_TIME_120 = 120;
    public static final int VIDEO_SAVE_TIME_300 = 300;
    public static final int VIDEO_SAVE_TIME_10 = 10;
    public static final int VIDEO_SAVE_TIME_30 = 30;
    public static final int VIDEO_SAVE_TIME_DEFAULT = VIDEO_SAVE_TIME_10;
    public static final int[] VIDEO_SAVE_TIME_VALUE = {VIDEO_SAVE_TIME_60, VIDEO_SAVE_TIME_120, VIDEO_SAVE_TIME_300, VIDEO_SAVE_TIME_10, VIDEO_SAVE_TIME_30};
    public static final String RECORDING_TIME_KEY = "recording_time_key";

    public static final int NONE_ACTIVITY_VIDEO_SAVE_TIME_60 = 60;
    public static final int NONE_ACTIVITY_VIDEO_SAVE_TIME_120 = 120;
    public static final int NONE_ACTIVITY_VIDEO_SAVE_TIME_300 = 300;
    public static final int NONE_ACTIVITY_VIDEO_SAVE_TIME_10 = 10;
    public static final int NONE_ACTIVITY_VIDEO_SAVE_TIME_30 = 30;
    public static final int NONE_ACTIVITY_VIDEO_SAVE_TIME_DEFAULT = NONE_ACTIVITY_VIDEO_SAVE_TIME_10;
    public static final int[] NONE_ACTIVITY_VIDEO_SAVE_TIME_VALUE = {NONE_ACTIVITY_VIDEO_SAVE_TIME_60, NONE_ACTIVITY_VIDEO_SAVE_TIME_120, NONE_ACTIVITY_VIDEO_SAVE_TIME_300, NONE_ACTIVITY_VIDEO_SAVE_TIME_10, NONE_ACTIVITY_VIDEO_SAVE_TIME_30};
    public static final String NONE_ACTIVITY_RECORDING_TIME_KEY = "none_activity_recording_time_key";

    public static final int DO_AFTER_SETTING_TIME_60 = 60;
    public static final int DO_AFTER_SETTING_TIME_120 = 120;
    public static final int DO_AFTER_SETTING_TIME_300 = 300;
    public static final int DO_AFTER_SETTING_TIME_10 = 10;
    public static final int DO_AFTER_SETTING_TIME_30 = 30;
    public static final int DO_AFTER_SETTING_TIME_DEFAULT = DO_AFTER_SETTING_TIME_10;
    public static final int[] DO_AFTER_SETTING_TIME = {DO_AFTER_SETTING_TIME_60, DO_AFTER_SETTING_TIME_120, DO_AFTER_SETTING_TIME_300, DO_AFTER_SETTING_TIME_10, DO_AFTER_SETTING_TIME_30};
    public static final String SECURITY_TIME_KEY = "security_setting_time_key";

    public static final int NONE_ACTIVITY_SETTING_TIME_720 = 43200;
    public static final int NONE_ACTIVITY_SETTING_TIME_1440 = 86400;
    public static final int NONE_ACTIVITY_SETTING_TIME_2880 = 172800;
    public static final int NONE_ACTIVITY_SETTING_TIME_360 = 3600;
    public static final int NONE_ACTIVITY_SETTING_TIME_3600 = 21600;
    public static final int NONE_ACTIVITY_SETTING_TIME_DEFAULT = NONE_ACTIVITY_SETTING_TIME_3600;
    public static final int[] NONE_ACTIVITY_SETTING_TIME_TEST = {10, 60, 120, 180, 240};
    public static final int[] NONE_ACTIVITY_SETTING_TIME = {NONE_ACTIVITY_SETTING_TIME_720, NONE_ACTIVITY_SETTING_TIME_1440, NONE_ACTIVITY_SETTING_TIME_2880,NONE_ACTIVITY_SETTING_TIME_360, NONE_ACTIVITY_SETTING_TIME_3600};
    public static final String NONE_ACTIVITY_TIME_KEY = "none_activity_time_key";

    public static final int VIDEO_SAVE_MODE_OFF = 0;
    public static final int VIDEO_SAVE_MODE_ON = 1;

    public static final int VIDEO_SAVE_MODE_DEAFULT = VIDEO_SAVE_MODE_ON;
    public static final String VIDEO_SAVE_MODE_KEY = "video_save_mode_key";

    public static final int NONE_ACTIVITY_VIDEO_SAVE_MODE_DEFAULT = VIDEO_SAVE_MODE_ON;
    public static final String NONE_ACTIVITY_VIDEO_SAVE_MODE_KEY = "none_activity_recording_option_key";

    public static final int DETECTION_SENSITIVITY_0 = 0;
    public static final int DETECTION_SENSITIVITY_1 = 1;
    public static final int DETECTION_SENSITIVITY_2 = 2;
    public static final int[] DETECTION_SENSITIVITY_VALUE = {DETECTION_SENSITIVITY_0, DETECTION_SENSITIVITY_1, DETECTION_SENSITIVITY_2};
    public static final int DETECTION_SENSITIVITY_DEFAULT = DETECTION_SENSITIVITY_1;


    public static final int NONE_ACTIVITY_DETECTION_SENSITIVITY_0 = 0;
    public static final int NONE_ACTIVITY_DETECTION_SENSITIVITY_1 = 1;
    public static final int NONE_ACTIVITY_DETECTION_SENSITIVITY_2 = 2;
    public static final int NONE_ACTIVITY_DETECTION_SENSITIVITY_DEFAULT = NONE_ACTIVITY_DETECTION_SENSITIVITY_1;

    public static final int[] NONE_ACTIVITY_DETECTION_SENSITIVITY_VALUE = {NONE_ACTIVITY_DETECTION_SENSITIVITY_0, NONE_ACTIVITY_DETECTION_SENSITIVITY_1, NONE_ACTIVITY_DETECTION_SENSITIVITY_2};

    public static final String INTENT_ACTION_SECURE_OPTION_CHANGED = "intent_action_secure_option_changed";

    public static final String INTENT_ACTION_REFESH_SECURE_PAGER_DATA = "intent_action_refesh_secure_pager_data";
//    public static final String INTENT_ACTION_GET_SECURE_CONFIG_DATA = "intent_action_get_secure_config_data";
//    public static final String INTENT_ACTION_GET_SECURE_CONFIG_DATA_KEY = "intent_action_get_secure_config_data_key";

    public static final String INTENT_ACTION_SET_SECURE_CONFIG_DATA = "intent_action_set_secure_config_data";
    public static final String INTENT_ACTION_SECURE_ACTIVITY_FINISH = "com.secure.mates.ouibot.FINISH";
    public static final String INTENT_ACTION_SECURE_ACTIVITY_MOVE_TO_HOME = "INTENT_ACTION_SECURE_ACTIVITY_MOVE_TO_HOME";
    public static final String INTENT_ACTION_SET_SECURE_CONFIG_DATA_ACK = "intent_action_set_secure_config_data_ack";
    public static final String INTENT_ACTION_MICRO_SDCARD_ERROR = "intent_action_micro_sdcard_error";
    public static final String INTENT_ACTION_ALREADY_CONNECTED_PHONE_CALL = "intent_action_ALREADY_CONNECTED_PHONE_CALL";
    public static final String INTENT_ACTION_PEER_IS_NOT_LOGIN = "intent_action_peer_is_not_login";
    public static final String INTENT_ACTION_MESSAGE_TOAST = "intent_action_message_toast";
    public static final String INTENT_ACTION_MESSAGE_TOAST_MESSAGE = "intent_action_message_toast_message";

    public static final String INTENT_ACTION_SET_SECURE_CONFIG_DATA_ACK_KEY = "intent_action_set_secure_config_data_ack_key";
    public static final String MASTER_REQUEST_INTENT_MY_ID = "master_request_intent_my_id";
    public static final String MASTER_REQUEST_INTENT_MASTER_ID = "master_request_intent_master_id";
    public static final String MASTER_SETTING_RESULT = "master_setting_result";
    public static final String INTENT_DATA_JSON_KEY = "intent_data_json_key";

    public static final String GET_MASTER_AND_SLAVE_URL = "master_and_slave_list_post.php";
    public static final String DELETE_MASTER_URL = "master_delete_post.php";
    public static final String DELETE_SLAVE_URL = "slave_delete_post.php";
    public static final String EQUAL = "=";
    public static final String AND = "&";

    public static final String PARAM_DETECT_MODE = "detect_mode";
    public static final String PARAM_DETECT_SENSITIVITY = "detect_sensitivity";
    public static final String PARAM_NONE_ACTIVITY_SENSITIVITY = "none_activity_sensitivity";
    public static final String PARAM_SUCCESS = "success";
    public static final String PARAM_SUCCESS_CODE = "100";
    public static final String PARAM_WARRING_CODE = "101";
    public static final String PARAM_PEER_IS_NOT_LOGIN_CODE = "111";
    public static final String PARAM_PEER_IS_CALLING_CODE = "113";
    public static final String PARAM_SDCARD_ERROR_CODE = "161";
    public static final String PARAM_SINDEX = "sindex";
    public static final String PARAM_EINDEX = "eindex";
    public static final String PARAM_TYPE = "type";
    public static final String PARAM_SESSION_ID = "sessionid";
    public static final String PARAM_SDP = "sdp";
    public static final String PARAM_CANDIDATE = "candidate";
    public static final String PARAM_CANDIDATE_DATA_CHANNEL = "candidate_data_channel";
    public static final String PARAM_RTCID = "rtcid";
    public static final String PARAM_UUID = "uuid";
    public static final String PARAM_FROM = "from";
    public static final String PARAM_THREAD_TIME = "thread_time";
    public static final String PARAM_CONFIG = "config";
    public static final String PARAM_DETECT_ONOFF = "detect_onoff";
    public static final String PARAM_SUB_TYPE = "subtype";
    public static final String PARAM_SET_CONFIG = "setconfig";
    public static final String PARAM_CODE = "code";
    public static final String PARAM_DESCRIPTION = "description";
    public static final String PARAM_MODE = "mode";
    public static final String PARAM_ALREADY_DATA_CHANNEL_OPEN = "already_data_channel_open";
    public static final String PARAM_MASTER_RTCID = "master_rtcid";
    public static final String PARAM_SLAVE_RTCID = "slave_rtcid";
    public static final String PARAM_TO = "to";
    public static final String PARAM_RECORDING_OPTION = "recording_option";
    public static final String PARAM_NONE_ACTIVITY_RECORDING_OPTION = "none_activity_recording_option";
    public static final String PARAM_RECORDING_TIME = "recording_time";
    public static final String PARAM_NONE_ACTIVITY_RECORDING_TIME = "none_activity_recording_time";
    public static final String PARAM_GET_CONFIG = "getconfig";
    public static final String PARAM_GET_CONFIG_ACK = "getconfig_ack";
    public static final String PARAM_SET_CONFIG_ACK = "setconfig_ack";
    public static final String PARAM_MICRO_SD_EMPTY = "micro_sd_card_empty";
    public static final String PARAM_ALREADY_CONNECTED_PHONE_CALL = "already_connected_phone_call";
    public static final String PARAM_NONE_ACTIVITY_CHECK_TIME = "none_activity_check_time";
    public static final String PARAM_SECURITY_SETTING_TIME = "security_setting_time";
    public static final String PARAM_PEER_IS_NOT_LOGIN = "Peer is not login.";
    public static final String PARAM_PEER_IS_CALLING = "Peer is calling";
    public static final String DO_NOT_HAVE_MATER_ABILITY = "do_not_have_master_ability";
    public static final String END_OFFER_CANDIDATA = "end_offer_candidata";
    public static final String SDCARD_PATH = "/storage/sdcard1";
    public static final String CALL_TYPE = "0";
    public static final String SECURE_TYPE = "1";
    public static final String MESSAGE_TYPE = "2";
    public static final String FILE_MESSAGE_TYPE = "3";
    public static final String SECURE_GET_CONFIG_TYPE = "4";
    public static final String CALL_END_TYPE = "5";
    public static final int MAX_BITMAP_RESOLUTION = 960;


    public static boolean isServiceAlive(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("kr.co.netseason.myclebot.openwebrtc.SignalingChannel".equals(service.service.getClassName())) {
                Logger.e("!!!", "isServiceAlive true");
                return true;
            }
        }
        return false;
    }

    public static String getRoot() {
        File fileRoot = null;
        if (!SECURE_TEST_MODE) {
            fileRoot = new File(Config.SDCARD_PATH + File.separator + "Ouibot");

        } else {
            fileRoot = new File(Environment.getExternalStorageDirectory() + File.separator + "OuibotMe");

        }
        if (!fileRoot.exists()) {
            fileRoot.mkdir();
        }
        return fileRoot.getPath();
    }

    public static String getDirectory() {
        File file = new File(getRoot() + File.separator + getDate());
        if (!file.exists()) {
            file.mkdir();
        }
        return file.getPath() + File.separator;
    }

    public static String getSaveImageFileExternalDirectory() {
        File fileRoot = new File(Environment.getExternalStorageDirectory() + File.separator + "Ouibot");
        if (!fileRoot.exists()) {
            fileRoot.mkdir();
        }
        return fileRoot.getPath() + File.separator;
    }

    public static boolean isMountedSDcard() {
        File file = new File(Config.SDCARD_PATH);
        if (file.getTotalSpace() == 0) {
            return false;
        } else {
            return true;
        }
    }

    public final static long SIZE_KB = 1024L;
    public final static long SIZE_MB = SIZE_KB * SIZE_KB;


    public static long getSDCardAvailableSpaceInMB() {
        return getExternalAvailableSpaceInBytes() / SIZE_MB;
    }

    public static long getExternalAvailableSpaceInBytes() {
        try {
            File file = new File(Config.SDCARD_PATH);
            return file.getFreeSpace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String getDate() {
        return new SimpleDateFormat("yyyyMMdd").format(new Date(System.currentTimeMillis()));
    }

    public static byte[] readBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        long length = file.length();
        if (length > Integer.MAX_VALUE) {
            throw new IOException("Could not completely read file " + file.getName() + " as it is too long (" + length + " bytes, max supported " + Integer.MAX_VALUE + ")");
        }
        byte[] bytes = new byte[(int) length];
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }
        is.close();
        return bytes;
    }

    public static int getFileSliceNum(String path) {
        File file = new File(path);
        long totalSize = file.length();
        long sliceNum;
        if (totalSize % Config.MAX_BYTE_NUM == 0) {
            sliceNum = totalSize / Config.MAX_BYTE_NUM;
        } else if (totalSize < Config.MAX_BYTE_NUM) {
            sliceNum = 1;
        } else {
            sliceNum = totalSize / Config.MAX_BYTE_NUM + 1;
        }
        return (int) sliceNum;
    }

    public static String getFileName(String path) {
        if (path.length() >= 2)
            return path.split(File.separator)[path.split(File.separator).length - 1];
        return null;
    }

    public static boolean isPhoneId(String id) {
        if (id.startsWith("5") || id.startsWith("6") || id.startsWith("7") || id.startsWith("8") || id.startsWith("9")) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isBluetoothConnected(Context context) {
        if (context == null) {
            return false;
        }
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager.isBluetoothA2dpOn()) {
            return true;
        } else {
            return false;
        }
    }

    public static String getAndroidVersion() {
        String release = Build.VERSION.RELEASE;
        int sdkVersion = Build.VERSION.SDK_INT;
        return sdkVersion + " (" + release + ")";
    }

    public static String getPhoneNumber(Context context) {
        TelephonyManager telemamanger = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String phoneNum = telemamanger.getLine1Number();
        if (phoneNum == null) {
            phoneNum = "";
        }
        return phoneNum;
    }

    public static String getDeviceName() {
        return android.os.Build.MODEL;
    }

    public static String getAppVersionName(Context context) {
        try {
            return "v" + context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName + "." + context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (Exception e) {
            return "";
        }
    }

    public static boolean isImageFile(String path) {
        if (path.endsWith("jpeg") || path.endsWith("JPEG") || path.endsWith("png") | path.endsWith("PNG") || path.endsWith("jpg") || path.endsWith("JPG")) {
            return true;
        }
        return false;
    }

    public static boolean isVideoFile(String path) {
        if (path.endsWith("mov") || path.endsWith("MOV") || path.endsWith("AVI") | path.endsWith("avi") || path.endsWith("mp4") || path.endsWith("MP4") || path.endsWith("mkv") || path.endsWith("MKV") || path.endsWith("3gp") || path.endsWith("3GP")) {
            return true;
        }
        return false;
    }

    public static String getByteStringForSecureImage(File file) {
        return Base64.encodeToString(getBytesFromBitmap(BitmapFactory.decodeFile(file.getPath())),
                Base64.NO_WRAP);
    }

    public static String getByteStringForFileMessage(File file) {
        Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
        Bitmap resizeBitmap = resizeBitmapImageFn(bitmap, MAX_BITMAP_RESOLUTION);
        String result = Base64.encodeToString(getBytesFromBitmap(resizeBitmap),Base64.DEFAULT);
        Logger.d("Config", "base64Result length= "+result.length());
        return result;
    }

    public static Bitmap resizeBitmapImageFn(Bitmap bmpSource, int maxResolution){
        int iWidth = bmpSource.getWidth();      //비트맵이미지의 넓이
        int iHeight = bmpSource.getHeight();     //비트맵이미지의 높이
        int newWidth = iWidth ;
        int newHeight = iHeight ;
        float rate;

        //이미지의 가로 세로 비율에 맞게 조절
        if(iWidth > iHeight ){
            if(maxResolution < iWidth ){
                rate = maxResolution / (float) iWidth ;
                newHeight = (int) (iHeight * rate);
                newWidth = maxResolution;
            }
        }else{
            if(maxResolution < iHeight ){
                rate = maxResolution / (float) iHeight ;
                newWidth = (int) (iWidth * rate);
                newHeight = maxResolution;
            }
        }

        return Bitmap.createScaledBitmap(
                bmpSource, newWidth, newHeight, true);
    }
    public static byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        return stream.toByteArray();
    }

    public static int getFileType(String result) {
        if (Config.isImageFile(result)) {
            return MessageListData.IMAGE_TYPE;
        } else {
            return MessageListData.VIDEO_TYPE;
        }
    }

    public static HashMap<String, SenderDataChannel> THREAD_SENDER_CLASS = new HashMap<>();
    public static HashMap<String, RecieveDataChannel> THREAD_RECIEVER_CLASS = new HashMap<>();


}
