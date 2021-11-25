package com.gitlab.juancode.gigigochallenge.service

import com.gitlab.juancode.gigigochallenge.enitiy.DirectionsDTO
import retrofit2.http.GET
import retrofit2.http.Query

interface DirectionApi {

    @GET("directions/json?key=AIzaSyCGVMxXh4H3nPIlBL-J7Z0ot9a_b_KkWmE")
    suspend fun getDataFromDirection(
         @Query("origin") origin:String,
         @Query("destination") destiny:String,
    ): DirectionsDTO
}