package com.example.myapplicationtmppp.imageprocessing

import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

object QRScanner {

    fun scanQRCode(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val scanner = BarcodeScanning.getClient()

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    Log.d("QR Code", "Detected: ${barcode.rawValue}")
                }
            }
            .addOnFailureListener {
                Log.e("QR Code", "Failed to scan")
            }
    }
}
