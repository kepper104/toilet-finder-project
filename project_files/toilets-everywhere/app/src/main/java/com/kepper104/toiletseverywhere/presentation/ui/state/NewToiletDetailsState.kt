package com.kepper104.toiletseverywhere.presentation.ui.state

import com.google.android.gms.maps.model.LatLng
import java.time.LocalDate
import java.time.LocalTime

/**
 * TODO
 *
 */
data class NewToiletDetailsState(
    val enabled: Boolean = false,
    val coordinates: LatLng = LatLng(0.0, 0.0),
    val isPublic: Boolean = true,
    val name: String = "",
    val cost: Int = 0,
    val creationDate: LocalDate = LocalDate.of(2023, 1, 1),
    val openingTime: LocalTime = LocalTime.of(6, 0, 0),
    val closingTime: LocalTime = LocalTime.of(23, 0, 0),
    val disabledAccess: Boolean = false,
    val babyAccess: Boolean = false,
    val parkingNearby: Boolean = true,

    )