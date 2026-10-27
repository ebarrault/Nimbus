package com.example.nimbus.domain.model

/** Résultat d'une demande de position du téléphone. */
sealed interface LocationResult {
    data class Success(val latitude: Double, val longitude: Double) : LocationResult
    /** L'utilisateur n'a pas donné l'autorisation. */
    data object PermissionDenied : LocationResult
    /** La localisation est désactivée dans les paramètres du téléphone. */
    data object LocationDisabled : LocationResult
    /** Position introuvable ou erreur des services Google. */
    data object Unavailable : LocationResult
}
