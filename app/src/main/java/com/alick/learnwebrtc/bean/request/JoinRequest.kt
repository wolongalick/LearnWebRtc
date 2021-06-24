package com.alick.learnwebrtc.bean.request

import com.alick.learnwebrtc.bean.request.base.BaseSignalRequest
import com.alick.learnwebrtc.constant.RequestType

class JoinRequest(roomId: String, toAccount: String) :
    BaseSignalRequest(RequestType.JOIN, roomId, toAccount)