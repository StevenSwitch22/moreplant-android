package com.plant.levelcodemoreplant.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.plant.levelcodemoreplant.R
import com.plant.levelcodemoreplant.ui.theme.PrimaryBlue
import com.plant.levelcodemoreplant.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onTimeout: () -> Unit
) {
    // 启动画面显示时长，减少到 800ms 提升响应速度
    LaunchedEffect(Unit) {
        delay(800) // 0.8 秒后自动跳转
        onTimeout()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // APP 图标
            Image(
                painter = painterResource(id = R.drawable.ic_launcher),
                contentDescription = "APP Logo",
                modifier = Modifier.size(120.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // APP 名称
            Text(
                text = "v330文本生成器",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Slogan
            Text(
                text = "礼包码生成 | 快速便捷",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                letterSpacing = 1.sp
            )
        }
    }
}
