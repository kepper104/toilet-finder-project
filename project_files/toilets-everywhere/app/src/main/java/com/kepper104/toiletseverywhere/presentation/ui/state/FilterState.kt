package com.kepper104.toiletseverywhere.presentation.ui.state

import com.kepper104.toiletseverywhere.domain.model.Toilet

/**
 * TODO
 *
 */
data class FilterState (
    val filteredToilets: List<Toilet> = emptyList(),
    val isMenuShown: Boolean = false,
    val isPublic: Boolean = false,
    val disabledAccess: Boolean = false,
    val babyAccess: Boolean = false,
    val parkingNearby: Boolean = false,
    val currentlyOpen: Boolean = false,
    val isFree: Boolean = false,
)