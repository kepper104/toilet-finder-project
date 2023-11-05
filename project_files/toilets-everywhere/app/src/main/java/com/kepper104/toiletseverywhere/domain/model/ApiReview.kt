package com.kepper104.toiletseverywhere.domain.model

/**
 * Class with data of a [Toilet] that uses primitives, is received from the API .
 * Everything, including [user_display_name_], is received in a single API call.
 * (i.e. additional calls for retrieving data
 * not stored directly in the 'toilet_reviews_' table are not needed)
 * @property id_
 * @property toilet_id_
 * @property user_id_
 * @property rating_
 * @property review_text_
 * @property user_display_name_
 */
data class ApiReview (
    val id_: Int = 0,
    val toilet_id_: Int = 0,
    val user_id_: Int = 0,
    val rating_: Int = 0,
    val review_text_: String? = null,
    val user_display_name_: String = "ERROR"
)