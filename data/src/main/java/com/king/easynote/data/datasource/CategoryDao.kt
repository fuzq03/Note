package com.king.easynote.data.datasource

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.king.easynote.domain.model.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    /**
     * 查询所有分类
     */
    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    /**
     * 插入新分类
     */
    @Insert
    suspend fun insertCategory(category: CategoryEntity)

    /**
     * 删除分类
     */
    @Delete
    suspend fun deleteCategory(category: CategoryEntity)
}