package com.example.myapplicationtmppp.ai

import com.example.myapplicationtmppp.BuildConfig
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class DeepseekService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        })
        .build()

    private companion object {
        const val BASE_URL = "https://api.deepseek.com/v1"
        val JSON_MEDIA_TYPE = "application/json".toMediaTypeOrNull()
        const val MODEL_NAME = "deepseek-chat"
    }

    fun sendReceiptForAnalysis(extractedText: String): String {
        val prompt = createAnalysisPrompt(extractedText)
        val messages = createMessageStructure(prompt)

        val requestBody = JSONObject().apply {
            put("model", MODEL_NAME)
            put("messages", messages)
            put("temperature", 0.7)
            put("stream", false)
        }.toString()

        val request = Request.Builder()
            .url("$BASE_URL/chat/completions")
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer ${BuildConfig.DEEPSEEK_API_KEY}")
            .post(requestBody.toRequestBody(JSON_MEDIA_TYPE))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("API Error ${response.code}: ${response.body?.string()}")
            }
            return response.body?.string()?.takeIf { it.isNotBlank() }
                ?: throw Exception("Empty API response")
        }
    }

    private fun createAnalysisPrompt(text: String): String {
        return """
            Analizează textul unui bon fiscal și returnează date structurate în JSON. 
            Text extras OCR:
            ```
            $text
            ```
            
            Structură JSON așteptată:
            {
                "store_name": "Nume magazin",
                "cec_number":,
                "date": "YYYY-MM-DD",
                "total_amount": 100.00,
                "products": [
                    {
                        "name": "Denumire produs",
                        "quantity": 1,
                        "unit_price": 10.00,
                        "total_price": 10.00
                    }
                ],
                "discounts": [
                    {
                        "type": "Tip reducere",
                        "amount": 2.00
                    }
                ]
            }
            
            Reguli:
            1. Folosește doar informații din textul OCR
            2. Formatează toate sumele ca numere
            3. Dacă o informație nu există, folosește null
            4. Nu adăuga explicații suplimentare
            5. Data cecului nu poate fi mai mare decat data actuala
            6.cec_number are formatul ###-Bon-*******.*****.*****-###
        """.trimIndent()
    }

    private fun createMessageStructure(prompt: String): JSONArray {
        return JSONArray().apply {
            put(JSONObject().apply {
                put("role", "system")
                put("content", "Ești un asistent specializat în procesarea bonurilor fiscale.")
            })
            put(JSONObject().apply {
                put("role", "user")
                put("content", prompt)
            })
        }
    }
}