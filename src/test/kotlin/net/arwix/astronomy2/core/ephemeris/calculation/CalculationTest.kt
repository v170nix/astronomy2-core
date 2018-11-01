package net.arwix.astronomy2.core.ephemeris.calculation

import kotlinx.coroutines.runBlocking
import net.arwix.astronomy2.core.DEG_TO_RAD
import net.arwix.astronomy2.core.Degree
import net.arwix.astronomy2.core.calendar.*
import net.arwix.astronomy2.core.ephemeris.fast.createSuspendedFastMoonGeocentricEclipticApparentEphemeris
import net.arwix.astronomy2.core.ephemeris.fast.createFastSunGeocentricEclipticApparentEphemeris
import net.arwix.astronomy2.core.ephemeris.obliquity.ID_OBLIQUITY_SIMON_1994
import net.arwix.astronomy2.core.ephemeris.obliquity.getObliquityMatrix
import net.arwix.astronomy2.core.ephemeris.precession.ID_PRECESSION_IAU_1976
import net.arwix.astronomy2.ephemeris.vsop87a.ID_VSOP87_EARTH
import net.arwix.astronomy2.ephemeris.vsop87a.ID_VSOP87_SUN
import net.arwix.astronomy2.ephemeris.vsop87a.createSuspendedVsop87ACoordinates

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.text.SimpleDateFormat
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class CalculationTest {

    @ParameterizedTest
    @MethodSource("getRiseSetSunData")
    fun `RiseSetSunVsop87A`(data: RiseSetData) {
        val calendar = data.getCalendar()
        val jt0 = calendar.getJT()

//        val earthCoordinates = createSuspendedVsop87ACoordinates(ID_VSOP87_EARTH)
//        val sunCoordinates = createSuspendedVsop87ACoordinates(ID_VSOP87_SUN)
//
//        runBlocking {
//            val ephemeris = PositionEphemeris(ID_PRECESSION_IAU_1976, earthCoordinates)
//                    .setJT0(jt0)
//            val options = ephemeris.createBodyOptions(jt0, sunCoordinates)
//            val result =findRiseSet(ObjectType.SUN,
//                    calendar,
//                    data.getRadLatitude(),
//                    data.getRadLongitude())
//            { jt ->
//                ephemeris.getPosition(jt, options)
//            }
//            riseSetCheck(data, result)
//        }
    }

    @ParameterizedTest
    @MethodSource("getRiseSetSunData")
    fun `RiseSetSunFast`(data: RiseSetData) {
        val calendar = data.getCalendar()

        val sunCoordinates = createFastSunGeocentricEclipticApparentEphemeris()
        val obliquity = getObliquityMatrix(ID_OBLIQUITY_SIMON_1994, calendar.getJT())

        runBlocking {
            val result =findRiseSet(ObjectType.SUN,
                    calendar,
                    data.getRadLatitude(),
                    data.getRadLongitude())
            { jt -> obliquity * sunCoordinates(jt) }

            riseSetCheck(data, result)
        }
    }

    @ParameterizedTest
    @MethodSource("getRiseSetMoonData")
    fun `RiseSetMoonFast`(data: RiseSetData) {
        val calendar = data.getCalendar()

        val obliquity = getObliquityMatrix(ID_OBLIQUITY_SIMON_1994, calendar.getJT())

        runBlocking {
            val moonCoordinates = createSuspendedFastMoonGeocentricEclipticApparentEphemeris()
            val result =findRiseSet(ObjectType.MOON,
                    calendar,
                    data.getRadLatitude(),
                    data.getRadLongitude())
            { jt -> obliquity * moonCoordinates(jt) }

            riseSetCheck(data, result)
        }
    }

    private fun riseSetCheck(data: RiseSetData, result: RiseSetCalculationResult) {
        val riseResult = when (result) {
            is RiseSetCalculationResult.RiseSet -> result.rise
            is RiseSetCalculationResult.Rise -> result
            else -> null
        }

        val setResult = when (result) {
            is RiseSetCalculationResult.RiseSet -> result.set
            is RiseSetCalculationResult.Set -> result
            else -> null
        }

        val formater = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z")

        assertEquals(data.riseDate != null, riseResult != null)
        assertEquals(data.setDate != null, setResult != null)

        formater.timeZone = riseResult?.calendar?.timeZone ?: setResult!!.calendar.timeZone

        if (riseResult != null) assertEquals(data.riseDate, formater.format(riseResult.calendar.time))
        if (setResult != null) assertEquals(data.setDate, formater.format(setResult.calendar.time))
    }

    internal data class RiseSetData(val year: Int,
                                    val month: Int,
                                    val day: Int,
                                    val timeZoneId: String,
                                    val longitude: Degree,
                                    val latitude: Degree,
                                    val riseDate: String? = null,
                                    val setDate: String? = null) {
        fun getCalendar() = Calendar.getInstance(TimeZone.getTimeZone(timeZoneId))
                .year(year)
                .month(month)
                .dayOfMonth(day)
                .hourOfDay(1)

        fun getRadLatitude() = DEG_TO_RAD * latitude
        fun getRadLongitude() = DEG_TO_RAD * longitude
    }

    fun getRiseSetSunData() = listOf(
            RiseSetData(2018, Calendar.AUGUST, 7, "Europe/Moscow", 30.325, 60.0583333,
                    "2018-08-07 04:52:41 MSK", "2018-08-07 21:14:35 MSK"),
            RiseSetData(2018, Calendar.AUGUST, 7, "America/New_York", -74.0059, 40.7127,
                    "2018-08-07 05:58:28 EDT", "2018-08-07 20:04:19 EDT"),
            RiseSetData(2018, Calendar.AUGUST, 24, "Australia/Sydney", 151.209444, -33.865,
                    "2018-08-24 06:23:58 AEST", "2018-08-24 17:31:43 AEST")
    )

    fun getRiseSetMoonData() = listOf(
            RiseSetData(2018, Calendar.AUGUST, 6, "Europe/Moscow", 30.325, 60.0583333,
                    null, "2018-08-06 16:28:42 MSK"),
            RiseSetData(2018, Calendar.AUGUST, 7, "America/New_York", -74.0059, 40.7127,
                    "2018-08-07 01:49:30 EDT", "2018-08-07 16:43:37 EDT"),
            RiseSetData(2018, Calendar.AUGUST, 24, "Australia/Sydney", 151.209444, -33.865,
                    "2018-08-24 15:27:41 AEST", "2018-08-24 04:53:35 AEST")
    )

}