package kr.co.netseason.myclebot.Security;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import kr.co.netseason.myclebot.Logger.Logger;
import kr.co.netseason.myclebot.openwebrtc.Config;

public class ForIOSNotificationHttpAsyncTask extends AsyncTask<String, Void, String> {

    private String mToken;
    private String mTitle;
    private String mMessage;
    private Handler mHandler;
    private boolean mRingSound;
    private String mKey;
    private String mType;

    public ForIOSNotificationHttpAsyncTask(String token, String title, String message, Handler handler, boolean ringSound, String key, String type) {
        mToken = token;
        mTitle = title;
        mMessage = message;
        mHandler = handler;
        mRingSound = ringSound;
        mKey = key;
        mType = type;
    }

    @Override
    protected String doInBackground(String... args) {
        try {

            String apiKey = Config.GCM_SEVER_API_KEY;
            JSONObject json2 = new JSONObject();
            json2.put("key", mKey);
            json2.put("type",mType);

            JSONObject json3 = new JSONObject();
            if(mRingSound) {
                json3.put("sound", "ring_bell.aiff");
            }else{
                json3.put("sound", "default");
            }
//            json.put("badge","10");
            json3.put("title",mTitle);
            json3.put("body",mMessage);

            JSONObject json = new JSONObject();
            json.put("to", mToken);
            json.put("priority","high");
            json.put("content_available",true);
            json.put("notification",json3);
            json.put("data", json2);
            URL url = new URL(Config.GCM_SEVER_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "key=" + apiKey);
            conn.setDoOutput(true);

            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            Logger.d("!!!","json = "+json.toString());
            byte[] data = json.toString().getBytes("UTF-8");
            wr.write(data);

            wr.flush();
            wr.close();

            //Get the response
            int responseCode = conn.getResponseCode();
            System.out.println("\nSending 'POST' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            System.out.println(response.toString());
            return response.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e){
            e.printStackTrace();
        }
        return "";
    }

    @Override
    protected void onPostExecute(String result) {
        if (mHandler != null) {
            Message msg = Message.obtain();
            msg.obj = result;
            mHandler.sendMessage(msg);
            mHandler = null;
        }
    }
}
