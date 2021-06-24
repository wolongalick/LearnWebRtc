package com.alick.learnwebrtc.bean.request

import com.alick.learnwebrtc.bean.SdpBean
import com.alick.learnwebrtc.bean.request.base.BaseSignalRequest
import com.alick.learnwebrtc.constant.RequestType

class SdpAnswerRequest(roomId: String, toAccount: String, sdpBean: SdpBean) : BaseSignalRequest(
    RequestType.SDP_ANSWER, roomId,
    toAccount, data = sdpBean
)