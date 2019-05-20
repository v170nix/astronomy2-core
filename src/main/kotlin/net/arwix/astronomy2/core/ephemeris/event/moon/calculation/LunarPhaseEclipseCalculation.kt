package net.arwix.astronomy2.core.ephemeris.event.moon.calculation

import kotlinx.coroutines.*
import net.arwix.astronomy2.core.DELTA_JD_MJD
import net.arwix.astronomy2.core.JULIAN_DAYS_PER_CENTURY
import net.arwix.astronomy2.core.SECONDS_PER_DAY
import net.arwix.astronomy2.core.calendar.dayOfYear
import net.arwix.astronomy2.core.calendar.getHours
import net.arwix.astronomy2.core.calendar.year
import net.arwix.astronomy2.core.ephemeris.event.moon.EventMoonEclipse
import net.arwix.astronomy2.core.ephemeris.event.moon.MoonEvent
import net.arwix.astronomy2.core.ephemeris.event.moon.MoonPhase
import net.arwix.astronomy2.core.ephemeris.event.sun.EventSolarEclipse
import net.arwix.astronomy2.core.math.polynomialSum
import net.arwix.astronomy2.core.math.toRad
import java.util.*
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


/**
 * Calculates the instant of a given Moon phase or eclipse, following Meeus's Astronomical
 * Algorithms. Error is always below 2 minutes, and usually below 1 minute.
 */
@Suppress("LocalVariableName", "MemberVisibilityCanBePrivate")
class LunarPhaseEclipseCalculation(initialCalendar: Calendar, private val dispatcher: CoroutineDispatcher = Dispatchers.Default) {

    private val initK = getClosestK(12.3685 * with(initialCalendar) {
        year() - 2000.0 + (dayOfYear() + getHours() / 24.0) / getActualMaximum(Calendar.DAY_OF_YEAR)
    })

    suspend fun getNext(count: Int, addEclipses: Boolean = false) = getEventsImpl(count, addEclipses, true)
    suspend fun getNext(endTimeCalendar: Calendar, addEclipses: Boolean = false) = getEventsImpl(endTimeCalendar, addEclipses)
    suspend fun getPrevious(count: Int, addEclipses: Boolean = false) = getEventsImpl(count, addEclipses, false)
    suspend fun getPrevious(endTimeCalendar: Calendar, addEclipses: Boolean = false) = getEventsImpl(endTimeCalendar, addEclipses)

    private suspend fun getEventsImpl(count: Int, addEclipses: Boolean, isNext: Boolean): List<MoonEvent> = coroutineScope {
        val d = if (isNext) 1.0 else -1.0
        ArrayList<MoonEvent>().apply {
            (0..count).map {
                yield()
                async(dispatcher) { getTruePhaseJd(initK + d * 0.25 * it, addEclipses) }
            }.awaitAll().let(this::addAll)
        }
    }

    private suspend fun getEventsImpl(endTimeCalendar: Calendar, addEclipses: Boolean = false): List<MoonEvent> {
        val endK = getClosestK(12.3685 * with(endTimeCalendar) {
            year() - 2000.0 + (dayOfYear() + getHours() / 24.0) / getActualMaximum(Calendar.DAY_OF_YEAR)
        })
        val count = (Math.abs(endK - initK) * 4).toInt()
        return getEventsImpl(count, addEclipses, endK > initK)
    }

    fun getMeanPhaseJd(k: Double): Double {
        val t = k / 1236.85
        return doubleArrayOf(2451550.09765 + 29.530588853 * k,
                0.0, 0.0001337, -0.000000150, 0.00000000073).polynomialSum(t)
    }

