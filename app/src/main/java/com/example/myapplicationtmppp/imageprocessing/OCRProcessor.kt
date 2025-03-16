package com.example.myapplicationtmppp.imageprocessing

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.googlecode.tesseract.android.TessBaseAPI

object OCRProcessor {

    fun recognizeText(bitmap: Bitmap, context: Context): String {
        val tess = TessBaseAPI()
        val dataPath = context.filesDir.absolutePath + "/tesseract/"

        tess.init(dataPath, "eng") // Load English language model
        tess.setImage(bitmap)

        val extractedText = tess.utF8Text
        tess.end()

        Log.d("OCR Result", extractedText)
        return extractedText
    }
}
