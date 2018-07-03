package net.arwix.astronomy2.core.ephemeris.nutation

import net.arwix.astronomy2.core.Ecliptic
import net.arwix.astronomy2.core.Equatorial
import net.arwix.astronomy2.core.Geocentric
import net.arwix.astronomy2.core.ephemeris.obliquity.Obliquity
import net.arwix.astronomy2.core.ephemeris.obliquity.ObliquityElements
import net.arwix.astronomy2.core.ephemeris.obliquity.getObliquity
import net.arwix.astronomy2.core.vector.Matrix
import net.arwix.astronomy2.core.vector.Matrix.Companion.AXIS_X
import net.arwix.astronomy2.core.vector.Matrix.Companion.AXIS_Z
import net.arwix.astronomy2.core.vector.Vector

typealias NutationId = Int

fun getNutationAngles(id: NutationId, t: Double): NutationAngles {
    return when (id) {
        ID_NUTATION_IAU1980 -> calcNutation_IAU1980(t)
        ID_NUTATION_IAU2000 -> calcNutation_IAU2000(t)
        ID_NUTATION_IAU2006 -> calcNutation_IAU2000(t).let {
            NutationAngles(it.deltaLongitude * (1.0 + (0.4697E-6 - 2.7774E-6 * t)), it.deltaObliquity * (1.0 + (2.7774E-6 * t)))
        }
        else -> throw IndexOutOfBoundsException()
    }
}

const val ID_NUTATION_IAU1980: NutationId = 2
const val ID_NUTATION_IAU2000: NutationId = 3
const val ID_NUTATION_IAU2006: NutationId = 4

@Ecliptic
fun createEclipticNutationMatrix(angles: NutationAngles): Matrix {
    return Matrix(AXIS_X, -angles.deltaObliquity) * Matrix(AXIS_Z, -angles.deltaLongitude)
}

@Equatorial
fun createEquatorialNutationMatrix(angles: NutationAngles, obliquity: Obliquity): Matrix {
    return Matrix(AXIS_X, -obliquity - angles.deltaObliquity) *
            Matrix(AXIS_Z, -angles.deltaLongitude) *
            Matrix(AXIS_X, obliquity)
}

fun createObliquityElements(id: NutationId, t: Double, obliquity: Obliquity? = null): NutationElements = object : NutationElements {
    override val id = id
    override val t: Double = t
    override val angles = getNutationAngles(id, t)
    override val eclipticMatrix = createEclipticNutationMatrix(angles)
    override val equatorialMatrix = obliquity?.let { createEquatorialNutationMatrix(angles, it) }

    override fun applyNutationToEclipticVector(vector: Vector) = eclipticMatrix * vector
    override fun removeNutationFromEclipticVector(vector: Vector) = vector * eclipticMatrix
    override fun applyNutationToEquatorialVector(vector: Vector) = equatorialMatrix!! * vector
    override fun removeNutationFromEquatorialVector(vector: Vector) = vector * equatorialMatrix!!

}