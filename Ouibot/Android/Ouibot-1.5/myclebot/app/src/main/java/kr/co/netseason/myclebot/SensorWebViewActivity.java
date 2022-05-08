package kr.co.netseason.myclebot;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

/**
 * Created by tbzm on 15. 12. 10.
 */
public class SensorWebViewActivity extends Activity implements View.OnClickListener{
    private WebView mSensorWebView;
    private ProgressBar mProgressBar;
    private final String TAG = getClass().getName();
    private LinearLayout mLayoutRequestError = null;
    private Handler mErrorLayoutHide = null;

    private boolean mbErrorOccured = false;
    private boolean mbReloadPressed = false;
    public final String DEMO_WEB_URL = "http://sh-demo.beeob.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_PROGRESS); // 프로그레스
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensor_webview_activity);

        ((ImageButton) findViewById(R.id.btnRetry)).setOnClickListener(this);
        mLayoutRequestError = (LinearLayout) findViewById(R.id.layout_request_error);
        mErrorLayoutHide = getErrorLayoutHideHandler();

        mSensorWebView = (WebView) findViewById(R.id.sensor_webview);
        mProgressBar = (ProgressBar) this.findViewById(R.id.progress);
        WebSettings set = mSensorWebView.getSettings();
        set.setJavaScriptEnabled(true);
        set.setBuiltInZoomControls(true);
        set.setJavaScriptCanOpenWindowsAutomatically(true);
        mSensorWebView.setWebViewClient(new SensorWebViewClient());
        mSensorWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                mProgressBar.setProgress(newProgress);
            }
        });
        mSensorWebView.loadUrl(DEMO_WEB_URL);
    }

    @Override
    public void onBackPressed() {
        if (mSensorWebView.canGoBack()) {
            mSensorWebView.goBack();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.btnRetry) {
            if (!mbErrorOccured) {
                return;
            }

            mbReloadPressed = true;
            mSensorWebView.reload();
            mbErrorOccured = false;
        }
    }

    private Handler getErrorLayoutHideHandler() {
        return new mErrorHandler();
    }

    private class mErrorHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            mLayoutRequestError.setVisibility(View.GONE);
            super.handleMessage(msg);
        }
    }

    private void showErrorLayout() {
        mLayoutRequestError.setVisibility(View.VISIBLE);
    }

    private void hideErrorLayout() {
        mErrorLayoutHide.sendEmptyMessageDelayed(10000, 200);
    }

    public class SensorWebViewClient extends WebViewClient {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            mProgressBar.setVisibility(View.VISIBLE);
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            mProgressBar.setVisibility(View.INVISIBLE);
            if (mbErrorOccured == false && mbReloadPressed) {
                hideErrorLayout();
                mbReloadPressed = false;
            }
            super.onPageFinished(view, url);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            mbErrorOccured = true;
            showErrorLayout();
            super.onReceivedError(view, errorCode, description, failingUrl);
        }
    }
}
