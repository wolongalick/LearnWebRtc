package com.alick.learnwebrtc.bean.response

import com.alick.learnwebrtc.bean.IceCandidateBean
import com.alick.learnwebrtc.bean.response.base.BaseResponse

class IceCandidateResponse(
    msgType: String,
    roomId: String ,
    fromAccount: String ,
    data: IceCandidateBean
) : BaseResponse<IceCandidateBean>(msgType, roomId, fromAccount, data) {
}