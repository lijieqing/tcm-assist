package com.tcm.traditionalchinesemedician.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for Herb entities
 */
@Dao
interface HerbDao {
    
    /**
     * Insert a list of herbs into the database
     * If there's a conflict (same ID), replace the old entity
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(herbs: List<HerbEntity>)
    
    /**
     * Get all herbs from the database
     * @return A Flow of List<HerbEntity> that emits whenever data changes
     */
    @Query("SELECT * FROM herbs")
    fun getAllHerbs(): Flow<List<HerbEntity>>

    /**
     * Get all herbs with pagination
     * @param limit Maximum number of herbs to return
     * @param offset Number of herbs to skip
     * @return A list of HerbEntity objects
     */
    @Query("SELECT * FROM herbs LIMIT :limit OFFSET :offset")
    suspend fun getHerbsPaged(limit: Int, offset: Int): List<HerbEntity>
    
    /**
     * Get a herb by its ID
     * @param id The herb ID
     * @return The HerbEntity with the given ID, or null if not found
     */
    @Query("SELECT * FROM herbs WHERE id = :id")
    suspend fun getHerbById(id: Int): HerbEntity?
    
    /**
     * Get herbs by category
     * @param category The category to filter by
     * @return A list of HerbEntity objects in the given category
     */
    @Query("SELECT * FROM herbs WHERE category = :category")
    suspend fun getHerbsByCategory(category: String): List<HerbEntity>

    /**
     * Get herbs by category with pagination
     * @param category The category to filter by
     * @param limit Maximum number of herbs to return
     * @param offset Number of herbs to skip
     * @return A list of HerbEntity objects in the given category
     */
    @Query("SELECT * FROM herbs WHERE category = :category LIMIT :limit OFFSET :offset")
    suspend fun getHerbsByCategoryPaged(category: String, limit: Int, offset: Int): List<HerbEntity>
    
    /**
     * Search herbs by name, pinYin, functions, or clinicalApplication
     * @param query The search query
     * @return A list of HerbEntity objects that match the query
     */
    @Query("SELECT * FROM herbs WHERE name LIKE '%' || :query || '%' OR " +
           "(pinYin IS NOT NULL AND pinYin LIKE '%' || :query || '%') OR " +
           "(functions IS NOT NULL AND functions LIKE '%' || :query || '%') OR " +
           "(clinicalApplication IS NOT NULL AND clinicalApplication LIKE '%' || :query || '%') OR " +
           "(effects IS NOT NULL AND effects LIKE '%' || :query || '%')")
    suspend fun searchHerbs(query: String): List<HerbEntity>
    
    /**
     * Search herbs with pagination
     * @param query The search query
     * @param limit Maximum number of herbs to return
     * @param offset Number of herbs to skip
     * @return A list of HerbEntity objects that match the query
     */
    @Query("SELECT * FROM herbs WHERE name LIKE '%' || :query || '%' OR " +
           "(pinYin IS NOT NULL AND pinYin LIKE '%' || :query || '%') OR " +
           "(functions IS NOT NULL AND functions LIKE '%' || :query || '%') OR " +
           "(clinicalApplication IS NOT NULL AND clinicalApplication LIKE '%' || :query || '%') OR " +
           "(effects IS NOT NULL AND effects LIKE '%' || :query || '%') " +
           "LIMIT :limit OFFSET :offset")
    suspend fun searchHerbsPaged(query: String, limit: Int, offset: Int): List<HerbEntity>
    
    /**
     * Get all unique categories from the database
     * @return A list of all unique category names
     */
    @Query("SELECT DISTINCT category FROM herbs")
    suspend fun getAllCategories(): List<String>
    
    /**
     * Delete all herbs from the database
     */
    @Query("DELETE FROM herbs")
    suspend fun deleteAllHerbs()
    
    /**
     * Count total number of herbs
     */
    @Query("SELECT COUNT(*) FROM herbs")
    suspend fun getHerbCount(): Int
} 