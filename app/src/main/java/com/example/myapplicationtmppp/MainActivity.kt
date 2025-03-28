package com.example.myapplicationtmppp

import android.Manifest
import android.content.Context
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
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.myapplicationtmppp.databinding.ActivityMainBinding
import com.example.myapplicationtmppp.expenses.MonthlyExpensesActivity
import com.example.myapplicationtmppp.ui.notifications.GmailSender
import com.example.myapplicationtmppp.ui.notifications.NotificationActivity
import com.example.myapplicationtmppp.ui.notifications.NotificationSettingsActivity
import com.example.myapplicationtmppp.ui.notifications.NotificationUtils
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    companion object {
        lateinit var appContext: Context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContext = applicationContext
        
        // Initialize view binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the toolbar
        setSupportActionBar(binding.appBarMain.toolbar)

        // Request permissions
        requestPermissions()

        // Set up navigation
        setupNavigation()

        // Set up notifications
        setupNotifications()

    }

    private fun setupExpensesButton() {
        // Access the button through the binding
        binding.appBarMain.btnViewExpenses.setOnClickListener {
            startActivity(Intent(this, MonthlyExpensesActivity::class.java))
        }
    }

    private fun requestPermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), 101)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    102
                )
            }
        }
    }

    private fun setupNavigation() {
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_home, R.id.nav_gallery, R.id.nav_scanner, R.id.nav_expenses),
            drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Add this to handle the expenses item click
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_expenses -> {
                    startActivity(Intent(this, MonthlyExpensesActivity::class.java))
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                else -> {
                    // Default navigation handling
                    menuItem.isChecked = true
                    drawerLayout.closeDrawer(GravityCompat.START)
                    navController.navigate(menuItem.itemId)
                    true
                }
            }
        }
    }

    private fun setupNotifications() {
        val notificationUtils = NotificationUtils(this)
        val handler = Handler(Looper.getMainLooper())

        // Send push notification after 1 minute
        handler.postDelayed({
            notificationUtils.showNotification("Notificare Push", "Aceasta este o notificare push.")
        }, 60_000)

        // Send email notification after 1 minute
        handler.postDelayed({
            sendEmailAfterDelay()
        }, 60_000)

        // Send SMS notification after 3 minutes
        handler.postDelayed({
            notificationUtils.showNotification(
                "Notificare SMS",
                "Aceasta este o notificare prin SMS."
            )
        }, 180_000)
    }

    private fun sendEmailAfterDelay() {
        val gmailSender = GmailSender("dumitrufrimu118@gmail.com", "tusgczrlcofqvzic")
        val toEmail = "dumitrufrimu.r@gmail.com"
        val subject = "Test Email"
        val body = "Acesta este un email de test trimis din aplicația mea Android."

        Thread {
            val isEmailSent = gmailSender.sendEmail(toEmail, subject, body)
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
                startActivity(Intent(this, NotificationSettingsActivity::class.java))
                true
            }
            R.id.notification_list -> {
                startActivity(Intent(this, NotificationActivity::class.java))
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
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