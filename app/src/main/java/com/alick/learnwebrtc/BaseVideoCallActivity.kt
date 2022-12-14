package com.alick.learnwebrtc

import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.alick.learnwebrtc.bean.IceCandidateBean
import com.alick.learnwebrtc.bean.request.SendIceCandidateRemovedRequest
import com.alick.learnwebrtc.bean.request.SendIceCandidateRequest
import com.alick.learnwebrtc.constant.WebRtcConstant
import com.alick.learnwebrtc.manager.SignalManager
import com.alick.learnwebrtc.manager.WebSocketManager
import com.alick.learnwebrtc.utils.BLog
import com.alick.learnwebrtc.utils.ToastUtils
import org.java_websocket.handshake.ServerHandshake
import org.webrtc.*
import org.webrtc.PeerConnection.*
import org.webrtc.RendererCommon.ScalingType
import org.webrtc.audio.AudioDeviceModule
import org.webrtc.audio.JavaAudioDeviceModule.*
import java.io.File
import java.util.*

abstract class BaseVideoCallActivity : AppCompatActivity() {
    companion object {
        const val TAG = "BaseVideoCallActivity"
        const val INTENT_KEY_TO_ACCOUNT: String = "intent_key_to_account"
        const val INTENT_KEY_FROM_ACCOUNT: String = "intent_key_from_account"
        const val INTENT_KEY_ROOM_ID: String = "intent_key_room_id"
    }

    private val loopback = false
    private val tracing = true
    private val videoCodecHwAcceleration = true
    private val isRecordVideo = false   //是否需要录制视频
    protected var roomId: String = ""
    protected var isAnswered = false //是否已接听
    private var isUsingFrontCamera: Boolean = false     //是否正在使用前置摄像头
    private var isInitialized = false                   //是否已经初始化过了
    private var isRendererSwapped = false               //是否交换过本地和远程的渲染器位置

    protected abstract val fromAccount: String
    protected abstract val toAccount: String

    protected abstract val mPipRenderer: SurfaceViewRenderer

    protected abstract val mFullscreenRenderer: SurfaceViewRenderer

    protected abstract val isSender: Boolean            //是否是主叫方


    private val surfaceTextureHelper: SurfaceTextureHelper by lazy {
        val surfaceTextureHelper =
            SurfaceTextureHelper.create("CaptureThread", eglBase.eglBaseContext)

        surfaceTextureHelper
    }

    private val videoCapturer: CameraVideoCapturer? by lazy {
        val videoCapturer = if (Camera2Enumerator.isSupported(this)) {
            createCameraCapture(Camera2Enumerator(this))
        } else {
            createCameraCapture(Camera1Enumerator(true))
        }
        videoCapturer
    }

    private val remoteVideoSink: VideoSink = VideoSink {
        if (isRendererSwapped) {
            mPipRenderer.onFrame(it)
        } else {
            mFullscreenRenderer.onFrame(it)
        }
    }

    private val localVideoSink: VideoSink = VideoSink {
        if (isRendererSwapped) {
            mFullscreenRenderer.onFrame(it)
        } else {
            mPipRenderer.onFrame(it)
        }
    }

    private val remoteSinks: MutableList<VideoSink> by lazy {
        val remoteSinks = mutableListOf<VideoSink>()
        remoteSinks.add(remoteVideoSink)
        if (isRecordVideo) {
            remoteSinks.add(videoFileRenderer)
        }
        remoteSinks
    }

    private val localVideoTrack: VideoTrack by lazy {
        videoCapturer?.let {
            it.initialize(surfaceTextureHelper, this, videoSource.capturerObserver)
            it.startCapture(
                WebRtcConstant.HD_VIDEO_WIDTH,
                WebRtcConstant.HD_VIDEO_HEIGHT,
                WebRtcConstant.BPS_IN_KBPS
            )
        }
        val localVideoTrack = factory.createVideoTrack(
            WebRtcConstant.VIDEO_TRACK_ID,
            videoSource
        )
        localVideoTrack.setEnabled(true)
        localVideoTrack.addSink(localVideoSink)
        localVideoTrack
    }

    private val remoteVideoTrack: VideoTrack? by lazy {
        for (transceiver in peerConnection.transceivers) {
            val track = transceiver.receiver.track()
            if (track is VideoTrack) {
                return@lazy track
            }
        }
        null
    }

    private val videoSource: VideoSource by lazy {
        val videoSource = factory.createVideoSource(videoCapturer?.isScreencast ?: false)
        videoSource
    }

    private val audioSource: AudioSource by lazy {
        val audioSource = factory.createAudioSource(audioConstraints)
        audioSource
    }

