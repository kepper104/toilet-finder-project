package com.kepper104.toiletseverywhere.presentation.ui.state

import com.kepper104.toiletseverywhere.domain.model.Review
import com.kepper104.toiletseverywhere.domain.model.Toilet

/**
 * TODO
 *
 */
data class ToiletViewDetailsState (
    val toilet: Toilet? = null,
    val currentDetailScreen: CurrentDetailsScreen = CurrentDetailsScreen.NONE,
    val authorName: String = "None",
    val selectedRating: Int = 5,
    val currentReviewText: String = "",
    val reviewPostConfirmationDialogOpen: Boolean = false,
    val reviews: List<Review> = emptyList(),
    val allReviewsMenuOpen: Boolean = false
)

/**
 * TODO
 *
 */
enum class CurrentDetailsScreen{
    MAP, LIST, NONE
}