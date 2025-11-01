package com.plant.levelcodemoreplant.ui.plantsearch

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Divider
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.plant.levelcodemoreplant.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleLevelScreen(
    viewModel: SimpleLevelViewModel = viewModel(),
    onNavigateToMultiPlant: () -> Unit = {},    // 导航到多植物界面
    onNavigateToSinglePlant: () -> Unit = {},   // 导航到单植物界面
    onNavigateToMultiCostume: () -> Unit = {}   // 导航到多装扮界面
) {
    Log.d("SimpleLevelScreen", "开始渲染")
    
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "🎁 礼包兑换码生成",
                        fontWeight = FontWeight.Bold
                    ) 
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
            // 功能区 - 两个按钮分别占一行
            // 单植物/装扮礼包按钮
            Card(
                onClick = onNavigateToSinglePlant,
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = PrimaryBlue.copy(alpha = 0.15f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🎁",
                            style = MaterialTheme.typography.displaySmall
                        )
                        Column {
                            Text(
                                text = "单植物/装扮礼包",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                            Text(
                                text = "搜索单个植物生成礼包",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "搜索",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 多植物礼包按钮
            Card(
                onClick = onNavigateToMultiPlant,
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = AccentOrange.copy(alpha = 0.15f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🎯",
                            style = MaterialTheme.typography.displaySmall
                        )
                        Column {
                            Text(
                                text = "多植物礼包",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = AccentOrange
                            )
                            Text(
                                text = "从多个植物中选择组合生成",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "进入",
                        tint = AccentOrange,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 多装扮礼包按钮 - 新增
            Card(
                onClick = onNavigateToMultiCostume,
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = CostumePurple.copy(alpha = 0.15f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "👗",
                            style = MaterialTheme.typography.displaySmall
                        )
                        Column {
                            Text(
                                text = "多装扮礼包",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = CostumePurple
                            )
                            Text(
                                text = "12个超级装扮任意选择",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "进入",
                        tint = CostumePurple,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 内容区域
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.searchQuery.isEmpty() -> {
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
                                text = "选择礼包名称开始生成",
                                color = TextSecondary,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                }
                uiState.plantItems.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "😔",
                                style = MaterialTheme.typography.displayMedium
                            )
                            Text(
                                text = "未找到相关礼包",
                                color = TextSecondary,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = PrimaryBlue.copy(alpha = 0.1f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "✨ 找到 ${uiState.plantItems.size} 个植物",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = PrimaryBlue,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        items(uiState.plantItems) { plantItem ->
                            PlantItemCard(
                                plantItem = plantItem,
                                searchQuery = uiState.searchQuery,
                                onGenerate = { viewModel.generateCode(plantItem.id) },
                                onToggleExpand = { viewModel.toggleExpanded(plantItem.id) },
                                onCopy = { copyToClipboard(context, plantItem.jsonCode) }
                            )
                        }
                    }
                }
            }
            
            // 错误提示
            uiState.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * 植物项卡片组件
 */
@Composable
fun PlantItemCard(
    plantItem: PlantItem,
    searchQuery: String,
    onGenerate: () -> Unit,
    onToggleExpand: () -> Unit,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp))
            .animateContentSize(  // 添加展开/收起动画
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // 植物名称和按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 高亮显示植物名
                HighlightedText(
                    text = plantItem.name,
                    highlight = searchQuery,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // 状态按钮
                when {
                    plantItem.isGenerating -> {
                        // 正在生成
                        GeneratingButton()
                    }
                    plantItem.isGenerated -> {
                        // 已生成
                        GeneratedButton(onToggleExpand)
                    }
                    else -> {
                        // 未生成
                        GenerateButton(onGenerate)
                    }
                }
            }
            
            // 展开显示兑换码
            if (plantItem.isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // 分割线
                Divider(
                    color = TextHint.copy(alpha = 0.3f),
                    thickness = 1.dp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 代码标签
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✨ 兑换码",
                        style = MaterialTheme.typography.labelLarge,
                        color = PrimaryBlue,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 兑换码内容
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
                
                // 复制按钮
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
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "复制",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "📋 复制兑换码",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * 生成按钮
 */
@Composable
fun GenerateButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.height(40.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryBlue,
            contentColor = Color.White
        )
    ) {
        Text(
            text = "⚡ 生成",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 正在生成按钮（加载中）
 */
@Composable
fun GeneratingButton() {
    Button(
        onClick = { },
        enabled = false,
        modifier = Modifier.height(40.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            disabledContainerColor = PrimaryBlue.copy(alpha = 0.6f),
            disabledContentColor = Color.White
        )
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            color = Color.White,
            strokeWidth = 2.dp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "生成中",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 已生成按钮
 */
@Composable
fun GeneratedButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.height(40.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AccentGreen,
            contentColor = Color.White
        )
    ) {
        Text(
            text = "✅ 已生成",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 高亮文本组件
 */
@Composable
fun HighlightedText(
    text: String,
    highlight: String,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyLarge,
    color: Color = TextPrimary,
    highlightColor: Color = AccentOrange
) {
    if (highlight.isBlank()) {
        Text(
            text = "🌱 $text",
            modifier = modifier,
            style = style,
            color = color,
            fontWeight = FontWeight.Bold
        )
        return
    }
    
    val annotatedString = buildAnnotatedString {
        append("🌱 ")
        var startIndex = 0
        var currentIndex = text.indexOf(highlight, startIndex, ignoreCase = true)
        
        while (currentIndex >= 0) {
            // 添加高亮前的普通文本
            append(text.substring(startIndex, currentIndex))
            
            // 添加高亮文本
            withStyle(
                style = SpanStyle(
                    color = highlightColor,
                    fontWeight = FontWeight.ExtraBold,
                    background = highlightColor.copy(alpha = 0.2f)
                )
            ) {
                append(text.substring(currentIndex, currentIndex + highlight.length))
            }
            
            startIndex = currentIndex + highlight.length
            currentIndex = text.indexOf(highlight, startIndex, ignoreCase = true)
        }
        
        // 添加剩余文本
        if (startIndex < text.length) {
            append(text.substring(startIndex))
        }
    }
    
    Text(
        text = annotatedString,
        modifier = modifier,
        style = style.copy(fontWeight = FontWeight.Bold),
        color = color
    )
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("兑换码", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "✅ 已复制到剪贴板！", Toast.LENGTH_SHORT).show()
}