    private val videoFileRenderer: VideoFileRenderer by lazy {
        val videoFileRenderer = VideoFileRenderer(
            File(
                getExternalFilesDir("webRtc"),
                "${System.currentTimeMillis()}.mp4"
            ).absolutePath,
            WebRtcConstant.HD_VIDEO_WIDTH,
            WebRtcConstant.HD_VIDEO_HEIGHT,
            eglBase.eglBaseContext
        )
        videoFileRenderer
    }

    private val localAudioTrack: AudioTrack by lazy {
        val localAudioTrack = factory.createAudioTrack(
            WebRtcConstant.AUDIO_TRACK_ID,
            audioSource
        )
        localAudioTrack.setEnabled(true)
        localAudioTrack
    }

    private val localVideoSender: RtpSender? by lazy {
        var localVideoSender: RtpSender? = null
        for (sender in peerConnection.senders) {
            if (sender.track() != null) {
                val trackType = sender.track()!!.kind()
                if (trackType == WebRtcConstant.VIDEO_TRACK_TYPE) {
                    Log.i(TAG, "Found video sender.")
                    localVideoSender = sender
                }
            }
        }
        localVideoSender
    }

    private val peerConnectionObserver: PeerConnection.Observer = object :
        PeerConnection.Observer {
        override fun onIceCandidate(candidate: IceCandidate) {
            BLog.d("--->onIceCandidate()")
            SignalManager.candidate(
                SendIceCandidateRequest(
                    roomId,
                    toAccount,
                    IceCandidateBean(
                        candidate.sdpMLineIndex,
                        candidate.sdpMid,
                        candidate.sdp
                    )
                )
            )
        }

        override fun onIceCandidatesRemoved(candidates: Array<IceCandidate>) {
            BLog.d("--->onIceCandidatesRemoved()")
            SignalManager.removeCandidates(
                SendIceCandidateRemovedRequest(
                    roomId,
                    toAccount,
                    SendIceCandidateRemovedRequest.Data("remove-candidates", candidates.map {
                        IceCandidateBean(it.sdpMLineIndex, it.sdpMid, it.sdp)
                    }.toMutableList())
                )
            )
        }

        override fun onSignalingChange(newState: SignalingState) {
            BLog.d("SignalingState: $newState")
        }

        override fun onIceConnectionChange(newState: IceConnectionState) {
            BLog.d("IceConnectionState: $newState")
            when (newState) {
                IceConnectionState.CONNECTED -> {
                    ToastUtils.show("Ice连接成功")
                }
                IceConnectionState.DISCONNECTED -> {
                    ToastUtils.show("Ice断开连接")
                }
                IceConnectionState.FAILED -> {
                    BLog.i("ICE connection failed.", TAG)
                }
            }
        }

        override fun onConnectionChange(newState: PeerConnectionState) {
            BLog.d("PeerConnectionState: $newState")
            when (newState) {
                PeerConnectionState.CONNECTED -> {
                    ToastUtils.show("Peer连接成功")
                }
                PeerConnectionState.DISCONNECTED -> {
                    ToastUtils.show("Peer断开连接")
                }
                PeerConnectionState.FAILED -> {
                    BLog.i("DTLS connection failed.", TAG)
                }
            }
        }

        override fun onIceGatheringChange(newState: IceGatheringState) {
            BLog.d("IceGatheringState: $newState")
        }

        override fun onIceConnectionReceivingChange(receiving: Boolean) {
            BLog.d("IceConnectionReceiving changed to $receiving")
        }

        override fun onSelectedCandidatePairChanged(event: CandidatePairChangeEvent) {
            BLog.d("Selected candidate pair changed because: $event")
        }

        override fun onAddStream(stream: MediaStream) {
            BLog.d("--->onAddStream()")
        }

        override fun onRemoveStream(stream: MediaStream) {
            BLog.d("--->onAddStream()")
        }

        override fun onDataChannel(dc: DataChannel) {
            BLog.d("New Data channel " + dc.label())
        }

        override fun onRenegotiationNeeded() {
            BLog.d("--->onRenegotiationNeeded()")
            // No need to do anything; AppRTC follows a pre-agreed-upon
            // signaling/negotiation protocol.
        }

        override fun onAddTrack(receiver: RtpReceiver, mediaStreams: Array<MediaStream>) {
            BLog.d("--->onAddStream()")
        }
    }

