package com.plant.levelcodemoreplant.data.datasource

import android.content.Context
import android.util.Log
import com.plant.levelcodemoreplant.data.model.PlantPools
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * 多植物礼包码数据源
 */
class MultiPlantDataSource(private val context: Context) {
    
    // 编号 -> 名称映射（懒加载）
    private var plantNameMap: Map<String, String>? = null
    
    // 模式ID -> (组合键 -> 礼包码JSON) 映射
    private val codeCacheMap = mutableMapOf<String, Map<String, JSONObject>>()
    
    /**
     * 加载植物名称映射
     */
    suspend fun loadPlantNames(): Map<String, String> = withContext(Dispatchers.IO) {
        if (plantNameMap != null) {
            return@withContext plantNameMap!!
        }
        
        try {
            Log.d(TAG, "开始加载植物名称映射...")
            
            val inputStream = context.assets.open("db/植物编号.txt")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonString)
            
            val map = mutableMapOf<String, String>()
            jsonObject.keys().forEach { key ->
                if (key != "游戏版本") {
                    map[key] = jsonObject.getString(key)
                }
            }
            
            plantNameMap = map
            Log.d(TAG, "植物名称映射加载完成，共 ${map.size} 个植物")
            
            map
        } catch (e: Exception) {
            Log.e(TAG, "加载植物名称映射失败", e)
            emptyMap()
        }
    }
    
    /**
     * 加载指定模式的礼包码数据
     */
    suspend fun loadMultiPlantCodes(modeId: String): Map<String, JSONObject> = withContext(Dispatchers.IO) {
        // 如果已缓存，直接返回
        codeCacheMap[modeId]?.let {
            Log.d(TAG, "从缓存返回模式 $modeId 的数据")
            return@withContext it
        }
        
        try {
            val mode = PlantPools.getMode(modeId)
            if (mode == null) {
                Log.e(TAG, "未知的模式ID: $modeId")
                return@withContext emptyMap()
            }
            
            Log.d(TAG, "开始加载模式 ${mode.displayName} 的礼包码...")
            
            // 读取文件
            val inputStream = context.assets.open(mode.fileName)
            val content = inputStream.bufferedReader().use { it.readText() }
            
            // 解析每一行：格式为 "key": {...},
            val codeMap = mutableMapOf<String, JSONObject>()
            
            // 按行分割
            val lines = content.lines()
            var currentKey: String? = null
            var jsonBuilder = StringBuilder()
            var inJsonObject = false
            
            for (line in lines) {
                val trimmed = line.trim()
                
                // 跳过空行
                if (trimmed.isEmpty()) continue
                
                // 匹配键：如 "200134 200143 ...":
                if (trimmed.startsWith("\"") && trimmed.contains("\":")) {
                    // 保存上一个对象
                    if (currentKey != null && jsonBuilder.isNotEmpty()) {
                        try {
                            val jsonObj = JSONObject(jsonBuilder.toString())
                            codeMap[currentKey] = jsonObj
                        } catch (e: Exception) {
                            Log.e(TAG, "解析JSON失败: $currentKey", e)
                        }
                    }
                    
                    // 提取新的键
                    val keyEnd = trimmed.indexOf("\":")
                    currentKey = trimmed.substring(1, keyEnd)
                    
                    // 开始新的JSON对象
                    jsonBuilder = StringBuilder()
                    val jsonStart = trimmed.indexOf("{")
                    if (jsonStart != -1) {
                        jsonBuilder.append(trimmed.substring(jsonStart))
                        inJsonObject = true
                    }
                } else if (inJsonObject) {
                    // 继续追加JSON内容
                    jsonBuilder.append(trimmed)
                    
                    // 检查是否结束（包含 },）
                    if (trimmed.endsWith("},") || trimmed.endsWith("}")) {
                        inJsonObject = false
                        // 移除末尾的逗号
                        val jsonStr = jsonBuilder.toString().removeSuffix(",")
                        if (currentKey != null) {
                            try {
                                val jsonObj = JSONObject(jsonStr)
                                codeMap[currentKey] = jsonObj
                            } catch (e: Exception) {
                                Log.e(TAG, "解析JSON失败: $currentKey", e)
                            }
                        }
                        currentKey = null
                        jsonBuilder = StringBuilder()
                    }
                }
            }
            
            // 处理最后一个对象
            if (currentKey != null && jsonBuilder.isNotEmpty()) {
                try {
                    val jsonStr = jsonBuilder.toString().removeSuffix(",")
                    val jsonObj = JSONObject(jsonStr)
                    codeMap[currentKey] = jsonObj
                } catch (e: Exception) {
                    Log.e(TAG, "解析最后一个JSON失败: $currentKey", e)
                }
            }
            
            // 缓存结果
            codeCacheMap[modeId] = codeMap
            
            Log.d(TAG, "模式 ${mode.displayName} 加载完成，共 ${codeMap.size} 个组合")
            
            codeMap
        } catch (e: Exception) {
            Log.e(TAG, "加载模式 $modeId 的礼包码失败", e)
            e.printStackTrace()
            emptyMap()
        }
    }
    
    /**
     * 查询礼包码
     * @param modeId 模式ID
     * @param selectedPlantIds 用户选择的植物ID列表
     * @return 礼包码JSON对象，如果未找到返回null
     */
    suspend fun queryGiftCode(
        modeId: String,
        selectedPlantIds: List<String>
    ): Result<JSONObject> = withContext(Dispatchers.IO) {
        try {
            // 1. 获取该模式的植物池
            val plantPool = PlantPools.getPlantPool(modeId)
            if (plantPool.isEmpty()) {
                return@withContext Result.failure(Exception("未知的模式ID: $modeId"))
            }
            
            // 2. 将选中的植物ID转换为索引
            val selectedIndices = selectedPlantIds.map { id ->
                plantPool.indexOf(id)
            }.filter { it != -1 }.sorted()  // 过滤无效索引并排序
            
            // 3. 将索引转换回ID（按原始植物池顺序）
            val sortedIds = selectedIndices.map { plantPool[it] }
            
            // 4. 生成查询键
            val queryKey = sortedIds.joinToString(" ")
            
            Log.d(TAG, "查询键: $queryKey")
            
            // 5. 确保数据已加载
            val codes = codeCacheMap[modeId] ?: loadMultiPlantCodes(modeId)
            
            // 6. 查找
            val result = codes[queryKey]
            
            if (result != null) {
                Log.d(TAG, "查询成功！")
                Result.success(result)
            } else {
                Log.w(TAG, "未找到该植物组合的礼包码")
                Result.failure(Exception("未找到该植物组合的礼包码"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "查询礼包码失败", e)
            Result.failure(e)
        }
    }
    
    /**
     * 获取植物名称
     */
    suspend fun getPlantName(plantId: String): String {
        if (plantNameMap == null) {
            loadPlantNames()
        }
        return plantNameMap?.get(plantId) ?: "未知植物"
    }
    
    /**
     * 预加载所有模式的数据（启动时调用）
     */
    suspend fun preloadAllModes() = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "开始预加载所有模式...")
            loadPlantNames()
            PlantPools.MODES.forEach { mode ->
                loadMultiPlantCodes(mode.id)
            }
            Log.d(TAG, "预加载完成！")
        } catch (e: Exception) {
            Log.e(TAG, "预加载失败", e)
        }
    }
    
    companion object {
        private const val TAG = "MultiPlantDataSource"
    }
}
