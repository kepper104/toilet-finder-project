package com.kepper104.toiletseverywhere.presentation.ui.screen

import android.annotation.SuppressLint
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.kepper104.toiletseverywhere.data.Tags
import com.kepper104.toiletseverywhere.isNavStackReady
import com.kepper104.toiletseverywhere.presentation.MainViewModel
import com.kepper104.toiletseverywhere.presentation.navigation.BottomNavigationBar
import com.kepper104.toiletseverywhere.presentation.navigation.HandleEvents
import com.kepper104.toiletseverywhere.presentation.navigation.HandleMessageToasts
import com.kepper104.toiletseverywhere.presentation.navigation.HandleNavigationEvents
import com.kepper104.toiletseverywhere.presentation.navigation.MapTopAppBar
import com.kepper104.toiletseverywhere.presentation.navigation.NavScaffold
import com.kepper104.toiletseverywhere.presentation.ui.screen.destinations.AuthScreenDestination
import com.kepper104.toiletseverywhere.presentation.ui.screen.destinations.MainScreenDestination
import com.kepper104.toiletseverywhere.presentation.ui.screen.destinations.WelcomeScreenDestination
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.spec.Route
import kotlinx.coroutines.delay


/**
 * TODO
 *
 */
@RootNavGraph(start = true)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Destination
@Composable
fun MainScreen(

) {
    val navController = rememberNavController()
    val mainViewModel: MainViewModel = hiltViewModel(LocalContext.current as ComponentActivity)
    Log.d(Tags.CompositionLogger.tag, "Recomposing main screen")

    var currentRoute: Route by remember{
        mutableStateOf(AuthScreenDestination)
    }

    val isLoggedInFlowChecker = mainViewModel.isLoggedInFlow.collectAsState(initial = null)

    LaunchedEffect(key1 = mainViewModel.loggedInUserState.isLoggedIn){
        Log.d(Tags.NavigationLogger.tag, "Getting navstack ready..")
        while (!isNavStackReady){
            Log.d(Tags.NavigationLogger.tag, "Still not ready...")

            delay(16)
        }
        Log.d(Tags.NavigationLogger.tag, "Now navstack is ready!")


        if (isLoggedInFlowChecker.value == false){
            currentRoute = AuthScreenDestination
            Log.d(Tags.CompositionLogger.toString(), "Going to login")

            navController.navigate(AuthScreenDestination){
                launchSingleTop = true
            }
        } else if (isLoggedInFlowChecker.value == true){
            currentRoute = WelcomeScreenDestination
            Log.d(Tags.CompositionLogger.toString(), "Going to welcome")

            navController.navigate(MainScreenDestination){
                launchSingleTop = true
            }
        }
    }

    HandleMessageToasts(viewModel = mainViewModel, composeContext = LocalContext.current)
    HandleEvents(viewModel = mainViewModel, composeContext = LocalContext.current)
    HandleNavigationEvents(viewModel = mainViewModel, navController = navController)

    NavScaffold(
        navController = navController,
        topBar = { MapTopAppBar() },
        bottomBar = {
            if (it != AuthScreenDestination){
                BottomNavigationBar(
                    navController = navController,
                )
            }
        }) {

        LaunchedEffect(key1 = true){
            mainViewModel.setPadding(it)
        }

        DestinationsNavHost(
            navGraph = NavGraphs.root,
            navController = navController,
            startRoute = currentRoute
        )
    }



}

