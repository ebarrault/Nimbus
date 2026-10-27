package com.example.nimbus.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.nimbus.data.local.entity.FavoriteCityEntity
import com.example.nimbus.data.local.entity.WeatherCacheEntity

@Database(
    entities = [FavoriteCityEntity::class, WeatherCacheEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class NimbusDatabase : RoomDatabase() {

    abstract fun favoriteCityDao(): FavoriteCityDao
    abstract fun weatherCacheDao(): WeatherCacheDao

    companion object {
        /** Crée la base. Hilt s'assurera plus tard qu'il n'en existe qu'une seule. */
        fun create(context: Context): NimbusDatabase =
            Room.databaseBuilder(context, NimbusDatabase::class.java, "nimbus.db").build()
    }
}
