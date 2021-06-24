package com.alick.learnwebrtc.utils

import android.os.Handler
import android.os.Looper
import android.widget.Toast

object ToastUtils {
    private val mainHandler by lazy {
        Handler(Looper.getMainLooper())
    }

    fun show(msg: String, isError: Boolean = true) {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            Toast.makeText(AppHolder.getApp(), msg, Toast.LENGTH_SHORT).show()
        } else {
            mainHandler.post {
                Toast.makeText(AppHolder.getApp(), msg, Toast.LENGTH_SHORT).show()
            }
        }

        if (isError) {
            BLog.e(msg)
        } else {
            BLog.i(msg)
        }
    }

}