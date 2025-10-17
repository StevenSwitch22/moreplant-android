package com.plant.levelcodemoreplant.utils

import android.content.Context
import android.os.Build
import android.provider.Settings
import com.plant.levelcodemoreplant.BuildConfig
import com.plant.levelcodemoreplant.data.model.DeviceInfo
import java.security.MessageDigest

/**
 * 设备信息工具类
 */
object DeviceUtils {
    
    /**
     * 获取设备唯一ID
     * 使用 Android ID 作为设备标识（简单版本）
     */
    fun getDeviceId(context: Context): String {
        val androidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: "unknown"
        
        // 组合多个设备信息提高唯一性
        val deviceInfo = "${androidId}_${Build.BRAND}_${Build.MODEL}"
        
        // 使用 SHA-256 加密，生成固定长度的设备ID
        return sha256(deviceInfo).take(32)
    }
    
    /**
     * 获取完整的设备信息
     */
    fun getDeviceInfo(context: Context): DeviceInfo {
        val androidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: "unknown"
        
        return DeviceInfo(
            deviceId = getDeviceId(context),
            androidId = androidId,
            model = Build.MODEL,
            brand = Build.BRAND,
            osVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            appVersion = BuildConfig.VERSION_NAME
        )
    }
    
    /**
     * SHA-256 加密
     */
    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
