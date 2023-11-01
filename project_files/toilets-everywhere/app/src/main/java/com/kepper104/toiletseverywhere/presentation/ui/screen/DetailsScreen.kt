package com.kepper104.toiletseverywhere.presentation.ui.screen

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessible
import androidx.compose.material.icons.filled.BabyChangingStation
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.kepper104.toiletseverywhere.data.Tags
import com.kepper104.toiletseverywhere.data.getDistanceMeters
import com.kepper104.toiletseverywhere.data.getToiletOpenString
import com.kepper104.toiletseverywhere.data.getToiletWorkingHoursString
import com.kepper104.toiletseverywhere.presentation.MainViewModel
import com.ramcosta.composedestinations.annotation.Destination

@Preview
@Destination
@Composable
fun DetailsScreen() {
    val mainViewModel: MainViewModel = hiltViewModel(LocalContext.current as ComponentActivity)
    Log.d(Tags.CompositionLogger.toString(), "Composing details screen")
    Column (
//        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = Modifier
            .fillMaxSize()
    ){
        val toiletInfo = mainViewModel.toiletViewDetailsState
        val toilet = toiletInfo.toilet!!

        Text(
            text =
            if (toilet.isPublic) {
                "Public Toilet"
            } else {
                "Toilet in ${toilet.placeName}"
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        )

        Text(
            text = "Created by ${toiletInfo.authorName} ${toilet.creationDate}",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        )


        Row {
            Text(
                text = "${
                    getDistanceMeters(
                        mainViewModel.mapState.userPosition,
                        toilet.coordinates
                    )
                } away"
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(imageVector = Icons.Default.StarRate, contentDescription = "Star")
            Text(text = "4/5 (10)")
        }
        Text(text = if (toilet.cost == 0) "Free" else "${toilet.cost}₽")

        Text(text = getToiletWorkingHoursString(toilet, true))
        Text(text = "Currently ${getToiletOpenString(toilet)}")

        if (toilet.disabledAccess || toilet.babyAccess || toilet.parkingNearby){
            Text(text = "Features:")
        }else{
            Text(text = "Has no features ;(")
        }

        if (toilet.disabledAccess){
            Row {
                Icon(imageVector = Icons.Default.Accessible, contentDescription = "Wheelchair icon")
                Text(text = "Wheelchair Accessible")

            }
        }
        if (toilet.babyAccess){
            Row {
                Icon(imageVector = Icons.Default.BabyChangingStation, contentDescription = "Baby Changing Station icon")
                Text(text = "Has a Baby Changing Station")

            }
        }
        if (toilet.parkingNearby){
            Row {
                Icon(imageVector = Icons.Default.LocalParking, contentDescription = "Parking nearby icon")
                Text(text = "Has a parking nearby")
            }
        }

        Button(onClick = { mainViewModel.moveCameraToToiletLocation(toilet = toilet) }) {
            Text(text = "Go to map")
        }
        

    }

}