package com.kepper104.toiletseverywhere.presentation.ui.state

import com.kepper104.toiletseverywhere.domain.model.Review
import com.kepper104.toiletseverywhere.domain.model.Toilet

/**
 * State describing everything about current Details screen
 *
 * @property toilet
 * @property currentDetailScreen
 * @property authorName
 * @property selectedRating
 * @property currentReviewText
 * @property reviewPostConfirmationDialogOpen
 * @property reviews
 * @property allReviewsMenuOpen
 * @property reportDialogOpen
 */
data class ToiletViewDetailsState (
    val toilet: Toilet? = null,
    val currentDetailScreen: CurrentDetailsScreen = CurrentDetailsScreen.NONE,
    val authorName: String = "None",
    val selectedRating: Int = 0,
    val currentReviewText: String = "",
    val reviewPostConfirmationDialogOpen: Boolean = false,
    val reviews: List<Review> = emptyList(),
    val allReviewsMenuOpen: Boolean = false,
    val reportDialogOpen: Boolean = false,
    val reportMessage: String = ""
)

/**
 * Destination on which Details screen is currently shown
 */
enum class CurrentDetailsScreen{
    MAP, LIST, NONE
}