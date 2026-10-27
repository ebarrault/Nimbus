package com.example.nimbus.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Réponse de https://geocoding-api.open-meteo.com/v1/search
 * Quand aucune ville ne correspond, l'API ne renvoie pas le champ "results" : d'où le null par défaut.
 */
@Serializable
data class GeocodingResponseDto(
    val results: List<GeocodingResultDto>? = null,
)

@Serializable
data class GeocodingResultDto(
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String? = null,
    @SerialName("admin1") val region: String? = null,
    val timezone: String? = null,
)
