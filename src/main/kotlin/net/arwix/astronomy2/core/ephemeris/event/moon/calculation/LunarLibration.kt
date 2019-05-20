package net.arwix.astronomy2.core.ephemeris.event.moon.calculation

import net.arwix.astronomy2.core.*
import net.arwix.astronomy2.core.ephemeris.nutation.NutationAngles
import net.arwix.astronomy2.core.ephemeris.obliquity.ID_OBLIQUITY_WILLIAMS_1994
import net.arwix.astronomy2.core.ephemeris.obliquity.getObliquity
import net.arwix.astronomy2.core.math.normalize
import net.arwix.astronomy2.core.math.polynomialSum
import net.arwix.astronomy2.core.math.toRad
import net.arwix.astronomy2.core.vector.Matrix
import net.arwix.astronomy2.core.vector.SphericalVector
import net.arwix.astronomy2.core.vector.Vector
import net.arwix.astronomy2.core.vector.convert
import kotlin.math.*

/**
 * distance in AU
 */
fun isSupermoon(distance: Double) = distance * AU < 382_900

/**
 * distance in AU
 */
fun isMicromoon(distance: Double) = distance * AU > 405_000


/**
 * Obtains the orientation of the Moon according to Eckhardt's analytical theory.
 * The results in current dates are about 0.1 deg from those obtained using IAU
 * recommendations, and it seems the values from Eckhardt's theory are closer
 * to those obtained using JPL ephemerides, for a few centuries around year 2000.
 * IAU lunar rotation model is no longer used in JPARSEC.<P>
 *
 * An adequate ephemeris object should be provided, with the algorithm to apply to
 * obtain the lunar position. The returning values will be referred to the geocenter
 * or the topocentric place depending on it.</P><P>
 *
 * Reference:</P><P>
 *
 * Eckhardt, D.H., "Theory of the Libration of the Moon", Moon and planets 25, 3 (1981).
 *
 * @param jT Time object.
 * @param moon Ephemeris object.
 * @return An array with the total librations (longitude and planetocentric
 * latitude), and the position angle of axis as the 3rd element. In radians, for
 * mean equinox of date.
</P> */
fun getEckhardtMoonLibrations(jT: JT,
                              nutationAngles: NutationAngles,
                              eclipticToEquatorialMatrix: Matrix,
                              @Geocentric
                              @Equatorial
                              @Apparent moon: Vector): DoubleArray {
    val sphericalMoon = convert<SphericalVector>(moon)

    val moonLon = sphericalMoon.phi
    val moonLat = sphericalMoon.theta

    // Obtain mean parameters for the Moon.
    val k1 = (119.75 + 131.849 * jT) * DEG_TO_RAD
    val k2 = (72.56 + 20.186 * jT) * DEG_TO_RAD
    // Mean elongation of Moon
    val D = doubleArrayOf(297.8502042, 445267.1115168, -0.0016300, 1.0 / 545868.0, -1.0 / 113065000.0).polynomialSum(jT).toRad()
    // Sun's mean anomaly
    val M = doubleArrayOf(357.5291092, 35999.0502909, -0.0001536, 1.0 / 24490000.0).polynomialSum(jT).toRad()
    // Moon's mean anomaly
    val Mp = doubleArrayOf(134.9634114, 477198.8676313, 0.0089979, 1.0 / 69699.0, -1.0 / 14712000.0).polynomialSum(jT).toRad()
    // Earth's eccentricity
    val E = 1.0 - 0.002516 * jT - 0.0000074 * jT * jT
    // Moon's argument of latitude
    val F = doubleArrayOf(93.2720993, 483202.0175273, -0.0034029, -1.0 / 3526000.0, 1.0 / 863310000.0).polynomialSum(jT).toRad()
    // Moon's inclination
    val I = 1.54242 * DEG_TO_RAD
    // Moon's mean ascending node longitude
    val omega = doubleArrayOf(125.0445550, -1934.1361849, 0.0020762, 1.0 / 467410.0, -1.0 / 18999000.0).polynomialSum(jT).toRad()

    // Obtain optical librations
//        val nutLon = getNutationAngles(ID_NUTATION_IAU_1980, jT).deltaLongitude
    val W = moonLon - nutationAngles.deltaLongitude - omega
    val sinA = sin(W) * cos(moonLat) * cos(I) - sin(moonLat) * sin(I)
    val cosA = cos(W) * cos(moonLat)
    val A = atan2(sinA, cosA)
    val lp = (A - F).normalize()
    val sinbp = -sin(W) * cos(moonLat) * sin(I) - sin(moonLat) * cos(I)
    val bp = asin(sinbp)

    // Obtain rho, sigma, and tau parameters
    var rho = -0.02752 * cos(Mp) - 0.02245 * sin(F) + 0.00684 * cos(Mp - 2.0 * F)
    rho -= 0.00293 * cos(2.0 * F) + 0.00085 * cos(2.0 * F - 2.0 * D) + 0.00054 * cos(Mp - 2.0 * D)
    rho -= 0.00020 * sin(Mp + F) + 0.00020 * cos(Mp + 2.0 * F) + 0.00020 * cos(Mp - F)
    rho += 0.00014 * cos(Mp + 2.0 * F - 2.0 * D)

    var sigma = -0.02816 * sin(Mp) + 0.02244 * cos(F) - 0.00682 * sin(Mp - 2.0 * F)
    sigma += -0.00279 * sin(2.0 * F) - 0.00083 * sin(2.0 * F - 2.0 * D) + 0.00069 * sin(Mp - 2.0 * D)
    sigma += 0.00040 * cos(Mp + F) - 0.00025 * sin(2.0 * Mp) - 0.00023 * sin(Mp + 2.0 * F)
    sigma += 0.00020 * cos(Mp - F) - 0.00019 * sin(Mp - F) + 0.00013 * sin(Mp + 2.0 * F - 2.0 * D)
    sigma += -0.00010 * cos(Mp - 3.0 * F)

    var tau = 0.02520 * E * sin(M) + 0.00473 * sin(2.0 * Mp - 2.0 * F) - 0.00467 * sin(Mp)
    tau += 0.00396 * sin(k1) + 0.00276 * sin(2.0 * Mp - 2.0 * D) + 0.00196 * sin(omega)
    tau += -0.00183 * cos(Mp - F) + 0.00115 * sin(Mp - 2.0 * D) - 0.00096 * sin(Mp - D)
    tau += 0.00046 * sin(2.0 * F - 2.0 * D) - 0.00039 * sin(Mp - F) - 0.00032 * sin(Mp - M - D)
    tau += 0.00027 * sin(2.0 * Mp - M - 2.0 * D) + 0.00023 * sin(k2) - 0.00014 * sin(2.0 * D)
    tau += 0.00014 * cos(2.0 * Mp - 2.0 * F) - 0.00012 * sin(Mp - 2.0 * F) - 0.00012 * sin(2.0 * Mp)
    tau += 0.00011 * sin(2.0 * Mp - 2.0 * M - 2.0 * D)

    rho *= DEG_TO_RAD
    sigma *= DEG_TO_RAD
    tau *= DEG_TO_RAD

    // Obtain physical librations
    val lpp = -tau + (rho * Math.cos(A) + sigma * Math.sin(A)) * Math.tan(bp)
    val bpp = sigma * Math.cos(A) - rho * Math.sin(A)

    // Obtain total librations
    val l = lp + lpp
    val b = bp + bpp

    val eps = getObliquity(ID_OBLIQUITY_WILLIAMS_1994, jT)

    // Obtain position angle of axis
    val v = omega + nutationAngles.deltaLongitude + sigma / sin(I)
    val x = sin(I + rho) * sin(v)
    val y = sin(I + rho) * cos(v) * cos(eps) - cos(I + rho) * sin(eps)
    val w = atan2(x, y)

    val equ = convert<SphericalVector>(eclipticToEquatorialMatrix * moon)

    val sinp = sqrt(x * x + y * y) * cos(equ.phi - w) / Math.cos(b)
    val p = asin(sinp)

    return doubleArrayOf(l, b, p)
}


