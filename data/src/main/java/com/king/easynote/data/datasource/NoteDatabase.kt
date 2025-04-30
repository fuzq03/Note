package com.king.easynote.data.datasource

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.king.easynote.domain.model.Note

@Database(entities = [Note::class], version = 4, exportSchema = false)
@TypeConverters(ListConverter::class)
abstract class NoteDatabase : RoomDatabase() {

    abstract val noteDao: NoteDao

    companion object {
        const val DATABASE_NAME = "note.db"
    }
}