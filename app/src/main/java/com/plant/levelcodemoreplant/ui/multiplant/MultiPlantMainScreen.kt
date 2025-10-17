package com.plant.levelcodemoreplant.ui.multiplant

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * 多植物礼包主界面 - 根据状态展示不同的子界面
 */
@Composable
fun MultiPlantMainScreen(
    onBack: () -> Unit,
    viewModel: MultiPlantViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    when (uiState.currentScreen) {
        MultiPlantScreen.MODE_SELECTION -> {
            MultiPlantModeSelectionScreen(
                onBack = onBack,
                onModeSelected = { mode ->
                    viewModel.selectMode(mode)
                }
            )
        }
        
        MultiPlantScreen.SELECTION -> {
            MultiPlantSelectionScreen(
                uiState = uiState,
                onBack = { viewModel.backToModeSelection() },
                onPlantClick = { plantId ->
                    viewModel.togglePlantSelection(plantId)
                },
                onGenerate = {
                    viewModel.generateGiftCode()
                },
                onErrorDismiss = {
                    viewModel.clearError()
                }
            )
        }
        
        MultiPlantScreen.RESULT -> {
            MultiPlantResultScreen(
                uiState = uiState,
                onBack = { viewModel.backToPlantSelection() },
                onBackToModeSelection = { viewModel.backToModeSelection() }
            )
        }
    }
}
