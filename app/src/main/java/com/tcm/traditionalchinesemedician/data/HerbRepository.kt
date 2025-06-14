package com.tcm.traditionalchinesemedician.data

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.tcm.traditionalchinesemedician.data.room.AppDatabase
import com.tcm.traditionalchinesemedician.data.room.HerbEntity
import com.tcm.traditionalchinesemedician.data.room.VersionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Repository for herb data, now using Room database for storage
 */
class HerbRepository private constructor(private val context: Context) {
    
    private val database = AppDatabase.getInstance(context)
    private val herbDao = database.herbDao()
    private val versionDao = database.versionDao()
    private val appScope = CoroutineScope(Dispatchers.IO)
    
    // 缓存随机推荐的功效和临床应用，避免每次返回首页都重新随机
    private var cachedRandomFunctions: List<String> = emptyList()
    private var cachedRandomClinicalApplications: List<String> = emptyList()
    
    init {
        // Always initialize on construction to ensure data is available
        initialize()
    }
    
    /**
     * Initialize repository by checking if database needs to be updated from JSON
     */
    fun initialize() {
        appScope.launch {
            checkAndUpdateDatabase()
            // 初始化时进行一次随机，并缓存结果
            generateRandomRecommendations()
        }
    }
    
    /**
     * Check if the database needs to be updated from the JSON file
     */
    private suspend fun checkAndUpdateDatabase() {
        try {
            // Read the JSON file to get the version
            val inputStream = context.assets.open("herbs.json")
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            val herbsJson = bufferedReader.use { it.readText() }
            
            val jsonObject = Gson().fromJson(herbsJson, JsonObject::class.java)
            val jsonVersion = jsonObject.get("version").asInt
            
            // Get current database version
            val currentVersionInfo = versionDao.getVersionInfo()
            val currentVersion = currentVersionInfo?.version ?: 0
            
            Log.d("HerbRepository", "JSON version: $jsonVersion, DB version: $currentVersion")
            
            // If JSON version is newer, update the database
            if (jsonVersion > currentVersion) {
                Log.d("HerbRepository", "Updating database from JSON")
                updateDatabaseFromJson(herbsJson, jsonVersion)
            } else {
                Log.d("HerbRepository", "Database is up to date")
            }
            
            bufferedReader.close()
            inputStream.close()
        } catch (e: Exception) {
            Log.e("HerbRepository", "Error checking database version", e)
        }
    }
    
