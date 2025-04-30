package com.king.easynote.domain.usecase

import com.king.easynote.domain.model.NoteExtraInfoEntity
import com.king.easynote.domain.repository.NoteExtraInfoRepository

class SaveNoteExtraInfoUseCase (private val noteExtraInfoRepository: NoteExtraInfoRepository) {
    suspend operator fun invoke(info: NoteExtraInfoEntity) {
        noteExtraInfoRepository.saveNoteExtraInfo(info)
    }
}