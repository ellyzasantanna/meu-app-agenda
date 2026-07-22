package com.example.data.dao

import androidx.room.*
import com.example.data.model.ImportantDate
import kotlinx.coroutines.flow.Flow

@Dao
interface ImportantDateDao {
    @Query("SELECT * FROM important_dates ORDER BY dateEpochDay ASC")
    fun getAllImportantDates(): Flow<List<ImportantDate>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImportantDate(importantDate: ImportantDate): Long

    @Update
    suspend fun updateImportantDate(importantDate: ImportantDate)

    @Delete
    suspend fun deleteImportantDate(importantDate: ImportantDate)

    @Query("DELETE FROM important_dates WHERE id = :id")
    suspend fun deleteById(id: Long)
}
