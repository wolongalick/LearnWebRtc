package com.alick.learnwebrtc.manager

import android.content.Intent
import com.alick.learnwebrtc.BaseVideoCallActivity
import com.alick.learnwebrtc.VideoCallReceiveActivity
import com.alick.learnwebrtc.bean.Contact
import com.alick.learnwebrtc.bean.RoomBean
import com.alick.learnwebrtc.bean.request.base.BaseRequest
import com.alick.learnwebrtc.bean.response.*
import com.alick.learnwebrtc.bean.response.base.BaseResponse
import com.alick.learnwebrtc.constant.ResponseType
import com.alick.learnwebrtc.utils.AppHolder
import com.alick.learnwebrtc.utils.BLog
import com.alick.learnwebrtc.utils.JsonUtils
import com.alick.learnwebrtc.utils.ToastUtils
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.webrtc.IceCandidate
import java.net.URI

object WebSocketManager {
    const val TAG = "WebSocketManager"


    interface IWebSocketListener {
        fun onOpen(serverHandshake: ServerHandshake)
        fun onClose(code: Int, reason: String, remote: Boolean)
        fun onMessage(message: String)
        fun onError(exception: Exception)
    }

    interface IContactsListener {
        fun onAdd(contact: Contact)
        fun onRemove(contact: Contact)
        fun onGetAll(allContactList: MutableList<Contact>)
    }

    interface IBaseSignalListener {
        /**
         * 被叫方加入房间成功
         */
        fun onJoinSuccess()

        fun onIceCandidateFromRemote(candidate: IceCandidate)

        fun onIceCandidatesRemovedFromRemote(candidates: MutableList<IceCandidate>)

        /**
         * 对方离开房间
         */
        fun onLeave()
    }

    /**
     * 主叫方的监听
     */
    interface ISignalOutgoingListener : IBaseSignalListener {
        /**
         * 创建房间成功
         */
        fun onCreateSuccess(roomBean: RoomBean)

        /**
         * 对方已响铃
         */
        fun onRing()

        /**
         * 对方拒接
         */
        fun onReject()

        /**
         * 收到来自被叫方的sdp
         */
        fun onSdpAnswer(sdpAnswerResponse: SdpAnswerResponse)

    }

    /**
     * 被叫叫方的监听
     */
    interface ISignalIncomingListener : IBaseSignalListener {
        fun onCancel()

        /**
         * 收到来自主叫方的sdp
         */
        fun onSdpOffer(sdpOfferResponse: SdpOfferResponse)
    }

    private val iWebSocketListenerList: MutableList<IWebSocketListener> = mutableListOf()
    private val iContactsListenerList: MutableList<IContactsListener> = mutableListOf()
    private val iSignalOutgoingListenerList: MutableList<ISignalOutgoingListener> = mutableListOf()
    private val iSignalIncomingListenerList: MutableList<ISignalIncomingListener> = mutableListOf()

    private lateinit var webSocketClient: WebSocketClient

    fun init(url: String, account: String) {
        webSocketClient = object : WebSocketClient(URI("ws://${url}/${account}")) {
            override fun onOpen(handshakedata: ServerHandshake) {
                iWebSocketListenerList.forEach {
                    it.onOpen(handshakedata)
                }
            }

            override fun onClose(code: Int, reason: String, remote: Boolean) {
                BLog.i("--->onClose,code:${code},reason:${reason},remote:${remote}")
                iWebSocketListenerList.forEach {
                    it.onClose(code, reason, remote)
                }
            }

            override fun onMessage(message: String) {
                BLog.i("接收的消息:${message}", TAG)
                iWebSocketListenerList.forEach {
                    it.onMessage(message)
                }

                handleMessage(message)
            }

            override fun onError(exception: Exception) {
                BLog.e("--->onError,exception:${exception.message}", TAG)
                iWebSocketListenerList.forEach {
                    it.onError(exception)
                }
            }
        }
    }

