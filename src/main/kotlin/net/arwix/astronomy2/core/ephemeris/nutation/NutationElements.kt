package net.arwix.astronomy2.core.ephemeris.nutation

import net.arwix.astronomy2.core.Ecliptic
import net.arwix.astronomy2.core.Equatorial
import net.arwix.astronomy2.core.vector.Matrix
import net.arwix.astronomy2.core.vector.Vector

interface NutationElements {

    val id: NutationId
    val t: Double
    val angles: NutationAngles
    @Ecliptic val eclipticMatrix: Matrix
    @Equatorial val equatorialMatrix: Matrix?

    @Ecliptic fun applyNutationToEclipticVector(@Ecliptic vector: Vector): Vector
    @Ecliptic fun removeNutationFromEclipticVector(@Ecliptic vector: Vector): Vector
    @Equatorial fun applyNutationToEquatorialVector(@Equatorial vector: Vector): Vector
    @Equatorial fun removeNutationFromEquatorialVector(@Equatorial vector: Vector): Vector
}