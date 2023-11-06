package com.kepper104.toiletseverywhere.presentation

import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.CameraPositionState
import com.kepper104.toiletseverywhere.data.AuthUiStatus
import com.kepper104.toiletseverywhere.data.BottomBarDestination
import com.kepper104.toiletseverywhere.data.LoginStatus
import com.kepper104.toiletseverywhere.data.MIN_PASSWORD_LENGTH
import com.kepper104.toiletseverywhere.data.NavigationEvent
import com.kepper104.toiletseverywhere.data.RegistrationError
import com.kepper104.toiletseverywhere.data.ScreenEvent
import com.kepper104.toiletseverywhere.data.Tags
import com.kepper104.toiletseverywhere.data.fromApiReview
import com.kepper104.toiletseverywhere.data.getToiletDistanceMeters
import com.kepper104.toiletseverywhere.data.getToiletOpenString
import com.kepper104.toiletseverywhere.data.toToiletMarker
import com.kepper104.toiletseverywhere.domain.model.Toilet
import com.kepper104.toiletseverywhere.domain.repository.Repository
import com.kepper104.toiletseverywhere.presentation.ui.MapStyle
import com.kepper104.toiletseverywhere.presentation.ui.state.AuthState
import com.kepper104.toiletseverywhere.presentation.ui.state.CurrentDetailsScreen
import com.kepper104.toiletseverywhere.presentation.ui.state.FilterState
import com.kepper104.toiletseverywhere.presentation.ui.state.LoggedInUserState
import com.kepper104.toiletseverywhere.presentation.ui.state.MapState
import com.kepper104.toiletseverywhere.presentation.ui.state.NavigationState
import com.kepper104.toiletseverywhere.presentation.ui.state.NewToiletDetailsState
import com.kepper104.toiletseverywhere.presentation.ui.state.DarkModeStatus
import com.kepper104.toiletseverywhere.presentation.ui.state.SettingsState
import com.kepper104.toiletseverywhere.presentation.ui.state.ToiletViewDetailsState
import com.kepper104.toiletseverywhere.presentation.ui.state.ToiletsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.math.RoundingMode
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject


