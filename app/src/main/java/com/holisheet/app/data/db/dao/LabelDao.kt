package com.holisheet.app.data.db.dao

import androidx.room.*
import com.holisheet.app.data.model.Label
import kotlinx.coroutines.flow.Flow

@Dao
interface LabelDao {

    @Query("SELECT * FROM labels ORDER BY name ASC")
    fun getAllLabels(): Flow<List<Label>>

    @Query("SELECT * FROM labels WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchLabels(query: String): Flow<List<Label>>

    @Query("SELECT * FROM labels WHERE name = :name LIMIT 1")
    suspend fun getLabelByName(name: String): Label?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLabel(label: Label): Long

    @Delete
    suspend fun deleteLabel(label: Label)
}
