package com.matescorp.soyu.farmkingapp.util;

import android.content.SharedPreferences;
import android.util.Log;

public class DataPreference {

    public static SharedPreferences PREF = null;
    public final static String TAG = "DataPreference";

    public static void setAutoLogin(boolean b) {
        if (PREF != null) {
            SharedPreferences.Editor editor = PREF.edit();
            editor.putBoolean("autologin", b);
            editor.commit();
        } else {
            Log.d(TAG, "PREF == " + PREF);
        }
    }

    public static void setId(String value) {
        if (PREF != null) {
            SharedPreferences.Editor editor = PREF.edit();
            editor.putString("id", value);
            editor.commit();
        } else {
            Log.d(TAG, "PREF == " + PREF);
        }
    }

    public static void setPwd(String value) {
        if (PREF != null) {
            SharedPreferences.Editor editor = PREF.edit();
            editor.putString("pwd", value);
            editor.commit();
        } else {
            Log.d(TAG, "PREF == " + PREF);
        }
    }

    public static void setSerial(String value) {
        if (PREF != null) {
            SharedPreferences.Editor editor = PREF.edit();
            editor.putString("serial", value);
            editor.commit();
        } else {
            Log.d(TAG, "PREF == " + PREF);
        }
    }
    public static void setFarmName(String value) {
        if (PREF != null) {
            SharedPreferences.Editor editor = PREF.edit();
            editor.putString("farmname", value);
            editor.commit();
        } else {
            Log.d(TAG, "PREF == " + PREF);
        }
    }
    public static void setName(String value) {
        if (PREF != null) {
            SharedPreferences.Editor editor = PREF.edit();
            editor.putString("name", value);
            editor.commit();
        } else {
            Log.d(TAG, "PREF == " + PREF);
        }
    }
    public static void setPhone(String value) {
        if (PREF != null) {
            SharedPreferences.Editor editor = PREF.edit();
            editor.putString("phone", value);
            editor.commit();
        } else {
            Log.d(TAG, "PREF == " + PREF);
        }
    }
    public static void setAddr(String value) {
        if (PREF != null) {
            SharedPreferences.Editor editor = PREF.edit();
            editor.putString("addr", value);
            editor.commit();
        } else {
            Log.d(TAG, "PREF == " + PREF);
        }
    }

    public static void setToken(String value) {
        if (PREF != null) {
            SharedPreferences.Editor editor = PREF.edit();
            editor.putString("token", value);
            editor.commit();
        } else {
            Log.d(TAG, "TOKEN == " + PREF);
        }
    }

    public static boolean getAutoLogin() {
        return PREF != null ? PREF.getBoolean("autologin", false) : false;
    }

    public static String getId() {
        return PREF != null ? PREF.getString("id", null) : null;
    }

    public static String getPwd() {
        return PREF != null ? PREF.getString("pwd", null) : null;
    }

    public static String getSerial() {
        return PREF != null ? PREF.getString("serial", null) : null;
    }

    public static String getFarmName() {
        return PREF != null ? PREF.getString("farmname", null) : null;
    }

    public static String getName() {
        return PREF != null ? PREF.getString("name", null) : null;
    }

    public static String getPhone() {
        return PREF != null ? PREF.getString("phone", null) : null;
    }

    public static String getAddr() {
        return PREF != null ? PREF.getString("addr", null) : null;
    }

    public static String getToken() { return PREF != null ? PREF.getString("token", null) : null;}
}
