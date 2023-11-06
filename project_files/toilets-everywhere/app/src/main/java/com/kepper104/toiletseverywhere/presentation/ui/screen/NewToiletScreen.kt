package com.kepper104.toiletseverywhere.presentation.ui.screen


import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kepper104.toiletseverywhere.data.Tags
import com.kepper104.toiletseverywhere.presentation.MainViewModel
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.time.timepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * TODO
 *
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewToiletDetailsScreen() {
    val mainViewModel: MainViewModel = hiltViewModel(LocalContext.current as ComponentActivity)
    Log.d(Tags.CompositionLogger.tag, "Recomposing new toilet")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)

    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ){
            Text(text = "Creating a new toilet", fontSize = 30.sp)
        }
        Spacer(modifier = Modifier.height(10.dp))

        Text(text = "Please fill in some details about the new toilet:")
        Spacer(modifier = Modifier.height(20.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = mainViewModel.newToiletDetailsState.isPublic,
                onCheckedChange = {
                    mainViewModel.newToiletDetailsState =
                        mainViewModel.newToiletDetailsState.copy(isPublic = it)
                }
            )
            Text(text = "Is it public?")
        }

        if (!mainViewModel.newToiletDetailsState.isPublic){
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Please give this toilet a name")
                Text(text = "Example: Toilet in Cofix")
                TextField(
                    value = mainViewModel.newToiletDetailsState.name,
                    onValueChange = {
                        mainViewModel.newToiletDetailsState =
                            mainViewModel.newToiletDetailsState.copy(name = it)
                    },
                )
            }

        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(text = "Select all conveniences available at this toilet")

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = mainViewModel.newToiletDetailsState.disabledAccess,
                onCheckedChange = {
                    mainViewModel.newToiletDetailsState =
                        mainViewModel.newToiletDetailsState.copy(disabledAccess = it)
                })
            Text(text = "Wheelchair Accessible")
        }
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = mainViewModel.newToiletDetailsState.babyAccess,
                onCheckedChange = {
                    mainViewModel.newToiletDetailsState =
                        mainViewModel.newToiletDetailsState.copy(babyAccess = it)
                })
            Text(text = "Has baby changing station")
        }
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = mainViewModel.newToiletDetailsState.parkingNearby,
                onCheckedChange = {
                    mainViewModel.newToiletDetailsState =
                        mainViewModel.newToiletDetailsState.copy(parkingNearby = it)
                })
            Text(text = "Has parking nearby")
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Per visit price, leave empty if free")
            Spacer(modifier = Modifier.width(5.dp))
            TextField(
                value = mainViewModel.newToiletDetailsState.costString,
                onValueChange = {
                    if (it == "") {
                        mainViewModel.newToiletDetailsState.copy(cost = 0, costString = "")
                            .also { mainViewModel.newToiletDetailsState = it }
                    }
                    else {
                        mainViewModel.newToiletDetailsState.copy(cost = it.toInt(), costString = it)
                            .also { mainViewModel.newToiletDetailsState = it }
                    }
                },
                modifier = Modifier
                    .width(80.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        Text(text = "Select toilet's working hours:")

        Spacer(modifier = Modifier.height(10.dp))

        TimePickers(mainViewModel)

        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Button(onClick = { mainViewModel.openNewToiletConfirmationDialog() }) {
                Text(text = "Create a new toilet")
            }
        }
    }
}

/**
 * TODO
 *
 * @param vm
 */
@Composable
fun TimePickers(vm: MainViewModel) {
    val formattedOpeningTime by remember {
        derivedStateOf {
            DateTimeFormatter
                .ofPattern("HH:mm")
                .format(vm.newToiletDetailsState.openingTime)
        }
    }
    val formattedClosingTime by remember {
        derivedStateOf {
            DateTimeFormatter
                .ofPattern("HH:mm")
                .format(vm.newToiletDetailsState.closingTime)
        }
    }

    val openingTimeDialogState = rememberMaterialDialogState()
    val closingTimeDialogState = rememberMaterialDialogState()

    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Column(
            modifier = Modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Button(onClick = {
                openingTimeDialogState.show()
            }) {
                Text(text = "Pick opening time")
            }
            Text(text = formattedOpeningTime)
        }

        MaterialDialog(
            dialogState = openingTimeDialogState,
            buttons = {
                positiveButton(text = "Ok") {
                    Toast.makeText(
                        context,
                        "Clicked ok",
                        Toast.LENGTH_LONG
                    ).show()
                }
                negativeButton(text = "Cancel")
            }
        ) {
            timepicker(
                initialTime = LocalTime.of(6, 0),
                title = "Pick opening time",
                timeRange = LocalTime.MIDNIGHT..LocalTime.MAX,
                is24HourClock = true
            ) {
                vm.newToiletDetailsState = vm.newToiletDetailsState.copy(openingTime = it)
            }
        }
        Column(
            modifier = Modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Button(onClick = {
                closingTimeDialogState.show()
            }) {
                Text(text = "Pick closing time")
            }
            Text(text = formattedClosingTime)
        }

        MaterialDialog(
            dialogState = closingTimeDialogState,
            buttons = {
                positiveButton(text = "Ok") {
                    Toast.makeText(
                        context,
                        "Clicked ok",
                        Toast.LENGTH_LONG
                    ).show()
                }
                negativeButton(text = "Cancel")
            }
        ) {
            timepicker(
                initialTime = LocalTime.of(22, 0),
                title = "Pick closing time",
                timeRange = LocalTime.MIDNIGHT..LocalTime.MAX,
                is24HourClock = true
            ) {
                vm.newToiletDetailsState = vm.newToiletDetailsState.copy(closingTime = it)

            }
        }
    }

}
