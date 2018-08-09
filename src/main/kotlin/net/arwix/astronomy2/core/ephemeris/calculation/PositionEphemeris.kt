package net.arwix.astronomy2.core.ephemeris.calculation

import kotlinx.coroutines.experimental.*
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
        it.earthVelocity = RectangularVector(earthVelocity)
        it.elements = EclipticToEquatorialElements(elements.precessionElements.id, elements.precessionElements.jT)
    }

    suspend fun setJT0(jt0: JT): PositionEphemeris {
        val earth = async(CommonPool) { findEarthCoordinates(jt0) }
        val earthPlus = async(CommonPool) { findEarthCoordinates(jt0 + 0.01 / JULIAN_DAYS_PER_CENTURY) }
        elements = EclipticToEquatorialElements(idPrecession, jt0)
        earthVelocity = (earthPlus.await() - earth.await()) / 0.01
        return this
    }

    suspend fun createBodyOptions(jt0: JT, findBodyCoordinates: suspend (jt: JT) -> Vector): Options {
        val body = async(CommonPool) { findBodyCoordinates(jt0) }
        val bodyPlus = async(CommonPool) { findBodyCoordinates(jt0 + 0.01 / JULIAN_DAYS_PER_CENTURY) }
        return Options(findBodyCoordinates, (bodyPlus.await() - body.await()) / 0.01)
    }

    fun getEarthVelocity(): Vector = RectangularVector(earthVelocity)
    fun getElements() = elements

    suspend fun getPosition(jt: JT, options: Options): Vector {
        val body = async(CommonPool) { options.findBodyCoordinates(jt) }
        val earth = async(CommonPool) { findEarthCoordinates(jt) }
        val geocentricBody = body.await() - earth.await()
        yield()
        val lightTime = geocentricBody.normalize() / C_Light
        return elements.transformMatrix * (geocentricBody - (options.bodyVelocity - earthVelocity) * lightTime)
    }

    data class Options(internal val findBodyCoordinates: suspend (jt: JT) -> Vector,
                       internal val bodyVelocity: Vector)
}


@Geocentric
@Equatorial
@Apparent
suspend fun getPositionEphemeris(jT: JT,
                                 elements: EclipticToEquatorialElements,
                                 @Heliocentric
                                 @Ecliptic
                                 @J2000
                                 findEarthCoordinates: suspend (jT: JT) -> Vector,
                                 @Heliocentric
                                 @Ecliptic
                                 @J2000
                                 findBodyCoordinates: suspend (jT: JT) -> Vector): Vector {

    val body = async(CommonPool) { findBodyCoordinates(jT) }
    val earth = async(CommonPool) { findEarthCoordinates(jT) }
    val geocentricBody = body.await() - earth.await()
    yield()
    val lightTime = geocentricBody.normalize() / C_Light
    val earthVelocity = getBodyVelocity(earth.await(), jT = jT, findCoordinates = findEarthCoordinates)
    //  val earthVelocity = getSimonJ2000KeplerElements(ID_EARTH_KEPLER_ELEMENTS).getOrbitalPlane(jT).velocity
    val bodyVelocity = getBodyVelocity(body.await(), jT = jT, findCoordinates = findBodyCoordinates)

    return elements.transformMatrix * (geocentricBody - (bodyVelocity - earthVelocity) * lightTime)
}

@Heliocentric
@Ecliptic
private suspend fun getBodyVelocity(currentCoordinates: Vector, jT: JT, lightTime: Double = 0.01,
                                    @Heliocentric
                                    @Ecliptic
                                    @J2000
                                    findCoordinates: suspend (jT: JT) -> Vector): Vector {
    //  val body = findCoordinates(jT)
    val bodyPlus = findCoordinates(jT + lightTime / JULIAN_DAYS_PER_CENTURY)
    return (bodyPlus - currentCoordinates) / lightTime
}
