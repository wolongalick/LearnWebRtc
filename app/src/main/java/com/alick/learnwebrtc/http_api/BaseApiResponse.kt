package com.alick.learnwebrtc.http_api

open class BaseApiResponse<Data>(var data:Data?=null, val code:Int=0, val msg:String="")