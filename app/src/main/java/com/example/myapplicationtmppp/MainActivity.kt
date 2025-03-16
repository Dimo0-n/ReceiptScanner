package com.example.myapplicationtmppp.ui

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplicationtmppp.imageprocessing.*
import com.example.myapplicationtmppp.imageprocessing.OpenCVInitializer
import com.example.myapplicationtmppp.R


class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var btnProcess: Button
    private var capturedBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        OpenCVInitializer.initialize()

        imageView = findViewById(R.id.imageView)
        btnProcess = findViewById(R.id.btnProcess)
        
        btnProcess.setOnClickListener {
            capturedBitmap?.let { bitmap ->
                val processedBitmap = ImageProcessor.preprocessImage(bitmap)
                imageView.setImageBitmap(processedBitmap)
                OCRProcessor.recognizeText(processedBitmap, this)
            }
        }

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_home, R.id.nav_gallery, R.id.nav_scanner), drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
