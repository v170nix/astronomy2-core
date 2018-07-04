package net.arwix.astronomy2.core.ephemeris.precession

import net.arwix.astronomy2.core.ARCSEC_TO_RAD
import net.arwix.astronomy2.core.Ecliptic
import net.arwix.astronomy2.core.Equatorial
import net.arwix.astronomy2.core.JT
import net.arwix.astronomy2.core.ephemeris.nutation.ID_NUTATION_IAU_1980
import net.arwix.astronomy2.core.ephemeris.nutation.ID_NUTATION_IAU_2000
import net.arwix.astronomy2.core.ephemeris.nutation.ID_NUTATION_IAU_2006
import net.arwix.astronomy2.core.ephemeris.nutation.IdNutation
import net.arwix.astronomy2.core.ephemeris.obliquity.*
import net.arwix.astronomy2.core.vector.Matrix
import net.arwix.astronomy2.core.vector.Matrix.Companion.AXIS_X
import net.arwix.astronomy2.core.vector.Matrix.Companion.AXIS_Z
import net.arwix.astronomy2.core.vector.RectangularVector

typealias IdPrecession = Int


/**
 * Precession for selecting IAU 1976 formulae of precession, nutation (IAU
 * 1980), and Greenwich mean sidereal time. This will use
 * old formulae that will match results from IMCCE ephemeris server. You
 * may consider using this formulae for VSOP82 theory. See
 * J. H. Lieske, T. Lederle, W. Fricke, and B. Morando, "Expressions for the
 * Precession Quantities Based upon the IAU (1976) System of Astronomical
 * Constants," Astronomy and Astrophysics 58, 1-16 (1977).
 */
@Ecliptic
const val ID_PRECESSION_IAU_1976: IdPrecession = 1

/**
 * Precession for selecting Laskar formulae of precession, nutation (IAU
 * 1980), and Greenwich mean sidereal time. See J. Laskar,
 * "Secular terms of classical planetary theories using the results of
 * general theory," Astronomy and Astrophysics 157, 59070 (1986).
 */
@Ecliptic
const val ID_PRECESSION_LASKAR_1986: IdPrecession = 2

/**
 * Precession for selecting Williams formulae of precession (DE403 JPL
 * Ephemeris), nutation (IAU 1980), obliquity, and Greenwich mean sidereal
 * time. See James G. Williams, "Contributions to the Earth's obliquity rate,
 * precession, and nutation," Astron. J. 108, 711-724 (1994). It is convenient
 * to use this when obtaining ephemeris of the Moon using Moshier method.
 */
@Ecliptic
const val ID_PRECESSION_WILLIAMS_1994: IdPrecession = 3

/**
 * Precession for selecting SIMON formulae of precession, obliquity,
 * nutation (IAU 1980), and Greenwich mean sidereal time. See
 * J. L. Simon, P. Bretagnon, J. Chapront, M. Chapront-Touze', G. Francou,
 * and J. Laskar, "Numerical Expressions for precession formulae and mean
 * elements for the Moon and the planets," Astronomy and Astrophysics 282,
 * 663-683 (1994).
 */
@Ecliptic
const val ID_PRECESSION_SIMON_1994: IdPrecession = 4

/**
 * Precession for selecting JPL DE403/404/405/406 formulae for precession,
 * obliquity, nutation (IAU 1980), and Greenwich mean sidereal time. Quite
 * similar to Williams formulae. Adequate for planets using Moshier
 * method, Series96, or JPL DE40x ephemerides.
 */
@Ecliptic
const val ID_PRECESSION_DE4xx: IdPrecession = 5

/**
 * Precession following IAU2000 definitions. From SOFA software library.
 * Reference: Capitaine et al., Astronomy & Astrophysics 400, 1145-1154,
 * 2003. See also Lieske et al. 1977.
 */
@Equatorial
const val ID_PRECESSION_IAU_2000: IdPrecession = 6

