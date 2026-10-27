package com.example.nimbus.data.remote

import com.example.nimbus.data.remote.dto.ForecastResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

/** API de prévisions. Base : https://api.open-meteo.com/ */
interface ForecastApi {

    @GET("v1/forecast")
    suspend fun forecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("temperature_unit") temperatureUnit: String,
        @Query("wind_speed_unit") windSpeedUnit: String,
        @Query("current") current: String = CURRENT_FIELDS,
        @Query("hourly") hourly: String = HOURLY_FIELDS,
        @Query("daily") daily: String = DAILY_FIELDS,
        @Query("timezone") timezone: String = "auto",
        @Query("forecast_days") forecastDays: Int = 7,
    ): ForecastResponseDto

    companion object {
        const val CURRENT_FIELDS =
            "temperature_2m,apparent_temperature,relative_humidity_2m,weather_code,wind_speed_10m,is_day"
        const val HOURLY_FIELDS = "temperature_2m,precipitation_probability,weather_code"
        const val DAILY_FIELDS =
            "temperature_2m_max,temperature_2m_min,precipitation_probability_max,weather_code,sunrise,sunset"
    }
}
