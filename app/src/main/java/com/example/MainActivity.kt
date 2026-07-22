package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AgendaDatabase
import com.example.data.AgendaRepository
import com.example.ui.components.AgendaBottomNavigationBar
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.ImportantDatesScreen
import com.example.ui.screens.NotesScreen
import com.example.ui.theme.AgendaTheme
import com.example.ui.viewmodel.AgendaTab
import com.example.ui.viewmodel.AgendaViewModel
import com.example.ui.viewmodel.AgendaViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AgendaDatabase.getDatabase(applicationContext)
        val repository = AgendaRepository(
            appointmentDao = database.appointmentDao(),
            stickyNoteDao = database.stickyNoteDao(),
            importantDateDao = database.importantDateDao()
        )
        val factory = AgendaViewModelFactory(repository)

        setContent {
            val viewModel: AgendaViewModel = viewModel(factory = factory)
            val userDarkThemeSetting by viewModel.isDarkTheme.collectAsStateWithLifecycle()
            val isDarkTheme = userDarkThemeSetting ?: isSystemInDarkTheme()

            AgendaTheme(darkTheme = isDarkTheme) {
                AgendaAppScreen(
                    viewModel = viewModel,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { viewModel.toggleDarkTheme(!isDarkTheme) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaAppScreen(
    viewModel: AgendaViewModel,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val allAppointments by viewModel.allAppointments.collectAsStateWithLifecycle()
    val appointmentsForSelectedDate by viewModel.appointmentsForSelectedDate.collectAsStateWithLifecycle()
    val allNotes by viewModel.allNotes.collectAsStateWithLifecycle()
    val noteSearchQuery by viewModel.noteSearchQuery.collectAsStateWithLifecycle()
    val allImportantDates by viewModel.allImportantDates.collectAsStateWithLifecycle()
    val importantDateCategory by viewModel.importantDateCategory.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Agenda Pessoal",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onToggleTheme,
                        modifier = Modifier.testTag("theme_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                            contentDescription = if (isDarkTheme) "Modo Claro" else "Modo Escuro"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            AgendaBottomNavigationBar(
                currentTab = selectedTab,
                onTabSelected = { viewModel.selectTab(it) }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = selectedTab, label = "TabSwitch") { tab ->
                when (tab) {
                    AgendaTab.CALENDAR -> CalendarScreen(
                        selectedDate = selectedDate,
                        allAppointments = allAppointments,
                        appointmentsForSelectedDate = appointmentsForSelectedDate,
                        allImportantDates = allImportantDates,
                        onSelectDate = { viewModel.selectDate(it) },
                        onSaveAppointment = { viewModel.addOrUpdateAppointment(it) },
                        onToggleCompleted = { viewModel.toggleAppointmentCompleted(it) },
                        onDeleteAppointment = { viewModel.deleteAppointment(it) }
                    )

                    AgendaTab.STICKY_NOTES -> NotesScreen(
                        notes = allNotes,
                        searchQuery = noteSearchQuery,
                        onSearchQueryChange = { viewModel.setNoteSearchQuery(it) },
                        onSaveNote = { viewModel.addOrUpdateNote(it) },
                        onTogglePin = { viewModel.togglePinNote(it) },
                        onDeleteNote = { viewModel.deleteNote(it) }
                    )

                    AgendaTab.IMPORTANT_DATES -> ImportantDatesScreen(
                        importantDates = allImportantDates,
                        selectedCategory = importantDateCategory,
                        onSelectCategory = { viewModel.setImportantDateCategory(it) },
                        onSaveImportantDate = { viewModel.addOrUpdateImportantDate(it) },
                        onDeleteImportantDate = { viewModel.deleteImportantDate(it) }
                    )
                }
            }
        }
    }
}
