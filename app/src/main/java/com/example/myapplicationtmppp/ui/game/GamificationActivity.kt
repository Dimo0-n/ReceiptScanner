package com.example.myapplicationtmppp.ui.game

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplicationtmppp.R
import com.example.myapplicationtmppp.ui.scanner.ScanResultActivity
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Locale

class GamificationActivity : AppCompatActivity() {
    private lateinit var savingsGameManager: SavingsGameManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gamification)
        savingsGameManager = SavingsGameManager(this)

        intent.getStringExtra("NEW_BADGE_ID")?.let { badgeId ->
            highlightBadge(badgeId) // Metodă pentru a evidenția insigna
        }

        setupUI()
        loadProgress()
        calculateStatistics()
    }

    private fun highlightBadge(badgeId: String) {
        findViewById<LinearLayout>(R.id.containerBadges).let { container ->
            (0 until container.childCount).forEach { i ->
                val badgeView = container.getChildAt(i)
                if (badgeView.getTag(R.id.badge_id) == badgeId) {
                    // Aplică un efect vizual
                    badgeView.animate()
                        .scaleX(1.3f)
                        .scaleY(1.3f)
                        .setInterpolator(OvershootInterpolator())
                        .setDuration(500)
                        .start()
                }
            }
        }
    }

    private fun setupUI() {
        // Buton de închidere
        findViewById<ImageView>(R.id.ivClose).setOnClickListener {
            finish()
        }

        // Buton de reset (pentru debug)
        findViewById<Button>(R.id.btnReset).setOnClickListener {
            resetProgress()
        }
    }

    private fun loadProgress() {
        val progress = savingsGameManager.loadProgress()
        updateProgressUI(progress)
    }

    private fun updateProgressUI(progress: SavingsGameManager.UserProgress) {
        // Progres general
        findViewById<TextView>(R.id.tvTotalSaved).text =
            "Total cheltuit: ${"%.2f".format(progress.totalSaved)} LEI"
        findViewById<TextView>(R.id.tvLevel).text = "Nivel ${progress.level}"

        // ProgressBar pentru nivel următor
        val nextLevelXp = progress.level * 100
        val progressXp = (progress.totalSaved % 100).toInt()
        findViewById<ProgressBar>(R.id.progressBar).apply {
            max = 100
        }
        findViewById<TextView>(R.id.tvXp).text = "$progressXp/$nextLevelXp XP"

        // Insigne
        val badgesContainer = findViewById<LinearLayout>(R.id.containerBadges)
        badgesContainer.removeAllViews()
        savingsGameManager.getAllBadges().forEach { badge ->
            val badgeView = layoutInflater.inflate(R.layout.item_badge, badgesContainer, false).apply {
                // Setează tag-ul cu ID-ul badge-ului
                setTag(R.id.badge_id, badge.id) // Folosește ID-ul definit în ids.xml

                // Restul configurației...
                findViewById<ImageView>(R.id.ivBadge).setImageResource(badge.iconResId)
                findViewById<TextView>(R.id.tvBadgeName).text = badge.name
            }
            badgesContainer.addView(badgeView)
        }
    }

    private fun resetProgress() {
        savingsGameManager.saveProgress(SavingsGameManager.UserProgress())
        loadProgress()
        Toast.makeText(this, "Progres resetat", Toast.LENGTH_SHORT).show()
    }

    private fun calculateStatistics() {
        val prefs = getSharedPreferences(ScanResultActivity.PREFS_NAME, Context.MODE_PRIVATE)
        val receiptsJsonList = prefs.getStringSet(ScanResultActivity.KEY_RECEIPTS_LIST, null)

        receiptsJsonList?.let {
            val gson = Gson()
            val allReceipts = mutableListOf<ScanResultActivity.ReceiptInfo>()
            val storeStats = mutableMapOf<String, StoreStats>()

            // Parsează și procesează toate bonurile
            it.forEach { json ->
                try {
                    val receipt = gson.fromJson(json, ScanResultActivity.ReceiptInfo::class.java)
                    allReceipts.add(receipt)

                    // Calculează statistici pentru magazine
                    val storeName = receipt.storeName
                    val currentStats = storeStats.getOrPut(storeName) { StoreStats() }
                    currentStats.totalSpent += receipt.total
                    currentStats.totalDiscounts += receipt.discounts.sumOf { it.amount }
                    currentStats.visitCount++
                } catch (e: Exception) {
                    Log.e("STATS", "Eroare parsare JSON: $json", e)
                }
            }

            // Actualizează UI-ul cu statisticile calculate
            updateStatisticsUI(
                monthlySpending = calculateMonthlySpending(allReceipts),
                storeStats = storeStats
            )
        }
    }

    private data class StoreStats(
        var totalSpent: Double = 0.0,
        var totalDiscounts: Double = 0.0,
        var visitCount: Int = 0
    )

    private fun calculateMonthlySpending(receipts: List<ScanResultActivity.ReceiptInfo>): Map<String, Double> {
        val monthlySums = mutableMapOf<String, Double>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        receipts.forEach { receipt ->
            try {
                val date = dateFormat.parse(receipt.date)
                val monthYear = SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(date)
                monthlySums[monthYear] = monthlySums.getOrDefault(monthYear, 0.0) + receipt.total
            } catch (e: Exception) {
                Log.e("STATS", "Eroare format dată: ${receipt.date}", e)
            }
        }
        return monthlySums
    }

    private fun getTopProducts(productCounts: Map<String, Int>): List<Pair<String, Int>> {
        return productCounts.entries
            .sortedByDescending { it.value }
            .take(3)
            .map { Pair(it.key, it.value) }
    }

    private fun getMostCostEffectiveStore(storeData: Map<String, List<Double>>): Pair<String, Double>? {
        return storeData.mapValues { entry ->
            entry.value.average()
        }.minByOrNull { it.value }?.let { Pair(it.key, it.value) }
    }

    private fun updateStatisticsUI(
        monthlySpending: Map<String, Double>,
        storeStats: Map<String, StoreStats>
    ) {
        val statsContainer = findViewById<LinearLayout>(R.id.containerStats)
        statsContainer.removeAllViews()

        if (monthlySpending.isEmpty()) {
            addStatisticView("📊 Statistici", "Nu există date suficiente", statsContainer)
            return
        }

        //Statistici pe magazine
        addStatisticView("🏪 Statistici Magazine:", storeStats.entries.joinToString("\n\n") {
            val store = it.value
            "${it.key}:\n" +
            "Total cheltuit: ${"%.2f".format(store.totalSpent)} LEI\n" +
            "Total reduceri: ${"%.2f".format(store.totalDiscounts)} LEI\n" +
            "Vizite: ${store.visitCount}"
        }, statsContainer)
    }

    private fun addStatisticView(title: String, content: String, container: LinearLayout) {
        LayoutInflater.from(this).inflate(R.layout.item_statistic, container).apply {
            findViewById<TextView>(R.id.tvStatTitle).text = title
        }
    }
}