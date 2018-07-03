package net.arwix.astronomy2.core.ephemeris.nutation

import net.arwix.astronomy2.core.ARCSEC_TO_RAD
import net.arwix.astronomy2.core.math.mod3600
import kotlin.math.cos
import kotlin.math.sin

internal fun calcNutation_IAU1980(t: Double): Pair<Double, Double> {

    /**
     * Array to hold sines of multiple angles
     */
    val ss = Array(5) { DoubleArray(8) }

    /**
     * Array to hold cosines of multiple angles
     */
    val cc = Array(5) { DoubleArray(8) }


    /**
     * Prepare lookup table of sin and cos ( i*Lj ) for required multiple angles
     */
    fun sscc(k: Int, arg: Double, n: Int) {
        var s: Double
        val su = sin(arg)
        val cu = cos(arg)
        ss[k][0] = su /* sin(L) */
        cc[k][0] = cu /* cos(L) */
        var sv = 2.0 * su * cu
        var cv = cu * cu - su * su
        ss[k][1] = sv /* sin(2L) */
        cc[k][1] = cv

        var i = 2
        while (i < n) {
            s = su * cv + cu * sv
            cv = cu * cv - su * sv
            sv = s
            ss[k][i] = sv /* sin( i+1 L ) */
            cc[k][i] = cv
            i++
        }
    }

    var f: Double
    var g: Double
    var cu: Double
    var su: Double
    var cv: Double
    var sv: Double
    var sw: Double

    var j: Int
    var k: Int
    var k1: Int
    var m: Int

    val T2 = t * t
    val T10 = t / 10.0

    /* Fundamental arguments in the FK5 reference system. */

    /**
     * longitude of the mean ascending node of the lunar orbit on the
     * ecliptic, measured from the mean equinox of date
     */
    val OM = ((-6962890.539 * t + 450160.280).mod3600() + (0.008 * t + 7.455) * T2) * ARCSEC_TO_RAD

    /**
     * mean longitude of the Sun minus the mean longitude of the Sun's
     * perigee
     */
    val MS = ((129596581.224 * t + 1287099.804).mod3600() - (0.012 * t + 0.577) * T2) * ARCSEC_TO_RAD

    /**
     * mean longitude of the Moon minus the mean longitude of the Moon's
     * perigee
     */
    val MM = ((1717915922.633 * t + 485866.733).mod3600() + (0.064 * t + 31.310) * T2) * ARCSEC_TO_RAD

    /**
     * mean longitude of the Moon minus the mean longitude of the Moon's
     * node
     */
    val FF = ((1739527263.137 * t + 335778.877).mod3600() + (0.011 * t - 13.257) * T2) * ARCSEC_TO_RAD

    /**
     * mean elongation of the Moon from the Sun.
     */
    val DD = ((1602961601.328 * t + 1072261.307).mod3600() + (0.019 * t - 6.891) * T2) * ARCSEC_TO_RAD

    /**
     * Calculate sin( i*MM ), etc. for needed multiple angles
     */
    sscc(0, MM, 3)
    sscc(1, MS, 2)
    sscc(2, FF, 4)
    sscc(3, DD, 4)
    sscc(4, OM, 2)

    var C = 0.0
    var D = 0.0
    var p = -1 /* point to start of table */

    var i = 0
    while (i < 105) {
        /* argument of sine and cosine */
        k1 = 0
        cv = 0.0
        sv = 0.0
        m = 0
        while (m < 5) {
            p++
            j = IAU1980_NT[p]
            if (j != 0) {
                k = j
                if (j < 0)
                    k = -k
                su = ss[m][k - 1] /* sin(k*angle) */
                if (j < 0)
                    su = -su
                cu = cc[m][k - 1]
                if (k1 == 0) { /* set first angle */
                    sv = su
                    cv = cu
                    k1 = 1
                } else { /* combine angles */
                    sw = su * cv + cu * sv
                    cv = cu * cv - su * sv
                    sv = sw
                }
            }
            m++
        }
        /* longitude coefficient */
        p++
        f = IAU1980_NT[p].toDouble()
        p++
        k = IAU1980_NT[p]
        if (k != 0)
            f += T10 * k

        /* obliquity coefficient */
        p++
        g = IAU1980_NT[p].toDouble()
        p++
        k = IAU1980_NT[p]
        if (k != 0)
            g += T10 * k

        /* accumulate the terms */
        C += f * sv
        D += g * cv
        i++
    }

    /* first terms, not in table: */
    C += (-1742.0 * T10 - 171996.0) * ss[4][0] /* sin(OM) */
    D += (89.0 * T10 + 92025.0) * cc[4][0] /* cos(OM) */

    /* Save answers, expressed in radians */
    val nutationInLongitude = 0.0001 * ARCSEC_TO_RAD * C
    val nutationInObliquity = 0.0001 * ARCSEC_TO_RAD * D


    return nutationInLongitude to nutationInObliquity
}