    /**
     * Update the database with data from the JSON file
     */
    private suspend fun updateDatabaseFromJson(herbsJson: String, newVersion: Int) {
        withContext(Dispatchers.IO) {
            try {
                // Parse JSON to get herb list
                val jsonObject = Gson().fromJson(herbsJson, JsonObject::class.java)
                val herbsArray = jsonObject.getAsJsonArray("herbs")
                
                // 将JSON中的蛇形命名转换为驼峰命名
                val herbs = mutableListOf<Herb>()
                
                for (i in 0 until herbsArray.size()) {
                    val herbObj = herbsArray.get(i).asJsonObject
                    
                    // 提取基本字段
                    val id = if (herbObj.has("id")) herbObj.get("id").asInt else i + 1
                    val name = herbObj.get("name").asString
                    val category = herbObj.get("category").asString
                    
                    // 提取可空字段，注意命名转换
                    val pinYin = if (herbObj.has("pin_yin")) herbObj.get("pin_yin").asString else null
                    val url = if (herbObj.has("url")) herbObj.get("url").asString else null
                    val medicinalPart = if (herbObj.has("medicinal_part")) herbObj.get("medicinal_part").asString else null
                    val tasteMeridian = if (herbObj.has("taste_meridian")) herbObj.get("taste_meridian").asString else null
                    val properties = if (herbObj.has("properties")) herbObj.get("properties").asString else null
                    val taste = if (herbObj.has("taste")) herbObj.get("taste").asString else null
                    val effects = if (herbObj.has("effects")) herbObj.get("effects").asString else null
                    val prescriptionName = if (herbObj.has("prescription_name")) herbObj.get("prescription_name").asString else null
                    val usageDosage = if (herbObj.has("usage_dosage")) herbObj.get("usage_dosage").asString else null
                    
                    // 提取数组类型字段
                    val gson = Gson()
                    val typeToken = object : TypeToken<List<String>>() {}.type
                    
                    val meridians = if (herbObj.has("meridians")) 
                        gson.fromJson<List<String>>(herbObj.get("meridians"), typeToken) else null
                    
                    val functions = if (herbObj.has("functions")) 
                        gson.fromJson<List<String>>(herbObj.get("functions"), typeToken) else null
                    
                    val clinicalApplication = if (herbObj.has("clinical_application")) 
                        gson.fromJson<List<String>>(herbObj.get("clinical_application"), typeToken) else null
                    
                    val notes = if (herbObj.has("notes")) 
                        gson.fromJson<List<String>>(herbObj.get("notes"), typeToken) else null
                    
                    val formulas = if (herbObj.has("formulas")) 
                        gson.fromJson<List<String>>(herbObj.get("formulas"), typeToken) else null
                    
                    val literature = if (herbObj.has("literature")) 
                        gson.fromJson<List<String>>(herbObj.get("literature"), typeToken) else null
                    
                    val affiliatedHerbs = if (herbObj.has("affiliated_herbs")) 
                        gson.fromJson<List<String>>(herbObj.get("affiliated_herbs"), typeToken) else null
                    
                    val images = if (herbObj.has("images")) 
                        gson.fromJson<List<String>>(herbObj.get("images"), typeToken) else null
                    
                    // 创建Herb对象
                    val herb = Herb(
                        id = id,
                        name = name,
                        pinYin = pinYin,
                        category = category,
                        url = url,
                        medicinalPart = medicinalPart,
                        tasteMeridian = tasteMeridian,
                        properties = properties,
                        taste = taste,
                        meridians = meridians,
                        effects = effects,
                        functions = functions,
                        clinicalApplication = clinicalApplication,
                        prescriptionName = prescriptionName,
                        usageDosage = usageDosage,
                        notes = notes,
                        formulas = formulas,
                        literature = literature,
                        affiliatedHerbs = affiliatedHerbs,
                        images = images
                    )
                    
                    herbs.add(herb)
                }
                
                // Clear existing herbs
                herbDao.deleteAllHerbs()
                
                // Convert and insert new herbs
                val herbEntities = herbs.map { HerbEntity.fromHerb(it) }
                herbDao.insertAll(herbEntities)
                
                // Update version info
                val versionEntity = VersionEntity(
                    version = newVersion,
                    lastUpdated = System.currentTimeMillis()
                )
                versionDao.updateVersionInfo(versionEntity)
                
                Log.d("HerbRepository", "Database updated successfully with ${herbs.size} herbs")
            } catch (e: Exception) {
                Log.e("HerbRepository", "Error updating database", e)
            }
        }
    }
    
    /**
     * Generate random recommendations for functions and clinical applications
     */
    private suspend fun generateRandomRecommendations() {
        withContext(Dispatchers.IO) {
            try {
                // Get all herbs only once for better performance
                val allHerbs = getAllHerbsSync()
                
                // Update functions recommendations
                updateFunctionsRecommendations(allHerbs)
                
                // Update clinical applications recommendations
                updateClinicalApplicationsRecommendations(allHerbs)
                
                // If either list is still empty after updating, try one more time with a different approach
                if (cachedRandomFunctions.isEmpty() || cachedRandomClinicalApplications.isEmpty()) {
                    Log.d("HerbRepository", "Empty cached lists detected, trying alternative method")
                    generateFallbackRecommendations(allHerbs)
                }
            } catch (e: Exception) {
                Log.e("HerbRepository", "Error generating recommendations", e)
                // Initialize with default values if an error occurs
                if (cachedRandomFunctions.isEmpty()) {
                    cachedRandomFunctions = listOf("祛风", "解表", "清热", "泻火", "消食", "理气", "活血", "化瘀")
                }
                if (cachedRandomClinicalApplications.isEmpty()) {
                    cachedRandomClinicalApplications = listOf("感冒", "发热", "咳嗽", "腹痛", "头痛", "消化不良", "月经不调", "失眠")
                }
            }
        }
    }
    
