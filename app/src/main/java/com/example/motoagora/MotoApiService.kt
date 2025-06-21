package com.example.motoagora

import com.google.gson.JsonElement
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response // <-- IMPORT ADICIONADO AQUI
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

data class LoginRequest(val login: String, val password: String)
data class ApiResponse(val message: String)
data class AcceptRideRequest(val driverId: String)

interface MotoApiService {

    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<JsonElement>

    @Multipart
    @POST("register/driver")
    suspend fun registerDriver(
        @Part("name") name: RequestBody,
        @Part("age") age: RequestBody,
        @Part("maritalStatus") maritalStatus: RequestBody,
        @Part("cpf") cpf: RequestBody,
        @Part("phoneNumber") phoneNumber: RequestBody,
        @Part("cidade") cidade: RequestBody,
        @Part("email") email: RequestBody,
        @Part("password") password: RequestBody,
        @Part cnhPhoto: MultipartBody.Part,
        @Part motoDoc: MultipartBody.Part,
        @Part profilePhoto: MultipartBody.Part
    ): Response<ApiResponse>

    // Admin Routes
    @GET("admin/drivers")
    suspend fun getDriversForAdmin(@Header("admin-id") adminId: String): Response<List<UserModel>>

    @POST("admin/drivers/{id}/approve")
    suspend fun approveDriver(
        @Header("admin-id") adminId: String,
        @Path("id") driverId: String
    ): Response<ApiResponse>

    @POST("admin/drivers/{id}/reprove")
    suspend fun reproveDriver(
        @Header("admin-id") adminId: String,
        @Path("id") driverId: String
    ): Response<ApiResponse>

    @POST("admin/create-ride")
    suspend fun createRide(
        @Header("admin-id") adminId: String,
        @Body ride: RideModel
    ): Response<ApiResponse>

    @POST("admin/rides/stop-all")
    suspend fun stopAllSimulations(@Header("admin-id") adminId: String): Response<ApiResponse>

    // Driver Routes
    @GET("driver/rides")
    suspend fun getAvailableRides(): Response<List<RideModel>>

    @POST("driver/rides/{rideId}/accept")
    suspend fun acceptRide(
        @Path("rideId") rideId: String,
        @Body request: AcceptRideRequest
    ): Response<RideModel>
}
