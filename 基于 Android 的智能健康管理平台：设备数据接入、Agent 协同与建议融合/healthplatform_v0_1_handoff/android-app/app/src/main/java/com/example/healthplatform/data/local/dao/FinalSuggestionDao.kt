package com.example.healthplatform.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.healthplatform.data.local.entity.FinalSuggestionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FinalSuggestionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<FinalSuggestionEntity>)

    @Query("DELETE FROM final_suggestions")
    suspend fun clearAll()

    @Query(
        """
        SELECT * FROM final_suggestions
        ORDER BY priority DESC, createdAt DESC
        """
    )
    fun observeSuggestions(): Flow<List<FinalSuggestionEntity>>
}
