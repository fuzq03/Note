package com.king.easynote.domain.model

sealed class BaseNote{
    abstract val id: Int?
    abstract val title: String
    abstract val timestamp: Long
    abstract val color: Int
}
