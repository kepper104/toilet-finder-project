package com.kepper104.toiletseverywhere.presentation.ui.screen

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.kepper104.toiletseverywhere.data.Tags
import com.kepper104.toiletseverywhere.data.getToiletDistanceMeters
import com.kepper104.toiletseverywhere.data.getToiletDistanceString
import com.kepper104.toiletseverywhere.data.getToiletNameString
import com.kepper104.toiletseverywhere.data.getToiletOpenString
import com.kepper104.toiletseverywhere.data.getToiletPriceString
import com.kepper104.toiletseverywhere.data.getToiletStatusColor
import com.kepper104.toiletseverywhere.presentation.MainViewModel
import com.kepper104.toiletseverywhere.presentation.ui.state.CurrentDetailsScreen
import com.ramcosta.composedestinations.annotation.Destination


/**
 * TODO
 *
 */
@Destination
@Composable
fun MapScreen(

) {
    val mainViewModel: MainViewModel = hiltViewModel(LocalContext.current as ComponentActivity)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(mainViewModel.scaffoldPadding),
    ) {

        if (mainViewModel.toiletViewDetailsState.currentDetailScreen == CurrentDetailsScreen.MAP){
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

        if (mainViewModel.newToiletDetailsState.enabled){
            BackHandler (
                onBack = {Log.d("BackLogger", "Handled back from MAP"); mainViewModel.leaveNewToiletDetailsScreen()}
            )
            NewToiletDetailsScreen()
            return
        }

        Log.d(Tags.CompositionLogger.toString(), "Composing map!, mapstate prop is ${mainViewModel.mapState.properties.mapStyleOptions.toString()}")


        GoogleMap(
            modifier = Modifier
                .fillMaxSize(),
            properties = mainViewModel.mapState.properties,
            uiSettings = mainViewModel.mapState.mapUiSettings,
            cameraPositionState = mainViewModel.mapState.cameraPosition,
            onMapLongClick = {
                if (mainViewModel.mapState.addingToilet){
                    Log.d("ToiletAddLogger", "Adding toilet: $it")
                }
            }
        ) {

            Marker(
                state = MarkerState(position = mainViewModel.mapState.cameraPosition.position.target),
                visible = mainViewModel.mapState.addingToilet,
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)
            )


            for (marker in mainViewModel.toiletsState.toiletMarkerList){
                val curToilet = marker.toilet
                Marker(
                    state = MarkerState(position = marker.position),
                    title = getToiletNameString(curToilet),
                    icon = getToiletStatusColor(curToilet),
                    snippet = "${getToiletOpenString(curToilet)}, " +
                            "${getToiletPriceString(curToilet)}, " +
                            getToiletDistanceString(
                        getToiletDistanceMeters(mainViewModel.mapState.userPosition, marker.position)
                    ),
                    onInfoWindowClick = {mainViewModel.navigateToDetails(curToilet, CurrentDetailsScreen.MAP)}
                )
            }
        }
    }
}
