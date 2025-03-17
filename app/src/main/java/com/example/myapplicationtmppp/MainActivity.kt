package com.example.myapplicationtmppp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.myapplicationtmppp.databinding.ActivityMainBinding
import com.example.myapplicationtmppp.ui.notifications.GmailSender
import com.example.myapplicationtmppp.ui.notifications.NotificationActivity
import com.example.myapplicationtmppp.ui.notifications.NotificationSettingsActivity
import com.example.myapplicationtmppp.ui.notifications.NotificationUtils
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        // Cerere permisiuni
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), 101)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 102)
            }
        }

        val notificationUtils = NotificationUtils(this)
        val handler = Handler(Looper.getMainLooper())

        // Trimite o notificare push după 1 minut
        handler.postDelayed({
            notificationUtils.showNotification("Notificare Push", "Aceasta este o notificare push.")
        }, 60_000)

        // Trimite o notificare prin email după 1 minut
        handler.postDelayed({
            sendEmailAfterDelay()
        }, 60_000)

        // Trimite o notificare prin SMS după 3 minute
        handler.postDelayed({
            notificationUtils.showNotification("Notificare SMS", "Aceasta este o notificare prin SMS.")
        }, 180_000)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_home, R.id.nav_gallery, R.id.nav_scanner), drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    private fun sendEmailAfterDelay() {
        // Inițializează GmailSender
        val gmailSender = GmailSender("dumitrufrimu118@gmail.com", "tusgczrlcofqvzic")

        // Detalii email
        val toEmail = "dumitrufrimu.r@gmail.com"
        val subject = "Test Email"
        val body = "Acesta este un email de test trimis din aplicația mea Android."

        // Trimite emailul pe un fir de execuție separat
        Thread {
            val isEmailSent = gmailSender.sendEmail(toEmail, subject, body)

            // Afișează rezultatul pe firul principal (UI thread)
            runOnUiThread {
                if (isEmailSent) {
                    Toast.makeText(this, "✅ Email trimis cu succes!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "❌ Trimiterea emailului a eșuat.", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_notification_settings -> {
                val intent = Intent(this, NotificationSettingsActivity::class.java)
                startActivity(intent)
                true
            }

            R.id.notification_list -> {
                startActivity(Intent(this, NotificationActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permisiunea pentru SMS a fost acordată!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permisiunea pentru SMS a fost refuzată!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}