package com.king.easynote.domain.usecase

import com.king.easynote.base.domain.usecase.UseCase
import com.king.easynote.domain.model.Note
import com.king.easynote.domain.repository.NoteRepository

/**
 * 删除笔记
 */
class DeleteNoteUseCase(private val repository: NoteRepository) : UseCase<Int, Unit> {

    override suspend fun execute(noteId: Int) {
        repository.deleteNote(noteId)
    }

}