package com.king.easynote.presentation.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.king.easynote.base.presentation.viewmodel.BaseViewModel
import com.king.easynote.domain.model.Note
import com.king.easynote.domain.usecase.NoteUseCases
import com.king.easynote.presentation.search.SortOption
import com.king.easynote.presentation.share.SaveSearchInfoUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 */
@HiltViewModel
class NoteListViewModel @Inject constructor(private val noteUseCases: NoteUseCases, private val application: Application) :
    BaseViewModel<NoteListViewModel.NoteListState, NoteListViewModel.NoteListEvent>(
        NoteListState()
    ) {

    /**
     * 原始数据
     */
    private val originData = mutableStateOf<List<Note>>(ArrayList())
    //当前选中标签状态
    public val selectedTag = mutableStateOf<String>("全部")

    init {
        viewModelScope.launch {
            noteUseCases.getNoteList.execute(Unit).onEach {
                originData.value = it
                updateNoteList(state.value.text)
            }.launchIn(viewModelScope)
        }
    }

    override fun onEvent(event: NoteListEvent) {
        when (event) {
            is NoteListEvent.SearchNote -> {
                updateNoteList(event.text)
            }
            is NoteListEvent.OpenNote -> {
                openNote(event.noteId)
            }
        }
    }

    /**
     * 打开某个笔记
     */
    private fun openNote(noteId: Int?) {
        if (noteId == null) return
        SaveSearchInfoUtil.saveNoteInfo(application, noteId, SaveSearchInfoUtil.getUseCount(noteId, application))
    }

    /**
     * 更新笔记列表
     */
    private fun updateNoteList(text: String) {
        var filteredNotes = if (text.isNotBlank()) {
            originData.value.filter { note ->
                note.title.contains(text) || note.content.contains(text) || note.category.contains(
                    text
                )
            }
        } else originData.value

        // 根据选中标签过滤
        if (selectedTag.value != null && selectedTag.value != "全部") {
            filteredNotes = filteredNotes.filter { it.category == selectedTag.value }
        }

        var sortedNotes = filteredNotes
        // 先按创建时间降序排序，让新创建的笔记在最上面
        sortedNotes = sortedNotes.sortedByDescending { it.timestamp }
        // 再根据排序选项进行二次排序
        val sortOption = SaveSearchInfoUtil.getSortOption(application)
        sortedNotes = when (sortOption) {
            SortOption.RECENTLY_USED -> sortedNotes.sortedByDescending { note ->
                note.id?.let { SaveSearchInfoUtil.getLastUsedTime(it, application) }
            }
            SortOption.FREQUENTLY_USED -> sortedNotes.sortedByDescending { note ->
                note.id?.let { SaveSearchInfoUtil.getUseCount(it, application) }
            }
        }

        internalState.value = state.value.copy(
            text = text,
            notes = sortedNotes
        )
    }

    fun updateSelectedTag(tag: String) {
        selectedTag.value = tag
        updateNoteList(state.value.text)
    }

    /**
     * 状态
     */
    data class NoteListState(
        val text: String = "",
        val notes: List<Note> = emptyList(),
    )

    /**
     * 事件
     */
    sealed interface NoteListEvent {
        data class SearchNote(val text: String) : NoteListEvent
        data class OpenNote(val noteId: Int?) : NoteListEvent
    }
}

