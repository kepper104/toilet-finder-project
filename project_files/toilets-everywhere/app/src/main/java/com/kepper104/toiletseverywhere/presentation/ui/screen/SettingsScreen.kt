package com.kepper104.toiletseverywhere.presentation.ui.screen

import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.kepper104.toiletseverywhere.presentation.MainViewModel
import com.ramcosta.composedestinations.BuildConfig
import com.ramcosta.composedestinations.annotation.Destination

@Destination
@Composable
fun SettingsScreen(

) {
    val mainViewModel: MainViewModel = hiltViewModel(LocalContext.current as ComponentActivity)

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(text = "Current Display Name: ${mainViewModel.loggedInUserState.currentUserName}")
            Button(
                onClick = {
                    mainViewModel.logout()
                }
            ) {
                Text(
                    text = "Log Out"
                )
            }

            Text(text = mainViewModel.mapState.cameraPosition.position.target.toString())
        }
    }
}