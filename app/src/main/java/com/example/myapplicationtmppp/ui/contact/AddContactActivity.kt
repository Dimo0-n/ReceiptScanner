package com.example.myapplicationtmppp.ui.contact

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplicationtmppp.R

class AddContactActivity : AppCompatActivity() {

    private lateinit var contactManager: ContactManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_add_contact)

        contactManager = ContactManager(this)

        val nameEditText: EditText = findViewById(R.id.inputNume)
        val emailEditText: EditText = findViewById(R.id.inputEmail)
        val saveButton: Button = findViewById(R.id.saveButton)

        saveButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val email = emailEditText.text.toString()

            if (name.isNotEmpty() && email.isNotEmpty()) {
                val newContact = Contact(name, email)

                val contacts = contactManager.loadContacts().toMutableList()
                contacts.add(newContact)
                contactManager.saveContacts(contacts)

                Toast.makeText(this, "Contact adăugat cu succes!", Toast.LENGTH_SHORT).show()

                // Trimite rezultat către activitatea precedentă
                val resultIntent = Intent()
                resultIntent.putExtra("contact_added", true)
                setResult(Activity.RESULT_OK, resultIntent)

                finish()
            } else {
                Toast.makeText(this, "Completează toate câmpurile", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
