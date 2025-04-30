package com.king.easynote.domain.usecase

import com.king.easynote.domain.model.CategoryEntity
import com.king.easynote.domain.repository.CategoryRepository

class DeleteCategoryUseCase(private val categoryRepository: CategoryRepository) {
    suspend operator fun invoke(category: CategoryEntity) {
        categoryRepository.deleteCategory(category)
    }
}