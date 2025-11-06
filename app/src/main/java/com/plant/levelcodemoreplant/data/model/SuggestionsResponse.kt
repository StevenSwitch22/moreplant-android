package com.plant.levelcodemoreplant.data.model

/**
 * 模糊搜索建议响应模型
 */
data class SuggestionsResponse(
    val success: Boolean,
    val data: List<String>?,  // 建议列表，如 ["火龙草兑换码", "龙舌兰兑换码"]
    val total: Int?,
    val message: String?
)
