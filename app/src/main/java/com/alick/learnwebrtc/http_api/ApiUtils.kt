package com.alick.learnwebrtc.http_api

import com.alick.learnwebrtc.constant.Constant
import com.alick.learnwebrtc.utils.BLog
import com.alick.learnwebrtc.utils.JsonUtils
import com.alick.learnwebrtc.utils.MMKVUtils
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * @author 崔兴旺
 * @description
 * @date 2021/6/27 12:43
 */
class ApiUtils {

    companion object {
        val executor: ExecutorService = Executors.newSingleThreadExecutor()
        const val API_GET_ROOM_LIST = "/api/get-room-list"


        inline fun <reified Data> requestGet(
            url_path: String,
            crossinline onSuccess: ((Data?) -> Unit),
            crossinline onFail: ((String) -> Unit)
        ) {
            executor.execute {
                var bufferedReader: BufferedReader? = null
                var httpURLConnection: HttpURLConnection? = null
                try {//使用该地址创建一个 URL 对象
                    val url =
                        URL("http://" + MMKVUtils.getString(Constant.MMKV_KEY_USING_ADDRESS) + url_path)
                    //使用创建的URL对象的openConnection()方法创建一个HttpURLConnection对象
                    httpURLConnection = url.openConnection() as HttpURLConnection
                    //设置HttpURLConnection对象的参数
                    //设置请求方法为 GET 请求
                    httpURLConnection.requestMethod = "GET"
                    //使用输入流
                    httpURLConnection.doInput = true
                    //GET 方式，不需要使用输出流
                    httpURLConnection.doOutput = false
                    //设置超时
                    httpURLConnection.connectTimeout = 10000
                    httpURLConnection.readTimeout = 1000
                    //连接
                    httpURLConnection.connect()
                    //还有很多参数设置 请自行查阅
                    //连接后，创建一个输入流来读取response
                    bufferedReader =
                        BufferedReader(
                            InputStreamReader(
                                httpURLConnection.getInputStream(),
                                "utf-8"
                            )
                        )
                    var line: String?
                    val stringBuilder = StringBuilder()
                    //每次读取一行，若非空则添加至 stringBuilder
                    while (bufferedReader.readLine().also { line = it } != null) {
                        stringBuilder.append(line)
                    }
                    //读取所有的数据后，赋值给 response
                    val responseString: String = stringBuilder.toString().trim { it <= ' ' }
                    BLog.i("响应json:$responseString")
                    onSuccess(JsonUtils.parseJson2Bean(responseString, BaseApiResponse<Data>().javaClass).data)
                    //切换到ui线程更新ui
                } catch (e: Exception) {
                    e.printStackTrace()
                    onFail(e.message ?: "未知错误")
                } finally {
                    bufferedReader?.close()
                    httpURLConnection?.disconnect()
                }
            }

        }
    }
}