package com.example.myapplicationtmppp.ui.scanner

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplicationtmppp.R
import com.example.myapplicationtmppp.ui.scanner.api.ApiService
import com.example.myapplicationtmppp.ui.scanner.api.ExtractProductsRequest
import com.example.myapplicationtmppp.ui.scanner.api.ExtractProductsResponse
import com.example.myapplicationtmppp.ui.scanner.api.Product
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ScanResultActivity : AppCompatActivity() {

    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_result)

        // Inițializează Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.1.2:5000/")  // Folosește 10.0.2.2 pentru localhost în emulator
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)

        // Inițializează componentele UI
        val imageView: ImageView = findViewById(R.id.imageViewResult)
        val textViewResults: TextView = findViewById(R.id.textViewResults)

        // Afișează imaginea capturată
        val imagePath = intent.getStringExtra("IMAGE_PATH")
        if (imagePath != null) {
            val imageUri = Uri.parse(imagePath)
            try {
                val inputStream = contentResolver.openInputStream(imageUri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                imageView.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
                imageView.setImageResource(R.drawable.placeholder)
            }
        } else {
            imageView.setImageResource(R.drawable.placeholder)
        }

        // Afișează textul extras și trimite-l la serverul Flask
        val extractedText = intent.getStringExtra("EXTRACTED_TEXT")
        if (extractedText != null) {
            // Trimite textul la serverul Flask
            sendTextToServer(extractedText, textViewResults)
        } else {
            textViewResults.text = "Nu s-a extras niciun text."
        }
    }

    // Trimite textul la serverul Flask și afișează răspunsul
    private fun sendTextToServer(extractedText: String, textViewResults: TextView) {
        val request = ExtractProductsRequest(extractedText)
        val call = apiService.extractProducts(request)

        call.enqueue(object : Callback<ExtractProductsResponse> {
            override fun onResponse(
                call: Call<ExtractProductsResponse>,
                response: Response<ExtractProductsResponse>
            ) {
                if (response.isSuccessful) {
                    val products = response.body()?.products
                    if (products != null) {
                        // Formatează lista de produse pentru afișare
                        val formattedText = buildString {
                            append("Text extras:\n")
                            append(extractedText)
                            append("\n\nLista de produse:\n")
                            append(formatProductList(products))
                        }

                        // Afișează textul în TextView
                        textViewResults.text = formattedText
                    } else {
                        textViewResults.text = "Nu s-au găsit produse în textul extras."
                    }
                } else {
                    textViewResults.text = "Eroare la comunicarea cu serverul."
                }
            }

            override fun onFailure(call: Call<ExtractProductsResponse>, t: Throwable) {
                textViewResults.text = "Eroare de rețea: ${t.message}"
            }
        })
    }

    // Formatează lista de produse pentru afișare
    private fun formatProductList(products: List<Product>): String {
        return products.joinToString("\n") {
            "${it.product}: ${it.quantity} ${it.unit ?: ""} x ${it.unit_price} = ${it.total_price} LEI"
        }
    }
}