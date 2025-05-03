package com.example.myapplicationtmppp.ui.notifications

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplicationtmppp.R

class SettingsActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var checkPush: CheckBox
    private lateinit var checkEmail: CheckBox
    private lateinit var checkSMS: CheckBox
    private lateinit var emailEditText: EditText  // Câmp pentru email

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_settings)

        sharedPreferences = getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)

        // Legătura cu UI
        checkPush = findViewById(R.id.check_push_notifications)
        checkEmail = findViewById(R.id.check_email_notifications)
        checkSMS = findViewById(R.id.check_sms_notifications)
        emailEditText = findViewById(R.id.editTextEmail)  // Câmpul de email
        val buttonSave: Button = findViewById(R.id.button_save_notifications)

        // Încărcăm setările salvate
        loadSettings()

        // Salvăm setările când utilizatorul apasă pe buton
        buttonSave.setOnClickListener {
            saveSettings()
        }
    }

    private fun loadSettings() {
        // Încarcă preferințele de notificare
        checkPush.isChecked = sharedPreferences.getBoolean("push_enabled", true)
        checkEmail.isChecked = sharedPreferences.getBoolean("email_enabled", false)
        checkSMS.isChecked = sharedPreferences.getBoolean("sms_enabled", false)

        // Încarcă email-ul salvat
        val savedEmail = sharedPreferences.getString("userEmail", "")
        emailEditText.setText(savedEmail)
    }

    private fun saveSettings() {
        // Salvăm preferințele de notificare
        val editor = sharedPreferences.edit()
        editor.putBoolean("push_enabled", checkPush.isChecked)
        editor.putBoolean("email_enabled", checkEmail.isChecked)
        editor.putBoolean("sms_enabled", checkSMS.isChecked)

        // Salvăm email-ul introdus
        editor.putString("userEmail", emailEditText.text.toString())

        // Aplicăm modificările
        editor.apply()

        // Afișăm un mesaj de confirmare
        Toast.makeText(this, "Setările au fost salvate!", Toast.LENGTH_SHORT).show()
    }
}