/**
 * Precession following Capitaine et al. 2003.
 *
 * Capitaine formula of precession is to be officially adopted by the IAU,
 * see recommendation in the report of the IAU Division I Working Group on
 * Precession and the Ecliptic (Hilton et al. 2006, Celest. Mech., 94,
 * 351-367).
 * Reference: Capitaine et al., Astronomy & Astrophysics 412, 567-586,
 * 2003.
 */
@Equatorial
const val ID_PRECESSION_IAU_2006: IdPrecession = 7

/**
 * Same as IAU2006, but planetary rotation models are those recommended by
 * the IAU working group on carthographic coordinates, in 2009.
 */
@Equatorial
const val ID_PRECESSION_IAU_2009: IdPrecession = 8

/**
 * Precession following Vondrak et al. 2011. See A&amp;A 534, A22.
 */
@Equatorial
const val ID_PRECESSION_VONDRAK_2011: IdPrecession = 9


@Ecliptic
fun createEclipticPrecessionMatrix(id: IdPrecession, jT: JT): Matrix {
    return when (id) {
        ID_PRECESSION_IAU_1976 -> createEclipticMatrix(IAU_1976_Matrices, jT)
        ID_PRECESSION_LASKAR_1986 -> createEclipticMatrix(LASKAR_1986_Matrices, jT)
        ID_PRECESSION_WILLIAMS_1994 -> createEclipticMatrix(WILLIAMS_1994_Matrices, jT)
        ID_PRECESSION_SIMON_1994 -> createEclipticMatrix(SIMON_1994_Matrices, jT)
        ID_PRECESSION_DE4xx -> createEclipticMatrix(JPL_DE4xx_Matrices, jT)
        else -> throw IndexOutOfBoundsException()
    }
}

@Equatorial
fun createEquatorialPrecessionMatrix(id: IdPrecession, jT: JT): Matrix {
    return when (id) {
        ID_PRECESSION_IAU_2000 -> createPrecessionIAU2000Matrix(jT, true)
        ID_PRECESSION_IAU_2006 -> createIAU2006Matrix(jT)
        ID_PRECESSION_IAU_2009 -> createIAU2006Matrix(jT)
        ID_PRECESSION_VONDRAK_2011 -> createVondrakMatrix(jT)
        else -> throw IndexOutOfBoundsException()
    }
}

fun isEclipticPrecession(idPrecession: IdPrecession): Boolean = when (idPrecession) {
    ID_PRECESSION_IAU_1976,
    ID_PRECESSION_LASKAR_1986,
    ID_PRECESSION_WILLIAMS_1994,
    ID_PRECESSION_SIMON_1994,
    ID_PRECESSION_DE4xx -> true
    ID_PRECESSION_IAU_2000,
    ID_PRECESSION_IAU_2006,
    ID_PRECESSION_IAU_2009,
    ID_PRECESSION_VONDRAK_2011 -> false
    else -> throw IndexOutOfBoundsException()
}

fun createPrecessionElements(id: IdPrecession, jT: JT): PrecessionElements = object : PrecessionElements {
    override val id: IdPrecession = id
    override val jT: JT = jT
    override val isEcliptic: Boolean = isEclipticPrecession(id)
    override val fromJ2000Matrix: Matrix = if (isEcliptic) createEclipticPrecessionMatrix(id, jT) else
        createEquatorialPrecessionMatrix(id, jT)
    override val toJ2000Matrix: Matrix = if (id != ID_PRECESSION_IAU_2000) fromJ2000Matrix.transpose() else
        createPrecessionIAU2000Matrix(jT, false)
}

