package com.promptmaster.data

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "prompts")
data class Prompt(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val category: String,
    val tags: List<String>, // Will need a TypeConverter
    val recommendedModel: String,
    val customizableFields: Map<String, String>, // Will need a TypeConverter
    val imagePath: String?, // Path to image in internal storage
    val videoPath: String?, // Path to video in internal storage (optional)
    val isFavorite: Boolean = false
)
