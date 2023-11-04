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
import com.kepper104.toiletseverywhere.data.api.DisplayNameUpdateData
import com.kepper104.toiletseverywhere.data.api.LoginData
import com.kepper104.toiletseverywhere.data.api.LoginResponse
import com.kepper104.toiletseverywhere.data.api.MainApi
import com.kepper104.toiletseverywhere.data.api.RegisterData
import com.kepper104.toiletseverywhere.data.fromApiToilet
import com.kepper104.toiletseverywhere.data.fromApiUser
import com.kepper104.toiletseverywhere.data.toApiToilet
import com.kepper104.toiletseverywhere.domain.model.ApiReview
import com.kepper104.toiletseverywhere.domain.model.LocalUser
import com.kepper104.toiletseverywhere.domain.model.Toilet
import com.kepper104.toiletseverywhere.domain.model.User
import com.kepper104.toiletseverywhere.presentation.ui.MapStyle
import com.kepper104.toiletseverywhere.presentation.ui.state.DarkModeStatus
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


enum class DataStoreKeys(val key: String){
    UserID("user_id"),
    IsLoggedIn("is_logged_in"),
    DisplayName("display_name"),
    CreationDate("creation_date"),
    DarkMode("dark_mode"),
    MapStyle("map_style")
}

/**
 * Main production implementation of [Repository] interface,
 * requires [mainApi] and [dataStore] to be injected by dagger hilt to get information from
 */
