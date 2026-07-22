package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ImportantDate
import com.example.data.model.Priority
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportantDateDialog(
    dateToEdit: ImportantDate? = null,
    onDismiss: () -> Unit,
    onSave: (ImportantDate) -> Unit
) {
    var title by remember { mutableStateOf(dateToEdit?.title ?: "") }
    var notes by remember { mutableStateOf(dateToEdit?.notes ?: "") }
    var category by remember { mutableStateOf(dateToEdit?.category ?: "Aniversário") }
    var selectedPriority by remember { mutableStateOf(Priority.fromString(dateToEdit?.priority)) }
    var selectedLocalDate by remember {
        mutableStateOf(
            if (dateToEdit != null) LocalDate.ofEpochDay(dateToEdit.dateEpochDay)
            else LocalDate.now()
        )
    }
    var isYearlyRepeating by remember { mutableStateOf(dateToEdit?.isYearlyRepeating ?: true) }
    var isDatePickerOpen by remember { mutableStateOf(false) }

    val categories = listOf("Aniversário", "Feriado", "Prazo", "Outro")
    val portugueseLocale = Locale("pt", "BR")
    val dateFormatter = DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", portugueseLocale)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("important_date_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (dateToEdit == null) "Nova Data Importante" else "Editar Data Importante",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nome do Evento / Pessoa") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("important_date_title_input")
                )

                // Priority Selector
                PrioritySelector(
                    selectedPriority = selectedPriority,
                    onPrioritySelected = { selectedPriority = it }
                )

                // Category selection
                Text(
                    text = "Tipo de Evento:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = {
                                category = cat
                                if (cat == "Feriado" || cat == "Aniversário") {
                                    isYearlyRepeating = true
                                }
                            },
                            label = { Text(cat, style = MaterialTheme.typography.bodySmall) }
                        )
                    }
                }

                // Date Picker trigger
                OutlinedCard(
                    onClick = { isDatePickerOpen = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                imageVector = Icons.Filled.CalendarToday,
                                contentDescription = "Data",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text("Data:")
                        }
                        Text(
                            text = selectedLocalDate.format(dateFormatter),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Yearly repetition checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isYearlyRepeating,
                        onCheckedChange = { isYearlyRepeating = it }
                    )
                    Text(
                        text = "Repetir anualmente",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Notes Input
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Observação / Lembrete (opcional)") },
                    maxLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("important_date_notes_input")
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
                            if (title.isNotBlank()) {
                                onSave(
                                    ImportantDate(
                                        id = dateToEdit?.id ?: 0L,
                                        title = title.trim(),
                                        dateEpochDay = selectedLocalDate.toEpochDay(),
                                        category = category,
                                        priority = selectedPriority.name,
                                        notes = notes.trim(),
                                        isYearlyRepeating = isYearlyRepeating
                                    )
                                )
                            }
                        },
                        enabled = title.isNotBlank(),
                        modifier = Modifier.testTag("save_important_date_button")
                    ) {
                        Text("Salvar")
                    }
                }
            }
        }
    }


    if (isDatePickerOpen) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedLocalDate
                .atStartOfDay(ZoneId.of("UTC"))
                .toInstant()
                .toEpochMilli()
        )

        DatePickerDialog(
            onDismissRequest = { isDatePickerOpen = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedLocalDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.of("UTC"))
                                .toLocalDate()
                        }
                        isDatePickerOpen = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { isDatePickerOpen = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
