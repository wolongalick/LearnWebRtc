package com.alick.learnwebrtc.bean.response.base

open class BaseResponse<Data>(
    val msgType: String,val roomId:String="", val fromAccount: String = "", val data: Data? = null
)