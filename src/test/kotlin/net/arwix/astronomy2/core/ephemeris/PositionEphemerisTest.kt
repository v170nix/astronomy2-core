package net.arwix.astronomy2.core.ephemeris

import kotlinx.coroutines.experimental.runBlocking
import net.arwix.astronomy2.core.RAD_TO_DEG
import net.arwix.astronomy2.core.calendar.getJT
import net.arwix.astronomy2.core.calendar.getMJD
import net.arwix.astronomy2.core.ephemeris.calculation.PositionEphemeris
import net.arwix.astronomy2.core.ephemeris.precession.*
import net.arwix.astronomy2.core.vector.SphericalVector
import net.arwix.astronomy2.core.vector.Vector
import net.arwix.astronomy2.core.vector.convert
import net.arwix.astronomy2.ephemeris.vsop87a.ID_VSOP87_EARTH
import net.arwix.astronomy2.ephemeris.vsop87a.ID_VSOP87_MARS
import net.arwix.astronomy2.ephemeris.vsop87a.createSuspendedVsop87ACoordinates
import org.junit.jupiter.api.Test
import java.util.*


internal class PositionEphemerisTest {

    @Test
    fun getPositionEphemeris() {
        val deltaT =  69.183278 / 86400.0 / 36525.0 //  getDeltaT(2018, 7) / 86400.0 / 36525


        val jt =  getJT(getMJD(2018, 7, 31, 0, 0, 0, 0)) + deltaT
        val jt0 = jt - 5 / 36525.0
        //    val jt = 0.1857768876007777

//        val precessionElements = createPrecessionElements(ID_PRECESSION_VONDRAK_2011, jt)
//        val elements = EclipticToEquatorialElements(ID_PRECESSION_IAU_2006, jt0)

        val cE = createSuspendedVsop87ACoordinates(ID_VSOP87_EARTH)
        val cB = createSuspendedVsop87ACoordinates(ID_VSOP87_MARS)
        runBlocking {
            val positionEphemeris = PositionEphemeris(ID_PRECESSION_IAU_2006, cE)
            positionEphemeris.setJT0(jt0)
            val options = positionEphemeris.createBodyOptions(jt0, cB)
//            var l = getPositionEphemeris(jt,
//                    elements,
//                    cE,
//                    cB)
//            var r = positionEphemeris.getPosition(jt0, PositionEphemeris.RequestOptions(true),
//                    cB)
            val l = positionEphemeris.getPosition(jt, options)

//            l = r.position

            val p = convert<SphericalVector>(l)
            System.out.println(printLong(p))
            System.out.println(printLat(p))
            System.out.println(printR(p))
        }

//        val cE1 = createMo(ID_VSOP87_EARTH)
//        val cB1 = createSuspendedVsop87ACoordinates(ID_VSOP87_JUPITER)
//        runBlocking {
//            val l = getPositionEphemeris(jt,
//                    precessionElements,
//                    cE,
//                    cB)
//            val p = convert<SphericalVector>(l)
//            System.out.println(printLong(p))
//            System.out.println(printLat(p))
//            System.out.println(printR(p))
//        }
    }

    private fun printLong(p: Vector): String {
        val vector = convert<SphericalVector>(p)

        val hours = RAD_TO_DEG * vector.phi / 15.0

        val hour = hours.toInt()
        val minutes = (hours - hour) * 60.0
        val minute = minutes.toInt()
        val seconds = (minutes - minute) * 60.0

        return String.format(Locale.ENGLISH, "%1$02d:%2$02d:%3$.2f", hour, minute, seconds)
    }

    private fun printLat(p: Vector): String {
        val vector = convert<SphericalVector>(p)

        val g = Math.toDegrees(vector.theta).toInt()
        val mm = (Math.toDegrees(vector.theta) - g) * 60.0
        val m = mm.toInt()
        val s = (mm - m) * 60.0
        return String.format(Locale.ENGLISH, "%1$02d %2$02d %3$.1f", g, Math.abs(m), Math.abs(s))
    }

    private fun printR(p: Vector): String {
        val vector = convert<SphericalVector>(p)
        return String.format(Locale.ENGLISH, "%1$.6f", vector.r)
    }
}