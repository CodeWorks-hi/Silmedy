package com.example.silmedy;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

public class MapActivity extends AppCompatActivity {

    private WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        setContentView(webView);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());

        // JS → Android 통신 인터페이스 등록
        webView.addJavascriptInterface(new WebAppInterface(), "Android");

        // 로컬 HTML 파일 로드
        webView.loadUrl("file:///android_asset/map_kakao.html");
    }

    public class WebAppInterface {
        @JavascriptInterface
        public void onAddressSelected(String address) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("selected_address", address);
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }
}