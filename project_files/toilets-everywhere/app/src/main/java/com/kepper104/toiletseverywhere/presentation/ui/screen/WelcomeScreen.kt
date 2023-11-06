package com.kepper104.toiletseverywhere.presentation.ui.screen

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.*
import com.kepper104.toiletseverywhere.data.Tags
import com.kepper104.toiletseverywhere.presentation.MainViewModel
import com.ramcosta.composedestinations.annotation.Destination

/**
 * TODO
 *
 */
@OptIn(ExperimentalPermissionsApi::class)
@Destination
@Composable
fun WelcomeScreen() {
    val mainViewModel: MainViewModel = hiltViewModel(LocalContext.current as ComponentActivity)
    Log.d(Tags.CompositionLogger.tag, "Recomposing welcome")

    val locationPermissionState = rememberPermissionState(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )


    if (!locationPermissionState.status.isGranted) {
        LaunchedEffect(key1 = true){
            locationPermissionState.launchPermissionRequest()
        }
    } else{
        val context = LocalContext.current
        val locationClient = remember {
            LocationServices.getFusedLocationProviderClient(context)
        }
        LaunchedEffect(key1 = locationPermissionState.status.isGranted){
            mainViewModel.enableLocationServices(locationClient)

        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally

    ) {

        Log.d(Tags.CompositionLogger.toString(), "Composing welcome!")
        Text(
            text = "Welcome to Toilets Everywhere!",
        )
        Text(
            text = "Select any of the options below:"
        )
    }
}
