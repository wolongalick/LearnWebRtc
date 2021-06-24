package com.alick.learnwebrtc.bean.request

import com.alick.learnwebrtc.bean.SdpBean
import com.alick.learnwebrtc.bean.request.base.BaseSignalRequest
import com.alick.learnwebrtc.constant.RequestType

class SdpOfferRequest(roomId: String, toAccount: String, sdpBean: SdpBean) : BaseSignalRequest(
    RequestType.SDP_OFFER, roomId,
    toAccount,data = sdpBean
)