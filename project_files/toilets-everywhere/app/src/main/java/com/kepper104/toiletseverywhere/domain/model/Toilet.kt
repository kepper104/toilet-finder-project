package com.kepper104.toiletseverywhere.domain.model

import com.google.android.gms.maps.model.LatLng
import java.time.LocalDate
import java.time.LocalTime

/**
 * TODO
 *
 *
 */
data class Toilet(
    val id: Int = 0,
    val authorId: Int = 0,
    val coordinates: LatLng = LatLng(0.0, 0.0),
    val placeName: String = "Public Toilet",
    val isPublic: Boolean = false,
    val disabledAccess: Boolean = false,
    val babyAccess: Boolean = false,
    val parkingNearby: Boolean = false,
    val creationDate: LocalDate = LocalDate.of(2023, 1, 1),
    val openingTime: LocalTime = LocalTime.of(0, 0, 0),
    val closingTime: LocalTime = LocalTime.of(23, 59, 59),
    val cost: Int = 0,
    val authorName: String = "Unnamed Author"
)