package com.plant.levelcodemoreplant.data.model

import com.google.gson.annotations.SerializedName

/**
 * 卡密激活响应模型
 */
data class LicenseResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: LicenseData? = null
)

/**
 * 卡密数据（可选，用于后续扩展）
 */
data class LicenseData(
    @SerializedName("license_key")
    val licenseKey: String? = null,
    
    @SerializedName("expires_at")
    val expiresAt: Long? = null,
    
    @SerializedName("license_type")
    val licenseType: String? = null
)
