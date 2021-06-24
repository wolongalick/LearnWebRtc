package com.alick.learnwebrtc.bean.request.base

import com.alick.learnwebrtc.manager.AccountManager

open class BaseRequest(val msgType: String, val data: Any? = null) {
    val fromAccount: String = AccountManager.getAccount()
}