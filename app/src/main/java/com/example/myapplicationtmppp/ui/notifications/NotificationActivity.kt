package com.example.myapplicationtmppp.ui.notifications

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplicationtmppp.R

// NotificationActivity.kt
class NotificationActivity : AppCompatActivity() {

    private lateinit var adapter: ArrayAdapter<String>
    private val notifications = mutableListOf<String>() // Listă mutabilă locală

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.notification_list)

        val listView: ListView = findViewById(R.id.notification_list)

        // Inițializează lista cu datele din storage (convertite la mutabile)
        notifications.addAll(NotificationStorage.getNotifications(this).reversed())

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, notifications)
        listView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        // Actualizează lista locală și notifică adapterul
        notifications.clear()
        notifications.addAll(NotificationStorage.getNotifications(this).reversed())
        adapter.notifyDataSetChanged() // Actualizează UI-ul
    }
}