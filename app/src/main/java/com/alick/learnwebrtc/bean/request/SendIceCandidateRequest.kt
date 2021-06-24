package com.alick.learnwebrtc.bean.request

import com.alick.learnwebrtc.bean.IceCandidateBean
import com.alick.learnwebrtc.bean.request.base.BaseSignalRequest
import com.alick.learnwebrtc.constant.RequestType

class SendIceCandidateRequest(roomId: String,toAccount: String, data: IceCandidateBean) :
    BaseSignalRequest(RequestType.CANDIDATE,roomId, toAccount, data) {

}