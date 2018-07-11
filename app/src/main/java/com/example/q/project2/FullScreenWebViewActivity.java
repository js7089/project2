package com.example.q.project2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.webkit.WebView;

public class FullScreenWebViewActivity extends Activity{


    private WebView webView ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_view);
        webView = findViewById(R.id.webUImain);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(Contacts.baseURL);
    }
}
