package com.example.motoagora

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response // <-- IMPORT ADICIONADO AQUI

class MotoRepository {

    private val apiService = RetrofitInstance.api

    suspend fun registerDriver(
        name: RequestBody,
        age: RequestBody,
        maritalStatus: RequestBody,
        cpf: RequestBody,
        phoneNumber: RequestBody,
        cidade: RequestBody,
        email: RequestBody,
        password: RequestBody,
        cnhPhoto: MultipartBody.Part,
        motoDoc: MultipartBody.Part,
        profilePhoto: MultipartBody.Part
    ) = apiService.registerDriver(
        name, age, maritalStatus, cpf, phoneNumber, cidade, email, password, cnhPhoto, motoDoc, profilePhoto
    )

    suspend fun login(loginRequest: LoginRequest) = apiService.login(loginRequest)

    suspend fun getDriversForAdmin(adminId: String) = apiService.getDriversForAdmin(adminId)

    suspend fun approveDriver(adminId: String, driverId: String) = apiService.approveDriver(adminId, driverId)

    suspend fun reproveDriver(adminId: String, driverId: String) = apiService.reproveDriver(adminId, driverId)

    suspend fun createRide(adminId: String, ride: RideModel) = apiService.createRide(adminId, ride)

    suspend fun getAvailableRides() = apiService.getAvailableRides()

    suspend fun stopAllSimulations(adminId: String) = apiService.stopAllSimulations(adminId)

    suspend fun acceptRide(driverId: String, rideId: String): Response<RideModel> {
        val request = AcceptRideRequest(driverId = driverId)
        return apiService.acceptRide(rideId, request)
    }
}
