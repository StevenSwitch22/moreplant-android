package com.plant.levelcodemoreplant.data.datasource

import android.app.Application
import android.util.Log
import com.plant.levelcodemoreplant.data.model.CostumePools
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * 多装扮数据源
 */
class MultiCostumeDataSource(private val application: Application) {
    
    companion object {
        private const val TAG = "MultiCostumeDataSource"
    }
    
    // 缓存装扮礼包码数据
    private val costumeCodeCache = mutableMapOf<String, String>()
    
    /**
     * 预加载装扮数据
     */
    suspend fun preloadData() = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "开始预加载装扮数据")
            val fileName = CostumePools.MODE.fileName
            val content = application.assets.open(fileName).bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(content)
            
            // 缓存所有装扮组合的兑换码
            jsonObject.keys().forEach { key ->
                val value = jsonObject.getJSONObject(key).toString()
                costumeCodeCache[key] = value
            }
            
            Log.d(TAG, "预加载完成，共 ${costumeCodeCache.size} 条数据")
        } catch (e: Exception) {
            Log.e(TAG, "预加载装扮数据失败", e)
        }
    }
    
    /**
     * 加载装扮名称映射
     */
    fun loadCostumeNames(): Map<String, String> {
        return CostumePools.getCostumeNames()
    }
    
    /**
     * 生成礼包码
     * @param selectedCostumeIds 选中的装扮ID列表
     * @return 生成的礼包码JSON字符串
     */
    suspend fun generateCode(selectedCostumeIds: List<String>): String = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "生成礼包码，选中装扮: $selectedCostumeIds")
            
            // 构造查找key：装扮ID用空格分隔
            val key = selectedCostumeIds.joinToString(" ")
            Log.d(TAG, "查找key: $key")
            
            // 从缓存中查找
            val codeData = costumeCodeCache[key]
            if (codeData != null) {
                Log.d(TAG, "找到礼包码")
                return@withContext codeData
            }
            
            // 缓存未命中，直接读取文件
            val fileName = CostumePools.MODE.fileName
            val content = application.assets.open(fileName).bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(content)
            
            if (jsonObject.has(key)) {
                val result = jsonObject.getJSONObject(key).toString()
                costumeCodeCache[key] = result
                Log.d(TAG, "从文件读取礼包码成功")
                return@withContext result
            }
            
            Log.e(TAG, "未找到对应的礼包码，key: $key")
            throw Exception("未找到对应的装扮组合")
            
        } catch (e: Exception) {
            Log.e(TAG, "生成礼包码失败", e)
            throw e
        }
    }
}
