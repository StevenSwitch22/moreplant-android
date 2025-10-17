package com.plant.levelcodemoreplant.ui.multiplant

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.plant.levelcodemoreplant.data.datasource.MultiPlantDataSource
import com.plant.levelcodemoreplant.data.model.MultiPlantMode
import com.plant.levelcodemoreplant.data.model.PlantPools
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 多植物礼包码ViewModel
 */
class MultiPlantViewModel(application: Application) : AndroidViewModel(application) {
    
    private val dataSource = MultiPlantDataSource(application)
    
    private val _uiState = MutableStateFlow(MultiPlantUiState())
    val uiState: StateFlow<MultiPlantUiState> = _uiState.asStateFlow()
    
    init {
        Log.d(TAG, "MultiPlantViewModel 初始化")
        // 预加载数据
        viewModelScope.launch {
            dataSource.preloadAllModes()
        }
    }
    
    /**
     * 选择模式
     */
    fun selectMode(mode: MultiPlantMode) {
        Log.d(TAG, "选择模式: ${mode.displayName}")
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // 加载该模式的植物列表
                val plantPool = PlantPools.getPlantPool(mode.id)
                val plantNames = dataSource.loadPlantNames()
                
                val availablePlants = plantPool.mapIndexed { index, plantId ->
                    SelectablePlant(
                        id = plantId,
                        name = plantNames[plantId] ?: "未知植物",
                        emoji = "🌱",  // 统一使用默认emoji
                        position = index,
                        isSelected = false
                    )
                }
                
                _uiState.update {
                    it.copy(
                        currentMode = mode,
                        availablePlants = availablePlants,
                        selectedPlants = emptyList(),
                        isLoading = false,
                        currentScreen = MultiPlantScreen.SELECTION
                    )
                }
                
                Log.d(TAG, "模式 ${mode.displayName} 加载完成，共 ${availablePlants.size} 个植物")
            } catch (e: Exception) {
                Log.e(TAG, "加载模式失败", e)
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
     * 切换植物选中状态
     */
    fun togglePlantSelection(plantId: String) {
        val currentState = _uiState.value
        val mode = currentState.currentMode ?: return
        val plant = currentState.availablePlants.find { it.id == plantId } ?: return
        
        val currentSelectedCount = currentState.selectedPlants.size
        
        if (plant.isSelected) {
            // 取消选中
            Log.d(TAG, "取消选中植物: ${plant.name}")
            _uiState.update { state ->
                state.copy(
                    availablePlants = state.availablePlants.map {
                        if (it.id == plantId) it.copy(isSelected = false) else it
                    },
                    selectedPlants = state.selectedPlants.filter { it.id != plantId }
                )
            }
        } else {
            // 检查是否已达上限
            if (currentSelectedCount >= mode.selectCount) {
                Log.w(TAG, "已达到选择上限")
                _uiState.update {
                    it.copy(errorMessage = "最多只能选择 ${mode.selectCount} 个植物")
                }
                // 2秒后清除错误消息
                viewModelScope.launch {
                    kotlinx.coroutines.delay(2000)
                    _uiState.update { it.copy(errorMessage = null) }
                }
                return
            }
            
            // 选中
            Log.d(TAG, "选中植物: ${plant.name}")
            _uiState.update { state ->
                state.copy(
                    availablePlants = state.availablePlants.map {
                        if (it.id == plantId) it.copy(isSelected = true) else it
                    },
                    selectedPlants = state.selectedPlants + plant.copy(isSelected = true)
                )
            }
        }
    }
    
    /**
     * 检查是否可以生成
     */
    fun canGenerate(): Boolean {
        val state = _uiState.value
        val mode = state.currentMode ?: return false
        return state.selectedPlants.size == mode.selectCount
    }
    
    /**
     * 生成礼包码
     */
    fun generateGiftCode() {
        val currentState = _uiState.value
        val mode = currentState.currentMode ?: return
        
        if (!canGenerate()) {
            _uiState.update {
                it.copy(errorMessage = "请选择 ${mode.selectCount} 个植物")
            }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true) }
            
            try {
                // 模拟生成过程
                kotlinx.coroutines.delay(500)
                
                // 查询礼包码
                val selectedIds = currentState.selectedPlants.map { it.id }
                val result = dataSource.queryGiftCode(mode.id, selectedIds)
                
                result.onSuccess { jsonObject ->
                    // 提取礼包码（格式化显示）
                    val giftCodeJson = jsonObject.toString(2)  // 缩进2格
                    
                    Log.d(TAG, "生成成功！")
                    _uiState.update {
                        it.copy(
                            isGenerating = false,
                            generatedCode = giftCodeJson,
                            currentScreen = MultiPlantScreen.RESULT
                        )
                    }
                }.onFailure { e ->
                    Log.e(TAG, "查询失败", e)
                    _uiState.update {
                        it.copy(
                            isGenerating = false,
                            errorMessage = e.message ?: "查询失败"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "生成失败", e)
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
     * 返回模式选择界面
     */
    fun backToModeSelection() {
        Log.d(TAG, "返回模式选择")
        _uiState.update {
            MultiPlantUiState(currentScreen = MultiPlantScreen.MODE_SELECTION)
        }
    }
    
    /**
     * 返回植物选择界面
     */
    fun backToPlantSelection() {
        Log.d(TAG, "返回植物选择")
        _uiState.update {
            it.copy(
                currentScreen = MultiPlantScreen.SELECTION,
                generatedCode = null
            )
        }
    }
    
    /**
     * 清除错误消息
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    companion object {
        private const val TAG = "MultiPlantViewModel"
    }
}

/**
 * 可选植物
 */
data class SelectablePlant(
    val id: String,           // 植物编号
    val name: String,         // 植物名称
    val emoji: String,        // emoji占位符
    val position: Int,        // 在植物池中的位置
    val isSelected: Boolean   // 是否已选中
)

/**
 * 界面状态
 */
enum class MultiPlantScreen {
    MODE_SELECTION,  // 模式选择
    SELECTION,       // 植物选择
    RESULT           // 结果展示
}

/**
 * UI状态
 */
data class MultiPlantUiState(
    val currentScreen: MultiPlantScreen = MultiPlantScreen.MODE_SELECTION,
    val currentMode: MultiPlantMode? = null,
    val availablePlants: List<SelectablePlant> = emptyList(),
    val selectedPlants: List<SelectablePlant> = emptyList(),
    val generatedCode: String? = null,
    val isLoading: Boolean = false,
    val isGenerating: Boolean = false,
    val errorMessage: String? = null
)
