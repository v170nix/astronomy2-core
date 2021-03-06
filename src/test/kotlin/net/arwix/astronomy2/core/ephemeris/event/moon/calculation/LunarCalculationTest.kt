package net.arwix.astronomy2.core.ephemeris.event.moon.calculation

import kotlinx.coroutines.runBlocking
import net.arwix.astronomy2.core.DELTA_JD_MJD
import net.arwix.astronomy2.core.JT
import net.arwix.astronomy2.core.calendar.*
import net.arwix.astronomy2.core.ephemeris.event.sun.calculation.SolarEclipseCalculation
import net.arwix.astronomy2.core.ephemeris.fast.createEphemerisFastMoonGeocentricEclipticApparent
import net.arwix.astronomy2.core.ephemeris.fast.createEphemerisFastSunGeocentricEclipticApparent
import net.arwix.astronomy2.core.ephemeris.nutation.ID_NUTATION_IAU_1980
import net.arwix.astronomy2.core.ephemeris.nutation.getNutationAngles
import net.arwix.astronomy2.core.ephemeris.obliquity.ID_OBLIQUITY_SIMON_1994
import net.arwix.astronomy2.core.ephemeris.obliquity.ID_OBLIQUITY_WILLIAMS_1994
import net.arwix.astronomy2.core.ephemeris.obliquity.createObliquityElements
import net.arwix.astronomy2.core.ephemeris.obliquity.getObliquityMatrix
import net.arwix.astronomy2.core.math.toDeg
import net.arwix.astronomy2.core.vector.Vector
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.*
import java.util.concurrent.TimeUnit

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class LunarCalculationTest {
    @Test
    fun `Lunar`() {
//4.994219212560823 -5.126502039191297 11.662078982717082
//4.454790274069735 -5.913972855171356 15.89616
        runBlocking {
            val calendar = Calendar.getInstance().apply {
                dayOfMonth(18)
                setHours(3.0)
                minute(0)
            }
            val jt = calendar.getJT(true)
            val ephemeris = createEphemerisFastMoonGeocentricEclipticApparent()
            val vector = ephemeris(jt)
            val r = getEckhardtMoonLibrations(jt,
                    getNutationAngles(ID_NUTATION_IAU_1980, jt),
                    getObliquityMatrix(ID_OBLIQUITY_SIMON_1994, jt),
                    vector)
            print(r[0].toDeg().toString() + " ")
            print(r[1].toDeg().toString() + " ")
            println(r[2].toDeg())

        }
    }

    @Test
    fun `PerigeeTest`() {
//        runBlocking {
        val calendar = Calendar.getInstance().apply {
            //                year(1988)
//                month(10)
            dayOfMonth(20)
            setHours(0.0)
            minute(0)
        }
        val cals = LunarPerigeeApogeeCalculation(calendar)
        val l = cals.getPreviousApogee().take(8).toList()
        println(l.size)
        l.forEach {
            println((it.first.toCalendar(false, TimeZone.getTimeZone("UTC")) as Calendar).time)
            println(6378.14 / Math.sin(it.second))
            println("---")
        }
//        }
    }

    //709+151=860 14:20
    @Test
    fun `LunarTest`() {
        runBlocking {
            val calendar = Calendar.getInstance().apply {
                year(2021)
                month(4)
                dayOfMonth(19)
                setHours(15.0)
                minute(0)
            }
            val moonEph = createEphemerisFastMoonGeocentricEclipticApparent()
            val sunEph = createEphemerisFastSunGeocentricEclipticApparent()
            val susSunEph: suspend (jt: JT) -> Vector = { sunEph(it) }

            val cals = LunarEclipseCalculation(calendar.getMJD() + DELTA_JD_MJD, moonEph, susSunEph)
            cals.init()
            cals.events.forEach {
                println(((it - DELTA_JD_MJD).toCalendar(false, TimeZone.getTimeZone("UTC")) as Calendar).time)
            }

        }
    }

    @Test
    fun `testBE`() {
        runBlocking {
            val calendar = Calendar.getInstance().apply {
                year(2017)
                month(7)
                dayOfMonth(21)
                setHours(16.0 + 3)
                minute(25)
                second(30)
            }
            //                  val mjd = getMJD(2019, 7, 2, 19, 22, 53, 0) + getDeltaT(2019, 7, TimeUnit.DAYS)
            //           val mjd = getMJD(2021, 12, 4, 7, 33, 22, 0) + getDeltaT(2021, 12, TimeUnit.DAYS)

            val mjd = calendar.getMJD() + getDeltaT(2017, 6, TimeUnit.DAYS) //  2459552.814844 - DELTA_JD_MJD
            val obl = createObliquityElements(ID_OBLIQUITY_WILLIAMS_1994, getJT(mjd))
            val moonEcliptic = createEphemerisFastMoonGeocentricEclipticApparent()
            val sunEcliptic = createEphemerisFastSunGeocentricEclipticApparent()

            val moonEph: suspend (jt: JT) -> Vector = { obl.rotateEclipticVector(moonEcliptic(it)) }
            val susSunEph: suspend (jt: JT) -> Vector = { obl.rotateEclipticVector(sunEcliptic(it)) }
            val cals = SolarEclipseCalculation(mjd, moonEph, susSunEph)

            (0..(300 / 3)).forEach {
                //                cals.init()

                cals.intersect(mjd + it * 3 / 60.0 / 24.0)
            }



        }
    }

    private fun printLat(theta: Double): String {
        val g = Math.toDegrees(theta).toInt()
        val mm = (Math.toDegrees(theta) - g) * 60.0
        val m = mm.toInt()
        val s = (mm - m) * 60.0
        return String.format(Locale.ENGLISH, "%1$02d %2$02d %3$.1f", g, Math.abs(m), Math.abs(s))
    }
}