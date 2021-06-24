package com.alick.learnwebrtc.bean.response

import com.alick.learnwebrtc.bean.Contact
import com.alick.learnwebrtc.bean.response.base.BaseResponse
import com.alick.learnwebrtc.constant.ResponseType


class RemoveContactResponse: BaseResponse<Contact>(ResponseType.REMOVE_CONTACT) {
}