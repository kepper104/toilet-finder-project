package com.kepper104.toiletseverywhere.domain.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.kepper104.toiletseverywhere.data.APP_LAUNCH_INIT_DELAY
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
import com.kepper104.toiletseverywhere.domain.model.Toilet
import com.kepper104.toiletseverywhere.domain.model.LocalUser
import com.kepper104.toiletseverywhere.domain.model.User
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONObject

@OptIn(DelicateCoroutinesApi::class)
class RepositoryImplementation (
    private val mainApi: MainApi,
    private val dataStore: DataStore<Preferences>
) : Repository{

    override var currentUser: LocalUser = LocalUser()

    override var loginStatus: LoginStatus = LoginStatus.None

    init {
        GlobalScope.launch {
            // TODO check if navbackstackentry is built rather than waiting
            // TODO EXTRA IMPORTANT, NEED TO INCREASE DELAY ON WORSE DEVICES
            delay(APP_LAUNCH_INIT_DELAY)

            refreshCurrentUser()

        }
    }



    override suspend fun retrieveToilets(): List<Toilet>? {
        val toilets = mainApi.getToilets()
        if (toilets.isSuccessful){
            val mappedToilets = toilets.body()!!.map { apiToilet -> fromApiToilet(apiToilet) }
            val res = mappedToilets.map { toilet ->  toilet.copy(authorName = retrieveUsernameById(toilet.authorId))}
            return res
        }
        return null
    }

    override suspend fun retrieveToiletById(id: Int): Toilet? {
        val toilet = mainApi.getToiletById(id)

        if (toilet.isSuccessful) {
            return fromApiToilet(toilet.body()!!)
        }
        return null
    }

    override suspend fun retrieveUserById(id: Int): User? {
        val user = mainApi.getUserById(id)
        if (user.isSuccessful){
            return fromApiUser(user.body()!!)
        }
        Log.e(Tags.RepositoryLogger.toString(), "User by id not found")

        return null
    }

    override suspend fun createToilet(toilet: Toilet) {
        Log.d(Tags.RepositoryLogger.toString(), "Adding toilet $toilet")

        val res = mainApi.createToilet(toApiToilet(toilet))
        Log.d(Tags.RepositoryLogger.tag, res.body().toString())




    }

    private suspend fun retrieveUsernameById(id: Int): String{
        val user = retrieveUserById(id)

        return user?.displayName ?: "Error"
    }


    private suspend fun checkLogin(login: String, password: String): LoginResponse? {
        val res = mainApi.loginUser(LoginData(login, password))
        Log.d(Tags.RepositoryLogger.toString(), "$res, ${res.isSuccessful}, ${res.body()}")
        if (res.isSuccessful){
            val user = res.body()!!
            Log.d(Tags.RepositoryLogger.toString(), res.body()!!.toString())


//            return res.body()
            return user
        }
        return null
    }



    override suspend fun login(login: String, password: String) {
        loginStatus = LoginStatus.Processing
        val user = checkLogin(login, password)
        // TODO maybe change to isSuccessful check?
        if (user != null){
            loginStatus = LoginStatus.Success
            saveDataStore(user.id_, true, user.display_name_, user.creation_date_)
            Log.d(Tags.RepositoryLogger.toString(), "Login success")
        }else{
            loginStatus = LoginStatus.Fail

            Log.d(Tags.RepositoryLogger.toString(), "Login failure")
        }
    }


    override suspend fun logout() {
        clearDataStore()
    }

    override suspend fun register(login: String, password: String, displayName: String) {
        Log.d(Tags.RepositoryLogger.toString(), "Registering...")

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
        clearDataStore()
        saveDataStore(isLoggedIn = true)
    }

    override suspend fun checkIfLoginExists(login: String): Boolean? {
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



