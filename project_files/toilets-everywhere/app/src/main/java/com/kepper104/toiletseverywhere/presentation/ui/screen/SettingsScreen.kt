package com.kepper104.toiletseverywhere.presentation.ui.screen

import android.os.Build.VERSION
import androidx.activity.ComponentActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.kepper104.toiletseverywhere.data.NOT_LOGGED_IN_STRING
import com.kepper104.toiletseverywhere.presentation.MainViewModel
import com.kepper104.toiletseverywhere.presentation.ui.MapStyle
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
            .fillMaxSize()
            .padding(mainViewModel.scaffoldPadding)
            .padding(20.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {

            Text(text = "Select Dark Mode preferences:")
            DarkModeSelectButtons(viewModel = mainViewModel)

            Spacer(modifier = Modifier.height(20.dp))

            MapStyleSelection(viewModel = mainViewModel)

            Spacer(modifier = Modifier.weight(1f))

            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (mainViewModel.loggedInUserState.currentUserName != NOT_LOGGED_IN_STRING){
                    Text(text = "Your current name is '${mainViewModel.loggedInUserState.currentUserName}'")

                    Row{
                        Button(
                            onClick = { mainViewModel.showNameChangeDialog()}
                        ) {
                            Text(text = "Change name")
                        }
                        Button(
                            onClick = { mainViewModel.logout() }
                        ) {
                            Text(text = "Log Out")
                        }

                    }

                } else {
                    Text(text = "To have full access to all app features, log in:")

                    Row{
                        Button(
                            onClick = { mainViewModel.logout() }
                        ) {
                            Text(text = "Log in")
                        }

                    }
                }

                Spacer(modifier = Modifier.height(20.dp))


                Text(text = "Toilets Everywhere, version ${VERSION.CODENAME}")
                Text(text = "Made by kepper104")

                if (mainViewModel.settingsState.nameChangeDialogOpen){
                    NameChangeDialog(viewModel = mainViewModel)
                }
            }

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapStyleSelection(viewModel: MainViewModel) {
    CompositionLocalProvider (
        LocalTextInputService provides null
    ){
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Select a map style")
            Spacer(modifier = Modifier.width(10.dp))
            ExposedDropdownMenuBox(
                expanded = viewModel.settingsState.mapStyleSelectionExpanded,
                onExpandedChange = {viewModel.toggleMapStyleSelectionMenu()}
            ) {
                TextField(
                    value = viewModel.settingsState.selectedMapStyle.styleName,
                    onValueChange = {},
                    modifier = Modifier.menuAnchor(),
                    readOnly = true,
                    trailingIcon = { Icon(
                        imageVector = Icons.Default.Brush,
                        contentDescription = "Brush icon"
                    )}

                )
                ExposedDropdownMenu(
                    expanded = viewModel.settingsState.mapStyleSelectionExpanded,
                    onDismissRequest = { viewModel.closeMapStyleSelectionMenu() }
                ) {
                    MapStyle.values().forEach {
                        DropdownMenuItem(
                            text = {Text(text = it.styleName)},
                            onClick = { viewModel.changeSelectedMapStyle(it); viewModel.closeMapStyleSelectionMenu()}
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NameChangeDialog(
    viewModel: MainViewModel
) {
    Dialog(onDismissRequest = { viewModel.closeNameChangeDialog() }) {
        // Draw a rectangle shape with rounded corners inside the dialog
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(375.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                Text(
                    text = "Your current name is '${viewModel.loggedInUserState.currentUserName}'",
                    modifier = Modifier.padding(16.dp),
                )
                OutlinedTextField(
                    value = viewModel.settingsState.newDisplayName,
                    onValueChange = {
                        viewModel.settingsState = viewModel.settingsState.copy(newDisplayName = it)
                    })
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TextButton(
                        onClick = { viewModel.closeNameChangeDialog() },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("Dismiss")
                    }
                    TextButton(
                        onClick = { viewModel.changeDisplayName() },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}