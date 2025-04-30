package com.king.easynote.domain.usecase

import com.king.easynote.base.domain.usecase.UseCase
import com.king.easynote.domain.model.Note
import com.king.easynote.domain.repository.NoteRepository

/**
 * 获取笔记
 */
class GetNoteUseCase(private val repository: NoteRepository) : UseCase<Int, Note?> {

    override suspend fun execute(noteId: Int): Note? {
        return repository.queryNoteById(noteId)
    }
}