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
        // TODO: 后端部署后，替换为实际的服务器地址
        // 本地测试: http://10.0.2.2:3000/ (Android 模拟器访问本机)
        // 本地测试: http://192.168.x.x:3000/ (真机访问局域网)
        // 云服务器: http://your-server.com/
        const val BASE_URL = "http://104.208.113.142:3000/"
    }
}
