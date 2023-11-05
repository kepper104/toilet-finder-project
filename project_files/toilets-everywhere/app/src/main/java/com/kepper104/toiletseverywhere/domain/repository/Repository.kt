package com.kepper104.toiletseverywhere.domain.repository

import com.kepper104.toiletseverywhere.data.LoginStatus
import com.kepper104.toiletseverywhere.domain.model.ApiReview
import com.kepper104.toiletseverywhere.domain.model.LocalUser
import com.kepper104.toiletseverywhere.domain.model.Toilet
import com.kepper104.toiletseverywhere.domain.model.User
import com.kepper104.toiletseverywhere.presentation.ui.MapStyle
import com.kepper104.toiletseverywhere.presentation.ui.state.DarkModeStatus

/**
 * TODO
 *
 */
interface Repository{

    var currentUser: LocalUser
    var loginStatus: LoginStatus
    var darkMode: Int
    var mapStyle: Int

    /**
     * TODO
     *
     * @param login
     * @param password
     */
    suspend fun login(login: String, password: String)

    /**
     * TODO
     *
     */
    suspend fun logout()

    /**
     * TODO
     *
     * @param login
     * @param password
     * @param displayName
     */

    suspend fun register(login: String, password: String, displayName: String)

    /**
     * TODO
     *
     * @param newDisplayName
     */
    suspend fun changeDisplayName(newDisplayName: String): Boolean

    /**
     * TODO
     *
     */
    suspend fun continueWithoutLogin()

    /**
     * TODO
     *
     * @param login
     * @return
     */
    suspend fun checkIfLoginExists(login: String): Boolean?

    /**
     * TODO
     *
     * @return
     */
    suspend fun retrieveToilets(): List<Toilet>?

    /**
     * TODO
     *
     * @param id
     * @return
     */
    suspend fun retrieveToiletById(id: Int): Toilet?

    /**
     * TODO
     *
     * @param id
     * @return
     */
    suspend fun retrieveUserById(id: Int): User?

    /**
     * TODO
     *
     * @param toilet
     */
    suspend fun createToilet(toilet: Toilet)

    /**
     * TODO
     *
     * @param newDarkModeSetting
     */
    suspend fun saveDarkModeDataStore(newDarkModeSetting: DarkModeStatus)

    /**
     * TODO
     *
     */
    suspend fun loadDarkModeDataStore()

    /**
     * TODO
     *
     */
    suspend fun saveMapStyleDataStore(newMapStyle: MapStyle)

    /**
     * TODO
     *
     */
    suspend fun loadMapStyleDataStore()

    suspend fun postToiletReview(rating: Int, reviewText: String?, toiletId: Int): Boolean
    suspend fun retrieveToiletReviewsById(toiletId: Int): List<ApiReview>?

}