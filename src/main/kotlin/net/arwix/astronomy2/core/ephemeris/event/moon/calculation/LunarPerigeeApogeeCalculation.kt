package net.arwix.astronomy2.core.ephemeris.event.moon.calculation

import net.arwix.astronomy2.core.DEG_TO_RAD
import net.arwix.astronomy2.core.DELTA_JD_MJD
import net.arwix.astronomy2.core.JULIAN_DAYS_PER_CENTURY
import net.arwix.astronomy2.core.SECONDS_PER_DAY
import net.arwix.astronomy2.core.calendar.dayOfYear
import net.arwix.astronomy2.core.calendar.getHours
import net.arwix.astronomy2.core.calendar.getMJD
import net.arwix.astronomy2.core.calendar.year
import net.arwix.astronomy2.core.math.normalizeDegree
import java.util.*
import kotlin.math.floor


@Suppress("LocalVariableName")
class LunarPerigeeApogeeCalculation(initialCalendar: Calendar) {

    private val initPerigeeK: Double
    private val initApogeeK: Double
    private val initJd = initialCalendar.getMJD() + DELTA_JD_MJD

    init {
        val k = (with(initialCalendar) {
            year() + (dayOfYear() + getHours() / 24.0) / getActualMaximum(Calendar.DAY_OF_YEAR)
        } - 1999.973) * 13.2555
        initPerigeeK = getPerigeeClosestK(k)
        initApogeeK = getAppogeeClosestK(k)
    }

    fun getNextPerigee() = getPerigeeImpl(EventTime.NEXT)
    fun getPreviousPerigee() = getPerigeeImpl(EventTime.PREVIOUS)
    fun getNextApogee() = getApogeeImpl(EventTime.NEXT)
    fun getPreviousApogee() = getApogeeImpl(EventTime.PREVIOUS)

    private fun getPerigeeImpl(eventTime: EventTime) = sequence {
        val d = if (eventTime == EventTime.NEXT) 1.0 else -1.0
        var k = initPerigeeK
        var jd = initJd
        while (true) {
            val result = getPerigeeImpl(k, jd, eventTime)
            jd = result.first + DELTA_JD_MJD + 15.0 * d
            k += d
            yield(result)
        }
    }

    private fun getApogeeImpl(eventTime: EventTime) = sequence {
        val d = if (eventTime == EventTime.NEXT) 1.0 else -1.0
        var k = initApogeeK
        var jd = initJd
        while (true) {
            val result = getApogeeImpl(k, jd, eventTime)
            jd = result.first + DELTA_JD_MJD + 15.0 * d
            k += d
            yield(result)
        }
    }

    /**
     * Calculates the instant of the Moon's perigee, following Meeus' Astronomical
     * Algorithms, chapter 48. Largest error compared to ELP2000 is 30 minutes.
     * Time is corrected for Moon secular acceleration.
     * @param initJd The starting Julian day of calculations (TDB).
     * @return The event. In the details field the value of the equatorial horizontal
     * parallax in radians is provided. The distance in km can be calculated as
     * d = 6378.14 / Math.sin(parallax).
     */
    private fun getPerigeeImpl(initK: Double, initJd: Double, eventType: EventTime): Pair<Double, Double> {
        var jd = initJd
        val (k, MeanJD) = getKMeanJD(round(initK, 0.0, eventType), jd, eventType)

        val T = k / 1325.55
        val Tsquared = T * T
        val Tcubed = Tsquared * T
        val T4 = Tcubed * T

        val D = (171.9179 + 335.9106046 * k - 0.0100250 * Tsquared - 0.00001156 * Tcubed + 0.000000055 * T4).normalizeDegree() * DEG_TO_RAD
        val M = (347.3477 + 27.1577721 * k - 0.0008323 * Tsquared - 0.0000010 * Tcubed).normalizeDegree() * DEG_TO_RAD
        val F = (316.6109 + 364.5287911 * k - 0.0125131 * Tsquared - 0.0000148 * Tcubed).normalizeDegree() * DEG_TO_RAD

        var nPerigeeCoefficients = moonPerigeeCoefficients.size
        var Sigma = 0.0
        for (i in 0 until nPerigeeCoefficients) {
            Sigma += (moonPerigeeCoefficients[i][3] + T * moonPerigeeCoefficients[i][4]) * Math.sin(D * moonPerigeeCoefficients[i][0] + M * moonPerigeeCoefficients[i][1] + F * moonPerigeeCoefficients[i][2])
        }
        jd = MeanJD + Sigma

        nPerigeeCoefficients = moonPerigeeParallaxCoefficients.size
        var parallax = 3629.215
        for (i in 0 until nPerigeeCoefficients) {
            parallax += (moonPerigeeParallaxCoefficients[i][3] + T * moonPerigeeParallaxCoefficients[i][4]) * Math.cos(D * moonPerigeeParallaxCoefficients[i][0] + M * moonPerigeeParallaxCoefficients[i][1] + F * moonPerigeeParallaxCoefficients[i][2])
        }
        val p = parallax / 3600.0 * DEG_TO_RAD

        // Correct Meeus results for secular acceleration
        val deltaT = dynamicalTimeCorrectionForMoonSecularAcceleration(jd) - jd
        jd -= deltaT
        return Pair(jd - DELTA_JD_MJD, p)
    }

