package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "appointments")
data class Appointment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val dateEpochDay: Long, // LocalDate.toEpochDay()
    val hour: Int = 9,
    val minute: Int = 0,
    val category: String = "Geral", // Geral, Trabalho, Pessoal, Estudo, Saúde
    val priority: String = "MEDIA", // ALTA, MEDIA, BAIXA
    val isCompleted: Boolean = false,
    val colorHex: String = "#6750A4"
)

