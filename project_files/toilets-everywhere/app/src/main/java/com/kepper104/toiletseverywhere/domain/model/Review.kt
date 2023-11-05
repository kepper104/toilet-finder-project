package com.kepper104.toiletseverywhere.domain.model

data class Review (
    val id: Int = 0,
    val toiletId: Int = 0,
    val userId: Int = 0,
    val rating: Int = 0,
    val review: String? = null,
    val userDisplayName: String = "ERROR"
)