package com.kepper104.toiletseverywhere.presentation.ui.state

import com.kepper104.toiletseverywhere.domain.model.Toilet

/**
 * TODO
 *
 * @property toiletList
 */
data class ToiletsState (
    val toiletList: List<Toilet> = emptyList()
)