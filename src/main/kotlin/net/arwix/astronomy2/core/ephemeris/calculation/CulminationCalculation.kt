package net.arwix.astronomy2.core.ephemeris.calculation

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.yield
import net.arwix.astronomy2.core.*
import net.arwix.astronomy2.core.calendar.*
import net.arwix.astronomy2.core.math.SearchGoldenExtremum
import net.arwix.astronomy2.core.vector.Vector
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.cos
import kotlin.math.sin

sealed class CulminationCalculationResult {
    data class Upper(val isAbove: Boolean, val calendar: Calendar) : CulminationCalculationResult()
    data class Lower(val isAbove: Boolean, val calendar: Calendar) : CulminationCalculationResult()
    data class UpperLower(val upper: Upper, val lower: Lower) : CulminationCalculationResult()
}

suspend fun findCulmination(
        precision: Double,
        calendar: Calendar,
        latitude: Radian,
        longitude: Radian,
        @Geocentric
        @Equatorial
        @Apparent
        findCoordinates: suspend (jT: JT) -> Vector
): CulminationCalculationResult = coroutineScope {

    val innerCalendar = calendar.copy().resetTime()
    val deltaT = innerCalendar.getDeltaT(TimeUnit.DAYS)
    val MJD0 = innerCalendar.getMJD()

    val cosLatitude = cos(latitude)
    val sinLatitude = sin(latitude)

    val searchGoldenExtremum = SearchGoldenExtremum(0.0, 24.0, precision, 50) {
        x ->
        yield()
        getSinAltitude(MJD0 + x / 24.0, deltaT, longitude, cosLatitude, sinLatitude, findCoordinates)
    }

    val maxHours = async { searchGoldenExtremum.getMax() }
    val minHours = async { searchGoldenExtremum.getMin() }

    val upperCalendar = innerCalendar.copy().setHours(maxHours.await())
    val lowerCalendar = innerCalendar.copy().setHours(minHours.await())
    val upperIsAbove = async {
        getSinAltitude(upperCalendar.getMJD(), deltaT, longitude, cosLatitude, sinLatitude, findCoordinates)
    }

    val lowerIsAbove = async {
        getSinAltitude(lowerCalendar.getMJD(), deltaT, longitude, cosLatitude, sinLatitude, findCoordinates)
    }

    CulminationCalculationResult.UpperLower(
            CulminationCalculationResult.Upper( upperIsAbove.await() > 0.0, upperCalendar),
            CulminationCalculationResult.Lower( lowerIsAbove.await() > 0.0, lowerCalendar)
    )

}