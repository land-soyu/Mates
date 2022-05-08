package kr.co.netseason.myclebot.Security;

import android.content.SharedPreferences;

import kr.co.netseason.myclebot.openwebrtc.Config;

public class SecurePreference {

    public static SharedPreferences PREF = null;


    public static void saveDetecteTime(int value) {
        if (PREF != null) {
            SharedPreferences.Editor editor = PREF.edit();
            editor.putInt(Config.SAVE_DETECT_TIME, value);
            editor.commit();
        }
    }

    public static void saveNoneActivityTime(int value) {
        if (PREF != null) {
            SharedPreferences.Editor editor = PREF.edit();
            editor.putInt(Config.SAVE_NONE_ACTIVITY_TIME, value);
            editor.commit();
        }
    }

    public static void setDetectMode(int value) {
        if (PREF != null) {
            SharedPreferences.Editor editor = PREF.edit();
            editor.putInt(Config.DETECT_MODE_KEY, value);
            editor.commit();
        }
    }

    public static void setDetectForceOnOff(String value) {
        if (PREF != null) {
            SharedPreferences.Editor editor = PREF.edit();
            editor.putString(Config.DETECT_FORCE_ONOFF_KEY, value);
            editor.commit();
        }
    }

    public static void setDetectOnOff(String value) {
        if (PREF != null) {
            SharedPreferences.Editor editor = PREF.edit();
            editor.putString(Config.DETECT_ONOFF_KEY, value);
            editor.commit();
        }
    }

    public static void setDetectSensitivity(int value) {
        if (PREF != null) {
            SharedPreferences.Editor editor = PREF.edit();
            editor.putInt(Config.SENSITIVITY_KEY, value);
            editor.commit();
        }
    }

    public static void setRecordingOption(int value) {
        if (PREF != null) {
            SharedPreferences.Editor editor = PREF.edit();
            editor.putInt(Config.VIDEO_SAVE_MODE_KEY, value);
            editor.commit();
        }
    }

    public static void setRecordingTime(int value) {
        if (PREF != null) {
            SharedPreferences.Editor editor = PREF.edit();
            editor.putInt(Config.RECORDING_TIME_KEY, value);
            editor.commit();
        }
    }

    public static void setSucuritySettingTime(int value) {
        if (PREF != null) {
            SharedPreferences.Editor editor = PREF.edit();
            editor.putInt(Config.SECURITY_TIME_KEY, value);
            editor.commit();
        }
    }

    public static void setNoneActivityDetectSensitivity(int value) {
        if (PREF != null) {
            SharedPreferences.Editor editor = PREF.edit();
            editor.putInt(Config.NONE_ACTIVITY_SENSITIVITY_KEY, value);
            editor.commit();
        }
    }

    public static void setNoneActivityCheckTime(int value) {
        if (PREF != null) {
            SharedPreferences.Editor editor = PREF.edit();
            editor.putInt(Config.NONE_ACTIVITY_TIME_KEY, value);
            editor.commit();
        }
    }
    public static void setNoneActivityRecordingOption(int value) {
        if (PREF != null) {
            SharedPreferences.Editor editor = PREF.edit();
            editor.putInt(Config.NONE_ACTIVITY_VIDEO_SAVE_MODE_KEY, value);
            editor.commit();
        }
    }
    public static void setNoneActivityRecordingTime(int value) {
        if (PREF != null) {
            SharedPreferences.Editor editor = PREF.edit();
            editor.putInt(Config.NONE_ACTIVITY_RECORDING_TIME_KEY, value);
            editor.commit();
        }
    }

    public static int getDetectMode() {
        return PREF != null ? PREF.getInt(Config.DETECT_MODE_KEY, Config.DETECT_MODE_DEFAULT) : Config.DETECT_MODE_DEFAULT;
    }

    public static String getDetectForceOnOff() {
        return PREF != null ? PREF.getString(Config.DETECT_FORCE_ONOFF_KEY, Config.DETECT_FORCE_ONOFF_DEFAULT) : Config.DETECT_FORCE_ONOFF_DEFAULT;
    }

    public static String getDetectOnOff() {
        return PREF != null ? PREF.getString(Config.DETECT_ONOFF_KEY, Config.DETECT_ONOFF_DEFAULT) : Config.DETECT_ONOFF_DEFAULT;
    }

    public static int getDetectSensitivity() {
        return PREF != null ? PREF.getInt(Config.SENSITIVITY_KEY, Config.SENSITIVITY_DEFAULT) : Config.SENSITIVITY_DEFAULT;
    }

    public static int getRecordingOption() {
        return PREF != null ? PREF.getInt(Config.VIDEO_SAVE_MODE_KEY, Config.VIDEO_SAVE_MODE_DEAFULT) : Config.VIDEO_SAVE_MODE_DEAFULT;
    }

    public static int getRecordingTime() {
        return PREF != null ? PREF.getInt(Config.RECORDING_TIME_KEY, Config.VIDEO_SAVE_TIME_DEFAULT) : Config.VIDEO_SAVE_TIME_DEFAULT;
    }

    public static int getNoneActivityCheckTime() {
        return PREF != null ? PREF.getInt(Config.NONE_ACTIVITY_TIME_KEY, Config.NONE_ACTIVITY_SETTING_TIME_DEFAULT) : Config.NONE_ACTIVITY_SETTING_TIME_DEFAULT;
    }

    public static int getSecuritySettingTime() {
        return PREF != null ? PREF.getInt(Config.SECURITY_TIME_KEY, Config.DO_AFTER_SETTING_TIME_DEFAULT) : Config.DO_AFTER_SETTING_TIME_DEFAULT;
    }

    public static int getNoneActivitySensitivity() {
        return PREF != null ? PREF.getInt(Config.NONE_ACTIVITY_SENSITIVITY_KEY, Config.NONE_ACTIVITY_SENSITIVITY_DEFAULT) : Config.NONE_ACTIVITY_SENSITIVITY_DEFAULT;
    }

    public static int getNoneActivityRecordingOption() {
        return PREF != null ? PREF.getInt(Config.NONE_ACTIVITY_VIDEO_SAVE_MODE_KEY, Config.NONE_ACTIVITY_VIDEO_SAVE_MODE_DEFAULT) : Config.NONE_ACTIVITY_VIDEO_SAVE_MODE_DEFAULT;
    }

    public static int getNoneActivityRecordingTime() {
        return PREF != null ? PREF.getInt(Config.NONE_ACTIVITY_RECORDING_TIME_KEY, Config.NONE_ACTIVITY_VIDEO_SAVE_TIME_DEFAULT) : Config.NONE_ACTIVITY_VIDEO_SAVE_TIME_DEFAULT;
    }
    public static int getSavedDetecteTime() {
        return PREF != null ? PREF.getInt(Config.SAVE_DETECT_TIME, -1) : -1;
    }
    public static int getSavedNoneActivityTime() {
        return PREF != null ? PREF.getInt(Config.SAVE_NONE_ACTIVITY_TIME, -1) : -1;
    }

}
