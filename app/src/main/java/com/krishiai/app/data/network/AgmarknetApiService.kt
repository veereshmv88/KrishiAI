package com.krishiai.app.data.network

import com.krishiai.app.data.network.dto.AgmarknetResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface AgmarknetApiService {
    
    // The resource ID for daily market prices on data.gov.in
    @GET("resource/9ef84268-d588-465a-a308-a864a43d0070")
    suspend fun getMarketPrices(
        @Query("api-key") apiKey: String,
        @Query("format") format: String = "json",
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 100,
        @Query("filters[state]") state: String? = null,
        @Query("filters[district]") district: String? = null,
        @Query("filters[commodity]") commodity: String? = null
    ): Response<AgmarknetResponse>
}
