package com.example.myapplicationtmppp.ui.scanner

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplicationtmppp.R
import com.example.myapplicationtmppp.ui.game.GamificationActivity
import com.example.myapplicationtmppp.ui.game.SavingsGameManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.*

class ScanResultActivity : AppCompatActivity() {

    private val gson = Gson()

    // Clase de date
    data class ReceiptInfo(
        @SerializedName("store_name") val storeName: String,
        @SerializedName("cec_number") val cecNumber: String,
        @SerializedName("date") val date: String,
        @SerializedName("total_amount") val total: Double,
        @SerializedName("products") val products: List<Product>,
        @SerializedName("discounts") val discounts: List<Discount>,
        val currency: String = "LEI"
    )

    data class Product(
        @SerializedName("name") val name: String,
        @SerializedName("quantity") val quantity: Double,
        @SerializedName("unit_price") val unitPrice: Double,
        @SerializedName("total_price") val totalPrice: Double,
        val unit: String = ""
    )

    data class Discount(
        @SerializedName("type") val type: String,
        @SerializedName("amount") val amount: Double,
        val currency: String = "LEI"
    )

    private val savingsGameManager by lazy { SavingsGameManager(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_result)

        val imageUriString = intent.getStringExtra("IMAGE_PATH")
        val deepseekResponse = intent.getStringExtra("DEEPSEEK_RESPONSE")

        displayImage(imageUriString)
        processReceiptData(deepseekResponse)
        findViewById<FloatingActionButton>(R.id.fabGameProgress).setOnClickListener {
            // Pornește activitatea de gamificare
            startActivity(Intent(this, GamificationActivity::class.java))

            // Opțional: animație de tranziție
            overridePendingTransition(R.anim.slide_in_up, R.anim.fade_out)
        }
    }

    private fun displayImage(uriString: String?) {
        uriString?.let {
            findViewById<ImageView>(R.id.imageViewResult).setImageURI(Uri.parse(it))
        }
    }

    private fun processReceiptData(response: String?) {
        try {
            val errorCard = findViewById<CardView>(R.id.errorCard)
            val errorMessage = findViewById<TextView>(R.id.tvErrorMessage)

            if (response.isNullOrEmpty()) {
                showError("Nu s-au primit date valide", errorCard, errorMessage)
                return
            }

            Log.d("RAW_INPUT", "Text brut primit:\n$response")

            // 1. Încercăm mai întâi să extragem JSON-ul standard
            val jsonContent = extractJsonContent(response) ?: run {
                // 2. Dacă nu reușim, încercăm să reparăm manual structura
                repairBrokenJson(response)
            }

            Log.d("PROCESSED_JSON", "Conținut procesat:\n$jsonContent")

            // 3. Parsare finală
            val receiptData = try {
                gson.fromJson(jsonContent, ReceiptInfo::class.java)?.takeIf {
                    it.storeName.isNotBlank() && it.total > 0
                }
            } catch (e: Exception) {
                Log.e("FINAL_PARSE", "Eroare parsare finală", e)
                null
            }

            if (receiptData == null) {
                showError("Structura datelor este invalidă. Verificați textul brut pentru detalii.",
                    errorCard, errorMessage)
                displayRawResponse(response) // Afișăm răspunsul original pentru depanare
                return
            }

            val (progress, newBadge) = savingsGameManager.updateProgress(receiptData.total)

            // Afișează progresul în UI
            displayGamificationProgress(progress)

            // Arată notificare pentru noua insignă (dacă există)
            newBadge?.let { showBadgeNotification(it) }

            // Afișare succes
            displayStoreInfo(receiptData)
            displayProducts(receiptData.products)
            displayDiscounts(receiptData.discounts)
            errorCard.visibility = View.GONE

        } catch (e: Exception) {
            Log.e("PROCESS_ERROR", "Eroare neașteptată", e)
            showError("Eroare neașteptată: ${e.message ?: "contactați dezvoltatorul"}",
                findViewById(R.id.errorCard),
                findViewById(R.id.tvErrorMessage))
        }
    }

    private fun showBadgeNotification(badge: SavingsGameManager.Badge) {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Felicitări! 🎉")
            .setMessage("Ai câștigat insigna: **${badge.name}**\n${badge.description}")
            .setPositiveButton("OK", null)
            .setIcon(badge.iconResId)
            .create()

        dialog.setOnShowListener {
            // Animație de scalare
            val iconView = dialog.findViewById<ImageView>(android.R.id.icon)!!
            iconView.scaleX = 0f
            iconView.scaleY = 0f
            iconView.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(500)
                .start()
        }
        dialog.show()
    }

