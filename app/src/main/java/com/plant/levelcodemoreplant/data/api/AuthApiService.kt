package com.plant.levelcodemoreplant.data.api

import com.plant.levelcodemoreplant.data.model.LicenseRequest
import com.plant.levelcodemoreplant.data.model.LicenseResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * 认证 API 接口
 */
interface AuthApiService {
    
    /**
     * 激活卡密
     */
    @POST("api/activate")
    suspend fun activateLicense(
        @Body request: LicenseRequest
    ): LicenseResponse
    
    companion object {
        // 服务器地址
        const val BASE_URL = "http://106.54.228.106:3000/"
    }
}
