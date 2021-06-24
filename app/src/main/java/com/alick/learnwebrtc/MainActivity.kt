package com.alick.learnwebrtc

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.alick.learnwebrtc.constant.Constant
import com.alick.learnwebrtc.manager.WebSocketManager
import com.alick.learnwebrtc.utils.BLog
import com.alick.learnwebrtc.utils.MMKVUtils
import kotlinx.android.synthetic.main.activity_main.*
import org.java_websocket.handshake.ServerHandshake
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions


class MainActivity : AppCompatActivity() {
    companion object {
        private const val REQUEST_CODE: Int = 1000
    }

    private val webSocketListener by lazy {
        object : WebSocketManager.IWebSocketListener {
            override fun onOpen(serverHandshake: ServerHandshake) {
                startActivity(Intent(this@MainActivity, ContactsActivity::class.java))
            }

            override fun onClose(code: Int, reason: String, remote: Boolean) {

            }

            override fun onMessage(message: String) {

            }

            override fun onError(exception: Exception) {

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etAddress.setText(MMKVUtils.getString(Constant.MMKV_KEY_ADDRESS))
        etAccount.setText(MMKVUtils.getString(Constant.MMKV_KEY_ACCOUNT))


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), REQUEST_CODE
            )
        }
    }

    fun login(view: View) {
        val address = etAddress.text.toString().trim()
        val account = etAccount.text.toString().trim()
        MMKVUtils.setString(Constant.MMKV_KEY_ADDRESS, address)
        MMKVUtils.setString(Constant.MMKV_KEY_ACCOUNT, account)

        WebSocketManager.init(address, account)
        WebSocketManager.addWebSocketListener(webSocketListener)
        WebSocketManager.connect()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    @AfterPermissionGranted(REQUEST_CODE)
    private fun methodRequiresTwoPermission() {
        val perms = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,

            )
        if (EasyPermissions.hasPermissions(this, *perms)) {
            // Already have permission, do the thing
            // ...
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, "音视频通话需要获取摄像头和录音权限", REQUEST_CODE, *perms)
        }
    }

    override fun onDestroy() {
        WebSocketManager.clearAllWebSocketListener()
        super.onDestroy()
    }
}