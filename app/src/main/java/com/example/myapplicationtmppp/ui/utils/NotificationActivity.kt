package com.example.myapplicationtmppp.ui.notifications

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplicationtmppp.R
import com.example.myapplicationtmppp.ui.utils.NotificationStorage

class NotificationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.notification_list)

        val listView: ListView = findViewById(R.id.notification_list)
        val notifications = NotificationStorage.getNotifications(this)

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, notifications)
        listView.adapter = adapter
    }
}
