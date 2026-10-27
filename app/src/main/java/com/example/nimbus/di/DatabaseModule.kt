package com.example.nimbus.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.nimbus.data.local.FavoriteCityDao
import com.example.nimbus.data.local.NimbusDatabase
import com.example.nimbus.data.local.WeatherCacheDao
import com.example.nimbus.data.preferences.settingsDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /** Une seule base pour toute l'app : @Singleton. */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NimbusDatabase =
        NimbusDatabase.create(context)

    @Provides
    fun provideFavoriteCityDao(db: NimbusDatabase): FavoriteCityDao = db.favoriteCityDao()

    @Provides
    fun provideWeatherCacheDao(db: NimbusDatabase): WeatherCacheDao = db.weatherCacheDao()

    @Provides
    @Singleton
    fun provideSettingsDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.settingsDataStore
}
