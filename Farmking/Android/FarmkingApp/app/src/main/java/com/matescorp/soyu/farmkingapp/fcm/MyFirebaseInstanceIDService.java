package com.matescorp.soyu.farmkingapp.fcm;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.matescorp.soyu.farmkingapp.LoginActivity;
import com.matescorp.soyu.farmkingapp.util.DataPreference;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by jin-won on 2018. 1. 23..
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        String token = FirebaseInstanceId.getInstance().getToken();
        DataPreference.setToken(token);
        Log.d("MyFCM", "FCM token: " + token );

        //sendRegistrationServer(token, id);
    }

    public void sendRegistrationServer(String id, String token) {
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("id", id)
                .add("Token", token)
                .build();
        Log.d("sRS : ", "Make Body : " + id + "/" + token);

        // request
        Request request = new Request.Builder()
                .url("http://www.farmking.co.kr/fcm/register.php")
                .post(body)
                .build();
        Log.d("sRS : ", "Request : " + body);

        try {
            client.newCall(request).execute();
            Log.d("sRS : ", "execute");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
