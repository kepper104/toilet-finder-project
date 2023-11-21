package com.kepper104.toiletseverywhere.data

import android.location.Location
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.kepper104.toiletseverywhere.domain.model.Toilet
import java.time.LocalTime
import java.time.format.DateTimeFormatter

val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
val timeFormatterWithSeconds: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
val timeFormatterWithoutSeconds: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")


/**
 * Calculate distance in whole meters from a given [userPosition] to a [toiletPosition].
 */
fun getToiletDistanceMeters(userPosition: LatLng, toiletPosition: LatLng): Int {
    val res = FloatArray(10)
    Location.distanceBetween(
        userPosition.latitude,
        userPosition.longitude,
        toiletPosition.latitude,
        toiletPosition.longitude,
        res
    )
    return res[0].toInt()
}

/**
 * Convert given [distanceMeters] number from whole meters
 * to a user-friendly [String] with specified meters or also converted to kilometers.
 */
fun getToiletDistanceString(distanceMeters: Int): String{
    return if (distanceMeters < 1000) distanceMeters.toString() + "m"
    else (distanceMeters / 1000).toString() + "km"
}

/**
 * Use [toilet]'s working hours and current time to calculate if the toilet is currently open
 * and return a [String] "Open" or "Closed"
 */
fun getToiletOpenString(toilet: Toilet, currentTime: LocalTime = LocalTime.now()): String {
    if (toilet.openingTime <= currentTime &&  currentTime <= toilet.closingTime){
        return "Open"
    }
    return "Closed"
}

/**
 * Get [toilet]'s marker color: Green or Red if toilet is currently open or closed.
 */
fun getToiletStatusColor(toilet: Toilet, currentTime: LocalTime = LocalTime.now()): BitmapDescriptor{
    return if (getToiletOpenString(toilet, currentTime) == "Open")
                ToiletIcons.ToiletGreen.icon
            else
                ToiletIcons.ToiletRed.icon

}

/**
 * Convert [toilet]'s [Toilet.cost] to a user-friendly [String]
 * containing either [toilet]'s price with a ruble sign or saying "Free" if toilet is free.
 */
fun getToiletPriceString(toilet: Toilet): String {
    return if (toilet.cost == 0)
                "Free"
            else
                "${toilet.cost}₽"
}

/**
 * Convert [toilet]'s [Toilet.placeName] to a user-friendly [String]
 * containing either [toilet]'s place or facility name or saying "Public toilet"
 * if the [toilet] is public.
 */
fun getToiletNameString(toilet: Toilet): String{
    return if (toilet.isPublic)
                "Public Toilet"
            else
                toilet.placeName
}

/**
 * Convert [toilet]'s working hours to a user-friendly working hours string [String] in form of
 * "From 00:00 to 23:59" or "00:00 - 23:59" depending on [includeFromToLabels] parameter
 */
fun getToiletWorkingHoursString(toilet: Toilet, includeFromToLabels: Boolean = false): String {
    val openingTime = toilet.openingTime.format(timeFormatterWithoutSeconds)
    val closingTime = toilet.closingTime.format(timeFormatterWithoutSeconds)

    return if (includeFromToLabels)
                "From $openingTime to $closingTime"
            else
                "$openingTime - $closingTime"
}

