package net.arwix.astronomy2.core.ephemeris.calculation

import net.arwix.astronomy2.core.*
import net.arwix.astronomy2.core.math.toRad
import net.arwix.astronomy2.core.vector.Vector
import java.util.*
import kotlin.math.sin


enum class TwilightType constructor(angle: Double) {
    CIVIL(-6.0), NAUTICAL(-12.0), ASTRONOMICAL(-18.0);
    internal val sinRefractionAngle = sin(angle.toRad())
}

sealed class TwilightResult(open val type: TwilightType) {
    data class Begin(override val type: TwilightType, val calendar: Calendar) : TwilightResult(type)
    data class End(override val type: TwilightType, val calendar: Calendar) : TwilightResult(type)
    data class BeginEnd(override val type: TwilightType, val begin: Calendar, val end: Calendar) : TwilightResult(type)
    data class None(override val type: TwilightType, val isAbove: Boolean) : TwilightResult(type)
}

suspend fun findTwilight(
        type: TwilightType,
        calendar: Calendar,
        latitude: Radian,
        longitude: Radian,
        @Geocentric
        @Equatorial
        @Apparent
        findCoordinates: suspend (jT: JT) -> Vector): TwilightResult {
    val result = findRiseSet(type.sinRefractionAngle, calendar, latitude, longitude, findCoordinates)
    return when (result) {
        is RiseSetCalculationResult.RiseSet -> TwilightResult.BeginEnd(type, result.rise.calendar, result.set.calendar)
        is RiseSetCalculationResult.Rise -> TwilightResult.Begin(type, result.calendar)
        is RiseSetCalculationResult.Set -> TwilightResult.End(type, result.calendar)
        is RiseSetCalculationResult.None -> TwilightResult.None(type, result.isAbove)
    }
}
