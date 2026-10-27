package com.example.nimbus.data.repository

import com.example.nimbus.data.local.WeatherCacheDao
import com.example.nimbus.data.local.entity.WeatherCacheEntity
import com.example.nimbus.data.mapper.apiValue
import com.example.nimbus.data.mapper.toDomain
import com.example.nimbus.data.remote.ForecastApi
import com.example.nimbus.data.remote.NetworkFactory
import com.example.nimbus.data.remote.dto.ForecastResponseDto
import com.example.nimbus.data.remote.safeApiCall
import com.example.nimbus.domain.model.City
import com.example.nimbus.domain.model.DataResult
import com.example.nimbus.domain.model.TemperatureUnit
import com.example.nimbus.domain.model.Weather
import com.example.nimbus.domain.model.WindSpeedUnit
import com.example.nimbus.domain.model.map
import com.example.nimbus.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.time.Clock
import java.time.Duration
import java.time.Instant

/**
 * "Cache d'abord" : l'écran observe toujours le cache (Room),
 * et [refresh] le met à jour depuis l'API quand c'est nécessaire.
 */
class WeatherRepositoryImpl(
    private val api: ForecastApi,
    private val cacheDao: WeatherCacheDao,
    private val json: Json = NetworkFactory.json,
    /** Horloge injectable : les tests peuvent simuler "35 minutes plus tard". */
    private val clock: Clock = Clock.systemUTC(),
) : WeatherRepository {

    override fun observeWeather(cityId: Long): Flow<Weather?> =
        cacheDao.observe(cityId).map { entry -> entry?.toDomain(json) }

    override suspend fun refresh(
        city: City,
        temperatureUnit: TemperatureUnit,
        windSpeedUnit: WindSpeedUnit,
        force: Boolean,
    ): DataResult<Unit> {
        val now = clock.instant()

        if (!force) {
            val cached = cacheDao.get(city.id)
            if (cached != null &&
                cached.isFresh(now) &&
                cached.temperatureUnit == temperatureUnit.apiValue &&
                cached.windSpeedUnit == windSpeedUnit.apiValue
            ) {
                return DataResult.Success(Unit) // cache récent : pas d'appel réseau
            }
        }

        return safeApiCall {
            val dto = api.forecast(
                latitude = city.latitude,
                longitude = city.longitude,
                temperatureUnit = temperatureUnit.apiValue,
                windSpeedUnit = windSpeedUnit.apiValue,
            )
            // On vérifie que la réponse se convertit bien AVANT de l'enregistrer :
            // une réponse bizarre donne une erreur PARSING au lieu d'écraser un bon cache.
            dto.toDomain(temperatureUnit, windSpeedUnit, now)
            dto
        }.map { dto ->
            cacheDao.upsert(
                WeatherCacheEntity(
                    cityId = city.id,
                    json = json.encodeToString(ForecastResponseDto.serializer(), dto),
                    temperatureUnit = temperatureUnit.apiValue,
                    windSpeedUnit = windSpeedUnit.apiValue,
                    fetchedAt = now.toEpochMilli(),
                ),
            )
        }
    }

    private fun WeatherCacheEntity.isFresh(now: Instant): Boolean =
        Duration.between(Instant.ofEpochMilli(fetchedAt), now) < CACHE_DURATION

    companion object {
        /** Règle métier du cahier des charges : on ne rappelle pas l'API avant 30 minutes. */
        val CACHE_DURATION: Duration = Duration.ofMinutes(30)
    }
}