    private val factory: PeerConnectionFactory by lazy {
        val options = PeerConnectionFactory.Options()
        if (loopback) {
            options.networkIgnoreMask = 0
        }
        if (tracing) {
            PeerConnectionFactory.startInternalTracingCapture(
                Environment.getExternalStorageDirectory().absolutePath + File.separator
                        + "webrtc-trace.txt"
            )
        }

        val audioDeviceModule: AudioDeviceModule = createJavaAudioDevice()
        val encoderFactory: VideoEncoderFactory
        val decoderFactory: VideoDecoderFactory

        if (videoCodecHwAcceleration) {
            encoderFactory = DefaultVideoEncoderFactory(eglBase.eglBaseContext, true, false)
            decoderFactory = DefaultVideoDecoderFactory(eglBase.eglBaseContext)
        } else {
            encoderFactory = SoftwareVideoEncoderFactory()
            decoderFactory = SoftwareVideoDecoderFactory()
        }

        val factory = PeerConnectionFactory.builder()
            .setOptions(options)
            .setAudioDeviceModule(audioDeviceModule)
            .setVideoEncoderFactory(encoderFactory)
            .setVideoDecoderFactory(decoderFactory)
            .createPeerConnectionFactory()

        audioDeviceModule.release()

        factory
    }


    private val eglBase: EglBase by lazy {
        EglBase.create()
    }


    protected val videoSink = VideoSink { }

    /**
     * 音频约束
     */
    protected val audioConstraints: MediaConstraints by lazy {
        val audioConstraints = MediaConstraints()
        audioConstraints.mandatory.add(
            MediaConstraints.KeyValuePair(
                WebRtcConstant.AUDIO_ECHO_CANCELLATION_CONSTRAINT,
                "true"
            )
        )
        audioConstraints.mandatory.add(
            MediaConstraints.KeyValuePair(
                WebRtcConstant.AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT,
                "true"
            )
        )
        audioConstraints.mandatory.add(
            MediaConstraints.KeyValuePair(
                WebRtcConstant.AUDIO_HIGH_PASS_FILTER_CONSTRAINT,
                "true"
            )
        )
        audioConstraints.mandatory.add(
            MediaConstraints.KeyValuePair(
                WebRtcConstant.AUDIO_NOISE_SUPPRESSION_CONSTRAINT,
                "true"
            )
        )
        audioConstraints
    }

    /**
     * sdp媒体约束
     */
    protected val sdpMediaConstraints: MediaConstraints by lazy {
        val sdpMediaConstraints = MediaConstraints()
        sdpMediaConstraints.mandatory.add(
            MediaConstraints.KeyValuePair(
                WebRtcConstant.SDP_OFFER_TO_RECEIVE_AUDIO,
                "true"
            )
        )
        sdpMediaConstraints.mandatory.add(
            MediaConstraints.KeyValuePair(
                WebRtcConstant.SDP_OFFER_TO_RECEIVE_VIDEO,
                "true"
            )
        )

        sdpMediaConstraints
    }

    private var queuedRemoteCandidates: MutableList<IceCandidate>? = mutableListOf<IceCandidate>()

    private val iceServers: MutableList<IceServer> by lazy {
        val iceServers = mutableListOf<IceServer>()
        val turnServer: IceServer =
            IceServer.builder("stun:turn2.l.google.com")//google官方的turn服务器
                .setUsername("")//无用户名
                .setPassword("")//无密码
                .createIceServer()

        iceServers.add(turnServer)
        iceServers
    }

    private val rtcConfig: RTCConfiguration by lazy {
        val rtcConfig = RTCConfiguration(iceServers)
        // TCP candidates are only useful when connecting to a server that supports
        // ICE-TCP.
        rtcConfig.tcpCandidatePolicy = TcpCandidatePolicy.DISABLED
        rtcConfig.bundlePolicy = BundlePolicy.MAXBUNDLE
        rtcConfig.rtcpMuxPolicy = RtcpMuxPolicy.REQUIRE
        rtcConfig.continualGatheringPolicy =
            ContinualGatheringPolicy.GATHER_CONTINUALLY
        // Use ECDSA encryption.
        rtcConfig.keyType = KeyType.ECDSA
        // Enable DTLS for normal calls and disable for loopback calls.
        rtcConfig.enableDtlsSrtp = true//普通呼叫启用DTLS，环回呼叫禁用DTLS。
        rtcConfig.sdpSemantics = SdpSemantics.UNIFIED_PLAN

        rtcConfig
    }


    protected val peerConnection: PeerConnection by lazy {
        val peerConnection = factory.createPeerConnection(rtcConfig, peerConnectionObserver)



        peerConnection!!
    }

