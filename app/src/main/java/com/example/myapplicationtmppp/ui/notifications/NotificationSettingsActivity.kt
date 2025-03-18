package com.example.myapplicationtmppp.ui.notifications

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplicationtmppp.R

class NotificationSettingsActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var checkPush: CheckBox
    private lateinit var checkEmail: CheckBox
    private lateinit var checkSMS: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_settings)

        sharedPreferences = getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)

        checkPush = findViewById(R.id.check_push_notifications)
        checkEmail = findViewById(R.id.check_email_notifications)
        checkSMS = findViewById(R.id.check_sms_notifications)
        val buttonSave: Button = findViewById(R.id.button_save_notifications)

        // Încărcăm setările salvate
        loadSettings()

        buttonSave.setOnClickListener {
            saveSettings()
        }
    }

    private fun loadSettings() {
        checkPush.isChecked = sharedPreferences.getBoolean("push_enabled", true)
        checkEmail.isChecked = sharedPreferences.getBoolean("email_enabled", false)
        checkSMS.isChecked = sharedPreferences.getBoolean("sms_enabled", false)
    }

    private fun saveSettings() {
        val editor = sharedPreferences.edit()
        editor.putBoolean("push_enabled", checkPush.isChecked)
        editor.putBoolean("email_enabled", checkEmail.isChecked)
        editor.putBoolean("sms_enabled", checkSMS.isChecked)
        editor.apply()

        Toast.makeText(this, "Setările au fost salvate!", Toast.LENGTH_SHORT).show()
    }
}
