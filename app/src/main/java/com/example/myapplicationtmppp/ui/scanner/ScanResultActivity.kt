package com.example.myapplicationtmppp.ui.scanner

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplicationtmppp.R

class ScanResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_result)

        val imageView: ImageView = findViewById(R.id.imageViewResult)
        val textViewResults: TextView = findViewById(R.id.textViewResults)

        // Preluăm calea fișierului imagine trimis din ScannerFragment
        val imagePath = intent.getStringExtra("IMAGE_PATH")
        if (imagePath != null) {
            val imageUri = Uri.parse(imagePath) // Convertim calea înapoi în URI
            try {
                // Încarcă imaginea din URI
                val inputStream = contentResolver.openInputStream(imageUri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                imageView.setImageBitmap(bitmap)  // Afișează imaginea în ImageView
            } catch (e: Exception) {
                e.printStackTrace()
                imageView.setImageResource(R.drawable.placeholder)  // Afișează o imagine de rezervă în caz de eroare
            }
        } else {
            imageView.setImageResource(R.drawable.placeholder)  // Afișează o imagine de rezervă dacă calea este null
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