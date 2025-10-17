package com.plant.levelcodemoreplant.data.repository

import android.content.Context
import com.plant.levelcodemoreplant.data.api.RetrofitClient
import com.plant.levelcodemoreplant.data.local.PrefsManager
import com.plant.levelcodemoreplant.data.model.LicenseRequest
import com.plant.levelcodemoreplant.utils.DeviceUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 认证数据仓库
 * 处理认证相关的业务逻辑
 */
class AuthRepository(private val context: Context) {
    
    private val apiService = RetrofitClient.authApiService
    private val prefsManager = PrefsManager(context)
    
    /**
     * 检查是否已激活
     */
    fun isActivated(): Boolean {
        return prefsManager.isActivated()
    }
    
    /**
     * 获取已保存的卡密
     */
    fun getSavedLicenseKey(): String? {
        return prefsManager.getLicenseKey()
    }
    
    /**
     * 激活卡密
     */
    suspend fun activateLicense(licenseKey: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            // 获取设备ID
            val deviceId = DeviceUtils.getDeviceId(context)
            
            // 创建请求
            val request = LicenseRequest(
                licenseKey = licenseKey.trim(),
                deviceId = deviceId
            )
            
            // 发送网络请求
            val response = apiService.activateLicense(request)
            
            if (response.success) {
                // 激活成功，保存到本地
                prefsManager.saveActivationStatus(
                    isActivated = true,
                    licenseKey = licenseKey.trim()
                )
                Result.success(response.message)
            } else {
                // 激活失败
                Result.failure(Exception(response.message))
            }
            
        } catch (e: Exception) {
            // 网络错误或其他异常
            val errorMessage = when {
                e.message?.contains("Unable to resolve host") == true -> "网络连接失败，请检查网络设置"
                e.message?.contains("timeout") == true -> "请求超时，请重试"
                e.message?.contains("Connection refused") == true -> "无法连接服务器，请检查服务器地址"
                else -> "激活失败: ${e.message ?: "未知错误"}"
            }
            Result.failure(Exception(errorMessage))
        }
    }
    
    /**
     * 清除激活状态（用于测试）
     */
    fun clearActivation() {
        prefsManager.clearActivationStatus()
    }
}