fun findNearestObliquityModel(idPrecession: IdPrecession): IdObliquity = when (idPrecession) {
    ID_PRECESSION_VONDRAK_2011 -> ID_OBLIQUITY_VONDRAK_2011
    ID_PRECESSION_IAU_2000,
    ID_PRECESSION_IAU_2006,
    ID_PRECESSION_IAU_2009 -> ID_OBLIQUITY_IAU_2006
    ID_PRECESSION_WILLIAMS_1994,
    ID_PRECESSION_DE4xx -> ID_OBLIQUITY_WILLIAMS_1994
    ID_PRECESSION_SIMON_1994 -> ID_OBLIQUITY_SIMON_1994
    ID_PRECESSION_LASKAR_1986 -> ID_OBLIQUITY_LASKAR_1996
    ID_PRECESSION_IAU_1976 -> ID_OBLIQUITY_IAU_1976
    else -> throw IndexOutOfBoundsException()
}

fun findNearestNutationModel(idPrecession: IdPrecession): IdNutation = when (idPrecession) {
    ID_PRECESSION_IAU_2009,
    ID_PRECESSION_IAU_2006 -> ID_NUTATION_IAU_2006
    ID_PRECESSION_WILLIAMS_1994,
    ID_PRECESSION_DE4xx,
    ID_PRECESSION_SIMON_1994,
    ID_PRECESSION_LASKAR_1986,
    ID_PRECESSION_IAU_1976 -> ID_NUTATION_IAU_1980
    else -> ID_NUTATION_IAU_2000
}

@Equatorial
fun createPrecessionIAU2000Matrix(T: Double, isFromJ2000ToApparent: Boolean): Matrix {
    val T0: Double = if (isFromJ2000ToApparent) T else 0.0
    var EPS0 = 84381.448
    val (PSIA, OMEGAA, CHIA) = RectangularVector(
            ((((-0.0 * T + 0.0) * T - 0.001147) * T - 1.07259) * T + 5038.7784) * T - 0.29965 * T0,
            ((((+0.0 * T - 0.0) * T - 0.007726) * T + 0.05127) * T - 0.0) * T + EPS0 - 0.02524 * T0,
            ((((-0.0 * T + 0.0) * T - 0.001125) * T - 2.38064) * T + 10.5526) * T) * ARCSEC_TO_RAD

    EPS0 *= ARCSEC_TO_RAD
    return Matrix(AXIS_Z, CHIA) * Matrix(AXIS_X, -OMEGAA) * Matrix(AXIS_Z, -PSIA) * Matrix(AXIS_X, EPS0)
}

private fun createIAU2006Matrix(T: Double): Matrix {
    var EPS0 = 84381.406
    val (PSIA, OMEGAA, CHIA) = RectangularVector(
            ((((-0.0000000951 * T + 0.000132851) * T - 0.00114045) * T - 1.0790069) * T + 5038.481507) * T,
            ((((+0.0000003337 * T - 0.000000467) * T - 0.00772503) * T + 0.0512623) * T - 0.025754) * T + EPS0,
            ((((-0.0000000560 * T + 0.000170663) * T - 0.00121197) * T - 2.3814292) * T + 10.556403) * T) * ARCSEC_TO_RAD
    EPS0 *= ARCSEC_TO_RAD
    return Matrix(AXIS_Z, CHIA) * Matrix(AXIS_X, -OMEGAA) * Matrix(AXIS_Z, -PSIA) * Matrix(AXIS_X, EPS0)
}

private fun createEclipticMatrix(list: Array<DoubleArray>, jT: JT, isFromJ2000ToApparent: Boolean = true): Matrix {
    val jT10 = jT / 10.0 /* thousands of years */
    val pA = ARCSEC_TO_RAD * jT10 * list[0].fold(0.0) { acc, d -> acc * jT10 + d }
    val W = list[1].fold(0.0) { acc, d -> acc * jT10 + d }
    val z = list[2].fold(0.0) { acc, d -> acc * jT10 + d }.let { if (!isFromJ2000ToApparent) -it else it }

    return Matrix(AXIS_Z, -(W + pA)) * Matrix(AXIS_X, z) * Matrix(AXIS_Z, W)
}

