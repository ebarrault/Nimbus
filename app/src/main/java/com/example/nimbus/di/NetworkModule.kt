package com.example.nimbus.di

import com.example.nimbus.data.remote.ForecastApi
import com.example.nimbus.data.remote.GeocodingApi
import com.example.nimbus.data.remote.NetworkFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /** Un seul client HTTP pour toute l'app (il garde les connexions ouvertes). */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = NetworkFactory.okHttpClient()

    @Provides
    @Singleton
    fun provideGeocodingApi(client: OkHttpClient): GeocodingApi = NetworkFactory.geocodingApi(client)

    @Provides
    @Singleton
    fun provideForecastApi(client: OkHttpClient): ForecastApi = NetworkFactory.forecastApi(client)
}