    private fun handleMessage(message: String) {
        val baseResponse = JsonUtils.parseJson2Bean(message, BaseResponse::class.java)
        when (baseResponse.msgType) {
            ResponseType.ADD_CONTACT -> {
                iContactsListenerList.forEach { listener ->
                    val contact = JsonUtils.parseJson2Bean(
                        message,
                        AddContactResponse::class.java
                    ).data
                    contact?.let {
                        if (it.account != AccountManager.getAccount()) {
                            listener.onAdd(
                                it
                            )
                        }
                    }

                }
            }
            ResponseType.ALL_CONTACT -> {
                val allContactList = JsonUtils.parseJson2Bean(
                    message,
                    AllContactResponse::class.java
                )!!.data!!.toMutableList()
                iContactsListenerList.forEach { listener ->
                    listener.onGetAll(
                        allContactList
                    )
                }
            }
            ResponseType.REMOVE_CONTACT -> {
                val contact = JsonUtils.parseJson2Bean(
                    message,
                    RemoveContactResponse::class.java
                ).data!!
                iContactsListenerList.forEach {
                    it.onRemove(
                        contact
                    )
                }
            }
            ResponseType.CREATE_SUCCESS -> {
                val roomBean = JsonUtils.parseJson2Bean(
                    message,
                    CreateRoomResponse::class.java
                ).data!!
                iSignalOutgoingListenerList.forEach {
                    it.onCreateSuccess(roomBean)
                }
            }
            ResponseType.INVITE -> {
                val intent = Intent(AppHolder.getApp(), VideoCallReceiveActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.putExtra(BaseVideoCallActivity.INTENT_KEY_ROOM_ID, baseResponse.roomId)
                intent.putExtra(
                    BaseVideoCallActivity.INTENT_KEY_FROM_ACCOUNT,
                    baseResponse.fromAccount
                )
                AppHolder.getApp().startActivity(intent)
            }
            ResponseType.CANCEL -> {
                iSignalIncomingListenerList.forEach {
                    it.onCancel()
                }
            }
            ResponseType.RING -> {
                iSignalOutgoingListenerList.forEach {
                    it.onRing()
                }
            }
            ResponseType.REJECT -> {
                iSignalOutgoingListenerList.forEach {
                    it.onReject()
                }
            }
            ResponseType.JOIN_SUCCESS -> {
                iSignalOutgoingListenerList.forEach {
                    it.onJoinSuccess()
                }
                iSignalIncomingListenerList.forEach {
                    it.onJoinSuccess()
                }
            }
            ResponseType.SDP_OFFER -> {
                val sdpOfferResponse = JsonUtils.parseJson2Bean(
                    message,
                    SdpOfferResponse::class.java
                )
                iSignalIncomingListenerList.forEach {
                    it.onSdpOffer(sdpOfferResponse)
                }
            }
            ResponseType.SDP_ANSWER -> {
                val sdpAnswerResponse = JsonUtils.parseJson2Bean(
                    message,
                    SdpAnswerResponse::class.java
                )
                iSignalOutgoingListenerList.forEach {
                    it.onSdpAnswer(sdpAnswerResponse)
                }
            }
            ResponseType.CANDIDATE->{
                val iceCandidateResponse = JsonUtils.parseJson2Bean(message,IceCandidateResponse::class.java)
                iceCandidateResponse?.data?.let { iceCandidateBean ->
                    val candidate = IceCandidate(iceCandidateBean.id,iceCandidateBean.label,iceCandidateBean.candidate)
                    iSignalOutgoingListenerList.forEach {
                        it.onIceCandidateFromRemote(candidate)
                    }
                    iSignalIncomingListenerList.forEach {
                        it.onIceCandidateFromRemote(candidate)
                    }
                }
            }
            ResponseType.LEAVE->{
                iSignalOutgoingListenerList.forEach {
                    it.onLeave()
                }
                iSignalIncomingListenerList.forEach {
                    it.onLeave()
                }
            }


        }
    }

    fun addWebSocketListener(webSocketListener: IWebSocketListener) {
        if (!iWebSocketListenerList.contains(webSocketListener)) {
            iWebSocketListenerList.add(webSocketListener)
        }
    }

    fun removeWebSocketListener(webSocketListener: IWebSocketListener) {
        iWebSocketListenerList.remove(webSocketListener)
    }

    fun clearAllWebSocketListener() {
        iWebSocketListenerList.clear()
    }

    fun addIContactsListener(iContactsListener: IContactsListener) {
        if (!iContactsListenerList.contains(iContactsListener)) {
            iContactsListenerList.add(iContactsListener)
        }
    }

    fun removeIContactsListener(iContactsListener: IContactsListener) {
        iContactsListenerList.remove(iContactsListener)
    }

    fun clearAllIContactsListener() {
        iContactsListenerList.clear()
    }

    fun addISignalOutgoingListener(iSignalListener: ISignalOutgoingListener) {
        iSignalOutgoingListenerList.add(iSignalListener)
    }

    fun removeISignalOutgoingListener(iSignalListener: ISignalOutgoingListener) {
        iSignalOutgoingListenerList.remove(iSignalListener)
    }

    fun clearISignalOutgoingListener() {
        iSignalOutgoingListenerList.clear()
    }

    fun addISignalIncomingListener(iSignalListener: ISignalIncomingListener) {
        iSignalIncomingListenerList.add(iSignalListener)
    }

    fun removeISignalIncomingListener(iSignalListener: ISignalIncomingListener) {
        iSignalIncomingListenerList.remove(iSignalListener)
    }

    fun clearISignalIncomingListener() {
        iSignalIncomingListenerList.clear()
    }

    fun connect() {
        webSocketClient.connect()
    }

    fun close() {
        webSocketClient.close()
    }

    fun sendMsg(baseRequest: BaseRequest) {
        if (webSocketClient.isClosed) {
            ToastUtils.show("socket已关闭")
            return
        }
        if (webSocketClient.isClosing) {
            ToastUtils.show("socket正在关闭")
            return
        }
        try {
            val json = JsonUtils.parseBean2json(baseRequest)
            BLog.i("发送的消息:${json}", TAG)
            webSocketClient.send(json)
        } catch (e: Exception) {
            e.printStackTrace()
            ToastUtils.show("发送失败:${e.message}")
        }
    }

}