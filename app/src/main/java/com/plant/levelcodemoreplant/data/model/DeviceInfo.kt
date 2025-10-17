package com.plant.levelcodemoreplant.data.model

/**
 * 设备信息模型
 */
data class DeviceInfo(
    val deviceId: String,       // 设备唯一ID
    val androidId: String,      // Android ID
    val model: String,          // 设备型号
    val brand: String,          // 设备品牌
    val osVersion: String,      // Android 版本
    val appVersion: String      // APP 版本
)
