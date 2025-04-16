package com.king.easynote.domain.model

import androidx.room.ColumnInfo
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
    @PrimaryKey override val id: Int? = null,
    val type: NoteType = NoteType.TEXT,
    val created: Long = 0,
    @ColumnInfo(name = "images")
    val images: List<String> = emptyList(), // 新增图片URI列表
    val isRecording: Boolean = false, //是否录音
    val isPlaying: Boolean = false, //音频是否播放
    val audioPath: String = "", // 音频路径
    val duration: Int = 0, // 音频时长(秒)
    val audioNote: String = "" // 音频备注
): BaseNote()