package com.alick.learnwebrtc.manager

import com.alick.learnwebrtc.bean.request.*

object SignalManager {

    /**
     * 主叫方创建房间
     */
    fun create(createRoomRequest: CreateRoomRequest) {
        WebSocketManager.sendMsg(createRoomRequest)
    }

    /**
     * 主叫方邀请
     */
    fun invite(inviteRequest: InviteRequest) {
        WebSocketManager.sendMsg(inviteRequest)
    }

    /**
     * 主叫方取消邀请
     */
    fun cancel(cancelRequest: CancelRequest){
        WebSocketManager.sendMsg(cancelRequest)
    }

    /**
     * 被叫方响铃
     */
    fun ring(ringRequest: RingRequest){
        WebSocketManager.sendMsg(ringRequest)
    }

    /**
     * 被叫方拒接
     */
    fun reject(rejectRequest: RejectRequest){
        WebSocketManager.sendMsg(rejectRequest)
    }

    /**
     * 被叫方接听并加入房间
     */
    fun join(joinRequest: JoinRequest){
        WebSocketManager.sendMsg(joinRequest)
    }

    fun sdpOffer(sdpOfferRequest: SdpOfferRequest){
        WebSocketManager.sendMsg(sdpOfferRequest)
    }

    fun sdpAnswer(sdpAnswerRequest: SdpAnswerRequest){
        WebSocketManager.sendMsg(sdpAnswerRequest)
    }

    fun candidate(iceCandidateRequest: SendIceCandidateRequest){
        WebSocketManager.sendMsg(iceCandidateRequest)
    }

    fun removeCandidates(sendIceCandidateRemovedRequest: SendIceCandidateRemovedRequest){
        WebSocketManager.sendMsg(sendIceCandidateRemovedRequest)
    }

    /**
     * 主叫或被叫离开房间
     */
    fun leave(leaveRequest: LeaveRequest){
        WebSocketManager.sendMsg(leaveRequest)
    }

}