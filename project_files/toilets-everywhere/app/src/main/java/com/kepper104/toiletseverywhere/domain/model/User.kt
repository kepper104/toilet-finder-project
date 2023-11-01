package com.kepper104.toiletseverywhere.domain.model

import java.time.LocalDate

/**
 * TODO
 *
 *
 */
data class User (
    val id: Int = 0,
    val displayName: String = "None",
    val creationDate: LocalDate = LocalDate.of(2023, 1, 1)
)