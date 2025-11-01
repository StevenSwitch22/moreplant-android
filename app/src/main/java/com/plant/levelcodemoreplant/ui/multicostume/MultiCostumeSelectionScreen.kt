package com.plant.levelcodemoreplant.ui.multicostume

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.plant.levelcodemoreplant.data.model.CostumePools
import com.plant.levelcodemoreplant.ui.common.CostumeImage
import com.plant.levelcodemoreplant.ui.theme.*

/**
 * 装扮选择界面（12选任意）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiCostumeSelectionScreen(
    uiState: MultiCostumeUiState,
    onBack: () -> Unit,
    onCostumeClick: (String) -> Unit,
    onGenerate: () -> Unit,
    onErrorDismiss: () -> Unit
) {
    val mode = CostumePools.MODE
    val selectedCount = uiState.selectedCostumes.size
    val canGenerate = selectedCount >= mode.minSelect && selectedCount <= mode.maxSelect
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "👗 ${mode.displayName}",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "12个超级装扮任意选择",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
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
                actions = {
                    // 进度指示（紫色主题）
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = when {
                            canGenerate -> AccentGreen
                            selectedCount > 0 -> CostumePurpleLight
                            else -> Color.White.copy(alpha = 0.3f)
                        }
                    ) {
                        Text(
                            text = "$selectedCount/12",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CostumePurple,  // 紫色主题
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            // 底部栏：已选装扮 + 生成按钮
            CostumeBottomBar(
                selectedCostumes = uiState.selectedCostumes,
                mode = mode,
                canGenerate = canGenerate,
                isGenerating = uiState.isGenerating,
                onCostumeRemove = onCostumeClick,
                onGenerate = onGenerate
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 装扮宫格
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),  // 3列宫格
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(uiState.availableCostumes) { costume ->
                    CostumeGridItem(
                        costume = costume,
                        onClick = { onCostumeClick(costume.id) }
                    )
                }
            }
        }
    }
    
    // 错误提示
    uiState.errorMessage?.let { error ->
        AlertDialog(
            onDismissRequest = onErrorDismiss,
            title = { Text("提示") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = onErrorDismiss) {
                    Text("确定", color = CostumePurple)
                }
            }
        )
    }
}

/**
 * 装扮宫格项（紫色主题）
 */
@Composable
fun CostumeGridItem(
    costume: SelectableCostume,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f),  // 稍微高一点，适合装扮图片
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (costume.isSelected) 8.dp else 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (costume.isSelected) 
                CostumePurple.copy(alpha = 0.15f)  // 紫色选中背景
            else 
                Color.White
        ),
        border = BorderStroke(
            width = if (costume.isSelected) 3.dp else 1.dp,
            color = if (costume.isSelected) CostumePurple else Color.LightGray
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(8.dp)
            ) {
                // 装扮图片
                CostumeImage(
                    costumeId = costume.id,
                    emoji = costume.emoji,
                    modifier = Modifier
                        .size(80.dp)
                        .padding(4.dp)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // 装扮名称
                Text(
                    text = costume.name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (costume.isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (costume.isSelected) CostumePurple else TextPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // 选中标记
            if (costume.isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "已选中",
                    tint = CostumePurple,  // 紫色勾
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(24.dp)
                )
            }
        }
    }
}

/**
 * 底部栏（紫色主题）
 */
@Composable
fun CostumeBottomBar(
    selectedCostumes: List<SelectableCostume>,
    mode: com.plant.levelcodemoreplant.data.model.MultiCostumeMode,
    canGenerate: Boolean,
    isGenerating: Boolean,
    onCostumeRemove: (String) -> Unit,
    onGenerate: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 已选装扮列表（横向滚动）
            if (selectedCostumes.isNotEmpty()) {
                Text(
                    text = "已选装扮 (${selectedCostumes.size}/12):",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    items(selectedCostumes) { costume ->
                        SelectedCostumeChip(
                            costume = costume,
                            onRemove = { onCostumeRemove(costume.id) }
                        )
                    }
                }
            }
            
            // 提示信息
            if (!canGenerate) {
                Text(
                    text = if (selectedCostumes.isEmpty()) {
                        "💡 请至少选择 1 个装扮，最多可选 12 个"
                    } else {
                        "✅ 已选择 ${selectedCostumes.size} 个，可以继续选择或生成"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (selectedCostumes.isEmpty()) TextHint else CostumePurple,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            } else {
                Text(
                    text = "✨ 已选择 ${selectedCostumes.size} 个装扮，可以生成礼包码了！",
                    style = MaterialTheme.typography.bodySmall,
                    color = AccentGreen,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            // 生成按钮（紫色主题）
            Button(
                onClick = onGenerate,
                enabled = canGenerate && !isGenerating,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CostumePurple,  // 紫色按钮
                    disabledContainerColor = Color.LightGray
                )
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("生成中...")
                } else {
                    Text(
                        text = "👗 生成装扮礼包码",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * 已选装扮芯片（紫色主题）
 */
@Composable
fun SelectedCostumeChip(
    costume: SelectableCostume,
    onRemove: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = CostumePurple.copy(alpha = 0.15f),  // 紫色背景
        border = BorderStroke(1.dp, CostumePurple)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CostumeImage(
                costumeId = costume.id,
                emoji = costume.emoji,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = costume.name,
                style = MaterialTheme.typography.bodySmall,
                color = CostumePurple,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "移除",
                tint = CostumePurple,
                modifier = Modifier
                    .size(18.dp)
                    .clickable(onClick = onRemove)
            )
        }
    }
}
