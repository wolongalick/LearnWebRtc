package com.alick.learnwebrtc.utils

import android.util.Log

class BLog {
    companion object {

        fun i(msg: String, tag: String = "alick") {
            Log.i(tag, msg)
        }

        fun d(msg: String, tag: String = "alick") {
            Log.d(tag, msg)
        }

        fun e(msg: String, tag: String = "alick") {
            Log.e(tag, msg)
        }
    }


}