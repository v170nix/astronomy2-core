package net.arwix.astronomy2.core.ephemeris.obliquity

import net.arwix.astronomy2.core.*
import net.arwix.astronomy2.core.math.polynomialSum
import net.arwix.astronomy2.core.vector.Matrix
import net.arwix.astronomy2.core.vector.Matrix.Companion.AXIS_X
import net.arwix.astronomy2.core.vector.Vector
import kotlin.lazy
import kotlin.math.cos
import kotlin.math.sin

typealias Obliquity = Radian
typealias ObliquityId = Int

fun getObliquity(id: ObliquityId, t: JT): Obliquity = when (id) {
    ID_OBLIQUITY_WILLIAMS_1994 -> getEps(t, rvalStart_WIL, coeffs_WIL)
    ID_OBLIQUITY_SIMON_1994 -> getEps(t, rvalStart_SIM, coeffs_SIM)
    ID_OBLIQUITY_LASKAR_1996 -> getEps(t, rvalStart_LAS, coeffs_LAS)
    ID_OBLIQUITY_IAU_1976 -> getEps(t, rvalStart_IAU, coeffs_IAU)
    ID_OBLIQUITY_IAU_2006 -> getEps(t, rvalStart_CAP, coeffs_CAP)
    ID_OBLIQUITY_VONDRAK_2011 -> (PI2 * t).let { w ->
        xyper.sumByDouble { doubles: DoubleArray ->
            (w / doubles[0]).let { a ->
                cos(a) * doubles[1] + sin(a) * doubles[2]
            }
        } + xypol.polynomialSum(t)
    } * ARCSEC_TO_RAD
    else -> throw IndexOutOfBoundsException()
}

// ecliptic to equatorial
fun getObliquityMatrix(id: ObliquityId, t: JT): Matrix = Matrix.getRotateX(-getObliquity(id, t))

fun createObliquityElements(id: ObliquityId, t: JT): ObliquityElements = object : ObliquityElements {
    override val id = id
    override val t: JT = t
    override val obliquity = getObliquity(id, t)
    override val eclipticToEquatorialMatrix = Matrix(AXIS_X, -getObliquity(id, t))
    override val equatorialToEclipticMatrix = eclipticToEquatorialMatrix.transpose()

    override fun rotateEclipticVector(vector: Vector): Vector {
        return eclipticToEquatorialMatrix * vector
    }

    override fun rotateEquatorialVector(vector: Vector): Vector {
        return equatorialToEclipticMatrix * vector
    }
}


private fun getEps(t: Double, rvalStart: Double, coeffs: DoubleArray): Radian {
    return (rvalStart + coeffs.polynomialSum(t / 100.0)) * ARCSEC_TO_RAD
}

// Williams et al., DE403 Ephemeris
const val ID_OBLIQUITY_WILLIAMS_1994: ObliquityId = 1
private val rvalStart_WIL by lazy { 23.0 * SECONDS_PER_DEGREE + 26.0 * MINUTES_PER_DEGREE + 21.406173 }
private val coeffs_WIL by lazy { doubleArrayOf(0.0, -4683.396, -1.75, 1998.9, -51.38, -249.67, -39.05, 7.12, 27.87, 5.79, 2.45) }

// Simon et al., 1994
const val ID_OBLIQUITY_SIMON_1994: ObliquityId = 2
private val rvalStart_SIM by lazy { 23.0 * SECONDS_PER_DEGREE + 26.0 * MINUTES_PER_DEGREE + 21.412 }
private val coeffs_SIM by lazy { doubleArrayOf(0.0, -4680.927, -1.52, 1998.9, -51.38, -249.67, -39.05, 7.12, 27.87, 5.79, 2.45) }


// Laskar et al.
/*
 * This expansion is from Laskar, cited above. Bretagnon and Simon say,
 * in Planetary Programs and Tables, that it is accurate to 0.1" over a
 * span of 6000 years. Laskar estimates the precision to be 0.01" after
 * 1000 years and a few seconds of arc after 10000 years.
 */
const val ID_OBLIQUITY_LASKAR_1996: ObliquityId = 3
private val rvalStart_LAS by lazy { 23.0 * SECONDS_PER_DEGREE + 26.0 * MINUTES_PER_DEGREE + 21.448 }
private val coeffs_LAS by lazy { doubleArrayOf(0.0, -4680.93, -1.55, 1999.25, -51.38, -249.67, -39.05, 7.12, 27.87, 5.79, 2.45) }

// IAU 1976
const val ID_OBLIQUITY_IAU_1976: ObliquityId = 4
private val rvalStart_IAU by lazy { 23.0 * SECONDS_PER_DEGREE + 26.0 * MINUTES_PER_DEGREE + 21.448 }
private val coeffs_IAU by lazy { doubleArrayOf(0.0, -4681.5, -5.9, 1813.0) }

// Capitaine et al. 2003, Hilton et al. 2006
const val ID_OBLIQUITY_IAU_2006: ObliquityId = 5
private val rvalStart_CAP by lazy { 23.0 * SECONDS_PER_DEGREE + 26.0 * MINUTES_PER_DEGREE + 21.406 }
private val coeffs_CAP by lazy { doubleArrayOf(0.0, -4683.6769, -1.831, 2003.400, -57.6, -434.0) }

const val ID_OBLIQUITY_VONDRAK_2011: ObliquityId = 6
private val xypol by lazy { doubleArrayOf(84028.206305, 0.3624445, -0.00004039, -110E-9) }
private val xyper by lazy {
    arrayOf(
            doubleArrayOf(409.90, 753.872780, -1704.720302),
            doubleArrayOf(396.15, -247.805823, -862.308358),
            doubleArrayOf(537.22, 379.471484, 447.832178),
            doubleArrayOf(402.90, -53.880558, -889.571909),
            doubleArrayOf(417.15, -90.109153, 190.402846),
            doubleArrayOf(288.92, -353.600190, -56.564991),
            doubleArrayOf(4043.00, -63.115353, -296.222622),
            doubleArrayOf(306.00, -28.248187, -75.859952),
            doubleArrayOf(277.00, 17.703387, 67.473503),
            doubleArrayOf(203.00, 38.911307, 3.014055))
}


