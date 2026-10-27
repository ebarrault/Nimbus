package com.example.nimbus.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.nimbus.data.local.entity.WeatherCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherCacheDao {

    @Query("SELECT * FROM weather_cache WHERE cityId = :cityId")
    fun observe(cityId: Long): Flow<WeatherCacheEntity?>

    @Query("SELECT * FROM weather_cache WHERE cityId = :cityId")
    suspend fun get(cityId: Long): WeatherCacheEntity?

    /** Insère, ou remplace si la ville a déjà un cache. */
    @Upsert
    suspend fun upsert(entry: WeatherCacheEntity)

    @Query("DELETE FROM weather_cache WHERE cityId = :cityId")
    suspend fun delete(cityId: Long)
}
