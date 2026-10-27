package com.example.nimbus.data.preferences

import com.example.nimbus.domain.model.City
import kotlinx.serialization.Serializable

/** Version "enregistrable" d'une ville : DataStore la stocke en JSON. */
@Serializable
data class StoredCity(
    val id: Long,
    val name: String,
    val region: String? = null,
    val country: String? = null,
    val latitude: Double,
    val longitude: Double,
    val timezone: String? = null,
)

fun City.toStored() = StoredCity(id, name, region, country, latitude, longitude, timezone)

fun StoredCity.toDomain() = City(id, name, region, country, latitude, longitude, timezone)
