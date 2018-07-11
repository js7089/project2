package com.example.q.project2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaSession2;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    public WebViewFragment(){ } // constructor
    private static final String baseURL = "http://52.162.211.235:8080/";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.tab3_webview, container, false);
        //Intent i = new Intent(getActivity(), FullScreenWebViewActivity.class);
        //startActivity(i);
        return view;
    }

    @Override
    public void onRefresh() {

    }
}
