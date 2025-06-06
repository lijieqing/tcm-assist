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
    val pinYin: String?,
    val category: String,
    val url: String?,
    val medicinalPart: String?,
    val tasteMeridian: String?,
    val properties: String?,
    val taste: String?,
    val meridians: String?, // Stored as JSON string
    val effects: String?,
    val functions: String?, // Stored as JSON string
    val clinicalApplication: String?, // Stored as JSON string
    val prescriptionName: String?,
    val usageDosage: String?,
    val notes: String?, // Stored as JSON string
    val formulas: String?, // Stored as JSON string
    val literature: String?, // Stored as JSON string
    val affiliatedHerbs: String?, // Stored as JSON string
    val images: String? // Stored as JSON string
) {
    // Convert from database entity to domain model
    fun toHerb(): Herb {
        val gson = Gson()
        
        // 使用明确的类型转换方法而不是直接使用TypeToken泛型
        fun parseStringList(json: String?): List<String>? {
            if (json == null) return null
            return try {
                gson.fromJson(json, Array<String>::class.java).toList()
            } catch (e: Exception) {
                null
            }
        }
        
        return Herb(
            id = id,
            name = name,
            pinYin = pinYin,
            category = category,
            url = url,
            medicinalPart = medicinalPart,
            tasteMeridian = tasteMeridian,
            properties = properties,
            taste = taste,
            meridians = parseStringList(meridians),
            effects = effects,
            functions = parseStringList(functions),
            clinicalApplication = parseStringList(clinicalApplication),
            prescriptionName = prescriptionName,
            usageDosage = usageDosage,
            notes = parseStringList(notes),
            formulas = parseStringList(formulas),
            literature = parseStringList(literature),
            affiliatedHerbs = parseStringList(affiliatedHerbs),
            images = parseStringList(images)
        )
    }

    companion object {
        // Convert from domain model to database entity
        fun fromHerb(herb: Herb): HerbEntity {
            val gson = Gson()
            
            // 将List<String>转换为JSON字符串
            fun stringListToJson(list: List<String>?): String? {
                if (list == null) return null
                return try {
                    gson.toJson(list)
                } catch (e: Exception) {
                    null
                }
            }
            
            return HerbEntity(
                id = herb.id,
                name = herb.name,
                pinYin = herb.pinYin,
                category = herb.category,
                url = herb.url,
                medicinalPart = herb.medicinalPart,
                tasteMeridian = herb.tasteMeridian,
                properties = herb.properties,
                taste = herb.taste,
                meridians = stringListToJson(herb.meridians),
                effects = herb.effects,
                functions = stringListToJson(herb.functions),
                clinicalApplication = stringListToJson(herb.clinicalApplication),
                prescriptionName = herb.prescriptionName,
                usageDosage = herb.usageDosage,
                notes = stringListToJson(herb.notes),
                formulas = stringListToJson(herb.formulas),
                literature = stringListToJson(herb.literature),
                affiliatedHerbs = stringListToJson(herb.affiliatedHerbs),
                images = stringListToJson(herb.images)
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