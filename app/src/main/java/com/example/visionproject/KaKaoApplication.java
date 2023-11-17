package com.example.visionproject;

import android.app.Application;

import com.kakao.sdk.common.KakaoSdk;

public class KaKaoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        KakaoSdk.init(this, "6d5700ed05cc573bd8debe87cb6dbe85");
    }
}
