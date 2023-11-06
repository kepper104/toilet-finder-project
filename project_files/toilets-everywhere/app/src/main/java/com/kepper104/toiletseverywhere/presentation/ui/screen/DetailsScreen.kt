package com.kepper104.toiletseverywhere.presentation.ui.screen

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessible
import androidx.compose.material.icons.filled.BabyChangingStation
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarHalf
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.kepper104.toiletseverywhere.data.NOT_LOGGED_IN_STRING
import com.kepper104.toiletseverywhere.data.Tags
import com.kepper104.toiletseverywhere.data.getToiletDistanceMeters
import com.kepper104.toiletseverywhere.data.getToiletDistanceString
import com.kepper104.toiletseverywhere.data.getToiletOpenString
import com.kepper104.toiletseverywhere.data.getToiletWorkingHoursString
import com.kepper104.toiletseverywhere.presentation.MainViewModel
import com.kepper104.toiletseverywhere.presentation.navigation.ConfirmActionAlertDialog
import com.kepper104.toiletseverywhere.presentation.roundDouble
import com.ramcosta.composedestinations.annotation.Destination
import kotlin.math.ceil
import kotlin.math.floor

/**
 * TODO
 *
 */
@Destination
@Composable
fun DetailsScreen() {
    val mainViewModel: MainViewModel = hiltViewModel(LocalContext.current as ComponentActivity)
    Log.d(Tags.CompositionLogger.tag, "Recomposing details")
    Column (
        verticalArrangement = Arrangement.Top,
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ){
        val toiletInfo = mainViewModel.toiletViewDetailsState
        val toilet = toiletInfo.toilet!!

        Text(
            text =
            if (toilet.isPublic) {
                "Public Toilet"
            } else {
                toilet.placeName
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally),
            fontSize = 30.sp
        )

        Text(
            text = "Created by ${toiletInfo.authorName} ${toilet.creationDate}",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        )

        if (mainViewModel.toiletViewDetailsState.allReviewsMenuOpen){
            Spacer(modifier = Modifier.height(10.dp))
            mainViewModel.toiletViewDetailsState.reviews.forEach {
                ReviewCard(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp),
                    authorName = it.userDisplayName,
                    rating = it.rating,
                    text = it.review,
                    textCutoff = null
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
            return
        }



        Row {
            Text(
                text = "${
                    getToiletDistanceString(getToiletDistanceMeters(
                        mainViewModel.mapState.userPosition,
                        toilet.coordinates
                    ))
                    
                } away"
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(imageVector = Icons.Default.StarRate, contentDescription = "Star")
            if (toilet.reviewCount == 0){
                Text(text = "No reviews yet")
            } else {
                Text(text = "${roundDouble(toilet.averageRating)}/5 (${toilet.reviewCount})")
            }
        }
        Text(text = if (toilet.cost == 0) "Free" else "${toilet.cost}₽")

        Text(text = "Working " + getToiletWorkingHoursString(toilet, true))
        Text(text = "Currently ${getToiletOpenString(toilet)}")

        Spacer(modifier = Modifier.height(10.dp))
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

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text =
            if (mainViewModel.toiletViewDetailsState.reviews.isNotEmpty())
                "Most recent review"
            else
                "This toilet has no reviews yet"
        )
        Spacer(modifier = Modifier.height(10.dp))

        Column(
            modifier = Modifier.clickable { mainViewModel.openAllReviews() }
        ){
            val reviewsCount = 1
            val allReviews = mainViewModel.toiletViewDetailsState.reviews
            val reviewsToShow =
                if (allReviews.size <= reviewsCount)
                    allReviews
                else
                    allReviews.subList(0, reviewsCount)

            reviewsToShow.forEach {
                ReviewCard(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp),
                    authorName = it.userDisplayName,
                    rating = it.rating,
                    text = it.review,
                    preview = true,
                    textCutoff = 15
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
        if (mainViewModel.loggedInUserState.currentUserName != NOT_LOGGED_IN_STRING){
            Text(text = "Have you been here? Leave a review!")
            PostReviewCard(
                viewModel = mainViewModel,
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth()
            )
        } else {
            Text(text = "Login in to leave a review!")
        }

        if (mainViewModel.toiletViewDetailsState.reviewPostConfirmationDialogOpen){
            ConfirmActionAlertDialog(
                onDismissRequest = { mainViewModel.closeReviewConfirmationDialog() },
                onConfirmation = { mainViewModel.closeReviewConfirmationDialog(); mainViewModel.postToiletReview() },
                dialogText = "Confirm posting a new toilet review",
                icon = Icons.Default.RateReview
            )
        }

        if (mainViewModel.toiletViewDetailsState.reportDialogOpen){
            ToiletReportDialog(viewModel = mainViewModel)
        }

        

    }

}

@Composable
fun ReviewCard(
    modifier: Modifier = Modifier,
    authorName: String,
    rating: Int,
    text: String?,
    preview: Boolean = false,
    textCutoff: Int? = null
) {
    Card(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Row {
                Text(text = authorName)
                Spacer(modifier = Modifier.weight(1f))
                RatingBar(rating = rating.toDouble())
            }
            if (preview) {
                Row {
                    if (text != null) {
                        Text(
                            text =
                            if (text.length <= textCutoff!!)
                                text
                            else
                                text.subSequence(0, textCutoff - 3).toString() + "..."
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(text = "Click to view all reviews")
                }
            } else {
                if (text != null) {
                    Text(text = text)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostReviewCard(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth()
        ) {
            Text(text = "Select your rating:  ")

            ClickableRatingBar(viewModel = viewModel)

            TextField(
                value = viewModel.toiletViewDetailsState.currentReviewText,
                onValueChange = {
                    viewModel.toiletViewDetailsState =
                        viewModel.toiletViewDetailsState.copy(currentReviewText = it)
                },
                placeholder = { Text(text = "If you want to, leave a text review")},
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(5.dp))

            Row(modifier = Modifier.fillMaxWidth()){
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = { viewModel.showReviewConfirmationDialog() }) {
                    Text(text = "Post review")
                }
            }
        }
    }
}

@Composable
fun RatingBar(
    modifier: Modifier = Modifier,
    rating: Double = 0.0,
    starsColor: Color = Color.Yellow,
) {

    val filledStars = floor(rating).toInt()
    val unfilledStars = (5 - ceil(rating)).toInt()
    val halfStar = !(rating.rem(1).equals(0.0))

    Row(modifier = modifier) {
        repeat(filledStars) {
            Icon(imageVector = Icons.Outlined.Star, contentDescription = null, tint = starsColor)
        }

        if (halfStar) {
            Icon(
                imageVector = Icons.Outlined.StarHalf,
                contentDescription = null,
                tint = starsColor
            )
        }

        repeat(unfilledStars) {
            Icon(imageVector = Icons.Outlined.StarOutline, contentDescription = null, tint = starsColor)
        }
    }
}

@Composable
fun ClickableRatingBar(
    modifier: Modifier = Modifier,
    starsColor: Color = Color.Yellow,
    viewModel: MainViewModel
) {
    val rating = viewModel.toiletViewDetailsState.selectedRating.toDouble()

    Row(modifier = modifier) {
        for (i in 1..5){
            IconButton(onClick = {
                viewModel.toiletViewDetailsState =
                    viewModel.toiletViewDetailsState.copy(selectedRating = i)
            }) {
                Icon(
                    imageVector = if (i <= rating) Icons.Outlined.Star else Icons.Outlined.StarOutline,
                    contentDescription = null,
                    tint = starsColor,
                    modifier = Modifier.scale(2f)
                )
            }
        }
    }
}


@Preview
@Composable
fun ReviewCardPreview() {
    ReviewCard(authorName = "Kirill", rating = 3, text = "Impressive, very nice", textCutoff = 5)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToiletReportDialog(
    viewModel: MainViewModel
) {
    Dialog(onDismissRequest = { viewModel.closeReportDialog() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(375.dp)
                .padding(10.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                Text(
                    text =
                            "You are about to report " +
                            "'${viewModel.toiletViewDetailsState.toilet!!.placeName}' " +
                            "by '${viewModel.toiletViewDetailsState.authorName}', " +
                            "please describe what the problem is",

                    modifier = Modifier.padding(5.dp),
                )
                OutlinedTextField(
                    value = viewModel.toiletViewDetailsState.reportMessage,
                    onValueChange = {
                        viewModel.toiletViewDetailsState =
                            viewModel.toiletViewDetailsState.copy(reportMessage = it)
                    })
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TextButton(
                        onClick = { viewModel.closeReportDialog() },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("Dismiss")
                    }
                    TextButton(
                        onClick = { viewModel.closeReportDialog(); viewModel.sendToiletReport() },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}