package com.tcm.traditionalchinesemedician.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader

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

// Repository for herb data
object HerbRepository {
    private var herbs: List<Herb> = emptyList()
    
    // 缓存随机推荐的功效和主治，避免每次返回首页都重新随机
    private var cachedRandomFunctions: List<String> = emptyList()
    private var cachedRandomIndications: List<String> = emptyList()
    
    // Initialize repository with herbs from JSON file
    fun initialize(context: Context) {
        if (herbs.isEmpty()) {
            val inputStream = context.assets.open("herbs.json")
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            val herbsJson = bufferedReader.use { it.readText() }
            
            val herbListType = object : TypeToken<List<Herb>>() {}.type
            herbs = Gson().fromJson(herbsJson, herbListType)
            
            bufferedReader.close()
            inputStream.close()
            
            // 初始化时进行一次随机，并缓存结果
            generateRandomRecommendations()
        }
    }
    
    // 生成随机推荐的功效和主治
    private fun generateRandomRecommendations() {
        // 只有在缓存为空时才重新生成
        if (cachedRandomFunctions.isEmpty()) {
            val functionsList = mutableListOf<String>()
            herbs.forEach { herb ->
                functionsList.addAll(herb.functions)
            }
            cachedRandomFunctions = functionsList.distinct().shuffled()
        }
        
        if (cachedRandomIndications.isEmpty()) {
            val indicationsList = mutableListOf<String>()
            herbs.forEach { herb ->
                indicationsList.addAll(herb.indications)
            }
            cachedRandomIndications = indicationsList.distinct().shuffled()
        }
    }
    
    // Get all herbs
    fun getAllHerbs(): List<Herb> = herbs
    
    // Get all herb categories for filtering
    val categories: List<String>
        get() = herbs.map { it.category }.distinct()
    
    // Get random recommended functions (only randomized once when app starts)
    val recommendedFunctions: List<String>
        get() = cachedRandomFunctions.take(8)
    
    // Get random recommended indications (only randomized once when app starts)
    val recommendedIndications: List<String>
        get() = cachedRandomIndications.take(8)
    
    // Get all unique functions across all herbs
    val allFunctions: List<String>
        get() {
            val functionsList = mutableListOf<String>()
            herbs.forEach { herb ->
                functionsList.addAll(herb.functions)
            }
            return functionsList.distinct().shuffled()
        }
    
    // Get all unique indications across all herbs
    val allIndications: List<String>
        get() {
            val indicationsList = mutableListOf<String>()
            herbs.forEach { herb ->
                indicationsList.addAll(herb.indications)
            }
            return indicationsList.distinct().shuffled()
        }
    
    // Search herbs by name, pinyin, or functions (effects)
    fun searchHerbs(query: String): List<Herb> {
        if (query.isBlank()) return herbs
        
        val normalizedQuery = query.trim().lowercase()
        
        return herbs.filter { herb ->
            // 搜索药材名称
            herb.name.contains(normalizedQuery) || 
            // 搜索拼音（不区分大小写）
            herb.pinyin.lowercase().contains(normalizedQuery) ||
            // 搜索功效（模糊匹配）
            herb.functions.any { function -> 
                function.lowercase().contains(normalizedQuery)
            } ||
            // 搜索主治（模糊匹配）
            herb.indications.any { indication ->
                indication.lowercase().contains(normalizedQuery)
            }
        }
    }
    
    // Filter herbs by category
    fun getHerbsByCategory(category: String): List<Herb> {
        if (category == "全部") return herbs
        
        return herbs.filter { it.category == category }
    }
} 