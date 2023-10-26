package com.kepper104.toiletseverywhere.domain.repository

import com.kepper104.toiletseverywhere.data.LoginStatus
import com.kepper104.toiletseverywhere.domain.model.Toilet
import com.kepper104.toiletseverywhere.domain.model.LocalUser
import com.kepper104.toiletseverywhere.domain.model.User

interface Repository{
    suspend fun login(login: String, password: String)
    suspend fun logout()

    suspend fun register(login: String, password: String, displayName: String)

    suspend fun continueWithoutLogin()

    suspend fun checkIfLoginExists(login: String): Boolean?

    var currentUser: LocalUser

    var loginStatus: LoginStatus

    suspend fun retrieveToilets(): List<Toilet>?

    suspend fun retrieveToiletById(id: Int): Toilet?

    suspend fun retrieveUserById(id: Int): User?

    suspend fun createToilet(toilet: Toilet)

}