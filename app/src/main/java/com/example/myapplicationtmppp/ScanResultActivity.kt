package com.example.myapplicationtmppp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class ScanResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_result)

        val imageView: ImageView = findViewById(R.id.imageViewResult)
        val textViewResults: TextView = findViewById(R.id.textViewResults)

        // Preluăm calea fișierului imagine trimis din ScannerFragment (opțional)
        val imagePath = intent.getStringExtra("IMAGE_PATH")
        if (imagePath != null) {
            val imageFile = File(imagePath)
            if (imageFile.exists()) {
                val bitmap: Bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                imageView.setImageBitmap(bitmap)
            }
        }

        // Preluăm textul extras din intent și îl afișăm
        val extractedText = intent.getStringExtra("EXTRACTED_TEXT")
        if (extractedText != null) {
            textViewResults.text = extractedText  // Afișează textul extras
        } else {
            textViewResults.text = "Nu s-a extras niciun text."
        }
    }
}
