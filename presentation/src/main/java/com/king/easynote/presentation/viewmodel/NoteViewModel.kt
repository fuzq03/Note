package com.king.easynote.presentation.viewmodel

import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.king.easynote.base.presentation.viewmodel.BaseViewModel
import com.king.easynote.base.ui.theme.noteColors
import com.king.easynote.domain.exception.NoteException
import com.king.easynote.domain.model.Note
import com.king.easynote.domain.usecase.NoteUseCases
import com.king.easynote.presentation.navigation.NavArgumentKey
import com.king.easynote.presentation.navigation.NavRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 */
@HiltViewModel
class NoteViewModel @Inject constructor(
    private val noteUseCases: NoteUseCases,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<NoteViewModel.NoteState, NoteViewModel.NoteEvent>(NoteState()) {

    private val _event = MutableSharedFlow<UIEvent>()
    val event = _event.asSharedFlow()

    private var noteId: Int? = null

    init {
        savedStateHandle.get<Int>(NavArgumentKey.NoteId.name)?.takeIf { it != NavRoute.NONE_ID }
            ?.let {
                viewModelScope.launch {
                    noteUseCases.getNote.execute(it)?.also {
                        noteId = it.id
                        internalState.value = state.value.copy(
                            title = it.title,
                            content = it.content,
                            color = it.color
                        )
                    }
                }
            }
    }

    override fun onEvent(event: NoteEvent) {
        when (event) {
            is NoteEvent.ChangeTitle -> {
                internalState.value = state.value.copy(title = event.title)
            }
            is NoteEvent.ChangeContent -> {
                internalState.value = state.value.copy(content = event.content)
            }
            is NoteEvent.ChangeColor -> {
                internalState.value = state.value.copy(color = event.color)
            }
            is NoteEvent.SaveNote -> {
                viewModelScope.launch {
                    try {
                        noteUseCases.saveNote.execute(state.value.toNote(noteId))
                        // 保存后通知UI
                        _event.emit(UIEvent.SaveNote)
                    } catch (e: NoteException) {
                        // 保存失败
                        _event.emit(UIEvent.ShowMessage(e.message))
                    }
                }
            }
            else -> {}
        }
    }


    private fun NoteState.toNote(noteId: Int?): Note {
        return Note(title, content, System.currentTimeMillis(), color, noteId)
    }

    /**
     * 状态
     */
    data class NoteState(
        val title: String = "",
        val content: String = "",
        val color: Int = noteColors.random().toArgb(),
        val images: List<String> = emptyList(), // 图片URI列表
        val imageCaption: String = "", // 图片描述
        val audioPath: String = "", // 音频路径
        val duration: Int = 0, // 音频时长(秒)
        val audioNote: String = "" // 音频备注
    )

    /**
     * 事件
     */
    sealed interface NoteEvent {
        data class ChangeTitle(val title: String) : NoteEvent
        data class ChangeContent(val content: String) : NoteEvent
        data class ChangeColor(val color: Int) : NoteEvent
        object SaveNote : NoteEvent
        class AddImage(val uri: String) : NoteEvent
        class RemoveImage(val uri: String) : NoteEvent
        class UpdateCaption(val text: String) : NoteEvent
        class RecordAudio(val path: String) : NoteEvent
        class UpdateAudioNote(val text: String) : NoteEvent
    }

    /**
     * UI事件
     */
    sealed interface UIEvent {
        data class ShowMessage(val message: String?) : UIEvent
        object SaveNote : UIEvent
    }

}


