package com.plant.levelcodemoreplant

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import com.plant.levelcodemoreplant.ui.auth.AuthState
import com.plant.levelcodemoreplant.ui.auth.AuthViewModel
import com.plant.levelcodemoreplant.ui.auth.LoginScreen
import com.plant.levelcodemoreplant.ui.multicostume.MultiCostumeMainScreen
import com.plant.levelcodemoreplant.ui.multiplant.MultiPlantMainScreen
import com.plant.levelcodemoreplant.ui.plantsearch.SimpleLevelScreen
import com.plant.levelcodemoreplant.ui.plantsearch.SinglePlantGeneratorScreen
import com.plant.levelcodemoreplant.ui.splash.SplashScreen
import com.plant.levelcodemoreplant.ui.theme.CcTheme

class MainActivity : ComponentActivity() {
    
    private lateinit var authViewModel: AuthViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 切换回正常主题，让启动画面平滑过渡到主界面
        setTheme(R.style.Theme_Cc)
        Log.d(TAG, "MainActivity onCreate")
        
        // 初始化 ViewModel
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        
        setContent {
            Log.d(TAG, "setContent 开始")
            CcTheme {
                MainScreen(authViewModel)
            }
        }
        Log.d(TAG, "MainActivity onCreate 完成")
    }
    
    companion object {
        private const val TAG = "MainActivity"
    }
}

@Composable
fun MainScreen(authViewModel: AuthViewModel) {
    var showSplash by remember { mutableStateOf(true) }
    var showMultiPlant by remember { mutableStateOf(false) }
    var showSinglePlant by remember { mutableStateOf(false) }
    var showMultiCostume by remember { mutableStateOf(false) }
    val authState by authViewModel.authState.collectAsState()
    
    if (showSplash) {
        // 显示启动画面（包含图标 + 文字）
        SplashScreen(
            onTimeout = { showSplash = false }
        )
    } else {
        // 根据认证状态显示不同界面
        when (authState) {
            is AuthState.Authenticated -> {
                // 已激活 -> 显示主界面或其他界面
                when {
                    showMultiPlant -> {
                        MultiPlantMainScreen(
                            onBack = { showMultiPlant = false }
                        )
                    }
                    showSinglePlant -> {
                        SinglePlantGeneratorScreen(
                            onBack = { showSinglePlant = false }
                        )
                    }
                    showMultiCostume -> {
                        MultiCostumeMainScreen(
                            onBack = { showMultiCostume = false }
                        )
                    }
                    else -> {
                        SimpleLevelScreen(
                            onNavigateToMultiPlant = { showMultiPlant = true },
                            onNavigateToSinglePlant = { showSinglePlant = true },
                            onNavigateToMultiCostume = { showMultiCostume = true }
                        )
                    }
                }
            }
            is AuthState.Unauthenticated, is AuthState.Error -> {
                // 未激活或错误 -> 显示登录界面
                LoginScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = {
                        // 登录成功后会自动切换到主界面
                    }
                )
            }
            else -> {
                // 初始化或加载中 -> 显示空白
                // 实际上启动画面会覆盖这个状态
            }
        }
    }
}
