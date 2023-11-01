package com.kepper104.toiletseverywhere.domain.repository

import com.kepper104.toiletseverywhere.data.LoginStatus
import com.kepper104.toiletseverywhere.domain.model.LocalUser
import com.kepper104.toiletseverywhere.domain.model.Toilet
import com.kepper104.toiletseverywhere.domain.model.User

/**
 * TODO
 *
 */
interface Repository{

    var currentUser: LocalUser

    var loginStatus: LoginStatus

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

}