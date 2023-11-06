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
import com.kepper104.toiletseverywhere.data.api.ToiletReportData
import com.kepper104.toiletseverywhere.data.api.ToiletReviewData
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

/**
 * Enum containing string [key]s for all datastore stored data.
 */
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
 * requires [mainApi] and [dataStore] to be injected by Dagger Hilt
 * to then be used as persistent data sources
 */
@OptIn(DelicateCoroutinesApi::class)
class RepositoryImplementation (
    private val mainApi: MainApi,
    private val dataStore: DataStore<Preferences>,
) : Repository {

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

    // Interface function implementations -------------
    /**
     * Login a user by making a call to [MainApi] with given [login] and [password].
     */
    override suspend fun login(login: String, password: String) {
        Log.d(Tags.RepositoryLogger.tag, "Logging in")

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
     * Logout a user by erasing currently stored information on them
     * from repository memory and datastore.
     */
    override suspend fun logout() {
        Log.d(Tags.RepositoryLogger.tag, "Logging out")

        clearAuthDataStore()
    }

    /**
     * Post a register request to the API
     * that consists of already availability verified [login], [password] and [displayName].
     */
    override suspend fun register(login: String, password: String, displayName: String) {
        Log.d(Tags.RepositoryLogger.tag, "Registering")

        try{
            val res = mainApi.registerUser(RegisterData(login, password, displayName))

            if (res.isSuccessful) {
                Log.d(Tags.RepositoryLogger.toString(), "Register success")

                login(login, password)

            } else{
                Log.d(Tags.RepositoryLogger.toString(), "Login failure")

            }
        } catch (e: Exception){
            Log.e(Tags.NetworkLogger.tag, e.message.toString())
        }
    }

    /**
     * Login a user anonymously, making the app usable, but without certain features.
     */
    override suspend fun continueWithoutLogin() {
        Log.d(Tags.RepositoryLogger.tag, "Logging in anonymously")

        clearAuthDataStore()
        saveAuthDataStore(isLoggedIn = true)
    }

    /**
     * Check [login]'s current availability by making an API call.
     * Returns [Boolean]? that is true if login is occupied, false if it is available
     * and null if this data if currently unavailable. (e.g. API encountered a network error).
     */
    override suspend fun checkIfLoginExists(login: String): Boolean? {
        Log.d(Tags.RepositoryLogger.tag, "Checking login availability")

        try {
            val res = mainApi.checkLogin(login)

            if (res.isSuccessful){
                return res.body()!!.UserExists
            }
        } catch (e: Exception) {
            Log.e(Tags.NetworkLogger.tag, e.message.toString())
        }
        return null
    }

    /**
     * Change user's current display name to a desired [newDisplayName] by making an API call.
     * Returns [Boolean] whether the update operation was successful.
     */
    override suspend fun changeDisplayName(newDisplayName: String): Boolean {
        val userID = currentUser.id
        Log.d(Tags.RepositoryLogger.tag, "Changing display name to $newDisplayName for $userID")
        try{
            val res = mainApi.changeDisplayName(DisplayNameUpdateData(userID, newDisplayName))
            return if (res.isSuccessful){
                Log.d(Tags.RepositoryLogger.toString(), "Name change success")

                updateDisplayName(newDisplayName)
                true

            } else {
                Log.d(Tags.RepositoryLogger.toString(), "Name change failure")
                false
            }
        } catch (e: Exception) {
            Log.e(Tags.NetworkLogger.tag, e.message.toString())
            return false
        }
    }


    /**
     * Retrieve all toilets from the API,
     * (currently really all toilets, regardless of the distance to user).
     * Returns a [List] of [Toilet]s containing all stored toilets
     * or null, if this data if currently unavailable. (e.g. API encountered a network error).
     */
    override suspend fun retrieveToilets(): List<Toilet>? {
        Log.d(Tags.RepositoryLogger.tag, "Retrieving toilets")
        try{
            val toilets = mainApi.getToilets()

            if (toilets.isSuccessful) {
                return toilets.body()!!.map { apiToilet -> fromApiToilet(apiToilet) }
            }
        } catch (e: Exception){
            Log.e(Tags.NetworkLogger.tag, e.message.toString())
        }
        return null
    }

    /**
     * Retrieve a toilet with a given [id] from the API.
     * Returns [Toilet] if a toilet with a given [id] was found
     * or null if not or in case of network error.
     */
    override suspend fun retrieveToiletById(id: Int): Toilet? {
        Log.d(Tags.RepositoryLogger.tag, "Retrieving toilet by id")

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
     * Retrieve a user with a given [id] from the API.
     * Returns [User] if a user with a given [id] was found
     * or null if not or in case of network error.
     */
    override suspend fun retrieveUserById(id: Int): User? {
        Log.d(Tags.RepositoryLogger.tag, "Retrieving user by id")

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
     * Create a new toilet by making an API call.
     * Requires a [Toilet] class instance with all the data of the new toilet.
     */
    override suspend fun createToilet(toilet: Toilet) {
        Log.d(Tags.RepositoryLogger.tag, "Creating toilet")

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
     * Save user's dark mode preference to [DataStore]. Requires [newDarkModeSetting].
     */
    override suspend fun saveDarkModeDataStore(newDarkModeSetting: DarkModeStatus) {
        dataStore.edit {
            it[intPreferencesKey(DataStoreKeys.DarkMode.key)] = newDarkModeSetting.ordinal
            Log.d(Tags.RepositoryLogger.toString(), "Saved dark mode to $newDarkModeSetting")
        }
    }

    /**
     * Load user's dark mode preference from [DataStore] into repository memory.
     */
    override suspend fun loadDarkModeDataStore(){
        val id = dataStore.data.first()[intPreferencesKey(DataStoreKeys.DarkMode.key)] ?: 0
        darkMode = id
    }

    /**
     * Save user's map style preference to [DataStore]. Requires [newMapStyle].
     */
    override suspend fun saveMapStyleDataStore(newMapStyle: MapStyle) {
        dataStore.edit {
            it[intPreferencesKey(DataStoreKeys.MapStyle.key)] = newMapStyle.ordinal
            Log.d(Tags.RepositoryLogger.toString(), "Saved map style to $newMapStyle")
        }
    }

    /**
     * Load user's map style preference from [DataStore] into repository memory.
     */
    override suspend fun loadMapStyleDataStore() {
        val id = dataStore.data.first()[intPreferencesKey(DataStoreKeys.MapStyle.key)] ?: 0
        Log.d(Tags.RepositoryLogger.tag, "Loading map style: $id")

        mapStyle = id
    }

    /**
     * Post a new review to the API.
     * Review's [rating], optional [reviewText] and [toiletId] are required.
     * Returns a [Boolean] indicating whether posting a review was successful.
     */
    override suspend fun postToiletReview(rating: Int, reviewText: String?, toiletId: Int): Boolean {
        Log.d(Tags.RepositoryLogger.tag, "Posting review")

        try {
            val res = mainApi.postToiletReview(ToiletReviewData(toiletId, currentUser.id, rating, reviewText))

            if (res.isSuccessful) {
                Log.d(Tags.NetworkLogger.tag, "Review post successfull")

                return true
            }
        } catch (e: Exception) {
            Log.e(Tags.NetworkLogger.tag, e.message.toString())

        }
        return false
    }

    /**
     * Retrieve all reviews of a toilet with a given [toiletId] from the API.
     * Returns [List] of [ApiReview]s if a toilet with a given [toiletId] was found or null if not.
     */
    override suspend fun retrieveToiletReviewsById(toiletId: Int): List<ApiReview>? {
        Log.d(Tags.RepositoryLogger.tag, "Getting reviews by id")
        try {
            val res = mainApi.getReviewsById(toiletId)

            if (res.isSuccessful){
                for (apiReview in res.body()!!) {
                    Log.d(Tags.RepositoryLogger.tag, apiReview.toString())

                }
                return res.body()!!
            }
        } catch (e: Exception) {
            Log.e(Tags.NetworkLogger.tag, e.message.toString())

        }
        return null
    }
    /**
     * Send an API report about misinformation in toilet's info to be manually reviewed by admin.
     * Requires [toiletId] and report [message].
     * Does not have any retries or network error indication except for Log.e
     */
    override suspend fun sendToiletReport(toiletId: Int, message: String) {
        try{
            mainApi.sendToiletReport(ToiletReportData(currentUser.id, toiletId, message))
        } catch (e: Exception) {
            Log.e(Tags.NetworkLogger.tag, e.toString())
        }
    }

    // Private functions ------------------------------

    /**
     * Update display name that is currently in repository memory to [newDisplayName]
     * and also store it to persistent [DataStore].
     * To be called only after successfully saving the [newDisplayName] serverside!
     */
    private suspend fun updateDisplayName(newDisplayName: String){
        currentUser = currentUser.copy(displayName = newDisplayName)
        saveAuthDataStore(displayName = newDisplayName)
    }


    /**
     * A wrapper for the [retrieveUserById]
     * that only returns user's display name or "Error" in case of some error.
     */
    private suspend fun retrieveUsernameById(id: Int): String{
        Log.d(Tags.RepositoryLogger.tag, "Retrieving username by id")

        // Error handling not needed since it already is in retrieveUserById
        val user = retrieveUserById(id)

        return user?.displayName ?: "Error"
    }



    /**
     * Save new [LocalUser] data to [DataStore] and update repository memory.
     * Allows only certain variables to be updated while leaving others same.
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
     * Clear currently saved [LocalUser] from [DataStore] and from repository memory.
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
     * Load current [LocalUser] from [DataStore] to repository memory.
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

    /**
     * A part of the [login] function that is responsible for making an API call
     * and returning received [LoginResponse] (or null in case of an error)
     * to then be saved in repository memory.
     */
    private suspend fun checkLogin(login: String, password: String): LoginResponse? {
        Log.d(Tags.RepositoryLogger.tag, "Checking login")

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
}



