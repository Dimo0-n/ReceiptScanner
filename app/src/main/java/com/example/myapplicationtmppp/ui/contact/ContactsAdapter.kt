package com.example.myapplicationtmppp.ui.contact

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplicationtmppp.R

class ContactsAdapter(
    private val contactList: MutableList<Contact>,
    private val onDeleteClick: (Contact) -> Unit
) : RecyclerView.Adapter<ContactsAdapter.ContactViewHolder>() {

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.contactName)
        val emailText: TextView = itemView.findViewById(R.id.contactEmail)
        val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contactList[position]
        holder.nameText.text = contact.name
        holder.emailText.text = contact.email

        holder.deleteButton.setOnClickListener {
            onDeleteClick(contact)
        }
    }

    override fun getItemCount(): Int = contactList.size
}
