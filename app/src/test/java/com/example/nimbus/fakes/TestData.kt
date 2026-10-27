package com.example.nimbus.fakes

import com.example.nimbus.data.remote.NetworkFactory
import com.example.nimbus.data.remote.dto.ForecastResponseDto
import com.example.nimbus.domain.model.City

/** Données partagées par les tests. */
object TestData {

    /** Le JSON d'exemple (src/test/resources/forecast_paris.json), comme le renverrait l'API. */
    val forecastJson: String by lazy {
        requireNotNull(javaClass.classLoader?.getResource("forecast_paris.json")) {
            "forecast_paris.json introuvable dans src/test/resources"
        }.readText()
    }

    val forecastDto: ForecastResponseDto by lazy {
        NetworkFactory.json.decodeFromString(ForecastResponseDto.serializer(), forecastJson)
    }

    val paris = City(
        id = 2988507,
        name = "Paris",
        region = "Île-de-France",
        country = "France",
        latitude = 48.85,
        longitude = 2.35,
        timezone = "Europe/Paris",
    )

    val pau = City(
        id = 2988358,
        name = "Pau",
        region = "Nouvelle-Aquitaine",
        country = "France",
        latitude = 43.31,
        longitude = -0.36,
        timezone = "Europe/Paris",
    )
}
