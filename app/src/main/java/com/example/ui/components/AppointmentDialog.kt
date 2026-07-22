package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
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
import com.example.data.model.Appointment
import com.example.data.model.Priority
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentDialog(
    appointmentToEdit: Appointment? = null,
    selectedDate: LocalDate,
    onDismiss: () -> Unit,
    onSave: (Appointment) -> Unit
) {
    var title by remember { mutableStateOf(appointmentToEdit?.title ?: "") }
    var description by remember { mutableStateOf(appointmentToEdit?.description ?: "") }
    var hour by remember { mutableIntStateOf(appointmentToEdit?.hour ?: 9) }
    var minute by remember { mutableIntStateOf(appointmentToEdit?.minute ?: 0) }
    var category by remember { mutableStateOf(appointmentToEdit?.category ?: "Trabalho") }
    var selectedPriority by remember { mutableStateOf(Priority.fromString(appointmentToEdit?.priority)) }
    var isTimePickerOpen by remember { mutableStateOf(false) }

    val categories = listOf("Trabalho", "Pessoal", "Estudo", "Saúde", "Geral")
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
                .testTag("appointment_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (appointmentToEdit == null) "Novo Compromisso" else "Editar Compromisso",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = selectedDate.format(dateFormatter),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("appointment_title_input")
                )

                // Description Input
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição / Anotação (opcional)") },
                    maxLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("appointment_desc_input")
                )

                // Priority Selector
                PrioritySelector(
                    selectedPriority = selectedPriority,
                    onPrioritySelected = { selectedPriority = it }
                )

                // Time selector
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .clickable { isTimePickerOpen = true }
                        .padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccessTime,
                            contentDescription = "Horário",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Horário:",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Text(
                        text = String.format(Locale.getDefault(), "%02d:%02d", hour, minute),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Category chips
                Text(
                    text = "Categoria:",
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
                            onClick = { category = cat },
                            label = { Text(cat, style = MaterialTheme.typography.bodySmall) }
                        )
                    }
                }

                // Actions
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
                                    Appointment(
                                        id = appointmentToEdit?.id ?: 0L,
                                        title = title.trim(),
                                        description = description.trim(),
                                        dateEpochDay = selectedDate.toEpochDay(),
                                        hour = hour,
                                        minute = minute,
                                        category = category,
                                        priority = selectedPriority.name,
                                        isCompleted = appointmentToEdit?.isCompleted ?: false
                                    )
                                )
                            }
                        },
                        enabled = title.isNotBlank(),
                        modifier = Modifier.testTag("save_appointment_button")
                    ) {
                        Text("Salvar")
                    }
                }
            }
        }
    }


    if (isTimePickerOpen) {
        val timePickerState = rememberTimePickerState(
            initialHour = hour,
            initialMinute = minute,
            is24Hour = true
        )

        AlertDialog(
            onDismissRequest = { isTimePickerOpen = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        hour = timePickerState.hour
                        minute = timePickerState.minute
                        isTimePickerOpen = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { isTimePickerOpen = false }) {
                    Text("Cancelar")
                }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }
}
