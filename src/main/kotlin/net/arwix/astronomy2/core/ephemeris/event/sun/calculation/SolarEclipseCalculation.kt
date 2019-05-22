package net.arwix.astronomy2.core.ephemeris.event.sun.calculation

import net.arwix.astronomy2.core.*
import net.arwix.astronomy2.core.calendar.getGMST
import net.arwix.astronomy2.core.calendar.getJT
import net.arwix.astronomy2.core.ephemeris.event.moon.calculation.LunarEclipseCalculation
import net.arwix.astronomy2.core.ephemeris.event.moon.calculation.getEckhardtMoonLibrations
import net.arwix.astronomy2.core.ephemeris.nutation.ID_NUTATION_IAU_1980
import net.arwix.astronomy2.core.ephemeris.nutation.getNutationAngles
import net.arwix.astronomy2.core.ephemeris.obliquity.ID_OBLIQUITY_WILLIAMS_1994
import net.arwix.astronomy2.core.ephemeris.obliquity.createObliquityElements
import net.arwix.astronomy2.core.math.modulo
import net.arwix.astronomy2.core.math.toDeg
import net.arwix.astronomy2.core.vector.*
import net.arwix.astronomy2.core.vector.Vector
import java.util.*
import kotlin.math.*

class SolarEclipseCalculation(
        var jd0: Double,
        val moonEphemeris: suspend (jt: JT) -> Vector,
        val sunEphemeris: suspend (jt: JT) -> Vector) {


    suspend fun init() {
//        val jt =  getJT(2458667.30800  - DELTA_JD_MJD)
        jd0 = 2459198.1759259 - DELTA_JD_MJD
        jd0 = 2458667.307558 - DELTA_JD_MJD
        println(jd0)
        val jt = getJT(jd0)
        val obl = createObliquityElements(ID_OBLIQUITY_WILLIAMS_1994, jt)
        val moon = convert<SphericalVector>(moonEphemeris(jt))
//        moon.r = moon.r * AU
        val sun = convert<SphericalVector>(sunEphemeris(jt))
//        sun.r = sun.r * AU

        val ephem_moon = convert<SphericalVector>(obl.rotateEclipticVector(moon))
        val ephem_sun = convert<SphericalVector>(obl.rotateEclipticVector(sun))
        println("===moon===")
        println("long=" + printLong(ephem_moon))
        println("lat=" + printLat(ephem_moon))
        println("r=" + printR(ephem_moon))
        println("===sun===")
        println("long=" + printLong(ephem_sun))
        println("lat=" + printLat(ephem_sun))
        println("r=" + printR(ephem_sun))

//        ephem_moon.r = 1.0
//        ephem_sun.r = 1.0

//====
        val dist = LunarEclipseCalculation.getAngularDistance(ephem_moon, ephem_sun)
        println("dist=$dist")

        val lib = getEckhardtMoonLibrations(jt, getNutationAngles(ID_NUTATION_IAU_1980, jt), obl.eclipticToEquatorialMatrix, ephem_moon)

        val positionAngleOfAxis = lib[2]
        println("positionAngleOfAxis=${positionAngleOfAxis.toDeg()}")

        val pa = 3 * PI_OVER_TWO - LunarEclipseCalculation.getPositionAngle(ephem_sun, ephem_moon) - positionAngleOfAxis
        println("pa=$pa")
        val m_x = sin(pa) / LunarEclipseCalculation.getAngularRadius(ephem_moon, 1737.4)
        println("m_x=$m_x")
        val m_y = cos(pa) / LunarEclipseCalculation.getAngularRadius(ephem_moon, 1737.4)
        println("m_y=$m_y")
        val m_r = 1.0 / hypot(m_x, m_y)
        println("m_r=$m_r")
        val s_r = LunarEclipseCalculation.getAngularRadius(ephem_sun, 696000.0)
        println("s_r=$s_r")

        println("sun=${convert<RectangularVector>(ephem_sun)}")

        val r_MS = (ephem_moon - ephem_sun).normalize()
        println("r_MS=${r_MS * AU}")
        val e = (ephem_moon - ephem_sun) / r_MS
        println("e=$e")
        val e_spher: SphericalVector = convert<SphericalVector>(e)
        println("e_spher=${e_spher.r}")

        val s0 = -(ephem_moon dot e)
        println("s0=${s0 * AU}")

        val R_Sun = 696000.0
        val R_Moon = 1737.4
        val R_Earth = 6376.0606

        val Delta = s0 * s0 + R_Earth * R_Earth / AU / AU - (ephem_moon dot ephem_moon)
        println("Delta=${Delta * AU}")
        val r0 = sqrt(R_Earth * R_Earth - Delta * AU)
        println("r0=${r0}")

        val dUmbra = (R_Sun - R_Moon) * (s0 / r_MS) - R_Moon
        val dPenumbra = (R_Sun - R_Moon) * (s0 / r_MS) + R_Moon

        println("umbra=${dUmbra / R_Earth}")
        println("dPenumbra=${dPenumbra / R_Earth}")

        val s = s0 - sqrt(Delta)
        println("s=$s")
        val r = ephem_moon + e * s
        println("r=$r")
        val rr = RectangularVector(r.component1(), r.component2(), 0.996647 * r.component3())
        println("rr=$rr")

        val r_G: SphericalVector = convert(Matrix.getRotateZ(getGMST(jd0)) * rr)
        val lambda = (r_G.phi + PI).modulo(PI2) - PI
        println("lambda=${lambda.toDeg()}")
        val phi = r_G.theta
        val phiG = phi + 0.1924 * DEG_TO_RAD * sin(2 * phi)
        println("phig=${phiG.toDeg()}")


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