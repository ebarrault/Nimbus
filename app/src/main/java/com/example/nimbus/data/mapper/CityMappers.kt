package com.example.nimbus.data.mapper

import com.example.nimbus.data.local.entity.FavoriteCityEntity
import com.example.nimbus.data.remote.dto.GeocodingResultDto
import com.example.nimbus.domain.model.City

fun GeocodingResultDto.toDomain(): City = City(
    id = id,
    name = name,
    region = region,
    country = country,
    latitude = latitude,
    longitude = longitude,
    timezone = timezone,
)

fun FavoriteCityEntity.toDomain(): City = City(
    id = id,
    name = name,
    region = region,
    country = country,
    latitude = latitude,
    longitude = longitude,
    timezone = timezone,
)

fun City.toEntity(position: Int): FavoriteCityEntity = FavoriteCityEntity(
    id = id,
    name = name,
    region = region,
    country = country,
    latitude = latitude,
    longitude = longitude,
    timezone = timezone,
    position = position,
)
