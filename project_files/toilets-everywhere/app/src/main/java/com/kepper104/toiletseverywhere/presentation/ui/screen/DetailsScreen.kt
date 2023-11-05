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
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kepper104.toiletseverywhere.data.Tags
import com.kepper104.toiletseverywhere.data.getToiletDistanceMeters
import com.kepper104.toiletseverywhere.data.getToiletOpenString
import com.kepper104.toiletseverywhere.data.getToiletWorkingHoursString
import com.kepper104.toiletseverywhere.presentation.MainViewModel
import com.kepper104.toiletseverywhere.presentation.navigation.ConfirmActionAlertDialog
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

        if (mainViewModel.toiletViewDetailsState.allReviewsMenuOpen){
            mainViewModel.toiletViewDetailsState.reviews.forEach {
                Log.d(Tags.TempLogger.tag, it.review.toString())
                ReviewCard(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp),
                    authorName = it.userDisplayName,
                    rating = it.rating,
                    text = it.review
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
            return
        }



        Row {
            Text(
                text = "${
                    getToiletDistanceMeters(
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

        Text(text = "Working " + getToiletWorkingHoursString(toilet, true))
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

        Text(text = "Most recent reviews")

        Column(
            modifier = Modifier.clickable { mainViewModel.openAllReviews() }
        ){
            val allReviews = mainViewModel.toiletViewDetailsState.reviews
            val reviewsToShow = if (allReviews.size <= 1) allReviews else allReviews.subList(0, 1)
            reviewsToShow.forEach {
                Log.d(Tags.TempLogger.tag, it.review.toString())
                ReviewCard(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp),
                    authorName = it.userDisplayName,
                    rating = it.rating,
                    text = it.review
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

//        }
        Text(text = "Have you been here? Leave a review!")
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 30.dp)
//                .border(10.dp, Color.Gray, shape = RoundedCornerShape(3.dp))
//        ) {
//            RatingBar(clickable = true)
//        }
        PostReviewCard(viewModel = mainViewModel, modifier = Modifier.padding(10.dp).fillMaxWidth())

//        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = { mainViewModel.moveCameraToToiletLocation(toilet = toilet) }) {
            Text(text = "Go to map")
        }

        if (mainViewModel.toiletViewDetailsState.reviewPostConfirmationDialogOpen){
            ConfirmActionAlertDialog(
                onDismissRequest = { mainViewModel.toiletViewDetailsState = mainViewModel.toiletViewDetailsState.copy(reviewPostConfirmationDialogOpen = false) },
                onConfirmation = { /*TODO*/ },
                dialogText = "Confirm posting a new toilet review",
                icon = Icons.Default.RateReview
            )
        }
        

    }

}

@Composable
fun ReviewCard(modifier: Modifier = Modifier, authorName: String, rating: Int, text: String?) {
    Card(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Row{
                Text(text = authorName)
                Spacer(modifier = Modifier.weight(1f))
                RatingBar(rating = rating.toDouble())
            }
            if (text != null){
                Text(text = text)

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
            modifier = Modifier.padding(10.dp).fillMaxWidth()
        ) {
            Text(text = "Select your rating:  ")

            ClickableRatingBar(viewModel = viewModel)

            TextField(
                value = viewModel.toiletViewDetailsState.currentReviewText,
                onValueChange = {
                    viewModel.toiletViewDetailsState =
                        viewModel.toiletViewDetailsState.copy(currentReviewText = it)
                },
                placeholder = { Text(text = "If you want to, briefly describe your experience here")}
            )
            Button(onClick = { viewModel.postToiletReview() }) {
                Text(text = "Post review")
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
                    viewModel.toiletViewDetailsState.copy(selectedRating = i); Log.d(Tags.TempLogger.tag, i.toString())
            }) {
                Icon(imageVector = if (i <= rating) Icons.Outlined.Star else Icons.Outlined.StarOutline, contentDescription = null, tint = starsColor, modifier = Modifier.scale(2f))
            }
        }
    }
}


@Preview
@Composable
fun ReviewCardPreview() {
    ReviewCard(authorName = "Kirill", rating = 3, text = "Impressive, very nice")
}

//@Preview
//@Composable
//fun LeaveReviewCardPreview() {
//    LeaveReviewCard()
//}