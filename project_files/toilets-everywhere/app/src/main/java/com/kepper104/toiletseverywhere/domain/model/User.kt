package com.kepper104.toiletseverywhere.domain.model

import java.time.LocalDate

/**
 * Class containing data of a user.
 * The class is usually constructed from an [ApiUser] class instance using a converter.
 *
 * @property id
 * @property displayName
 * @property creationDate
 */
data class User (
    val id: Int = 0,
    val displayName: String = "None",
    val creationDate: LocalDate = LocalDate.of(2023, 1, 1)
)