private val moonApogeeCoefficients = arrayOf(doubleArrayOf(2.0, 0.0, 0.0, 0.4392, 0.0), doubleArrayOf(4.0, 0.0, 0.0, 0.0684, 0.0), doubleArrayOf(0.0, 1.0, 0.0, 0.0456, -0.00011), doubleArrayOf(2.0, -1.0, 0.0, 0.0426, -0.00011), doubleArrayOf(0.0, 0.0, 2.0, 0.0212, 0.0), doubleArrayOf(1.0, 0.0, 0.0, -0.0189, 0.0), doubleArrayOf(6.0, 0.0, 0.0, 0.0144, 0.0), doubleArrayOf(4.0, -1.0, 0.0, 0.0113, 0.0), doubleArrayOf(2.0, 0.0, 2.0, 0.0047, 0.0), doubleArrayOf(1.0, 1.0, 0.0, 0.0036, 0.0), doubleArrayOf(8.0, 0.0, 0.0, 0.0035, 0.0), doubleArrayOf(6.0, -1.0, 0.0, 0.0034, 0.0), doubleArrayOf(2.0, 0.0, -2.0, -0.0034, 0.0), doubleArrayOf(2.0, -2.0, 0.0, 0.0022, 0.0), doubleArrayOf(3.0, 0.0, 0.0, -0.0017, 0.0), doubleArrayOf(4.0, 0.0, 2.0, 0.0013, 0.0), doubleArrayOf(8.0, -1.0, 0.0, 0.0011, 0.0), doubleArrayOf(4.0, -2.0, 0.0, 0.0010, 0.0), doubleArrayOf(10.0, 0.0, 0.0, 0.0009, 0.0), doubleArrayOf(3.0, 1.0, 0.0, 0.0007, 0.0), doubleArrayOf(0.0, 2.0, 0.0, 0.0006, 0.0), doubleArrayOf(2.0, 1.0, 0.0, 0.0005, 0.0), doubleArrayOf(2.0, 2.0, 0.0, 0.0005, 0.0), doubleArrayOf(6.0, 0.0, 2.0, 0.0004, 0.0), doubleArrayOf(6.0, -2.0, 0.0, 0.0004, 0.0), doubleArrayOf(10.0, -1.0, 0.0, 0.0004, 0.0), doubleArrayOf(5.0, 0.0, 0.0, -0.0004, 0.0), doubleArrayOf(4.0, 0.0, -2.0, -0.0004, 0.0), doubleArrayOf(0.0, 1.0, 2.0, 0.0003, 0.0), doubleArrayOf(12.0, 0.0, 0.0, 0.0003, 0.0), doubleArrayOf(2.0, -1.0, 2.0, 0.0003, 0.0), doubleArrayOf(1.0, -1.0, 0.0, -0.0003, 0.0))
private val moonApogeeParalllaxCoefficients = arrayOf(doubleArrayOf(2.0, 0.0, 0.0, -9.147, 0.0), doubleArrayOf(1.0, 0.0, 0.0, -0.841, 0.0), doubleArrayOf(0.0, 0.0, 2.0, 0.697, 0.0), doubleArrayOf(0.0, 1.0, 0.0, -0.656, 0.0016), doubleArrayOf(4.0, 0.0, 0.0, 0.355, 0.0), doubleArrayOf(2.0, -1.0, 0.0, 0.159, 0.0), doubleArrayOf(1.0, 1.0, 0.0, 0.127, 0.0), doubleArrayOf(4.0, -1.0, 0.0, 0.065, 0.0), doubleArrayOf(6.0, 0.0, 0.0, 0.052, 0.0), doubleArrayOf(2.0, 1.0, 0.0, 0.043, 0.0), doubleArrayOf(2.0, 0.0, 2.0, 0.031, 0.0), doubleArrayOf(2.0, 0.0, -2.0, -0.023, 0.0), doubleArrayOf(2.0, -2.0, 0.0, 0.022, 0.0), doubleArrayOf(2.0, 2.0, 0.0, 0.019, 0.0), doubleArrayOf(0.0, 2.0, 0.0, -0.016, 0.0), doubleArrayOf(6.0, -1.0, 0.0, 0.014, 0.0), doubleArrayOf(8.0, 0.0, 0.0, 0.010, 0.0))
