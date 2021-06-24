package com.alick.learnwebrtc

import android.os.Bundle
import android.view.View
import com.alick.learnwebrtc.bean.Contact
import com.alick.learnwebrtc.bean.SdpBean
import com.alick.learnwebrtc.bean.request.*
import com.alick.learnwebrtc.bean.response.SdpOfferResponse
import com.alick.learnwebrtc.core.sdp.CreateSdpObserver
import com.alick.learnwebrtc.core.sdp.SetSdpObserver
import com.alick.learnwebrtc.manager.AccountManager
import com.alick.learnwebrtc.manager.SignalManager
import com.alick.learnwebrtc.manager.WebSocketManager
import com.alick.learnwebrtc.utils.BLog
import com.alick.learnwebrtc.utils.ToastUtils
import kotlinx.android.synthetic.main.activity_video_call_receive.*
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import org.webrtc.SurfaceViewRenderer

class VideoCallReceiveActivity : BaseVideoCallActivity() {
    companion object {
        const val TAG = "VideoCallReceiveActivity"
    }

    private val iSignalIncomingListener = object : WebSocketManager.ISignalIncomingListener {
        override fun onCancel() {
            ToastUtils.show("对方已取消")
            finish()
        }

        override fun onSdpOffer(sdpOfferResponse: SdpOfferResponse) {
            setRemoteSdp(SessionDescription(SessionDescription.Type.OFFER,sdpOfferResponse.data?.description))
            createAnswer()
        }

        override fun onJoinSuccess() {
            ToastUtils.show("接听成功")
            runOnUiThread {
                tvDuration.beginChatTime()
                llIncomingInfo.visibility = View.GONE
                tvReject.visibility = View.GONE
                tvAnswer.visibility = View.GONE
                ivCoverAvatar.visibility = View.GONE
                rlLocalVideoContainer.visibility = View.VISIBLE
                ivSwitchCamera.visibility = View.VISIBLE
                tvHangup.visibility = View.VISIBLE
            }
            init()
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
            if(contact.account==fromAccount){
                ToastUtils.show("对方异常退出")
                leave()
            }
        }

        override fun onGetAll(allContactList: MutableList<Contact>) {
            //不用处理
        }

    }

    override val fromAccount: String
        get() = intent.getStringExtra(INTENT_KEY_FROM_ACCOUNT) ?: ""

    override val toAccount: String
        get() = AccountManager.getAccount()

    override val mLocalRenderer: SurfaceViewRenderer
        get() = localRenderer

    override val mRemoteRenderer: SurfaceViewRenderer
        get() = remoteRenderer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_call_receive)

        roomId = intent.getStringExtra(INTENT_KEY_ROOM_ID) ?: ""

        tvIncomingNickname.text = fromAccount

        WebSocketManager.addISignalIncomingListener(iSignalIncomingListener)
        WebSocketManager.addIContactsListener(iContactsListener)

        SignalManager.ring(RingRequest(roomId, fromAccount))

        //被叫方拒接
        tvReject.setOnClickListener {
            SignalManager.reject(RejectRequest(roomId, fromAccount))
            finish()
        }

        //被叫方接听
        tvAnswer.setOnClickListener {
            SignalManager.join(JoinRequest(roomId, fromAccount))
        }

        //被叫方挂断
        tvHangup.setOnClickListener {
            leave()
        }
    }

    private fun leave() {
        SignalManager.leave(LeaveRequest(roomId, fromAccount))
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        WebSocketManager.removeISignalIncomingListener(iSignalIncomingListener)
        WebSocketManager.removeIContactsListener(iContactsListener)
        tvDuration.stopChatTime()
    }

    fun createAnswer(){
        peerConnection.createAnswer(object : CreateSdpObserver() {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                BLog.i("--->createAnswer()--->onCreateSuccess(),description:${sessionDescription.description},type:${sessionDescription.type}",TAG)
                setLocalSdp(sessionDescription)
            }

            override fun onCreateFailure(errorMsg: String) {
                BLog.e("--->createAnswer()--->onCreateFailure(),errorMsg:${errorMsg}", TAG)
            }

        },sdpMediaConstraints)
    }

    fun setRemoteSdp(sessionDescription: SessionDescription) {
        peerConnection.setRemoteDescription(object : SetSdpObserver() {
            override fun onSetSuccess() {
                BLog.i("--->setRemoteSdp()--->onSetSuccess()", TAG)
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
                SignalManager.sdpAnswer(SdpAnswerRequest(roomId,fromAccount, SdpBean(peerConnection.localDescription.description)))
                drainCandidates()
            }

            override fun onSetFailure(errorMsg: String) {
                BLog.e("--->setLocalSdp()--->onSetFailure(),${errorMsg}", TAG)
            }

        }, sessionDescription)
    }
}