    private val webSocketListener = object : WebSocketManager.IWebSocketListener {
        override fun onOpen(serverHandshake: ServerHandshake) {
        }

        override fun onClose(code: Int, reason: String, remote: Boolean) {
            finish()
        }

        override fun onMessage(message: String) {
        }

        override fun onError(exception: Exception) {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WebSocketManager.addWebSocketListener(webSocketListener)
    }

    protected fun switchCamera() {
        videoCapturer?.switchCamera(object : CameraVideoCapturer.CameraSwitchHandler {
            override fun onCameraSwitchDone(isFrontFacing: Boolean) {
                isUsingFrontCamera = isFrontFacing
                setMirror()
            }

            override fun onCameraSwitchError(error: String?) {
                ToastUtils.show("切换摄像头失败:${error}")
            }

        })
    }

    protected fun init() {
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(applicationContext)
                .setFieldTrials("WebRTC-IntelVP8/Enabled/")
                .setEnableInternalTracer(true)
                .createInitializationOptions()
        )
        initRenderer()

        val mediaStreamLabels = listOf(WebRtcConstant.VIDEO_AUDIO_TRACK_ID)
        peerConnection.addTrack(localVideoTrack, mediaStreamLabels)

        // We can add the renderers right away because we don't need to wait for an
        // answer to get the remote track.
        remoteVideoTrack?.setEnabled(true)
        for (remoteSink in remoteSinks) {
            remoteVideoTrack?.addSink(remoteSink)
        }
        peerConnection.addTrack(localAudioTrack, mediaStreamLabels)
        findVideoSender()
        isInitialized = true
    }

    private fun initRenderer() {
        mFullscreenRenderer.init(eglBase.eglBaseContext, null)
        mFullscreenRenderer.setScalingType(ScalingType.SCALE_ASPECT_FILL)
        mFullscreenRenderer.setEnableHardwareScaler(true)

        mPipRenderer.setOnClickListener {
            swapRenderer()
        }

        mPipRenderer.init(eglBase.eglBaseContext, null)
        mPipRenderer.setScalingType(ScalingType.SCALE_ASPECT_FIT)
        mPipRenderer.setZOrderMediaOverlay(true)
        mPipRenderer.setEnableHardwareScaler(true)

        previewSelf()
    }

    private fun findVideoSender() {
        localVideoSender
    }

    private fun createJavaAudioDevice(): AudioDeviceModule {
        // Set audio record error callbacks.
        val audioRecordErrorCallback: AudioRecordErrorCallback = object : AudioRecordErrorCallback {
            override fun onWebRtcAudioRecordInitError(errorMessage: String) {
                BLog.e("onWebRtcAudioRecordInitError: $errorMessage", TAG)
            }

            override fun onWebRtcAudioRecordStartError(
                errorCode: AudioRecordStartErrorCode, errorMessage: String
            ) {
                BLog.e("onWebRtcAudioRecordStartError: $errorCode. $errorMessage", TAG)
            }

            override fun onWebRtcAudioRecordError(errorMessage: String) {
                BLog.e("onWebRtcAudioRecordError: $errorMessage", TAG)
            }
        }
        val audioTrackErrorCallback: AudioTrackErrorCallback = object : AudioTrackErrorCallback {
            override fun onWebRtcAudioTrackInitError(errorMessage: String) {
                BLog.e("onWebRtcAudioTrackInitError: $errorMessage", TAG)
            }

            override fun onWebRtcAudioTrackStartError(
                errorCode: AudioTrackStartErrorCode, errorMessage: String
            ) {
                BLog.e("onWebRtcAudioTrackStartError: $errorCode. $errorMessage", TAG)
            }

            override fun onWebRtcAudioTrackError(errorMessage: String) {
                BLog.e("onWebRtcAudioTrackError: $errorMessage", TAG)
            }
        }

        // Set audio record state callbacks.
        val audioRecordStateCallback: AudioRecordStateCallback = object : AudioRecordStateCallback {
            override fun onWebRtcAudioRecordStart() {
                BLog.i("Audio recording starts", TAG)
            }

            override fun onWebRtcAudioRecordStop() {
                BLog.i("Audio recording stops", TAG)
            }
        }

        // Set audio track state callbacks.
        val audioTrackStateCallback: AudioTrackStateCallback = object : AudioTrackStateCallback {
            override fun onWebRtcAudioTrackStart() {
                BLog.i("Audio playout starts", TAG)
            }

            override fun onWebRtcAudioTrackStop() {
                BLog.i("Audio playout stops", TAG)
            }
        }
        return builder(applicationContext)
            .setSamplesReadyCallback(null)
            .setUseHardwareAcousticEchoCanceler(true)
            .setUseHardwareNoiseSuppressor(true)
            .setAudioRecordErrorCallback(audioRecordErrorCallback)
            .setAudioTrackErrorCallback(audioTrackErrorCallback)
            .setAudioRecordStateCallback(audioRecordStateCallback)
            .setAudioTrackStateCallback(audioTrackStateCallback)
            .createAudioDeviceModule()
    }

