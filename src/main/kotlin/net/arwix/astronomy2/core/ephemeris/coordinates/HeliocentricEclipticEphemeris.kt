package net.arwix.astronomy2.core.ephemeris.coordinates

import kotlinx.coroutines.experimental.CoroutineScope
import net.arwix.astronomy2.core.Ecliptic
import net.arwix.astronomy2.core.Heliocentric
import net.arwix.astronomy2.core.JT
import net.arwix.astronomy2.core.vector.Vector

@Heliocentric @Ecliptic
interface HeliocentricEclipticEphemeris {

    @Heliocentric @Ecliptic
    fun getCoordinates(jT: JT) :Vector
}

typealias createHeliocentricEclipticCoordinates = (jT: JT) -> Vector
typealias createSuspendHeliocentricEclipticCoordinates = suspend CoroutineScope.(jT: JT) -> Vector