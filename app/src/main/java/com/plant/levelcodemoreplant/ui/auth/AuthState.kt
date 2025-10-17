package com.plant.levelcodemoreplant.ui.auth

/**
 * 认证状态
 */
sealed class AuthState {
    /**
     * 初始状态
     */
    object Initial : AuthState()
    
    /**
     * 加载中
     */
    object Loading : AuthState()
    
    /**
     * 已认证（激活成功）
     */
    data class Authenticated(val licenseKey: String) : AuthState()
    
    /**
     * 未认证（需要登录）
     */
    object Unauthenticated : AuthState()
    
    /**
     * 错误
     */
    data class Error(val message: String) : AuthState()
}
