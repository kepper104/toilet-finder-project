package com.kepper104.toiletseverywhere.presentation.ui.screen

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Divider
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
import com.kepper104.toiletseverywhere.presentation.ui.state.CurrentDetailsScreen
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



    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(mainViewModel.scaffoldPadding),
    ) {

        if (mainViewModel.toiletViewDetailsState.currentDetailScreen == CurrentDetailsScreen.LIST){
            BackHandler (
                onBack = {Log.d("BackLogger", "Handled back from MAP"); mainViewModel.leaveToiletViewDetailsScreen()}
            )
            DetailsScreen()
            return
        }

        Log.d(Tags.CompositionLogger.toString(), "Composing list!")
//        Text(text = "List Screen")
        
        LazyColumn(

        ){
            for (toilet in mainViewModel.filterState.filteredToilets){
                item{
                    ToiletCard(toilet = toilet, navigateToDetails = mainViewModel::navigateToDetails, getToiletDistanceMeters(mainViewModel.mapState.userPosition, toilet.coordinates))
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
@Preview
@Composable
fun ToiletCard(toilet: Toilet = Toilet(), navigateToDetails: (toilet: Toilet, source: CurrentDetailsScreen) -> Unit = ::placeHolderFunc, distanceToToilet: Int = 100) {
    Column(
        modifier = Modifier
            .clickable {
                navigateToDetails(toilet, CurrentDetailsScreen.LIST)
            }
    ) {
        Text(
            text = getToiletNameString(toilet)
        )

        Row{
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Created by ${toilet.authorName} on ${toilet.creationDate}",
                modifier = Modifier
            )

        }

        Row {
            AttributeBadge(icon = Icons.Default.LocalParking, enabled = toilet.parkingNearby)
            AttributeBadge(icon = Icons.Default.Accessible, enabled = toilet.disabledAccess)
            AttributeBadge(icon = Icons.Default.BabyChangingStation, enabled = toilet.babyAccess)
        }
        Text(text = "Currently ${getToiletOpenString(toilet)} (Working hours ${getToiletWorkingHoursString(toilet)})")
        Text(text = getToiletPriceString(toilet) + ", " +  getToiletDistanceString(distanceToToilet) + " away")
        Divider(modifier = Modifier.fillMaxWidth())
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
fun AttributeBadge(icon: ImageVector, enabled: Boolean) {
    Icon(
        imageVector = icon,
        contentDescription = icon.name,
        tint = if (enabled) Color.White else Color.Gray
    )
    
}