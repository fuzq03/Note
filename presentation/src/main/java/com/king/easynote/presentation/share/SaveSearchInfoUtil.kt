package com.king.easynote.presentation.share

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.SystemClock
import com.king.easynote.presentation.search.SortOption

object SaveSearchInfoUtil {


    private val SORT_PREFS_NAME = "NoteSortPrefs"//最后时间和使用次数表名
    private val SORT_OPTION_KEY = "sort_option"

    fun getSortOption(application: Application): SortOption {
        val sharedPreferences: SharedPreferences = application.getSharedPreferences(SORT_PREFS_NAME, Context.MODE_PRIVATE)
        val optionString = sharedPreferences.getString(SORT_OPTION_KEY, SortOption.RECENTLY_USED.name)
        return SortOption.valueOf(optionString ?: SortOption.RECENTLY_USED.name)
    }

    fun saveSortOption(option: SortOption, application: Application) {
        val sharedPreferences: SharedPreferences = application.getSharedPreferences(SORT_PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(SORT_OPTION_KEY, option.name).apply()
    }

    // 读取 lastUsedTime
    fun getLastUsedTime(noteId: Int, application: Application): Long {
        val sharedPreferences: SharedPreferences = application.getSharedPreferences(SORT_PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getLong("${noteId}_lastUsedTime", 0)
    }

    // 读取 useCount
    fun getUseCount(noteId: Int, application: Application): Int {
        val sharedPreferences: SharedPreferences = application.getSharedPreferences(SORT_PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getInt("${noteId}_useCount", 0)
    }

    //保存 lastUsedTime和UseCount,传入的useCount为当前次数，保存时会自动+1
    fun saveNoteInfo(application: Application, noteId: Int, useCount: Int) {
        val sharedPreferences: SharedPreferences = application.getSharedPreferences(SORT_PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putLong("${noteId}_lastUsedTime", SystemClock.currentThreadTimeMillis())
        editor.putInt("${noteId}_useCount", useCount+1)
        editor.apply()
    }

}