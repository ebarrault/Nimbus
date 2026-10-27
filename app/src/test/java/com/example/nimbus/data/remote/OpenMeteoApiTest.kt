package com.example.nimbus.data.remote

import com.example.nimbus.domain.model.DataError
import com.example.nimbus.domain.model.DataResult
import com.example.nimbus.fakes.TestData
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

/**
 * Teste les vraies interfaces Retrofit contre un faux serveur HTTP local (MockWebServer) :
 * on vérifie l'URL envoyée et la lecture de la réponse, sans dépendre d'Internet.
 */
class OpenMeteoApiTest {

    private lateinit var server: MockWebServer
    private lateinit var forecastApi: ForecastApi
    private lateinit var geocodingApi: GeocodingApi

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        // Délai court pour que le test "délai dépassé" ne dure pas 15 secondes
        val client = OkHttpClient.Builder().readTimeout(1, TimeUnit.SECONDS).build()
        val retrofit = NetworkFactory.retrofit(server.url("/").toString(), client)
        forecastApi = retrofit.create(ForecastApi::class.java)
        geocodingApi = retrofit.create(GeocodingApi::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun forecast_reponse200_estLue() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(TestData.forecastJson))

        val dto = forecastApi.forecast(48.85, 2.35, "celsius", "kmh")

        assertEquals("Europe/Paris", dto.timezone)
        assertEquals(18.2, dto.current.temperature, 0.0)
        assertEquals(4, dto.hourly.time.size)
    }

    @Test
    fun forecast_envoieLesBonsParametres() = runTest {
        server.enqueue(MockResponse().setBody(TestData.forecastJson))

        forecastApi.forecast(48.85, 2.35, temperatureUnit = "fahrenheit", windSpeedUnit = "ms")

        val url = server.takeRequest().requestUrl!!
        assertEquals("/v1/forecast", url.encodedPath)
        assertEquals("48.85", url.queryParameter("latitude"))
        assertEquals("fahrenheit", url.queryParameter("temperature_unit"))
        assertEquals("ms", url.queryParameter("wind_speed_unit"))
        assertEquals("auto", url.queryParameter("timezone"))
        assertEquals("7", url.queryParameter("forecast_days"))
        assertTrue(url.queryParameter("current")!!.contains("weather_code"))
    }

    @Test
    fun forecast_erreur500_donneServer() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))

        val result = safeApiCall { forecastApi.forecast(48.85, 2.35, "celsius", "kmh") }

        assertEquals(DataResult.Error(DataError.SERVER), result)
    }

    @Test
    fun forecast_jsonInvalide_donneParsing() = runTest {
        server.enqueue(MockResponse().setBody("{ pas du json"))

        val result = safeApiCall { forecastApi.forecast(48.85, 2.35, "celsius", "kmh") }

        assertEquals(DataResult.Error(DataError.PARSING), result)
    }

    @Test
    fun forecast_champObligatoireManquant_donneParsing() = runTest {
        server.enqueue(MockResponse().setBody("""{ "latitude": 48.85 }"""))

        val result = safeApiCall { forecastApi.forecast(48.85, 2.35, "celsius", "kmh") }

        assertEquals(DataResult.Error(DataError.PARSING), result)
    }

    @Test
    fun forecast_delaiDepasse_donneNetwork() = runTest {
        server.enqueue(MockResponse().setBody(TestData.forecastJson).setHeadersDelay(3, TimeUnit.SECONDS))

        val result = safeApiCall { forecastApi.forecast(48.85, 2.35, "celsius", "kmh") }

        assertEquals(DataResult.Error(DataError.NETWORK), result)
    }

    @Test
    fun geocoding_aucuneVille_resultsEstNull() = runTest {
        // Quand rien ne correspond, l'API ne renvoie pas le champ "results"
        server.enqueue(MockResponse().setBody("""{ "generationtime_ms": 0.2 }"""))

        val response = geocodingApi.search(name = "zzzz", language = "fr")

        assertNull(response.results)
    }

    @Test
    fun geocoding_lectureDesVilles_etParametres() = runTest {
        server.enqueue(
            MockResponse().setBody(
                """
                { "results": [
                    { "id": 2988358, "name": "Pau", "latitude": 43.31117, "longitude": -0.35583,
                      "elevation": 219.0, "country": "France", "admin1": "Nouvelle-Aquitaine",
                      "timezone": "Europe/Paris", "postcodes": ["64000"] }
                ] }
                """.trimIndent(),
            ),
        )

        val response = geocodingApi.search(name = "Pau", language = "fr")

        val pau = response.results!!.single()
        assertEquals("Pau", pau.name)
        assertEquals("Nouvelle-Aquitaine", pau.region)
        val url = server.takeRequest().requestUrl!!
        assertEquals("/v1/search", url.encodedPath)
        assertEquals("Pau", url.queryParameter("name"))
        assertEquals("fr", url.queryParameter("language"))
    }
}
