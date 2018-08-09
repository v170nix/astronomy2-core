package net.arwix.astronomy2.core.ephemeris.fast

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import net.arwix.astronomy2.core.*
import net.arwix.astronomy2.core.ephemeris.coordinates.getCoroutineGeocentricEclipticCoordinates
import net.arwix.astronomy2.core.ephemeris.coordinates.getGeocentricEclipticCoordinates
import net.arwix.astronomy2.core.math.*
import net.arwix.astronomy2.core.vector.SphericalVector
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin


@Geocentric @Ecliptic @Apparent
fun findMoonGeocentricEclipticApparentLongitude(t: JT): Radian {

    val solarAnomaly = getSolarAnomaly(t) //M
    val lunarElongation = getLunarElongation(t) //D
    val lunarAnomaly = getLunarAnomaly(t) //ML
    val ascendingNode = getAscendingNode(t) //F

    val E = 1.0 - 0.002516 * t - 0.0000074 * t * t
    val E2 = E * E

    val meanLunarLongitude = getMeanLunarLongitude(t) // L`
    val venus: Degree = 3958.0 / 1000000.0 * sin((119.75 + t * 131.849) * DEG_TO_RAD)
    val jupiter: Degree = 318.0 / 1000000.0 * sin((53.09 + t * 479264.29) * DEG_TO_RAD)
    val nutation: Radian = getNutation(t)

    val correctionLongitude: Degree = sinLng.foldIndexed(0.0) { index: Int, acc: Double, v: Double ->
        val w = dLng[index]
        val x = mLng[index]
        val y = mpLng[index]
        val z = fLng[index]
        acc + v * (if (abs(x) == 1.0) E else if (abs(x) == 2.0) E2 else 1.0) *
                sin((w * lunarElongation + x * solarAnomaly + y * lunarAnomaly + z * ascendingNode) * DEG_TO_RAD)
    } / 1000000.0

    val flatEarth: Degree = (1962.0 / 1000000.0 * sin((meanLunarLongitude - ascendingNode) * DEG_TO_RAD))

    return (meanLunarLongitude + correctionLongitude + venus + jupiter + flatEarth + nutation * RAD_TO_DEG) * DEG_TO_RAD
}

@Geocentric @Ecliptic @Apparent
fun getFastMoonGeocentricEclipticApparentLatitude(t: JT): Radian {

    val solarAnomaly = getSolarAnomaly(t) //M
    val lunarElongation = getLunarElongation(t) //D
    val lunarAnomaly = getLunarAnomaly(t) //ML
    val ascendingNode = getAscendingNode(t) //F
    val meanLunarLongitude = getMeanLunarLongitude(t) // L`

    val E = 1.0 - 0.002516 * t - 0.0000074 * t * t
    val E2 = E * E

    val venus: Degree = 175.0 / 1000000.0 * sin((119.75 + t * 131.849) * DEG_TO_RAD)
    val flatEarth: Degree = (1962.0 / 1000000.0 * sin((meanLunarLongitude - ascendingNode) * DEG_TO_RAD))

    val lat0: Degree = sinLat.foldIndexed(0.0) { index: Int, acc: Double, v: Double ->
        val w = dLat[index]
        val x = mLat[index]
        val y = mpLat[index]
        val z = fLat[index]
        acc + v * (if (abs(x) == 1.0) E else if (abs(x) == 2.0) E2 else 1.0) *
                sin((w * lunarElongation + x * solarAnomaly + y * lunarAnomaly + z * ascendingNode) * DEG_TO_RAD)
    } / 1000000.0

    val lat = lat0 + venus + flatEarth + 382.0 / 1000000.0 * sin((313.45 + t * 481266.484) * DEG_TO_RAD)
    return lat * DEG_TO_RAD
}


@Geocentric @Ecliptic @Apparent
fun getFastMoonGeocentricApparentDistance(t: JT): Double {

    val solarAnomaly = getSolarAnomaly(t) //M
    val lunarElongation = getLunarElongation(t) //D
    val lunarAnomaly = getLunarAnomaly(t) //ML
    val ascendingNode = getAscendingNode(t) //F

    val E = 1.0 - 0.002516 * t - 0.0000074 * t * t
    val E2 = E * E

    val correction: Double = cosLng.foldIndexed(0.0) { index: Int, acc: Double, v: Double ->
        val w = dLng[index]
        val x = mLng[index]
        val y = mpLng[index]
        val z = fLng[index]
        acc + v * (if (abs(x) == 1.0) E else if (abs(x) == 2.0) E2 else 1.0) *
                cos((w * lunarElongation + x * solarAnomaly + y * lunarAnomaly + z * ascendingNode) * DEG_TO_RAD)
    }
    return (385000560 + correction) / 1000.0 / AU
}

