package com.example.nimbus.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.nimbus.data.local.entity.FavoriteCityEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Teste les vraies requêtes SQL de Room, dans une base en mémoire (effacée après chaque test). */
@RunWith(AndroidJUnit4::class)
class FavoriteCityDaoTest {

    private lateinit var db: NimbusDatabase
    private lateinit var dao: FavoriteCityDao

    private fun city(id: Long, name: String) =
        FavoriteCityEntity(id, name, null, "France", 0.0, 0.0, "Europe/Paris", position = 0)

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, NimbusDatabase::class.java).build()
        dao = db.favoriteCityDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertAtEnd_ajouteDansLOrdre() = runTest {
        dao.insertAtEnd(city(1, "Paris"))
        dao.insertAtEnd(city(2, "Pau"))
        dao.insertAtEnd(city(3, "Lyon"))

        val favoris = dao.observeAll().first()
        assertEquals(listOf("Paris", "Pau", "Lyon"), favoris.map { it.name })
        assertEquals(listOf(0, 1, 2), favoris.map { it.position })
    }

    @Test
    fun insert_villeDejaPresente_estIgnoree() = runTest {
        dao.insertAtEnd(city(1, "Paris"))
        dao.insertAtEnd(city(1, "Paris"))

        assertEquals(1, dao.observeAll().first().size)
    }

    @Test
    fun deleteById_supprimeLaVille() = runTest {
        dao.insertAtEnd(city(1, "Paris"))
        dao.insertAtEnd(city(2, "Pau"))

        dao.deleteById(1)

        assertEquals(listOf("Pau"), dao.observeAll().first().map { it.name })
    }

    @Test
    fun reorder_changeLOrdre() = runTest {
        dao.insertAtEnd(city(1, "Paris"))
        dao.insertAtEnd(city(2, "Pau"))
        dao.insertAtEnd(city(3, "Lyon"))

        dao.reorder(listOf(3, 1, 2))

        assertEquals(listOf("Lyon", "Paris", "Pau"), dao.observeAll().first().map { it.name })
    }
}
