package com.example.myapplicationtmppp.ui.game

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplicationtmppp.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MonthlyExpensesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_monthly_expenses)

        // Buton de înapoi
        findViewById<ImageView>(R.id.ivBack).setOnClickListener {
            finish()
        }

        // Selector de lună/an
        findViewById<Button>(R.id.btnSelectMonth).setOnClickListener {
            showMonthYearPicker()
        }
    }

    private fun showMonthYearPicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, _ ->
                val selectedMonthYear = "${month + 1}/$year"
                val total = calculateMonthlyExpense(month + 1, year)
                displayExpenses(selectedMonthYear, total)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            1
        ).apply {
            // Ascunde selectorul de zile
            datePicker.findViewById<View>(resources.getIdentifier("day", "id", "android"))?.visibility = View.GONE
        }.show()
    }

    private fun calculateMonthlyExpense(month: Int, year: Int): Double {
        val prefs = getSharedPreferences("ReceiptsStorage", MODE_PRIVATE)
        val receiptsJsonList = prefs.getStringSet("receipts_json_list", null)
        var total = 0.0

        receiptsJsonList?.forEach { json ->
            try {
                val receipt = com.google.gson.Gson().fromJson(json, com.example.myapplicationtmppp.ui.scanner.ScanResultActivity.ReceiptInfo::class.java)
                val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                val date = dateFormat.parse(receipt.date)
                val cal = java.util.Calendar.getInstance().apply { time = date!! }

                if (cal.get(java.util.Calendar.MONTH) + 1 == month && cal.get(java.util.Calendar.YEAR) == year) {
                    total += receipt.total
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return total
    }

    private fun displayExpenses(monthYear: String, total: Double) {
        val container = findViewById<LinearLayout>(R.id.containerExpenses)
        container.removeAllViews()

        addExpenseView("📆 $monthYear", "Total cheltuit: ${"%.2f".format(total)} LEI", container)
    }

    private fun addExpenseView(title: String, content: String, container: LinearLayout) {
        val view = layoutInflater.inflate(R.layout.item_statistic, container, false)
        view.findViewById<TextView>(R.id.tvStatTitle).text = title
        view.findViewById<TextView>(R.id.tvStatContent).text = content
        container.addView(view)
    }
}