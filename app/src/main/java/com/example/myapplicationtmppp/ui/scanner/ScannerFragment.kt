package com.example.myapplicationtmppp.ui.scanner

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.myapplicationtmppp.R
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScannerFragment : Fragment() {

    private lateinit var imageView: ImageView
    private lateinit var ocrProcessor: OCRProcessor
    private var imageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.scanner_layout, container, false)

        val buttonScan: Button = view.findViewById(R.id.button_scan)
        val buttonRecentsScan: Button = view.findViewById(R.id.gallery_button)
        imageView = view.findViewById(R.id.imageViewPreview)

        ocrProcessor = OCRProcessor(requireContext())

        buttonScan.setOnClickListener { openCamera() }
        buttonRecentsScan.setOnClickListener {
            Toast.makeText(requireContext(), "Opening recent scans...", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val galleryButton: Button = view.findViewById(R.id.gallery_button)
        galleryButton.setOnClickListener {
            openGallery()
        }
    }

    private fun openCamera() {
        if (!isAdded) return

        if (checkCameraPermission()) {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val imageFile = createImageFile()
            imageUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                imageFile
            )
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            cameraLauncher.launch(takePictureIntent)
        } else {
            requestCameraPermission()
        }
    }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                imageUri?.let {
                    imageView.setImageURI(it)
                    processImage(it)
                } ?: Toast.makeText(requireContext(), "Eroare: Imaginea capturată este null!", Toast.LENGTH_SHORT).show()
            }
        }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            parentFile?.mkdirs()
        }
    }

    private fun processImage(imageUri: Uri) {
        try {
            val image = InputImage.fromFilePath(requireContext(), imageUri)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val intent = Intent(requireContext(), ScanResultActivity::class.java)
                    intent.putExtra("IMAGE_PATH", imageUri.toString())
                    intent.putExtra("EXTRACTED_TEXT", visionText.text)
                    startActivity(intent)
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImageUri: Uri? = data.data
            selectedImageUri?.let { uri ->
                processImage(uri)
            }
        }
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.CAMERA
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

    fun processImageFromCamera(imageUri: Uri) {
        processImage(imageUri)
    }

    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
    }
}

class OCRProcessor(context: Context) {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    fun processImage(
        image: InputImage,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                onSuccess(visionText.text)
            }
            .addOnFailureListener { e ->
                onFailure(e)
            }
    }

    fun close() {
        recognizer.close()
    }
}