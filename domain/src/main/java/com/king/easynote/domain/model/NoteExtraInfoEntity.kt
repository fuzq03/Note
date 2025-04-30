package com.king.easynote.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "note_extra_info")
data class NoteExtraInfoEntity (
    @PrimaryKey
    val noteId: Int,
    val selectedCategory: String = "",
    val isStarred: Boolean = false
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String
)