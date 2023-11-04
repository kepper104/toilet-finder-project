package com.kepper104.toiletseverywhere.domain.model

data class ApiReview (
    val id_: Int = 0,
    val toilet_id_: Int = 0,
    val user_id_: Int = 0,
    val stars_: Int = 0,
    val review_: String? = null,
    val user_display_name: String = "ERROR"
)