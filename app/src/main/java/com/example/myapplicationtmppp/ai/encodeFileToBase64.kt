package com.example.myapplicationtmppp.ai

import android.os.Build
import androidx.annotation.RequiresApi
import java.io.File
import java.io.FileInputStream
import java.util.Base64

@RequiresApi(Build.VERSION_CODES.O)
fun encodeFileToBase64(file: File): String {
    val fileInputStream = FileInputStream(file)
    val bytes = fileInputStream.readBytes()
    return Base64.getEncoder().encodeToString(bytes)
}