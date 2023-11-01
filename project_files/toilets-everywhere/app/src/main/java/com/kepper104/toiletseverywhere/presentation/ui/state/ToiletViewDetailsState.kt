package com.kepper104.toiletseverywhere.presentation.ui.state

import com.kepper104.toiletseverywhere.domain.model.Toilet

/**
 * TODO
 *
 * @property toilet
 * @property currentDetailScreen
 * @property authorName
 */
data class ToiletViewDetailsState (
    val toilet: Toilet? = null,
    val currentDetailScreen: CurrentDetailsScreen = CurrentDetailsScreen.NONE,
    val authorName: String = "None"
)

/**
 * TODO
 *
 */
enum class CurrentDetailsScreen{
    MAP, LIST, NONE
}