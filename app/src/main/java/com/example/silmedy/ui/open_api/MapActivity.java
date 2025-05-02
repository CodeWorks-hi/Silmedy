package com.example.silmedy.ui.open_api;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.example.silmedy.ui.config.TokenManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class MapActivity extends AppCompatActivity {

    private WebView webView;
    private Double cachedLatitude = null;
    private Double cachedLongitude = null;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new TokenManager(getApplicationContext()).refreshAccessTokenAsync(new TokenManager.TokenRefreshCallback() {
            @Override
            public void onTokenRefreshed(String newAccessToken) {
                // Token refreshed successfully, you can add further logic if needed
            }
        });

        webView = new WebView(this);
        setContentView(webView);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true); // 추가
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true); // 추가
        webView.getSettings().setAllowFileAccessFromFileURLs(true); // 추가
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true); // 추가 ✅ 가장 중요
        webView.getSettings().setGeolocationEnabled(true); // Enable geolocation in WebView

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, android.webkit.GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }
        });
        webView.setWebViewClient(new WebViewClient());

        // JS → Android 통신 인터페이스 등록
        webView.addJavascriptInterface(new WebAppInterface(), "Android");

        // 로컬 HTML 파일 로드
        webView.loadUrl("file:///android_asset/map_kakao.html");

        Double lastSelectedLat = (double) getSharedPreferences("MapPrefs", MODE_PRIVATE).getFloat("lastSelectedLat", 0);
        Double lastSelectedLng = (double) getSharedPreferences("MapPrefs", MODE_PRIVATE).getFloat("lastSelectedLng", 0);

        if (lastSelectedLat != 0 && lastSelectedLng != 0) {
            webView.post(() -> {
                webView.evaluateJavascript("updateCurrentLocation(" + lastSelectedLat + "," + lastSelectedLng + ");", null);
            });
        } else {
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        cachedLatitude = location.getLatitude();
                        cachedLongitude = location.getLongitude();
                        webView.post(() -> {
                            webView.evaluateJavascript("updateCurrentLocation(" + cachedLatitude + "," + cachedLongitude + ");", null);
                        });
                    }
                });
        }
    }

    public class WebAppInterface {
        @JavascriptInterface
        public void onAddressSelected(String address, double latitude, double longitude) {
            // ✅ Save last selected location to SharedPreferences at the moment of confirm
            getSharedPreferences("MapPrefs", MODE_PRIVATE).edit()
                .putFloat("lastSelectedLat", (float) latitude)
                .putFloat("lastSelectedLng", (float) longitude)
                .apply();

            Intent resultIntent = new Intent();
            resultIntent.putExtra("selected_address", address);
            resultIntent.putExtra("latitude", latitude);
            resultIntent.putExtra("longitude", longitude);
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }
}