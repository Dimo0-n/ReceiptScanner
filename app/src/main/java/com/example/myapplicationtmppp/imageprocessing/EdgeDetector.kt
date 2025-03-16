package com.example.myapplicationtmppp.imageprocessing

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc

object EdgeDetector {

    fun detectEdges(bitmap: Bitmap): Bitmap {
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)

        // Convert to grayscale
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGBA2GRAY)

        // Apply Canny edge detection
        Imgproc.Canny(mat, mat, 50.0, 150.0)

        // Convert back to Bitmap
        val edgeBitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(mat, edgeBitmap)

        return edgeBitmap
    }
}