@OptIn(DelicateCoroutinesApi::class)
class RepositoryImplementation (
    private val mainApi: MainApi,
    private val dataStore: DataStore<Preferences>,
) : Repository{

    override var currentUser: LocalUser = LocalUser()
    override var loginStatus: LoginStatus = LoginStatus.None

    override var darkMode: Int = -1
    override var mapStyle: Int = -1


    init {
        GlobalScope.launch {
            loadCurrentUser()
            loadDarkModeDataStore()
            loadMapStyleDataStore()
        }
    }


    /**
     * TODO
     *
     * @return
     */
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

    /**
     * TODO
     *
     * @param id
     * @return
     */
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

    /**
     * TODO
     *
     * @param id
     * @return
     */
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

    /**
     * TODO
     *
     * @param toilet
     */
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

    /**
     * TODO
     *
     * @param id
     * @return
     */
    private suspend fun retrieveUsernameById(id: Int): String{
        Log.d(Tags.TempLogger.tag, "Retrieving username by id")

        // Error handling not needed since it already is in retrieveUserById
        val user = retrieveUserById(id)

        return user?.displayName ?: "Error"
    }


    /**
     * TODO
     *
     * @param login
     * @param password
     * @return
     */
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


    /**
     * TODO
     *
     * @param login
     * @param password
     */
    override suspend fun login(login: String, password: String) {
        Log.d(Tags.TempLogger.tag, "Logging in")

        loginStatus = LoginStatus.Processing

        val user = checkLogin(login, password)

        if (user != null){
            loginStatus = LoginStatus.Success
            saveAuthDataStore(user.id_, true, user.display_name_, user.creation_date_)
            Log.d(Tags.RepositoryLogger.toString(), "Login success")
        } else{
            loginStatus = LoginStatus.Fail

            Log.d(Tags.RepositoryLogger.toString(), "Login failure")
        }
    }


    /**
     * TODO
     *
     */
    override suspend fun logout() {
        Log.d(Tags.TempLogger.tag, "Logging out")

        clearAuthDataStore()
    }

    /**
     * Post a register request to the [mainApi]
     * that consists of [login], [password] and [displayName]
     */
    override suspend fun register(login: String, password: String, displayName: String) {
        Log.d(Tags.TempLogger.tag, "Registering")

        try{


            val res = mainApi.registerUser(RegisterData(login, password, displayName))

            if (res.isSuccessful) {
                Log.d(Tags.RepositoryLogger.toString(), "Register success")

                login(login, password)

            } else{
                Log.d(Tags.RepositoryLogger.toString(), "Login failure")
                // TODO login failure handling and messaging

            }
        } catch (e: Exception){
            Log.e(Tags.NetworkLogger.tag, e.message.toString())
        }
    }

    /**
     * TODO
     *
     * @param newDisplayName
     */
    override suspend fun changeDisplayName(newDisplayName: String): Boolean {
        val userID = currentUser.id
        Log.d(Tags.RepositoryLogger.tag, "Changing display name to $newDisplayName for $userID")
        try{
            val res = mainApi.changeDisplayName(DisplayNameUpdateData(userID, newDisplayName))
            if (res.isSuccessful){
                Log.d(Tags.RepositoryLogger.toString(), "Name change success")

                updateDisplayName(newDisplayName)
                return true

            } else {
                Log.d(Tags.RepositoryLogger.toString(), "Name change failure")
                return false
            }
        } catch (e: Exception) {
            Log.e(Tags.NetworkLogger.tag, e.message.toString())
            return false
        }

    }

    private suspend fun updateDisplayName(newDisplayName: String){
        currentUser = currentUser.copy(displayName = newDisplayName)
        saveAuthDataStore(displayName = newDisplayName)
    }


    /**
     * TODO
     *
     */
    override suspend fun continueWithoutLogin() {
        Log.d(Tags.TempLogger.tag, "Logging in anonymously")

        clearAuthDataStore()
        saveAuthDataStore(isLoggedIn = true)
    }

    /**
     * TODO
     *
     * @param login
     * @return
     */
    override suspend fun checkIfLoginExists(login: String): Boolean? {
        Log.d(Tags.TempLogger.tag, "Checking login availability")

        try {
            val res = mainApi.checkLogin(login)
            // TODO naming of this function is clusterfucked

            if (res.isSuccessful){
                return res.body()!!.UserExists
            }
        } catch (e: Exception) {
            Log.e(Tags.NetworkLogger.tag, e.message.toString())
        }
        return null

    }

    /**
     * TODO
     *
     * @param newDarkModeSetting
     */
    override suspend fun saveDarkModeDataStore(newDarkModeSetting: DarkModeStatus) {
        dataStore.edit {
            it[intPreferencesKey(DataStoreKeys.DarkMode.key)] = newDarkModeSetting.ordinal
            Log.d(Tags.RepositoryLogger.toString(), "Saved dark mode to $newDarkModeSetting")
        }
    }

    /**
     * TODO
     *
     */
    override suspend fun loadDarkModeDataStore(){
        val id = dataStore.data.first()[intPreferencesKey(DataStoreKeys.DarkMode.key)] ?: 0
        darkMode = id
    }

    override suspend fun saveMapStyleDataStore(newMapStyle: MapStyle) {
        dataStore.edit {
            it[intPreferencesKey(DataStoreKeys.MapStyle.key)] = newMapStyle.ordinal
            Log.d(Tags.RepositoryLogger.toString(), "Saved map style to $newMapStyle")
        }
    }

    override suspend fun loadMapStyleDataStore() {
        val id = dataStore.data.first()[intPreferencesKey(DataStoreKeys.MapStyle.key)] ?: 0
        Log.d(Tags.RepositoryLogger.tag, "Loading map style: $id")

        mapStyle = id
    }

    override suspend fun postToiletReview(rating: Int, reviewText: String?) {

    }

    override suspend fun retrieveToiletReviewsById(toiletId: Int): List<ApiReview> {
        TODO("Not yet implemented")
    }


    /**
     * TODO
     *
     * @param id
     * @param isLoggedIn
     * @param displayName
     * @param creationDate
     */
    private suspend fun saveAuthDataStore(
        id: Int? = null,
        isLoggedIn: Boolean? = null,
        displayName: String? = null,
        creationDate: String? = null)
    {
        Log.d(Tags.RepositoryLogger.toString(), "Saving auth datastore...")
        if (id != null) {
            dataStore.edit {
                it[intPreferencesKey(DataStoreKeys.UserID.key)] = id
                Log.d(Tags.RepositoryLogger.toString(), "Saved id")

            }
        }
        if (isLoggedIn != null) {
            dataStore.edit {
                it[booleanPreferencesKey(DataStoreKeys.IsLoggedIn.key)] = isLoggedIn
                Log.d(Tags.RepositoryLogger.toString(), "Saved isLoggedIn")

            }
        }
        if (displayName != null) {
            dataStore.edit {
                it[stringPreferencesKey(DataStoreKeys.DisplayName.key)] = displayName
                Log.d(Tags.RepositoryLogger.toString(), "Saved name")

            }
        }
        if (creationDate != null) {
            dataStore.edit {
                it[stringPreferencesKey(DataStoreKeys.CreationDate.key)] = creationDate
                Log.d(Tags.RepositoryLogger.toString(), "Saved date")

            }

        }

        loadCurrentUser()

    }


    /**
     * TODO
     *
     */
    private suspend fun clearAuthDataStore(){
        dataStore.edit {
            it[intPreferencesKey(DataStoreKeys.UserID.key)] = 0
        }

        dataStore.edit {
            it[booleanPreferencesKey(DataStoreKeys.IsLoggedIn.key)] = false
        }

        dataStore.edit {
            it[stringPreferencesKey(DataStoreKeys.DisplayName.key)] = NOT_LOGGED_IN_STRING
        }

        dataStore.edit {
            it[stringPreferencesKey(DataStoreKeys.CreationDate.key)] = "2023-01-01"
        }
        loadCurrentUser()

    }

    /**
     * TODO
     *
     */
    private suspend fun loadCurrentUser(){
        val id = dataStore.data.first()[intPreferencesKey(DataStoreKeys.UserID.key)] ?: 0
        val isLoggedIn = dataStore.data.first()[booleanPreferencesKey(DataStoreKeys.IsLoggedIn.key)] ?: false
        val displayName = dataStore.data.first()[stringPreferencesKey(DataStoreKeys.DisplayName.key)] ?: NOT_LOGGED_IN_STRING
        val creationDate = dataStore.data.first()[stringPreferencesKey(DataStoreKeys.CreationDate.key)] ?: "2023-01-01"

        currentUser.id = id
        currentUser.isLoggedIn = isLoggedIn
        currentUser.displayName = displayName
        currentUser.creationDate = creationDate
    }
}



