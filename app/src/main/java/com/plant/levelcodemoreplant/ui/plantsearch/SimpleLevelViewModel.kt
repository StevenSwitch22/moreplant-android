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
            
            // 调用接口获取模糊搜索建议
            val result = dataSource.getSuggestions(query)
            
            result.onSuccess { suggestions ->
                // 转换为植物项（此时还没有兑换码）
                val plantItems = suggestions.map { name ->
                    PlantItem(
                        id = name,  // 使用名称作为ID
                        name = name,
                        jsonCode = ""  // 暂时为空，点击生成时再查询
                    )
                }
                
                Log.d(TAG, "搜索 '$query'，找到 ${plantItems.size} 个结果")
                _uiState.update { it.copy(plantItems = plantItems) }
            }.onFailure { e ->
                Log.e(TAG, "搜索失败", e)
                _uiState.update { it.copy(
                    plantItems = emptyList(),
                    errorMessage = e.message
                ) }
            }
        }
    }
    
    fun clearSearch() {
        searchJob?.cancel()
        _uiState.update { it.copy(searchQuery = "", plantItems = emptyList()) }
    }
    
    /**
     * 生成兑换码（从服务器查询）
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
            
            try {
                // 从服务器查询兑换码
                val result = dataSource.searchCodeFromServer(plantId)
                
                result.onSuccess { jsonCode ->
                    // 生成完成，展开显示
                    _uiState.update { state ->
                        val updatedItems = state.plantItems.map { item ->
                            if (item.id == plantId) {
                                item.copy(
                                    isGenerating = false,
                                    isGenerated = true,
                                    isExpanded = true,
                                    jsonCode = jsonCode  // 更新为服务器返回的兑换码
                                )
                            } else item
                        }
                        state.copy(plantItems = updatedItems)
                    }
                    Log.d(TAG, "植物 '$plantId' 兑换码生成完成")
                }.onFailure { e ->
                    // 生成失败
                    _uiState.update { state ->
                        val updatedItems = state.plantItems.map { item ->
                            if (item.id == plantId) {
                                item.copy(isGenerating = false)
                            } else item
                        }
                        state.copy(
                            plantItems = updatedItems,
                            errorMessage = e.message
                        )
                    }
                    Log.e(TAG, "植物 '$plantId' 兑换码生成失败", e)
                }
            } catch (e: Exception) {
                // 异常处理
                _uiState.update { state ->
                    val updatedItems = state.plantItems.map { item ->
                        if (item.id == plantId) {
                            item.copy(isGenerating = false)
                        } else item
                    }
                    state.copy(
                        plantItems = updatedItems,
                        errorMessage = "生成失败: ${e.message}"
                    )
                }
                Log.e(TAG, "生成兑换码异常", e)
            }
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
            
            // 调用接口获取模糊搜索建议
            val result = dataSource.getSuggestions(query)
            
            result.onSuccess { suggestions ->
                val plantItems = suggestions.map { name ->
                    PlantItem(
                        id = name,
                        name = name,
                        jsonCode = ""  // 暂时为空
                    )
                }
                
                _uiState.update { it.copy(plantItems = plantItems, matchedCount = 0) }
            }.onFailure { e ->
                Log.e(TAG, "搜索失败", e)
                _uiState.update { it.copy(
                    plantItems = emptyList(),
                    matchedCount = 0,
                    errorMessage = e.message
                ) }
            }
        }
    }
    
    /**
     * 选择植物用于生成
     */
    fun selectPlantForGeneration(plantId: String) {
        _uiState.update { it.copy(selectedPlantId = plantId) }
    }
    
    /**
     * 开始生成礼包（从服务器查询）
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
                
                // 调用接口获取模糊搜索建议
                val suggestionsResult = dataSource.getSuggestions(query)
                
                if (suggestionsResult.isFailure) {
                    _uiState.update { it.copy(
                        isGenerating = false,
                        errorMessage = suggestionsResult.exceptionOrNull()?.message
                    ) }
                    return@launch
                }
                
                val suggestions = suggestionsResult.getOrNull() ?: emptyList()
                val matchedCount = suggestions.size
                val expectedPackages = if (matchedCount > 0) 1 else 0
                
                _uiState.update { it.copy(
                    matchedCount = matchedCount,
                    expectedPackages = expectedPackages
                ) }
                
                // 阶段2: 从服务器查询兑换码 (30-90%)
                val generatorResults = mutableListOf<PlantItem>()
                suggestions.forEachIndexed { index, name ->
                    // 更新进度
                    val progress = 0.3f + (index.toFloat() / suggestions.size) * 0.6f
                    _uiState.update { it.copy(generationProgress = progress) }
                    
                    // 查询兑换码
                    val result = dataSource.searchCodeFromServer(name)
                    result.onSuccess { jsonCode ->
                        generatorResults.add(
                            PlantItem(
                                id = name,
                                name = name,
                                jsonCode = jsonCode,
                                isGenerated = true,
                                isExpanded = true
                            )
                        )
                    }.onFailure { e ->
                        Log.w(TAG, "查询 $name 失败: ${e.message}")
                        // 查询失败的也添加，但标记为未生成
                        generatorResults.add(
                            PlantItem(
                                id = name,
                                name = name,
                                jsonCode = "查询失败: ${e.message}",
                                isGenerated = false,
                                isExpanded = false
                            )
                        )
                    }
                }
                
                // 阶段3: 完成 (90-100%)
                for (i in 9..10) {
                    delay(100)
                    _uiState.update { it.copy(generationProgress = i * 0.1f) }
                }
                
                _uiState.update { it.copy(
                    isGenerating = false,
                    plantItems = emptyList(),
                    generatorResults = generatorResults,
                    selectedPlantId = null
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
