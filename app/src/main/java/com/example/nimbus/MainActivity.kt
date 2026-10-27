package com.example.nimbus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nimbus.domain.model.Settings
import com.example.nimbus.domain.model.ThemeMode
import com.example.nimbus.domain.repository.SettingsRepository
import com.example.nimbus.ui.navigation.NimbusNavHost
import com.example.nimbus.ui.theme.NimbusTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/** @AndroidEntryPoint : Hilt peut fournir les ViewModels et les dépendances de cette activité. */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /** Pour appliquer le thème choisi dans les réglages à toute l'app. */
    @Inject lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by settingsRepository.settings.collectAsStateWithLifecycle(initialValue = Settings())
            val darkTheme = when (settings.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            NimbusTheme(darkTheme = darkTheme) {
                NimbusNavHost()
            }
        }
    }
}
