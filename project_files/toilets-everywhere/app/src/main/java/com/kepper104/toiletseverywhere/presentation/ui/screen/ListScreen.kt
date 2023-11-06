package com.kepper104.toiletseverywhere.presentation.ui.screen

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessible
import androidx.compose.material.icons.filled.BabyChangingStation
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kepper104.toiletseverywhere.data.Tags
import com.kepper104.toiletseverywhere.data.getToiletDistanceMeters
import com.kepper104.toiletseverywhere.data.getToiletDistanceString
import com.kepper104.toiletseverywhere.data.getToiletNameString
import com.kepper104.toiletseverywhere.data.getToiletOpenString
import com.kepper104.toiletseverywhere.data.getToiletPriceString
import com.kepper104.toiletseverywhere.data.getToiletWorkingHoursString
import com.kepper104.toiletseverywhere.domain.model.Toilet
import com.kepper104.toiletseverywhere.presentation.MainViewModel
import com.kepper104.toiletseverywhere.presentation.roundDouble
import com.kepper104.toiletseverywhere.presentation.ui.state.CurrentDetailsScreen
import com.kepper104.toiletseverywhere.presentation.ui.state.DarkModeStatus
import com.kepper104.toiletseverywhere.presentation.ui.state.SettingsState
import com.ramcosta.composedestinations.annotation.Destination


/**
 * TODO
 *
 */
@Destination
@Composable
fun ListScreen(

) {
    val mainViewModel: MainViewModel = hiltViewModel(LocalContext.current as ComponentActivity)
    Log.d(Tags.CompositionLogger.tag, "Recomposing list")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(mainViewModel.scaffoldPadding),
    ) {

        if (mainViewModel.toiletViewDetailsState.currentDetailScreen == CurrentDetailsScreen.LIST){
            if (mainViewModel.toiletViewDetailsState.allReviewsMenuOpen){
                BackHandler (
                    onBack = { mainViewModel.closeAllReviews()}
                )
            } else {
                BackHandler (
                    onBack = {Log.d("BackLogger", "Handled back from MAP"); mainViewModel.leaveToiletViewDetailsScreen()}
                )
            }
            DetailsScreen()
            return
        }

        Log.d(Tags.CompositionLogger.toString(), "Composing list!")

        LazyColumn {
            if (mainViewModel.toiletsState.toiletList.isEmpty()){
                item{
                    Text(text = "We couldn't find any toilets whatsoever ;( Try again later")
                }
            }
            else if (mainViewModel.toiletsState.filteredToiletList.isEmpty()){
                item{
                    Text(text = "Selected filters don't match any available toilets, so try changing them up!")
                }
            }
            for (toilet in mainViewModel.toiletsState.filteredToiletList){
                item{
                    ToiletCard(toilet = toilet, navigateToDetails = mainViewModel::navigateToDetails, getToiletDistanceMeters(mainViewModel.mapState.userPosition, toilet.coordinates), settingsState = mainViewModel.settingsState)
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

/**
 * TODO
 *
 * @param toilet
 * @param navigateToDetails
 * @param distanceToToilet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun ToiletCard(toilet: Toilet = Toilet(), navigateToDetails: (toilet: Toilet, source: CurrentDetailsScreen) -> Unit = ::placeHolderFunc, distanceToToilet: Int = 100, settingsState: SettingsState = SettingsState()) {
    Card(
        onClick = { navigateToDetails(toilet, CurrentDetailsScreen.LIST) },
        modifier = Modifier.padding(5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(5.dp)
        ) {
            Row {
                
                Text(
                    text = getToiletNameString(toilet)
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(imageVector = Icons.Default.StarRate, contentDescription = "Star")

                if (toilet.reviewCount == 0){
                    Text(text = "No rating")
                } else {
                    Text(text = "${roundDouble(toilet.averageRating)}/5 (${toilet.reviewCount})")
                }

            }

            Row{
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Created by ${toilet.authorName} on ${toilet.creationDate}",
                    modifier = Modifier
                )

            }

            Row {
                AttributeBadge(icon = Icons.Default.LocalParking, enabled = toilet.parkingNearby, settingsState = settingsState)
                AttributeBadge(icon = Icons.Default.Accessible, enabled = toilet.disabledAccess, settingsState = settingsState)
                AttributeBadge(icon = Icons.Default.BabyChangingStation, enabled = toilet.babyAccess, settingsState = settingsState)
            }
            Text(text = "Currently ${getToiletOpenString(toilet)} (Working hours ${getToiletWorkingHoursString(toilet)})")
            Text(text = getToiletPriceString(toilet) + ", " +  getToiletDistanceString(distanceToToilet) + " away")
        }

    }

}

/**
 * TODO
 *
 * @param toilet
 * @param source
 */
fun placeHolderFunc(toilet: Toilet, source: CurrentDetailsScreen): Unit {

}

/**
 * TODO
 *
 * @param icon
 * @param enabled
 */
@Composable
fun AttributeBadge(icon: ImageVector, enabled: Boolean, settingsState: SettingsState) {

    Icon(
        imageVector = icon,
        contentDescription = icon.name,

        tint = if (settingsState.selectedDarkModeOption == DarkModeStatus.FORCED_ON)
            {if (enabled) Color.White else Color.Gray}
        else if (settingsState.selectedDarkModeOption == DarkModeStatus.FORCED_OFF){
            if (enabled) Color.Black else Color.Gray
        } else {
            if (isSystemInDarkTheme())
            {if (enabled) Color.White else Color.Gray}
            else
            {if (enabled) Color.Black else Color.Gray}
        }
    )
}


