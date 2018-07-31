package net.arwix.astronomy2.core.ephemeris

import net.arwix.astronomy2.core.*
import net.arwix.astronomy2.core.ephemeris.nutation.createNutationElements
import net.arwix.astronomy2.core.ephemeris.obliquity.createObliquityElements
import net.arwix.astronomy2.core.ephemeris.precession.PrecessionElements
import net.arwix.astronomy2.core.ephemeris.precession.findNearestNutationModel
import net.arwix.astronomy2.core.ephemeris.precession.findNearestObliquityModel
import net.arwix.astronomy2.core.kepler.ID_EARTH_KEPLER_ELEMENTS
import net.arwix.astronomy2.core.kepler.getOrbitalPlane
import net.arwix.astronomy2.core.kepler.getSimonJ2000KeplerElements
import net.arwix.astronomy2.core.vector.Vector


@Geocentric
@Equatorial
@Apparent
suspend fun getPositionEphemeris(jT: JT,
                                 precessionElements: PrecessionElements,
                                 @Heliocentric
                                 @Ecliptic
                                 @J2000
                                 findEarthCoordinates: suspend (jT: JT) -> Vector,
                                 @Heliocentric
                                 @Ecliptic
                                 @J2000
                                 findCoordinates: suspend (jT: JT) -> Vector): Vector {

    val body = findCoordinates(jT)
    val earth = findEarthCoordinates(jT)
    val geocentricBody = body - earth
    val lightTime = geocentricBody.normalize() / C_Light
    val earthVelocity = getBodyVelocity(jT = jT, findCoordinates = findEarthCoordinates) //getSimonJ2000KeplerElements(ID_EARTH_KEPLER_ELEMENTS).getOrbitalPlane(jT).velocity
    val bodyVelocity = getBodyVelocity(jT = jT, findCoordinates = findCoordinates)
    val obliquityId = findNearestObliquityModel(precessionElements.id)
    val obliquity = createObliquityElements(obliquityId, if (precessionElements.isEcliptic) jT else 0.0)
    val nutationId = findNearestNutationModel(precessionElements.id)
    val nutation = createNutationElements(nutationId, jT, obliquity.obliquity)
    val matrix = if (precessionElements.isEcliptic) {
        nutation.equatorialMatrix!! * obliquity.eclipticToEquatorialMatrix * precessionElements.fromJ2000Matrix
    } else {
        nutation.equatorialMatrix!! * precessionElements.fromJ2000Matrix * obliquity.eclipticToEquatorialMatrix
    }
    return matrix * (geocentricBody - (bodyVelocity - earthVelocity) * lightTime)
}

@Heliocentric
@Ecliptic
private suspend fun getBodyVelocity(jT: JT, lightTime: Double = 0.01,
                    @Heliocentric
                    @Ecliptic
                    @J2000
                    findCoordinates: suspend (jT: JT) -> Vector): Vector {
    val body = findCoordinates(jT)
    val bodyPlus = findCoordinates(jT + lightTime / JULIAN_DAYS_PER_CENTURY)
    return (bodyPlus - body) / lightTime
}
