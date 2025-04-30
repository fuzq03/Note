package com.king.easynote.domain.repository

import com.king.easynote.domain.model.NoteExtraInfoEntity

interface NoteExtraInfoRepository {
    suspend fun saveNoteExtraInfo(info: NoteExtraInfoEntity)
}