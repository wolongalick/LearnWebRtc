package com.alick.learnwebrtc

import android.app.Application
import com.alick.learnwebrtc.utils.AppHolder
import com.alick.learnwebrtc.utils.ToastUtils
import com.tencent.mmkv.MMKV

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        AppHolder.init(this);
        MMKV.initialize(this)
    }
}