    /**
     * Calculates the instant of the next Moon's apogee, following Meeus' Astronomical
     * Algorithms, chapter 48. Largest error compared to ELP2000 is only 3 minutes.
     * Time is corrected for Moon secular acceleration.
     * @param initJd The starting Julian day of calculations (TDB).
     * @return The event. In the details field the value of the equatorial horizontal
     * parallax in radians is provided. The distance in km can be calculated as
     * d = 6378.14 / Math.sin(parallax).
     */
    private fun getApogeeImpl(initK: Double, initJd: Double, eventType: EventTime): Pair<Double, Double> {

        var jd = initJd
        val (k, MeanJD) = getKMeanJD(round(initK, 0.5, eventType), jd, eventType)

        val T = k / 1325.55
        val Tsquared = T * T
        val Tcubed = Tsquared * T
        val T4 = Tcubed * T

        val D = (171.9179 + 335.9106046 * k - 0.0100250 * Tsquared - 0.00001156 * Tcubed + 0.000000055 * T4).normalizeDegree() * DEG_TO_RAD
        val M = (347.3477 + 27.1577721 * k - 0.0008323 * Tsquared - 0.0000010 * Tcubed).normalizeDegree() * DEG_TO_RAD
        val F = (316.6109 + 364.5287911 * k - 0.0125131 * Tsquared - 0.0000148 * Tcubed).normalizeDegree() * DEG_TO_RAD

        var nApogeeCoefficients = moonApogeeCoefficients.size
        var Sigma = 0.0
        for (i in 0 until nApogeeCoefficients) {
            Sigma += (moonApogeeCoefficients[i][3] + T * moonApogeeCoefficients[i][4]) * Math.sin(D * moonApogeeCoefficients[i][0] + M * moonApogeeCoefficients[i][1] + F * moonApogeeCoefficients[i][2])
        }
        jd = MeanJD + Sigma

        nApogeeCoefficients = moonApogeeParalllaxCoefficients.size
        var parallax = 3245.251
        for (i in 0 until nApogeeCoefficients) {
            parallax += (moonApogeeParalllaxCoefficients[i][3] + T * moonApogeeParalllaxCoefficients[i][4]) * Math.cos(D * moonApogeeParalllaxCoefficients[i][0] + M * moonApogeeParalllaxCoefficients[i][1] + F * moonApogeeParalllaxCoefficients[i][2])
        }
        val p = parallax / 3600.0 * DEG_TO_RAD

        // Correct Meeus results for secular acceleration
        val deltaT = dynamicalTimeCorrectionForMoonSecularAcceleration(jd) - jd
        jd -= deltaT

        return Pair(jd - DELTA_JD_MJD, p)
    }

    private fun getKMeanJD(inK: Double, jd: Double, eventType: EventTime): Pair<Double, Double> {
        var k = inK
        var MeanJD = meanPerigeeApogee(k)
        if (MeanJD > jd && eventType == EventTime.PREVIOUS) {
            k--
            MeanJD = meanPerigeeApogee(k)
        }
        if (MeanJD < jd && eventType == EventTime.NEXT) {
            k++
            MeanJD = meanPerigeeApogee(k)
        }
        if (MeanJD > jd && eventType != EventTime.PREVIOUS) {
            val km = k - 1
            val MeanJDm = meanPerigeeApogee(km)
            if (MeanJDm > jd && eventType == EventTime.NEXT || Math.abs(jd - MeanJDm) < Math.abs(jd - MeanJD) && eventType == EventTime.CLOSEST) {
                k = km
                MeanJD = MeanJDm
            }
        }
        if (MeanJD < jd && eventType !== EventTime.NEXT) {
            val kp = k + 1
            val MeanJDp = meanPerigeeApogee(kp)
            if (MeanJDp < jd && eventType == EventTime.PREVIOUS || Math.abs(jd - MeanJDp) < Math.abs(jd - MeanJD) && eventType == EventTime.CLOSEST) {
                k = kp
                MeanJD = MeanJDp
            }
        }
        return k to MeanJD
    }

