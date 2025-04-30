package com.king.easynote.data.repository

import com.king.easynote.data.datasource.NoteExtraInfoDao
import com.king.easynote.domain.model.NoteExtraInfoEntity
import com.king.easynote.domain.repository.NoteExtraInfoRepository

class NoteExtraInfoRepositoryImpl (private val noteExtraInfoDao: NoteExtraInfoDao) :
    NoteExtraInfoRepository {
    override suspend fun saveNoteExtraInfo(info: NoteExtraInfoEntity) {
        noteExtraInfoDao.insertOrUpdateNoteExtraInfo(info)
    }
}