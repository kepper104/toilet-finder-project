package com.kepper104.toiletseverywhere.domain.model

data class ApiReview (
    val id_: Int = 0,
    val toilet_id_: Int = 0,
    val user_id_: Int = 0,
    val rating_: Int = 0,
    val review_text_: String? = null,
    val user_display_name_: String = "ERROR"
)