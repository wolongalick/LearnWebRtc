package com.alick.learnwebrtc.bean.response

import com.alick.learnwebrtc.bean.Contact
import com.alick.learnwebrtc.bean.response.base.BaseResponse

class AllContactResponse(msgType: String) : BaseResponse<List<Contact>>(msgType)