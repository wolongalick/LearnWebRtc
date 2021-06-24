package com.alick.learnwebrtc.bean

data class RoomBean(val roomId: String,val creatorAccount:String) {
    var roomName: String = ""
    var roomSize: Int = 0
    val contactList: MutableList<Contact> = mutableListOf()
}
