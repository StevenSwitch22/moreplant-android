package com.plant.levelcodemoreplant.ui.plantsearch

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.plant.levelcodemoreplant.data.datasource.LevelDataSource
import com.plant.levelcodemoreplant.data.model.LevelData
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 简化版 ViewModel - 只支持礼包查询
 */
class SimpleLevelViewModel(application: Application) : AndroidViewModel(application) {
    
    private val dataSource = LevelDataSource(application)
    private var cachedLevels: List<LevelData>? = null
    
    private val _uiState = MutableStateFlow(SimpleLevelUiState())
    val uiState: StateFlow<SimpleLevelUiState> = _uiState.asStateFlow()
    
    private var searchJob: Job? = null
    
    init {
        Log.d(TAG, "SimpleLevelViewModel 初始化")
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                Log.d(TAG, "开始加载礼包数据")
                cachedLevels = dataSource.loadLevels()
                Log.d(TAG, "加载完成，共 ${cachedLevels?.size} 个礼包")
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                Log.e(TAG, "加载失败", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = "加载失败: ${e.message}") }
            }
        }
    }
    
    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        
        searchJob?.cancel()
        
        if (query.isBlank()) {
            _uiState.update { it.copy(plantItems = emptyList()) }
            return
        }
        
        searchJob = viewModelScope.launch {
            delay(300)
            
            val levels = cachedLevels ?: emptyList()
            val results = levels.filter { it.name.contains(query, ignoreCase = true) }
            
            // 转换为植物项
            val plantItems = results.map { level ->
                PlantItem(
                    id = level.name,  // 使用名称作为ID
                    name = level.name,
                    jsonCode = level.jsonCode
                )
            }
            
            Log.d(TAG, "搜索 '$query'，找到 ${plantItems.size} 个结果")
            _uiState.update { it.copy(plantItems = plantItems) }
        }
    }
    
    fun clearSearch() {
        searchJob?.cancel()
        _uiState.update { it.copy(searchQuery = "", plantItems = emptyList()) }
    }
    
    /**
     * 生成兑换码（模拟生成过程）
     */
    fun generateCode(plantId: String) {
        viewModelScope.launch {
            // 设置为正在生成
            _uiState.update { state ->
                val updatedItems = state.plantItems.map { item ->
                    if (item.id == plantId) {
                        item.copy(isGenerating = true, isExpanded = false)
                    } else item
                }
                state.copy(plantItems = updatedItems)
            }
            
            // 模拟生成过程（1-2秒）
            val loadingTime = (1000L..2000L).random()
            delay(loadingTime)
            
            // 生成完成，展开显示
            _uiState.update { state ->
                val updatedItems = state.plantItems.map { item ->
                    if (item.id == plantId) {
                        item.copy(
                            isGenerating = false, 
                            isGenerated = true,
                            isExpanded = true
                        )
                    } else item
                }
                state.copy(plantItems = updatedItems)
            }
            
            Log.d(TAG, "植物 '$plantId' 兑换码生成完成")
        }
    }
    
    /**
     * 切换展开/收起状态
     */
    fun toggleExpanded(plantId: String) {
        _uiState.update { state ->
            val updatedItems = state.plantItems.map { item ->
                if (item.id == plantId && item.isGenerated) {
                    item.copy(isExpanded = !item.isExpanded)
                } else item
            }
            state.copy(plantItems = updatedItems)
        }
    }
    
    /**
     * 显示生成器对话框
     */
    fun showGeneratorDialog() {
        _uiState.update { it.copy(
            showGeneratorDialog = true,
            generatorQuery = "",
            isGenerating = false,
            generationProgress = 0f,
            matchedCount = 0,
            expectedPackages = 0
        ) }
    }
    
    /**
     * 关闭生成器对话框
     */
    fun hideGeneratorDialog() {
        _uiState.update { it.copy(
            showGeneratorDialog = false,
            isGenerating = false
        ) }
    }
    
    /**
     * 更新生成器查询
     */
    fun onGeneratorQueryChange(query: String) {
        _uiState.update { it.copy(
            generatorQuery = query,
            generatorResults = emptyList()  // 清除之前的生成结果
        ) }
        
        // 实时搜索匹配
        searchJob?.cancel()
        
        if (query.isBlank()) {
            _uiState.update { it.copy(
                plantItems = emptyList(), 
                matchedCount = 0,
                selectedPlantId = null  // 只有在查询为空时才清除选中状态
            ) }
            return
        }
        
        searchJob = viewModelScope.launch {
            delay(300)
            
            val levels = cachedLevels ?: emptyList()
            val results = levels.filter { it.name.contains(query, ignoreCase = true) }
            
            val plantItems = results.map { level ->
                PlantItem(
                    id = level.name,
                    name = level.name,
                    jsonCode = level.jsonCode
                )
            }
            
            _uiState.update { it.copy(plantItems = plantItems, matchedCount = 0) }
        }
    }
    
    /**
     * 选择植物用于生成
     */
    fun selectPlantForGeneration(plantId: String) {
        _uiState.update { it.copy(selectedPlantId = plantId) }
    }
    
    /**
     * 开始生成礼包（带动画效果）
     */
    fun startGeneration() {
        val query = _uiState.value.generatorQuery
        if (query.isBlank()) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(
                isGenerating = true,
                generationProgress = 0f,
                matchedCount = 0,
                expectedPackages = 0
            ) }
            
            try {
                // 阶段1: 搜索匹配 (0-30%)
                for (i in 1..3) {
                    delay(200)
                    _uiState.update { it.copy(generationProgress = i * 0.1f) }
                }
                
                // 查找匹配结果
                val levels = cachedLevels ?: emptyList()
                val results = levels.filter { it.name.contains(query, ignoreCase = true) }
                val matchedCount = results.size
                val expectedPackages = if (matchedCount > 0) 1 else 0
                
                _uiState.update { it.copy(
                    matchedCount = matchedCount,
                    expectedPackages = expectedPackages
                ) }
                
                // 阶段2: 分析数据 (30-60%)
                for (i in 4..6) {
                    delay(300)
                    _uiState.update { it.copy(generationProgress = i * 0.1f) }
                }
                
                // 阶段3: 生成礼包 (60-100%)
                for (i in 7..10) {
                    delay(250)
                    _uiState.update { it.copy(generationProgress = i * 0.1f) }
                }
                
                // 生成完成
                delay(300)
                
                // 转换为植物项并显示结果
                val generatorResults = results.map { level ->
                    PlantItem(
                        id = level.name,
                        name = level.name,
                        jsonCode = level.jsonCode,
                        isGenerated = true,
                        isExpanded = true  // 默认展开
                    )
                }
                
                _uiState.update { it.copy(
                    isGenerating = false,
                    plantItems = emptyList(),  // 清空搜索提示列表
                    generatorResults = generatorResults,  // 专门用于生成器结果
                    selectedPlantId = null  // 清除选中状态
                ) }
                
                Log.d(TAG, "生成器: 搜索 '$query'，找到 ${generatorResults.size} 个结果")
                
            } catch (e: Exception) {
                Log.e(TAG, "生成失败", e)
                _uiState.update { it.copy(
                    isGenerating = false,
                    errorMessage = "生成失败: ${e.message}"
                ) }
            }
        }
    }
    
    companion object {
        private const val TAG = "SimpleLevelViewModel"
    }
}

/**
 * 植物项状态
 */
data class PlantItem(
    val id: String,  // 唯一ID
    val name: String,  // 植物名称
    val jsonCode: String,  // 兑换码
    val isGenerating: Boolean = false,  // 是否正在生成
    val isGenerated: Boolean = false,  // 是否已生成
    val isExpanded: Boolean = false  // 是否展开显示兑换码
)

data class SimpleLevelUiState(
    val searchQuery: String = "",
    val plantItems: List<PlantItem> = emptyList(),  // 改为植物项列表
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    // 生成器对话框状态
    val showGeneratorDialog: Boolean = false,
    val generatorQuery: String = "",
    val isGenerating: Boolean = false,
    val generationProgress: Float = 0f,  // 0.0 - 1.0
    val matchedCount: Int = 0,
    val expectedPackages: Int = 0,
    val selectedPlantId: String? = null,  // 选中的植物ID
    val generatorResults: List<PlantItem> = emptyList()  // 生成器专用结果
)
