package com.alick.learnwebrtc.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import java.util.*

/**
 * @author 崔兴旺
 * @description
 * @date 2020/11/21 23:32
 */
class ChatTimeTextView(context: Context, attrs: AttributeSet?) : androidx.appcompat.widget.AppCompatTextView(context, attrs) {


    private var timerTask: TimerTask? = null
    private var timer: Timer? = null
    private var chatDuration: Long = 0L

    private val interval: Long = 1000L


    /**
     * 开始会话计时
     */
    fun beginChatTime() {
        visibility = View.VISIBLE
        if (timer == null) {
            timer = Timer()
        }
        if (timerTask == null) {
            timerTask = object : TimerTask() {
                override fun run() {
                    chatDuration += interval

                    val durationBySecond: Long = chatDuration / interval

                    var str = ""
                    if (durationBySecond < 10L) {
                        str = "00:0${durationBySecond}"
                    } else if (durationBySecond < 60L) {
                        str = "00:${durationBySecond}"
                    } else if (durationBySecond == 60L) {
                        str = "01:00"
                    } else {
                        val min = durationBySecond / 60
                        if (min < 10) {
                            str = "0${min}:"
                        } else if (min < 60) {
                            str = "${min}:"
                        }
                        val second = durationBySecond % 60
                        if (second < 10) {
                            str += "0${second}"
                        } else {
                            str += "$second"
                        }
                    }

                    post {
                        text = str
                    }
                }
            }
        }
        timer?.schedule(timerTask, interval, interval)
    }

    fun stopChatTime() {
        if (timer != null) {
            timer?.cancel()
        }
    }

}