@Geocentric @Ecliptic
fun findSuspendedFastMoonGeocentricEclipticApparentEphemeris(): getCoroutineGeocentricEclipticCoordinates =
    { t: Double ->

        val solarAnomaly = getSolarAnomaly(t) //M
        val lunarElongation = getLunarElongation(t) //D
        val lunarAnomaly = getLunarAnomaly(t) //ML
        val ascendingNode = getAscendingNode(t) //F

        val E = 1.0 - 0.002516 * t - 0.0000074 * t * t
        val E2 = E * E

        val meanLunarLongitude = getMeanLunarLongitude(t) // L`

        val longitude = async(CommonPool) {
            val venus: Degree = 3958.0 / 1000000.0 * sin((119.75 + t * 131.849) * DEG_TO_RAD)
            val jupiter: Degree = 318.0 / 1000000.0 * sin((53.09 + t * 479264.29) * DEG_TO_RAD)
            val nutation: Radian = getNutation(t)
            val correctionLongitude: Degree = sinLng.foldIndexed(0.0) { index: Int, acc: Double, v: Double ->
                val w = dLng[index]
                val x = mLng[index]
                val y = mpLng[index]
                val z = fLng[index]
                acc + v * (if (abs(x) == 1.0) E else if (abs(x) == 2.0) E2 else 1.0) *
                        sin((w * lunarElongation + x * solarAnomaly + y * lunarAnomaly + z * ascendingNode) * DEG_TO_RAD)
            } / 1000000.0

            val flatEarth: Degree = (1962.0 / 1000000.0 * sin((meanLunarLongitude - ascendingNode) * DEG_TO_RAD))

            (meanLunarLongitude + correctionLongitude + venus + jupiter + flatEarth + nutation * RAD_TO_DEG) * DEG_TO_RAD
        }

        val latitude = async(CommonPool) {
            val venusLat: Degree = 175.0 / 1000000.0 * sin((119.75 + t * 131.849) * DEG_TO_RAD)
            val flatEarthLat: Degree = (1962.0 / 1000000.0 * sin((meanLunarLongitude - ascendingNode) * DEG_TO_RAD))
            val lat0: Degree = sinLat.foldIndexed(0.0) { index: Int, acc: Double, v: Double ->
                val w = dLat[index]
                val x = mLat[index]
                val y = mpLat[index]
                val z = fLat[index]
                acc + v * (if (Math.abs(x) == 1.0) E else if (abs(x) == 2.0) E2 else 1.0) *
                        sin((w * lunarElongation + x * solarAnomaly + y * lunarAnomaly + z * ascendingNode) * DEG_TO_RAD)
            } / 1000000.0

            val lat = lat0 + venusLat + flatEarthLat + 382.0 / 1000000.0 * sin((313.45 + t * 481266.484) * DEG_TO_RAD)
            lat * DEG_TO_RAD
        }

        val distance = async(CommonPool) {
            val correction =
                    cosLng.foldIndexed(0.0) { index: Int, acc: Double, v: Double ->
                        val w = dLng[index]
                        val x = mLng[index]
                        val y = mpLng[index]
                        val z = fLng[index]
                        acc + v * (if (abs(x) == 1.0) E else if (abs(x) == 2.0) E2 else 1.0) *
                                cos((w * lunarElongation + x * solarAnomaly + y * lunarAnomaly + z * ascendingNode) * DEG_TO_RAD)
                    }

            (385000560 + correction) / 1000.0 / AU
        }

        SphericalVector(longitude.await(), latitude.await(), distance.await())
}



private fun getSolarAnomaly(jT: JT): Degree =
        (357.5291092 + 35999.0502909 * jT - .0001536 * jT * jT + 1.0 / 24490000.0 * jT * jT * jT).normalizeDegree()

// MOON PARAMETERS (Formulae from "Calendrical Calculations") //D
private fun getLunarElongation(jT: JT): Degree =
        doubleArrayOf(297.8501921, 445267.1114034, -0.0018819, 1.0 / 545868.0, -1.0 / 113065000.0)
                .polynomialSum(jT).normalizeDegree()

// Anomalistic phase /ML
private fun getLunarAnomaly(jT: JT): Degree =
        doubleArrayOf(134.9633964, 477198.8675055, .0087414, 1.0 / 69699.0, -1.0 / 14712000.0)
                .polynomialSum(jT).normalizeDegree()

