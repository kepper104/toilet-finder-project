package com.kepper104.toiletseverywhere.domain.model

/**
 * TODO
 *
 */
data class LocalUser (
    var id: Int = 0,
    var isLoggedIn: Boolean = false,
    var displayName: String = "John Doe",
    var creationDate: String = "2023-01-01",
)


