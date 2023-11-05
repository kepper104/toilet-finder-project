package com.kepper104.toiletseverywhere.data

import com.google.android.gms.maps.model.LatLng
import com.kepper104.toiletseverywhere.domain.model.ApiReview
import com.kepper104.toiletseverywhere.domain.model.ApiToilet
import com.kepper104.toiletseverywhere.domain.model.ApiUser
import com.kepper104.toiletseverywhere.domain.model.Review
import com.kepper104.toiletseverywhere.domain.model.Toilet
import com.kepper104.toiletseverywhere.domain.model.User
import com.kepper104.toiletseverywhere.presentation.ui.state.ToiletMarker
import java.time.LocalDate
import java.time.LocalTime

/**
 * Converts [apiToilet] from the [ApiToilet] class
 * (which is received from API and uses primitive types)
 * to the used in the app [Toilet] class
 */
fun fromApiToilet(apiToilet: ApiToilet): Toilet {

    val values = apiToilet.coordinates_.trim('(', ')').split(',')

    val xStr = values[0].trim().toFloat()
    val yStr = values[1].trim().toFloat()

    val coords = LatLng(xStr.toDouble(), yStr.toDouble())

    val creationDate: LocalDate = LocalDate.parse(apiToilet.creation_date_, dateFormatter)

    val openingTime: LocalTime = LocalTime.parse(apiToilet.opening_time_, timeFormatterWithSeconds)
    val closingTime: LocalTime = LocalTime.parse(apiToilet.closing_time_, timeFormatterWithSeconds)

    return Toilet(
        id = apiToilet.id_,
        authorId = apiToilet.author_id_,
        coordinates = coords,
        placeName = apiToilet.place_name_,
        isPublic = apiToilet.is_public_,
        disabledAccess = apiToilet.disabled_access_,
        babyAccess = apiToilet.baby_access_,
        parkingNearby = apiToilet.parking_nearby_,
        creationDate = creationDate,
        openingTime = openingTime,
        closingTime = closingTime,
        cost = apiToilet.cost_,
        authorName = "ToBeRetrieved"
    )
}

/**
 * Converts [toilet] from the used in the app [Toilet] class
 * to the [ApiToilet] class that uses primitives and is then sent to the API.
 */
fun toApiToilet(toilet: Toilet): ApiToilet {
    val openingTime = toilet.openingTime.format(timeFormatterWithoutSeconds)
    val closingTime = toilet.closingTime.format(timeFormatterWithoutSeconds)

    return ApiToilet(
        id_ = toilet.id,
        author_id_ = toilet.authorId,
        coordinates_ = "(${toilet.coordinates.latitude}, ${toilet.coordinates.longitude})",
        place_name_ = toilet.placeName,
        is_public_ = toilet.isPublic,
        disabled_access_ = toilet.disabledAccess,
        baby_access_ = toilet.babyAccess,
        parking_nearby_ = toilet.parkingNearby,
        creation_date_ = toilet.creationDate.toString(),
        opening_time_ = openingTime,
        closing_time_ = closingTime,
        cost_ = toilet.cost,
    )
}

/**
 * Converts [toilet] from the [Toilet] class containing all information about a toilet
 * to the [ToiletMarker] class used for placing markers on the map.
 *
 * @param toilet
 * @return
 */
fun toToiletMarker(toilet: Toilet): ToiletMarker {
    return ToiletMarker(
        id = toilet.id,
        position = toilet.coordinates,
        rating = 0f, // TODO get rating from separate api request
        isPublic = toilet.isPublic,
        toilet = toilet
    )
}

/**
 * Converts [apiUser] from the [ApiUser] class
 * (which is received from API and uses primitive types)
 * to the used in the app [User] class
 */
fun fromApiUser(apiUser: ApiUser): User {
    val creationDate: LocalDate = LocalDate.parse(apiUser.creation_date_, dateFormatter)

    return User(
        id = apiUser.id_,
        displayName = apiUser.display_name_,
        creationDate = creationDate
    )
}

/**
 * Converts [apiReview] from the [ApiUser] class
 * (which is received from API and uses primitive types)
 * to the used in the app [User] class
 */
fun fromApiReview(apiReview: ApiReview): Review {
    return Review(
        id = apiReview.id_,
        toiletId = apiReview.toilet_id_,
        userId = apiReview.user_id_,
        rating = apiReview.rating_,
        review = apiReview.review_text_,
        userDisplayName = apiReview.user_display_name_
    )
}