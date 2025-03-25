package com.example.myapplicationtmppp.ui.scanner.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/extract-products")
    fun extractProducts(@Body request: ExtractProductsRequest): Call<ExtractProductsResponse>
}

data class ExtractProductsRequest(val text: String)
data class ExtractProductsResponse(val products: List<Product>)

data class Product(
    val product: String,
    val quantity: Double,
    val unit: String?,
    val unit_price: Double,
    val total_price: Double
)