    private fun meanPerigeeApogee(k: Double): Double {
        val t = k / 1325.55
        val t2 = t * t
        val t3 = t2 * t
        val t4 = t3 * t
        return 2451534.6698 + 27.55454988 * k - 0.0006886 * t2 - 0.000001098 * t3 + 0.0000000052 * t4
    }

    private fun round(kapprox: Double, delta: Double, eventType: EventTime): Double {
        var k = delta + floor(kapprox)
        if (eventType === EventTime.NEXT && k < kapprox) k++
        if (eventType === EventTime.PREVIOUS && k > kapprox) k--
        if (eventType === EventTime.CLOSEST && k < kapprox - 0.5 && delta == 0.0) k++
        return k
    }

    private fun getPerigeeClosestK(k: Double) = round(k, 0.0, EventTime.CLOSEST)
    private fun getAppogeeClosestK(k: Double) = round(k, 0.5, EventTime.CLOSEST)

    companion object {
        /**
         * Holds the value of the secular acceleration of the Moon. Currently equal
         * to -25.858 arcsec/cent^2 (Chapront, Chapront-Touze and Francou, 2002).
         */
        private const val MOON_SECULAR_ACCELERATION = -25.858

        /**
         * Corrects Julian day of calculations in dynamical time to consider a different value for
         * the Moon secular acceleration. The value used by Stephenson and Morrison is
         * -26"/centuri^2, and the one considered as correct is slightly different (see
         * [MOON_SECULAR_ACCELERATION]). This correction is used
         * in JPARSEC although its magnitude is below the uncertainty in TT-UT, you will only
         * notice some difference when studying eclipses in ancient times. Correction
         * for different years are as follows:<BR></BR>
         * <pre>
         * Year       Correction (seconds)
         * -2000      -202
         * -1000      -113
         * 0      -49
         * 1000      -12
         * 1955       0.000
         * 2000      -0.026
         * 3000      -14
        </pre> *
         *
         * @param jd Julian day in TDB/TT.
         * @return Output (corrected) Julian day in the same time scale.
         */
        fun dynamicalTimeCorrectionForMoonSecularAcceleration(jd: Double): Double {
            val cent = (jd - 2435109.0) / JULIAN_DAYS_PER_CENTURY
            val deltaT = -0.91072 * (MOON_SECULAR_ACCELERATION + 26.0) * cent * cent
            return jd + deltaT / SECONDS_PER_DAY
        }

        // See Astronomical Algorithms by J. Meeus.
        private val moonPerigeeCoefficients by lazy {
            arrayOf(
                    doubleArrayOf(2.0, 0.0, 0.0, -1.6769, 0.0),
                    doubleArrayOf(4.0, 0.0, 0.0, 0.4589, 0.0),
                    doubleArrayOf(6.0, 0.0, 0.0, -0.1856, 0.0),
                    doubleArrayOf(8.0, 0.0, 0.0, 0.0883, 0.0),
                    doubleArrayOf(2.0, -1.0, 0.0, -0.0773, 0.00019),
                    doubleArrayOf(0.0, 1.0, 0.0, 0.0502, -0.00013),
                    doubleArrayOf(10.0, 0.0, 0.0, -0.0460, 0.0),
                    doubleArrayOf(4.0, -1.0, 0.0, 0.0422, -0.00011),
                    doubleArrayOf(6.0, -1.0, 0.0, -0.0256, 0.0),
                    doubleArrayOf(12.0, 0.0, 0.0, 0.0253, 0.0),
                    doubleArrayOf(1.0, 0.0, 0.0, 0.0237, 0.0),
                    doubleArrayOf(8.0, -1.0, 0.0, 0.0162, 0.0),
                    doubleArrayOf(14.0, 0.0, 0.0, -0.0145, 0.0),
                    doubleArrayOf(0.0, 0.0, 2.0, 0.0129, 0.0),
                    doubleArrayOf(3.0, 0.0, 0.0, -0.0112, 0.0),
                    doubleArrayOf(10.0, -1.0, 0.0, -0.0104, 0.0),
                    doubleArrayOf(16.0, 0.0, 0.0, 0.0086, 0.0),
                    doubleArrayOf(12.0, -1.0, 0.0, 0.0069, 0.0),
                    doubleArrayOf(5.0, 0.0, 0.0, 0.0066, 0.0),
                    doubleArrayOf(2.0, 0.0, 2.0, -0.0053, 0.0),
                    doubleArrayOf(18.0, 0.0, 0.0, -0.0052, 0.0),
                    doubleArrayOf(14.0, -1.0, 0.0, -0.0046, 0.0),
                    doubleArrayOf(7.0, 0.0, 0.0, -0.0041, 0.0),
                    doubleArrayOf(2.0, 1.0, 0.0, 0.0040, 0.0),
                    doubleArrayOf(20.0, 0.0, 0.0, 0.0032, 0.0),
                    doubleArrayOf(1.0, 1.0, 0.0, -0.0032, 0.0),
                    doubleArrayOf(16.0, -1.0, 0.0, 0.0031, 0.0),
                    doubleArrayOf(4.0, 1.0, 0.0, -0.0029, 0.0),
                    doubleArrayOf(9.0, 0.0, 0.0, 0.0027, 0.0),
                    doubleArrayOf(4.0, 0.0, 2.0, 0.0027, 0.0),
                    doubleArrayOf(2.0, -2.0, 0.0, -0.0027, 0.0),
                    doubleArrayOf(4.0, -2.0, 0.0, 0.0024, 0.0),
                    doubleArrayOf(6.0, -2.0, 0.0, -0.0021, 0.0),
                    doubleArrayOf(22.0, 0.0, 0.0, -0.0021, 0.0),
                    doubleArrayOf(18.0, -1.0, 0.0, -0.0021, 0.0),
                    doubleArrayOf(6.0, 1.0, 0.0, 0.0019, 0.0),
                    doubleArrayOf(11.0, 0.0, 0.0, -0.0018, 0.0),
                    doubleArrayOf(8.0, 1.0, 0.0, -0.0014, 0.0),
                    doubleArrayOf(4.0, 0.0, -2.0, -0.0014, 0.0),
                    doubleArrayOf(6.0, 0.0, 2.0, -0.0014, 0.0),
                    doubleArrayOf(3.0, 1.0, 0.0, 0.0014, 0.0),
                    doubleArrayOf(5.0, 1.0, 0.0, -0.0014, 0.0),
                    doubleArrayOf(13.0, 0.0, 0.0, 0.0013, 0.0),
                    doubleArrayOf(20.0, -1.0, 0.0, 0.0013, 0.0),
                    doubleArrayOf(3.0, 2.0, 0.0, 0.0011, 0.0),
                    doubleArrayOf(4.0, -2.0, 2.0, -0.0011, 0.0),
                    doubleArrayOf(1.0, 2.0, 0.0, -0.0010, 0.0),
                    doubleArrayOf(22.0, -1.0, 0.0, -0.0009, 0.0),
                    doubleArrayOf(0.0, 0.0, 4.0, -0.0008, 0.0),
                    doubleArrayOf(6.0, 0.0, -2.0, 0.0008, 0.0),
                    doubleArrayOf(2.0, 1.0, -2.0, 0.0008, 0.0),
                    doubleArrayOf(0.0, 2.0, 0.0, 0.0007, 0.0),
                    doubleArrayOf(0.0, -1.0, 2.0, 0.0007, 0.0),
                    doubleArrayOf(2.0, 0.0, 4.0, 0.0007, 0.0),
                    doubleArrayOf(0.0, -2.0, 2.0, -0.0006, 0.0),
                    doubleArrayOf(2.0, 2.0, -2.0, -0.0006, 0.0),
                    doubleArrayOf(24.0, 0.0, 0.0, 0.0006, 0.0),
                    doubleArrayOf(4.0, 0.0, -4.0, 0.0005, 0.0),
                    doubleArrayOf(2.0, 2.0, 0.0, 0.0005, 0.0),
                    doubleArrayOf(1.0, -1.0, 0.0, -0.0004, 0.0))
        }

        private val moonPerigeeParallaxCoefficients by lazy {
            arrayOf(
                    doubleArrayOf(2.0, 0.0, 0.0, 63.224, 0.0),
                    doubleArrayOf(4.0, 0.0, 0.0, -6.990, 0.0),
                    doubleArrayOf(2.0, -1.0, 0.0, 2.834, -0.0071),
                    doubleArrayOf(6.0, 0.0, 0.0, 1.927, 0.0),
                    doubleArrayOf(1.0, 0.0, 0.0, -1.263, 0.0),
                    doubleArrayOf(8.0, 0.0, 0.0, -0.702, 0.0),
                    doubleArrayOf(0.0, 1.0, 0.0, 0.696, -0.0017),
                    doubleArrayOf(0.0, 0.0, 2.0, -0.690, 0.0),
                    doubleArrayOf(4.0, -1.0, 0.0, 0.629, 0.0016),
                    doubleArrayOf(2.0, 0.0, -2.0, -0.392, 0.0),
                    doubleArrayOf(10.0, 0.0, 0.0, 0.297, 0.0),
                    doubleArrayOf(6.0, -1.0, 0.0, 0.260, 0.0),
                    doubleArrayOf(3.0, 0.0, 0.0, 0.201, 0.0),
                    doubleArrayOf(2.0, 1.0, 0.0, -0.161, 0.0),
                    doubleArrayOf(1.0, 1.0, 0.0, 0.157, 0.0),
                    doubleArrayOf(12.0, 0.0, 0.0, -0.138, 0.0),
                    doubleArrayOf(8.0, -1.0, 0.0, -0.127, 0.0),
                    doubleArrayOf(2.0, 0.0, 2.0, 0.104, 0.0),
                    doubleArrayOf(2.0, -2.0, 0.0, 0.104, 0.0),
                    doubleArrayOf(5.0, 0.0, 0.0, -0.079, 0.0),
                    doubleArrayOf(14.0, 0.0, 0.0, 0.068, 0.0),
                    doubleArrayOf(10.0, -1.0, 0.0, 0.067, 0.0),
                    doubleArrayOf(4.0, 1.0, 0.0, 0.054, 0.0),
                    doubleArrayOf(12.0, -1.0, 0.0, -0.038, 0.0),
                    doubleArrayOf(4.0, -2.0, 0.0, -0.038, 0.0),
                    doubleArrayOf(7.0, 0.0, 0.0, 0.037, 0.0),
                    doubleArrayOf(4.0, 0.0, 2.0, -0.037, 0.0),
                    doubleArrayOf(16.0, 0.0, 0.0, -0.035, 0.0),
                    doubleArrayOf(3.0, 1.0, 0.0, -0.030, 0.0),
                    doubleArrayOf(1.0, -1.0, 0.0, 0.029, 0.0),
                    doubleArrayOf(6.0, 1.0, 0.0, -0.025, 0.0),
                    doubleArrayOf(0.0, 2.0, 0.0, 0.023, 0.0),
                    doubleArrayOf(14.0, -1.0, 0.0, 0.023, 0.0),
                    doubleArrayOf(2.0, 2.0, 0.0, -0.023, 0.0),
                    doubleArrayOf(6.0, -2.0, 0.0, 0.022, 0.0),
                    doubleArrayOf(2.0, -1.0, -2.0, -0.021, 0.0),
                    doubleArrayOf(9.0, 0.0, 0.0, -0.020, 0.0),
                    doubleArrayOf(18.0, 0.0, 0.0, 0.019, 0.0),
                    doubleArrayOf(6.0, 0.0, 2.0, 0.017, 0.0),
                    doubleArrayOf(0.0, -1.0, 2.0, 0.014, 0.0),
                    doubleArrayOf(16.0, -1.0, 0.0, -0.014, 0.0),
                    doubleArrayOf(4.0, 0.0, -2.0, 0.013, 0.0),
                    doubleArrayOf(8.0, 1.0, 0.0, 0.012, 0.0),
                    doubleArrayOf(11.0, 0.0, 0.0, 0.011, 0.0),
                    doubleArrayOf(5.0, 1.0, 0.0, 0.010, 0.0),
                    doubleArrayOf(20.0, 0.0, 0.0, -0.010, 0.0))
        }

        private val moonApogeeCoefficients by lazy {
            arrayOf(
                    doubleArrayOf(2.0, 0.0, 0.0, 0.4392, 0.0),
                    doubleArrayOf(4.0, 0.0, 0.0, 0.0684, 0.0),
                    doubleArrayOf(0.0, 1.0, 0.0, 0.0456, -0.00011),
                    doubleArrayOf(2.0, -1.0, 0.0, 0.0426, -0.00011),
                    doubleArrayOf(0.0, 0.0, 2.0, 0.0212, 0.0),
                    doubleArrayOf(1.0, 0.0, 0.0, -0.0189, 0.0),
                    doubleArrayOf(6.0, 0.0, 0.0, 0.0144, 0.0),
                    doubleArrayOf(4.0, -1.0, 0.0, 0.0113, 0.0),
                    doubleArrayOf(2.0, 0.0, 2.0, 0.0047, 0.0),
                    doubleArrayOf(1.0, 1.0, 0.0, 0.0036, 0.0),
                    doubleArrayOf(8.0, 0.0, 0.0, 0.0035, 0.0),
                    doubleArrayOf(6.0, -1.0, 0.0, 0.0034, 0.0),
                    doubleArrayOf(2.0, 0.0, -2.0, -0.0034, 0.0),
                    doubleArrayOf(2.0, -2.0, 0.0, 0.0022, 0.0),
                    doubleArrayOf(3.0, 0.0, 0.0, -0.0017, 0.0),
                    doubleArrayOf(4.0, 0.0, 2.0, 0.0013, 0.0),
                    doubleArrayOf(8.0, -1.0, 0.0, 0.0011, 0.0),
                    doubleArrayOf(4.0, -2.0, 0.0, 0.0010, 0.0),
                    doubleArrayOf(10.0, 0.0, 0.0, 0.0009, 0.0),
                    doubleArrayOf(3.0, 1.0, 0.0, 0.0007, 0.0),
                    doubleArrayOf(0.0, 2.0, 0.0, 0.0006, 0.0),
                    doubleArrayOf(2.0, 1.0, 0.0, 0.0005, 0.0),
                    doubleArrayOf(2.0, 2.0, 0.0, 0.0005, 0.0),
                    doubleArrayOf(6.0, 0.0, 2.0, 0.0004, 0.0),
                    doubleArrayOf(6.0, -2.0, 0.0, 0.0004, 0.0),
                    doubleArrayOf(10.0, -1.0, 0.0, 0.0004, 0.0),
                    doubleArrayOf(5.0, 0.0, 0.0, -0.0004, 0.0),
                    doubleArrayOf(4.0, 0.0, -2.0, -0.0004, 0.0),
                    doubleArrayOf(0.0, 1.0, 2.0, 0.0003, 0.0),
                    doubleArrayOf(12.0, 0.0, 0.0, 0.0003, 0.0),
                    doubleArrayOf(2.0, -1.0, 2.0, 0.0003, 0.0),
                    doubleArrayOf(1.0, -1.0, 0.0, -0.0003, 0.0))
        }

        private val moonApogeeParalllaxCoefficients by lazy {
            arrayOf(
                    doubleArrayOf(2.0, 0.0, 0.0, -9.147, 0.0),
                    doubleArrayOf(1.0, 0.0, 0.0, -0.841, 0.0),
                    doubleArrayOf(0.0, 0.0, 2.0, 0.697, 0.0),
                    doubleArrayOf(0.0, 1.0, 0.0, -0.656, 0.0016),
                    doubleArrayOf(4.0, 0.0, 0.0, 0.355, 0.0),
                    doubleArrayOf(2.0, -1.0, 0.0, 0.159, 0.0),
                    doubleArrayOf(1.0, 1.0, 0.0, 0.127, 0.0),
                    doubleArrayOf(4.0, -1.0, 0.0, 0.065, 0.0),
                    doubleArrayOf(6.0, 0.0, 0.0, 0.052, 0.0),
                    doubleArrayOf(2.0, 1.0, 0.0, 0.043, 0.0),
                    doubleArrayOf(2.0, 0.0, 2.0, 0.031, 0.0),
                    doubleArrayOf(2.0, 0.0, -2.0, -0.023, 0.0),
                    doubleArrayOf(2.0, -2.0, 0.0, 0.022, 0.0),
                    doubleArrayOf(2.0, 2.0, 0.0, 0.019, 0.0),
                    doubleArrayOf(0.0, 2.0, 0.0, -0.016, 0.0),
                    doubleArrayOf(6.0, -1.0, 0.0, 0.014, 0.0),
                    doubleArrayOf(8.0, 0.0, 0.0, 0.010, 0.0))
        }
    }

    private enum class EventTime {
        NEXT,
        PREVIOUS,
        CLOSEST
    }
}



