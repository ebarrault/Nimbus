package com.example.nimbus.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Table des villes favorites. [position] donne l'ordre d'affichage. */
@Entity(tableName = "favorite_cities")
data class FavoriteCityEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val region: String?,
    val country: String?,
    val latitude: Double,
    val longitude: Double,
    val timezone: String?,
    val position: Int,
)
