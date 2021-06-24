package com.alick.learnwebrtc.bean.request

import com.alick.learnwebrtc.bean.request.base.BaseSignalRequest
import com.alick.learnwebrtc.constant.RequestType

class LeaveRequest(roomId: String, toAccount: String) :
    BaseSignalRequest(RequestType.LEAVE, roomId, toAccount)