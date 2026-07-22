package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AgendaRepository
import com.example.data.model.Appointment
import com.example.data.model.ImportantDate
import com.example.data.model.StickyNote
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class AgendaTab(val title: String) {
    CALENDAR("Calendário"),
    STICKY_NOTES("Post-its"),
    IMPORTANT_DATES("Datas")
}

class AgendaViewModel(private val repository: AgendaRepository) : ViewModel() {

    private val _selectedTab = MutableStateFlow(AgendaTab.CALENDAR)
    val selectedTab: StateFlow<AgendaTab> = _selectedTab.asStateFlow()

    private val _isDarkTheme = MutableStateFlow<Boolean?>(null) // null = system default
    val isDarkTheme: StateFlow<Boolean?> = _isDarkTheme.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    // Query for notes search / filter
    private val _noteSearchQuery = MutableStateFlow("")
    val noteSearchQuery: StateFlow<String> = _noteSearchQuery.asStateFlow()

    // Query for important date filter category
    private val _importantDateCategory = MutableStateFlow("Todos")
    val importantDateCategory: StateFlow<String> = _importantDateCategory.asStateFlow()

    val allAppointments: StateFlow<List<Appointment>> = repository.allAppointments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appointmentsForSelectedDate: StateFlow<List<Appointment>> = _selectedDate.flatMapLatest { date ->
        repository.getAppointmentsForDate(date.toEpochDay())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotes: StateFlow<List<StickyNote>> = repository.allNotes
        .combine(_noteSearchQuery) { notes, query ->
            if (query.isBlank()) notes
            else notes.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.content.contains(query, ignoreCase = true)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allImportantDates: StateFlow<List<ImportantDate>> = repository.allImportantDates
        .combine(_importantDateCategory) { dates, category ->
            if (category == "Todos") dates
            else dates.filter { it.category == category }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        seedInitialDataIfEmpty()
    }

    fun selectTab(tab: AgendaTab) {
        _selectedTab.value = tab
    }

    fun toggleDarkTheme(isDark: Boolean) {
        _isDarkTheme.value = isDark
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun setNoteSearchQuery(query: String) {
        _noteSearchQuery.value = query
    }

    fun setImportantDateCategory(category: String) {
        _importantDateCategory.value = category
    }

    // Appointment Operations
    fun addOrUpdateAppointment(appointment: Appointment) {
        viewModelScope.launch {
            if (appointment.id == 0L) {
                repository.insertAppointment(appointment)
            } else {
                repository.updateAppointment(appointment)
            }
        }
    }

    fun toggleAppointmentCompleted(appointment: Appointment) {
        viewModelScope.launch {
            repository.updateAppointment(appointment.copy(isCompleted = !appointment.isCompleted))
        }
    }

    fun deleteAppointment(appointment: Appointment) {
        viewModelScope.launch {
            repository.deleteAppointment(appointment)
        }
    }

    // Sticky Note Operations
    fun addOrUpdateNote(note: StickyNote) {
        viewModelScope.launch {
            if (note.id == 0L) {
                repository.insertNote(note)
            } else {
                repository.updateNote(note.copy(updatedAt = System.currentTimeMillis()))
            }
        }
    }

    fun togglePinNote(note: StickyNote) {
        viewModelScope.launch {
            repository.updateNote(note.copy(isPinned = !note.isPinned, updatedAt = System.currentTimeMillis()))
        }
    }

    fun deleteNote(note: StickyNote) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    fun moveNoteOrder(note: StickyNote, moveUp: Boolean) {
        viewModelScope.launch {
            val currentList = allNotes.value
            val index = currentList.indexOfFirst { it.id == note.id }
            if (index != -1) {
                val newOrder = if (moveUp) note.orderPosition - 1 else note.orderPosition + 1
                repository.updateNote(note.copy(orderPosition = newOrder, updatedAt = System.currentTimeMillis()))
            }
        }
    }

    // Important Date Operations
    fun addOrUpdateImportantDate(importantDate: ImportantDate) {
        viewModelScope.launch {
            if (importantDate.id == 0L) {
                repository.insertImportantDate(importantDate)
            } else {
                repository.updateImportantDate(importantDate)
            }
        }
    }

    fun deleteImportantDate(importantDate: ImportantDate) {
        viewModelScope.launch {
            repository.deleteImportantDate(importantDate)
        }
    }

    private fun seedInitialDataIfEmpty() {
        viewModelScope.launch {
            val currentYear = LocalDate.now().year
            val today = LocalDate.now()

            // Seed Notes if empty
            if (repository.allNotes.first().isEmpty()) {
                repository.insertNote(
                    StickyNote(
                        title = "Boas-vindas!",
                        content = "Seja bem-vindo à sua nova Agenda Pessoal! Aqui você pode criar Post-its coloridos, organizar compromissos e acompanhar datas especiais.",
                        colorHex = "#FFF176",
                        isPinned = true
                    )
                )
                repository.insertNote(
                    StickyNote(
                        title = "Lista de Compras",
                        content = "• Café\n• Leite\n• Pão integral\n• Frutas da estação",
                        colorHex = "#A7FFEB",
                        isPinned = false
                    )
                )
                repository.insertNote(
                    StickyNote(
                        title = "Idéias de Projeto",
                        content = "Criar um design responsivo com modo escuro e contagem regressiva para os próximos eventos do mês.",
                        colorHex = "#FF8A80",
                        isPinned = false
                    )
                )
            }

            // Seed Important Dates if empty
            if (repository.allImportantDates.first().isEmpty()) {
                repository.insertImportantDate(
                    ImportantDate(
                        title = "Ano Novo",
                        dateEpochDay = LocalDate.of(currentYear, 1, 1).toEpochDay(),
                        category = "Feriado",
                        notes = "Confraternização Universal",
                        isYearlyRepeating = true
                    )
                )
                repository.insertImportantDate(
                    ImportantDate(
                        title = "Aniversário da Ana",
                        dateEpochDay = today.plusDays(5).toEpochDay(),
                        category = "Aniversário",
                        notes = "Comprar presente e bolo!",
                        isYearlyRepeating = true
                    )
                )
                repository.insertImportantDate(
                    ImportantDate(
                        title = "Prazo do Projeto MVP",
                        dateEpochDay = today.plusDays(12).toEpochDay(),
                        category = "Prazo",
                        notes = "Entregar versão 1.0 com relatórios",
                        isYearlyRepeating = false
                    )
                )
                repository.insertImportantDate(
                    ImportantDate(
                        title = "Independência do Brasil",
                        dateEpochDay = LocalDate.of(currentYear, 9, 7).toEpochDay(),
                        category = "Feriado",
                        notes = "Feriado nacional",
                        isYearlyRepeating = true
                    )
                )
                repository.insertImportantDate(
                    ImportantDate(
                        title = "Natal",
                        dateEpochDay = LocalDate.of(currentYear, 12, 25).toEpochDay(),
                        category = "Feriado",
                        notes = "Reunião de família",
                        isYearlyRepeating = true
                    )
                )
            }

            // Seed sample appointments for today
            if (repository.allAppointments.first().isEmpty()) {
                repository.insertAppointment(
                    Appointment(
                        title = "Reunião de Alinhamento",
                        description = "Alinhamento semanal do projeto com a equipe",
                        dateEpochDay = today.toEpochDay(),
                        hour = 10,
                        minute = 0,
                        category = "Trabalho",
                        colorHex = "#42A5F5"
                    )
                )
                repository.insertAppointment(
                    Appointment(
                        title = "Treino na Academia",
                        description = "Musculação e esteira",
                        dateEpochDay = today.toEpochDay(),
                        hour = 17,
                        minute = 30,
                        category = "Saúde",
                        colorHex = "#EF5350"
                    )
                )
            }
        }
    }
}

class AgendaViewModelFactory(private val repository: AgendaRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AgendaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AgendaViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
