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
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.Normalizer.normalize

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
        try {
            ocrProcessor = OCRProcessor(requireContext())
        } catch (e: RuntimeException) {
            Toast.makeText(requireContext(), "Eroare la inițializarea OCR: ${e.message}", Toast.LENGTH_LONG).show()
            return view
        }

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

                // Redimensionează imaginea la 320x320 pixeli înainte de procesare
                val resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, 320, 320, true)

                // Salvăm imaginea și procesează-o
                try {
                    val imageText = ocrProcessor.processImage(resizedBitmap)
                    Toast.makeText(requireContext(), "Text extracted: $imageText", Toast.LENGTH_LONG).show()

                    // Trimite textul extras într-o nouă activitate
                    val intent = Intent(requireContext(), ScanResultActivity::class.java).apply {
                        putExtra("EXTRACTED_TEXT", imageText)
                    }
                    startActivity(intent)
                } catch (e: RuntimeException) {
                    Toast.makeText(requireContext(), "Eroare la procesarea imaginii: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(requireContext(), "Eroare: Imaginea capturată este null!", Toast.LENGTH_SHORT).show()
            }
        }
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
    private val interpreter: Interpreter

    init {
        try {
            // Încarcă modelul TFLite din folderul assets
            val model = FileUtil.loadMappedFile(context, "1.tflite")  // Asigură-te că folosești numele corect al fișierului
            Log.d("OCRProcessor", "Model loaded successfully")
            interpreter = Interpreter(model)
        } catch (e: IOException) {
            Log.e("OCRProcessor", "Error loading model: ${e.message}")
            throw RuntimeException("Eroare la încărcarea modelului TFLite", e)
        } catch (e: Exception) {
            Log.e("OCRProcessor", "Unexpected error: ${e.message}")
            throw RuntimeException("Eroare neașteptată", e)
        }
    }

    fun processImage(bitmap: Bitmap): String {
        try {
            // Redimensionează imaginea la 320x320 pixeli
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 320, 320, true)

            // Creează un buffer pentru imaginea normalizată
            val inputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, 320, 320, 3), DataType.FLOAT32)

            // Convertim imaginea Bitmap într-un array de float-uri
            val floatArray = FloatArray(320 * 320 * 3)
            var pixelIndex = 0

            // Convertim fiecare pixel în valoare RGB normalizată
            for (y in 0 until 320) {
                for (x in 0 until 320) {
                    val pixel = resizedBitmap.getPixel(x, y)
                    val r = ((pixel shr 16) and 0xFF) / 255.0f // Normalizare R
                    val g = ((pixel shr 8) and 0xFF) / 255.0f  // Normalizare G
                    val b = (pixel and 0xFF) / 255.0f          // Normalizare B

                    // Adăugăm valorile normalizate în array-ul nostru
                    floatArray[pixelIndex++] = r
                    floatArray[pixelIndex++] = g
                    floatArray[pixelIndex++] = b
                }
            }

            val byteBuffer = ByteBuffer.allocateDirect(floatArray.size * 4)  // Fiecare float are 4 bytes
            byteBuffer.order(ByteOrder.nativeOrder())  // Setează ordinea octeților pentru platformă

            for (value in floatArray) {
                byteBuffer.putFloat(value)
            }

            inputBuffer.loadBuffer(byteBuffer)

            // Creează un buffer de ieșire pentru rezultate
            val outputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, 80, 80, 5), DataType.FLOAT32)

            // Rulează modelul TFLite
            interpreter.run(inputBuffer.buffer, outputBuffer.buffer.rewind())

            // Extrage și interpretează ieșirea
            return interpretOutput(outputBuffer)
        } catch (e: Exception) {
            Log.e("OCRProcessor", "Error processing image: ${e.message}")
            throw RuntimeException("Eroare la procesarea imaginii", e)
        }
    }

    private fun interpretOutput(outputBuffer: TensorBuffer): String {
        val outputArray = outputBuffer.floatArray  // Folosește floatArray pentru ieșirea modelului de tip float32
        val stringBuilder = StringBuilder()

        // Filtrare și interpretare pentru a transforma valorile numerice într-un text
        for (value in outputArray) {
            if (value > 0) {
                // Poți adăuga condiții suplimentare pentru a extrage doar valorile relevante
                stringBuilder.append(value.toString()).append(" ")
            }
        }

        // Întoarce textul procesat
        return stringBuilder.toString().trim()
    }


    fun close() {
        interpreter.close()
    }
}
