package com.alick.learnwebrtc.bean.request

import com.alick.learnwebrtc.bean.request.base.BaseSignalRequest
import com.alick.learnwebrtc.constant.RequestType

class RingRequest(roomId: String, toAccount: String) :
    BaseSignalRequest(RequestType.RING, roomId, toAccount)