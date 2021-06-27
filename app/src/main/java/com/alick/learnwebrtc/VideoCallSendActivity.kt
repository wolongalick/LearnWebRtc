package com.alick.learnwebrtc

import android.os.Bundle
import android.view.View
import com.alick.learnwebrtc.bean.Contact
import com.alick.learnwebrtc.bean.RoomBean
import com.alick.learnwebrtc.bean.SdpBean
import com.alick.learnwebrtc.bean.request.*
import com.alick.learnwebrtc.bean.response.SdpAnswerResponse
import com.alick.learnwebrtc.core.sdp.CreateSdpObserver
import com.alick.learnwebrtc.core.sdp.SetSdpObserver
import com.alick.learnwebrtc.manager.AccountManager
import com.alick.learnwebrtc.manager.SignalManager
import com.alick.learnwebrtc.manager.WebSocketManager
import com.alick.learnwebrtc.utils.BLog
import com.alick.learnwebrtc.utils.ToastUtils
import kotlinx.android.synthetic.main.activity_video_call_send.*
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import org.webrtc.SurfaceViewRenderer

class VideoCallSendActivity : BaseVideoCallActivity() {
    companion object {
        const val TAG = "VideoCallSendActivity"
    }

    override val fromAccount: String
        get() = AccountManager.getAccount()

    override val toAccount: String
        get() = intent.getStringExtra(INTENT_KEY_TO_ACCOUNT) ?: ""

    override val mPipRenderer: SurfaceViewRenderer
        get() = pipRenderer

    override val mFullscreenRenderer: SurfaceViewRenderer
        get() = fullscreenRRenderer

    override val isSender: Boolean
        get()= true

    private val iSignalOutgoingListener = object : WebSocketManager.ISignalOutgoingListener {
        override fun onCreateSuccess(roomBean: RoomBean) {
            roomId = roomBean.roomId
            SignalManager.invite(InviteRequest(roomBean.roomId, toAccount))
        }

        override fun onRing() {
            ToastUtils.show("对方已响铃")
        }

        override fun onReject() {
            ToastUtils.show("对方拒接")
            finish()
        }

        override fun onSdpAnswer(sdpAnswerResponse: SdpAnswerResponse) {
            setRemoteSdp(
                SessionDescription(
                    SessionDescription.Type.ANSWER,
                    sdpAnswerResponse.data?.description
                )
            )
        }

        override fun onJoinSuccess() {
            ToastUtils.show("对方已接听")
            runOnUiThread {
                tvCancel.visibility = View.GONE
                llPeerInfo.visibility = View.GONE
                ivSwitchCamera.visibility = View.VISIBLE
                tvHangup.visibility = View.VISIBLE
                rlLocalVideoContainer.visibility = View.VISIBLE
                isAnswered = true
                tvDuration.beginChatTime()
                swapRenderer()
                createOffer()
            }
        }

        override fun onIceCandidateFromRemote(candidate: IceCandidate) {
            addIceCandidateFromRemote(candidate)
        }

        override fun onIceCandidatesRemovedFromRemote(candidates: MutableList<IceCandidate>) {

        }

        override fun onLeave() {
            ToastUtils.show(" 对方已挂断")
            finish()
        }
    }

    private val iContactsListener = object : WebSocketManager.IContactsListener{
        override fun onAdd(contact: Contact) {
            //不用处理
        }

        override fun onRemove(contact: Contact) {
            if(contact.account==toAccount){
                ToastUtils.show("对方异常退出")
                leave()
            }
        }

        override fun onGetAll(allContactList: MutableList<Contact>) {
            //不用处理
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_call_send)

        tvNickname.text = toAccount

        //主叫方点击取消按钮
        tvCancel.setOnClickListener {
            SignalManager.cancel(CancelRequest(roomId, toAccount))
            finish()
        }

        //主叫方挂断
        tvHangup.setOnClickListener {
            leave()
        }

        //切换摄像头
        ivSwitchCamera.setOnClickListener {
            switchCamera()
        }

        init()

        WebSocketManager.addISignalOutgoingListener(iSignalOutgoingListener)
        WebSocketManager.addIContactsListener(iContactsListener)
        SignalManager.create(CreateRoomRequest())


    }

    private fun leave() {
        SignalManager.leave(LeaveRequest(roomId, toAccount))
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        WebSocketManager.removeISignalOutgoingListener(iSignalOutgoingListener)
        WebSocketManager.removeIContactsListener(iContactsListener)
        tvDuration.stopChatTime()
    }

    private fun createOffer() {
        peerConnection.createOffer(object : CreateSdpObserver() {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                BLog.i(
                    "--->createOffer()--->onCreateSuccess(),description:${sessionDescription.description},type:${sessionDescription.type}",
                    TAG
                )
                setLocalSdp(sessionDescription)
            }

            override fun onCreateFailure(errorMsg: String) {
                BLog.e("--->createOffer()--->onCreateFailure(),errorMsg:${errorMsg}", TAG)
            }

        }, sdpMediaConstraints)
    }

    fun setRemoteSdp(sessionDescription: SessionDescription) {
        peerConnection.setRemoteDescription(object : SetSdpObserver() {
            override fun onSetSuccess() {
                BLog.i("--->setRemoteSdp()--->onSetSuccess()", TAG)
                drainCandidates()
            }

            override fun onSetFailure(errorMsg: String) {
                BLog.e("--->setRemoteSdp()--->onSetFailure(),${errorMsg}", TAG)
            }

        }, sessionDescription)
    }

    fun setLocalSdp(sessionDescription: SessionDescription) {
        peerConnection.setLocalDescription(object : SetSdpObserver() {
            override fun onSetSuccess() {
                BLog.i("--->setLocalSdp()--->onSetSuccess()", TAG)
                SignalManager.sdpOffer(
                    SdpOfferRequest(
                        roomId,
                        toAccount,
                        SdpBean(peerConnection.localDescription.description)
                    )
                )
            }

            override fun onSetFailure(errorMsg: String) {
                BLog.e("--->setLocalSdp()--->onSetFailure(),${errorMsg}", TAG)
            }
        }, sessionDescription)
    }
}