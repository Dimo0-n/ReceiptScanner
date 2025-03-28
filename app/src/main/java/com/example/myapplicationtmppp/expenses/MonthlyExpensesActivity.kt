package com.example.myapplicationtmppp.expenses

import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplicationtmppp.R
import com.example.myapplicationtmppp.data.ExpenseData
import com.example.myapplicationtmppp.databinding.ActivityMonthlyExpensesBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MonthlyExpensesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMonthlyExpensesBinding
    private lateinit var adapter: ExpensesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMonthlyExpensesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupListeners()
    }

    private fun setupRecyclerView() {
        adapter = ExpensesAdapter(emptyList())
        binding.rvExpenses.layoutManager = LinearLayoutManager(this)
        binding.rvExpenses.adapter = adapter
    }

    private fun setupListeners() {
        binding.monthYearInput.inputType = InputType.TYPE_CLASS_TEXT

        binding.monthYearInput.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return

                isFormatting = true

                try {
                    val digits = s.toString().replace("[^0-9]".toRegex(), "")

                    val formatted = when {
                        digits.length <= 2 -> digits
                        digits.length <= 6 -> "${digits.substring(0, 2)}/${digits.substring(2)}"
                        else -> "${digits.substring(0, 2)}/${digits.substring(2, 6)}"
                    }

                    if (s.toString() != formatted) {
                        s?.replace(0, s.length, formatted)
                        binding.monthYearInput.setSelection(formatted.length)
                    }
                } finally {
                    isFormatting = false
                }
            }
        })

        binding.btnShowExpenses.setOnClickListener {
            val input = binding.monthYearInput.text.toString()
            if (input.matches(Regex("\\d{2}/\\d{4}"))) {
                val parts = input.split("/")
                val month = parts[0].toInt()
                val year = parts[1].toInt()

                if (month in 1..12) {
                    showExpensesForMonth(month, year)
                } else {
                    Toast.makeText(this, "Luna invalidă (1-12)", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Format invalid. Introduceți MM/YYYY", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showExpensesForMonth(month: Int, year: Int) {
        val expenses = ExpenseData.getMonthlyExpenses(month, year)
        val monthlyTotals = ExpenseData.getMonthlyTotals()
        val monthKey = String.format(Locale.getDefault(), "%04d-%02d", year, month)
        val total = monthlyTotals[monthKey] ?: 0.0

        binding.tvMonthlyTotal.text = getString(
            R.string.monthly_total_format,
            formatMonthYear(month, year),
            total
        )
        adapter.updateData(expenses)
    }

    private fun formatMonthYear(month: Int, year: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, month - 1)
        calendar.set(Calendar.YEAR, year)
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        return sdf.format(calendar.time)
    }
}