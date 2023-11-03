package com.kepper104.toiletseverywhere.presentation.ui.screen

import android.os.Build.VERSION
import androidx.activity.ComponentActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kepper104.toiletseverywhere.presentation.MainViewModel
import com.kepper104.toiletseverywhere.presentation.ui.state.DarkModeStatus
import com.ramcosta.composedestinations.annotation.Destination


/**
 * TODO
 *
 */
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

            Text(text = "Select Dark Mode preferences:")
            DarkModeSelectButtons(viewModel = mainViewModel)

            Text(text = "Current Display Name: ${mainViewModel.loggedInUserState.currentUserName}")

            Button(onClick = { mainViewModel.logout() }) { Text(text = "Log Out") }
            Text(text = "Toilets Everywhere, version ${VERSION.CODENAME}")
            Text(text = "Made by kepper104")

        }
    }
}
@Composable
fun DarkModeSelectButtons(viewModel: MainViewModel) {
    Row (
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ){

        Button(
            onClick = { viewModel.changeSelectedDarkModeSetting(DarkModeStatus.AUTO) },
            border = if (viewModel.settingsState.selectedDarkModeOption == DarkModeStatus.AUTO) BorderStroke(5.dp, Color.Blue) else BorderStroke(0.dp, Color.Blue)
        ) {
            Text(text = "Auto")
        }

        Button(
            onClick = { viewModel.changeSelectedDarkModeSetting(DarkModeStatus.FORCED_ON) },
            border = if (viewModel.settingsState.selectedDarkModeOption == DarkModeStatus.FORCED_ON) BorderStroke(5.dp, Color.Blue) else BorderStroke(0.dp, Color.Blue)
        ) {
            Text(text = "Dark")
        }

        Button(
            onClick = { viewModel.changeSelectedDarkModeSetting(DarkModeStatus.FORCED_OFF) },
            border = if (viewModel.settingsState.selectedDarkModeOption == DarkModeStatus.FORCED_OFF) BorderStroke(5.dp, Color.Blue) else BorderStroke(0.dp, Color.Blue)
        ) {
            Text(text = "Light")
        }
    }


}