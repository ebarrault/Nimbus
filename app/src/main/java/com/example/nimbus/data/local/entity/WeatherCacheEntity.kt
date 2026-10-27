package com.example.nimbus.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Dernière météo téléchargée pour une ville (une ligne par ville).
 * On stocke la réponse JSON de l'API telle quelle : simple, et le format reste celui des DTO.
 */
@Entity(tableName = "weather_cache")
data class WeatherCacheEntity(
    @PrimaryKey val cityId: Long,
    val json: String,
    /** Unités dans lesquelles la réponse a été demandée ("celsius", "kmh"…). */
    val temperatureUnit: String,
    val windSpeedUnit: String,
    /** Date du téléchargement, en millisecondes depuis 1970. */
    val fetchedAt: Long,
)
