package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Appointment
import com.example.data.model.ImportantDate
import com.example.data.model.Priority
import com.example.ui.components.AppointmentDialog
import com.example.ui.components.DeleteConfirmationDialog
import com.example.ui.components.PriorityBadge
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    selectedDate: LocalDate,
    allAppointments: List<Appointment>,
    appointmentsForSelectedDate: List<Appointment>,
    allImportantDates: List<ImportantDate>,
    onSelectDate: (LocalDate) -> Unit,
    onSaveAppointment: (Appointment) -> Unit,
    onToggleCompleted: (Appointment) -> Unit,
    onDeleteAppointment: (Appointment) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentMonth by remember { mutableStateOf(YearMonth.from(selectedDate)) }
    var isMonthlyView by remember { mutableStateOf(true) }
    var showAppointmentDialog by remember { mutableStateOf(false) }
    var appointmentToEdit by remember { mutableStateOf<Appointment?>(null) }
    var appointmentToDelete by remember { mutableStateOf<Appointment?>(null) }

    val portugueseLocale = Locale("pt", "BR")
    val today = LocalDate.now()

    Scaffold(
        modifier = modifier.testTag("calendar_screen"),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    appointmentToEdit = null
                    showAppointmentDialog = true
                },
                icon = { Icon(Icons.Filled.Add, contentDescription = "Novo Compromisso") },
                text = { Text("Novo Compromisso") },
                modifier = Modifier.testTag("add_appointment_fab")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Month Selector & View Toggle Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Column {
                    Text(
                        text = currentMonth.month
                            .getDisplayName(TextStyle.FULL, portugueseLocale)
                            .replaceFirstChar { it.uppercase() } + " ${currentMonth.year}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (selectedDate == today) "Hoje" else selectedDate.format(DateTimeFormatter.ofPattern("dd 'de' MMM", portugueseLocale)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Today button
                    IconButton(
                        onClick = {
                            val now = LocalDate.now()
                            currentMonth = YearMonth.from(now)
                            onSelectDate(now)
                        },
                        modifier = Modifier.testTag("today_button")
                    ) {
                        Icon(Icons.Filled.Today, contentDescription = "Ir para Hoje", tint = MaterialTheme.colorScheme.primary)
                    }

                    IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                        Icon(Icons.Filled.ChevronLeft, contentDescription = "Mês Anterior")
                    }
                    IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                        Icon(Icons.Filled.ChevronRight, contentDescription = "Próximo Mês")
                    }

                    // View Mode Toggle (Monthly / Daily)
                    FilterChip(
                        selected = isMonthlyView,
                        onClick = { isMonthlyView = !isMonthlyView },
                        label = { Text(if (isMonthlyView) "Mês" else "Dia") },
                        leadingIcon = {
                            Icon(
                                imageVector = if (isMonthlyView) Icons.Filled.GridView else Icons.Filled.ViewDay,
                                contentDescription = "Alternar Visão",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }

            if (isMonthlyView) {
                // Days of Week Header
                val daysOfWeek = listOf("Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    daysOfWeek.forEach { dayName ->
                        Text(
                            text = dayName,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Month Calendar Grid
                val firstDayOfMonth = currentMonth.atDay(1)
                val dayOfWeekOffset = firstDayOfMonth.dayOfWeek.value % 7 // Sunday = 0
                val totalDaysInMonth = currentMonth.lengthOfMonth()

                val calendarDays = remember(currentMonth) {
                    val days = mutableListOf<LocalDate?>()
                    for (i in 0 until dayOfWeekOffset) {
                        days.add(null)
                    }
                    for (day in 1..totalDaysInMonth) {
                        days.add(currentMonth.atDay(day))
                    }
                    days
                }

                // Grid of Days
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(8.dp)
                ) {
                    items(calendarDays) { date ->
                        if (date == null) {
                            Spacer(modifier = Modifier.aspectRatio(1f))
                        } else {
                            val isSelected = date == selectedDate
                            val isToday = date == today
                            val dayAppointments = allAppointments.filter { it.dateEpochDay == date.toEpochDay() }
                            val hasAppointments = dayAppointments.isNotEmpty()
                            val topPriorityForDay = dayAppointments.map { Priority.fromString(it.priority) }
                                .minByOrNull { it.ordinal } // ALTA = 0 (highest)
                            val hasImportantDate = allImportantDates.any {
                                val itemDate = LocalDate.ofEpochDay(it.dateEpochDay)
                                itemDate.dayOfMonth == date.dayOfMonth && itemDate.month == date.month
                            }

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isSelected -> MaterialTheme.colorScheme.primary
                                            isToday -> MaterialTheme.colorScheme.primaryContainer
                                            else -> Color.Transparent
                                        }
                                    )
                                    .border(
                                        width = if (isToday && !isSelected) 2.dp else 0.dp,
                                        color = if (isToday && !isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        onSelectDate(date)
                                    }
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = date.dayOfMonth.toString(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                        color = when {
                                            isSelected -> MaterialTheme.colorScheme.onPrimary
                                            isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                                            else -> MaterialTheme.colorScheme.onSurface
                                        }
                                    )

                                    // Priority Event Indicator Dots
                                    if (hasAppointments || hasImportantDate) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                                            modifier = Modifier.padding(top = 1.dp)
                                        ) {
                                            if (hasAppointments) {
                                                val dotColor = if (isSelected) Color.White else (topPriorityForDay?.color ?: MaterialTheme.colorScheme.primary)
                                                Box(
                                                    modifier = Modifier
                                                        .size(5.dp)
                                                        .clip(CircleShape)
                                                        .background(dotColor)
                                                )
                                            }
                                            if (hasImportantDate) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(5.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isSelected) Color.White else MaterialTheme.colorScheme.tertiary)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Daily Agenda Section Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "Compromissos para " + selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${appointmentsForSelectedDate.size} evento(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Appointments List for Selected Day
            if (appointmentsForSelectedDate.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.EventAvailable,
                            contentDescription = "Nenhum compromisso",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(56.dp)
                        )
                        Text(
                            text = "Nenhum compromisso marcado para este dia.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = {
                                appointmentToEdit = null
                                showAppointmentDialog = true
                            },
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Adicionar Compromisso")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(appointmentsForSelectedDate, key = { it.id }) { appointment ->
                        AppointmentCard(
                            appointment = appointment,
                            onToggleCompleted = { onToggleCompleted(appointment) },
                            onEdit = {
                                appointmentToEdit = appointment
                                showAppointmentDialog = true
                            },
                            onDelete = { appointmentToDelete = appointment }
                        )
                    }
                }
            }
        }
    }

    if (showAppointmentDialog) {
        AppointmentDialog(
            appointmentToEdit = appointmentToEdit,
            selectedDate = selectedDate,
            onDismiss = { showAppointmentDialog = false },
            onSave = {
                onSaveAppointment(it)
                showAppointmentDialog = false
            }
        )
    }

    appointmentToDelete?.let { appt ->
        DeleteConfirmationDialog(
            itemTitle = appt.title,
            itemType = "compromisso",
            onDismiss = { appointmentToDelete = null },
            onConfirmDelete = {
                onDeleteAppointment(appt)
                appointmentToDelete = null
            }
        )
    }
}

@Composable
fun AppointmentCard(
    appointment: Appointment,
    onToggleCompleted: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val priority = Priority.fromString(appointment.priority)

    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (appointment.isCompleted)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            else MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("appointment_card_${appointment.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth()
        ) {
            // Checkbox
            Checkbox(
                checked = appointment.isCompleted,
                onCheckedChange = { onToggleCompleted() },
                modifier = Modifier.testTag("appointment_checkbox_${appointment.id}")
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    // Priority Badge
                    PriorityBadge(priority = priority)

                    // Category Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                    ) {
                        Text(
                            text = appointment.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Time display
                    Text(
                        text = String.format(Locale.getDefault(), "%02d:%02d", appointment.hour, appointment.minute),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = appointment.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (appointment.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (appointment.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )

                if (appointment.description.isNotBlank()) {
                    Text(
                        text = appointment.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            // Edit & Delete actions
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Editar",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Excluir",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

