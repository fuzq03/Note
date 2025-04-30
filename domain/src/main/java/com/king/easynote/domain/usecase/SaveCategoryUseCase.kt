package com.king.easynote.domain.usecase

import com.king.easynote.domain.model.CategoryEntity
import com.king.easynote.domain.repository.CategoryRepository

class SaveCategoryUseCase (private val categoryRepository: CategoryRepository) {
    suspend operator fun invoke(category: CategoryEntity) {
        categoryRepository.saveCategory(category)
    }
}