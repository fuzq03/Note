package com.king.easynote.data.datasource

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.king.easynote.domain.model.NoteExtraInfoEntity

@Dao
interface NoteExtraInfoDao {
    @Query("SELECT * FROM note_extra_info WHERE noteId = :noteId")
    suspend fun getNoteExtraInfo(noteId: Int): NoteExtraInfoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateNoteExtraInfo(noteExtraInfo: NoteExtraInfoEntity)
}