package com.plant.levelcodemoreplant.ui.multicostume

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * 多装扮礼包主导航界面
 */
@Composable
fun MultiCostumeMainScreen(
    onBack: () -> Unit,
    viewModel: MultiCostumeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    when (uiState.currentScreen) {
        MultiCostumeScreen.SELECTION -> {
            // 装扮选择界面
            MultiCostumeSelectionScreen(
                uiState = uiState,
                onBack = onBack,
                onCostumeClick = { costumeId ->
                    viewModel.toggleCostumeSelection(costumeId)
                },
                onGenerate = {
                    viewModel.generateGiftCode()
                },
                onErrorDismiss = {
                    viewModel.clearError()
                }
            )
        }
        MultiCostumeScreen.RESULT -> {
            // 结果展示界面
            MultiCostumeResultScreen(
                uiState = uiState,
                onBack = {
                    viewModel.backToSelection()
                },
                onReset = {
                    viewModel.resetSelection()
                }
            )
        }
    }
}
