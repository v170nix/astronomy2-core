package net.arwix.astronomy2.core.ephemeris.calculation

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.yield
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
        objectType: ObjectType,
        precision: Double,
        calendar: Calendar,
        latitude: Radian,
        longitude: Radian,
        @Geocentric
        @Equatorial
        @Apparent
        findCoordinates: suspend (jT: JT) -> Vector
): CulminationCalculationResult {

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

    val maxHours = async(CommonPool) { searchGoldenExtremum.getMax() }
    val minHours = async(CommonPool) { searchGoldenExtremum.getMin() }

    val upperCalendar = innerCalendar.copy().setHours(maxHours.await())
    val lowerCalendar = innerCalendar.copy().setHours(minHours.await())
    val upperIsAbove = async(CommonPool) {
        getSinAltitude(upperCalendar.getMJD(), deltaT, longitude, cosLatitude, sinLatitude, findCoordinates)
    }

    val lowerIsAbove = async(CommonPool) {
        getSinAltitude(lowerCalendar.getMJD(), deltaT, longitude, cosLatitude, sinLatitude, findCoordinates)
    }

    return CulminationCalculationResult.UpperLower(
            CulminationCalculationResult.Upper( upperIsAbove.await() > 0.0, upperCalendar),
            CulminationCalculationResult.Lower( lowerIsAbove.await() > 0.0, lowerCalendar)
    )

}