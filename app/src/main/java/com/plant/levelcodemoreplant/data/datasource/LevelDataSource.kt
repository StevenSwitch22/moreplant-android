package com.plant.levelcodemoreplant.data.datasource

import android.content.Context
import android.util.Log
import com.plant.levelcodemoreplant.data.model.LevelData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

/**
 * 关卡数据源
 * 负责从 assets 中读取礼包数据并解析
 */
class LevelDataSource(private val context: Context) {

    private val customLevelsFile: File
        get() = File(context.filesDir, "custom_levels.json")

    /**
     * 加载所有礼包数据（包括 assets 和自定义礼包）
     * @return 礼包列表
     */
    suspend fun loadLevels(): List<LevelData> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "开始加载礼包数据...")
            
            // 读取 db/数据库.txt 文件
            val inputStream = context.assets.open("db/最终植物装扮兑换代码.txt")
            val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
            val jsonString = reader.use { it.readText() }
            
            Log.d(TAG, "文件读取成功，长度: ${jsonString.length}")
            
            // 解析 JSON
            val jsonObject = JSONObject(jsonString)
            val levels = mutableListOf<LevelData>()
            
            // 遍历所有键值对
            jsonObject.keys().forEach { name ->
                val levelJsonObject = jsonObject.getJSONObject(name)
                // 将整个 JSON 对象转换为格式化的字符串
                val jsonCode = levelJsonObject.toString(2) // 2 表示缩进
                levels.add(LevelData(name = name, jsonCode = jsonCode))
            }
            
            Log.d(TAG, "从 assets 加载 ${levels.size} 个礼包")
            
            // 加载自定义礼包
            val customLevels = loadCustomLevels()
            Log.d(TAG, "从内部存储加载 ${customLevels.size} 个自定义礼包")
            
            // 合并两个列表（自定义礼包在前）
            val allLevels = customLevels + levels
            Log.d(TAG, "总共加载 ${allLevels.size} 个礼包")
            
            allLevels
        } catch (e: Exception) {
            Log.e(TAG, "加载礼包数据失败", e)
            e.printStackTrace()
            emptyList()
        }
    }
    
    /**
     * 加载自定义礼包
     */
    private fun loadCustomLevels(): List<LevelData> {
        return try {
            if (!customLevelsFile.exists()) {
                return emptyList()
            }
            
            val jsonString = customLevelsFile.readText()
            if (jsonString.isEmpty()) {
                return emptyList()
            }
            
            val jsonObject = JSONObject(jsonString)
            val levels = mutableListOf<LevelData>()
            
            jsonObject.keys().forEach { name ->
                val levelJsonObject = jsonObject.getJSONObject(name)
                val jsonCode = levelJsonObject.toString(2)
                levels.add(LevelData(name = name, jsonCode = jsonCode))
            }
            
            levels
        } catch (e: Exception) {
            Log.e(TAG, "加载自定义礼包失败", e)
            emptyList()
        }
    }
    
    /**
     * 保存新的礼包
     */
    suspend fun saveLevel(name: String, jsonCode: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // 读取现有的自定义礼包
            val existingJson = if (customLevelsFile.exists()) {
                JSONObject(customLevelsFile.readText())
            } else {
                JSONObject()
            }
            
            // 检查礼包名是否已存在
            if (existingJson.has(name)) {
                return@withContext Result.failure(Exception("关卡名称已存在"))
            }
            
            // 解析用户输入的 JSON 代码
            val levelJsonObject = try {
                JSONObject(jsonCode)
            } catch (e: Exception) {
                return@withContext Result.failure(Exception("JSON 格式错误：${e.message}"))
            }
            
            // 添加新礼包
            existingJson.put(name, levelJsonObject)
            
            // 保存到文件
            customLevelsFile.writeText(existingJson.toString(2))
            
            Log.d(TAG, "礼包保存成功: $name")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "保存礼包失败", e)
            Result.failure(e)
        }
    }
    
    companion object {
        private const val TAG = "LevelDataSource"
    }
}