    /**
     * Fallback method to generate recommendations if the primary method fails
     */
    private fun generateFallbackRecommendations(allHerbs: List<Herb>) {
        // Extract functions
        if (cachedRandomFunctions.isEmpty()) {
            val functionsList = mutableListOf<String>()
            allHerbs.forEach { herb ->
                herb.functions?.forEach { function ->
                    if (function.isNotBlank() && function.length < 15) {
                        functionsList.add(function)
                    }
                }
            }
            
            if (functionsList.isNotEmpty()) {
                cachedRandomFunctions = functionsList.distinct().shuffled()
                Log.d("HerbRepository", "Generated ${cachedRandomFunctions.size} functions using fallback")
            } else {
                // Hardcoded default if extraction still fails
                cachedRandomFunctions = listOf("祛风", "解表", "清热", "泻火", "消食", "理气", "活血", "化瘀")
                Log.d("HerbRepository", "Using hardcoded functions")
            }
        }
        
        // Extract clinical applications
        if (cachedRandomClinicalApplications.isEmpty()) {
            val clinicalApplicationsList = mutableListOf<String>()
            allHerbs.forEach { herb ->
                herb.clinicalApplication?.forEach { application ->
                    // Extract shorter, concise terms from clinical applications
                    val cleanedApplication = application.replace(Regex("^\\d+\\.用于"), "")
                                                       .replace(Regex("等症.*$"), "")
                                                       .trim()
                    if (cleanedApplication.isNotBlank() && cleanedApplication.length < 20) {
                        clinicalApplicationsList.add(cleanedApplication)
                    }
                }
            }
            
            if (clinicalApplicationsList.isNotEmpty()) {
                cachedRandomClinicalApplications = clinicalApplicationsList.distinct().shuffled()
                Log.d("HerbRepository", "Generated ${cachedRandomClinicalApplications.size} applications using fallback")
            } else {
                // Hardcoded default if extraction still fails
                cachedRandomClinicalApplications = listOf("感冒", "发热", "咳嗽", "腹痛", "头痛", "消化不良", "月经不调", "失眠")
                Log.d("HerbRepository", "Using hardcoded applications")
            }
        }
    }
    
    /**
     * Helper method to update functions recommendations
     */
    private fun updateFunctionsRecommendations(allHerbs: List<Herb>) {
        // Only update if cache is empty
        if (cachedRandomFunctions.isEmpty()) {
            val functionsList = mutableListOf<String>()
            for (herb in allHerbs) {
                herb.functions?.let { functionsList.addAll(it) }
            }
            cachedRandomFunctions = functionsList.distinct().shuffled()
            Log.d("HerbRepository", "Generated ${cachedRandomFunctions.size} functions")
        }
    }
    
    /**
     * Helper method to update clinical applications recommendations
     */
    private fun updateClinicalApplicationsRecommendations(allHerbs: List<Herb>) {
        // Only update if cache is empty
        if (cachedRandomClinicalApplications.isEmpty()) {
            val clinicalApplicationsList = mutableListOf<String>()
            for (herb in allHerbs) {
                herb.clinicalApplication?.let { clinicalApplicationsList.addAll(it) }
            }
            cachedRandomClinicalApplications = clinicalApplicationsList.distinct().shuffled()
            Log.d("HerbRepository", "Generated ${cachedRandomClinicalApplications.size} applications")
        }
    }
    
    /**
     * Get all herbs as a Flow (reactive)
     */
    fun getAllHerbsFlow(): Flow<List<Herb>> {
        return herbDao.getAllHerbs().map { entities ->
            entities.map { it.toHerb() }
        }
    }
    
    /**
     * Get all herbs synchronously
     */
    suspend fun getAllHerbsSync(): List<Herb> = withContext(Dispatchers.IO) {
        try {
            val entities = herbDao.getAllHerbs().first()
            entities.map { it.toHerb() }
        } catch (e: Exception) {
            Log.e("HerbRepository", "Error getting herbs: ${e.message}", e)
            e.printStackTrace()
            emptyList()
        }
    }
    
    /**
     * Get all herbs with pagination
     */
    suspend fun getAllHerbsPaged(page: Int, pageSize: Int): List<Herb> = withContext(Dispatchers.IO) {
        val offset = page * pageSize
        herbDao.getHerbsPaged(pageSize, offset).map { it.toHerb() }
    }
    
    /**
     * Get herb by ID
     */
    suspend fun getHerbById(id: Int): Herb? = withContext(Dispatchers.IO) {
        herbDao.getHerbById(id)?.toHerb()
    }
    
    /**
     * Get all unique categories
     */
    suspend fun getAllCategories(): List<String> = withContext(Dispatchers.IO) {
        herbDao.getAllCategories()
    }
    
