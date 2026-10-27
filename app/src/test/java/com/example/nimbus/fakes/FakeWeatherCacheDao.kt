package com.example.nimbus.fakes

import com.example.nimbus.data.local.WeatherCacheDao
import com.example.nimbus.data.local.entity.WeatherCacheEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** Cache en mémoire qui se comporte comme le DAO Room (le Flow réémet à chaque changement). */
class FakeWeatherCacheDao : WeatherCacheDao {
    private val entries = MutableStateFlow<Map<Long, WeatherCacheEntity>>(emptyMap())

    override fun observe(cityId: Long): Flow<WeatherCacheEntity?> = entries.map { it[cityId] }
    override suspend fun get(cityId: Long): WeatherCacheEntity? = entries.value[cityId]
    override suspend fun upsert(entry: WeatherCacheEntity) = entries.update { it + (entry.cityId to entry) }
    override suspend fun delete(cityId: Long) = entries.update { it - cityId }
}
