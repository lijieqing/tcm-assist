package com.tcm.traditionalchinesemedician.data

data class HerbPairing(
    val name: String,        // 配伍药材名称
    val usage: String,       // 配伍用法
    val effect: String       // 配伍功效
)

data class Herb(
    val id: Int,                    // 唯一标识符
    val name: String,               // 中药名称
    val pinyin: String,             // 拼音
    val category: String,           // 药物分类
    val properties: String,         // 药性（寒、热、温、凉、平）
    val taste: String,              // 药味（酸、苦、甘、辛、咸）
    val meridians: List<String>,    // 归经
    val functions: List<String>,    // 功效
    val indications: List<String>,  // 主治
    val dosage: String,             // 用量
    val usage: String,              // 用法
    val commonPairings: List<HerbPairing>, // 常见配伍
    val contraindications: String,  // 禁忌
    val description: String,        // 药物描述
    val imageUrl: String? = null    // 药材图片地址
) 