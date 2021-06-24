package com.alick.learnwebrtc

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.alick.learnwebrtc.adapter.ContactsAdapter
import com.alick.learnwebrtc.bean.Contact
import com.alick.learnwebrtc.bean.request.GetAllContactRequest
import com.alick.learnwebrtc.manager.WebSocketManager
import com.alick.learnwebrtc.utils.FilterUtils
import kotlinx.android.synthetic.main.activity_contacts.*
import org.java_websocket.handshake.ServerHandshake


class ContactsActivity : AppCompatActivity() {

    private val allContactsList: MutableList<Contact> by lazy {
        mutableListOf()
    }

    private val contactsAdapter: ContactsAdapter by lazy {
        ContactsAdapter(allContactsList)
    }

    private val iContactsListener by lazy {
        object : WebSocketManager.IContactsListener {
            override fun onAdd(contact: Contact) {
                allContactsList.add(contact)
                runOnUiThread {
                    contactsAdapter.notifyDataSetChanged()
                }
            }

            override fun onRemove(contact: Contact) {
                FilterUtils.filterList(
                    allContactsList
                ) { model -> model.account == contact.account }
                runOnUiThread {
                    contactsAdapter.notifyDataSetChanged()
                }
            }

            override fun onGetAll(allContactList: MutableList<Contact>) {
                allContactsList.clear()
                allContactsList.addAll(allContactList)
                runOnUiThread {
                    contactsAdapter.notifyDataSetChanged()
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

        rvContacts.layoutManager = LinearLayoutManager(this)
        rvContacts.adapter = contactsAdapter

        contactsAdapter.onClickVideo = {
            val intent = Intent(this, VideoCallSendActivity::class.java)
            intent.putExtra(BaseVideoCallActivity.INTENT_KEY_TO_ACCOUNT, it.account)
            startActivity(intent)
        }

        WebSocketManager.addWebSocketListener(object : WebSocketManager.IWebSocketListener {
            override fun onOpen(serverHandshake: ServerHandshake) {
            }

            override fun onClose(code: Int, reason: String, remote: Boolean) {
                finish()
            }

            override fun onMessage(message: String) {
            }

            override fun onError(exception: Exception) {
                swipeRefreshLayout.isRefreshing = false
            }
        })

        WebSocketManager.addIContactsListener(iContactsListener)

        swipeRefreshLayout.setOnRefreshListener {
            loadContacts()
        }

        swipeRefreshLayout.isRefreshing = true
        loadContacts()

    }

    private fun loadContacts() {
        WebSocketManager.sendMsg(GetAllContactRequest())
    }

    fun back(view: View) {
        onBack()
    }

    override fun onBackPressed() {
        onBack()
    }

    private fun onBack() {
        val alertDialog: AlertDialog = AlertDialog.Builder(this)
            .setTitle("提示")
            .setMessage("确定要退出吗")
            .setNegativeButton("取消", null)
            .setPositiveButton("退出", object : DialogInterface.OnClickListener {
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    WebSocketManager.close()
                    finish()
                }
            })
            .create()
        alertDialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        WebSocketManager.removeIContactsListener(iContactsListener)
    }

}