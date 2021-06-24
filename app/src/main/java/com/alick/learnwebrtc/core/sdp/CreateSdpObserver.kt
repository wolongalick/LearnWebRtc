package com.alick.learnwebrtc.core.sdp

import org.webrtc.SdpObserver

abstract class CreateSdpObserver : SdpObserver {

    final override fun onSetSuccess() {

    }

    final override fun onSetFailure(p0: String?) {

    }
}