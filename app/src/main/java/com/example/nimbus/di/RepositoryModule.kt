package com.example.nimbus.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.nimbus.data.local.FavoriteCityDao
import com.example.nimbus.data.local.WeatherCacheDao
import com.example.nimbus.data.location.FusedLocationProvider
import com.example.nimbus.data.preferences.DataStoreSettingsRepository
import com.example.nimbus.data.remote.ForecastApi
import com.example.nimbus.data.remote.GeocodingApi
import com.example.nimbus.data.repository.CityRepositoryImpl
import com.example.nimbus.data.repository.WeatherRepositoryImpl
import com.example.nimbus.domain.repository.CityRepository
import com.example.nimbus.domain.repository.LocationProvider
import com.example.nimbus.domain.repository.SettingsRepository
import com.example.nimbus.domain.repository.WeatherRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton

/**
 * Relie chaque interface du domaine à son implémentation dans data/.
 * Les ViewModels demandent un WeatherRepository ; Hilt leur donne un WeatherRepositoryImpl.
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideWeatherRepository(api: ForecastApi, dao: WeatherCacheDao, clock: Clock): WeatherRepository =
        WeatherRepositoryImpl(api = api, cacheDao = dao, clock = clock)

    @Provides
    @Singleton
    fun provideCityRepository(api: GeocodingApi, dao: FavoriteCityDao): CityRepository =
        CityRepositoryImpl(api = api, favoriteDao = dao)

    @Provides
    @Singleton
    fun provideSettingsRepository(dataStore: DataStore<Preferences>): SettingsRepository =
        DataStoreSettingsRepository(dataStore)

    @Provides
    @Singleton
    fun provideLocationProvider(@ApplicationContext context: Context): LocationProvider =
        FusedLocationProvider(context)
}
