package com.example.nimbus.data.remote

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Construit les clients réseau.
 * Pour l'instant on l'appelle à la main ; l'étape ViewModel le branchera avec Hilt (di/NetworkModule).
 */
object NetworkFactory {

    const val GEOCODING_BASE_URL = "https://geocoding-api.open-meteo.com/"
    const val FORECAST_BASE_URL = "https://api.open-meteo.com/"

    /** Ignore les champs JSON qu'on ne déclare pas dans nos DTO (ex. "elevation", "postcodes"). */
    val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    fun okHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    fun retrofit(baseUrl: String, client: OkHttpClient = okHttpClient()): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    fun geocodingApi(client: OkHttpClient = okHttpClient()): GeocodingApi =
        retrofit(GEOCODING_BASE_URL, client).create(GeocodingApi::class.java)

    fun forecastApi(client: OkHttpClient = okHttpClient()): ForecastApi =
        retrofit(FORECAST_BASE_URL, client).create(ForecastApi::class.java)
}
