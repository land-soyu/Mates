package com.matescorp.soyu.farmkingapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.matescorp.soyu.farmkingapp.fcm.MyFirebaseInstanceIDService;
import com.matescorp.soyu.farmkingapp.util.DataPreference;

public class MainActivity extends AppCompatActivity {

    private Context context;

    private WebView webView;
    private Button reload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;

        webView = (WebView)findViewById(R.id.webView);
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            if (0 != (getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE))
            { WebView.setWebContentsDebuggingEnabled(true); }
        }*/
        reload = (Button)findViewById(R.id.reload);
        reload.setVisibility(View.GONE);

    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onStart() {
        super.onStart();

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if(url.startsWith("tel:")){
                    Intent dial = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(dial);
                    return true;
                } else if(url.startsWith("logout:")){

                    DataPreference.setId(null);

                    Intent intent = new Intent(context, LoginActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                }

                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                //  page loading start
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                //  page loading end
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
        });
        webView.loadUrl(Urls.farmking + "/mobile/index.html?id="+ DataPreference.getId()+"&pwd="+DataPreference.getPwd());

        reload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.reload();
            }
        });

        String _token = FirebaseInstanceId.getInstance().getToken();
        String id = DataPreference.getId();

        SendThread thread = new SendThread(id, _token);
        thread.start();
    }

    @Override
    public void onBackPressed() {
        if ( webView.getUrl().contains("http://www.farmking.co.kr/mobile/index.html") ) {
            finish();
        }
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            finish();
        }
    }

    private class SendThread extends Thread {
        private static final String TAG = "Running SendThread = ";
        String id;
        String _token;

        private SendThread(String mid,String m_token){
            id = mid;
            _token = m_token;
        }

        public void run() {
            MyFirebaseInstanceIDService iidService = new MyFirebaseInstanceIDService();
            String serial = DataPreference.getSerial();
            if (serial != null ) {
                FirebaseMessaging.getInstance().subscribeToTopic(serial);
            } else {
                Log.d(TAG, "Failed subscribe to Topic");
            }
            iidService.sendRegistrationServer(id, _token);
            Log.d(TAG, "Register in server : " + id);
        }
    }
}