    /**
     * 创建相机媒体流
     */
    private fun createCameraCapture(enumerator: CameraEnumerator): CameraVideoCapturer? {
        val deviceNames = enumerator.deviceNames

        //1.优先使用前置摄像头(用于视频通话时自拍)
        for (deviceName in deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                val videoCapturer: CameraVideoCapturer? =
                    enumerator.createCapturer(deviceName, null)
                if (videoCapturer != null) {
                    isUsingFrontCamera = true
                    return videoCapturer
                }
            }
        }

        //2.否则再使用其他摄像头(例如后置摄像头)
        for (deviceName in deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                val videoCapturer: CameraVideoCapturer? =
                    enumerator.createCapturer(deviceName, null)
                if (videoCapturer != null) {
                    isUsingFrontCamera = true
                    return videoCapturer
                }
            }
        }
        return null
    }

    protected fun drainCandidates() {
        BLog.i("--->drainCandidates()", TAG)
        synchronized(BaseVideoCallActivity::class.java) {
            if (queuedRemoteCandidates != null) {
                queuedRemoteCandidates?.let {
                    for (candidate in it) {
                        BLog.i("--->drainCandidates(),candidate:${candidate}", TAG)
                        peerConnection.addIceCandidate(candidate)
                    }
                }
                queuedRemoteCandidates = null
            }
        }
    }

    protected fun addIceCandidateFromRemote(candidate: IceCandidate) {
        BLog.i("--->addIceCandidateFromRemote()", TAG)
        if (queuedRemoteCandidates != null) {
            synchronized(BaseVideoCallActivity::class.java) {
                BLog.i(
                    "--->addIceCandidateFromRemote(),先添加到queuedRemoteCandidates集合中,candidate:${candidate}",
                    TAG
                )
                queuedRemoteCandidates?.add(candidate)
            }
        } else {
            BLog.i(
                "--->addIceCandidateFromRemote(),直接添加到peerConnection中,candidate:${candidate}",
                TAG
            )
            peerConnection.addIceCandidate(candidate)
        }
    }

    private fun releaseWebRtc() {
        if (!isInitialized) {
            return
        }
        mPipRenderer.release()
        mFullscreenRenderer.release()
        if (isRecordVideo) {
            videoFileRenderer.release()
        }

        factory.stopAecDump()
        peerConnection.dispose()
        audioSource.dispose()
        videoCapturer?.let {
            it.stopCapture()
            it.dispose()
        }
        videoSource.dispose()
        surfaceTextureHelper.dispose()
        factory.dispose()
        eglBase.release()
        PeerConnectionFactory.stopInternalTracingCapture()
        PeerConnectionFactory.shutdownInternalTracer()
    }

    override fun onBackPressed() {

    }

    override fun onDestroy() {
        WebSocketManager.removeWebSocketListener(webSocketListener)
        releaseWebRtc()
        super.onDestroy()
    }

    protected fun swapRenderer() {
        isRendererSwapped = !isRendererSwapped
        setMirror()
    }

    private fun setMirror() {
        setPipRendererMirror(mPipRenderer)
        setFullscreenRendererMirror(mFullscreenRenderer)
    }

    //设置画中画的
    private fun setPipRendererMirror(pipRenderer: SurfaceViewRenderer) {
        if (isUsingFrontCamera) {
            if (isRendererSwapped) {
                pipRenderer.setMirror(false)//
            } else {
                pipRenderer.setMirror(true)//自拍默认,需要设置为镜像
            }
        } else {
            if (isRendererSwapped) {
                pipRenderer.setMirror(false)
            } else {
                pipRenderer.setMirror(false)
            }
        }
    }

    //设置全屏的
    private fun setFullscreenRendererMirror(fullscreenRenderer: SurfaceViewRenderer) {
        if (isUsingFrontCamera) {
            if (isRendererSwapped) {
                fullscreenRenderer.setMirror(true)//自拍默认,需要设置为镜像
            } else {
                fullscreenRenderer.setMirror(false)
            }
        } else {
            if (isRendererSwapped) {
                fullscreenRenderer.setMirror(false)
            } else {
                fullscreenRenderer.setMirror(false)
            }
        }
    }

    private fun previewSelf() {
        swapRenderer()
    }

}