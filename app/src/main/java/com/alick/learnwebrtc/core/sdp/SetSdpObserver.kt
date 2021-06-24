package com.alick.learnwebrtc.core.sdp

import org.webrtc.SdpObserver
import org.webrtc.SessionDescription

abstract class SetSdpObserver : SdpObserver {
    final override fun onCreateSuccess(p0: SessionDescription?) {
    }

    final override fun onCreateFailure(p0: String?) {
    }


}