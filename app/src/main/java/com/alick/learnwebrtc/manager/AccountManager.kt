package com.alick.learnwebrtc.manager

import com.alick.learnwebrtc.constant.Constant
import com.alick.learnwebrtc.utils.MMKVUtils

object AccountManager {
    fun getAccount():String{
        return MMKVUtils.getString(Constant.MMKV_KEY_ACCOUNT)
    }
}