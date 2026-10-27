package com.example.nimbus.data.repository

import com.example.nimbus.data.local.FavoriteCityDao
import com.example.nimbus.data.mapper.toDomain
import com.example.nimbus.data.mapper.toEntity
import com.example.nimbus.data.remote.GeocodingApi
import com.example.nimbus.data.remote.safeApiCall
import com.example.nimbus.domain.model.City
import com.example.nimbus.domain.model.DataResult
import com.example.nimbus.domain.model.map
import com.example.nimbus.domain.repository.CityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale

class CityRepositoryImpl(
    private val api: GeocodingApi,
    private val favoriteDao: FavoriteCityDao,
    /** Langue des noms de villes renvoyés par l'API ("fr", "en"…) : celle du téléphone. */
    private val language: () -> String = { Locale.getDefault().language },
) : CityRepository {

    override suspend fun search(query: String): DataResult<List<City>> {
        val text = query.trim()
        if (text.length < MIN_QUERY_LENGTH) return DataResult.Success(emptyList())

        return safeApiCall { api.search(name = text, language = language()) }
            .map { response -> response.results.orEmpty().map { it.toDomain() } }
    }

    override fun observeFavorites(): Flow<List<City>> =
        favoriteDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun addFavorite(city: City) {
        favoriteDao.insertAtEnd(city.toEntity(position = 0)) // la vraie position est calculée par le DAO
    }

    override suspend fun removeFavorite(cityId: Long) {
        favoriteDao.deleteById(cityId)
    }

    override suspend fun reorderFavorites(orderedIds: List<Long>) {
        favoriteDao.reorder(orderedIds)
    }

    companion object {
        /** Règle métier : la recherche ne part qu'à partir de 2 caractères. */
        const val MIN_QUERY_LENGTH = 2
    }
}