    fun getTruePhaseJd(k: Double, addEclipses: Boolean = true): MoonEvent {
        val jd = getMeanPhaseJd(k)
        val t = k / 1236.85

        val M = doubleArrayOf(2.5534 + 29.10535669 * k, 0.0, -0.0000218, -0.00000011).polynomialSum(t).toRad()
        val Mp = doubleArrayOf(201.5643 + 385.81693528 * k, 0.0, 0.0107438, 0.00001239, -0.000000058).polynomialSum(t).toRad()
        val F = doubleArrayOf(160.7108 + 390.67050274 * k, 0.0, -0.0016341, -0.00000227, 0.000000011).polynomialSum(t).toRad()
        val O = doubleArrayOf(124.7746 - 1.5637558 * k, 0.0, 0.002069, 0.00000215).polynomialSum(t).toRad()

        val E = doubleArrayOf(1.0, -0.002516, -0.0000074).polynomialSum(t)

        val W = 0.00306 -
                0.00038 * E * cos(M) +
                0.00026 * cos(Mp) -
                0.00002 * cos(Mp - M) +
                0.00002 * cos(Mp + M) +
                0.00002 * cos(2.0 * F)

        val F1 by lazy { F - 0.02665 * sin(O).toRad() }
        val A1 = (299.77 + 0.107408 * k - 0.009173 * t * t).toRad()
        val A2 = (251.88 + 0.016321 * k).toRad()
        val A3 = (251.83 + 26.651886 * k).toRad()
        val A4 = (349.42 + 36.412478 * k).toRad()
        val A5 = (84.66 + 18.206239 * k).toRad()
        val A6 = (141.74 + 53.303771 * k).toRad()
        val A7 = (207.14 + 2.453732 * k).toRad()
        val A8 = (154.84 + 7.306860 * k).toRad()
        val A9 = (34.52 + 27.261239 * k).toRad()
        val A10 = (207.19 + 0.121824 * k).toRad()
        val A11 = (291.34 + 1.844379 * k).toRad()
        val A12 = (161.72 + 24.198154 * k).toRad()
        val A13 = (239.56 + 25.513099 * k).toRad()
        val A14 = (331.55 + 3.592518 * k).toRad()

        val p by lazy {
            -0.0392 * sin(Mp) + 0.2070 * E * sin(M) +
                    0.0024 * E * sin(2.0 * M) + 0.0116 * sin(2.0 * Mp) -
                    0.0073 * E * sin(Mp + M) + 0.0067 * E * sin(Mp - M) +
                    0.0118 * sin(2.0 * F1)
        }
        val q by lazy {
            5.2207 - 0.3299 * cos(Mp) - 0.0048 * E * cos(M) +
                    0.002 * E * cos(2.0 * M) - 0.006 * E * cos(Mp + M) +
                    0.0041 * E * cos(Mp - M)
        }
        val ww by lazy { abs(cos(F1)) }
        val gamma by lazy { (p * cos(F1) + q * sin(F1)) * (1.0 - 0.0048 * ww) }
        val u by lazy {
            0.0059 + 0.0046 * E * cos(M) - 0.0182 * cos(Mp) +
                    0.0004 * cos(2.0 * Mp) - 0.0005 * cos(M + Mp)
        }
        val absGamma by lazy { abs(gamma) }


        val phase = MoonPhase.getPhase(k)
        var deltaJd = when (phase) {

            MoonPhase.NEW -> {
                -0.4072 * sin(Mp) + 0.17241 * E * sin(M) +
                        0.01608 * sin(2.0 * Mp) + 0.01039 * sin(2.0 * F) +
                        0.00739 * E * sin(Mp - M) - 0.00514 * E * sin(Mp + M) +
                        0.00208 * E * E * sin(2.0 * M) - 0.00111 * sin(Mp - 2.0 * F) -
                        0.00057 * sin(Mp + 2.0 * F) + 0.00056 * E * sin(2.0 * Mp + M) -
                        0.00042 * sin(3.0 * Mp) + 0.00042 * E * sin(M + 2.0 * F) +
                        0.00038 * E * sin(M - 2.0 * F) - 0.00024 * E * sin(2.0 * Mp - M) -
                        0.00007 * sin(Mp + 2.0 * M) - 0.00017 * sin(O) +
                        0.00004 * (sin(2.0 * Mp - 2.0 * F) + sin(3.0 * M)) +
                        0.00003 * (sin(Mp + M - 2.0 * F) + sin(2.0 * Mp + 2.0 * F) - sin(Mp + M + 2.0 * F) + sin(Mp - M + 2.0 * F)) +
                        0.00002 * (-sin(Mp - M - 2.0 * F) - sin(3.0 * Mp + M) + sin(4.0 * Mp))
            }

            MoonPhase.FULL -> {
                -0.40614 * sin(Mp) + 0.17302 * E * sin(M) +
                        0.01614 * sin(2.0 * Mp) + 0.01043 * sin(2.0 * F) +
                        0.00734 * E * sin(Mp - M) - 0.00515 * E * sin(Mp + M) +
                        0.00209 * E * E * sin(2.0 * M) - 0.00111 * sin(Mp - 2.0 * F) -
                        0.00057 * sin(Mp + 2.0 * F) + 0.00056 * E * sin(2.0 * Mp + M) -
                        0.00042 * sin(3.0 * Mp) + 0.00042 * E * sin(M + 2.0 * F) +
                        0.00038 * E * sin(M - 2.0 * F) - 0.00024 * E * sin(2.0 * Mp - M) -
                        0.00007 * E * sin(Mp + 2.0 * M) - 0.00017 * sin(O) +
                        0.00004 * (sin(2.0 * Mp - 2.0 * F) + sin(3.0 * M)) +
                        0.00003 * (sin(Mp + M - 2.0 * F) + sin(2.0 * Mp + 2.0 * F) - sin(Mp + M + 2.0 * F) + sin(Mp - M + 2.0 * F)) +
                        0.00002 * (-sin(Mp - M - 2.0 * F) - sin(3.0 * Mp + M) + sin(4.0 * Mp))
            }

            MoonPhase.FIRST_QUARTER, MoonPhase.LAST_QUARTER -> {
                (if (phase == MoonPhase.FIRST_QUARTER) 1.0 else -1.0) * W -
                        0.62801 * sin(Mp) + 0.17172 * E * sin(M) +
                        0.00862 * sin(2.0 * Mp) + 0.00804 * sin(2.0 * F) +
                        0.00454 * E * sin(Mp - M) - 0.01183 * E * sin(Mp + M) +
                        0.00204 * E * E * sin(2.0 * M) - 0.00180 * sin(Mp - 2.0 * F) -
                        0.0007 * sin(Mp + 2.0 * F) - 0.00040 * sin(3.0 * Mp) - 0.00034 * E * sin(2.0 * Mp - M) + 0.00032 * E * sin(M + 2.0 * F) + 0.00032 * E * sin(M - 2.0 * F) -
                        0.00028 * E * E * sin(Mp + 2.0 * M) + 0.00027 * E * sin(2.0 * Mp + M) - 0.00017 * sin(O) - 0.00005 * sin(Mp - M - 2.0 * F) +
                        0.00004 * sin(2.0 * Mp + 2.0 * F) - 0.00004 * sin(Mp + M + 2.0 * F) + 0.00004 * sin(Mp - 2 * M) + 0.00003 * sin(Mp + M - 2.0 * F) +
                        0.00003 * sin(3.0 * M) + 0.00002 * sin(2.0 * Mp - 2.0 * F) + 0.00002 * sin(Mp - M + 2.0 * F) - 0.00002 * sin(3.0 * Mp + M)
            }

        }

        val eclipse = if (addEclipses) {

            val sunEclipse = if (phase == MoonPhase.NEW && absGamma < (1.5433 + u)) {

                val timeOfMaximumEclipse = jd - DELTA_JD_MJD - 0.4075 * sin(Mp) + 0.1721 * E * sin(M) +
                        0.0161 * sin(2.0 * Mp) - 0.0097 * sin(2.0 * F1) +
                        0.0073 * E * sin(Mp - M) - 0.0050 * E * sin(Mp + M) +
                        0.0021 * E * sin(2.0 * M) - 0.0023 * sin(Mp - 2.0 * F1) +
                        0.0012 * sin(Mp + 2.0 * F1) + 0.0006 * E * sin(2.0 * Mp + M) -
                        0.0004 * sin(3.0 * Mp) - 0.0003 * E * sin(M + 2.0 * F1) +
                        0.0003 * sin(A1) - 0.0002 * E * sin(M - 2.0 * F1) -
                        0.0002 * E * sin(2.0 * Mp - M) - 0.0002 * sin(O)

                if (absGamma < 0.9972 || absGamma > 0.9972 && absGamma < 0.9972 + Math.abs(u)) {
                    val isCentral = absGamma < 0.9972

                    if (u < 0) EventSolarEclipse.Total(timeOfMaximumEclipse, isCentral) else
                        if (u > 0.0047) EventSolarEclipse.Annular(timeOfMaximumEclipse, isCentral) else {
                            val www = 0.00464 * Math.sqrt(1.0 - gamma * gamma)
                            if (u < www) {
                                EventSolarEclipse.Hybrid(timeOfMaximumEclipse, isCentral)
                            } else {
                                EventSolarEclipse.Annular(timeOfMaximumEclipse, isCentral)
                            }
                        }
                } else {
                    val mag = (1.5433 + u - absGamma) / (0.5461 + 2.0 * u)
                    EventSolarEclipse.Partial(timeOfMaximumEclipse, mag)
                }
            } else null

            (if (phase == MoonPhase.FULL) {
                val magnitudePenumbral = (1.5573 + u - absGamma) / 0.545
                val magnitudeUmbral = (1.0128 - u - absGamma) / 0.545
                if (magnitudePenumbral > 0.0 || magnitudeUmbral > 0.0) {

                    val radiusPenumbral = 1.2848 + u
                    val radiusUmbral = 0.7403 - u

                    val timeOfMaximumEclipse = jd - DELTA_JD_MJD - 0.4065 * sin(Mp) + 0.1727 * E * sin(M) +
                            0.0161 * sin(2.0 * Mp) - 0.0097 * sin(2.0 * F1) +
                            0.0073 * E * sin(Mp - M) - 0.0050 * E * sin(Mp + M) +
                            0.0021 * E * sin(2.0 * M) - 0.0023 * sin(Mp - 2.0 * F1) +
                            0.0012 * sin(Mp + 2.0 * F1) + 0.0006 * E * sin(2.0 * Mp + M) -
                            0.0004 * sin(3.0 * Mp) - 0.0003 * E * sin(M + 2.0 * F1) +
                            0.0003 * sin(A1) - 0.0002 * E * sin(M - 2.0 * F1) -
                            0.0002 * E * sin(2.0 * Mp - M) - 0.0002 * sin(O)

                    val pp = 1.0128 - u
                    val tt = 0.4678 - u
                    val n = 0.5458 + 0.04 * cos(Mp)
                    val gamma2 = gamma * gamma

                    fun getTime(it: Double) = if (it > gamma2) 60.0 * sqrt(it - gamma2) / n else null

                    // in min
                    val partialPhaseSemiDuration = (pp * pp).let(::getTime)
                    val totalPhaseSemiDuration = (tt * tt).let(::getTime)
                    val h = 1.5573 + u
                    val partialPhasePenumbraSemiDuration = (h * h).let(::getTime)

                    when {
                        magnitudeUmbral < 0.0 -> EventMoonEclipse.Penumbral(timeOfMaximumEclipse,
                                magnitudePenumbral,
                                radiusPenumbral,
                                partialPhasePenumbraSemiDuration!!)
                        magnitudeUmbral < 1.0 -> EventMoonEclipse.Partial(timeOfMaximumEclipse,
                                magnitudeUmbral,
                                radiusPenumbral,
                                radiusUmbral,
                                partialPhasePenumbraSemiDuration!!,
                                partialPhaseSemiDuration!!)
                        else -> EventMoonEclipse.Total(timeOfMaximumEclipse,
                                magnitudeUmbral,
                                radiusPenumbral,
                                radiusUmbral,
                                partialPhasePenumbraSemiDuration!!,
                                partialPhaseSemiDuration!!,
                                totalPhaseSemiDuration!!
                        )
                    }
                } else null
            } else null) ?: sunEclipse
        } else null

        deltaJd += 0.000325 * sin(A1) +
                0.000165 * sin(A2) +
                0.000164 * sin(A3) +
                0.000126 * sin(A4) +
                0.000110 * sin(A5) +
                0.000062 * sin(A6) +
                0.000060 * sin(A7) +
                0.000056 * sin(A8) +
                0.000047 * sin(A9) +
                0.000042 * sin(A10) +
                0.000040 * sin(A11) +
                0.000037 * sin(A12) +
                0.000035 * sin(A13) +
                0.000023 * sin(A14)

        return MoonEvent(
                timeCorrectionForSecularAcceleration(jd + deltaJd) - (jd + deltaJd) - DELTA_JD_MJD,
                MoonPhase.getPhase(k),
                eclipse)
    }


