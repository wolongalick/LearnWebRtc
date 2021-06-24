package com.alick.learnwebrtc.constant

/**
 * WebRtc相关常量
 */
class WebRtcConstant {
    companion object{
        const val AUDIO_ECHO_CANCELLATION_CONSTRAINT = "googEchoCancellation"   //回音消除
        const val AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT = "googAutoGainControl"    //自动增益
        const val AUDIO_HIGH_PASS_FILTER_CONSTRAINT = "googHighpassFilter"      //高通滤波器
        const val AUDIO_NOISE_SUPPRESSION_CONSTRAINT = "googNoiseSuppression"   //降噪

        const val SDP_OFFER_TO_RECEIVE_AUDIO = "OfferToReceiveAudio"            //提供接收音频
        const val SDP_OFFER_TO_RECEIVE_VIDEO = "OfferToReceiveVideo"            //提供接收视频

        const val VIDEO_TRACK_ID = "ARDAMSv0"
        const val AUDIO_TRACK_ID = "ARDAMSa0"
        const val VIDEO_AUDIO_TRACK_ID = "ARDAMS"

        const val HD_VIDEO_WIDTH = 1280
        const val HD_VIDEO_HEIGHT = 720
        const val BPS_IN_KBPS = 1000

        const val VIDEO_TRACK_TYPE = "video"
    }
}