package com.example.nimbus.fakes

import com.example.nimbus.data.remote.GeocodingApi
import com.example.nimbus.data.remote.dto.GeocodingResponseDto

class FakeGeocodingApi(
    var response: GeocodingResponseDto = GeocodingResponseDto(results = null),
) : GeocodingApi {
    var error: Throwable? = null
    var callCount = 0
        private set
    var lastName: String? = null
        private set
    var lastLanguage: String? = null
        private set

    override suspend fun search(name: String, language: String, count: Int, format: String): GeocodingResponseDto {
        callCount++
        lastName = name
        lastLanguage = language
        error?.let { throw it }
        return response
    }
}
