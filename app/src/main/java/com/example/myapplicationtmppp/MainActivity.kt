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
    }
}
