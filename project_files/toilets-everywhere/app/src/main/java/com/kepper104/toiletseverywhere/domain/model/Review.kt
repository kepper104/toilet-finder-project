package com.kepper104.toiletseverywhere.domain.model

/**
 * Class containing data of a toilet review.
 * The class is usually constructed from an [ApiReview] class instance using a converter.
 *
 * @property id
 * @property toiletId
 * @property userId
 * @property rating
 * @property review
 * @property userDisplayName
 */
data class Review (
    val id: Int = 0,
    val toiletId: Int = 0,
    val userId: Int = 0,
    val rating: Int = 0,
    val review: String? = null,
    val userDisplayName: String = "ERROR"
)