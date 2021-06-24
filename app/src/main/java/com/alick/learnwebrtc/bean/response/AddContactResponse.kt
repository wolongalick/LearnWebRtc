package com.alick.learnwebrtc.bean.response

import com.alick.learnwebrtc.bean.Contact
import com.alick.learnwebrtc.bean.response.base.BaseResponse


class AddContactResponse(msgType: String) : BaseResponse<Contact>(msgType)