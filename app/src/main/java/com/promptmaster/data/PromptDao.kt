package com.promptmaster.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PromptDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(prompt: Prompt): Long

    @Update
    suspend fun update(prompt: Prompt)

    @Delete
    suspend fun delete(prompt: Prompt)

    @Query("SELECT * FROM prompts WHERE id = :id")
    fun getPrompt(id: Int): Flow<Prompt>

    @Query("SELECT * FROM prompts ORDER BY title ASC")
    fun getAllPrompts(): Flow<List<Prompt>>

    @Query("""
        SELECT * FROM prompts
        WHERE (:searchQuery IS NULL OR :searchQuery = '' OR title LIKE '%' || :searchQuery || '%' OR description LIKE '%' || :searchQuery || '%' OR tags LIKE '%' || :searchQuery || '%' OR category LIKE '%' || :searchQuery || '%')
        AND (:category IS NULL OR category = :category)
        AND (:recommendedModel IS NULL OR recommendedModel = :recommendedModel)
        AND (:tag IS NULL OR tags LIKE '%' || :tag || '%')
        ORDER BY title ASC
    """)
    fun searchPrompts(
        searchQuery: String?,
        category: String?,
        recommendedModel: String?,
        tag: String? // Searching for one tag at a time for simplicity, can be extended
    ): Flow<List<Prompt>>

    @Query("SELECT * FROM prompts WHERE isFavorite = 1 ORDER BY title ASC")
    fun getFavoritePrompts(): Flow<List<Prompt>>
}
