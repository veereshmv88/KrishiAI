package com.krishiai.app.data.network.dto

import com.google.gson.annotations.SerializedName

data class AgmarknetResponse(
    @SerializedName("created") val created: Long?,
    @SerializedName("updated") val updated: Long?,
    @SerializedName("title") val title: String?,
    @SerializedName("desc") val desc: String?,
    @SerializedName("count") val count: Int?,
    @SerializedName("total") val total: Int?,
    @SerializedName("limit") val limit: String?,
    @SerializedName("offset") val offset: String?,
    @SerializedName("records") val records: List<AgmarknetRecordDto>?
)

data class AgmarknetRecordDto(
    @SerializedName("state") val state: String?,
    @SerializedName("district") val district: String?,
    @SerializedName("market") val market: String?,
    @SerializedName("commodity") val commodity: String?,
    @SerializedName("variety") val variety: String?,
    @SerializedName("grade") val grade: String?,
    @SerializedName("arrival_date") val arrivalDate: String?,
    @SerializedName("min_price") val minPrice: Double?,
    @SerializedName("max_price") val maxPrice: Double?,
    @SerializedName("modal_price") val modalPrice: Double?
)
