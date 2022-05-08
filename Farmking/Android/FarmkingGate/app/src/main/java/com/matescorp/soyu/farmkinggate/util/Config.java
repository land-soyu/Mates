package com.matescorp.soyu.farmkinggate.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import java.io.File;
import java.util.Calendar;

/**
 * Created by tbzm on 16. 4. 18.
 */
public class Config {
//    public static final String SERVER_URL = "http://www.farmking.co.kr/api/";
    public static final String SERVER_URL = "http://sf.matescorp.com/farm/api/";

    public static final String GET_FARM_SENSOR_LIST_DATA = "get_farm_sensor_list_data_post.php";
    public static final String GET_FARM_SENSOR_ITEM_LIST_DATA = "get_farm_sensor_item_list_data_post.php";
    public static final String SET_FARM_SENSOR_DATA = "set_farm_sensor_data_post.php";
    public static final String CHECK_FARM_SENSOR_DATA = "check_farm_sensor_data_post.php";

    public static final String INSERT_SENSOR_DATA_PHP = "sendSensorData_post_kr.php";
    public static final String UPDATE_SENSOR_DATA_PHP = "sendSensorData_update_post_kr.php";
    public static final String GET_MANAGER_PUSH_TOKEN = "get_manager_token_kr.php";
    public static final String GET_MANAGER_GWIDX_KR = "get_manager_gwidx_kr.php";
    public static final String GET_GATEWAY_FLOOR_INFO_POST = "get_gateway_floor_info_post.php";

    public static final String PARAM_EQUALS = "=";
    public static final String PARAM_AND = "&";
    public static final String PARAM_SERIAL = "serial";
    public static final String PARAM_GWIDX = "gwidx";
    public static final String PARAM_ID = "id";
    public static final String PARAM_DATE = "date";
    public static final String PARAM_TEMP = "temp";
    public static final String PARAM_MOVE = "move";

    public static final int MESSAGE_DATA = 1000;
    public static final int MESSAGE_GET_FARM_SENSOR_DATA = MESSAGE_DATA * 0x01;
    public static final int MESSAGE_GET_FARM_SENSOR_LIST_DATA = MESSAGE_DATA * 0x02;
    public static final int MESSAGE_SET_FARM_SENSOR_DATA = MESSAGE_DATA * 0x03;
    public static final int MESSAGE_GET_FARM_SENSOR_ITEM_LIST_DATA = MESSAGE_DATA * 0x04;
    public static final int MESSAGE_CHECK_FARM_SENSOR_DATA = MESSAGE_DATA * 0x05;

    public static final String PREF_GWIDX_KEY = "gwidx_key";
    public static final String PREF_FILE_PATH_KEY = "file_path_key";

    public static final String KEY_LOT_NAME = "map_lot_key";
    public static final String KEY_TOKEN = "map_token_key";


    public static final String PREF_PARKING_FEE_KEY = "parking_fee_key";
    public static final String PREF_BASIC_TM_KEY = "basic_tm_key";
    public static final String PREF_ADD_FEE_KEY = "add_fee_key";
    public static final String PREF_ADD_TM_KEY = "add_tm_key";
    public static final String PREF_DAY_FEE_KEY = "day_fee_key";
    public static final String PREF_MONTH_FEE_KEY = "month_fee_key";
    public static final String DAY_MAX_FEE_ENABLE = "0";
    public static final String PREF_ZOOM_LEVEL_POSITION_KEY = "zoom_level_key";

    public static String milliSecond2Time(long time) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(time);
        int hr = c.get(Calendar.HOUR_OF_DAY);
        int min = c.get(Calendar.MINUTE);
        String s_hr = String.valueOf(hr);
        String s_min = String.valueOf(min);
        if (hr < 10) {
            s_hr = "0" + hr;
        }
        if (min < 10) {
            s_min = "0" + min;
        }
        return s_hr + ":" + s_min;
    }

    public static String getSerialNum(Context con){
        return getSheardprference(con);
    }

    private static String getSheardprference(Context context) {
        SharedPreferences pref = context.getSharedPreferences("farmkinggate", context.MODE_PRIVATE);
        return pref.getString("serialnum", "");
    }


    public static String getSaveImageFileExternalDirectory() {
        File fileRoot = new File(Environment.getExternalStorageDirectory() + File.separator + "FARMKING");
        if (!fileRoot.exists()) {
            fileRoot.mkdir();
        }
        return fileRoot.getPath() + File.separator;
    }
}
