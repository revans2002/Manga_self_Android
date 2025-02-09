package com.bluebirdcorp.managashelfrev.api

data class PagedResponse<T>(
    val items: List<T>,
    val currentPage: Int,
    val totalPages: Int
)
