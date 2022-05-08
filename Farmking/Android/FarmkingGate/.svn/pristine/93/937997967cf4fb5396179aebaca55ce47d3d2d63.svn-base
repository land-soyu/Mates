package com.matescorp.soyu.farmkinggate.asynctask;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.matescorp.soyu.farmkinggate.util.Config;
import com.matescorp.soyu.farmkinggate.util.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by tbzm on 16. 4. 25.
 */
public class GetFarmSensorItemListDataHTTPTask extends AsyncTask<String, Void, String> {
    private Context mContext;
    private Handler mHandler;
    private final String TAG = getClass().getName();

    public GetFarmSensorItemListDataHTTPTask(Context context, Handler handler) {
        mContext = context;
        mHandler = handler;
    }

    @Override
    protected String doInBackground(String... args) {
        String returnValue = "";
        HttpURLConnection conn = null;
        try {
            Logger.e(TAG, "args[0] = " + args[0]);
            String urlString = Config.SERVER_URL + Config.GET_FARM_SENSOR_ITEM_LIST_DATA;
            Logger.e(TAG, "urlString = " + urlString);
            URL url = new URL(urlString);

            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);            // 입력스트림 사용여부
            conn.setDoOutput(false);            // 출력스트림 사용여부
            conn.setUseCaches(false);        // 캐시사용 여부
            conn.setReadTimeout(20000);        // 타임아웃 설정 ms단위
            conn.setRequestMethod("POST");

            StringBuffer params = new StringBuffer("");
            params.append(args[0]);
            PrintWriter output = new PrintWriter(conn.getOutputStream());
            output.print(params.toString());
            output.close();

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
            if (conn != null) {
                conn.disconnect();
            }
        }
        return returnValue;
    }

    @Override
    protected void onPostExecute(String result) {
        result = result.trim();
        Logger.e(TAG, "result = " + result);
        if (mHandler != null) {
            Message msg = Message.obtain();
            msg.what = Config.MESSAGE_GET_FARM_SENSOR_ITEM_LIST_DATA;
            msg.obj = result;
            mHandler.sendMessage(msg);
        }
        super.onPostExecute(result);
    }
}