// ascending node F
private fun getAscendingNode(jT: JT): Degree =
        doubleArrayOf(93.2720950, 483202.0175233, -0.0036539, -1.0 / 3526000.0, 1.0 / 863310000.0)
                .polynomialSum(jT).normalizeDegree()

private fun getMeanLunarLongitude(jT: JT): Degree =
        doubleArrayOf(218.3164477, 481267.88123421, -0.0015786, 1.0 / 538841.0, -1.0 / 65194000.0)
                .polynomialSum(jT).normalizeDegree()


private val dLng by lazy {doubleArrayOf(
        0.0, 2.0, 2.0, 0.0, 0.0, 0.0, 2.0, 2.0, 2.0, 2.0,
        0.0, 1.0, 0.0, 2.0, 0.0, 0.0, 4.0, 0.0, 4.0, 2.0,
        2.0, 1.0, 1.0, 2.0, 2.0, 4.0, 2.0, 0.0, 2.0, 2.0,
        1.0, 2.0, 0.0, 0.0, 2.0, 2.0, 2.0, 4.0, 0.0, 3.0,
        2.0, 4.0, 0.0, 2.0, 2.0, 2.0, 4.0, 0.0, 4.0, 1.0,
        2.0, 0.0, 1.0, 3.0, 4.0, 2.0, 0.0, 1.0, 2.0, 2.0) }

private val mLng by lazy { doubleArrayOf(
        0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, -1.0, 0.0, -1.0,
        1.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0,
        1.0, 0.0, 1.0, -1.0, 0.0, 0.0, 0.0, 1.0, 0.0, -1.0,
        0.0, -2.0, 1.0, 2.0, -2.0, 0.0, 0.0, -1.0, 0.0, 0.0,
        1.0, -1.0, 2.0, 2.0, 1.0, -1.0, 0.0, 0.0, -1.0, 0.0,
        1.0, 0.0, 1.0, 0.0, 0.0, -1.0, 2.0, 1.0, 0.0, 0.0) }

private val mpLng by lazy { doubleArrayOf(
        1.0, -1.0, 0.0, 2.0, 0.0, 0.0, -2.0, -1.0, 1.0, 0.0,
        -1.0, 0.0, 1.0, 0.0, 1.0, 1.0, -1.0, 3.0, -2.0, -1.0,
        0.0, -1.0, 0.0, 1.0, 2.0, 0.0, -3.0, -2.0, -1.0, -2.0,
        1.0, 0.0, 2.0, 0.0, -1.0, 1.0, 0.0, -1.0, 2.0, -1.0,
        1.0, -2.0, -1.0, -1.0, -2.0, 0.0, 1.0, 4.0, 0.0, -2.0,
        0.0, 2.0, 1.0, -2.0, -3.0, 2.0, 1.0, -1.0, 3.0, -1.0) }

private val fLng by lazy { doubleArrayOf(
        0.0, 0.0, 0.0, 0.0, 0.0, 2.0, 0.0, 0.0, 0.0, 0.0,
        0.0, 0.0, 0.0, -2.0, 2.0, -2.0, 0.0, 0.0, 0.0, 0.0,
        0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 2.0, 0.0,
        0.0, 0.0, 0.0, 0.0, 0.0, -2.0, 2.0, 0.0, 2.0, 0.0,
        0.0, 0.0, 0.0, 0.0, 0.0, -2.0, 0.0, 0.0, 0.0, 0.0,
        -2.0, -2.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -2.0) }

private val sinLng by lazy { doubleArrayOf(
        6288774.0, 1274027.0, 658314.0, 213618.0, -185116.0, -114332.0, 58793.0, 57066.0, 53322.0, 45758.0,
        -40923.0, -34720.0, -30383.0, 15327.0, -12528.0, 10980.0, 10675.0, 10034.0, 8548.0, -7888.0,
        -6766.0, -5163.0, 4987.0, 4036.0, 3994.0, 3861.0, 3665.0, -2689.0, -2602.0, 2390.0,
        -2348.0, 2236.0, -2120.0, -2069.0, 2048.0, -1773.0, -1595.0, 1215.0, -1110.0, -892.0,
        -810.0, 759.0, -713.0, -700.0, 691.0, 596.0, 549.0, 537.0, 520.0, -487.0,
        -399.0, -381.0, 351.0, -340.0, 330.0, 327.0, -323.0, 299.0, 294.0, 0.0) }

