package net.arwix.astronomy2.core.ephemeris.coordinates

import net.arwix.astronomy2.core.Ecliptic
import net.arwix.astronomy2.core.Heliocentric
import net.arwix.astronomy2.core.JT
import net.arwix.astronomy2.core.vector.Vector

@Heliocentric @Ecliptic
interface HeliocentricEclipticEphemeris {

    @Heliocentric @Ecliptic
    fun getCoordinates(jT: JT) :Vector
}

typealias getHeliocentricEclipticCoordinates = (jT: JT) -> Vector

typealias getCoroutineHeliocentricEclipticCoordinates = suspend (jT: JT) -> Vector