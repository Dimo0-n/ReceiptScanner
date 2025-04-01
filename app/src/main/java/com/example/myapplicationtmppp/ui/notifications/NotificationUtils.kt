package com.example.myapplicationtmppp.ui.notifications

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SmsManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.myapplicationtmppp.MainActivity
import com.example.myapplicationtmppp.R

class NotificationUtils(private val context: Context) {

    companion object {
        private const val CHANNEL_ID = "scanner_channel_id"
        private const val CHANNEL_NAME = "Scanner Notifications"
        private const val CHANNEL_DESCRIPTION = "Notifications for scanner events"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(title: String, message: String) {
        val sharedPreferences = context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)

        val pushEnabled = sharedPreferences.getBoolean("push_enabled", true)
        val emailEnabled = sharedPreferences.getBoolean("email_enabled", false)
        val smsEnabled = sharedPreferences.getBoolean("sms_enabled", false)

        // Save the notification first
        val fullNotification = "$title: $message"
        NotificationStorage.saveNotification(context, fullNotification)

        if (pushEnabled) {
            sendPushNotification(title, message)
        }
        if (emailEnabled) {
            sendEmailNotification(title, message)
        }
        if (smsEnabled) {
            sendSmsNotification(title, message)
        }
    }

    private fun sendPushNotification(title: String, message: String) {
        val notificationId = System.currentTimeMillis().toInt()
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }


    private fun sendEmailNotification(title: String, message: String) {
        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf("dumitrufrimu118@gmail.com")) // Înlocuiește cu adresa reală
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, message)
        }
        try {
            context.startActivity(Intent.createChooser(emailIntent, "Trimite email..."))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun sendSmsNotification(title: String, message: String) {
        val phoneNumber = "078008086" // Înlocuiește cu numărul real

        // Verifică permisiunea înainte de a trimite SMS
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            if (context is Activity) {
                ActivityCompat.requestPermissions(context, arrayOf(Manifest.permission.SEND_SMS), 101)
            }
            return
        }

        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNumber, null, "$title: $message", null, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}
