package com.kepper104.toiletseverywhere.presentation.ui.state

import com.kepper104.toiletseverywhere.data.BottomBarDestination

/**
 * TODO
 *
 * @property currentDestination
 */
data class NavigationState (
    val currentDestination: BottomBarDestination? = null,
)