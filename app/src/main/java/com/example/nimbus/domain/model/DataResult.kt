package com.example.nimbus.domain.model

/**
 * Résultat d'une opération qui peut échouer (appel réseau…).
 * Le repository renvoie ce type au lieu de laisser remonter des exceptions.
 */
sealed interface DataResult<out T> {
    data class Success<T>(val data: T) : DataResult<T>
    data class Error(val error: DataError) : DataResult<Nothing>
}

enum class DataError {
    /** Pas de connexion, délai dépassé… */
    NETWORK,
    /** Le serveur a répondu avec une erreur (4xx, 5xx). */
    SERVER,
    /** La réponse n'a pas le format attendu. */
    PARSING,
    UNKNOWN,
}

/** Transforme la donnée d'un succès, garde l'erreur telle quelle. */
inline fun <T, R> DataResult<T>.map(transform: (T) -> R): DataResult<R> = when (this) {
    is DataResult.Success -> DataResult.Success(transform(data))
    is DataResult.Error -> this
}