private val WILLIAMS_1994_Matrices by lazy {
    arrayOf(
            doubleArrayOf(-8.66e-10, -4.759e-8, 2.424e-7, 1.3095e-5, 1.7451e-4, -1.8055e-3, -0.235316, 0.076, 110.5407, 50287.70000),
            /* Pi from Williams' 1994 paper, in radians. */
            doubleArrayOf(6.6402e-16, -2.69151e-15, -1.547021e-12, 7.521313e-12, 1.9e-10, -3.54e-9, -1.8103e-7, 1.26e-7, 7.436169e-5, -0.04207794833, 3.052115282424),
            doubleArrayOf(1.2147e-16, 7.3759e-17, -8.26287e-14, 2.503410e-13, 2.4650839e-11, -5.4000441e-11, 1.32115526e-9, -6.012e-7, -1.62442e-5, 0.00227850649, 0.0))
}
private val JPL_DE4xx_Matrices by lazy {
    arrayOf(
            doubleArrayOf(-8.66e-10, -4.759e-8, 2.424e-7, 1.3095e-5, 1.7451e-4, -1.8055e-3, -0.235316, 0.076, 110.5414, 50287.91959),
            /* Pi from Williams' 1994 paper, in radians. No change in DE403. */
            doubleArrayOf(6.6402e-16, -2.69151e-15, -1.547021e-12, 7.521313e-12, 1.9e-10, -3.54e-9, -1.8103e-7, 1.26e-7, 7.436169e-5, -0.04207794833, 3.052115282424),
            doubleArrayOf(1.2147e-16, 7.3759e-17, -8.26287e-14, 2.503410e-13, 2.4650839e-11, -5.4000441e-11, 1.32115526e-9, -6.012e-7, -1.62442e-5, 0.00227850649, 0.0))
}
private val SIMON_1994_Matrices by lazy {
    arrayOf(
            doubleArrayOf(-8.66e-10, -4.759e-8, 2.424e-7, 1.3095e-5, 1.7451e-4, -1.8055e-3, -0.235316, 0.07732, 111.2022, 50288.200),
            doubleArrayOf(6.6402e-16, -2.69151e-15, -1.547021e-12, 7.521313e-12, 1.9e-10, -3.54e-9, -1.8103e-7, 2.579e-8, 7.4379679e-5, -0.0420782900, 3.0521126906),
            doubleArrayOf(1.2147e-16, 7.3759e-17, -8.26287e-14, 2.503410e-13, 2.4650839e-11, -5.4000441e-11, 1.32115526e-9, -5.99908e-7, -1.624383e-5, 0.002278492868, 0.0))
}

private val LASKAR_1986_Matrices by lazy {
    arrayOf(
            doubleArrayOf(-8.66e-10, -4.759e-8, 2.424e-7, 1.3095e-5, 1.7451e-4, -1.8055e-3, -0.235316, 0.07732, 111.1971, 50290.966),
            doubleArrayOf(6.6402e-16, -2.69151e-15, -1.547021e-12, 7.521313e-12, 6.3190131e-10, -3.48388152e-9, -1.813065896e-7, 2.75036225e-8, 7.4394531426e-5, -0.042078604317, 3.052112654975),
            doubleArrayOf(1.2147e-16, 7.3759e-17, -8.26287e-14, 2.503410e-13, 2.4650839e-11, -5.4000441e-11, 1.32115526e-9, -5.998737027e-7, -1.6242797091e-5, 0.002278495537, 0.0))
}

private val IAU_1976_Matrices by lazy {
    arrayOf(
            doubleArrayOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -0.006, 111.113, 50290.966),
            doubleArrayOf(6.6402e-16, -2.69151e-15, -1.547021e-12, 7.521313e-12, 6.3190131e-10, -3.48388152e-9, -1.813065896e-7, 2.75036225e-8, 7.4394531426e-5, -0.042078604317, 3.052112654975),
            doubleArrayOf(1.2147e-16, 7.3759e-17, -8.26287e-14, 2.503410e-13, 2.4650839e-11, -5.4000441e-11, 1.32115526e-9, -5.998737027e-7, -1.6242797091e-5, 0.002278495537, 0.0))
}
