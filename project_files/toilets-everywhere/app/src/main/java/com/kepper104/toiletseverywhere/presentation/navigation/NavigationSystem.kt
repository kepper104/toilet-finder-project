package com.kepper104.toiletseverywhere.presentation.navigation

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.kepper104.toiletseverywhere.data.BottomBarDestination
import com.kepper104.toiletseverywhere.data.NOT_LOGGED_IN_STRING
import com.kepper104.toiletseverywhere.data.NavigationEvent
import com.kepper104.toiletseverywhere.data.ScreenEvent
import com.kepper104.toiletseverywhere.data.Tags
import com.kepper104.toiletseverywhere.data.makeToast
import com.kepper104.toiletseverywhere.presentation.MainViewModel
import com.kepper104.toiletseverywhere.presentation.ui.screen.NavGraphs
import com.kepper104.toiletseverywhere.presentation.ui.screen.appCurrentDestinationAsState
import com.kepper104.toiletseverywhere.presentation.ui.screen.destinations.MapScreenDestination
import com.kepper104.toiletseverywhere.presentation.ui.screen.destinations.TypedDestination
import com.kepper104.toiletseverywhere.presentation.ui.screen.startAppDestination
import com.kepper104.toiletseverywhere.presentation.ui.state.CurrentDetailsScreen
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.navigation.popBackStack
import com.ramcosta.composedestinations.navigation.popUpTo
import com.ramcosta.composedestinations.utils.isRouteOnBackStack

// TODO maybe move to utils
val destinationToDetailScreenMapping = mapOf(CurrentDetailsScreen.MAP to BottomBarDestination.MapView, CurrentDetailsScreen.LIST to BottomBarDestination.ListView)

/**
 * TODO
 *
 * @param navController
 */
