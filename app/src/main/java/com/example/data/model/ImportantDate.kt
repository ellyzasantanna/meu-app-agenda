package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "important_dates")
data class ImportantDate(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val dateEpochDay: Long, // LocalDate.toEpochDay()
    val category: String = "Aniversário", // Aniversário, Feriado, Prazo, Outro
    val priority: String = "MEDIA", // ALTA, MEDIA, BAIXA
    val notes: String = "",
    val isYearlyRepeating: Boolean = true
)

