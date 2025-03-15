package com.example.myapplicationtmppp.ui.scanner

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
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
import java.io.File

class ScannerFragment : Fragment() {

    private lateinit var imageView: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.scanner_layout, container, false)

        val buttonScan: Button = view.findViewById(R.id.button_scan)
        val buttonRecentsScan: Button = view.findViewById(R.id.button_recents_scan)
        imageView = view.findViewById(R.id.imageViewPreview) // Adaugă un ImageView în XML pentru previzualizare

        buttonScan.setOnClickListener {
            openCamera()
        }

        buttonRecentsScan.setOnClickListener {
            Toast.makeText(requireContext(), "Opening recent scans...", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    // Verifică permisiunea camerei
    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Cere permisiunea pentru cameră
    private fun requestCameraPermission() {
        requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
    }

    // Lansator pentru capturarea imaginii
    private var capturedBitmap: Bitmap? = null

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            if (imageBitmap != null) {
                imageView.setImageBitmap(imageBitmap)

                // Salvăm imaginea doar dacă nu este null
                val imageFile = saveBitmapToFile(imageBitmap)
                if (imageFile.exists()) {
                    val intent = Intent(requireContext(), ScanResultActivity::class.java).apply {
                        putExtra("IMAGE_PATH", imageFile.absolutePath)
                    }
                    startActivity(intent)
                } else {
                    Toast.makeText(requireContext(), "Eroare la salvarea imaginii", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Eroare: Imaginea capturată este null!", Toast.LENGTH_SHORT).show()
            }
        }
    }


    //salvarea fotografiei facute intr-un fisier temporar
    private fun saveBitmapToFile(bitmap: Bitmap): File {
        val file = File(requireContext().cacheDir, "captured_image.jpg")
        file.outputStream().use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        }
        return file
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

    // Gestionarea răspunsului pentru permisiunea camerei
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
