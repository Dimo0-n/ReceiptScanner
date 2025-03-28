package com.example.myapplicationtmppp.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.example.myapplicationtmppp.MainActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

object ExpenseData {
    private const val PREFS_NAME = "expense_prefs"
    private const val EXPENSES_KEY = "expenses_list"
    private const val MONTHLY_TOTALS_KEY = "monthly_totals"
    private const val TAG = "ExpenseData"

    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())

    private val sharedPreferences: SharedPreferences by lazy {
        MainActivity.appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun log(message: String) = Log.d(TAG, message)
    private fun error(message: String, e: Exception? = null) = e?.let { Log.e(TAG, message, it) } ?: Log.e(TAG, message)

    fun addExpense(storeName: String, amount: Double, date: String = getCurrentDate()) {
        val expenses = getAllExpenses().toMutableList()
        expenses.add(Expense(storeName, amount, date))
        saveExpenses(expenses)
        updateMonthlyTotal(amount, date)
    }

    fun getMonthlyExpenses(month: Int, year: Int): List<Expense> {
        return getAllExpenses().filter { expense ->
            val parts = expense.date.split("-")
            if (parts.size >= 2) {
                parts[0].toInt() == year && parts[1].toInt() == month
            } else {
                false
            }
        }
    }

    fun getAllExpenses(): List<Expense> {
        val json = sharedPreferences.getString(EXPENSES_KEY, null)
        return json?.let {
            val type = object : TypeToken<List<Expense>>() {}.type
            gson.fromJson(it, type) ?: emptyList()
        } ?: emptyList()
    }

    fun getMonthlyTotal(month: Int, year: Int): Double {
        return getMonthlyExpenses(month, year).sumOf { it.amount }
    }

    fun getCurrentMonthTotal(): Double {
        val currentMonth = monthFormat.format(Date())
        return getMonthlyTotals()[currentMonth] ?: 0.0
    }

    private fun updateMonthlyTotal(amount: Double, date: String) {
        try {
            val dateObj = dateFormat.parse(date) ?: Date()
            val monthYear = monthFormat.format(dateObj)

            val monthlyTotals = getMonthlyTotals().toMutableMap()
            val currentTotal = monthlyTotals[monthYear] ?: 0.0
            monthlyTotals[monthYear] = currentTotal + amount

            saveMonthlyTotals(monthlyTotals)
        } catch (e: Exception) {
            error("Error updating monthly total", e)
        }
    }

    fun getMonthlyTotals(): Map<String, Double> {
        val json = sharedPreferences.getString(MONTHLY_TOTALS_KEY, null)
        return json?.let {
            val type = object : TypeToken<Map<String, Double>>() {}.type
            gson.fromJson(it, type) ?: emptyMap()
        } ?: emptyMap()
    }

    private fun saveMonthlyTotals(totals: Map<String, Double>) {
        sharedPreferences.edit {
            putString(MONTHLY_TOTALS_KEY, gson.toJson(totals))
        }
    }

    private fun saveExpenses(expenses: List<Expense>) {
        sharedPreferences.edit {
            putString(EXPENSES_KEY, gson.toJson(expenses))
        }
    }

    private fun getCurrentDate(): String {
        return dateFormat.format(Date())
    }

    data class Expense(
        val storeName: String,
        val amount: Double,
        val date: String
    )
}