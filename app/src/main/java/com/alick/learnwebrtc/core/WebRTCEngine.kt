package com.alick.learnwebrtc.core

import android.content.Context
import android.util.Log
import com.alick.learnwebrtc.utils.BLog
import org.webrtc.*
import org.webrtc.audio.AudioDeviceModule
import org.webrtc.audio.JavaAudioDeviceModule.*

class WebRTCEngine(private val context: Context) {

    private val hw_codec = true  //视频编解码工具,默认开启

    private val eglBase: EglBase by lazy {
        EglBase.create()
    }

    private val factory: PeerConnectionFactory by lazy {

        val options = PeerConnectionFactory.Options()


        val adm: AudioDeviceModule = createJavaAudioDevice()

        val encoderFactory: VideoEncoderFactory
        val decoderFactory: VideoDecoderFactory

        if (hw_codec) {
            encoderFactory = DefaultVideoEncoderFactory(
                eglBase.eglBaseContext, true,
                false
            )
            decoderFactory = DefaultVideoDecoderFactory(eglBase.getEglBaseContext())
        } else {
            encoderFactory = SoftwareVideoEncoderFactory()
            decoderFactory = SoftwareVideoDecoderFactory()
        }

        val peerConnectionFactory = PeerConnectionFactory.builder()
            .setOptions(options)
            .setAudioDeviceModule(adm)
            .setVideoEncoderFactory(encoderFactory)
            .setVideoDecoderFactory(decoderFactory)
            .createPeerConnectionFactory()

        BLog.i("peerConnectionFactory创建成功")
        adm.release()
        peerConnectionFactory
    }


    init {
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(true)
                .createInitializationOptions()
        )
    }


    private fun createJavaAudioDevice(): AudioDeviceModule {
        // Set audio record error callbacks.
        val audioRecordErrorCallback: AudioRecordErrorCallback = object : AudioRecordErrorCallback {
            override fun onWebRtcAudioRecordInitError(errorMessage: String) {
                BLog.e("onWebRtcAudioRecordInitError: $errorMessage")
            }

            override fun onWebRtcAudioRecordStartError(
                errorCode: AudioRecordStartErrorCode, errorMessage: String
            ) {
                BLog.e("onWebRtcAudioRecordStartError: $errorCode. $errorMessage")
            }

            override fun onWebRtcAudioRecordError(errorMessage: String) {
                BLog.e("onWebRtcAudioRecordError: $errorMessage")
            }
        }
        val audioTrackErrorCallback: AudioTrackErrorCallback = object : AudioTrackErrorCallback {
            override fun onWebRtcAudioTrackInitError(errorMessage: String) {
                BLog.e("onWebRtcAudioTrackInitError: $errorMessage")
            }

            override fun onWebRtcAudioTrackStartError(
                errorCode: AudioTrackStartErrorCode, errorMessage: String
            ) {
                BLog.e("onWebRtcAudioTrackStartError: $errorCode. $errorMessage")
            }

            override fun onWebRtcAudioTrackError(errorMessage: String) {
                BLog.e("onWebRtcAudioTrackError: $errorMessage")
            }
        }

        // Set audio record state callbacks.
        val audioRecordStateCallback: AudioRecordStateCallback = object : AudioRecordStateCallback {
            override fun onWebRtcAudioRecordStart() {
                BLog.i("Audio recording starts")
            }

            override fun onWebRtcAudioRecordStop() {
                BLog.i("Audio recording stops")
            }
        }

        // Set audio track state callbacks.
        val audioTrackStateCallback: AudioTrackStateCallback = object : AudioTrackStateCallback {
            override fun onWebRtcAudioTrackStart() {
                BLog.i("Audio playout starts")
            }

            override fun onWebRtcAudioTrackStop() {
                BLog.i("Audio playout stops")
            }
        }
        return builder(context)
//            .setSamplesReadyCallback(saveRecordedAudioToFile)     //音频保存到文件(暂不实现,详见官方demo)
            .setUseHardwareAcousticEchoCanceler(true)               //声学回声消除器
            .setUseHardwareNoiseSuppressor(true)                    //噪声抑制
            .setAudioRecordErrorCallback(audioRecordErrorCallback)
            .setAudioTrackErrorCallback(audioTrackErrorCallback)
            .setAudioRecordStateCallback(audioRecordStateCallback)
            .setAudioTrackStateCallback(audioTrackStateCallback)
            .createAudioDeviceModule()
    }
}