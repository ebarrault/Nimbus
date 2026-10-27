package com.example.nimbus.domain.repository

import com.example.nimbus.domain.model.City
import com.example.nimbus.domain.model.DataResult
import com.example.nimbus.domain.model.TemperatureUnit
import com.example.nimbus.domain.model.Weather
import com.example.nimbus.domain.model.WindSpeedUnit
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {

    /**
     * Observe la météo en cache d'une ville.
     * Émet null s'il n'y a rien en cache, puis une nouvelle valeur à chaque rafraîchissement.
     */
    fun observeWeather(cityId: Long): Flow<Weather?>

    /**
     * Télécharge la météo et met à jour le cache.
     * Sans [force], ne fait rien si le cache a moins de 30 minutes et est dans les bonnes unités.
     */
    suspend fun refresh(
        city: City,
        temperatureUnit: TemperatureUnit,
        windSpeedUnit: WindSpeedUnit,
        force: Boolean = false,
    ): DataResult<Unit>
}
