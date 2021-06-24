package com.alick.learnwebrtc.bean.response

import com.alick.learnwebrtc.bean.RoomBean
import com.alick.learnwebrtc.bean.response.base.BaseResponse
import com.alick.learnwebrtc.constant.ResponseType

class CreateRoomResponse : BaseResponse<RoomBean>(ResponseType.CREATE_SUCCESS)