package com.kepper104.toiletseverywhere.presentation

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.MapProperties
import com.kepper104.toiletseverywhere.data.AuthUiStatus
import com.kepper104.toiletseverywhere.data.BottomBarDestination
import com.kepper104.toiletseverywhere.data.LoginStatus
import com.kepper104.toiletseverywhere.data.MIN_PASSWORD_LENGTH
import com.kepper104.toiletseverywhere.data.NavigationEvent
import com.kepper104.toiletseverywhere.data.RegistrationError
import com.kepper104.toiletseverywhere.data.ScreenEvent
import com.kepper104.toiletseverywhere.data.Tags
import com.kepper104.toiletseverywhere.data.getToiletDistanceMeters
import com.kepper104.toiletseverywhere.data.toToiletMarker
import com.kepper104.toiletseverywhere.domain.model.Toilet
import com.kepper104.toiletseverywhere.domain.repository.Repository
import com.kepper104.toiletseverywhere.presentation.ui.screen.NavGraphs
import com.kepper104.toiletseverywhere.presentation.ui.state.AuthState
import com.kepper104.toiletseverywhere.presentation.ui.state.CurrentDetailsScreen
import com.kepper104.toiletseverywhere.presentation.ui.state.LoggedInUserState
import com.kepper104.toiletseverywhere.presentation.ui.state.MapState
import com.kepper104.toiletseverywhere.presentation.ui.state.NavigationState
import com.kepper104.toiletseverywhere.presentation.ui.state.NewToiletDetailsState
import com.kepper104.toiletseverywhere.presentation.ui.state.ToiletViewDetailsState
import com.kepper104.toiletseverywhere.presentation.ui.state.ToiletsState
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.navigation.popBackStack
import com.ramcosta.composedestinations.navigation.popUpTo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject


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

    private val _eventFlow = MutableSharedFlow<ScreenEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private val _navigationEventFlow = MutableSharedFlow<NavigationEvent>()
    val navigationEventFlow = _navigationEventFlow.asSharedFlow()


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
    init {
        collectLoginStatusFlow()

        getLatestToilets()
    }


    fun moveCameraToToiletLocation(toilet: Toilet){
        mapState = mapState.copy(cameraPosition = CameraPositionState(CameraPosition(toilet.coordinates, 17F, 0F, 0F)))
        leaveToiletViewDetailsScreen()
        changeNavigationState(BottomBarDestination.MapView)
        viewModelScope.launch {
            _navigationEventFlow.emit(NavigationEvent.NavigateToMap)
        }
    }

    
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


    fun triggerEvent(event: ScreenEvent){
        viewModelScope.launch {
            _eventFlow.emit(event)
        }
    }


    fun enableLocationServices(locationProviderClient: FusedLocationProviderClient){
        mapState = mapState.copy(properties = MapProperties(isMyLocationEnabled = true))
        locationClient = locationProviderClient
        viewModelScope.launch {
            try{
                Log.d(Tags.MainViewModelTag.toString(), "Getting position on LocationServices enable")
                val res = locationClient.lastLocation
                res.addOnSuccessListener {

                    mapState = mapState.copy(
                        cameraPosition = CameraPositionState(CameraPosition(LatLng(res.result.latitude, res.result.longitude), 15F, 0F, 0F)),
                        userPosition = LatLng(res.result.latitude, res.result.longitude)
                    )
                    Log.d(Tags.MainViewModelTag.toString(), "Successfully got position on LocationServices enable")
                }
                res.addOnFailureListener{
                    Log.e(Tags.MainViewModelTag.toString(),  it.toString())
                }

            } catch (e: SecurityException){
                Log.e(Tags.MainViewModelTag.toString(), "Location permission not granted")
            }

        }
    }

    fun startLocationRefreshCycle() {
        viewModelScope.launch {
            while (true) {
                refreshUserLocation()
                delay(5000)
            }
        }
    }

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

    fun changeNavigationState(newDestination: BottomBarDestination){
        navigationState = navigationState.copy(currentDestination = newDestination)

        if (!mapState.addingToilet) return

        if (newDestination != BottomBarDestination.MapView){
            mapState = mapState.copy(addingToilet = false)
            triggerEvent(ScreenEvent.ToiletAddingDisabledToast) // TODO potentially remove
        }
    }


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
                    LoginStatus.Processing -> {Log.d(Tags.MainViewModelTag.toString(), "Logging in...")}  // TODO show a loading icon or smth
                }
            }
        }
    }

    private fun saveLoggedInUser(isLoggedIn: Boolean, newUserName: String){
        Log.d(Tags.MainViewModelTag.toString(), "Saving user data to vm: $isLoggedIn, $newUserName")
        loggedInUserState = loggedInUserState.copy(
            isLoggedIn = isLoggedIn,
            currentUserName = newUserName
        )
    }

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

            Log.d(Tags.MainViewModelTag.toString(), "Saved toilets $toilets")
            refreshToiletMarkers()

        }

    }

    fun navigateToDetails(toilet: Toilet, source: CurrentDetailsScreen){
        viewModelScope.launch {
            Log.d(Tags.MainViewModelTag.toString(), "Opening details: ${toilet.id}, $source")
            val author = repository.retrieveUserById(toilet.authorId)
            val authorName = author?.displayName ?: "Error"

            toiletViewDetailsState = toiletViewDetailsState.copy(
                toilet = toilet,
                currentDetailScreen = source,
                authorName = authorName
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
        var toiletList = toiletsState.toiletList.map { toilet ->  toToiletMarker(toilet)}
        toiletList = toiletList.sortedBy { getToiletDistanceMeters(mapState.userPosition, it.position) }
        mapState = mapState.copy(toiletMarkers = toiletList)
    }

    fun navigateBackButton(){
        Log.d(Tags.MainViewModelTag.toString(), "Navigating back")
    }

    fun placeholder(){
        triggerEvent(ScreenEvent.PlaceholderFunction)
    }


    fun login() {
        val login = authState.loginLogin
        val password = authState.loginPassword

        viewModelScope.launch {
            repository.login(login, password)
        }
    }

    fun logout(){
        viewModelScope.launch {
            repository.logout()
            clearAuthState()
        }
    }

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

    private suspend fun registerUser(res: RegistrationError?){
        authState = authState.copy(registrationError = res)

        if (authState.registrationError == null) {
            repository.register(authState.registerLogin, authState.registerPassword, authState.registerName.trim())
        }
    }

    fun startRegister(){
        viewModelScope.launch {
            val error = validateRegistrationData()

            registerUser(error)
        }
    }


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

    fun continueWithoutAccount(){
        viewModelScope.launch {
            repository.continueWithoutLogin()
        }
    }
    private fun checkForNumbers(str: String): Boolean {
        for (char in str){
            if (char.isDigit()) return true
        }
        return false
    }
    private fun checkForUpperCase(str: String): Boolean {
        for (char in str){
            if (char.isUpperCase()) return true
        }
        return false
    }

    private fun checkForLowerCase(str: String): Boolean {
        for (char in str){
            if (char.isLowerCase()) return true
        }
        return false
    }

    fun setPadding(paddingValues: PaddingValues){
        scaffoldPadding = paddingValues
    }

    fun setAuthStatus(status: AuthUiStatus){
        authState = authState.copy(status = status)
    }

    fun getToilets(){
        viewModelScope.launch {
            val toilets = repository.retrieveToilets()
            toilets?.forEach{
                Log.d("ToiletLogger", it.toString())
            }
        }
    }
    fun getToiletById(id: Int){
        viewModelScope.launch {
            val toilet = repository.retrieveToiletById(id)

            Log.d("ToiletLogger", toilet.toString())
        }
    }

}