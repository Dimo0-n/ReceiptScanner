package com.example.myapplicationtmppp.ui.scanner

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.myapplicationtmppp.R
import com.example.myapplicationtmppp.ScanResultActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File

class ScannerFragment : Fragment() {

    private lateinit var imageView: ImageView
    private lateinit var ocrProcessor: OCRProcessor

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.scanner_layout, container, false)

        val buttonScan: Button = view.findViewById(R.id.button_scan)
        val buttonRecentsScan: Button = view.findViewById(R.id.button_recents_scan)
        imageView = view.findViewById(R.id.imageViewPreview)

        // Inițializează procesorul OCR
        ocrProcessor = OCRProcessor(requireContext())

        buttonScan.setOnClickListener {
            openCamera()
        }

        buttonRecentsScan.setOnClickListener {
            Toast.makeText(requireContext(), "Opening recent scans...", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun openCamera() {
        if (!isAdded) return

        if (checkCameraPermission()) {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent.resolveActivity(requireContext().packageManager) != null) {
                cameraLauncher.launch(takePictureIntent)
            } else {
                Toast.makeText(requireContext(), "Camera not available", Toast.LENGTH_SHORT).show()
            }
        } else {
            requestCameraPermission()
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            if (imageBitmap != null) {
                imageView.setImageBitmap(imageBitmap)

                // Redimensionează și preprocesează imaginea
                val resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, 640, 640, true)
                val processedBitmap = ocrProcessor.adjustContrast(resizedBitmap, 1.5f)  // Ajustează contrastul

                // Salvează imaginea pe disc (într-un fișier temporar)
                val imagePath = saveBitmapToFile(processedBitmap)

                // Procesează imaginea și extrage textul
                ocrProcessor.processImage(
                    processedBitmap,
                    onSuccess = { extractedText ->
                        // Trimite textul extras și calea imaginii într-o nouă activitate
                        val intent = Intent(requireContext(), ScanResultActivity::class.java).apply {
                            putExtra("EXTRACTED_TEXT", extractedText)
                            putExtra("IMAGE_PATH", imagePath)  // Trimite calea imaginii
                        }
                        startActivity(intent)
                    },
                    onFailure = { e ->
                        // Afișează eroarea
                        Toast.makeText(requireContext(), "Eroare la procesarea imaginii: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                )
            } else {
                Toast.makeText(requireContext(), "Eroare: Imaginea capturată este null!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Salvarea imaginii într-un fișier temporar
    private fun saveBitmapToFile(bitmap: Bitmap): String {
        val file = File(requireContext().cacheDir, "scanned_image.png")
        val fos = file.outputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos.close()
        return file.absolutePath
    }


    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
    }
}

class OCRProcessor(context: Context) {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    fun processImage(bitmap: Bitmap, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        // Creează un obiect InputImage din Bitmap
        val image = InputImage.fromBitmap(bitmap, 0)

        // Procesează imaginea și extrage textul
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                // Textul a fost recunoscut cu succes
                val extractedText = visionText.text
                onSuccess(extractedText)
            }
            .addOnFailureListener { e ->
                // A apărut o eroare la recunoașterea textului
                onFailure(e)
            }
    }

    fun adjustContrast(bitmap: Bitmap, contrast: Float): Bitmap {
        val adjustedBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config!!)
        val canvas = Canvas(adjustedBitmap)
        val paint = Paint()
        val matrix = ColorMatrix().apply {
            setScale(contrast, contrast, contrast, 1f)  // Ajustează contrastul
        }
        paint.colorFilter = ColorMatrixColorFilter(matrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return adjustedBitmap
    }

    fun close() {
        recognizer.close()
    }
}