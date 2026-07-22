package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ImportantDate
import com.example.data.model.Priority
import com.example.ui.components.DeleteConfirmationDialog
import com.example.ui.components.ImportantDateDialog
import com.example.ui.components.PriorityBadge
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportantDatesScreen(
    importantDates: List<ImportantDate>,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    onSaveImportantDate: (ImportantDate) -> Unit,
    onDeleteImportantDate: (ImportantDate) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDateDialog by remember { mutableStateOf(false) }
    var dateToEdit by remember { mutableStateOf<ImportantDate?>(null) }
    var dateToDelete by remember { mutableStateOf<ImportantDate?>(null) }

    val categories = listOf("Todos", "Aniversário", "Feriado", "Prazo", "Outro")
    val portugueseLocale = Locale("pt", "BR")
    val today = LocalDate.now()

    // Calculate events happening this week and this month for alert banners
    val upcomingThisWeekCount = importantDates.count { date ->
        val eventDate = getAdjustedEventDate(date, today)
        val daysUntil = ChronoUnit.DAYS.between(today, eventDate)
        daysUntil in 0..7
    }

    val upcomingThisMonthCount = importantDates.count { date ->
        val eventDate = getAdjustedEventDate(date, today)
        eventDate.month == today.month && eventDate.year == today.year
    }

    Scaffold(
        modifier = modifier.testTag("important_dates_screen"),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    dateToEdit = null
                    showDateDialog = true
                },
                icon = { Icon(Icons.Filled.Add, contentDescription = "Nova Data") },
                text = { Text("Nova Data Importante") },
                modifier = Modifier.testTag("add_important_date_fab")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                Text(
                    text = "Datas Importantes",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Aniversários, feriados e prazos com contagem regressiva",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Visual Alerts for This Week / Month
            if (upcomingThisWeekCount > 0 || upcomingThisMonthCount > 0) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.8f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("visual_alert_banner")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.NotificationsActive,
                            contentDescription = "Alerta de Datas",
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Atenção a este mês!",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Text(
                                text = buildString {
                                    if (upcomingThisWeekCount > 0) append("$upcomingThisWeekCount evento(s) nesta semana! ")
                                    if (upcomingThisMonthCount > 0) append("$upcomingThisMonthCount no mês atual.")
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }

            // Category Filter Chips Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { onSelectCategory(category) },
                        label = { Text(category) },
                        leadingIcon = {
                            val icon = when (category) {
                                "Aniversário" -> Icons.Filled.Cake
                                "Feriado" -> Icons.Filled.Flag
                                "Prazo" -> Icons.Filled.Timer
                                else -> Icons.Filled.Event
                            }
                            if (category != "Todos") {
                                Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    )
                }
            }

            if (importantDates.isEmpty()) {
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
                            imageVector = Icons.Filled.EventNote,
                            contentDescription = "Nenhuma data",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = if (selectedCategory == "Todos") "Nenhuma data importante cadastrada.\nAdicione aniversários, prazos ou feriados!"
                            else "Nenhum evento na categoria \"$selectedCategory\".",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = {
                                dateToEdit = null
                                showDateDialog = true
                            }
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cadastrar Nova Data")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(importantDates, key = { it.id }) { date ->
                        ImportantDateCard(
                            importantDate = date,
                            today = today,
                            onEdit = {
                                dateToEdit = date
                                showDateDialog = true
                            },
                            onDelete = { dateToDelete = date }
                        )
                    }
                }
            }
        }
    }

    if (showDateDialog) {
        ImportantDateDialog(
            dateToEdit = dateToEdit,
            onDismiss = { showDateDialog = false },
            onSave = {
                onSaveImportantDate(it)
                showDateDialog = false
            }
        )
    }

    dateToDelete?.let { date ->
        DeleteConfirmationDialog(
            itemTitle = date.title,
            itemType = "data importante",
            onDismiss = { dateToDelete = null },
            onConfirmDelete = {
                onDeleteImportantDate(date)
                dateToDelete = null
            }
        )
    }
}

private fun getAdjustedEventDate(date: ImportantDate, today: LocalDate): LocalDate {
    val originalDate = LocalDate.ofEpochDay(date.dateEpochDay)
    return if (date.isYearlyRepeating) {
        var adjusted = originalDate.withYear(today.year)
        if (adjusted.isBefore(today) && ChronoUnit.DAYS.between(adjusted, today) > 0) {
            adjusted = adjusted.plusYears(1)
        }
        adjusted
    } else {
        originalDate
    }
}

@Composable
fun ImportantDateCard(
    importantDate: ImportantDate,
    today: LocalDate,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val priority = Priority.fromString(importantDate.priority)
    val portugueseLocale = Locale("pt", "BR")
    val eventDate = getAdjustedEventDate(importantDate, today)
    val daysRemaining = ChronoUnit.DAYS.between(today, eventDate)

    val categoryIcon = when (importantDate.category) {
        "Aniversário" -> Icons.Filled.Cake
        "Feriado" -> Icons.Filled.Flag
        "Prazo" -> Icons.Filled.Timer
        else -> Icons.Filled.Event
    }

    val isThisWeek = daysRemaining in 0..7
    val isToday = daysRemaining == 0L

    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = when {
                isToday -> MaterialTheme.colorScheme.primaryContainer
                isThisWeek -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("important_date_card_${importantDate.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth()
        ) {
            // Icon Badge
            Surface(
                shape = CircleShape,
                color = when (importantDate.category) {
                    "Aniversário" -> Color(0xFFAB47BC).copy(alpha = 0.2f)
                    "Feriado" -> Color(0xFF26A69A).copy(alpha = 0.2f)
                    "Prazo" -> Color(0xFFEF5350).copy(alpha = 0.2f)
                    else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                },
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = importantDate.category,
                        tint = when (importantDate.category) {
                            "Aniversário" -> Color(0xFFAB47BC)
                            "Feriado" -> Color(0xFF26A69A)
                            "Prazo" -> Color(0xFFEF5350)
                            else -> MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                PriorityBadge(priority = priority, modifier = Modifier.padding(bottom = 2.dp))

                Text(
                    text = importantDate.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = eventDate.format(DateTimeFormatter.ofPattern("dd 'de' MMMM", portugueseLocale)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                if (importantDate.notes.isNotBlank()) {
                    Text(
                        text = importantDate.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Countdown Badge & Actions
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when {
                        isToday -> MaterialTheme.colorScheme.primary
                        daysRemaining in 1..7 -> MaterialTheme.colorScheme.tertiary
                        daysRemaining < 0 -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(
                        text = when {
                            isToday -> "É HOJE!"
                            daysRemaining == 1L -> "Amanhã"
                            daysRemaining > 1 -> "Faltam $daysRemaining dias"
                            else -> "Passou há ${-daysRemaining} dias"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isToday -> MaterialTheme.colorScheme.onPrimary
                            daysRemaining in 1..7 -> MaterialTheme.colorScheme.onTertiary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                Row(modifier = Modifier.padding(top = 4.dp)) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Editar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Excluir",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

