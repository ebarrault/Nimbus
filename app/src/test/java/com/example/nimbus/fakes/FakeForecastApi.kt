package com.example.nimbus.fakes

import com.example.nimbus.data.remote.ForecastApi
import com.example.nimbus.data.remote.dto.ForecastResponseDto

/**
 * Fausse API : renvoie une réponse prévue à l'avance, ou lance [error] si on en définit une.
 * Elle compte aussi les appels, pour vérifier quand le repository passe par le réseau.
 */
class FakeForecastApi(
    var response: ForecastResponseDto = TestData.forecastDto,
) : ForecastApi {
    var error: Throwable? = null
    var callCount = 0
        private set
    var lastTemperatureUnit: String? = null
        private set
    var lastWindSpeedUnit: String? = null
        private set

    override suspend fun forecast(
        latitude: Double,
        longitude: Double,
        temperatureUnit: String,
        windSpeedUnit: String,
        current: String,
        hourly: String,
        daily: String,
        timezone: String,
        forecastDays: Int,
    ): ForecastResponseDto {
        callCount++
        lastTemperatureUnit = temperatureUnit
        lastWindSpeedUnit = windSpeedUnit
        error?.let { throw it }
        return response
    }
}
