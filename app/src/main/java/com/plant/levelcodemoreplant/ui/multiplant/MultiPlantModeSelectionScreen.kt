package com.plant.levelcodemoreplant.ui.multiplant

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.plant.levelcodemoreplant.data.model.MultiPlantMode
import com.plant.levelcodemoreplant.data.model.PlantPools
import com.plant.levelcodemoreplant.ui.theme.*

/**
 * 模式选择界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiPlantModeSelectionScreen(
    onBack: () -> Unit,
    onModeSelected: (MultiPlantMode) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "🎯 选择模式",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 顶部说明
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
                            text = "💡 请选择多植物礼包模式",
                            style = MaterialTheme.typography.titleMedium,
                            color = PrimaryBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            // 模式列表
            items(PlantPools.MODES) { mode ->
                ModeCard(
                    mode = mode,
                    onClick = { onModeSelected(mode) }
                )
            }
        }
    }
}

/**
 * 模式卡片
 */
@Composable
fun ModeCard(
    mode: MultiPlantMode,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 模式名称
                Text(
                    text = "🌟 ${mode.displayName}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                
                // 描述
                Text(
                    text = mode.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                
                // 统计信息
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InfoChip(
                        label = "总数",
                        value = "${mode.totalCount}个"
                    )
                    InfoChip(
                        label = "需选",
                        value = "${mode.selectCount}个"
                    )
                }
            }
            
            // 箭头图标
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "选择",
                tint = PrimaryBlue,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

/**
 * 信息芯片
 */
@Composable
fun InfoChip(
    label: String,
    value: String
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = PrimaryBlue.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = TextHint
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue
            )
        }
    }
}
