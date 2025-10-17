package com.plant.levelcodemoreplant.data.model

import com.google.gson.annotations.SerializedName

/**
 * 卡密激活请求模型
 */
data class LicenseRequest(
    @SerializedName("license_key")
    val licenseKey: String,
    
    @SerializedName("device_id")
    val deviceId: String
)
