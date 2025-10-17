package com.plant.levelcodemoreplant.ui.multiplant

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.plant.levelcodemoreplant.ui.theme.*

/**
 * 结果展示界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiPlantResultScreen(
    uiState: MultiPlantUiState,
    onBack: () -> Unit,
    onBackToModeSelection: () -> Unit
) {
    val mode = uiState.currentMode ?: return
    val context = LocalContext.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "🎉 生成成功",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "返回",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AccentGreen,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 成功提示卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = AccentGreen.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "✅",
                        style = MaterialTheme.typography.displayLarge
                    )
                    Text(
                        text = "礼包码生成成功！",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = AccentGreen
                    )
                    Text(
                        text = "模式：${mode.displayName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
            
            // 已选植物列表
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = CardBackground
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "✨ 已选植物",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // 植物列表
                    uiState.selectedPlants.forEachIndexed { index, plant ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${index + 1}. ${plant.emoji}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = plant.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
            
            // 兑换码展示
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = CardBackground
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "🎁 兑换码",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // 兑换码内容
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceLight,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = uiState.generatedCode ?: "",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            color = TextPrimary
                        )
                    }
                }
            }
            
            // 操作按钮
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 复制按钮
                Button(
                    onClick = {
                        uiState.generatedCode?.let { code ->
                            copyToClipboard(context, code)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentGreen
                    )
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "复制",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "📋 复制兑换码",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // 重新选择按钮
                OutlinedButton(
                    onClick = onBackToModeSelection,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PrimaryBlue
                    )
                ) {
                    Text(
                        text = "🔄 重新选择",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("礼包兑换码", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "✅ 已复制到剪贴板！", Toast.LENGTH_SHORT).show()
}
