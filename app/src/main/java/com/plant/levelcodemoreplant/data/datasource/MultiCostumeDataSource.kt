package com.plant.levelcodemoreplant.data.datasource

import android.app.Application
import android.util.Log
import com.plant.levelcodemoreplant.data.api.RetrofitClient
import com.plant.levelcodemoreplant.data.local.PrefsManager
import com.plant.levelcodemoreplant.data.model.CostumePools
import com.plant.levelcodemoreplant.data.model.SearchRequest
import com.plant.levelcodemoreplant.utils.DeviceUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * 多装扮数据源
 */
class MultiCostumeDataSource(private val application: Application) {
    
    private val searchApiService = RetrofitClient.searchApiService
    private val prefsManager = PrefsManager(application)
    
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
     * 生成礼包码（从服务器查询）
     * @param selectedCostumeIds 选中的装扮ID列表
     * @return 生成的礼包码JSON字符串
     */
    suspend fun generateCode(selectedCostumeIds: List<String>): String = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "生成礼包码，选中装扮: $selectedCostumeIds")
            
            // 构造查询关键词：装扮ID用空格分隔
            val keyword = selectedCostumeIds.joinToString(" ")
            Log.d(TAG, "查询关键词: $keyword")
            
            // 获取卡密和设备ID
            val licenseKey = prefsManager.getLicenseKey()
            if (licenseKey.isNullOrEmpty()) {
                throw Exception("未找到激活信息，请重新激活")
            }
            
            val deviceId = DeviceUtils.getDeviceId(application)
            
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
                val result = jsonObject.toString()
                
                Log.d(TAG, "查询成功")
                return@withContext result
            } else {
                val errorMsg = response.message ?: "未找到对应的装扮组合"
                Log.e(TAG, "查询失败: $errorMsg")
                throw Exception(errorMsg)
            }
        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "请求超时", e)
            throw Exception("请求超时，请检查网络连接后重试")
        } catch (e: java.net.UnknownHostException) {
            Log.e(TAG, "网络连接失败", e)
            throw Exception("网络连接失败，请检查网络设置")
        } catch (e: Exception) {
            Log.e(TAG, "生成礼包码失败", e)
            throw e
        }
    }
}
