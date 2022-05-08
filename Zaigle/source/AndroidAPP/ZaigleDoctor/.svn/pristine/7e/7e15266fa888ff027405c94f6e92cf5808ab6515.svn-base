package com.matescorp.system.zaigle.data;

import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by sjkim on 17. 7. 4.
 */

public class ProfilePreferences {

    public static SharedPreferences PREF = null;
    public final static String TAG = "DataPreference";
    private static final String height = "height";
    private static final String weight = "weight";
    private static final String gender = "gender";
    private static final String birth = "birth";
    private static final String language = "language";
    private static final String radiomode = "radiomode";
    private static final String name = "name";
    private static final String device_name = "device_name";
    private static final String device_addr= "device_addr";
    private static final String search= "search";
    private static final String blecon= "blecon";

    private static final String g_height = "g_height";
    private static final String g_weight = "g_weight";
    private static final String g_gender = "g_gender";
    private static final String g_birth = "g_birth";
    private static final String g_name = "g_name";
    private static final String permission = "permission";


    public static void setName(String value) {
        if (PREF != null) {
            SharedPreferences.Editor editor = PREF.edit();
            editor.putString(name, value);
            editor.commit();
        } else {
            Log.d(TAG, "PREF == " + PREF);
        }
    }

    public static void setHeight(String value) {
        if (PREF != null) {
            SharedPreferences.Editor editor = PREF.edit();
            editor.putString(height, value);
            editor.commit();
        } else {
            Log.d(TAG, "PREF == " + PREF);
        }
    }

    public static void setWeight(String value) {
        if (PREF != null) {
            SharedPreferences.Editor editor = PREF.edit();
            editor.putString(weight, value);
            editor.commit();
        } else {
            Log.d(TAG, "PREF == " + PREF);
        }
    }

    public static void setGender(String value) {
        if (PREF != null) {
            SharedPreferences.Editor editor = PREF.edit();
            editor.putString(gender, value);
            editor.commit();
        } else {
            Log.d(TAG, "PREF == " + PREF);
        }
    }

    public static void setRadiomode(String value) {
        if (PREF != null) {
            SharedPreferences.Editor editor = PREF.edit();
            editor.putString(radiomode, value);
            editor.commit();
        } else {
            Log.d(TAG, "PREF == " + PREF);
        }
    }

    public static void setBirth(String value) {
        if (PREF != null) {
            SharedPreferences.Editor editor = PREF.edit();
            editor.putString(birth, value);
            editor.commit();
        } else {
            Log.d(TAG, "PREF == " + PREF);
        }
    }


    public static void setLanguagesj(String value) {
        if (PREF != null) {
            SharedPreferences.Editor editor = PREF.edit();
            editor.putString(language, value);
            editor.commit();
        } else {
            Log.d(TAG, "PREF == " + PREF);
        }
    }

    public static void setDeviceName(String value) {
        if (PREF != null) {
            SharedPreferences.Editor editor = PREF.edit();
            editor.putString(device_name, value);
            editor.commit();
        } else {
            Log.d(TAG, "PREF == " + PREF);
        }
    }


    public static void setDeviceAddr(String value) {
        if (PREF != null) {
            SharedPreferences.Editor editor = PREF.edit();
            editor.putString(device_addr, value);
            editor.commit();
        } else {
            Log.d(TAG, "PREF == " + PREF);
        }
    }

    public static void setSearch(int value) {
        if (PREF != null) {
            SharedPreferences.Editor editor = PREF.edit();
            editor.putInt(search, value);
            editor.commit();
        } else {
            Log.d(TAG, "PREF == " + PREF);
        }
    }
    public static void setBleCon(int value) {
        if (PREF != null) {
            SharedPreferences.Editor editor = PREF.edit();
            editor.putInt(blecon, value);
            editor.commit();
        } else {
            Log.d(TAG, "PREF == " + PREF);
        }
    }

    public static void delDeviceAddr() {
        if (PREF != null) {
            SharedPreferences.Editor editor = PREF.edit();
            editor.remove(device_addr);
            editor.remove(device_name);
            editor.commit();
        } else {
            Log.d(TAG, "PREF == " + PREF);
        }
    }



    public static void setNameG(String value) {
        if (PREF != null) {
            SharedPreferences.Editor editor = PREF.edit();
            editor.putString(g_name, value);
            editor.commit();
        } else {
            Log.d(TAG, "PREF == " + PREF);
        }
    }

    public static void setHeightG(String value) {
        if (PREF != null) {
            SharedPreferences.Editor editor = PREF.edit();
            editor.putString(g_height, value);
            editor.commit();
        } else {
            Log.d(TAG, "PREF == " + PREF);
        }
    }

    public static void setWeightG(String value) {
        if (PREF != null) {
            SharedPreferences.Editor editor = PREF.edit();
            editor.putString(g_weight, value);
            editor.commit();
        } else {
            Log.d(TAG, "PREF == " + PREF);
        }
    }

    public static void setGenderG(String value) {
        if (PREF != null) {
            SharedPreferences.Editor editor = PREF.edit();
            editor.putString(g_gender, value);
            editor.commit();
        } else {
            Log.d(TAG, "PREF == " + PREF);
        }
    }


    public static void setBirthG(String value) {
        if (PREF != null) {
            SharedPreferences.Editor editor = PREF.edit();
            editor.putString(g_birth, value);
            editor.commit();
        } else {
            Log.d(TAG, "PREF == " + PREF);
        }
    }

    public static void setPermission(String value) {
        if (PREF != null) {
            SharedPreferences.Editor editor = PREF.edit();
            editor.putString(permission, value);
            editor.commit();
        } else {
            Log.d(TAG, "PREF == " + PREF);
        }
    }



    public static String getHeight() {
        return PREF != null ? PREF.getString(height, null) : null;
    }

    public static String getWeight() {
        return PREF != null ? PREF.getString(weight, null) : null;
    }

    public static String getGender() {
        return PREF != null ? PREF.getString(gender, null) : null;
    }

    public static String getBirth() {
        return PREF != null ? PREF.getString(birth, null) : null;
    }

    public static String getLanguage() {return PREF != null ? PREF.getString(language, null) : null;}

    public static String getRadiomode() {return PREF != null ? PREF.getString(radiomode, null) : null;}

    public static String getName() {return PREF != null ? PREF.getString(name, null) : null;}

    public static String getDeviceName() {return PREF != null ? PREF.getString(device_name, null) : null;}

    public static String getDeviceAddr() {return PREF != null ? PREF.getString(device_addr, null) : null;}
    public static int getSearch() {return PREF != null ? PREF.getInt(search, 0) : null;}

    public static int getBleCon() {return PREF != null ? PREF.getInt(blecon, 0) : null;}


    public static String getG_Height() {return PREF != null ? PREF.getString(g_height, null) : null;}
    public static String getG_Weight() {return PREF != null ? PREF.getString(g_weight, null) : null;}
    public static String getG_Gender() { return PREF != null ? PREF.getString(g_gender, null) : null;}
    public static String getG_birth() { return PREF != null ? PREF.getString(g_birth, null) : null;}
    public static String getG_Name() {return PREF != null ? PREF.getString(g_name, null) : null;}
    public static String getPermission() {return PREF != null ? PREF.getString(permission, null) : null;}



}
