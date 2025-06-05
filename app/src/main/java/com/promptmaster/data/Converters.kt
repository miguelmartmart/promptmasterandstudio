package com.promptmaster.data


import androidx.room.TypeConverter

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        val gson = Gson()
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        val gson = Gson()
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromStringMap(value: Map<String, String>?): String? {
        val gson = Gson()
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringMap(value: String?): Map<String, String>? {
        val gson = Gson()
        val type = object : TypeToken<Map<String, String>>() {}.type
        return gson.fromJson(value, type)
    }
}
