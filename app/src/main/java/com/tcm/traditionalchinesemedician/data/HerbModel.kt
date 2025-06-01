package com.tcm.traditionalchinesemedician.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader

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
    val contraindications: String,  // 禁忌
    val description: String,        // 药物描述
    val imageUrl: String? = null    // 药材图片地址
)

// Repository for herb data
object HerbRepository {
    private var herbs: List<Herb> = emptyList()
    
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
        }
    }
    
    // Get all herbs
    fun getAllHerbs(): List<Herb> = herbs
    
    // Get all herb categories for filtering
    val categories: List<String>
        get() = herbs.map { it.category }.distinct()
    
    // Search herbs by name or pinyin
    fun searchHerbs(query: String): List<Herb> {
        if (query.isBlank()) return herbs
        
        return herbs.filter { herb ->
            herb.name.contains(query) || 
            herb.pinyin.contains(query, ignoreCase = true)
        }
    }
    
    // Filter herbs by category
    fun getHerbsByCategory(category: String): List<Herb> {
        if (category == "全部") return herbs
        
        return herbs.filter { it.category == category }
    }
} 