/**
 * The only view-model in project, handles all app business logic
 * and communicating data between Repository and UI
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    var toiletsState by mutableStateOf(ToiletsState())
    var mapState by mutableStateOf(MapState())
    var authState by mutableStateOf(AuthState())
    var toiletViewDetailsState by mutableStateOf(ToiletViewDetailsState())
    var navigationState by mutableStateOf(NavigationState())
    var loggedInUserState by mutableStateOf(LoggedInUserState())
    var newToiletDetailsState by mutableStateOf(NewToiletDetailsState())
    var filterState by mutableStateOf(FilterState())
    var settingsState by mutableStateOf(SettingsState())


    private val _screenEventFlow = MutableSharedFlow<ScreenEvent>()
    val screenEventFlow = _screenEventFlow.asSharedFlow()

    private val _navigationEventFlow = MutableSharedFlow<NavigationEvent>()
    val navigationEventFlow = _navigationEventFlow.asSharedFlow()

    private val _messageEventFlow = MutableSharedFlow<String>()
    val messageEventFlow = _messageEventFlow.asSharedFlow()


    private lateinit var locationClient: FusedLocationProviderClient

    lateinit var scaffoldPadding: PaddingValues


    private var prevLoggedInValue = false

    var isLoggedInFlow = flow {
        emit(null)

        while(true){
            if (prevLoggedInValue != repository.currentUser.isLoggedIn){
                saveLoggedInUser(repository.currentUser.isLoggedIn, repository.currentUser.displayName)
                Log.d(Tags.MainViewModelTag.toString(), "Status changed! $prevLoggedInValue -> ${repository.currentUser.isLoggedIn}")
                emit(repository.currentUser.isLoggedIn)
                prevLoggedInValue = repository.currentUser.isLoggedIn
            }
            delay(200L)
        }
    }



    private var prevLoginStatus: LoginStatus = LoginStatus.None

    private var loginStatusFlow = flow {
        emit(LoginStatus.None)

        while(true){
            if (prevLoginStatus != repository.loginStatus){
                Log.d(Tags.MainViewModelTag.toString(), "Status changed! $prevLoginStatus -> ${repository.loginStatus}")
                emit(repository.loginStatus)
                prevLoginStatus = repository.loginStatus
            }
            delay(200L)
        }
    }

    private var darkModeStatusFlow = flow {
        while (repository.darkMode == -1){
            Log.d("Waiting", "dark mode still -1")

            delay(100L)
        }
        emit(repository.darkMode)
    }

    private var mapStyleStatusFlow = flow {
        while (repository.mapStyle == -1){
            Log.d("Waiting", "map style still -1")
            delay(100L)
        }
        emit(repository.mapStyle)
    }

    init {
        collectLoginStatusFlow()
        collectDarkModeStatusFlow()
        collectMapStyleStatusFlow()
        getLatestToilets()
    }



    // Generic dialog opening and closing functions
    fun closeReportDialog(){
        toiletViewDetailsState = toiletViewDetailsState.copy(reportDialogOpen = false)
    }
    fun openReportDialog(){
        toiletViewDetailsState = toiletViewDetailsState.copy(reportDialogOpen = true)
    }
    fun openNewToiletConfirmationDialog(){
        newToiletDetailsState = newToiletDetailsState.copy(newToiletConfirmationDialogOpen = true)
    }

    fun closeNewToiletConfirmationDialog(){
        newToiletDetailsState = newToiletDetailsState.copy(newToiletConfirmationDialogOpen = false)
    }

    fun openAllReviews(){
        toiletViewDetailsState = toiletViewDetailsState.copy(allReviewsMenuOpen = true)
    }

    fun closeAllReviews(){
        toiletViewDetailsState = toiletViewDetailsState.copy(allReviewsMenuOpen = false)
    }

    fun showReviewConfirmationDialog(){
        toiletViewDetailsState = toiletViewDetailsState.copy(reviewPostConfirmationDialogOpen = true)
    }

    fun closeReviewConfirmationDialog(){
        toiletViewDetailsState = toiletViewDetailsState.copy(reviewPostConfirmationDialogOpen = false)
    }

    fun closeNameChangeDialog() {
        settingsState = settingsState.copy(nameChangeDialogOpen = false, newDisplayName = "")
    }

    fun showNameChangeDialog() {
        settingsState = settingsState.copy(nameChangeDialogOpen = true)
    }

    fun toggleMapStyleSelectionMenu() {
        settingsState = settingsState.copy(mapStyleSelectionExpanded = !settingsState.mapStyleSelectionExpanded)
    }

    fun closeMapStyleSelectionMenu() {
        settingsState = settingsState.copy(mapStyleSelectionExpanded = false)
    }

    /**
     * Toggle the toilet filter menu dropdown. (So open if closed and close if open).
     */
    fun toggleToiletFilterMenu(){
        filterState = filterState.copy(isMenuShown = !filterState.isMenuShown)
    }

    /**
     * Close the toilet filter menu dropdown.
     */
    private fun closeToiletFilterMenu(){
        filterState = filterState.copy(isMenuShown = false)
    }

    /**
     * Post a toilet review to the Repository
     */
    fun postToiletReview(){
        viewModelScope.launch {
            toiletViewDetailsState = toiletViewDetailsState.copy(currentReviewText = toiletViewDetailsState.currentReviewText.trim())
            val postRes = repository.postToiletReview(
                toiletViewDetailsState.selectedRating,
                if (toiletViewDetailsState.currentReviewText == "") null else toiletViewDetailsState.currentReviewText,
                toiletViewDetailsState.toilet!!.id
            )

            toiletViewDetailsState = toiletViewDetailsState.copy(selectedRating = 0, currentReviewText = "")

            if (postRes){
                val toiletId = toiletViewDetailsState.toilet!!.id
                val currentDetailsScreen = toiletViewDetailsState.currentDetailScreen

                redownloadToilet(toiletId, currentDetailsScreen)

                leaveToiletViewDetailsScreen()
            } else {
                _screenEventFlow.emit(ScreenEvent.ReviewPostFailToast)
            }
        }
    }

    /**
     * Delete old info and download latest of a toilet with [toiletId]
     * and refresh it using [currentDetailsScreen] to reopen details screen
     */
    private fun redownloadToilet(toiletId: Int, currentDetailsScreen: CurrentDetailsScreen){
        val oldToiletList = toiletsState.toiletList.toMutableList()
        var oldToilet = Toilet()

        for (i in 0..toiletsState.toiletList.size){
            if (toiletsState.toiletList[i].id == toiletId){
                oldToilet = oldToiletList.removeAt(i)
                break
            }
        }
        viewModelScope.launch {
            val redownloadedToilet = repository.retrieveToiletById(toiletId)

            if (redownloadedToilet != null){
                oldToiletList.add(0, redownloadedToilet)
            } else {
                oldToiletList.add(0, oldToilet)
            }

            toiletsState = toiletsState.copy(toiletList = oldToiletList.toList())

            applyToiletFilters()

            navigateToDetails(toiletsState.toiletList[0], currentDetailsScreen)
        }
    }

    /**
     * Send a report about currently opened toilet's misinformation.
     */
    fun sendToiletReport(){
        viewModelScope.launch {
            repository.sendToiletReport(toiletViewDetailsState.toilet!!.id, toiletViewDetailsState.reportMessage)

            _screenEventFlow.emit(ScreenEvent.ReportSentToast)
        }
    }


    /**
     * Change user's display name in the repo and refresh it and notify user
     */
    fun changeDisplayName() {
        Log.d(Tags.MainViewModelTag.tag, "Changing name!")
        viewModelScope.launch {
            val res = repository.changeDisplayName(settingsState.newDisplayName.trim())

            if (res) {
                Log.d(Tags.MainViewModelTag.tag, "Changing name success")

                _screenEventFlow.emit(ScreenEvent.NameChangeSuccessToast)

                loggedInUserState = loggedInUserState.copy(currentUserName = repository.currentUser.displayName)
            } else {
                Log.d(Tags.MainViewModelTag.tag, "Changing name fail")

                _screenEventFlow.emit(ScreenEvent.NameChangeFailToast)
            }
            closeNameChangeDialog()

        }
    }


    fun changeSelectedMapStyle(newMapStyle: MapStyle){

        settingsState = settingsState.copy(selectedMapStyle = newMapStyle)

        Log.d(Tags.MainViewModelTag.tag, "Saving new map style: $newMapStyle")
        mapState = mapState.copy(properties =
            mapState.properties.copy(mapStyleOptions =
                                    if (settingsState.selectedMapStyle.json == null)
                                        null
                                    else
                                        MapStyleOptions(settingsState.selectedMapStyle.json!!)
            )
        )

        Log.d(Tags.MainViewModelTag.tag, mapState.properties.mapStyleOptions.toString())
        viewModelScope.launch {
            repository.saveMapStyleDataStore(newMapStyle)
        }
    }

    /**
     * Change currently saved dark mode preference to [newDarkModeSetting].
     */
    fun changeSelectedDarkModeSetting(newDarkModeSetting: DarkModeStatus){
        settingsState = settingsState.copy(selectedDarkModeOption = newDarkModeSetting)
        viewModelScope.launch {
            repository.saveDarkModeDataStore(newDarkModeSetting)
        }
    }



    /**
     * Saves [newFilterState] into internal [filterState] to then be used for applying filters.
     */
    fun updateToiletFilters(newFilterState: FilterState){
        filterState = newFilterState
    }

    /**
     * Gets current [filterState] properties, filters all currently cached toilets,
     * saves toilets into [filterState] to then be turned into markers or list items,
     * calls [refreshToiletMarkers] and closes the filter menu dropdown.
     */
    fun applyToiletFilters(){
        val isPublic = filterState.isPublic
        val disabledAccess = filterState.disabledAccess
        val babyAccess = filterState.babyAccess
        val parkingNearby = filterState.parkingNearby
        val currentlyOpen = filterState.currentlyOpen
        val isFree = filterState.isFree

        val filteredToiletList = toiletsState.toiletList.filter {
            (if (isPublic) it.isPublic else true) &&
            (if (disabledAccess) it.disabledAccess else true) &&
            (if (babyAccess) it.babyAccess else true) &&
            (if (parkingNearby) it.parkingNearby else true) &&
            (if (isFree) it.cost == 0 else true) &&
            (if (currentlyOpen) getToiletOpenString(it) == "Open" else true)
        }


        toiletsState = toiletsState.copy(filteredToiletList = filteredToiletList)

        sortToiletsByDistance()
        refreshToiletMarkers()
        closeToiletFilterMenu()

        if (toiletsState.filteredToiletList.isEmpty() && navigationState.currentDestination == BottomBarDestination.MapView){
            viewModelScope.launch {
                _screenEventFlow.emit(ScreenEvent.FiltersMatchNoToiletsToast)
            }
        }
    }

    private fun sortToiletsByDistance(){
        toiletsState = toiletsState.copy(filteredToiletList = toiletsState.filteredToiletList.sortedBy { getToiletDistanceMeters(mapState.userPosition, it.coordinates) })
    }

    /**
     * Resets all [filterState] properties to defaults (all false) and calls [applyToiletFilters]
     */
    fun resetToiletFilters(){
        filterState = filterState.copy(
            isPublic = false,
            disabledAccess = false,
            babyAccess = false,
            parkingNearby = false,
            currentlyOpen = false,
            isFree = false
        )
        applyToiletFilters()
    }

    /**
     * Open map view and move and zoom camera to selected [toilet] location
     */
    fun moveCameraToToiletLocation(toilet: Toilet){
        mapState = mapState.copy(cameraPosition = CameraPositionState(CameraPosition(toilet.coordinates, 17F, 0F, 0F)))
        leaveToiletViewDetailsScreen()
        changeNavigationState(BottomBarDestination.MapView)
        viewModelScope.launch {
            _navigationEventFlow.emit(NavigationEvent.NavigateToMap)
        }
    }

    /**
     * Create a new toilet from input data, publish it to repository,
     * clear input fields, show a toast with creation result
     */
    fun createToilet(){
        val toilet = Toilet(
            id = 0,
            authorId = repository.currentUser.id,
            coordinates = newToiletDetailsState.coordinates,
            placeName = if (!newToiletDetailsState.isPublic) newToiletDetailsState.name else "Public Toilet",
            isPublic = newToiletDetailsState.isPublic,
            disabledAccess = newToiletDetailsState.disabledAccess,
            babyAccess = newToiletDetailsState.babyAccess,
            parkingNearby = newToiletDetailsState.parkingNearby,
            creationDate = LocalDate.now(),
            openingTime = newToiletDetailsState.openingTime,
            closingTime = newToiletDetailsState.closingTime,
            cost = newToiletDetailsState.cost,
            authorName = repository.currentUser.displayName
        )

        viewModelScope.launch {
            try{
                repository.createToilet(toilet)
                newToiletDetailsState = newToiletDetailsState.copy(enabled = false, name = "", isPublic = true, cost = 0, openingTime = LocalTime.of(6, 0), closingTime = LocalTime.of(23, 0), disabledAccess = false, babyAccess = false, parkingNearby = false)

                triggerEvent(ScreenEvent.ToiletCreationSuccessToast)
                delay(2000)
                getLatestToilets()
                delay(1000)
                refreshToiletMarkers()

            } catch (e: Exception){
                triggerEvent(ScreenEvent.ToiletCreationFailToast)
            } finally {

            }
        }
    }


    /**
     * Emit a given [event] to eventFlow to then be collected and handled from UI
     */
    fun triggerEvent(event: ScreenEvent){
        viewModelScope.launch {
            _screenEventFlow.emit(event)
        }
    }


    /**
     * Enable showing user location on the map using given [locationProviderClient]
     */
    fun enableLocationServices(locationProviderClient: FusedLocationProviderClient){
        try{
            mapState = mapState.copy(
                properties = mapState.properties.copy(isMyLocationEnabled = true)
            )
            locationClient = locationProviderClient
            viewModelScope.launch {
                try{
                    Log.d(Tags.MainViewModelTag.toString(), "Getting position on LocationServices enable")
                    val res = locationClient.lastLocation
                    res.addOnSuccessListener {

                        try {
                            mapState = mapState.copy(
                                cameraPosition = CameraPositionState(CameraPosition(LatLng(res.result.latitude, res.result.longitude), 15F, 0F, 0F)),
                                userPosition = LatLng(res.result.latitude, res.result.longitude)
                            )
                            Log.d(Tags.MainViewModelTag.toString(), "Successfully got position on LocationServices enable")
                        } catch (e: Exception) {
                            Log.e(Tags.MainViewModelTag.toString(),  e.toString())
                        }
                    }
                    res.addOnFailureListener{
                        Log.e(Tags.MainViewModelTag.toString(),  it.toString())
                    }
                } catch (e: SecurityException){
                    Log.e(Tags.MainViewModelTag.toString(), "Location permission not granted")
                }

            }
        } catch (e: Exception){
            viewModelScope.launch {
                _messageEventFlow.emit(e.toString())
            }
        }

    }

    /**
     * Enable refreshing cached user position every 5 seconds
     */
    fun startLocationRefreshCycle() {
        viewModelScope.launch {
            while (true) {
                refreshUserLocation()
                delay(5000)
            }
        }
    }

    /**
     * Refresh currently remembered user location
     */
    private fun refreshUserLocation(){
        try{
            Log.d(Tags.MainViewModelTag.toString(), "Getting position on location refresh")
            val res = locationClient.lastLocation
            res.addOnSuccessListener {
                mapState = mapState.copy(userPosition = LatLng(res.result.latitude, res.result.longitude))

                Log.d(Tags.MainViewModelTag.toString(), "Successfully got position on LocationServices enable")
            }
            res.addOnFailureListener{
                Log.e(Tags.MainViewModelTag.toString(),  it.toString())
            }

        } catch (e: SecurityException){
            Log.e(Tags.MainViewModelTag.toString(), "Location permission not granted")
        }
    }

    /**
     * Set internal current navbar destination to [newDestination]
     */
    fun changeNavigationState(newDestination: BottomBarDestination){
        navigationState = navigationState.copy(currentDestination = newDestination)

        if (!mapState.addingToilet) return

        if (newDestination != BottomBarDestination.MapView){
            mapState = mapState.copy(addingToilet = false)
            triggerEvent(ScreenEvent.ToiletAddingDisabledToast)
        }
    }


    /**
     * Start listening to loginFlow from repository and modify UI login fields accordingly
     */
    private fun collectLoginStatusFlow(){
        viewModelScope.launch {
            loginStatusFlow.collect {loginStatus ->
                when(loginStatus){
                    LoginStatus.None -> {}
                    LoginStatus.Success -> {
                        authState = authState.copy(showInvalidPasswordPrompt = false)
                    }
                    LoginStatus.Fail -> {
                        authState = authState.copy(showInvalidPasswordPrompt = true)
                        authState = authState.copy(
                            loginLogin = "",
                            loginPassword = ""
                        )
                    }
                    LoginStatus.Processing -> {
                        Log.d(Tags.MainViewModelTag.toString(), "Logging in...")
                    }
                }
            }
        }
    }

    /**
     * Start collecting and observing new dark mode preference settings from the Repository
     *
     */
    private fun collectDarkModeStatusFlow(){
        viewModelScope.launch {
            darkModeStatusFlow.collect {darkModeStatusID ->
                Log.d(Tags.MainViewModelTag.tag, "New dark mode status: $darkModeStatusID")
                changeSelectedDarkModeSetting(DarkModeStatus.values()[darkModeStatusID])
            }
        }
    }

    /**
     * Start collecting and observing new map style preference settings from the Repository
     *
     */
    private fun collectMapStyleStatusFlow(){
        viewModelScope.launch {
            mapStyleStatusFlow.collect {mapStyleID ->
                Log.d(Tags.MainViewModelTag.tag, "New map style status: $mapStyleID")
                changeSelectedMapStyle(MapStyle.values()[mapStyleID])
            }
        }
    }

    /**
     * Write if user is currently [isLoggedIn] and their [newUsername] into loggedInUserState
     */
    private fun saveLoggedInUser(isLoggedIn: Boolean, newUsername: String){
        Log.d(Tags.MainViewModelTag.toString(), "Saving user data to vm: $isLoggedIn, $newUsername")
        loggedInUserState = loggedInUserState.copy(
            isLoggedIn = isLoggedIn,
            currentUserName = newUsername
        )
    }

    /**
     * Request all available toilets from repository,
     * saves them into [toiletsState] and calls [applyToiletFilters]
     */
    fun getLatestToilets(){
        Log.d(Tags.MainViewModelTag.toString(), "Getting latest toilets")

        viewModelScope.launch {
            val toilets = repository.retrieveToilets()
            Log.d(Tags.MainViewModelTag.toString(), "Got api result")


            if (toilets == null){
                Log.e(Tags.MainViewModelTag.toString(), "Error getting toilets")
                return@launch
            }

            toiletsState = toiletsState.copy(toiletList = toilets)

            Log.d(Tags.MainViewModelTag.toString(), "Saved toilets")

            applyToiletFilters()
        }
    }

    /**
     * Gets [toilet]'s author's name from id and saves selected [toilet] and [source]
     * (Screen from which the details menu was opened) into [toiletViewDetailsState]
     */
    fun navigateToDetails(toilet: Toilet, source: CurrentDetailsScreen){
        viewModelScope.launch {
            Log.d(Tags.MainViewModelTag.toString(), "Opening details: ${toilet.id}, $source")

            val apiReviews = repository.retrieveToiletReviewsById(toilet.id) ?: emptyList()

            val reviews = (apiReviews.map { apiReview -> fromApiReview(apiReview) }).reversed()

            toiletViewDetailsState = toiletViewDetailsState.copy(
                toilet = toilet,
                currentDetailScreen = source,
                authorName = toilet.authorName,
                reviews = reviews
            )
        }
    }

    fun leaveToiletViewDetailsScreen(){
        Log.d(Tags.MainViewModelTag.toString(), "Leaving details screen")
        toiletViewDetailsState = toiletViewDetailsState.copy(toilet = null, currentDetailScreen = CurrentDetailsScreen.NONE)
        Log.d(Tags.MainViewModelTag.toString(), "Left details: $toiletViewDetailsState")
    }

    fun leaveNewToiletDetailsScreen(){
        newToiletDetailsState = newToiletDetailsState.copy(
            enabled = false
        )
    }

    fun navigateToNewToiletDetailsScreen(){
        Log.d(Tags.MainViewModelTag.toString(), mapState.newToiletMarkerState.toString())
        newToiletDetailsState = newToiletDetailsState.copy(
            enabled = true,
            coordinates = mapState.cameraPosition.position.target
        )
    }


    private fun refreshToiletMarkers(){
        Log.d(Tags.MainViewModelTag.toString(), "Refreshing toilets")
        val toiletMarkerList = toiletsState.filteredToiletList.map { toilet -> toToiletMarker(toilet) }

        toiletsState = toiletsState.copy(toiletMarkerList = toiletMarkerList)
    }

    /**
     * Triggers a toast that the clicked button is not yet implemented
     *
     */
    fun placeholder(){
        triggerEvent(ScreenEvent.PlaceholderFunction)
    }


    /**
     * Login
     *
     */
    fun login() {
        val login = authState.loginLogin.trim()
        val password = authState.loginPassword.trim()

        viewModelScope.launch {
            repository.login(login, password)
        }
    }

    /**
     * TODO
     *
     */
    fun logout(){
        viewModelScope.launch {
            repository.logout()
            clearAuthState()
        }
    }

    /**
     * TODO
     *
     */
    fun clearAuthState(){
        authState = authState.copy(
            status = AuthUiStatus.MAIN,
            registerLogin = "",
            registerPassword = "",
            registerPasswordConfirmation = "",
            registerName = "",
            loginLogin = "",
            loginPassword = "",
            showInvalidPasswordPrompt = false,
            registrationError = null
        )
    }

    /**
     * TODO
     *
     */
    private suspend fun registerUser(res: RegistrationError?){
        authState = authState.copy(registrationError = res)

        if (authState.registrationError == null) {
            repository.register(authState.registerLogin.trim(), authState.registerPassword.trim(), authState.registerName.trim())
        }
    }

    /**
     * TODO
     *
     */
    fun startRegister(){
        viewModelScope.launch {
            val error = validateRegistrationData()

            registerUser(error)
        }
    }


    /**
     * TODO
     *
     */
    private suspend fun validateRegistrationData(): RegistrationError? {
        val password = authState.registerPassword
        val login = authState.registerLogin

        if (authState.registerLogin.isBlank() ||
            authState.registerPassword.isBlank() ||
            authState.registerName.isBlank() ||
            authState.registerPasswordConfirmation.isBlank())
            return RegistrationError.EMPTY_FIELD

        if (authState.registerPassword != authState.registerPasswordConfirmation)
            return RegistrationError.PASSWORD_CONFIRMATION_MATCH


        if (password.length < MIN_PASSWORD_LENGTH)
            return RegistrationError.PASSWORD_TOO_SHORT


        if (!checkForNumbers(password))
            return RegistrationError.PASSWORD_CONTAINS_NO_NUMBERS


        if (!checkForUpperCase(password))
            return RegistrationError.PASSWORD_CONTAINS_NO_UPPERCASE


        if (!checkForLowerCase(password))
            return RegistrationError.PASSWORD_CONTAINS_NO_LOWERCASE


        if (login.contains(" "))
            return RegistrationError.LOGIN_CONTAINS_SPACES

        if (password.contains(" "))
            return RegistrationError.PASSWORD_CONTAINS_SPACES


        when(repository.checkIfLoginExists(login)) {
            true -> { return RegistrationError.LOGIN_ALREADY_TAKEN }
            false -> {}
            null -> { return RegistrationError.NETWORK_ERROR }
        }
        return null
    }

    /**
     * TODO
     *
     */
    fun continueWithoutAccount(){
        viewModelScope.launch {
            repository.continueWithoutLogin()
        }
    }

    /**
     * TODO
     *
     */
    private fun checkForNumbers(str: String): Boolean {
        for (char in str){
            if (char.isDigit()) return true
        }
        return false
    }

    /**
     * TODO
     *
     */
    private fun checkForUpperCase(str: String): Boolean {
        for (char in str){
            if (char.isUpperCase()) return true
        }
        return false
    }

    /**
     * TODO
     *
     */
    private fun checkForLowerCase(str: String): Boolean {
        for (char in str){
            if (char.isLowerCase()) return true
        }
        return false
    }

    /**
     * TODO
     *
     */
    fun setPadding(paddingValues: PaddingValues){
        scaffoldPadding = paddingValues
    }

    /**
     * TODO
     *
     */
    fun setAuthStatus(status: AuthUiStatus){
        authState = authState.copy(status = status)
    }

    /**
     * TODO
     *
     */
    fun getToilets(){
        viewModelScope.launch {
            val toilets = repository.retrieveToilets()
            toilets?.forEach{
                Log.d("ToiletLogger", it.toString())
            }
        }
    }

    /**
     * TODO
     *
     */
    fun getToiletById(id: Int){
        viewModelScope.launch {
            val toilet = repository.retrieveToiletById(id)

            Log.d("ToiletLogger", toilet.toString())
        }
    }
}

operator fun PaddingValues.plus(other: PaddingValues): PaddingValues = PaddingValues(
    start = this.calculateStartPadding(LayoutDirection.Ltr) +
            other.calculateStartPadding(LayoutDirection.Ltr),
    top = this.calculateTopPadding() + other.calculateTopPadding(),
    end = this.calculateEndPadding(LayoutDirection.Ltr) +
            other.calculateEndPadding(LayoutDirection.Ltr),
    bottom = this.calculateBottomPadding() + other.calculateBottomPadding(),
)

fun roundDouble(number: Double): String{
    val format = DecimalFormat("#.#")
    format.roundingMode = RoundingMode.DOWN
    return format.format(number)
}