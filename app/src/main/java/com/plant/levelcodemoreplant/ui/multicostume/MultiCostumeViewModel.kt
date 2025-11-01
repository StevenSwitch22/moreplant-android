package com.plant.levelcodemoreplant.ui.multicostume

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.plant.levelcodemoreplant.data.datasource.MultiCostumeDataSource
import com.plant.levelcodemoreplant.data.model.CostumePools
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 多装扮礼包码ViewModel
 */
class MultiCostumeViewModel(application: Application) : AndroidViewModel(application) {
    
    private val dataSource = MultiCostumeDataSource(application)
    
    private val _uiState = MutableStateFlow(MultiCostumeUiState())
    val uiState: StateFlow<MultiCostumeUiState> = _uiState.asStateFlow()
    
    init {
        Log.d(TAG, "MultiCostumeViewModel 初始化")
        loadCostumes()
        // 预加载数据
        viewModelScope.launch {
            dataSource.preloadData()
        }
    }
    
    /**
     * 加载装扮列表
     */
    private fun loadCostumes() {
        Log.d(TAG, "加载装扮列表")
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val costumeNames = dataSource.loadCostumeNames()
                val costumes = CostumePools.getCostumeIds().mapIndexed { index, costumeId ->
                    SelectableCostume(
                        id = costumeId,
                        name = costumeNames[costumeId] ?: "未知装扮",
                        emoji = "👗",  // 装扮占位符
                        position = index,
                        isSelected = false
                    )
                }
                
                _uiState.update {
                    it.copy(
                        availableCostumes = costumes,
                        isLoading = false,
                        currentScreen = MultiCostumeScreen.SELECTION
                    )
                }
                
                Log.d(TAG, "装扮列表加载完成，共 ${costumes.size} 个装扮")
            } catch (e: Exception) {
                Log.e(TAG, "加载装扮列表失败", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "加载失败: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * 切换装扮选中状态
     */
    fun toggleCostumeSelection(costumeId: String) {
        val currentState = _uiState.value
        val costume = currentState.availableCostumes.find { it.id == costumeId } ?: return
        
        val mode = CostumePools.MODE
        val currentSelectedCount = currentState.selectedCostumes.size
        
        if (costume.isSelected) {
            // 取消选中
            Log.d(TAG, "取消选中装扮: ${costume.name}")
            _uiState.update { state ->
                state.copy(
                    availableCostumes = state.availableCostumes.map {
                        if (it.id == costumeId) it.copy(isSelected = false) else it
                    },
                    selectedCostumes = state.selectedCostumes.filter { it.id != costumeId }
                )
            }
        } else {
            // 选中
            if (currentSelectedCount >= mode.maxSelect) {
                Log.w(TAG, "已达最大选择数量: ${mode.maxSelect}")
                _uiState.update {
                    it.copy(errorMessage = "最多只能选择 ${mode.maxSelect} 个装扮")
                }
                return
            }
            
            Log.d(TAG, "选中装扮: ${costume.name}")
            _uiState.update { state ->
                state.copy(
                    availableCostumes = state.availableCostumes.map {
                        if (it.id == costumeId) it.copy(isSelected = true) else it
                    },
                    selectedCostumes = state.selectedCostumes + costume.copy(isSelected = true)
                )
            }
        }
    }
    
    /**
     * 生成礼包码
     */
    fun generateGiftCode() {
        val selectedCostumes = _uiState.value.selectedCostumes
        if (selectedCostumes.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "请至少选择 1 个装扮") }
            return
        }
        
        Log.d(TAG, "生成礼包码，已选装扮数量: ${selectedCostumes.size}")
        
        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true) }
            
            try {
                val costumeIds = selectedCostumes
                    .sortedBy { it.position }  // 按原始位置排序
                    .map { it.id }
                
                // 模拟生成过程，增加用户体验感（1.2秒延迟）
                delay(600)
                
                val code = dataSource.generateCode(costumeIds)
                
                _uiState.update {
                    it.copy(
                        generatedCode = code,
                        isGenerating = false,
                        currentScreen = MultiCostumeScreen.RESULT
                    )
                }
                
                Log.d(TAG, "礼包码生成成功")
            } catch (e: Exception) {
                Log.e(TAG, "生成礼包码失败", e)
                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        errorMessage = "生成失败: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * 返回装扮选择界面
     */
    fun backToSelection() {
        _uiState.update {
            it.copy(
                currentScreen = MultiCostumeScreen.SELECTION,
                generatedCode = null
            )
        }
    }
    
    /**
     * 重置选择
     */
    fun resetSelection() {
        _uiState.update { state ->
            state.copy(
                availableCostumes = state.availableCostumes.map { it.copy(isSelected = false) },
                selectedCostumes = emptyList(),
                generatedCode = null,
                currentScreen = MultiCostumeScreen.SELECTION
            )
        }
    }
    
    /**
     * 清除错误信息
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    companion object {
        private const val TAG = "MultiCostumeViewModel"
    }
}

/**
 * 可选装扮
 */
data class SelectableCostume(
    val id: String,           // 装扮编号
    val name: String,         // 装扮名称
    val emoji: String,        // emoji占位符
    val position: Int,        // 在装扮池中的位置
    val isSelected: Boolean   // 是否已选中
)

/**
 * 界面状态
 */
enum class MultiCostumeScreen {
    SELECTION,       // 装扮选择
    RESULT           // 结果展示
}

/**
 * UI状态
 */
data class MultiCostumeUiState(
    val currentScreen: MultiCostumeScreen = MultiCostumeScreen.SELECTION,
    val availableCostumes: List<SelectableCostume> = emptyList(),
    val selectedCostumes: List<SelectableCostume> = emptyList(),
    val generatedCode: String? = null,
    val isLoading: Boolean = false,
    val isGenerating: Boolean = false,
    val errorMessage: String? = null
)
