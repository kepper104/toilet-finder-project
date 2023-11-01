package com.kepper104.toiletseverywhere.data.api

import com.kepper104.toiletseverywhere.domain.model.ApiToilet
import com.kepper104.toiletseverywhere.domain.model.ApiUser
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * TODO
 *
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

}

/**
 * TODO
 *
 * @property login
 * @property password
 */
data class LoginData(
    val login: String,
    val password: String
)

/**
 * TODO
 *
 * @property login
 * @property password
 * @property display_name
 */
data class RegisterData(
    val login: String,
    val password: String,
    val display_name: String
)

/**
 * TODO
 *
 * @property id_
 * @property display_name_
 * @property creation_date_
 */
data class LoginResponse(
    val id_: Int,
    val display_name_: String,
    val creation_date_: String
)

/**
 * TODO
 *
 * @property UserExists
 */
data class LoginCheckResponse(
    val UserExists: Boolean
)

/**
 * TODO
 *
 * @property Message
 */
data class MessageResponse(
    val Message: String
)






// TODO change json variable names to kotlin conventions and add @JSON stuff for incoming json names
// RESPONSES AND API STUFF

// LoginResponse(id_=5, display_name_=Keril, creation_date_=2023-08-24)


// if correct  Response{protocol=http/1.1, code=200, message=OK, url=http://kepper104.fun:5010/users/login}, true, LoginResponse(result=true)
// else  Response{protocol=http/1.1, code=404, message=NOT FOUND, url=http://kepper104.fun:5010/users/login}, false, null