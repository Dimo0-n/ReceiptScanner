package com.example.myapplicationtmppp.expenses

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplicationtmppp.R
import com.example.myapplicationtmppp.data.ExpenseData
import java.text.DecimalFormat

class ExpensesAdapter(private var expenses: List<ExpenseData.Expense>) : RecyclerView.Adapter<ExpensesAdapter.ExpenseViewHolder>() {

    private val decimalFormat = DecimalFormat("#.##")

    inner class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvStore: TextView = itemView.findViewById(R.id.tvStore)
        private val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)

        fun bind(expense: ExpenseData.Expense) {
            tvStore.text = expense.storeName
            tvAmount.text = decimalFormat.format(expense.amount)
            tvDate.text = expense.date
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(expenses[position])
    }

    override fun getItemCount() = expenses.size

    fun updateData(newExpenses: List<ExpenseData.Expense>) {
        expenses = newExpenses
        notifyDataSetChanged()
    }
}