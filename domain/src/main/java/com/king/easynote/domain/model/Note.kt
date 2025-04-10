package com.king.easynote.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 文本笔记
 */
@Entity
data class Note(
    override val title: String,
    val content: String,
    override val timestamp: Long,
    override val color: Int,
    @PrimaryKey override val id: Int? = null
): BaseNote()