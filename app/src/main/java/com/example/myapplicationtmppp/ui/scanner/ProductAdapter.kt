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
        holder.tvQuantity.text = "Cant: ${product.quantity} ${product.unit}"
        holder.tvPrice.text = "Preț: ${"%.2f".format(product.unitPrice)} RON"
        holder.tvTotal.text = "Total: ${"%.2f".format(product.totalPrice)} RON"

        // Log pentru fiecare produs
        Log.d("PRODUCT_ITEM", "Produs $position: ${product.name}")
    }

    override fun getItemCount(): Int {
        Log.d("ADAPTER_SIZE", "Număr produse în adaptor: ${products.size}")
        return products.size
    }
}