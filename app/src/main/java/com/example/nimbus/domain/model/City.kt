package com.example.nimbus.domain.model

/**
 * Une ville.
 * [id] est l'identifiant renvoyé par l'API de géocodage d'Open-Meteo : il est stable,
 * on l'utilise donc aussi comme clé pour les favoris et le cache météo.
 */
data class City(
    val id: Long,
    val name: String,
    val region: String?,
    val country: String?,
    val latitude: Double,
    val longitude: Double,
    val timezone: String?,
) {
    companion object {
        /** Identifiant réservé à "Ma position" (jamais renvoyé par l'API). */
        const val CURRENT_LOCATION_ID = -1L
    }
}