private val dLat by lazy { doubleArrayOf(
        0.0, 0.0, 0.0, 2.0, 2.0, 2.0, 2.0, 0.0, 2.0, 0.0,
        2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 0.0, 4.0, 0.0,
        0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 4.0, 4.0,
        0.0, 4.0, 2.0, 2.0, 2.0, 2.0, 0.0, 2.0, 2.0, 2.0,
        2.0, 4.0, 2.0, 2.0, 0.0, 2.0, 1.0, 1.0, 0.0, 2.0,
        1.0, 2.0, 0.0, 4.0, 4.0, 1.0, 4.0, 1.0, 4.0, 2.0) }

private val mLat by lazy { doubleArrayOf(
        0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
        -1.0, 0.0, 0.0, 1.0, -1.0, -1.0, -1.0, 1.0, 0.0, 1.0,
        0.0, 1.0, 0.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0,
        0.0, 0.0, 0.0, 0.0, -1.0, 0.0, 0.0, 0.0, 0.0, 1.0,
        1.0, 0.0, -1.0, -2.0, 0.0, 1.0, 1.0, 1.0, 1.0, 1.0,
        0.0, -1.0, 1.0, 0.0, -1.0, 0.0, 0.0, 0.0, -1.0, -2.0) }

private val mpLat by lazy { doubleArrayOf(
        0.0, 1.0, 1.0, 0.0, -1.0, -1.0, 0.0, 2.0, 1.0, 2.0,
        0.0, -2.0, 1.0, 0.0, -1.0, 0.0, -1.0, -1.0, -1.0, 0.0,
        0.0, -1.0, 0.0, 1.0, 1.0, 0.0, 0.0, 3.0, 0.0, -1.0,
        1.0, -2.0, 0.0, 2.0, 1.0, -2.0, 3.0, 2.0, -3.0, -1.0,
        0.0, 0.0, 1.0, 0.0, 1.0, 1.0, 0.0, 0.0, -2.0, -1.0,
        1.0, -2.0, 2.0, -2.0, -1.0, 1.0, 1.0, -1.0, 0.0, 0.0) }

private val fLat by lazy { doubleArrayOf(
        1.0, 1.0, -1.0, -1.0, 1.0, -1.0, 1.0, 1.0, -1.0, -1.0,
        -1.0, -1.0, 1.0, -1.0, 1.0, 1.0, -1.0, -1.0, -1.0, 1.0,
        3.0, 1.0, 1.0, 1.0, -1.0, -1.0, -1.0, 1.0, -1.0, 1.0,
        -3.0, 1.0, -3.0, -1.0, -1.0, 1.0, -1.0, 1.0, -1.0, 1.0,
        1.0, 1.0, 1.0, -1.0, 3.0, -1.0, -1.0, 1.0, -1.0, -1.0,
        1.0, -1.0, 1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, 1.0) }

private val sinLat by lazy { doubleArrayOf(
        5128122.0, 280602.0, 277693.0, 173237.0, 55413.0, 46271.0, 32573.0, 17198.0, 9266.0, 8822.0,
        8216.0, 4324.0, 4200.0, -3359.0, 2463.0, 2211.0, 2065.0, -1870.0, 1828.0, -1794.0,
        -1749.0, -1565.0, -1491.0, -1475.0, -1410.0, -1344.0, -1335.0, 1107.0, 1021.0, 833.0,
        777.0, 671.0, 607.0, 596.0, 491.0, -451.0, 439.0, 422.0, 421.0, -366.0,
        -351.0, 331.0, 315.0, 302.0, -283.0, -229.0, 223.0, 223.0, -220.0, -220.0,
        -185.0, 181.0, -177.0, 176.0, 166.0, -164.0, 132.0, -119.0, 115.0, 107.0) }

private val cosLng by lazy { doubleArrayOf(
        -20905355.0, -3699111.0, -2955968.0, -569925.0, 48888.0, -3149.0, 246158.0, -152138.0, -170733.0, -204586.0,
        -129620.0, 108743.0, 104755.0, 10321.0, 0.0, 79661.0, -34782.0, -23210.0, -21636.0, 24208.0,
        30824.0, -8379.0, -16675.0, -12831.0, -10445.0, -11650.0, 14403.0, -7003.0, 0.0, 10056.0,
        6322.0, -9884.0, 5751.0, 0.0, -4950.0, 4130.0, 0.0, -3958.0, 0.0, 3258.0,
        2616.0, -1897.0, -2117.0, 2354.0, 0.0, 0.0, -1423.0, -1117.0, -1571.0, -1739.0,
        0.0, -4421.0, 0.0, 0.0, 0.0, 0.0, 1165.0, 0.0, 0.0, 8752.0) }