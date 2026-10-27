package com.example.nimbus

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/** Point de départ de Hilt : il crée ici les objets partagés par toute l'app. */
@HiltAndroidApp
class NimbusApp : Application()
