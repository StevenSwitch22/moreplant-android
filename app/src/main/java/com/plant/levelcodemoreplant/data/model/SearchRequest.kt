package com.plant.levelcodemoreplant.data.model

/**
 * 查询兑换码请求模型
 */
data class SearchRequest(
    val keyword: String,        // 植物名/装扮名 或 编号组合
    val license_key: String,    // 用户卡密
    val device_id: String       // 设备ID
)
