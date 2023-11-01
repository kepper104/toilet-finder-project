package com.kepper104.toiletseverywhere.domain.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kepper104.toiletseverywhere.data.LoginStatus
import com.kepper104.toiletseverywhere.data.NOT_LOGGED_IN_STRING
import com.kepper104.toiletseverywhere.data.Tags
import com.kepper104.toiletseverywhere.data.api.LoginData
import com.kepper104.toiletseverywhere.data.api.LoginResponse
import com.kepper104.toiletseverywhere.data.api.MainApi
import com.kepper104.toiletseverywhere.data.api.RegisterData
import com.kepper104.toiletseverywhere.data.fromApiToilet
import com.kepper104.toiletseverywhere.data.fromApiUser
import com.kepper104.toiletseverywhere.data.toApiToilet
import com.kepper104.toiletseverywhere.domain.model.LocalUser
import com.kepper104.toiletseverywhere.domain.model.Toilet
import com.kepper104.toiletseverywhere.domain.model.User
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(DelicateCoroutinesApi::class)
class RepositoryImplementation (
    private val mainApi: MainApi,
    private val dataStore: DataStore<Preferences>
) : Repository{

    override var currentUser: LocalUser = LocalUser()

    override var loginStatus: LoginStatus = LoginStatus.None

    init {
        GlobalScope.launch {
            refreshCurrentUser()
        }
    }


    override suspend fun retrieveToilets(): List<Toilet>? {
        Log.d(Tags.TempLogger.tag, "Retrieving toilets")
        try{
            val toilets = mainApi.getToilets()

            if (toilets.isSuccessful){
                val mappedToilets = toilets.body()!!.map { apiToilet -> fromApiToilet(apiToilet) }
                val res = mappedToilets.map { toilet ->  toilet.copy(authorName = retrieveUsernameById(toilet.authorId))}
                return res
            }
        } catch (e: Exception){
            Log.e(Tags.NetworkLogger.tag, e.message.toString())
        }
        return null
    }

    override suspend fun retrieveToiletById(id: Int): Toilet? {
        Log.d(Tags.TempLogger.tag, "Retrieving toilet by id")

        try {
            val toilet = mainApi.getToiletById(id)

            if (toilet.isSuccessful) {
                return fromApiToilet(toilet.body()!!)
            }

        } catch (e: Exception){
            Log.e(Tags.NetworkLogger.tag, e.message.toString())
        }
        return null
    }

    override suspend fun retrieveUserById(id: Int): User? {
        Log.d(Tags.TempLogger.tag, "Retrieving user by id")

        try{
            val user = mainApi.getUserById(id)
            if (user.isSuccessful){
                return fromApiUser(user.body()!!)
            }
        } catch (e: Exception){
            Log.e(Tags.RepositoryLogger.toString(), "User by id not found")
            Log.e(Tags.NetworkLogger.tag, e.message.toString())
        }

        return null
    }

    override suspend fun createToilet(toilet: Toilet) {
        Log.d(Tags.TempLogger.tag, "Creating toilet")

        try{
            val res = mainApi.createToilet(toApiToilet(toilet))

            if (!res.isSuccessful){
                Log.d(Tags.RepositoryLogger.tag, res.body().toString())
                throw Exception("Toilet creation error!")
            }

        } catch (e: Exception){
            Log.e(Tags.NetworkLogger.tag, e.message.toString())
            throw e
        }
    }

    private suspend fun retrieveUsernameById(id: Int): String{
        Log.d(Tags.TempLogger.tag, "Retrieving username by id")

        // Error handling not needed since it already is in retrieveUserById
        val user = retrieveUserById(id)

        return user?.displayName ?: "Error"
    }


    private suspend fun checkLogin(login: String, password: String): LoginResponse? {
        Log.d(Tags.TempLogger.tag, "Checking login")

        try{
            val res = mainApi.loginUser(LoginData(login, password))
            Log.d(Tags.RepositoryLogger.toString(), "$res, ${res.isSuccessful}, ${res.body()}")

            if (res.isSuccessful){
                val user = res.body()!!
                Log.d(Tags.RepositoryLogger.toString(), res.body()!!.toString())

                return user
            }

        } catch (e: Exception){
            Log.e(Tags.NetworkLogger.tag, e.message.toString())
        }

        return null
    }



    override suspend fun login(login: String, password: String) {
        Log.d(Tags.TempLogger.tag, "Logging in")

        loginStatus = LoginStatus.Processing

        val user = checkLogin(login, password)

        if (user != null){
            loginStatus = LoginStatus.Success
            saveDataStore(user.id_, true, user.display_name_, user.creation_date_)
            Log.d(Tags.RepositoryLogger.toString(), "Login success")
        } else{
            loginStatus = LoginStatus.Fail

            Log.d(Tags.RepositoryLogger.toString(), "Login failure")
        }
    }


    override suspend fun logout() {
        Log.d(Tags.TempLogger.tag, "Logging out")

        clearDataStore()
    }

    override suspend fun register(login: String, password: String, displayName: String) {
        Log.d(Tags.TempLogger.tag, "Registering")


        val res = mainApi.registerUser(RegisterData(login, password, displayName))

        if (res.isSuccessful){
            Log.d(Tags.RepositoryLogger.toString(), "Register success")

            login(login, password)

        }else{
            Log.d(Tags.RepositoryLogger.toString(), "Login failure")
            // TODO login failure handling and messaging

        }
    }


    override suspend fun continueWithoutLogin() {
        Log.d(Tags.TempLogger.tag, "Logging in anonymously")

        clearDataStore()
        saveDataStore(isLoggedIn = true)
    }

    override suspend fun checkIfLoginExists(login: String): Boolean? {
        Log.d(Tags.TempLogger.tag, "Checking login availability")

        val res = mainApi.checkLogin(login)
        // TODO naming of this function is clusterfucked

        if (res.isSuccessful){
            return res.body()!!.UserExists
        }
        return null

    }

    private suspend fun saveDataStore(
        id: Int? = null,
        isLoggedIn: Boolean? = null,
        displayName: String? = null,
        creationDate: String? = null)
    {
        Log.d(Tags.RepositoryLogger.toString(), "Saving datastore...")
        if (id != null) {
            dataStore.edit {
                it[intPreferencesKey("id")] = id
                Log.d(Tags.RepositoryLogger.toString(), "Saved id")

            }
        }
        if (isLoggedIn != null) {
            dataStore.edit {
                it[booleanPreferencesKey("isLoggedIn")] = isLoggedIn
                Log.d(Tags.RepositoryLogger.toString(), "Saved isLoggedIn")

            }
        }
        if (displayName != null) {
            dataStore.edit {
                it[stringPreferencesKey("displayName")] = displayName
                Log.d(Tags.RepositoryLogger.toString(), "Saved name")

            }
        }
        if (creationDate != null) {
            dataStore.edit {
                it[stringPreferencesKey("creationDate")] = creationDate
                Log.d(Tags.RepositoryLogger.toString(), "Saved date")

            }

        }

        refreshCurrentUser()

    }

    private suspend fun clearDataStore(){
        dataStore.edit {
            it[intPreferencesKey("id")] = 0
        }

        dataStore.edit {
            it[booleanPreferencesKey("isLoggedIn")] = false
        }

        dataStore.edit {
            it[stringPreferencesKey("displayName")] = NOT_LOGGED_IN_STRING
        }

        dataStore.edit {
            it[stringPreferencesKey("creationDate")] = "2023-01-01"
        }
        refreshCurrentUser()

    }


    private suspend fun refreshCurrentUser(){
        val id = dataStore.data.first()[intPreferencesKey("id")] ?: 0
        val isLoggedIn = dataStore.data.first()[booleanPreferencesKey("isLoggedIn")] ?: false
        val displayName = dataStore.data.first()[stringPreferencesKey("displayName")] ?: NOT_LOGGED_IN_STRING
        val creationDate = dataStore.data.first()[stringPreferencesKey("creationDate")] ?: "2023-01-01"

        currentUser.id = id
        currentUser.isLoggedIn = isLoggedIn
        currentUser.displayName = displayName
        currentUser.creationDate = creationDate
    }
}



