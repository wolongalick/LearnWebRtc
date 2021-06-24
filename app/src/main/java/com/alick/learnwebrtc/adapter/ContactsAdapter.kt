package com.alick.learnwebrtc.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alick.learnwebrtc.R
import com.alick.learnwebrtc.bean.Contact
import com.alick.learnwebrtc.manager.AccountManager
import kotlinx.android.synthetic.main.item_contact.view.*

class ContactsAdapter(private val list: MutableList<Contact>) :
    RecyclerView.Adapter<ContactViewHolder>() {

    var onClickVideo: ((Contact) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        return ContactViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = list[position]
        holder.itemView.tvAccount.text = contact.account

        if (contact.account == AccountManager.getAccount()) {
            holder.itemView.btnVideo.visibility = View.GONE
        } else {
            holder.itemView.btnVideo.visibility = View.VISIBLE
            holder.itemView.btnVideo.setOnClickListener {
                onClickVideo?.invoke(contact)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

}

class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)