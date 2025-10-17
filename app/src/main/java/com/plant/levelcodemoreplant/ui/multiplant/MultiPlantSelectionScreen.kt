package com.plant.levelcodemoreplant.ui.multiplant

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.plant.levelcodemoreplant.ui.theme.*

/**
 * 植物选择界面（宫格+底栏）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiPlantSelectionScreen(
    uiState: MultiPlantUiState,
    onBack: () -> Unit,
    onPlantClick: (String) -> Unit,
    onGenerate: () -> Unit,
    onErrorDismiss: () -> Unit
) {
    val mode = uiState.currentMode ?: return
    val selectedCount = uiState.selectedPlants.size
    val canGenerate = selectedCount == mode.selectCount
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = mode.displayName,
                            fontWeight = FontWeight.Bold
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
                    // 进度指示
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = when {
                            canGenerate -> AccentGreen
                            selectedCount > 0 -> AccentOrange
                            else -> Color.White.copy(alpha = 0.3f)
                        }
                    ) {
                        Text(
                            text = "$selectedCount/${mode.selectCount}",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            // 底部栏：已选植物 + 生成按钮
            BottomBar(
                selectedPlants = uiState.selectedPlants,
                mode = mode,
                canGenerate = canGenerate,
                isGenerating = uiState.isGenerating,
                onPlantRemove = onPlantClick,
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
            // 植物宫格
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),  // 4列宫格
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(uiState.availablePlants) { plant ->
                    PlantGridItem(
                        plant = plant,
                        onClick = { onPlantClick(plant.id) }
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
                    Text("确定")
                }
            }
        )
    }
}

/**
 * 植物宫格项
 */
@Composable
fun PlantGridItem(
    plant: SelectablePlant,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),  // 正方形
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (plant.isSelected) 8.dp else 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (plant.isSelected) 
                PrimaryBlue.copy(alpha = 0.15f) 
            else 
                Color.White
        ),
        border = BorderStroke(
            width = if (plant.isSelected) 3.dp else 1.dp,
            color = if (plant.isSelected) PrimaryBlue else Color.LightGray
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
                // 植物emoji
                Text(
                    text = plant.emoji,
                    style = MaterialTheme.typography.displayMedium
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // 植物名称
                Text(
                    text = plant.name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (plant.isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (plant.isSelected) PrimaryBlue else TextPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // 选中标记
            if (plant.isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "已选中",
                    tint = AccentGreen,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(20.dp)
                )
            }
        }
    }
}

/**
 * 底部栏
 */
@Composable
fun BottomBar(
    selectedPlants: List<SelectablePlant>,
    mode: com.plant.levelcodemoreplant.data.model.MultiPlantMode,
    canGenerate: Boolean,
    isGenerating: Boolean,
    onPlantRemove: (String) -> Unit,
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
            // 已选植物列表（横向滚动）
            if (selectedPlants.isNotEmpty()) {
                Text(
                    text = "已选择 (${selectedPlants.size}/${mode.selectCount}):",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    items(selectedPlants) { plant ->
                        SelectedPlantChip(
                            plant = plant,
                            onRemove = { onPlantRemove(plant.id) }
                        )
                    }
                }
            }
            
            // 提示信息
            if (!canGenerate) {
                Text(
                    text = if (selectedPlants.size < mode.selectCount) {
                        "还需选择 ${mode.selectCount - selectedPlants.size} 个植物"
                    } else {
                        "超出限制，请取消 ${selectedPlants.size - mode.selectCount} 个"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (selectedPlants.size < mode.selectCount) TextHint else MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            // 生成按钮
            Button(
                onClick = onGenerate,
                enabled = canGenerate && !isGenerating,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentGreen,
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
                        text = "🎁 生成礼包码",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * 已选植物芯片
 */
@Composable
fun SelectedPlantChip(
    plant: SelectablePlant,
    onRemove: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = PrimaryBlue.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, PrimaryBlue)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = plant.emoji,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = plant.name,
                style = MaterialTheme.typography.bodyMedium,
                color = PrimaryBlue,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "移除",
                tint = PrimaryBlue,
                modifier = Modifier
                    .size(18.dp)
                    .clickable(onClick = onRemove)
            )
        }
    }
}
