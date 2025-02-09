package com.bluebirdcorp.managashelfrev.paging

import com.google.gson.annotations.SerializedName

data class PagingResponse<T>(
    @SerializedName("items") val items: List<T>,
    @SerializedName("currentPage") val currentPage: Int,
    @SerializedName("totalPages") val totalPages: Int
)