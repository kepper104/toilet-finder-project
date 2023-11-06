package com.kepper104.toiletseverywhere.data.api

import com.kepper104.toiletseverywhere.domain.model.ApiReview
import com.kepper104.toiletseverywhere.domain.model.ApiToilet
import com.kepper104.toiletseverywhere.domain.model.ApiUser
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Interface for retrofit api framework to interact with python serverside backend,
 * needs to be injected by Dagger Hilt.
 */
interface MainApi {

    @GET("/toilets")
    suspend fun getToilets(): Response<List<ApiToilet>>

    @GET("/toilets/{id}")
    suspend fun getToiletById(@Path("id") toiletId: Int): Response<ApiToilet>

    @POST("/toilets")
    suspend fun createToilet(@Body newToiletData: ApiToilet): Response<MessageResponse>

    @POST("/users/login")
    suspend fun loginUser(@Body loginData: LoginData): Response<LoginResponse>

    @GET("/user_exists/{login}")
    suspend fun checkLogin(@Path("login") userLogin: String): Response<LoginCheckResponse>

    @POST("/users")
    suspend fun registerUser(@Body registerData: RegisterData): Response<MessageResponse>

    @GET("/users/{id}")
    suspend fun getUserById(@Path("id") userId: Int): Response<ApiUser>

    @POST("/users/change_name")
    suspend fun changeDisplayName(@Body displayNameUpdateData: DisplayNameUpdateData): Response<MessageResponse>

    @POST("/reviews")
    suspend fun postToiletReview(@Body reviewData: ToiletReviewData): Response<MessageResponse>

    @GET("/reviews/{id}")
    suspend fun getReviewsById(@Path("id") toiletId: Int): Response<List<ApiReview>>

    @POST("/toilets/report")
    suspend fun sendToiletReport(@Body toiletReportData: ToiletReportData): Response<MessageResponse>
}

/**
 * Request data to be sent to log in. [login] and [password] are both required.
 */
data class LoginData(
    val login: String,
    val password: String
)

/**
 * Request data to be sent to register a new user.
 * Availability verified [login], [display_name] and [password] are all required.
 */
data class RegisterData(
    val login: String,
    val password: String,
    val display_name: String
)

/**
 * Response data for view-model that contains account's [id_], [display_name_] and [creation_date_]
 * if the login + password have been correctly verified.
 */
data class LoginResponse(
    val id_: Int,
    val display_name_: String,
    val creation_date_: String
)

/**
 * Response data that contains information about given login availability.
 * [UserExists] is a [Boolean]
 */
data class LoginCheckResponse(
    val UserExists: Boolean
)

/**
 * Response that contains an info [Message] with text for humans
 * that is sent when request was successful.
 */
data class MessageResponse(
    val Message: String
)

/**
 * Request data used for changing user's display name.
 * Current [user_id] and desired [new_name] are required.
 */
data class DisplayNameUpdateData(
    val user_id: Int,
    val new_name: String
)

/**
 * Request data used for posting a new toilet review.
 * [toilet_id_], author's [user_id_], selected [rating_] and optional [review_text_] are required.
 */
data class ToiletReviewData(
    val toilet_id_: Int,
    val user_id_: Int,
    val rating_: Int,
    val review_text_: String?,
)

/**
 * Request data used for reporting toilet misinformation.
 * Requires current [user_id_], [toilet_id_] and [message_]
 */
data class ToiletReportData(
    val user_id_: Int,
    val toilet_id_: Int,
    val message_: String
)
