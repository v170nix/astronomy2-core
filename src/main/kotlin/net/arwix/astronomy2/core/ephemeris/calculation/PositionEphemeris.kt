package net.arwix.astronomy2.core.ephemeris.calculation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.yield
import net.arwix.astronomy2.core.*
import net.arwix.astronomy2.core.ephemeris.EclipticToEquatorialElements
import net.arwix.astronomy2.core.ephemeris.precession.IdPrecession
import net.arwix.astronomy2.core.vector.RectangularVector
import net.arwix.astronomy2.core.vector.Vector

class PositionEphemeris(
        private val idPrecession: IdPrecession,
        @Heliocentric
        @Ecliptic
        @J2000
        private val findEarthCoordinates: suspend (jT: JT) -> Vector
) {
    private lateinit var earthVelocity: Vector
    private lateinit var elements: EclipticToEquatorialElements

    fun copy() = PositionEphemeris(idPrecession, findEarthCoordinates).also {
        if (::earthVelocity.isInitialized)
            it.earthVelocity = RectangularVector(earthVelocity)
        if (::elements.isInitialized)
            it.elements = EclipticToEquatorialElements(elements.precessionElements.id, elements.precessionElements.jT)
    }

    suspend fun setJT0(jt0: JT): PositionEphemeris = coroutineScope {
        val earth = async { findEarthCoordinates(jt0) }
        val earthPlus = async { findEarthCoordinates(jt0 + 0.01 / JULIAN_DAYS_PER_CENTURY) }
        elements = EclipticToEquatorialElements(idPrecession, jt0)
        earthVelocity = (earthPlus.await() - earth.await()) / 0.01
        this@PositionEphemeris
    }

    suspend fun createBodyOptions(
            jt0: JT,
            findBodyCoordinates: suspend (jT: JT) -> Vector
    ): Options = coroutineScope {
        val body = async { findBodyCoordinates(jt0) }
        val bodyPlus = async { findBodyCoordinates(jt0 + 0.01 / JULIAN_DAYS_PER_CENTURY) }
        Options(findBodyCoordinates, (bodyPlus.await() - body.await()) / 0.01)
    }

    fun getEarthVelocity(): Vector = RectangularVector(earthVelocity)
    fun getElements() = elements

    suspend fun getPosition(jt: JT, options: Options): Vector = coroutineScope {
        val body = async { options.findBodyCoordinates(jt) }
        val earth = async { findEarthCoordinates(jt) }
        val geocentricBody = body.await() - earth.await()
        yield()
        val lightTime = geocentricBody.normalize() / C_Light
        elements.transformMatrix * (geocentricBody - (options.bodyVelocity - earthVelocity) * lightTime)
    }

    data class Options(internal val findBodyCoordinates: suspend (jt: JT) -> Vector,
                       internal val bodyVelocity: Vector)
}


@Geocentric
@Equatorial
@Apparent
suspend fun getPositionEphemeris(
        jT: JT,
        elements: EclipticToEquatorialElements,
        @Heliocentric
        @Ecliptic
        @J2000
        findEarthCoordinates: suspend (jT: JT) -> Vector,
        @Heliocentric
        @Ecliptic
        @J2000
        findBodyCoordinates: suspend (jT: JT) -> Vector
): Vector = coroutineScope {
    val body = async { findBodyCoordinates(jT) }
    val earth = async { findEarthCoordinates(jT) }
    val geocentricBody = body.await() - earth.await()
    yield()
    val lightTime = geocentricBody.normalize() / C_Light
    val earthVelocity = getBodyVelocity(earth.await(), jT = jT, findCoordinates = findEarthCoordinates)
    //  val earthVelocity = getSimonJ2000KeplerElements(ID_EARTH_KEPLER_ELEMENTS).getOrbitalPlane(jT).velocity
    val bodyVelocity = getBodyVelocity(body.await(), jT = jT, findCoordinates = findBodyCoordinates)

    elements.transformMatrix * (geocentricBody - (bodyVelocity - earthVelocity) * lightTime)
}

@Heliocentric
@Ecliptic
private suspend fun getBodyVelocity(
        currentCoordinates: Vector, jT: JT, lightTime: Double = 0.01,
        @Heliocentric
        @Ecliptic
        @J2000
        findCoordinates: suspend (jT: JT) -> Vector
): Vector {
    //  val body = findCoordinates(jT)
    val bodyPlus = findCoordinates(jT + lightTime / JULIAN_DAYS_PER_CENTURY)
    return (bodyPlus - currentCoordinates) / lightTime
}
