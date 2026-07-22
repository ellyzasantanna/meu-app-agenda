package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sticky_notes")
data class StickyNote(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val colorHex: String = "#FFF176", // Post-it Yellow default
    val priority: String = "MEDIA", // ALTA, MEDIA, BAIXA
    val isPinned: Boolean = false,
    val orderPosition: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

