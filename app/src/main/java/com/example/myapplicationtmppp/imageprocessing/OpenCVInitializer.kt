package com.example.myapplicationtmppp.imageprocessing

import android.util.Log
import org.opencv.android.OpenCVLoader

object OpenCVInitializer {
    fun initialize() {
        if (!OpenCVLoader.initDebug()) {
            Log.e("OpenCV", "OpenCV initialization failed!")
        } else {
            Log.d("OpenCV", "OpenCV initialized successfully.")
        }
    }
}
