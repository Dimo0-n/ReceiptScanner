package com.example.myapplicationtmppp.ui.scanner

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplicationtmppp.R

class DiscountAdapter(private val discounts: List<ScanResultActivity.Discount>) :
    RecyclerView.Adapter<DiscountAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvType: TextView = itemView.findViewById(R.id.tvDiscountType)
        val tvAmount: TextView = itemView.findViewById(R.id.tvDiscountAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_discount, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val discount = discounts[position]
        holder.tvType.text = discount.type
        holder.tvAmount.text = "-${"%.2f".format(discount.amount)} RON"
    }

    override fun getItemCount() = discounts.size
}