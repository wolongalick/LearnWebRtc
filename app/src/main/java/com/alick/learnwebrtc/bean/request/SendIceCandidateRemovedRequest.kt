package com.alick.learnwebrtc.bean.request

import com.alick.learnwebrtc.bean.IceCandidateBean
import com.alick.learnwebrtc.bean.request.base.BaseSignalRequest
import com.alick.learnwebrtc.constant.RequestType

class SendIceCandidateRemovedRequest(roomId: String,toAccount: String, data: Data) :
    BaseSignalRequest(RequestType.REMOVE_CANDIDATES,roomId, toAccount,  data) {

    data class Data(val type: String, val candidates: MutableList<IceCandidateBean>)

}