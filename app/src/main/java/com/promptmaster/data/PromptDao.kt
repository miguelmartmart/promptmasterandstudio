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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(prompts: List<Prompt>)

    @Update
    suspend fun update(prompt: Prompt)

    @Delete
    suspend fun delete(prompt: Prompt)

    @Query("DELETE FROM prompts")
    suspend fun deleteAllPrompts()

    @Query("SELECT * FROM prompts WHERE id = :id")
    fun getPrompt(id: Int): Flow<Prompt>

    @Query("SELECT * FROM prompts ORDER BY title ASC")
    fun getAllPrompts(): Flow<List<Prompt>>

    @Query("""
        SELECT * FROM prompts
        WHERE (:searchQuery IS NULL OR :searchQuery = '' OR title LIKE '%' || :searchQuery || '%' OR description LIKE '%' || :searchQuery || '%' OR tags LIKE '%' || :searchQuery || '%' OR category LIKE '%' || :searchQuery || '%' OR subcategory LIKE '%' || :searchQuery || '%')
        AND (:category IS NULL OR category = :category)
        AND (:recommendedModel IS NULL OR recommendedModel = :recommendedModel)
        AND (:tag IS NULL OR tags LIKE '%' || :tag || '%')
        ORDER BY title ASC
    """)
    fun searchPrompts(
        searchQuery: String?,
        category: String?,
        recommendedModel: String?,
        tag: String?
    ): Flow<List<Prompt>>

    @Query("SELECT * FROM prompts WHERE isFavorite = 1 ORDER BY lastUsed DESC")
    fun getFavoritePrompts(): Flow<List<Prompt>>

    @Query("SELECT DISTINCT category FROM prompts ORDER BY category")
    fun getAllCategories(): Flow<List<String>>

    @Query("SELECT * FROM prompts WHERE (:category IS NULL OR category = :category) ORDER BY lastUsed DESC")
    fun getPromptsFiltered(category: String?): Flow<List<Prompt>>

    @Query("UPDATE prompts SET lastUsed = :timestamp WHERE id = :id")
    suspend fun updateLastUsed(id: Int, timestamp: Long)

    @Query("""
        SELECT * FROM prompts
        WHERE (:searchQuery IS NULL OR :searchQuery = '' OR title LIKE '%' || :searchQuery || '%' OR description LIKE '%' || :searchQuery || '%' OR subcategory LIKE '%' || :searchQuery || '%')
        AND (category = :category OR :category IS NULL)
        AND (subcategory = :subcategory OR :subcategory IS NULL)
        AND (NOT :showFavoritesOnly OR isFavorite = 1)
        ORDER BY isFavorite DESC, lastUsed DESC
    """)
    fun getFilteredAndSortedPrompts(
        searchQuery: String?,
        category: String?,
        subcategory: String?,
        showFavoritesOnly: Boolean
    ): Flow<List<Prompt>>

    @Query("SELECT DISTINCT subcategory FROM prompts WHERE (:category IS NULL OR category = :category) ORDER BY subcategory")
    fun getAllSubcategories(category: String?): Flow<List<String>>

    @Query("SELECT * FROM prompts ORDER BY title ASC LIMIT :limit OFFSET :offset")
    fun getPaginatedPrompts(limit: Int, offset: Int): Flow<List<Prompt>>

    @Query("""
        SELECT * FROM prompts
        WHERE (:searchQuery IS NULL OR :searchQuery = '' OR title LIKE '%' || :searchQuery || '%' OR description LIKE '%' || :searchQuery || '%' OR subcategory LIKE '%' || :searchQuery || '%')
        AND (:category IS NULL OR category = :category)
        AND (:subcategory IS NULL OR subcategory = :subcategory)
        AND (NOT :showFavoritesOnly OR isFavorite = 1)
        ORDER BY isFavorite DESC, lastUsed DESC
        LIMIT :limit OFFSET :offset
""")
    suspend fun getPaginatedFilteredAndSortedPrompts(
        searchQuery: String?,
        category: String?,
        subcategory: String?,
        showFavoritesOnly: Boolean,
        limit: Int,
        offset: Int
    ): List<Prompt>

    @Query("""
    SELECT * FROM prompts
    WHERE (:searchQuery IS NULL OR title LIKE '%' || :searchQuery || '%' OR description LIKE '%' || :searchQuery || '%')
      AND (category = :category OR :category IS NULL)
      AND (subcategory = :subcategory OR :subcategory IS NULL)
      AND (NOT :showFavoritesOnly OR isFavorite = 1)
    ORDER BY isFavorite DESC, lastUsed DESC
        LIMIT :limit OFFSET :offset
""")
    suspend fun getPaginatedFilteredAndSortedPromptsList(
        searchQuery: String?,
        category: String?,
        subcategory: String?,
        showFavoritesOnly: Boolean,
        limit: Int,
        offset: Int
    ): List<Prompt>

    @Query("""
        SELECT * FROM prompts
        WHERE (:searchQuery IS NULL OR :searchQuery = '' OR title LIKE '%' || :searchQuery || '%' OR description LIKE '%' || :searchQuery || '%')
        AND (category = :category OR :category IS NULL)
        AND (subcategory = :subcategory OR :subcategory IS NULL)
        AND (NOT :showFavoritesOnly OR isFavorite = 1)
        ORDER BY isFavorite DESC, lastUsed DESC
    """)
    suspend fun getAllFilteredAndSortedPromptsList(
        searchQuery: String?,
        category: String?,
        subcategory: String?,
        showFavoritesOnly: Boolean
    ): List<Prompt>

    @Query("SELECT * FROM prompts ORDER BY title ASC")
    suspend fun getAllPromptsList(): List<Prompt>

    @Query("SELECT COUNT(*) FROM prompts WHERE title = :title AND description = :description")
    suspend fun getPromptCountByContent(title: String, description: String): Int

    @Query("SELECT * FROM prompts WHERE title = :title AND description = :description LIMIT 1")
    suspend fun getPromptByContent(title: String, description: String): Prompt?
}
