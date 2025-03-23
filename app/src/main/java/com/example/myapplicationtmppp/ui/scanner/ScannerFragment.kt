package com.example.myapplicationtmppp.ui.scanner

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.myapplicationtmppp.R
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.scanner_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setăm click listeners pentru butoane
        view.findViewById<Button>(R.id.camera_button).setOnClickListener {
            startCamera()
        }

        view.findViewById<Button>(R.id.gallery_button).setOnClickListener {
            openGallery()
        }
    }

    private fun startCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            intent.resolveActivity(requireActivity().packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    Log.e("ScannerFragment", "Error creating image file", ex)
                    null
                }
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        requireContext(),
                        "com.example.myapplicationtmppp.fileprovider",
                        it
                    )
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(intent, CAMERA_REQUEST_CODE)
                }
            }
        }
    }

    private fun openGallery() {
        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).also { intent ->
            intent.type = "image/*"
            startActivityForResult(intent, GALLERY_REQUEST_CODE)
        }
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = requireContext().getExternalFilesDir(null)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {
                    val imageUri = Uri.fromFile(File(currentPhotoPath))
                    processImage(imageUri)
                }
                GALLERY_REQUEST_CODE -> {
                    data?.data?.let { uri ->
                        processImage(uri)
                    }
                }
            }
        }
    }

    private fun processImage(imageUri: Uri) {
        try {
            // Convertim imaginea în bitmap
            val inputStream = requireContext().contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            // Convertim bitmap-ul în PDF temporar
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
            val page = pdfDocument.startPage(pageInfo)

            // Desenăm bitmap-ul pe pagina PDF
            page.canvas.drawBitmap(bitmap, 0f, 0f, null)
            pdfDocument.finishPage(page)

            // Salvăm PDF-ul temporar
            val tempPdfFile = File(requireContext().cacheDir, "temp.pdf")
            pdfDocument.writeTo(FileOutputStream(tempPdfFile))
            pdfDocument.close()

            // Extragem textul folosind ML Kit
            val image = InputImage.fromFilePath(requireContext(), imageUri)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    // Trimitem rezultatele către ScanResultActivity
                    val intent = Intent(requireContext(), ScanResultActivity::class.java)
                    intent.putExtra("IMAGE_PATH", imageUri.toString())
                    intent.putExtra("EXTRACTED_TEXT", visionText.text)
                    startActivity(intent)
                }
                .addOnFailureListener { e ->
                    Log.e("ScannerFragment", "Text recognition failed", e)
                }

        } catch (e: IOException) {
            Log.e("ScannerFragment", "Error processing image", e)
        }
    }
}