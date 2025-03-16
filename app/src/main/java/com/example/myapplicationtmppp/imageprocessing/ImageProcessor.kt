package com.example.myapplicationtmppp.imageprocessing

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc

object ImageProcessor {

    fun preprocessImage(bitmap: Bitmap): Bitmap {
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)

        // Convert to grayscale
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGBA2GRAY)

        // Adjust contrast and brightness
        Core.normalize(mat, mat, 0.0, 255.0, Core.NORM_MINMAX)

        // Reduce noise using Gaussian blur
        Imgproc.GaussianBlur(mat, mat, Size(5.0, 5.0), 0.0)

        // Deskewing (Hough Line Transform for angle correction)
        deskewImage(mat)

        // Convert back to Bitmap
        val processedBitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(mat, processedBitmap)

        return processedBitmap
    }

    private fun deskewImage(mat: Mat) {
        val edges = Mat()
        Imgproc.Canny(mat, edges, 50.0, 150.0)

        val lines = Mat()
        Imgproc.HoughLines(edges, lines, 1.0, Math.PI / 180, 100)

        var angle = 0.0
        for (i in 0 until lines.rows()) {
            val rhoTheta = lines.get(i, 0)
            val theta = rhoTheta[1]
            angle += Math.toDegrees(theta - Math.PI / 2)
        }
        angle /= lines.rows()

        val rotationMatrix = Imgproc.getRotationMatrix2D(Point(mat.cols() / 2.0, mat.rows() / 2.0), angle, 1.0)
        Imgproc.warpAffine(mat, mat, rotationMatrix, mat.size())
    }
}
