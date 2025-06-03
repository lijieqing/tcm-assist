package com.tcm.traditionalchinesemedician.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Data Access Object (DAO) for version info
 */
@Dao
interface VersionDao {
    
    /**
     * Get the current version info
     * @return The version entity, or null if not set
     */
    @Query("SELECT * FROM version_info WHERE id = 1")
    suspend fun getVersionInfo(): VersionEntity?
    
    /**
     * Insert or update version info
     * @param versionEntity The version entity to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateVersionInfo(versionEntity: VersionEntity)
} 