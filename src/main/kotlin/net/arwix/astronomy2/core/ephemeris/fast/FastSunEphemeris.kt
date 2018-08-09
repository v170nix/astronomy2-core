package net.arwix.astronomy2.core.ephemeris.fast

import net.arwix.astronomy2.core.*
import net.arwix.astronomy2.core.ephemeris.coordinates.getGeocentricEclipticCoordinates
import net.arwix.astronomy2.core.kepler.ID_EARTH_KEPLER_ELEMENTS
import net.arwix.astronomy2.core.kepler.getSimonJ2000KeplerElements
import net.arwix.astronomy2.core.math.normalizeDegree
import net.arwix.astronomy2.core.math.toRad
import net.arwix.astronomy2.core.vector.SphericalVector
import kotlin.coroutines.experimental.coroutineContext
import kotlin.math.cos
import kotlin.math.sin

@Geocentric @Ecliptic @Apparent
fun findFastSunGeocentricEclipticApparentEphemeris(): getGeocentricEclipticCoordinates {
    return { jT ->
        val aberration = (0.0000974 * cos((177.63 + 35999.01848 * jT) * DEG_TO_RAD) - 0.005575).normalizeDegree() * DEG_TO_RAD
        val longitude = (282.7771834
                + 36000.76952744 * jT
                + 0.000005729577951308232
                * args.fold(0.0) { acc, (x, y, z) -> acc + x * sin(y * DEG_TO_RAD + jT * z * DEG_TO_RAD) }
                ).normalizeDegree().toRad()

        val solarAnomaly = (357.5291092 + 35999.0502909 * jT - .0001536 * jT * jT + jT * jT * jT / 24490000.0)
                .normalizeDegree().toRad()


        var c = (1.9146 - .004817 * jT - .000014 * jT * jT) * Math.sin(solarAnomaly)
        c += (.019993 - .000101 * jT) * sin(2 * solarAnomaly)
        c += .00029 * sin(3.0 * solarAnomaly) // Correction to the mean ecliptic longitude
        val nutation: Radian = getNutation(jT)

        val ecc = getSimonJ2000KeplerElements(ID_EARTH_KEPLER_ELEMENTS).getEccentricity(jT)
        val v = solarAnomaly + c * DEG_TO_RAD
        val distance = 1.000001018 * (1.0 - ecc * ecc) / (1.0 + ecc * cos(v)) // In UA

        SphericalVector(longitude + nutation + aberration, 0.0, distance)
    }
}

internal fun getNutation(jT: JT): Radian {
    val a = (124.90 - 1934.134 * jT + 0.002063 * jT * jT).normalizeDegree() * DEG_TO_RAD
    val b = (201.11 + 72001.5377 * jT + 0.00057 * jT * jT).normalizeDegree().toRad()
    return (-.004778 * sin(a) - .0003667 * sin(b)) * DEG_TO_RAD
}

private val args by lazy {
    arrayOf(
            doubleArrayOf(403406.0, 270.54861, 0.9287892),
            doubleArrayOf(195207.0, 340.19128, 35999.1376958),
            doubleArrayOf(119433.0, 63.91854, 35999.4089666),
            doubleArrayOf(112392.0, 331.2622, 35998.7287385),
            doubleArrayOf(3891.0, 317.843, 71998.20261),
            doubleArrayOf(2819.0, 86.631, 71998.4403),
            doubleArrayOf(1721.0, 240.052, 36000.35726),
            doubleArrayOf(660.0, 310.26, 71997.4812),
            doubleArrayOf(350.0, 247.23, 32964.4678),
            doubleArrayOf(334.0, 260.87, -19.441),
            doubleArrayOf(314.0, 297.82, 445267.1117),
            doubleArrayOf(268.0, 343.14, 45036.884),
            doubleArrayOf(242.0, 166.79, 3.1008),
            doubleArrayOf(234.0, 81.53, 22518.4434),
            doubleArrayOf(158.0, 3.5, -19.9739),
            doubleArrayOf(132.0, 132.75, 65928.9345),
            doubleArrayOf(129.0, 182.95, 9038.0293),
            doubleArrayOf(114.0, 162.03, 3034.7684),
            doubleArrayOf(99.0, 29.8, 33718.148),
            doubleArrayOf(93.0, 266.4, 3034.448),
            doubleArrayOf(86.0, 249.2, -2280.773),
            doubleArrayOf(78.0, 157.6, 29929.992),
            doubleArrayOf(72.0, 257.8, 31556.493),
            doubleArrayOf(68.0, 185.1, 149.588),
            doubleArrayOf(64.0, 69.9, 9037.75),
            doubleArrayOf(46.0, 8.0, 107997.405),
            doubleArrayOf(38.0, 197.1, -4444.176),
            doubleArrayOf(37.0, 250.4, 151.771),
            doubleArrayOf(32.0, 65.3, 67555.316),
            doubleArrayOf(29.0, 162.7, 31556.08),
            doubleArrayOf(28.0, 341.5, -4561.54),
            doubleArrayOf(27.0, 291.6, 107996.706),
            doubleArrayOf(27.0, 98.5, 1221.655),
            doubleArrayOf(25.0, 146.7, 62894.167),
            doubleArrayOf(24.0, 110.0, 31437.369),
            doubleArrayOf(21.0, 5.2, 14578.298),
            doubleArrayOf(21.0, 342.6, -31931.757),
            doubleArrayOf(20.0, 230.9, 34777.243),
            doubleArrayOf(18.0, 256.1, 1221.999),
            doubleArrayOf(17.0, 45.3, 62894.511),
            doubleArrayOf(14.0, 242.9, -4442.039),
            doubleArrayOf(13.0, 115.2, 107997.909),
            doubleArrayOf(13.0, 151.8, 119.066),
            doubleArrayOf(13.0, 285.3, 16859.071),
            doubleArrayOf(12.0, 53.3, -4.578),
            doubleArrayOf(10.0, 126.6, 26895.292),
            doubleArrayOf(10.0, 205.7, -39.127),
            doubleArrayOf(10.0, 85.9, 12297.536),
            doubleArrayOf(10.0, 146.1, 90073.778))
}