package com.example.myapplicationtmppp.ui.game

import android.content.Context
import android.util.Log
import com.example.myapplicationtmppp.ui.scanner.ScanResultActivity
import java.text.SimpleDateFormat
import java.util.Locale

    class StatsManager {

        // 1. Cheltuieli lunare
        fun getMonthlySpending(): Map<String, Double> {
            val monthlySums = mutableMapOf<String, Double>()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            allReceipts.forEach { receipt ->
                try {
                    val date = dateFormat.parse(receipt.date) ?: return@forEach
                    val monthYear = SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(date)
                    monthlySums[monthYear] = monthlySums.getOrDefault(monthYear, 0.0) + receipt.total
                } catch (e: Exception) {
                    Log.e("STATS", "Error parsing date: ${receipt.date}", e)
                }
            }
            return monthlySums
        }

        // 2. Top 3 produse
        fun getTopProducts(): List<Pair<String, Int>> {
            val productCounts = mutableMapOf<String, Int>()
            allReceipts.flatMap { it.products }.forEach {
                productCounts[it.name] = productCounts.getOrDefault(it.name, 0) + 1
            }
            return productCounts.entries
                .sortedByDescending { it.value }
                .take(3)
                .map { Pair(it.key, it.value) }
        }

        // 3. Magazinul cel mai convenabil
        fun getBestStore(): Pair<String, Double>? {
            val storeAverages = allReceipts
                .groupBy { it.storeName }
                .mapValues { entry ->
                    entry.value.map { it.total }.average()
                }
            return storeAverages.minByOrNull { it.value }?.toPair()
        }
    }