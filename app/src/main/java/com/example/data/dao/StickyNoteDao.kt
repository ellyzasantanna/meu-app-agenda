package com.example.data.dao

import androidx.room.*
import com.example.data.model.StickyNote
import kotlinx.coroutines.flow.Flow

@Dao
interface StickyNoteDao {
    @Query("SELECT * FROM sticky_notes ORDER BY isPinned DESC, orderPosition ASC, updatedAt DESC")
    fun getAllNotes(): Flow<List<StickyNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: StickyNote): Long

    @Update
    suspend fun updateNote(note: StickyNote)

    @Delete
    suspend fun deleteNote(note: StickyNote)

    @Query("DELETE FROM sticky_notes WHERE id = :id")
    suspend fun deleteById(id: Long)
}
