package com.example.nimbus.domain.repository

import com.example.nimbus.domain.model.LocationResult

interface LocationProvider {
    /** Position actuelle du téléphone. Ne demande pas l'autorisation : c'est le rôle de l'écran. */
    suspend fun getCurrentLocation(): LocationResult
}
