package com.alick.learnwebrtc.constant

class RequestType {
    companion object {
        const val All_CONTACT: String = "All_CONTACT"
        const val CREATE: String = "CREATE"
        const val INVITE: String = "INVITE"
        const val CANCEL: String = "CANCEL"
        const val JOIN: String = "JOIN"
        const val RING: String = "RING"
        const val REJECT: String = "REJECT"
        const val SDP_OFFER: String = "SDP_OFFER"
        const val SDP_ANSWER: String = "SDP_ANSWER"
        const val CANDIDATE: String = "CANDIDATE"
        const val REMOVE_CANDIDATES: String = "REMOVE_CANDIDATES"
        const val LEAVE: String = "LEAVE"
    }
}

class ResponseType {
    companion object {
        const val ADD_CONTACT: String = "ADD_CONTACT"
        const val ALL_CONTACT: String = "ALL_CONTACT"
        const val REMOVE_CONTACT: String = "REMOVE_CONTACT"
        const val CREATE_SUCCESS: String = "CREATE_SUCCESS"
        const val PEERS: String = "PEERS"
        const val INVITE: String = "INVITE"
        const val CANCEL: String = "CANCEL"
        const val RING: String = "RING"
        const val REJECT: String = "REJECT"
        const val JOIN_SUCCESS: String = "JOIN_SUCCESS"
        const val SDP_OFFER: String = "SDP_OFFER"
        const val SDP_ANSWER: String = "SDP_ANSWER"
        const val CANDIDATE: String = "CANDIDATE"
        const val REMOVE_CANDIDATES: String = "REMOVE_CANDIDATES"
        const val LEAVE: String = "LEAVE"
    }
}