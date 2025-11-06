package com.plant.levelcodemoreplant.data.datasource

import android.content.Context
import android.util.Log
import com.plant.levelcodemoreplant.data.api.RetrofitClient
import com.plant.levelcodemoreplant.data.local.PrefsManager
import com.plant.levelcodemoreplant.data.model.LevelData
import com.plant.levelcodemoreplant.data.model.SearchRequest
import com.plant.levelcodemoreplant.utils.DeviceUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

/**
 * 关卡数据源
 * 负责从 assets 中读取礼包数据并解析，以及从服务器查询兑换码
 */
class LevelDataSource(private val context: Context) {
    
    private val searchApiService = RetrofitClient.searchApiService
    private val prefsManager = PrefsManager(context)

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
    
    /**
     * 从服务器获取模糊搜索建议
     * @param keyword 模糊搜索关键词
     * @return 建议列表
     */
    suspend fun getSuggestions(keyword: String): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "开始获取搜索建议: $keyword")
            
            val request = com.plant.levelcodemoreplant.data.model.SuggestionsRequest(keyword = keyword)
            val response = searchApiService.getSuggestions(request)
            
            if (response.success && response.data != null) {
                Log.d(TAG, "获取建议成功，共 ${response.data.size} 条")
                Result.success(response.data)
            } else {
                val errorMsg = response.message ?: "获取建议失败"
                Log.w(TAG, "获取建议失败: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "请求超时", e)
            Result.failure(Exception("请求超时，请检查网络连接后重试"))
        } catch (e: java.net.UnknownHostException) {
            Log.e(TAG, "网络连接失败", e)
            Result.failure(Exception("网络连接失败，请检查网络设置"))
        } catch (e: Exception) {
            Log.e(TAG, "获取建议失败", e)
            Result.failure(Exception("获取建议失败: ${e.message}"))
        }
    }
    
    /**
     * 从服务器查询单植物/单装扮兑换码
     * @param plantName 植物或装扮名称（如"大力花菜"）
     * @return 兑换码JSON字符串
     */
    suspend fun searchCodeFromServer(plantName: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "开始查询兑换码: $plantName")
            
            // 获取卡密和设备ID
            val licenseKey = prefsManager.getLicenseKey()
            if (licenseKey.isNullOrEmpty()) {
                return@withContext Result.failure(Exception("未找到激活信息，请重新激活"))
            }
            
            val deviceId = DeviceUtils.getDeviceId(context)
            
            // 构造查询关键词（plantName 已经包含完整的名称，如"大力花菜兑换码"）
            val keyword = plantName
            
            // 构造请求
            val request = SearchRequest(
                keyword = keyword,
                license_key = licenseKey,
                device_id = deviceId
            )
            
            Log.d(TAG, "请求参数: keyword=$keyword, license_key=$licenseKey, device_id=$deviceId")
            
            // 调用API
            val response = searchApiService.searchCode(request)
            
            if (response.success && response.data != null) {
                // 将 encrypted_data 转换为 JSON 字符串
                val encryptedData = response.data.encrypted_data
                val jsonObject = JSONObject().apply {
                    put("i", encryptedData.i)
                    put("r", encryptedData.r)
                    put("e", encryptedData.e)
                }
                val jsonCode = jsonObject.toString(2)
                
                Log.d(TAG, "查询成功: $plantName")
                Result.success(jsonCode)
            } else {
                val errorMsg = response.message ?: "未找到该植物/装扮的兑换码"
                Log.w(TAG, "查询失败: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "请求超时", e)
            Result.failure(Exception("请求超时，请检查网络连接后重试"))
        } catch (e: java.net.UnknownHostException) {
            Log.e(TAG, "网络连接失败", e)
            Result.failure(Exception("网络连接失败，请检查网络设置"))
        } catch (e: Exception) {
            Log.e(TAG, "查询兑换码失败", e)
            Result.failure(Exception("查询失败: ${e.message}"))
        }
    }
    
    companion object {
        private const val TAG = "LevelDataSource"
    }
}
