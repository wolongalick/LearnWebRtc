package com.alick.learnwebrtc.utils

import com.tencent.mmkv.MMKV


class MMKVUtils {
    companion object {
        private var kv: MMKV = MMKV.defaultMMKV()

        fun getString(key: String): String {
            return kv.getString(key, "") ?: ""
        }

        fun getInt(key: String): Int {
            return kv.getInt(key, 0)
        }

        fun setString(key: String, value: String) {
            kv.putString(key, value)
        }

        fun setInt(key: String, value: Int) {
            kv.putInt(key, value)
        }
    }
}