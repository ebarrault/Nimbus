package com.example.nimbus.fakes

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

/** Horloge qu'on peut avancer à la main pour simuler le temps qui passe. */
class TestClock(var now: Instant = Instant.parse("2026-10-27T12:00:00Z")) : Clock() {
    fun advanceBy(duration: Duration) { now = now.plus(duration) }
    override fun instant(): Instant = now
    override fun getZone(): ZoneId = ZoneOffset.UTC
    override fun withZone(zone: ZoneId?): Clock = this
}
