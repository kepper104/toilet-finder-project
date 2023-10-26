package com.kepper104.toiletseverywhere.presentation.ui.state

import com.kepper104.toiletseverywhere.domain.model.Toilet

data class ToiletViewDetailsState (
    val toilet: Toilet? = null,
    val currentDetailScreen: CurrentDetailsScreen = CurrentDetailsScreen.NONE,
    val authorName: String = "None"
)

enum class CurrentDetailsScreen{
    MAP, LIST, NONE
}