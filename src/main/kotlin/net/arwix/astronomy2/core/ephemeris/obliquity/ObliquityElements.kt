package net.arwix.astronomy2.core.ephemeris.obliquity

import net.arwix.astronomy2.core.Ecliptic
import net.arwix.astronomy2.core.Equatorial
import net.arwix.astronomy2.core.JT
import net.arwix.astronomy2.core.vector.Matrix
import net.arwix.astronomy2.core.vector.Vector

interface ObliquityElements {

    val id: IdObliquity
    val t: JT
    val obliquity: Obliquity
    val eclipticToEquatorialMatrix: Matrix
    val equatorialToEclipticMatrix: Matrix

    @Equatorial fun rotateEclipticVector(@Ecliptic vector: Vector): Vector
    @Ecliptic fun rotateEquatorialVector(@Equatorial vector: Vector): Vector
}