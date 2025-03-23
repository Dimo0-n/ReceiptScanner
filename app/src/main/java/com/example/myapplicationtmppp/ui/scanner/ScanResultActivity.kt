package com.example.myapplicationtmppp.ui.scanner

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplicationtmppp.R
import java.util.regex.Pattern

class ScanResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_result)

        val imageView: ImageView = findViewById(R.id.imageViewResult)
        val textViewResults: TextView = findViewById(R.id.textViewResults)

        // Preluăm calea fișierului imagine trimis din ScannerFragment
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

        // Preluăm textul extras din intent și îl procesăm
        val extractedText = intent.getStringExtra("EXTRACTED_TEXT")
        if (extractedText != null) {
            val processedText = processText(extractedText)
            textViewResults.text = processedText
        } else {
            textViewResults.text = "Nu s-a extras niciun text."
        }
    }

    private fun processText(text: String): String {
        // Normalizează caractere speciale
        val normalizedText = text
            .replace("\\n", "\n")
            .replace("\\u003e", ">")
            .replace("\\xbb", "»")
            .replace("\\", "")

        val lines = normalizedText.split("\n")
        val productPriceList = mutableListOf<Product>()

        // Dicționar pentru datele cheie extrase
        val extractedData = mutableMapOf(
            "cec_number" to "",
            "date" to "",
            "store_name" to "",
            "total_amount" to 0.0,
            "identification_codes" to mutableListOf<String>()
        )

        // Extrage numărul cecului
        val cecNumberRegex = Regex("INR N\\s*[:#]?\\s*(\\d+)", RegexOption.IGNORE_CASE)
        cecNumberRegex.find(normalizedText)?.let { match ->
            match.groupValues.getOrNull(1)?.let { value ->
                extractedData["cec_number"] = value
            }
        }

        // Extrage data
        val dateRegex = Regex("(\\d{2}-\\d{2}-\\d{4} \\d{2}:\\d{2})")
        dateRegex.find(normalizedText)?.let { match ->
            match.groupValues.getOrNull(1)?.let { value ->
                extractedData["date"] = value
            }
        }

        // Extrage numele magazinului
        var storeName = ""  // Inițializăm cu string gol în loc de null
        for (line in lines) {
            if (line.contains(Regex("\\bS\\.R\\.L\\.\\b|\\bS\\.A\\.\\b", RegexOption.IGNORE_CASE))) {
                storeName = line.trim()
                break
            }
        }

        if (storeName.isEmpty() && lines.isNotEmpty()) {
            storeName = lines[0].trim()
        }
        extractedData["store_name"] = storeName

        // Extrage codurile de identificare
        val identificationCodes = Regex("###-Bon-\\d+\\.\\d+\\.\\d+-+#").findAll(normalizedText)
            .map { it.value }
            .toList()
        extractedData["identification_codes"] = identificationCodes.toMutableList()

        for (line in lines) {
            val cleanedLine = line.trim()
                .replace(", ", ".")
                .replace(" .", ".")
                .replace(" ", "")

            val productRegex = Regex(
                "(\\d+)\\s*(buc|kg|g|q|kq|luc|ka)?\\s*[x×]\\s*(\\d+[.,]?\\d*)\\s*[-=:]\\s*(\\d+[.,]?\\d*)",
                RegexOption.IGNORE_CASE
            )
            
            productRegex.find(cleanedLine)?.let { match ->
                try {
                    val quantity = match.groupValues.getOrNull(1)?.replace(",", ".")?.toDoubleOrNull() ?: return@let
                    val unit = match.groupValues.getOrNull(2) ?: "buc"
                    val unitPrice = match.groupValues.getOrNull(3)?.replace(",", ".")?.toDoubleOrNull() ?: return@let
                    val totalPrice = match.groupValues.getOrNull(4)?.replace(",", ".")?.toDoubleOrNull() ?: return@let

                    // Verifică corelația prețurilor
                    if (Math.abs(totalPrice - (quantity * unitPrice)) > 0.01) {
                        return@let
                    }

                    // Extrage numele produsului
                    val productNameParts = line.split(Regex("\\d+[.,]?\\d*\\s*[x×]"))
                    if (productNameParts.isNotEmpty()) {
                        val productName = productNameParts[0]
                            .replace("»", "")
                            .replace(">", "")
                            .trim()

                        productPriceList.add(Product(productName, quantity, unit, unitPrice, totalPrice))
                    }
                } catch (e: Exception) {
                    // Skip this product if any parsing errors occur
                    return@let
                }
            }
        }

        // Calculează costul total
        val totalCost = calculateTotalCost(productPriceList)
        extractedData["total_amount"] = totalCost

        // Formatează rezultatul pentru afișare
        return formatExtractedData(extractedData, productPriceList, totalCost)
    }

    private fun calculateTotalCost(products: List<Product>): Double {
        return products.sumOf { it.totalPrice }
    }

    private fun formatExtractedData(
        extractedData: Map<String, Any?>,
        products: List<Product>,
        totalCost: Double
    ): String {
        val builder = StringBuilder()

        builder.append("Număr CEC: ${extractedData["cec_number"]}\n")
        builder.append("Data: ${extractedData["date"]}\n")
        builder.append("Nume magazin: ${extractedData["store_name"]}\n")
        builder.append("Coduri identificare: ${extractedData["identification_codes"]}\n\n")

        builder.append("Produse:\n")
        for (product in products) {
            builder.append("${product.product} - ${product.quantity} ${product.unit} x ${product.unitPrice} = ${product.totalPrice}\n")
        }

        builder.append("\nTotal: $totalCost")

        return builder.toString()
    }

    data class Product(
        val product: String,
        val quantity: Double,
        val unit: String,
        val unitPrice: Double,
        val totalPrice: Double
    )
}