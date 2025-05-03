package com.example.myapplicationtmppp.ui.contact

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplicationtmppp.R

class ContactsActivity : AppCompatActivity() {

    private lateinit var contactManager: ContactManager
    private lateinit var contactsRecyclerView: RecyclerView
    private lateinit var contactsAdapter: ContactsAdapter
    private lateinit var contacts: MutableList<Contact>

    // Launcher pentru AddContactActivity cu refresh la întoarcere
    private val addContactLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            contacts.clear()
            contacts.addAll(contactManager.loadContacts())
            contactsAdapter.notifyDataSetChanged()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

        contactManager = ContactManager(this)
        contactsRecyclerView = findViewById(R.id.contactRecyclerView)

        // Încarcă contactele inițiale
        contacts = contactManager.loadContacts().toMutableList()

        contactsAdapter = ContactsAdapter(contacts) { contactToDelete ->
            contacts.remove(contactToDelete)
            contactManager.saveContacts(contacts)
            contactsAdapter.notifyDataSetChanged()
        }

        contactsRecyclerView.layoutManager = LinearLayoutManager(this)
        contactsRecyclerView.adapter = contactsAdapter

        // Butonul de "+" pentru adăugare contact
        val addContactButton: ImageButton = findViewById(R.id.addContactButton)
        addContactButton.setOnClickListener {
            val intent = Intent(this, AddContactActivity::class.java)
            addContactLauncher.launch(intent)
        }
    }
}
