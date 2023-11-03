package com.kepper104.toiletseverywhere.presentation.ui.state


/**
 * Stores currently selected toilet filter options
 */
data class FilterState (
    val isMenuShown: Boolean = false,
    val isPublic: Boolean = false,
    val disabledAccess: Boolean = false,
    val babyAccess: Boolean = false,
    val parkingNearby: Boolean = false,
    val currentlyOpen: Boolean = false,
    val isFree: Boolean = false,
)