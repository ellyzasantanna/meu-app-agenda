package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.AppointmentDao
import com.example.data.dao.ImportantDateDao
import com.example.data.dao.StickyNoteDao
import com.example.data.model.Appointment
import com.example.data.model.ImportantDate
import com.example.data.model.StickyNote

@Database(
    entities = [Appointment::class, StickyNote::class, ImportantDate::class],
    version = 2,
    exportSchema = false
)
abstract class AgendaDatabase : RoomDatabase() {
    abstract fun appointmentDao(): AppointmentDao
    abstract fun stickyNoteDao(): StickyNoteDao
    abstract fun importantDateDao(): ImportantDateDao

    companion object {
        @Volatile
        private var INSTANCE: AgendaDatabase? = null

        fun getDatabase(context: Context): AgendaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AgendaDatabase::class.java,
                    "agenda_pessoal_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
