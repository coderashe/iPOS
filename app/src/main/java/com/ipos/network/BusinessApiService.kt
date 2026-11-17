package com.ipos.network

import com.ipos.network.dto.BusinessPerformanceRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface BusinessApiService {

    @POST("api/business-performance")
    suspend fun syncBusinessPerformance(
        @Body performance: BusinessPerformanceRequest
    )
}
