package com.alick.learnwebrtc.bean.request

import com.alick.learnwebrtc.bean.request.base.BaseSignalRequest
import com.alick.learnwebrtc.constant.RequestType
import com.alick.learnwebrtc.utils.RoomIdUtils

class CreateRoomRequest : BaseSignalRequest(
    RequestType.CREATE,
    RoomIdUtils.createRoomId(),
    ""
)