package com.alick.learnwebrtc.bean.request.base

open class BaseSignalRequest(
    msgType: String,
    val roomId: String,
    val toAccount: String,
    data: Any? = null
) : BaseRequest(msgType, data)