package com.plant.levelcodemoreplant.data.model

/**
 * 多装扮模式配置
 */
data class MultiCostumeMode(
    val id: String = "12_any",
    val totalCount: Int = 12,
    val minSelect: Int = 1,      // 最少选1个
    val maxSelect: Int = 12,     // 最多选12个
    val displayName: String = "12装扮任意选",
    val description: String = "从12个超级装扮中任意选择1-12个",
    val fileName: String = "db/12_任意选多装扮兑换代码.txt"
)

/**
 * 装扮池配置
 */
object CostumePools {
    
    // 单一自由选择模式
    val MODE = MultiCostumeMode()
    
    /**
     * 12个装扮列表（按顺序）
     */
    val COSTUMES = listOf(
        "30010082" to "双胞向日葵超级装扮",
        "30010394" to "激光豆魔龙超级装扮",
        "30010451" to "瓷砖萝卜超级装扮",
        "30010703" to "大嘴花超级装扮",
        "31110224" to "机枪射手超级装扮",
        "31110292" to "仙人掌超级装扮",
        "31110303" to "猫尾草怀旧超级装扮",
        "31110672" to "芦黎药师超级装扮",
        "31110704" to "桑葚爆破手超级装扮",
        "32000344" to "聚能山竹超级装扮",
        "32000792" to "贪吃龙草超级装扮",
        "32000832" to "电鳗香蕉超级装扮"
    )
    
    /**
     * 获取装扮名称映射
     */
    fun getCostumeNames(): Map<String, String> {
        return COSTUMES.toMap()
    }
    
    /**
     * 获取装扮ID列表
     */
    fun getCostumeIds(): List<String> {
        return COSTUMES.map { it.first }
    }
}
