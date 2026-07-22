package com.example.data

import com.example.data.dao.AppointmentDao
import com.example.data.dao.ImportantDateDao
import com.example.data.dao.StickyNoteDao
import com.example.data.model.Appointment
import com.example.data.model.ImportantDate
import com.example.data.model.StickyNote
import kotlinx.coroutines.flow.Flow

class AgendaRepository(
    private val appointmentDao: AppointmentDao,
    private val stickyNoteDao: StickyNoteDao,
    private val importantDateDao: ImportantDateDao
) {
    val allAppointments: Flow<List<Appointment>> = appointmentDao.getAllAppointments()
    val allNotes: Flow<List<StickyNote>> = stickyNoteDao.getAllNotes()
    val allImportantDates: Flow<List<ImportantDate>> = importantDateDao.getAllImportantDates()

    fun getAppointmentsForDate(dateEpochDay: Long): Flow<List<Appointment>> {
        return appointmentDao.getAppointmentsForDate(dateEpochDay)
    }

    suspend fun insertAppointment(appointment: Appointment) = appointmentDao.insertAppointment(appointment)
    suspend fun updateAppointment(appointment: Appointment) = appointmentDao.updateAppointment(appointment)
    suspend fun deleteAppointment(appointment: Appointment) = appointmentDao.deleteAppointment(appointment)

    suspend fun insertNote(note: StickyNote) = stickyNoteDao.insertNote(note)
    suspend fun updateNote(note: StickyNote) = stickyNoteDao.updateNote(note)
    suspend fun deleteNote(note: StickyNote) = stickyNoteDao.deleteNote(note)

    suspend fun insertImportantDate(importantDate: ImportantDate) = importantDateDao.insertImportantDate(importantDate)
    suspend fun updateImportantDate(importantDate: ImportantDate) = importantDateDao.updateImportantDate(importantDate)
    suspend fun deleteImportantDate(importantDate: ImportantDate) = importantDateDao.deleteImportantDate(importantDate)
}
