package com.grean.dustctrl.presenter;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.grean.dustctrl.R;

/**
 * Created by Administrator on 2017/8/25.
 */

public class FragmentVideo extends Fragment {
    private WebView webView;
    private WebSettings webSettings;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View messageLayout = inflater.inflate(R.layout.fragment_video,container,false);
        webView = messageLayout.findViewById(R.id.webView);
        webSettings = webView.getSettings();
        webView.loadUrl("http://192.168.168.84:189/login");
        return messageLayout;
    }
}
