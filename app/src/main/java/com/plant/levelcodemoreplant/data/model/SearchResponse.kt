package com.plant.levelcodemoreplant.data.model

/**
 * 查询兑换码响应模型
 */
data class SearchResponse(
    val success: Boolean,
    val data: SearchData?,
    val message: String?
)

data class SearchData(
    val search_key: String,
    val code_type: String,
    val encrypted_data: EncryptedData
)

data class EncryptedData(
    val i: String,
    val r: Int,
    val e: String
)
