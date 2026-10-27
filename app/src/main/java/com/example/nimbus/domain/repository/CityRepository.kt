package com.example.nimbus.domain.repository

import com.example.nimbus.domain.model.City
import com.example.nimbus.domain.model.DataResult
import kotlinx.coroutines.flow.Flow

interface CityRepository {

    /** Cherche des villes par leur nom. Renvoie une liste vide en dessous de 2 caractères. */
    suspend fun search(query: String): DataResult<List<City>>

    /** Les villes favorites, dans l'ordre choisi par l'utilisateur. */
    fun observeFavorites(): Flow<List<City>>

    /** Ajoute une ville à la fin des favoris (sans effet si elle y est déjà). */
    suspend fun addFavorite(city: City)

    suspend fun removeFavorite(cityId: Long)

    /** Enregistre le nouvel ordre des favoris. */
    suspend fun reorderFavorites(orderedIds: List<Long>)
}
