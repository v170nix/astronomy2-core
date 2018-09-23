package net.arwix.astronomy2.core.ephemeris.coordinates

import kotlinx.coroutines.experimental.CoroutineScope
import net.arwix.astronomy2.core.Ecliptic
import net.arwix.astronomy2.core.Geocentric
import net.arwix.astronomy2.core.JT
import net.arwix.astronomy2.core.vector.Vector

@Geocentric @Ecliptic
interface GeocentricEclipticEphemeris {
    @Geocentric @Ecliptic
    fun getCoordinates(jT: JT) : Vector
}

typealias createGeocentricEclipticCoordinates = (jT: JT) -> Vector

typealias createSuspendGeocentricEclipticCoordinates = suspend CoroutineScope.(jT: JT) -> Vector