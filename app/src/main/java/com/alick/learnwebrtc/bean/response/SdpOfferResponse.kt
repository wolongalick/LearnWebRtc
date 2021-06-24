package com.alick.learnwebrtc.bean.response

import com.alick.learnwebrtc.bean.SdpBean
import com.alick.learnwebrtc.bean.response.base.BaseResponse
import com.alick.learnwebrtc.constant.ResponseType

class SdpOfferResponse(
    roomId: String = "",
    fromAccount: String = "",
    data: SdpBean? = null
) : BaseResponse<SdpBean>(ResponseType.SDP_OFFER, roomId, fromAccount, data) {
}