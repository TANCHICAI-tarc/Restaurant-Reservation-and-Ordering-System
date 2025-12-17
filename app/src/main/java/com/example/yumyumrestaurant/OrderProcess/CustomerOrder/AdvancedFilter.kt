package com.example.yumyumrestaurant.OrderProcess.CustomerOrder

data class AdvancedFilter(
    val selectedSubCategories: List<String> = listOf("All"),
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val chefOnly: Boolean? = null,
)
