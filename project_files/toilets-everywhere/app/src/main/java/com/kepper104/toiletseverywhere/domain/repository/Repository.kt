package com.kepper104.toiletseverywhere.domain.repository

import com.kepper104.toiletseverywhere.data.LoginStatus
import com.kepper104.toiletseverywhere.domain.model.ApiReview
import com.kepper104.toiletseverywhere.domain.model.LocalUser
import com.kepper104.toiletseverywhere.domain.model.Toilet
import com.kepper104.toiletseverywhere.domain.model.User
import com.kepper104.toiletseverywhere.presentation.ui.MapStyle
import com.kepper104.toiletseverywhere.presentation.ui.state.DarkModeStatus

/**
 * The main Repository interface to be implemented by
 * [RepositoryImplementation] for use in prod
 * or a mock [RepositoryTestImplementation] for testing.
 * Production implementation handles communicating data from persistent data sources
 * such as [DataStore] or [MainApi],
 * storing it in memory and making it accessible to [MainViewModel]
 */
interface Repository {
    var currentUser: LocalUser
    var loginStatus: LoginStatus
    var darkMode: Int
    var mapStyle: Int

    /**
     * Login a user with given [login] and [password].
     */
    suspend fun login(login: String, password: String)

    /**
     * Logout a user by erasing currently stored information on them.
     */
    suspend fun logout()

    /**
     * Register a new user with given availability verified [login], [password] and [displayName]
     */
    suspend fun register(login: String, password: String, displayName: String)

    /**
     * Login a user anonymously, making the app usable, but without certain features.
     */
    suspend fun continueWithoutLogin()

    /**
     * Check [login]'s current availability. (i.e. if another user already uses this login)
     * Returns [Boolean]? that is true if login is occupied, false if it is available
     * and null if this data if currently unavailable. (e.g. api encountered a network error).
     */
    suspend fun checkIfLoginExists(login: String): Boolean?

    /**
     * Change user's current display name to a desired [newDisplayName].
     * Returns [Boolean] if the update operation was successful.
     */
    suspend fun changeDisplayName(newDisplayName: String): Boolean

    /**
     * Retrieve all toilets (currently really all toilets, regardless of the distance to user).
     * Returns a [List] of [Toilet]s containing all stored toilets
     * or null, if this data if currently unavailable. (e.g. api encountered a network error).
     */
    suspend fun retrieveToilets(): List<Toilet>?

    /**
     * Retrieve a toilet with a given [id].
     * Returns [Toilet] if a toilet with a given [id] was found or null if not.
     */
    suspend fun retrieveToiletById(id: Int): Toilet?

    /**
     * Retrieve a user with a given [id].
     * Returns [User] if a user with a given [id] was found or null if not.
     */
    suspend fun retrieveUserById(id: Int): User?

    /**
     * Create a new toilet.
     * Requires a [Toilet] class instance with all the data of the new toilet.
     */
    suspend fun createToilet(toilet: Toilet)

    /**
     * Save user's dark mode preference to a persistent storage. Requires [newDarkModeSetting].
     */
    suspend fun saveDarkModeDataStore(newDarkModeSetting: DarkModeStatus)

    /**
     * Load user's dark mode preference from a persistent storage into repository memory.
     */
    suspend fun loadDarkModeDataStore()

    /**
     * Save user's map style preference to a persistent storage. Requires [newMapStyle].
     */
    suspend fun saveMapStyleDataStore(newMapStyle: MapStyle)

    /**
     * Load user's map style preference from a persistent storage into repository memory.
     */
    suspend fun loadMapStyleDataStore()

    /**
     * Post a new review to a persistent storage. Review's [rating], optional [reviewText] and [toiletId] are required.
     * Returns a [Boolean] indicating whether posting a review was successful.
     */
    suspend fun postToiletReview(rating: Int, reviewText: String?, toiletId: Int): Boolean

    /**
     * Retrieve all reviews of a toilet with a given [toiletId].
     * Returns [List] of [ApiReview]s if a toilet with a given [toiletId] was found or null if not.
     */
    suspend fun retrieveToiletReviewsById(toiletId: Int): List<ApiReview>?

    /**
     * Send a report about misinformation in toilet's info to be manually reviewed by admin.
     * Requires [toiletId] and report [message].
     */
    suspend fun sendToiletReport(toiletId: Int, message: String)

}