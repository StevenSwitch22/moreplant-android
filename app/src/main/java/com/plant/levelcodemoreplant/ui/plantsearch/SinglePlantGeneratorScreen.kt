package com.plant.levelcodemoreplant.ui.plantsearch

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.plant.levelcodemoreplant.ui.theme.*

/**
 * 单植物礼包生成器界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SinglePlantGeneratorScreen(
    viewModel: SimpleLevelViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "🎁 单植物/装扮礼包",
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
                    containerColor = PrimaryBlue,
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
        ) {
            // 搜索输入框
            OutlinedTextField(
                value = uiState.generatorQuery,
                onValueChange = { viewModel.onGeneratorQueryChange(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { 
                    Text(
                        text = "输入植物或装扮名称...",
                        color = TextHint
                    ) 
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        "搜索",
                        tint = PrimaryBlue
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = TextHint.copy(alpha = 0.3f),
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = PrimaryBlue
                ),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 生成中状态显示
            if (uiState.isGenerating) {
                GeneratingProgressCard(
                    query = uiState.generatorQuery,
                    progress = uiState.generationProgress,
                    matchedCount = uiState.matchedCount,
                    expectedPackages = uiState.expectedPackages
                )
            }
            // 显示生成结果（优先级最高）
            else if (uiState.generatorResults.isNotEmpty()) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.generatorResults) { plantItem ->
                        GeneratedResultCard(
                            plantItem = plantItem,
                            onCopy = { copyToClipboard(context, plantItem.jsonCode) }
                        )
                    }
                }
            }
            // 显示生成按钮（已选择植物）
            else if (uiState.selectedPlantId != null && !uiState.isGenerating) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.startGeneration() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "✨ 生成礼包",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            // 搜索提示列表（未选择植物且有搜索结果）
            else if (uiState.generatorQuery.isNotEmpty() && uiState.selectedPlantId == null && uiState.plantItems.isNotEmpty()) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.plantItems) { plantItem ->
                        SuggestionItem(
                            name = plantItem.name,
                            onClick = {
                                viewModel.onGeneratorQueryChange(plantItem.name)
                                viewModel.selectPlantForGeneration(plantItem.id)
                            }
                        )
                    }
                }
            }
            // 空状态
            else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "✨",
                            style = MaterialTheme.typography.displayLarge
                        )
                        Text(
                            text = "输入植物或装扮名称开始生成",
                            color = TextSecondary,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            }
        }
    }
}

/**
 * 搜索提示项
 */
@Composable
fun SuggestionItem(
    name: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🌱",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * 生成进度卡片
 */
@Composable
fun GeneratingProgressCard(
    query: String,
    progress: Float,
    matchedCount: Int,
    expectedPackages: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "⚙️ 正在智能分析...",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue
            )
            
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = PrimaryBlue.copy(alpha = 0.1f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = query,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )
            }
            
            // 进度百分比
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = PrimaryBlue
            )
            
            // 进度条
            val animatedProgress by animateFloatAsState(
                targetValue = progress,
                animationSpec = tween(
                    durationMillis = 300,
                    easing = FastOutSlowInEasing
                ),
                label = "progress"
            )
            
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = PrimaryBlue,
                trackColor = PrimaryBlue.copy(alpha = 0.2f)
            )
            
            // 状态信息
            if (matchedCount > 0) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "✓ 已匹配",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary
                        )
                        Text(
                            text = "$matchedCount 个相关物品",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = AccentGreen
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "🎁 预计生成",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary
                        )
                        Text(
                            text = "$expectedPackages 个礼包",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                    }
                }
            }
        }
    }
}

/**
 * 生成结果卡片
 */
@Composable
fun GeneratedResultCard(
    plantItem: PlantItem,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "✅ 生成成功",
                style = MaterialTheme.typography.titleMedium,
                color = AccentGreen,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "🌱 ${plantItem.name}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Divider(
                color = TextHint.copy(alpha = 0.3f),
                thickness = 1.dp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "✨ 兑换码",
                style = MaterialTheme.typography.labelLarge,
                color = PrimaryBlue,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SurfaceLight,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = plantItem.jsonCode,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    color = TextPrimary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onCopy,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentGreen,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "📋 复制兑换码",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("兑换码", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "✅ 已复制到剪贴板！", Toast.LENGTH_SHORT).show()
}
