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
import com.kepper104.toiletseverywhere.data.getToiletOpenString
import com.kepper104.toiletseverywhere.data.getToiletWorkingHours
import com.kepper104.toiletseverywhere.domain.model.Toilet
import com.kepper104.toiletseverywhere.presentation.MainViewModel
import com.kepper104.toiletseverywhere.presentation.ui.state.CurrentDetailsScreen
import com.ramcosta.composedestinations.annotation.Destination

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
            for (toilet in mainViewModel.toiletsState.toiletList){
                item{
                    ToiletCard(toilet = toilet, navigateToDetails = mainViewModel::navigateToDetails)
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}
@Preview
@Composable
fun ToiletCard(toilet: Toilet = Toilet(), navigateToDetails: (toilet: Toilet, source: CurrentDetailsScreen) -> Unit = ::placeHolderFunc ) {
    Column(
        modifier = Modifier
            .clickable {
                navigateToDetails(toilet, CurrentDetailsScreen.LIST)
            }
    ) {
        Row {
//            Text(text = "ID: ${toilet.id}   ")
            Text(
                text = if (toilet.isPublic) "Public toilet" else {"a"}
            )
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
        Text(text = "Currently ${getToiletOpenString(toilet)} - working hours ${getToiletWorkingHours(toilet)}")
        Text(text = if (toilet.cost == 0) "Free" else toilet.cost.toString() + "₽")
        Divider(modifier = Modifier.fillMaxWidth())
    }

}

fun placeHolderFunc(toilet: Toilet, source: CurrentDetailsScreen): Unit {

}

@Composable
fun AttributeBadge(icon: ImageVector, enabled: Boolean) {
    Icon(
        imageVector = icon,
        contentDescription = icon.name,
        tint = if (enabled) Color.White else Color.Gray
    )
    
}