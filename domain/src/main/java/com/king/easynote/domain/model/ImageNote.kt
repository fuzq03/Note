package com.king.easynote.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ImageNote(
    val imagePath: String,
    override val timestamp: Long,
    override val color: Int,
    override val title: String,
    @PrimaryKey
    override val id: Int? = null,
): BaseNote() {

}

@Entity
data class AudioNote(
    val audioPath: String,
    val duration: Long,
    override val timestamp: Long,
    override val color: Int,
    override val title: String,
    @PrimaryKey
    override val id: Int? = null,
): BaseNote()