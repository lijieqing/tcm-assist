package com.tcm.traditionalchinesemedician.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tcm.traditionalchinesemedician.data.Herb
import com.tcm.traditionalchinesemedician.data.HerbPairing

/**
 * Room Entity representing a Chinese medicinal herb in the database
 */
@Entity(tableName = "herbs")
data class HerbEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val pinyin: String,
    val category: String,
    val properties: String,
    val taste: String,
    val meridians: String, // Stored as JSON string
    val functions: String, // Stored as JSON string
    val indications: String, // Stored as JSON string
    val dosage: String,
    val usage: String,
    val commonPairings: String, // Stored as JSON string
    val contraindications: String,
    val description: String,
    val imageUrl: String?
) {
    // Convert from database entity to domain model
    fun toHerb(): Herb {
        val gson = Gson()
        return Herb(
            id = id,
            name = name,
            pinyin = pinyin,
            category = category,
            properties = properties,
            taste = taste,
            meridians = gson.fromJson(meridians, object : TypeToken<List<String>>() {}.type),
            functions = gson.fromJson(functions, object : TypeToken<List<String>>() {}.type),
            indications = gson.fromJson(indications, object : TypeToken<List<String>>() {}.type),
            dosage = dosage,
            usage = usage,
            commonPairings = gson.fromJson(commonPairings, object : TypeToken<List<HerbPairing>>() {}.type),
            contraindications = contraindications,
            description = description,
            imageUrl = imageUrl
        )
    }

    companion object {
        // Convert from domain model to database entity
        fun fromHerb(herb: Herb): HerbEntity {
            val gson = Gson()
            return HerbEntity(
                id = herb.id,
                name = herb.name,
                pinyin = herb.pinyin,
                category = herb.category,
                properties = herb.properties,
                taste = herb.taste,
                meridians = gson.toJson(herb.meridians),
                functions = gson.toJson(herb.functions),
                indications = gson.toJson(herb.indications),
                dosage = herb.dosage,
                usage = herb.usage,
                commonPairings = gson.toJson(herb.commonPairings),
                contraindications = herb.contraindications,
                description = herb.description,
                imageUrl = herb.imageUrl
            )
        }
    }
}

/**
 * Type converters for complex types in HerbEntity
 */
class HerbConverters {
    private val gson = Gson()

    @TypeConverter
    fun stringListToJson(value: List<String>): String = gson.toJson(value)

    @TypeConverter
    fun jsonToStringList(value: String): List<String> = 
        gson.fromJson(value, object : TypeToken<List<String>>() {}.type)

    @TypeConverter
    fun herbPairingListToJson(value: List<HerbPairing>): String = gson.toJson(value)

    @TypeConverter
    fun jsonToHerbPairingList(value: String): List<HerbPairing> = 
        gson.fromJson(value, object : TypeToken<List<HerbPairing>>() {}.type)
} 