    private enum class EventTime {
        NEXT,
        PREVIOUS,
        CLOSEST
    }

    companion object {


        /**
         * Holds the value of the secular acceleration of the Moon. Currently equal
         * to -25.858 arcsec/cent^2 (Chapront, Chapront-Touze and Francou, 2002).
         */
        private const val MOON_SECULAR_ACCELERATION = -25.858

        /**
         * Value of the Moon secular acceleration ("/cy^2) for DE200.
         */
        private const val MOON_SECULAR_ACCELERATION_DE200 = -23.8946

        /**
         * Corrects Julian day of calculations of ELP2000 theory for secular
         * acceleration of the Moon. This method uses the current value of static
         * variable {@linkplain MOON_SECULAR_ACCELERATION}.
         * <BR>
         * Correction should be performed to standard dynamical time of calculations
         * (Barycentric Dynamical Time), as obtained by using the corresponding methods.
         * {@linkplain elp2000Ephemeris(TimeElement, ObserverElement, EphemerisElement)}
         * accepts any time scale, so it is possible to use the
         * output Julian day of this method with any time scale, unless a very
         * little error (well below the uncertainty in TT-UT correction) could exist
         * if this correction is applied to LT or UT, before the correction to TDB
         * which is performed in {@linkplain Elp2000#elp2000Ephemeris(TimeElement, ObserverElement, EphemerisElement)}.
         * <BR>
         * Correction for different years (using the default value) are as follows:
         *
         * <pre>
         * Year       Correction (seconds)
         * -2000      -2796
         * -1000      -1561
         *     0      -683
         *  1000      -163
         *  1955       0.000
         *  2000      -0.362
         *  3000      -195
         * </pre>
         *
         * @param jd Julian day in TDB.
         * @return Output (corrected) Julian day in TDB.
         */
        fun timeCorrectionForSecularAcceleration(jd: Double): Double {
            val cent = (jd - 2435109.0) / JULIAN_DAYS_PER_CENTURY
            val deltaT = 0.91072 * (MOON_SECULAR_ACCELERATION - MOON_SECULAR_ACCELERATION_DE200) * cent * cent
            return jd + deltaT / SECONDS_PER_DAY
        }

        private fun round(kapprox: Double, delta: Double, eventType: EventTime): Double {
            var k = delta + Math.floor(kapprox)
            if (eventType === EventTime.NEXT && k < kapprox) k++
            if (eventType === EventTime.PREVIOUS && k > kapprox) k--
            if (eventType === EventTime.CLOSEST && k < kapprox - 0.5 && delta == 0.0) k++
            return k
        }

        private fun getClosestK(k: Double) = round(k, MoonPhase.getClosestPhase(k).delta, EventTime.CLOSEST)
    }

}