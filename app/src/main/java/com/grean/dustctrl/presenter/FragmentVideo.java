package com.grean.dustctrl.presenter;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;

import com.grean.dustctrl.R;

/**
 * Created by Administrator on 2017/8/25.
 */

public class FragmentVideo extends Fragment {
    /*private WebView webView;
    private WebSettings webSettings;*/
    private Button btnVideo,btnSetting;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View messageLayout = inflater.inflate(R.layout.fragment_video,container,false);
        btnVideo = messageLayout.findViewById(R.id.btnVideoCtrl);
        btnSetting = messageLayout.findViewById(R.id.btnVideoSetting);
        btnVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().startActivity(getActivity().getPackageManager().getLaunchIntentForPackage("com.mcu.iVMSHD"));
            }
        });
        btnVideo.setEnabled(false);
        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("http://192.168.1.64");
                Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                startActivity(intent);
            }
        });
        messageLayout.findViewById(R.id.btnRouterSetting).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("http://192.168.1.1");
                Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                startActivity(intent);
            }
        });
        /*webSettings = webView.getSettings();

        webView.loadUrl("http://192.168.168.84:189/login");*/


        return messageLayout;
    }
}
