package com.example.nimbus.data.remote

import com.example.nimbus.domain.model.DataError
import com.example.nimbus.domain.model.DataResult
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.time.format.DateTimeParseException
import kotlin.coroutines.cancellation.CancellationException

/**
 * Exécute un appel réseau et transforme les exceptions en [DataResult.Error].
 * Ainsi l'app ne plante jamais à cause du réseau.
 */
suspend fun <T> safeApiCall(block: suspend () -> T): DataResult<T> = try {
    DataResult.Success(block())
} catch (e: CancellationException) {
    throw e // ne jamais avaler l'annulation d'une coroutine
} catch (e: HttpException) {
    DataResult.Error(DataError.SERVER)
} catch (e: SerializationException) {
    DataResult.Error(DataError.PARSING)
} catch (e: DateTimeParseException) {
    DataResult.Error(DataError.PARSING) // date dans un format inattendu
} catch (e: IllegalArgumentException) {
    DataResult.Error(DataError.PARSING) // JSON invalide ou champ manquant
} catch (e: IOException) {
    DataResult.Error(DataError.NETWORK) // pas de réseau, délai dépassé…
} catch (e: Exception) {
    DataResult.Error(DataError.UNKNOWN)
}
