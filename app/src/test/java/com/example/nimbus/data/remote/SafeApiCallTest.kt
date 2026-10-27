package com.example.nimbus.data.remote

import com.example.nimbus.domain.model.DataError
import com.example.nimbus.domain.model.DataResult
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import kotlin.coroutines.cancellation.CancellationException

class SafeApiCallTest {

    @Test
    fun succes_renvoieLaDonnee() = runTest {
        assertEquals(DataResult.Success(42), safeApiCall { 42 })
    }

    @Test
    fun pasDeReseau_ouDelaiDepasse_donneNetwork() = runTest {
        assertEquals(DataResult.Error(DataError.NETWORK), safeApiCall { throw IOException() })
        assertEquals(DataResult.Error(DataError.NETWORK), safeApiCall { throw SocketTimeoutException() })
    }

    @Test
    fun erreurHttp_donneServer() = runTest {
        val response = Response.error<Any>(500, "".toResponseBody(null))
        assertEquals(DataResult.Error(DataError.SERVER), safeApiCall { throw HttpException(response) })
    }

    @Test
    fun jsonInvalide_donneParsing() = runTest {
        assertEquals(DataResult.Error(DataError.PARSING), safeApiCall { throw SerializationException("oups") })
    }

    @Test
    fun autreErreur_donneUnknown() = runTest {
        assertEquals(DataResult.Error(DataError.UNKNOWN), safeApiCall { throw IllegalStateException() })
    }

    @Test
    fun annulation_nEstPasAvalee() = runTest {
        try {
            safeApiCall { throw CancellationException("annulé") }
            fail("La CancellationException aurait dû être relancée")
        } catch (e: CancellationException) {
            // attendu : annuler une coroutine ne doit jamais se transformer en "erreur réseau"
        }
    }
}
