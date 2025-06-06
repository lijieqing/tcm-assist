package com.tcm.traditionalchinesemedician.data

data class HerbPairing(
    val name: String,        // 配伍药材名称
    val usage: String,       // 配伍用法
    val effect: String       // 配伍功效
)

data class Herb(
    val id: Int,                         // 唯一标识符
    val name: String,                    // 中药名称
    val pinYin: String? = null,          // 拼音
    val category: String,                // 药物分类
    val url: String? = null,             // 药材详情页URL
    val medicinalPart: String? = null,   // 药用部位
    val tasteMeridian: String? = null,   // 性味归经
    val properties: String? = null,      // 药性（寒、热、温、凉、平）
    val taste: String? = null,           // 药味（酸、苦、甘、辛、咸）
    val meridians: List<String>? = null, // 归经
    val effects: String? = null,         // 功效描述
    val functions: List<String>? = null, // 功效列表
    val clinicalApplication: List<String>? = null, // 临床应用
    val prescriptionName: String? = null, // 处方用名
    val usageDosage: String? = null,     // 用法用量
    val notes: List<String>? = null,     // 按语/注意事项
    val formulas: List<String>? = null,  // 方剂举例
    val literature: List<String>? = null, // 文献摘录
    val affiliatedHerbs: List<String>? = null, // 附药
    val images: List<String>? = null     // 图片链接
) 