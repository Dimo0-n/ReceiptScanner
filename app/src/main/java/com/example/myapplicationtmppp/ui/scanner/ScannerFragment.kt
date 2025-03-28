package com.example.myapplicationtmppp.ui.scanner

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.myapplicationtmppp.R
import com.example.myapplicationtmppp.ai.DeepseekService
import com.google.android.gms.tasks.Task
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScannerFragment : Fragment() {
    private val CAMERA_REQUEST_CODE = 1
    private val GALLERY_REQUEST_CODE = 2
    private lateinit var currentPhotoPath: String
    private val deepseekService = DeepseekService()
    private lateinit var textRecognizer: TextRecognizer

    // Contract pentru cererea permisiunii CAMERA
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                showPermissionDeniedDialog()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Permisiunea camerei este necesară pentru a captura imagini",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.scanner_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initOCR()
        setupButtons(view)
    }

    private fun initOCR() {
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    private fun setupButtons(view: View) {
        view.findViewById<Button>(R.id.camera_button).setOnClickListener {
            checkCameraPermission()
        }
        view.findViewById<Button>(R.id.gallery_button).setOnClickListener {
            openGallery()
        }
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                startCamera()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                showPermissionRationaleDialog()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun showPermissionRationaleDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Permisiune necesară")
            .setMessage("Aplicația are nevoie de acces la cameră pentru a captura imagini. Permisiunea va fi folosită doar în acest scop.")
            .setPositiveButton("OK") { _, _ ->
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            .setNegativeButton("Anulează", null)
            .show()
    }

    private fun showPermissionDeniedDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Permisiune respinsă")
            .setMessage("Ai respins permisiunea permanent. Pentru a utiliza camera, te rugăm să o activezi manual în setări.")
            .setPositiveButton("Setări") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("Anulează", null)
            .show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", requireActivity().packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun startCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            intent.resolveActivity(requireActivity().packageManager)?.also {
                createImageFile()?.let { file ->
                    val photoURI = FileProvider.getUriForFile(
                        requireContext(),
                        "${requireContext().packageName}.fileprovider",
                        file
                    )
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(intent, CAMERA_REQUEST_CODE)
                }
            } ?: run {
                Toast.makeText(
                    requireContext(),
                    "Nu s-a putut deschide camera",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun openGallery() {
        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).also {
            it.type = "image/*"
            startActivityForResult(it, GALLERY_REQUEST_CODE)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> processImage(Uri.fromFile(File(currentPhotoPath)))
                GALLERY_REQUEST_CODE -> data?.data?.let { processImage(it) }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun processImage(imageUri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val bitmap = loadBitmap(imageUri)
                bitmap?.let {
                    recognizeText(it) { extractedText ->
                        sendToDeepseek(extractedText, imageUri)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showError("Eroare procesare imagine: ${e.message}")
                }
            }
        }
    }

    private suspend fun loadBitmap(uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        return@withContext try {
            requireContext().contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun recognizeText(bitmap: Bitmap, callback: (String) -> Unit) {
        val image = InputImage.fromBitmap(bitmap, 0)
        textRecognizer.process(image)
            .addOnSuccessListener { visionText ->
                callback(processVisionText(visionText))
            }
            .addOnFailureListener { e ->
                showError("Eroare OCR: ${e.message}")
            }
    }

    private fun processVisionText(visionText: Text): String {
        return buildString {
            for (block in visionText.textBlocks) {
                append(block.text)
                append("\n")
            }
        }
    }

    private fun sendToDeepseek(text: String, imageUri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = deepseekService.sendReceiptForAnalysis(text)
                withContext(Dispatchers.Main) {
                    navigateToResults(imageUri.toString(), response)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showError("Eroare API: ${e.message}")
                }
            }
        }
    }

    private fun navigateToResults(imagePath: String, response: String) {
        Intent(requireContext(), ScanResultActivity::class.java).apply {
            putExtra("IMAGE_PATH", imagePath)
            putExtra("DEEPSEEK_RESPONSE", response)
            startActivity(this)
        }
    }

    private fun showError(message: String?) {
        Toast.makeText(
            context,
            message ?: "Eroare necunoscută",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        textRecognizer.close()
    }
}