package com.example.q.project2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

public class WebViewFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    public WebViewFragment(){ } // constructor
    private static final String baseURL = "http://52.162.211.235:7714";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab3_webview, container, false);
        WebView mainview = (WebView) view.findViewById(R.id.webUImain);
        mainview.loadUrl(baseURL);

        return view;
    }

    @Override
    public void onRefresh() {

    }
}
