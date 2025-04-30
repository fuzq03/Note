package com.king.easynote.data.repository

import com.king.easynote.data.datasource.CategoryDao
import com.king.easynote.domain.model.CategoryEntity
import com.king.easynote.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow


/**
 * CategoryRepository 实现
 */
class CategoryRepositoryImpl (private val categoryDao: CategoryDao) : CategoryRepository {
    override suspend fun saveCategory(category: CategoryEntity) {

        categoryDao.insertCategory(category)
    }

    override suspend fun deleteCategory(category: CategoryEntity) {
        categoryDao.deleteCategory(category)
    }

    override fun getAllCategories(): Flow<List<CategoryEntity>> {
        return categoryDao.getAllCategories()
    }
}