package com.kepper104.toiletseverywhere.domain.model

// TODO lmao, change shall to should
/**
 * Class containing data on the user currently using the app.
 * [isLoggedIn] shall be true when class is in use.
 */
data class LocalUser (
    var id: Int = 0,
    var isLoggedIn: Boolean = false,
    var displayName: String = "John Doe",
    var creationDate: String = "2023-01-01",
)


