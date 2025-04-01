package com.example.myapplicationtmppp.ui.scanner

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplicationtmppp.R

class ProductAdapter(private val products: List<ScanResultActivity.Product>) :
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
        
        val quantityText = if (product.unit.isBlank() || product.unit == "null") {
            "Cant: ${product.quantity}"
        } else {
            "Cant: ${product.quantity} ${product.unit}"
        }
        holder.tvQuantity.text = quantityText

        holder.tvPrice.text = "Preț: ${"%.2f".format(product.unitPrice)} LEI"
        holder.tvTotal.text = "Total: ${"%.2f".format(product.totalPrice)} LEI"
    }

    override fun getItemCount(): Int {
        Log.d("ADAPTER_SIZE", "Număr produse în adaptor: ${products.size}")
        return products.size
    }
}