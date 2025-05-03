package com.example.myapplicationtmppp.ui.contact

import android.content.Context
import com.google.gson.Gson
import java.io.File
import java.io.FileWriter
import java.io.FileReader

class ContactManager(private val context: Context) {

    private val fileName = "contacts.json"

    // Salvează contactele în fișier
    fun saveContacts(contacts: List<Contact>) {
        val gson = Gson()
        val json = gson.toJson(contacts)
        val file = File(context.filesDir, fileName)
        file.writeText(json)
    }

    // Încarcă contactele din fișier
    fun loadContacts(): List<Contact> {
        val file = File(context.filesDir, fileName)
        if (file.exists()) {
            val json = file.readText()
            val gson = Gson()
            return gson.fromJson(json, Array<Contact>::class.java).toList()
        }
        return emptyList()  // Dacă nu există fișierul, returnează listă goală
    }
}
