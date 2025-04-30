package com.king.easynote.data.datasource

import androidx.room.Database
import androidx.room.RoomDatabase
import com.king.easynote.domain.model.CategoryEntity
import com.king.easynote.domain.model.NoteExtraInfoEntity

@Database(entities = [NoteExtraInfoEntity::class, CategoryEntity::class], version = 1, exportSchema = false)
abstract class NoteExtraInfoDataBase : RoomDatabase(){
    abstract fun noteExtraInfoDao(): NoteExtraInfoDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        const val DATABASE_NAME = "noteinfo.db"
    }
}