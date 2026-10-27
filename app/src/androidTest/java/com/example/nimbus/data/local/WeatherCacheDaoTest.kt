package com.example.nimbus.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.nimbus.data.local.entity.WeatherCacheEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WeatherCacheDaoTest {

    private lateinit var db: NimbusDatabase
    private lateinit var dao: WeatherCacheDao

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, NimbusDatabase::class.java).build()
        dao = db.weatherCacheDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun upsert_puisGet() = runTest {
        val entry = WeatherCacheEntity(1, "{}", "celsius", "kmh", fetchedAt = 1000)

        dao.upsert(entry)

        assertEquals(entry, dao.get(1))
        assertEquals(entry, dao.observe(1).first())
    }

    @Test
    fun upsert_remplaceLAncienCache() = runTest {
        dao.upsert(WeatherCacheEntity(1, "{}", "celsius", "kmh", fetchedAt = 1000))
        dao.upsert(WeatherCacheEntity(1, "{\"v\":2}", "fahrenheit", "ms", fetchedAt = 2000))

        val entry = dao.get(1)!!
        assertEquals(2000L, entry.fetchedAt)
        assertEquals("fahrenheit", entry.temperatureUnit)
    }

    @Test
    fun villeSansCache_renvoieNull() = runTest {
        assertNull(dao.get(42))
        assertNull(dao.observe(42).first())
    }

    @Test
    fun delete_supprimeLeCache() = runTest {
        dao.upsert(WeatherCacheEntity(1, "{}", "celsius", "kmh", fetchedAt = 1000))

        dao.delete(1)

        assertNull(dao.get(1))
    }
}
