package com.king.easynote.presentation.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.king.easynote.base.presentation.viewmodel.BaseViewModel
import com.king.easynote.base.ui.theme.noteColors
import com.king.easynote.data.datasource.NoteExtraInfoDataBase
import com.king.easynote.data.repository.CategoryRepositoryImpl
import com.king.easynote.domain.exception.NoteException
import com.king.easynote.domain.model.CategoryEntity
import com.king.easynote.domain.model.Note
import com.king.easynote.domain.model.NoteType
import com.king.easynote.domain.repository.CategoryRepository
import com.king.easynote.domain.usecase.DeleteCategoryUseCase
import com.king.easynote.domain.usecase.NoteUseCases
import com.king.easynote.domain.usecase.SaveCategoryUseCase
import com.king.easynote.presentation.navigation.NavArgumentKey
import com.king.easynote.presentation.navigation.NavRoute
import com.king.easynote.presentation.search.SortOption
import com.king.easynote.presentation.share.SaveSearchInfoUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject


@HiltViewModel
class NoteViewModel @Inject constructor(
    private val noteUseCases: NoteUseCases,
    savedStateHandle: SavedStateHandle,
    val application: Application,
) : BaseViewModel<NoteViewModel.NoteState, NoteViewModel.NoteEvent>(NoteState()) {

    private val _event = MutableSharedFlow<UIEvent>()
    private var _sortOption = mutableStateOf(SaveSearchInfoUtil.getSortOption(application))
    val event = _event.asSharedFlow()
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var startTime: Long = 0
    var sortOption = _sortOption.value

    private var noteId: Int? = null

    val noteExtraInfoDataBase = Room.databaseBuilder(
        application,
        NoteExtraInfoDataBase::class.java,
        NoteExtraInfoDataBase.DATABASE_NAME
    ).fallbackToDestructiveMigration()
        .build()

    private val categoryRepository: CategoryRepository = CategoryRepositoryImpl(noteExtraInfoDataBase.categoryDao())
    private val saveCategoryUseCase = SaveCategoryUseCase(categoryRepository)
    private val deleteCategoryUseCase = DeleteCategoryUseCase(categoryRepository)

    init {
        savedStateHandle.get<Int>(NavArgumentKey.NoteId.name)?.takeIf { it != NavRoute.NONE_ID }
            ?.let {
                viewModelScope.launch {
                    try {
                        noteUseCases.getNote.execute(it)?.let {
                            noteId = it.id
                            internalState.value = state.value.copy(
                                title = it.title,
                                content = it.content,
                                color = it.color,
                                type = it.type,
                                imageCaption = it.content,
                                images = it.images,
                                isRecording = it.isRecording, //是否录音
                                isPlaying = it.isPlaying, //音频是否播放
                                audioPath = it.audioPath, // 音频路径
                                duration = it.duration, // 音频时长(秒)
                                audioNote = it.audioNote, // 音频备注
                                isStarred = it.isStarred,
                                selectedCategory = it.category,
                            )
                        }
                    }catch (e: Exception) {
                        Log.d("NoteViewModel", e.message.toString())
                    }


                }



            }
        viewModelScope.launch{
            val list = getCategories()
            internalState.value = state.value.copy(categories = list)
        }
    }

    suspend fun getCategories(): List<String>{
        // 使用 map 操作符提取 name 并转换为 List<String>
        val nameListFlow: Flow<List<String>> = getAllCategories().map { categoryList ->
            categoryList.map { it.name }
        }

        // 将 Flow<List<String>> 转换为 List<String>
        val allNameLists: List<String> = nameListFlow.first()

        return allNameLists
    }

    fun saveCategory(id: Int, categoryName: String) {
        viewModelScope.launch {
            val category = CategoryEntity(id = id, name = categoryName)
            saveCategoryUseCase(category)
        }
    }

    fun deleteCategory(categoryName: String) {
        viewModelScope.launch {
            val category = CategoryEntity(name = categoryName)
            deleteCategoryUseCase(category)
        }
    }

    fun getAllCategories(): Flow<List<CategoryEntity>> {
        return categoryRepository.getAllCategories()
    }


    fun setContext(context: Context) {
        //this.context = context
    }

    override fun onEvent(event: NoteEvent) {
        when (event) {
            is NoteEvent.SetType -> {
                internalState.value = state.value.copy(type = event.type)
            }
            is NoteEvent.ChangeTitle -> {
                internalState.value = state.value.copy(title = event.title)
            }
            is NoteEvent.ChangeContent -> {
                internalState.value = state.value.copy(content = event.content)
            }
            is NoteEvent.ChangeColor -> {
                internalState.value = state.value.copy(color = event.color)
            }
            is NoteEvent.UpdateImageCaption -> {
                internalState.value = state.value.copy(imageCaption = event.text)
            }
            is NoteEvent.UpdateAudioNote -> {
                internalState.value = state.value.copy(audioNote = event.note)
            }
            is NoteEvent.StartRecording -> startRecording()
            is NoteEvent.StopRecording -> stopRecording()
            is NoteEvent.PlayAudio -> playAudio()
            is NoteEvent.PauseAudio -> pauseAudio()
            is NoteEvent.SaveNote -> {
                viewModelScope.launch {
                    try {
                        val currentState = state.value
                        noteUseCases.saveNote.execute(
                            state.value.toNote(noteId).copy(
                                type = currentState.type,
                                images = currentState.images,
                                audioPath = currentState.audioPath,
                                content = when (currentState.type) {
                                    NoteType.TEXT -> currentState.content
                                    NoteType.IMAGE -> currentState.imageCaption
                                    NoteType.AUDIO -> currentState.audioNote
                                }

                            )
                        )
                        _event.emit(UIEvent.SaveNote)

                    } catch (e: NoteException) {
                        // 保存失败
                        _event.emit(UIEvent.ShowMessage(e.message))
                    }
                }
            }
            is NoteEvent.AddImage -> {
                internalState.value = state.value.copy(images = state.value.images + event.uri)
            }
            is NoteEvent.SelectCategory -> {
                internalState.value = internalState.value.copy(selectedCategory = event.name)
                NoteEvent.SaveNote
            }
            is NoteEvent.DeleteCategory -> {
                deleteCategory(event.name)
            }
            is NoteEvent.ToggleStar -> {
                internalState.value = internalState.value.copy(isStarred = event.isStarred)
                NoteEvent.SaveNote
            }
            is NoteEvent.ChangeSortOption -> {
                sortOption = event.option
                SaveSearchInfoUtil.saveSortOption(event.option, application)
            }
            else -> {}
        }
    }


    private fun startRecording() {

        val cacheDir = application.externalCacheDir ?: run {
            viewModelScope.launch {
                _event.emit(UIEvent.ShowMessage("无法访问存储目录"))
            }
            return
        }

        val audioFile = File(cacheDir, "audio_${System.currentTimeMillis()}.mp3").apply {
            parentFile?.mkdirs()
        }
        internalState.value = state.value.copy(audioPath = audioFile.path)
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

            setOutputFile(audioFile.absolutePath)

            prepare()
            start()
        }
        startTime = System.currentTimeMillis()
        internalState.value = state.value.copy(isRecording = true)
    }

    private fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null

        val duration = ((System.currentTimeMillis() - startTime) / 1000).toInt()
        internalState.value = state.value.copy(
            isRecording = false,
            duration = duration
        )
    }

    private fun playAudio() {
        val audioPath = state.value.audioPath

        if (audioPath.isNullOrEmpty()) {
            viewModelScope.launch{
                _event.emit(UIEvent.ShowMessage("没有可播放的录音文件"))
            }

            return
        }
        val audioFile = File(audioPath)
        if (!audioFile.exists()) {
            viewModelScope.launch {
                _event.emit(UIEvent.ShowMessage("录音文件不存在"))
            }
            return
        }

        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioPath)
                prepare()
                start()
                setOnCompletionListener {
                    internalState.value = state.value.copy(isPlaying = false)
                }
                internalState.value = state.value.copy(isPlaying = true)
            }
        } catch (e: Exception) {
            viewModelScope.launch {
                _event.emit(UIEvent.ShowMessage("播放失败: ${e.message}"))
            }
        }
    }

    private fun pauseAudio() {
        mediaPlayer?.pause()
        internalState.value = state.value.copy(isPlaying = false)
    }



    private fun NoteState.toNote(noteId: Int?): Note {
        return Note(
            title,
            content,
            System.currentTimeMillis(),
            color,
            noteId,
            isRecording = isRecording,
            isPlaying = isPlaying,
            audioPath = audioPath,
            audioNote = audioNote,
            duration = duration,
            isStarred = isStarred,
            category = selectedCategory
        )
    }

    /**
     * 状态
     */
    data class NoteState(
        val title: String = "",
        val content: String = "",
        val color: Int = noteColors.random().toArgb(),
        val type: NoteType = NoteType.TEXT,
        val images: List<String> = emptyList(), // 图片URI列表
        val imageCaption: String = "", // 图片描述
        val isRecording: Boolean = false, //是否录音
        val isPlaying: Boolean = false, //音频是否播放
        val audioPath: String = "", // 音频路径
        val duration: Int = 0, // 音频时长(秒)
        val audioNote: String = "", // 音频备注
        val categories: List<String> = emptyList(),
        val selectedCategory: String = "",
        val isStarred: Boolean = false
    ){
        init{
            Log.d("111", "222")
        }
    }

    /**
     * 事件
     */
    sealed interface NoteEvent {
        data class SetType(val type: NoteType) : NoteEvent
        data class ChangeTitle(val title: String) : NoteEvent
        data class ChangeContent(val content: String) : NoteEvent
        data class ChangeColor(val color: Int) : NoteEvent
        object SaveNote : NoteEvent
        data class AddImage(val uri: String) : NoteEvent
        class RemoveImage(val uri: String) : NoteEvent
        class UpdateImageCaption(val text: String) : NoteEvent
        object StartRecording : NoteEvent
        object StopRecording : NoteEvent
        object PlayAudio : NoteEvent
        object PauseAudio : NoteEvent
        data class UpdateAudioNote(val note: String) : NoteEvent
        data class AddCategory(val name: String) : NoteEvent
        data class DeleteCategory(val name: String) : NoteEvent
        data class SelectCategory(val name: String) : NoteEvent
        data class ToggleStar(val isStarred: Boolean) : NoteEvent
        data class ChangeSortOption(val option: SortOption) : NoteEvent
    }

    /**
     * UI事件
     */
    sealed interface UIEvent {
        data class ShowMessage(val message: String?) : UIEvent
        object SaveNote : UIEvent
    }

}


