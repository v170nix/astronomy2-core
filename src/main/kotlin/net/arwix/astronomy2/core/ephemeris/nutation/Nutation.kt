package net.arwix.astronomy2.core.ephemeris.nutation

import net.arwix.astronomy2.core.Ecliptic
import net.arwix.astronomy2.core.Equatorial
import net.arwix.astronomy2.core.JT
import net.arwix.astronomy2.core.ephemeris.obliquity.Obliquity
import net.arwix.astronomy2.core.vector.Matrix
import net.arwix.astronomy2.core.vector.Matrix.Companion.AXIS_X
import net.arwix.astronomy2.core.vector.Matrix.Companion.AXIS_Z
import net.arwix.astronomy2.core.vector.Vector

typealias IdNutation = Int

fun getNutationAngles(id: IdNutation, t: JT): NutationAngles {
    return when (id) {
        ID_IAU_1980_NUTATION -> calcNutation_IAU1980(t)
        ID_IAU_2000_NUTATION -> calcNutation_IAU2000(t)
        ID_IAU_2006_NUTATION -> calcNutation_IAU2000(t).let {
            NutationAngles(it.deltaLongitude * (1.0 + (0.4697E-6 - 2.7774E-6 * t)), it.deltaObliquity * (1.0 + (2.7774E-6 * t)))
        }
        else -> throw IndexOutOfBoundsException()
    }
}

const val ID_IAU_1980_NUTATION: IdNutation = 2
const val ID_IAU_2000_NUTATION: IdNutation = 3
const val ID_IAU_2006_NUTATION: IdNutation = 4

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

fun createObliquityElements(id: IdNutation, t: JT, obliquity: Obliquity? = null): NutationElements = object : NutationElements {
    override val id = id
    override val t: JT = t
    override val angles = getNutationAngles(id, t)
    override val eclipticMatrix = createEclipticNutationMatrix(angles)
    override val equatorialMatrix = obliquity?.let { createEquatorialNutationMatrix(angles, it) }

    override fun applyNutationToEclipticVector(vector: Vector) = eclipticMatrix * vector
    override fun removeNutationFromEclipticVector(vector: Vector) = vector * eclipticMatrix
    override fun applyNutationToEquatorialVector(vector: Vector) = equatorialMatrix!! * vector
    override fun removeNutationFromEquatorialVector(vector: Vector) = vector * equatorialMatrix!!

}