    private fun displayGamificationProgress(progress: SavingsGameManager.UserProgress) {
        findViewById<TextView>(R.id.tvTotalSaved)?.text =
            "Total economisit: ${"%.2f".format(progress.totalSaved)} LEI"

        findViewById<TextView>(R.id.tvLevel)?.text =
            "Nivel: ${progress.level}"

        val badgesContainer = findViewById<LinearLayout>(R.id.containerBadges)
        badgesContainer?.removeAllViews()

        progress.badges.forEach { badge ->
            ImageView(this).apply {
                setImageResource(badge.iconResId)
                layoutParams = LinearLayout.LayoutParams(
                    64.dpToPx(),
                    64.dpToPx()
                ).apply { marginEnd = 8.dpToPx() }
                contentDescription = badge.name
            }.let { badgesContainer?.addView(it) }
        }
    }

    private fun extractJsonContent(rawText: String): String? {
        return try {
            // Încercăm extragerea standard
            val jsonBlock = rawText.substringAfter("```json").substringBefore("```").trim()
            if (jsonBlock.isNotEmpty()) {
                // Curățare de bază
                jsonBlock.replace(Regex("""\\n"""), "")
                    .replace(Regex("""\\""""), "\"")
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w("JSON_EXTRACT", "Extragere standard eșuată", e)
            null
        }
    }

    private fun repairBrokenJson(brokenJson: String): String {
        // Soluție defensivă pentru cazurile dificile
        return brokenJson
            .replace(Regex("""n\{"""), "{")
            .replace(Regex("""\}n"""), "}")
            .replace(Regex("""\\""""), "\"")
            .replace(Regex("""(\w+)(\s*:\s*)"""), """"$1"$2""")
            .replace(Regex("""[\u0000-\u001F]"""), "") // Elimină caractere de control
            .let { if (!it.startsWith("{")) "{$it" else it }
            .let { if (!it.endsWith("}")) "$it}" else it }
    }

    private fun displayRawResponse(response: String) {
    }

    private fun displayStoreInfo(data: ReceiptInfo) {
        findViewById<TextView>(R.id.tvStoreInfo).text = buildString {
            append("🏬 ${data.storeName}\n")
            append("Data📅 ${data.date}\n")
            append("Bon🆔 ${data.cecNumber}\n")
            append("💵 Total: ${"%.2f".format(data.total)} + LEI")
        }
    }

    private fun displayProducts(products: List<Product>) {
        val adapter = ProductAdapter(products)

        findViewById<RecyclerView>(R.id.containerProducts).apply {
            // Forțează recalcularea dimensiunilor
            setHasFixedSize(false)
            layoutManager = LinearLayoutManager(this@ScanResultActivity).apply {
                isMeasurementCacheEnabled = false
            }

            // Actualizează adaptorul
            this.adapter = adapter

            // Forțează redesenarea completă
            adapter.notifyDataSetChanged()

            // Log de verificare
            Log.d("PRODUCTS_DEBUG", "Total produse în adaptor: ${adapter.itemCount}")
            products.forEachIndexed { index, product ->
                Log.d("PRODUCTS_DEBUG", "Produs $index: ${product.name}")
            }
        }
    }

    private fun displayDiscounts(discounts: List<Discount>) {
        val container = findViewById<LinearLayout>(R.id.containerDiscounts)
        container.removeAllViews()

        if (discounts.isEmpty()) {
            // Afișează "0 Lei reducere" când nu există reduceri
            container.addView(createDiscountView(
                Discount(type = "Reducere", amount = 0.0, currency = "LEI")
            ))
        } else {
            discounts.forEach { discount ->
                container.addView(createDiscountView(discount))
            }
        }
    }

    private fun createDiscountView(discount: Discount): View {
        return LayoutInflater.from(this).inflate(R.layout.item_discount, null).apply {
            findViewById<TextView>(R.id.tvDiscountType).text = discount.type

            // Afișează "0 Lei" dacă reducerea este 0, altfel afișează valoarea cu minus
            val amountText = if (discount.amount == 0.0) {
                "0 ${discount.currency}"
            } else {
                "-${"%.2f".format(discount.amount)} + LEI"
            }

            findViewById<TextView>(R.id.tvDiscountAmount).text = amountText
        }
    }

    private fun showError(message: String, errorCard: CardView, errorText: TextView) {
        errorCard.visibility = View.VISIBLE
        errorText.text = message
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()

    inner class ProductAdapter(private val products: List<Product>) :
        RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(R.id.tvProductName)
            val tvQuantity: TextView = view.findViewById(R.id.tvQuantity)
            val tvPrice: TextView = view.findViewById(R.id.tvUnitPrice)
            val tvTotal: TextView = view.findViewById(R.id.tvTotalPrice)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_product, parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val product = products[position]
            holder.tvName.text = product.name
            holder.tvQuantity.text = "Cant: ${product.quantity}"
            holder.tvPrice.text = "Preț: ${"%.2f".format(product.unitPrice)} LEI"
            holder.tvTotal.text = "Total: ${"%.2f".format(product.totalPrice)} LEI"
        }

        override fun getItemCount(): Int = products.size
    }
}