package com.kepper104.toiletseverywhere.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ViewList
import androidx.compose.ui.graphics.vector.ImageVector
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.kepper104.toiletseverywhere.presentation.ui.screen.destinations.DirectionDestination
import com.kepper104.toiletseverywhere.presentation.ui.screen.destinations.ListScreenDestination
import com.kepper104.toiletseverywhere.presentation.ui.screen.destinations.MapScreenDestination
import com.kepper104.toiletseverywhere.presentation.ui.screen.destinations.SettingsScreenDestination

// Constants
const val NOT_LOGGED_IN_STRING = "NOTLOGGEDIN"
const val APP_LAUNCH_INIT_DELAY = 500L
const val MIN_PASSWORD_LENGTH = 8


/**
 * TODO
 *
 * @property errorMessage
 */
enum class RegistrationError (val errorMessage: String){
    PASSWORD_TOO_SHORT ("Password must be at least $MIN_PASSWORD_LENGTH symbols!"),
    PASSWORD_CONTAINS_NO_UPPERCASE("Password must contain at least 1 capital letter!"),
    PASSWORD_CONTAINS_NO_LOWERCASE("Password must contain at least 1 lowercase letter!"),
    PASSWORD_CONTAINS_NO_NUMBERS("Password must contain at least 1 digit!"),
    EMPTY_FIELD ("All fields must be filled in!"),
    PASSWORD_CONFIRMATION_MATCH ("Password and password confirmation don't match!"),
    LOGIN_CONTAINS_SPACES ("Login must not contain spaces!"),
    PASSWORD_CONTAINS_SPACES ("Password mustn't contain spaces!"),
    LOGIN_ALREADY_TAKEN ("Login is already taken!"),
    NETWORK_ERROR ("Network Error!")
}

/**
 * TODO
 *
 */
enum class LoginStatus{
    None, Success, Fail, Processing
}

/**
 * TODO
 *
 * @property direction
 * @property iconUnselected
 * @property iconSelected
 * @property label
 */
enum class BottomBarDestination(
    val direction: DirectionDestination,
    val iconUnselected: ImageVector,
    val iconSelected: ImageVector,
    val label: String
) {
    MapView(MapScreenDestination, Icons.Outlined.Map,  Icons.Filled.Map,"Toilet Map"),
    ListView(ListScreenDestination, Icons.Outlined.ViewList,  Icons.Filled.ViewList, "Toilet List"),
    Settings(SettingsScreenDestination, Icons.Outlined.Settings,  Icons.Filled.Settings, "Settings"),
}


/**
 * TODO
 *
 */
enum class AuthUiStatus {
    MAIN, REGISTER, LOGIN
}

/**
 * TODO
 *
 * @property tag
 */
enum class Tags(val tag: String){
    MainViewModelTag("ViewModelLogger"),
    RepositoryLogger("RepositoryLogger"),
    CompositionLogger("CompositionLogger"),
    NavigationLogger("NavigationLogger"),
    TempLogger("TempLogger"),
    NetworkLogger("NetworkLogger")

}

/**
 * TODO
 *
 * @property icon
 */
enum class ToiletIcons(val icon: BitmapDescriptor){
    ToiletRed(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)),
    ToiletGreen(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
}

/**
 * TODO
 *
 */
sealed class ScreenEvent{
    object ToiletAddingEnabledToast: ScreenEvent()
    object ToiletAddingDisabledToast: ScreenEvent()
    object PlaceholderFunction: ScreenEvent()
    object ToiletCreationFailToast: ScreenEvent()
    object ToiletCreationSuccessToast: ScreenEvent()
}

/**
 * TODO
 *
 */
sealed class NavigationEvent{
    object NavigateToMap: NavigationEvent()
    object NavigateToList: NavigationEvent()
    object NavigateToSettings: NavigationEvent()
}