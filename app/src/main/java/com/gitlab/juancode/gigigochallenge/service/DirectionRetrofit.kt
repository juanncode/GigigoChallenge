package com.gitlab.juancode.gigigochallenge.service

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object DirectionRetrofit {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://maps.googleapis.com/maps/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service: DirectionApi = retrofit.create(DirectionApi::class.java)
}