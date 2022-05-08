package com.matescorp.soyu.farmkinggate.util;

import android.content.SharedPreferences;

public class DataPreference {

    public static SharedPreferences PREF = null;
    public final static String TAG = "DataPreference";

    public static void setGwidx(String value) {
        if (PREF != null) {
            SharedPreferences.Editor editor = PREF.edit();
            editor.putString(Config.PREF_GWIDX_KEY, value);
            editor.commit();
        } else {
            Logger.d(TAG, "PREF == " + PREF);
        }
    }

    public static void setFilePath(String value) {
        if (PREF != null) {
            SharedPreferences.Editor editor = PREF.edit();
            editor.putString(Config.PREF_FILE_PATH_KEY, value);
            editor.commit();
        } else {
            Logger.d(TAG, "PREF == " + PREF);
        }
    }

    public static void setParkingFee(int parkingFee) {
        if (PREF != null) {
            SharedPreferences.Editor editor = PREF.edit();
            editor.putInt(Config.PREF_PARKING_FEE_KEY, parkingFee);
            editor.commit();
        } else {
            Logger.d(TAG, "PREF == " + PREF);
        }
    }

    public static void setBasicTm(int basicTm) {
        if (PREF != null) {
            SharedPreferences.Editor editor = PREF.edit();
            editor.putInt(Config.PREF_BASIC_TM_KEY, basicTm);
            editor.commit();
        } else {
            Logger.d(TAG, "PREF == " + PREF);
        }
    }

    public static void setAddFee(int addFee) {
        if (PREF != null) {
            SharedPreferences.Editor editor = PREF.edit();
            editor.putInt(Config.PREF_ADD_FEE_KEY, addFee);
            editor.commit();
        } else {
            Logger.d(TAG, "PREF == " + PREF);
        }
    }

    public static void setAddTm(int addTm) {
        if (PREF != null) {
            SharedPreferences.Editor editor = PREF.edit();
            editor.putInt(Config.PREF_ADD_TM_KEY, addTm);
            editor.commit();
        } else {
            Logger.d(TAG, "PREF == " + PREF);
        }
    }

    public static void setDayFee(int dayFee) {
        if (PREF != null) {
            SharedPreferences.Editor editor = PREF.edit();
            editor.putInt(Config.PREF_DAY_FEE_KEY, dayFee);
            editor.commit();
        } else {
            Logger.d(TAG, "PREF == " + PREF);
        }
    }

    public static void setDayMaxFeeEnable(String dayMaxFeeEnable) {
        if (PREF != null) {
            SharedPreferences.Editor editor = PREF.edit();
            if (dayMaxFeeEnable.equals(Config.DAY_MAX_FEE_ENABLE)) {
                editor.putBoolean(Config.PREF_MONTH_FEE_KEY, true);

            } else {
                editor.putBoolean(Config.PREF_MONTH_FEE_KEY, false);
            }
            editor.commit();
        } else {
            Logger.d(TAG, "PREF == " + PREF);
        }
    }

    public static void setMapZoomLevelPosition(int levelPosition) {
        if (PREF != null) {
            SharedPreferences.Editor editor = PREF.edit();
            editor.putInt(Config.PREF_ZOOM_LEVEL_POSITION_KEY, levelPosition);
            editor.commit();
        } else {
            Logger.d(TAG, "PREF == " + PREF);
        }
    }

    public static String getGwidx() {
        return PREF != null ? PREF.getString(Config.PREF_GWIDX_KEY, null) : null;
    }

    public static String getFilePath() {
        return PREF != null ? PREF.getString(Config.PREF_FILE_PATH_KEY, null) : null;
    }

    public static int getParkingFee() {
        return PREF != null ? PREF.getInt(Config.PREF_PARKING_FEE_KEY, 0) : -1;
    }

    public static int getBasicTm() {
        return PREF != null ? PREF.getInt(Config.PREF_BASIC_TM_KEY, 0) : 0;
    }

    public static int getAddFee() {
        return PREF != null ? PREF.getInt(Config.PREF_ADD_FEE_KEY, 0) : -1;
    }

    public static int getAddTm() {
        return PREF != null ? PREF.getInt(Config.PREF_ADD_TM_KEY, 0) : 0;
    }

    public static int getZoomLevelPosition() {
        return PREF != null ? PREF.getInt(Config.PREF_ZOOM_LEVEL_POSITION_KEY, 0) : 0;
    }
}
