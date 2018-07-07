package net.arwix.astronomy2.core.ephemeris.coordinates

import net.arwix.astronomy2.core.Ecliptic
import net.arwix.astronomy2.core.Geocentric
import net.arwix.astronomy2.core.JT
import net.arwix.astronomy2.core.vector.Vector

@Geocentric @Ecliptic
interface GeocentricEclipticEphemeris {

    @Geocentric @Ecliptic
    fun getCoordinates(jT: JT) : Vector
}

typealias getGeocentricEclipticCoordinates = (jT: JT) -> Vector

typealias getCoroutineGeocentricEclipticCoordinates = suspend (jT: JT) -> Vector