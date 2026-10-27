package com.example.nimbus.data.remote

import com.example.nimbus.data.remote.dto.GeocodingResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

/** API de recherche de villes. Base : https://geocoding-api.open-meteo.com/ */
interface GeocodingApi {

    @GET("v1/search")
    suspend fun search(
        @Query("name") name: String,
        @Query("language") language: String,
        @Query("count") count: Int = 10,
        @Query("format") format: String = "json",
    ): GeocodingResponseDto
}
