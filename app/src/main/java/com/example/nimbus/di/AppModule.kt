package com.example.nimbus.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /** L'heure réelle. Les tests fournissent une fausse horloge à la place. */
    @Provides
    @Singleton
    fun provideClock(): Clock = Clock.systemUTC()
}