    /**
     * Get random recommended functions
     */
    suspend fun getRecommendedFunctions(count: Int = 8): List<String> = withContext(Dispatchers.IO) {
        // If cache is empty, try to generate recommendations
        if (cachedRandomFunctions.isEmpty()) {
            try {
                generateRandomRecommendations()
            } catch (e: Exception) {
                Log.e("HerbRepository", "Error generating functions for retrieval", e)
            }
            
            // If still empty after generation attempt, use hardcoded fallback
            if (cachedRandomFunctions.isEmpty()) {
                cachedRandomFunctions = listOf("祛风", "解表", "清热", "泻火", "消食", "理气", "活血", "化瘀")
            }
        }
        
        // Return requested number of items, cycling through list if necessary
        return@withContext if (cachedRandomFunctions.size <= count) {
            cachedRandomFunctions
        } else {
            cachedRandomFunctions.take(count)
        }
    }
    
    /**
     * Get random recommended clinical applications
     */
    suspend fun getRecommendedClinicalApplications(count: Int = 8): List<String> = withContext(Dispatchers.IO) {
        // If cache is empty, try to generate recommendations
        if (cachedRandomClinicalApplications.isEmpty()) {
            try {
                generateRandomRecommendations()
            } catch (e: Exception) {
                Log.e("HerbRepository", "Error generating applications for retrieval", e)
            }
            
            // If still empty after generation attempt, use hardcoded fallback
            if (cachedRandomClinicalApplications.isEmpty()) {
                cachedRandomClinicalApplications = listOf("感冒", "发热", "咳嗽", "腹痛", "头痛", "消化不良", "月经不调", "失眠")
            }
        }
        
        // Return requested number of items, cycling through list if necessary
        return@withContext if (cachedRandomClinicalApplications.size <= count) {
            cachedRandomClinicalApplications
        } else {
            cachedRandomClinicalApplications.take(count)
        }
    }
    
    /**
     * Search herbs by name, pinYin, functions, or clinical application
     */
    suspend fun searchHerbs(query: String): List<Herb> = withContext(Dispatchers.IO) {
        if (query.isBlank()) {
            getAllHerbsSync()
        } else {
            val normalizedQuery = query.trim().lowercase()
            herbDao.searchHerbs(normalizedQuery).map { it.toHerb() }
        }
    }
    
    /**
     * Search herbs with pagination
     */
    suspend fun searchHerbsPaged(query: String, pageSize: Int, offset: Int): List<Herb> = withContext(Dispatchers.IO) {
        if (query.isBlank()) {
            getAllHerbsPaged(offset / pageSize, pageSize)
        } else {
            val normalizedQuery = query.trim().lowercase()
            herbDao.searchHerbsPaged(normalizedQuery, pageSize, offset).map { it.toHerb() }
        }
    }
    
    /**
     * Filter herbs by category
     */
    suspend fun getHerbsByCategory(category: String): List<Herb> = withContext(Dispatchers.IO) {
        if (category == "全部") {
            getAllHerbsSync()
        } else {
            herbDao.getHerbsByCategory(category).map { it.toHerb() }
        }
    }
    
    /**
     * Filter herbs by category with pagination
     */
    suspend fun getHerbsByCategoryPaged(category: String, pageSize: Int, offset: Int): List<Herb> = withContext(Dispatchers.IO) {
        if (category == "全部") {
            getAllHerbsPaged(offset / pageSize, pageSize)
        } else {
            herbDao.getHerbsByCategoryPaged(category, pageSize, offset).map { it.toHerb() }
        }
    }
    
    /**
     * Get total number of herbs
     */
    suspend fun getHerbCount(): Int = withContext(Dispatchers.IO) {
        herbDao.getHerbCount()
    }
    
    /**
     * 在特定分类内搜索中药
     */
    suspend fun searchHerbsInCategory(category: String, query: String, pageSize: Int, offset: Int): List<Herb> = withContext(Dispatchers.IO) {
        if (query.isBlank()) {
            // 如果搜索词为空，直接返回该分类的中药
            return@withContext getHerbsByCategoryPaged(category, pageSize, offset)
        } else {
            // 否则，在该分类内搜索
            val normalizedQuery = query.trim().lowercase()
            herbDao.searchHerbsInCategory(category, normalizedQuery, pageSize, offset).map { it.toHerb() }
        }
    }
    
    companion object {
        @Volatile
        private var INSTANCE: HerbRepository? = null
        
        fun getInstance(context: Context): HerbRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = HerbRepository(context)
                INSTANCE = instance
                instance
            }
        }
        
        /**
         * Initialize repository with application context
         */
        fun initialize(context: Context) {
            val repository = getInstance(context)
            repository.initialize()
        }
    }
} 