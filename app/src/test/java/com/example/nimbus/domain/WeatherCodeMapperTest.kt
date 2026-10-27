package com.example.nimbus.domain

import com.example.nimbus.domain.model.WeatherCondition
import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherCodeMapperTest {

    @Test
    fun code0_cielDegage() {
        assertEquals(WeatherCondition.CLEAR, WeatherCodeMapper.fromCode(0))
    }

    @Test
    fun code61_pluieFaible() {
        // Exemple donné dans ARCHITECTURE.md
        assertEquals(WeatherCondition.RAIN_LIGHT, WeatherCodeMapper.fromCode(61))
    }

    @Test
    fun codesDuMemeGroupe_donnentLaMemeCondition() {
        listOf(45, 48).forEach { assertEquals(WeatherCondition.FOG, WeatherCodeMapper.fromCode(it)) }
        listOf(71, 73, 75).forEach { assertEquals(WeatherCondition.SNOW, WeatherCodeMapper.fromCode(it)) }
        listOf(96, 99).forEach { assertEquals(WeatherCondition.THUNDERSTORM_HAIL, WeatherCodeMapper.fromCode(it)) }
    }

    @Test
    fun codeInconnu_donneUnknown() {
        listOf(-1, 4, 10, 100).forEach {
            assertEquals(WeatherCondition.UNKNOWN, WeatherCodeMapper.fromCode(it))
        }
    }
}