/**
 * IAU1980 model
 */
private val IAU1980_NT: IntArray
 = intArrayOf(0, 0, 0, 0, 2, 2062, 2, -895, 5, -2, 0, 2, 0, 1, 46, 0, -24, 0, 2, 0, -2, 0, 0, 11, 0, 0, 0, -2, 0, 2, 0, 2, -3, 0, 1, 0, 1, -1, 0, -1, 0, -3, 0, 0, 0, 0, -2, 2, -2, 1, -2, 0, 1, 0, 2, 0, -2, 0, 1, 1, 0, 0, 0, 0, 0, 2, -2, 2, -13187, -16, 5736, -31, 0, 1, 0, 0, 0, 1426, -34, 54, -1, 0, 1, 2, -2, 2, -517, 12, 224, -6, 0, -1, 2, -2, 2, 217, -5, -95, 3, 0, 0, 2, -2, 1, 129, 1, -70, 0, 2, 0, 0, -2, 0, 48, 0, 1, 0, 0, 0, 2, -2, 0, -22, 0, 0, 0, 0, 2, 0, 0, 0, 17, -1, 0, 0, 0, 1, 0, 0, 1, -15, 0, 9, 0, 0, 2, 2, -2, 2, -16, 1, 7, 0, 0, -1, 0, 0, 1, -12, 0, 6, 0, -2, 0, 0, 2, 1, -6, 0, 3, 0, 0, -1, 2, -2, 1, -5, 0, 3, 0, 2, 0, 0, -2, 1, 4, 0, -2, 0, 0, 1, 2, -2, 1, 4, 0, -2, 0, 1, 0, 0, -1, 0, -4, 0, 0, 0, 2, 1, 0, -2, 0, 1, 0, 0, 0, 0, 0, -2, 2, 1, 1, 0, 0, 0, 0, 1, -2, 2, 0, -1, 0, 0, 0, 0, 1, 0, 0, 2, 1, 0, 0, 0, -1, 0, 0, 1, 1, 1, 0, 0, 0, 0, 1, 2, -2, 0, -1, 0, 0, 0, 0, 0, 2, 0, 2, -2274, -2, 977, -5, 1, 0, 0, 0, 0, 712, 1, -7, 0, 0, 0, 2, 0, 1, -386, -4, 200, 0, 1, 0, 2, 0, 2, -301, 0, 129, -1, 1, 0, 0, -2, 0, -158, 0, -1, 0, -1, 0, 2, 0, 2, 123, 0, -53, 0, 0, 0, 0, 2, 0, 63, 0, -2, 0, 1, 0, 0, 0, 1, 63, 1, -33, 0, -1, 0, 0, 0, 1, -58, -1, 32, 0, -1, 0, 2, 2, 2, -59, 0, 26, 0, 1, 0, 2, 0, 1, -51, 0, 27, 0, 0, 0, 2, 2, 2, -38, 0, 16, 0, 2, 0, 0, 0, 0, 29, 0, -1, 0, 1, 0, 2, -2, 2, 29, 0, -12, 0, 2, 0, 2, 0, 2, -31, 0, 13, 0, 0, 0, 2, 0, 0, 26, 0, -1, 0, -1, 0, 2, 0, 1, 21, 0, -10, 0, -1, 0, 0, 2, 1, 16, 0, -8, 0, 1, 0, 0, -2, 1, -13, 0, 7, 0, -1, 0, 2, 2, 1, -10, 0, 5, 0, 1, 1, 0, -2, 0, -7, 0, 0, 0, 0, 1, 2, 0, 2, 7, 0, -3, 0, 0, -1, 2, 0, 2, -7, 0, 3, 0, 1, 0, 2, 2, 2, -8, 0, 3, 0, 1, 0, 0, 2, 0, 6, 0, 0, 0, 2, 0, 2, -2, 2, 6, 0, -3, 0, 0, 0, 0, 2, 1, -6, 0, 3, 0, 0, 0, 2, 2, 1, -7, 0, 3, 0, 1, 0, 2, -2, 1, 6, 0, -3, 0, 0, 0, 0, -2, 1, -5, 0, 3, 0, 1, -1, 0, 0, 0, 5, 0, 0, 0, 2, 0, 2, 0, 1, -5, 0, 3, 0, 0, 1, 0, -2, 0, -4, 0, 0, 0, 1, 0, -2, 0, 0, 4, 0, 0, 0, 0, 0, 0, 1, 0, -4, 0, 0, 0, 1, 1, 0, 0, 0, -3, 0, 0, 0, 1, 0, 2, 0, 0, 3, 0, 0, 0, 1, -1, 2, 0, 2, -3, 0, 1, 0, -1, -1, 2, 2, 2, -3, 0, 1, 0, -2, 0, 0, 0, 1, -2, 0, 1, 0, 3, 0, 2, 0, 2, -3, 0, 1, 0, 0, -1, 2, 2, 2, -3, 0, 1, 0, 1, 1, 2, 0, 2, 2, 0, -1, 0, -1, 0, 2, -2, 1, -2, 0, 1, 0, 2, 0, 0, 0, 1, 2, 0, -1, 0, 1, 0, 0, 0, 2, -2, 0, 1, 0, 3, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 2, 1, 2, 2, 0, -1, 0, -1, 0, 0, 0, 2, 1, 0, -1, 0, 1, 0, 0, -4, 0, -1, 0, 0, 0, -2, 0, 2, 2, 2, 1, 0, -1, 0, -1, 0, 2, 4, 2, -2, 0, 1, 0, 2, 0, 0, -4, 0, -1, 0, 0, 0, 1, 1, 2, -2, 2, 1, 0, -1, 0, 1, 0, 2, 2, 1, -1, 0, 1, 0, -2, 0, 2, 4, 2, -1, 0, 1, 0, -1, 0, 4, 0, 2, 1, 0, 0, 0, 1, -1, 0, -2, 0, 1, 0, 0, 0, 2, 0, 2, -2, 1, 1, 0, -1, 0, 2, 0, 2, 2, 2, -1, 0, 0, 0, 1, 0, 0, 2, 1, -1, 0, 0, 0, 0, 0, 4, -2, 2, 1, 0, 0, 0, 3, 0, 2, -2, 2, 1, 0, 0, 0, 1, 0, 2, -2, 0, -1, 0, 0, 0, 0, 1, 2, 0, 1, 1, 0, 0, 0, -1, -1, 0, 2, 1, 1, 0, 0, 0, 0, 0, -2, 0, 1, -1, 0, 0, 0, 0, 0, 2, -1, 2, -1, 0, 0, 0, 0, 1, 0, 2, 0, -1, 0, 0, 0, 1, 0, -2, -2, 0, -1, 0, 0, 0, 0, -1, 2, 0, 1, -1, 0, 0, 0, 1, 1, 0, -2, 1, -1, 0, 0, 0, 1, 0, -2, 2, 0, -1, 0, 0, 0, 2, 0, 0, 2, 0, 1, 0, 0, 0, 0, 0, 2, 4, 2, -1, 0, 0, 0, 0, 1, 0, 1, 0, 1, 0, 0, 0)


internal data class NutationResult(val longitude: Double, val obliquity: Double)