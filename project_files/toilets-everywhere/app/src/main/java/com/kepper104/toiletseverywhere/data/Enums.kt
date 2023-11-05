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
const val APP_LAUNCH_INIT_DELAY = 500L // UNUSED
const val MIN_PASSWORD_LENGTH = 8


/**
 * All possible registration [errorMessage]s to be shown to user.
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
 * All possible login statuses.
 */
enum class LoginStatus{
    None, Success, Fail, Processing
}

/**
 * All main app destinations (menus) shown on the bottom nav bar. Each destination contains data on
 * [direction] - composable to show, [iconUnselected], [iconSelected] and [label] for nav bar.
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
 * Current screen status on the starting auth screen.
 */
enum class AuthUiStatus {
    MAIN, REGISTER, LOGIN
}

/**
 * Tags for logging with respective [tag] labels for the logcat.
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
 * Two icons with different colors for the google map composable.
 * [icon] is a [BitmapDescriptor] with a HUE applied on top
 */
enum class ToiletIcons(val icon: BitmapDescriptor){
    ToiletRed(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)),
    ToiletGreen(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
}

/**
 * Enum for communicating screen events (mainly toasts) from view-model to UI.
 */

enum class ScreenEvent{
    ToiletAddingEnabledToast,
    ToiletAddingDisabledToast,
    PlaceholderFunction,
    ToiletCreationFailToast,
    ToiletCreationSuccessToast,
    FiltersMatchNoToiletsToast,
    NameChangeSuccessToast,
    NameChangeFailToast,
    ReviewPostFailToast,
}

/**
 * Enum for communicating navigation events
 * (e.g. forcing app to open a certain screen) from view-model to UI.
 */
enum class NavigationEvent{
    NavigateToMap,
    NavigateToList,
    NavigateToSettings,
}
