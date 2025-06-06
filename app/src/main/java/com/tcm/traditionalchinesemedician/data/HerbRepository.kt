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
    
    // 缓存随机推荐的功效和主治，避免每次返回首页都重新随机
    private var cachedRandomFunctions: List<String> = emptyList()
    private var cachedRandomIndications: List<String> = emptyList()
    
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
                val herbListType = object : TypeToken<List<Herb>>() {}.type
                val herbs = Gson().fromJson<List<Herb>>(herbsArray, herbListType)
                
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
     * Generate random recommendations for functions and indications
     */
    private suspend fun generateRandomRecommendations() {
        withContext(Dispatchers.IO) {
            try {
                // Get all herbs only once for better performance
                val allHerbs = getAllHerbsSync()
                
                // Update functions recommendations
                updateFunctionsRecommendations(allHerbs)
                
                // Update indications recommendations
                updateIndicationsRecommendations(allHerbs)
            } catch (e: Exception) {
                Log.e("HerbRepository", "Error generating recommendations", e)
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
                functionsList.addAll(herb.functions)
            }
            cachedRandomFunctions = functionsList.distinct().shuffled()
        }
    }
    
    /**
     * Helper method to update indications recommendations
     */
    private fun updateIndicationsRecommendations(allHerbs: List<Herb>) {
        // Only update if cache is empty
        if (cachedRandomIndications.isEmpty()) {
            val indicationsList = mutableListOf<String>()
            for (herb in allHerbs) {
                indicationsList.addAll(herb.indications)
            }
            cachedRandomIndications = indicationsList.distinct().shuffled()
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
            Log.e("HerbRepository", "Error getting herbs", e)
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
        cachedRandomFunctions.take(count)
    }
    
    /**
     * Get random recommended indications
     */
    suspend fun getRecommendedIndications(count: Int = 8): List<String> = withContext(Dispatchers.IO) {
        cachedRandomIndications.take(count)
    }
    
    /**
     * Search herbs by name, pinyin, functions, or indications
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
    suspend fun searchHerbsPaged(query: String, page: Int, pageSize: Int): List<Herb> = withContext(Dispatchers.IO) {
        if (query.isBlank()) {
            getAllHerbsPaged(page, pageSize)
        } else {
            val normalizedQuery = query.trim().lowercase()
            val offset = page * pageSize
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
    suspend fun getHerbsByCategoryPaged(category: String, page: Int, pageSize: Int): List<Herb> = withContext(Dispatchers.IO) {
        if (category == "全部") {
            getAllHerbsPaged(page, pageSize)
        } else {
            val offset = page * pageSize
            herbDao.getHerbsByCategoryPaged(category, pageSize, offset).map { it.toHerb() }
        }
    }
    
    /**
     * Get total count of herbs
     */
    suspend fun getHerbCount(): Int = withContext(Dispatchers.IO) {
        herbDao.getHerbCount()
    }
    
    companion object {
        @Volatile
        private var INSTANCE: HerbRepository? = null
        
        fun getInstance(context: Context): HerbRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = HerbRepository(context.applicationContext)
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