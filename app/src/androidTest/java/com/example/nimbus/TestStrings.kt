package com.example.nimbus

import androidx.annotation.StringRes
import androidx.test.platform.app.InstrumentationRegistry

/**
 * Lit un texte de strings.xml dans la langue du téléphone de test.
 * Comme ça, les tests passent que l'émulateur soit en français ou en anglais.
 */
fun str(@StringRes id: Int, vararg args: Any): String =
    InstrumentationRegistry.getInstrumentation().targetContext.getString(id, *args)
