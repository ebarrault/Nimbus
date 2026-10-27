package com.example.nimbus.fakes

import com.example.nimbus.data.local.FavoriteCityDao
import com.example.nimbus.data.local.entity.FavoriteCityEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * Favoris en mémoire. insertAtEnd() et reorder() ne sont pas redéfinis :
 * ce sont les vraies implémentations de l'interface FavoriteCityDao qui tournent.
 */
class FakeFavoriteCityDao : FavoriteCityDao {
    private val cities = MutableStateFlow<List<FavoriteCityEntity>>(emptyList())

    override fun observeAll(): Flow<List<FavoriteCityEntity>> = cities.map { list -> list.sortedBy { it.position } }

    override suspend fun maxPosition(): Int? = cities.value.maxOfOrNull { it.position }

    override suspend fun insert(city: FavoriteCityEntity) = cities.update { list ->
        if (list.any { it.id == city.id }) list else list + city // comme OnConflictStrategy.IGNORE
    }

    override suspend fun deleteById(id: Long) = cities.update { list -> list.filterNot { it.id == id } }

    override suspend fun updatePosition(id: Long, position: Int) = cities.update { list ->
        list.map { if (it.id == id) it.copy(position = position) else it }
    }
}
