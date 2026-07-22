package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class Priority(
    val label: String,
    val colorHex: String,
    val badgeBgColor: Color,
    val badgeTextColor: Color
) {
    ALTA("Alta", "#EF5350", Color(0xFFFFEBEE), Color(0xFFC62828)),
    MEDIA("Média", "#FFCA28", Color(0xFFFFF8E1), Color(0xFFF57F17)),
    BAIXA("Baixa", "#66BB6A", Color(0xFFE8F5E9), Color(0xFF2E7D32));

    val color: Color
        get() = when(this) {
            ALTA -> Color(0xFFEF5350)
            MEDIA -> Color(0xFFFFB300)
            BAIXA -> Color(0xFF4CAF50)
        }

    companion object {
        fun fromString(value: String?): Priority {
            if (value == null) return MEDIA
            return values().find { 
                it.name.equals(value, ignoreCase = true) || 
                it.label.equals(value, ignoreCase = true) 
            } ?: MEDIA
        }
    }
}
