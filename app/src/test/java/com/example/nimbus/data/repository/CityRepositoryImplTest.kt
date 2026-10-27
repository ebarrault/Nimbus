package com.example.nimbus.data.repository

import com.example.nimbus.data.remote.dto.GeocodingResponseDto
import com.example.nimbus.data.remote.dto.GeocodingResultDto
import com.example.nimbus.domain.model.DataError
import com.example.nimbus.domain.model.DataResult
import com.example.nimbus.fakes.FakeFavoriteCityDao
import com.example.nimbus.fakes.FakeGeocodingApi
import com.example.nimbus.fakes.TestData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.IOException

class CityRepositoryImplTest {

    private lateinit var api: FakeGeocodingApi
    private lateinit var dao: FakeFavoriteCityDao
    private lateinit var repository: CityRepositoryImpl

    private val paris = TestData.paris
    private val pau = TestData.pau

    @Before
    fun setUp() {
        api = FakeGeocodingApi()
        dao = FakeFavoriteCityDao()
        repository = CityRepositoryImpl(api = api, favoriteDao = dao, language = { "fr" })
    }

    // --- Recherche ---

    @Test
    fun recherche_moinsDe2Caracteres_pasDAppel() = runTest {
        assertEquals(DataResult.Success(emptyList<Any>()), repository.search("P"))
        assertEquals(DataResult.Success(emptyList<Any>()), repository.search("  a  "))
        assertEquals(0, api.callCount)
    }

    @Test
    fun recherche_envoieLeTexteSansEspaces_etLaLangue() = runTest {
        repository.search("  Pau ")

        assertEquals("Pau", api.lastName)
        assertEquals("fr", api.lastLanguage)
    }

    @Test
    fun recherche_convertitLesResultatsEnVilles() = runTest {
        api.response = GeocodingResponseDto(
            results = listOf(
                GeocodingResultDto(2988358, "Pau", 43.31, -0.36, "France", "Nouvelle-Aquitaine", "Europe/Paris"),
                GeocodingResultDto(3114198, "Pau", 42.32, 3.12, "Espagne", "Catalogne", "Europe/Madrid"),
            ),
        )

        val result = repository.search("Pau") as DataResult.Success

        assertEquals(2, result.data.size)
        assertEquals("Nouvelle-Aquitaine", result.data[0].region)
        assertEquals("Espagne", result.data[1].country) // les homonymes sont bien distingués
    }

    @Test
    fun recherche_sansResultat_renvoieListeVide() = runTest {
        api.response = GeocodingResponseDto(results = null)

        assertEquals(DataResult.Success(emptyList<Any>()), repository.search("zzzz"))
    }

    @Test
    fun recherche_erreurReseau_renvoieNetwork() = runTest {
        api.error = IOException()

        assertEquals(DataResult.Error(DataError.NETWORK), repository.search("Pau"))
    }

    // --- Favoris ---

    @Test
    fun favoris_ajoutesALaSuite() = runTest {
        repository.addFavorite(paris)
        repository.addFavorite(pau)

        assertEquals(listOf(paris, pau), repository.observeFavorites().first())
    }

    @Test
    fun favoris_ajouterDeuxFoisLaMemeVille_neLaDuplique_pas() = runTest {
        repository.addFavorite(paris)
        repository.addFavorite(paris)

        assertEquals(listOf(paris), repository.observeFavorites().first())
    }

    @Test
    fun favoris_suppression() = runTest {
        repository.addFavorite(paris)
        repository.addFavorite(pau)

        repository.removeFavorite(paris.id)

        assertEquals(listOf(pau), repository.observeFavorites().first())
    }

    @Test
    fun favoris_reordonner() = runTest {
        repository.addFavorite(paris)
        repository.addFavorite(pau)

        repository.reorderFavorites(listOf(pau.id, paris.id))

        assertEquals(listOf(pau, paris), repository.observeFavorites().first())
    }

    @Test
    fun favoris_apresSuppression_unNouvelAjoutVaALaFin() = runTest {
        repository.addFavorite(paris)
        repository.addFavorite(pau)
        repository.removeFavorite(paris.id)

        repository.addFavorite(paris)

        assertEquals(listOf(pau, paris), repository.observeFavorites().first())
    }
}
