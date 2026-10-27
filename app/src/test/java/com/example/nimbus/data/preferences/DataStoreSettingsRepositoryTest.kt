package com.example.nimbus.data.preferences

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.example.nimbus.domain.model.Settings
import com.example.nimbus.domain.model.TemperatureUnit
import com.example.nimbus.domain.model.ThemeMode
import com.example.nimbus.domain.model.WindSpeedUnit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

/** DataStore fonctionne sans Android : on le teste avec un vrai fichier dans un dossier temporaire. */
class DataStoreSettingsRepositoryTest {

    @get:Rule
    val tmpFolder = TemporaryFolder()

    private fun TestScope.createRepository() = DataStoreSettingsRepository(
        PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { File(tmpFolder.root, "test_settings.preferences_pb") },
        ),
    )

    @Test
    fun parDefaut_celsiusKmhThemeSysteme() = runTest {
        assertEquals(Settings(), createRepository().settings.first())
    }

    @Test
    fun unitesEtTheme_sontEnregistres() = runTest {
        val repository = createRepository()

        repository.setTemperatureUnit(TemperatureUnit.FAHRENHEIT)
        repository.setWindSpeedUnit(WindSpeedUnit.MS)
        repository.setThemeMode(ThemeMode.DARK)

        val settings = repository.settings.first()
        assertEquals(TemperatureUnit.FAHRENHEIT, settings.temperatureUnit)
        assertEquals(WindSpeedUnit.MS, settings.windSpeedUnit)
        assertEquals(ThemeMode.DARK, settings.themeMode)
    }

    @Test
    fun derniereVille_enregistreeEtEffacee() = runTest {
        val repository = createRepository()

        repository.setLastCityId(2988507)
        assertEquals(2988507L, repository.settings.first().lastCityId)

        repository.setLastCityId(null)
        assertNull(repository.settings.first().lastCityId)
    }
}
