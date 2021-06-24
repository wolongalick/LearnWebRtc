package com.alick.learnwebrtc.utils

import java.util.*

class RoomIdUtils {
    companion object{
        fun createRoomId():String{
            return UUID.randomUUID().toString()
        }
    }
}