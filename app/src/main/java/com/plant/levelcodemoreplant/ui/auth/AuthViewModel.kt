package com.plant.levelcodemoreplant.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.plant.levelcodemoreplant.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 认证 ViewModel
 * 管理认证相关的 UI 逻辑
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = AuthRepository(application.applicationContext)
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    init {
        checkActivationStatus()
    }
    
    /**
     * 检查激活状态
     */
    fun checkActivationStatus() {
        if (repository.isActivated()) {
            val licenseKey = repository.getSavedLicenseKey() ?: ""
            _authState.value = AuthState.Authenticated(licenseKey)
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }
    
    /**
     * 激活卡密
     */
    fun activateLicense(licenseKey: String) {
        // 验证输入
        if (licenseKey.isBlank()) {
            _authState.value = AuthState.Error("请输入卡密")
            return
        }
        
        // 开始加载
        _authState.value = AuthState.Loading
        
        viewModelScope.launch {
            repository.activateLicense(licenseKey)
                .onSuccess { message ->
                    _authState.value = AuthState.Authenticated(licenseKey)
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(
                        error.message ?: "激活失败"
                    )
                }
        }
    }
    
    /**
     * 重置错误状态
     */
    fun resetError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Unauthenticated
        }
    }
    
    /**
     * 清除激活状态（用于测试）
     */
    fun clearActivation() {
        repository.clearActivation()
        _authState.value = AuthState.Unauthenticated
    }
}
