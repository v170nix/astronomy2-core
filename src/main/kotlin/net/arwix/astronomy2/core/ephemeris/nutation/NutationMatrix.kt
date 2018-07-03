package net.arwix.astronomy2.core.ephemeris.nutation

import net.arwix.astronomy2.core.Ecliptic
import net.arwix.astronomy2.core.Equatorial
import net.arwix.astronomy2.core.vector.Matrix

interface NutationMatrix {
    @Ecliptic val ecliptic: Matrix
    @Equatorial val equatorial: Matrix
}