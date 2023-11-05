package com.kepper104.toiletseverywhere.domain.model

// TODO update ApiToilet class to include toilet rating and stuff
/**
 * Class with data of a [Toilet] that uses primitives, is received from the API .
 * Everything, including [author_name_], [average_rating_] and [review_count_]
 * is received in a single API call.
 * (i.e. additional calls for retrieving data
 * not stored directly in the 'toilets_' table are not needed)\
 *
 * @property id_
 * @property author_id_
 * @property coordinates_
 * @property place_name_
 * @property is_public_
 * @property disabled_access_
 * @property baby_access_
 * @property parking_nearby_
 * @property creation_date_
 * @property opening_time_
 * @property closing_time_
 * @property cost_
 */
data class ApiToilet (
    val id_: Int = 0,
    val author_id_: Int = 0,
    val coordinates_: String = "(0, 0)",
    val place_name_: String = "Public Toilet",
    val is_public_: Boolean = false,
    val disabled_access_: Boolean = false,
    val baby_access_: Boolean = false,
    val parking_nearby_: Boolean = true,
    val creation_date_: String = "2023-08-25",
    val opening_time_: String = "00:00:00",
    val closing_time_: String = "23:59:59",
    val cost_: Int = 0
)