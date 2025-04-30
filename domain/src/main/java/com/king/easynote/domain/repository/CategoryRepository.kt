package com.king.easynote.domain.repository

import com.king.easynote.domain.model.CategoryEntity
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    suspend fun saveCategory(category: CategoryEntity)

    suspend fun deleteCategory(category: CategoryEntity)

    fun getAllCategories(): Flow<List<CategoryEntity>>
}