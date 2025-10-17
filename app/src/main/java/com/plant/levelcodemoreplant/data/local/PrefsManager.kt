package com.plant.levelcodemoreplant.data.local

import android.content.Context
import android.content.SharedPreferences

/**
 * 本地存储管理类
 * 使用 SharedPreferences 保存激活状态
 */
class PrefsManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )
    
    /**
     * 保存激活状态
     */
    fun saveActivationStatus(isActivated: Boolean, licenseKey: String? = null) {
        prefs.edit().apply {
            putBoolean(KEY_IS_ACTIVATED, isActivated)
            if (licenseKey != null) {
                putString(KEY_LICENSE_KEY, licenseKey)
            }
            putLong(KEY_ACTIVATED_TIME, System.currentTimeMillis())
            apply()
        }
    }
    
    /**
     * 检查是否已激活
     */
    fun isActivated(): Boolean {
        return prefs.getBoolean(KEY_IS_ACTIVATED, false)
    }
    
    /**
     * 获取已保存的卡密
     */
    fun getLicenseKey(): String? {
        return prefs.getString(KEY_LICENSE_KEY, null)
    }
    
    /**
     * 获取激活时间
     */
    fun getActivatedTime(): Long {
        return prefs.getLong(KEY_ACTIVATED_TIME, 0)
    }
    
    /**
     * 清除激活状态（用于测试或重置）
     */
    fun clearActivationStatus() {
        prefs.edit().apply {
            remove(KEY_IS_ACTIVATED)
            remove(KEY_LICENSE_KEY)
            remove(KEY_ACTIVATED_TIME)
            apply()
        }
    }
    
    companion object {
        private const val PREFS_NAME = "auth_prefs"
        private const val KEY_IS_ACTIVATED = "is_activated"
        private const val KEY_LICENSE_KEY = "license_key"
        private const val KEY_ACTIVATED_TIME = "activated_time"
    }
}