@Composable
fun BottomNavigationBar(
    navController: NavHostController,
) {
    val currentDestination: TypedDestination<out Any?> = navController.appCurrentDestinationAsState().value
        ?: NavGraphs.root.startAppDestination
    val mainViewModel: MainViewModel = hiltViewModel(LocalContext.current as ComponentActivity)


    NavigationBar {
        BottomBarDestination.values().forEach { destination ->
            val isSelected = currentDestination == destination.direction
            val isCurrentDestOnBackStack = navController.isRouteOnBackStack(destination.direction)
            NavigationBarItem(
                selected = isSelected,

                onClick = {
                    if (mainViewModel.navigationState.currentDestination == destination){
                        mainViewModel.leaveToiletViewDetailsScreen()
                    }
                    mainViewModel.changeNavigationState(destination)

                    if (isCurrentDestOnBackStack){
                        navController.popBackStack(destination.direction, false)
                        return@NavigationBarItem
                    }
                    navController.navigate(destination.direction){
                        popUpTo(NavGraphs.root){
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    if (isSelected)
                        Icon(destination.iconSelected, contentDescription = "icon")
                    else
                    {
                        Icon(destination.iconUnselected, contentDescription = "icon")
                    }},

                label = { Text(text = destination.label) }
            )
        }
    }

}

/**
 * TODO
 *
 * @param navController
 * @param bottomBar
 * @param content
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavScaffold(
    navController: NavHostController,
    bottomBar: @Composable (TypedDestination<out Any?>) -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    val destination =
        navController.appCurrentDestinationAsState().value
        ?:
        MapScreenDestination


    Scaffold(
        topBar = { MapTopAppBar()},
        bottomBar = { bottomBar(destination) },
        content = content
    )
}

/**
 * TODO
 *
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapTopAppBar() {
    val mainViewModel: MainViewModel = hiltViewModel(LocalContext.current as ComponentActivity)


    TopAppBar(
        title = {
            when (mainViewModel.navigationState.currentDestination) {
                null -> {
                    Text(text = "Welcome!")
                }

                BottomBarDestination.MapView -> {
                    Text(text = "Toilet Map")
                }

                BottomBarDestination.ListView -> {
                    Text(text = "Toilet List")
                }

                BottomBarDestination.Settings -> {
                    Text(text = "Settings")
                }
            }
        },

        navigationIcon = {
            // Navigate back button for toilet details screen
            if(destinationToDetailScreenMapping[mainViewModel.toiletViewDetailsState.currentDetailScreen] == mainViewModel.navigationState.currentDestination && mainViewModel.navigationState.currentDestination != null){
                IconButton(onClick = { mainViewModel.leaveToiletViewDetailsScreen() }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Go Back")
                }
            }

            // Navigate back button for toilet creation screen
            if(mainViewModel.newToiletDetailsState.enabled && mainViewModel.navigationState.currentDestination == BottomBarDestination.MapView){
                IconButton(onClick = { mainViewModel.leaveNewToiletDetailsScreen() }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Go Back")
                }
            }


        },
        actions = {

            // Showing no icons when on toilet details view
            if (mainViewModel.toiletViewDetailsState.currentDetailScreen != CurrentDetailsScreen.NONE && destinationToDetailScreenMapping[mainViewModel.toiletViewDetailsState.currentDetailScreen] == mainViewModel.navigationState.currentDestination){
                return@TopAppBar
            }

            // Showing no icons when on new toilet add view
            if (mainViewModel.newToiletDetailsState.enabled && mainViewModel.navigationState.currentDestination == BottomBarDestination.MapView){
                return@TopAppBar
            }

            // Top Bar navigation buttons
            if (mainViewModel.navigationState.currentDestination == BottomBarDestination.MapView){
                Log.d(Tags.CompositionLogger.toString(), "Showing buttons for MapView")

                //
                if (mainViewModel.mapState.addingToilet){
                    IconButton(onClick = { mainViewModel.navigateToNewToiletDetailsScreen(); mainViewModel.mapState = mainViewModel.mapState.copy(addingToilet = false) }) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Add toilet in selected point"
                        )
                    }
                }

                // Showing toilet creation button only for logged in users
                if (mainViewModel.loggedInUserState.currentUserName != NOT_LOGGED_IN_STRING){
                    Log.d(Tags.CompositionLogger.toString(), "Showing Add Toilet button")
                    IconButton(onClick = {
                        if (!mainViewModel.mapState.addingToilet){
                            mainViewModel.triggerEvent(ScreenEvent.ToiletAddingEnabledToast)
                            mainViewModel.mapState = mainViewModel.mapState.copy(addingToilet = true)
                        }else{
                            mainViewModel.triggerEvent(ScreenEvent.ToiletAddingDisabledToast)
                            mainViewModel.mapState = mainViewModel.mapState.copy(addingToilet = false)
                        }

                    }) {
                        Icon(imageVector =
                                if (mainViewModel.mapState.addingToilet)
                                    Icons.Filled.Cancel
                                else
                                    Icons.Filled.AddCircleOutline,

                            contentDescription = "Add a new toilet")
                    }
                }
                else{
                    Log.d(Tags.CompositionLogger.toString(), "NOT Showing Add Toilet button")

                }
                IconButton(onClick = { mainViewModel.placeholder() }) {
                    Icon(imageVector = Icons.Default.FilterAlt, contentDescription = "Filter toilets")
                }
                IconButton(onClick = { mainViewModel.getLatestToilets() }) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh toilets")
                }

            }else if (mainViewModel.navigationState.currentDestination == BottomBarDestination.ListView){
                IconButton(onClick = { mainViewModel.placeholder() }) {
                    Icon(imageVector = Icons.Default.FilterAlt, contentDescription = "Filter toilets")
                }
                IconButton(onClick = { mainViewModel.getLatestToilets() }) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh toilets")
                }
            }
        }
    )
}

/**
 * TODO
 *
 * @param viewModel
 * @param composeContext
 */
@Composable
fun HandleEvents(viewModel: MainViewModel, composeContext: Context) {
    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                ScreenEvent.ToiletAddingDisabledToast -> {
                    makeToast("Toilet adding canceled", composeContext, Toast.LENGTH_LONG)
                }

                ScreenEvent.ToiletAddingEnabledToast -> {
                    makeToast("Move your map and press Tick to add toilet", composeContext, Toast.LENGTH_LONG)
                }

                ScreenEvent.PlaceholderFunction -> {
                    makeToast("This function is not implemented", composeContext, Toast.LENGTH_SHORT)
                }

                ScreenEvent.ToiletCreationFailToast -> {
                    makeToast("An error occurred when creating a new toilet", composeContext, Toast.LENGTH_SHORT)
                }
                ScreenEvent.ToiletCreationSuccessToast -> {
                    makeToast("New toilet created successfully!", composeContext, Toast.LENGTH_SHORT)
                }
            }
        }
    }
}

/**
 * TODO
 *
 * @param viewModel
 * @param navController
 */
@Composable
fun HandleNavigationEvents(viewModel: MainViewModel, navController: NavHostController) {
    LaunchedEffect(key1 = true) {
        viewModel.navigationEventFlow.collect { navigationEvent ->
            when (navigationEvent){
                NavigationEvent.NavigateToList -> viewModel.placeholder()
                NavigationEvent.NavigateToMap -> {
                    navController.navigate(BottomBarDestination.MapView.direction){
//                        popUpTo(NavGraphs.root){
//                            saveState = true
//                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
                NavigationEvent.NavigateToSettings -> viewModel.placeholder()
            }
        }
    }
}