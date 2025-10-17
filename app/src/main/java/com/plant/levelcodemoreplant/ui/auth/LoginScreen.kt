package com.plant.levelcodemoreplant.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.plant.levelcodemoreplant.R

/**
 * 卡密登录界面
 */
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    var licenseKey by remember { mutableStateOf(TextFieldValue("")) }
    val authState by viewModel.authState.collectAsState()
    
    // 监听认证状态，成功后跳转
    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            onLoginSuccess()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // APP 图标
            Image(
                painter = painterResource(id = R.drawable.ic_launcher),
                contentDescription = "APP Logo",
                modifier = Modifier.size(100.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 标题
            Text(
                text = "欢迎使用",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "请输入您的卡密激活应用",
                fontSize = 14.sp,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 卡密输入框
            OutlinedTextField(
                value = licenseKey,
                onValueChange = { 
                    licenseKey = it
                    // 清除错误状态
                    if (authState is AuthState.Error) {
                        viewModel.resetError()
                    }
                },
                label = { Text("卡密") }, //, color = Color.Gray
                placeholder = { Text("请输入卡密，例如：ABCD-1234-EFGH-5678", color = Color.LightGray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                enabled = authState !is AuthState.Loading,
                colors = OutlinedTextFieldDefaults.colors(
                    // 边框颜色
                    focusedBorderColor = Color(0xFF3498db),
                    unfocusedBorderColor = Color.LightGray,
                    // 输入文本颜色（重要！）
                    focusedTextColor = Color(0xFF2C3E50),
                    unfocusedTextColor = Color(0xFF2C3E50),
                    disabledTextColor = Color.Gray,
                    // 光标颜色
                    cursorColor = Color(0xFF3498db),
                    // 标签颜色
                    focusedLabelColor = Color(0xFF3498db),
                    unfocusedLabelColor = Color.Gray,
                    // 背景颜色
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 错误提示
            if (authState is AuthState.Error) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = (authState as AuthState.Error).message,
                        color = Color(0xFFD32F2F),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // 激活按钮
            Button(
                onClick = {
                    viewModel.activateLicense(licenseKey.text)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = authState !is AuthState.Loading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3498db)
                )
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "激活",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 帮助文本
            Text(
                text = "如遇问题，请联系开发者",
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            
            // 测试用：清除激活按钮（生产环境删除）
            /*
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = { viewModel.clearActivation() }) {
                Text("清除激活状态（测试）", color = Color.Red)
            }
            */
        }
    }
}
