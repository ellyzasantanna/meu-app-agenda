package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Priority
import com.example.data.model.StickyNote

@Composable
fun NoteDialog(
    noteToEdit: StickyNote? = null,
    onDismiss: () -> Unit,
    onSave: (StickyNote) -> Unit
) {
    var title by remember { mutableStateOf(noteToEdit?.title ?: "") }
    var content by remember { mutableStateOf(noteToEdit?.content ?: "") }
    var selectedColorHex by remember { mutableStateOf(noteToEdit?.colorHex ?: "#FFF176") }
    var selectedPriority by remember { mutableStateOf(Priority.fromString(noteToEdit?.priority)) }

    val colorOptions = listOf(
        "#FFF176" to "Amarelo",
        "#FF8A80" to "Rosa",
        "#A7FFEB" to "Menta",
        "#CCFF90" to "Verde",
        "#E1BEE7" to "Roxo",
        "#FFD180" to "Laranja"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("note_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (noteToEdit == null) "Novo Post-it" else "Editar Post-it",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                // Priority Selector
                PrioritySelector(
                    selectedPriority = selectedPriority,
                    onPrioritySelected = { selectedPriority = it }
                )

                // Color Selection Row
                Text(
                    text = "Escolha a cor do Post-it:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    colorOptions.forEach { (hex, name) ->
                        val colorValue = try {
                            Color(android.graphics.Color.parseColor(hex))
                        } catch (e: Exception) {
                            Color.Yellow
                        }
                        val isSelected = selectedColorHex == hex

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(colorValue)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                                .clickable { selectedColorHex = hex }
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = name,
                                    tint = Color.Black.copy(alpha = 0.8f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título da Nota") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("note_title_input")
                )

                // Content Input
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Conteúdo / Anotações") },
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("note_content_input")
                )

                // Action buttons
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank() || content.isNotBlank()) {
                                onSave(
                                    StickyNote(
                                        id = noteToEdit?.id ?: 0L,
                                        title = title.trim(),
                                        content = content.trim(),
                                        colorHex = selectedColorHex,
                                        priority = selectedPriority.name,
                                        isPinned = noteToEdit?.isPinned ?: false,
                                        orderPosition = noteToEdit?.orderPosition ?: 0
                                    )
                                )
                            }
                        },
                        enabled = title.isNotBlank() || content.isNotBlank(),
                        modifier = Modifier.testTag("save_note_button")
                    ) {
                        Text("Salvar Nota")
                    }
                }
            }
        }
    }
}

