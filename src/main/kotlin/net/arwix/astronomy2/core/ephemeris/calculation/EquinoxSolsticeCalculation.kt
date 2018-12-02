package net.arwix.astronomy2.core.ephemeris.calculation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.yield
import net.arwix.astronomy2.core.*
import net.arwix.astronomy2.core.calendar.*
import net.arwix.astronomy2.core.vector.RectangularVector
import net.arwix.astronomy2.core.vector.SphericalVector
import net.arwix.astronomy2.core.vector.Vector
import net.arwix.astronomy2.core.vector.convert
import java.util.*
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin


sealed class EquinoxSolsticeRequest(val year: Int) : Iterator<EquinoxSolsticeRequest> {

    class SpringEquinox(year: Int) : EquinoxSolsticeRequest(year) {
        override fun hasNext() = true
        override fun next() = SummerSolstice(year)
    }

    class SummerSolstice(year: Int) : EquinoxSolsticeRequest(year) {
        override fun hasNext() = true
        override fun next() = AutumnEquinox(year)
    }

    class AutumnEquinox(year: Int) : EquinoxSolsticeRequest(year) {
        override fun hasNext() = true
        override fun next() = WinterSolstice(year)
    }

    class WinterSolstice(year: Int) : EquinoxSolsticeRequest(year) {
        override fun hasNext() = false
        override fun next() = SpringEquinox(year)
    }

}

suspend fun findEquinoxSolstice(
        request: EquinoxSolsticeRequest,
        positionEphemeris: PositionEphemeris,
        precision: Double = 0.1 / 24.0 / 3600.0
): Pair<EquinoxSolsticeRequest, Calendar> {

    val sunCoordinates: suspend (jt: JT) -> Vector = { RectangularVector() }

    var mjd = Calendar.getInstance()
            .dayOfMonth(1)
            .month(getInitMonth(request))
            .year(request.year)
            .resetTime()
            .getMJD()
    val jt = getJT(mjd)
    var isFirst = true
    var delta = 100.0
    positionEphemeris.setJT0(jt)
    val options: PositionEphemeris.Options = positionEphemeris.createBodyOptions(jt, sunCoordinates)

    do {
        if (delta >= 1.0 && !isFirst) positionEphemeris.setJT0(getJT(mjd))
        yield()
        @Geocentric @Equatorial @Apparent
        val equatorialPosition = positionEphemeris.getPosition(getJT(mjd), options)

        @Geocentric @Ecliptic @Apparent
        val eclipticPosition = positionEphemeris.getElements().obliquityElements.rotateEquatorialVector(equatorialPosition)

        delta = getDelta(request, convert<SphericalVector>(eclipticPosition).phi)
        mjd += delta
        isFirst = false
    } while (abs(delta) > precision)
    return request to Calendar.getInstance().applyMJD(mjd, true)
}

suspend fun findEquinoxSolstice(
        year: Int,
        positionEphemeris: PositionEphemeris,
        precision: Double = 0.1 / 24.0 / 3600.0
): List<Pair<EquinoxSolsticeRequest, Calendar>> = coroutineScope {
    val requests = listOf(EquinoxSolsticeRequest.SpringEquinox(year),
            EquinoxSolsticeRequest.SummerSolstice(year),
            EquinoxSolsticeRequest.AutumnEquinox(year),
            EquinoxSolsticeRequest.WinterSolstice(year))

    Array(4) {
        async {
            findEquinoxSolstice(requests[it], positionEphemeris.copy(), precision)
        }
    }.map { yield(); it.await() }

}

private fun getDelta(request: EquinoxSolsticeRequest, longitude: Double) = when (request) {
    is EquinoxSolsticeRequest.SpringEquinox -> 58.13 * sin(-longitude)
    is EquinoxSolsticeRequest.SummerSolstice -> 58.13 * sin(PI_OVER_TWO - longitude)
    is EquinoxSolsticeRequest.AutumnEquinox -> 58.13 * sin(PI - longitude)
    is EquinoxSolsticeRequest.WinterSolstice -> 58.13 * sin(-PI_OVER_TWO - longitude)
}

private fun getInitMonth(request: EquinoxSolsticeRequest) = when (request) {
    is EquinoxSolsticeRequest.SpringEquinox -> 2
    is EquinoxSolsticeRequest.SummerSolstice -> 5
    is EquinoxSolsticeRequest.AutumnEquinox -> 8
    is EquinoxSolsticeRequest.WinterSolstice -> 11
}