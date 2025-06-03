package com.tcm.traditionalchinesemedician.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity to store version information for data synchronization
 */
@Entity(tableName = "version_info")
data class VersionEntity(
    @PrimaryKey
    val id: Int = 1, // Only one version record
    val version: Int, // Version of the data
    val lastUpdated: Long // Timestamp of last update
) 