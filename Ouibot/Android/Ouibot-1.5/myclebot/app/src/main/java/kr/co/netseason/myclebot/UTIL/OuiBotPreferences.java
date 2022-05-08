package kr.co.netseason.myclebot.UTIL;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Administrator on 2015-06-26.
 */
public class OuiBotPreferences {

    private static final String OuiBot = "OuiBot";
    private static final String LOGIN_ID = "loginid";
    private static final String UUID = "uuid";

    public synchronized static void setLoginId(Context context, String id) {
        SharedPreferences pref = context.getSharedPreferences(OuiBot, Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(LOGIN_ID, id);
        editor.commit();
    }
    public synchronized static String getLoginId(Context context) {
        SharedPreferences pref = context.getSharedPreferences(OuiBot, Context.MODE_MULTI_PROCESS);
        return pref.getString(LOGIN_ID, null);
    }

    public synchronized static void setUUID(Context context, String id) {
        SharedPreferences pref = context.getSharedPreferences(OuiBot, Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(UUID, id);
        editor.commit();
    }
    public synchronized static String getUUID(Context context) {
        SharedPreferences pref = context.getSharedPreferences(OuiBot, Context.MODE_MULTI_PROCESS);
        return pref.getString(UUID, null);
    }

    public synchronized static void delLoginId(Context context) {
        SharedPreferences pref = context.getSharedPreferences(OuiBot, Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.commit();
    }
}
