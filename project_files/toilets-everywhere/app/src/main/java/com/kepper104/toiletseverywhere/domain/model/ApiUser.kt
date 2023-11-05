package com.kepper104.toiletseverywhere.domain.model

/**
 * Class with data of a [User] that uses primitives, is received from the API .
 * Does not include user's login and password.
 *
 * @property id_
 * @property display_name_
 * @property creation_date_
 */
data class ApiUser (
    val id_: Int = 1,
    val display_name_: String = "Error Error",
    val creation_date_: String = "2023-01-01",

)