package net.arwix.astronomy2.core.ephemeris.event.sun.calculation

import net.arwix.astronomy2.core.*
import net.arwix.astronomy2.core.calendar.getGMST
import net.arwix.astronomy2.core.calendar.getJT
import net.arwix.astronomy2.core.ephemeris.obliquity.ID_OBLIQUITY_WILLIAMS_1994
import net.arwix.astronomy2.core.ephemeris.obliquity.createObliquityElements
import net.arwix.astronomy2.core.math.modulo
import net.arwix.astronomy2.core.math.toDeg
import net.arwix.astronomy2.core.vector.*
import net.arwix.astronomy2.core.vector.Vector
import java.util.*
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import kotlin.math.sqrt

class SolarEclipseCalculation2(
        var jd0: Double,
        val moonEphemeris: suspend (jt: JT) -> Vector,
        val sunEphemeris: suspend (jt: JT) -> Vector) {


    suspend fun init() {
//        val jt =  getJT(2458667.30800  - DELTA_JD_MJD)
//        jd0 = 2459198.1759259 - DELTA_JD_MJD
//        jd0 = 2458667.307558 - DELTA_JD_MJD
        val fac = 0.996647
        println(jd0)
        val jt = getJT(jd0)
        val obl = createObliquityElements(ID_OBLIQUITY_WILLIAMS_1994, jt)
        val moon = convert<SphericalVector>(moonEphemeris(jt))
//        moon.r = moon.r * AU
        val sun = convert<SphericalVector>(sunEphemeris(jt))
//        sun.r = sun.r * AU

        var ephem_moon: Vector = convert<SphericalVector>(obl.rotateEclipticVector(moon))
        var ephem_sun: Vector = convert<SphericalVector>(obl.rotateEclipticVector(sun))
//        ephem_moon as SphericalVector
//        ephem_moon.phi = ((6.0 + 46.0 / 60.0 + 17.8/60.0/60.0) * 15.0).toRad()
//        ephem_moon.theta = (22.0 + 22.0 / 60.0 + 9.7/60.0/60.0).toRad()
//        ephem_moon.r = 0.00245811959363
//
//        ephem_sun as SphericalVector
//        ephem_sun.phi = ((6.0 + 46.0 / 60.0 + 17.7/60.0/60.0) * 15.0).toRad()
//        ephem_sun.theta = (23.0 + 0.0 / 60.0 + 36.5/60.0/60.0).toRad()
//        ephem_sun.r = 1.01674030543874

//        ephem_moon as SphericalVector
//        ephem_moon.phi = ((16.0 + 42.0 / 60.0 + 34.9/60.0/60.0) * 15.0).toRad()
//        ephem_moon.theta = -(23.0 + 13.0 / 60.0 + 22.1/60.0/60.0).toRad()
////        ephem_moon.r = 0.00245811959363
////
//        ephem_sun as SphericalVector
//        ephem_sun.phi = ((16.0 + 43.0 / 60.0 + 32.3/60.0/60.0) * 15.0).toRad()
//        ephem_sun.theta = -(22.0 + 16.0 / 60.0 + 29.3/60.0/60.0).toRad()
//        ephem_sun.r = 1.01674030543874

        ephem_moon = convert<RectangularVector>(ephem_moon)
        ephem_moon.z = ephem_moon.z / fac
//
        ephem_sun = convert<RectangularVector>(ephem_sun)
        ephem_sun.z = ephem_sun.z / fac


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
//        val dist = LunarEclipseCalculation.getAngularDistance(ephem_moon, ephem_sun)
//        println("dist=$dist")

//        val lib = getEckhardtMoonLibrations(jt, getNutationAngles(ID_NUTATION_IAU_1980, jt), obl.eclipticToEquatorialMatrix, ephem_moon)

//        val positionAngleOfAxis = lib[2]
//        println("positionAngleOfAxis=${positionAngleOfAxis.toDeg()}")

//        val pa = 3 * PI_OVER_TWO - LunarEclipseCalculation.getPositionAngle(ephem_sun, ephem_moon) - positionAngleOfAxis
//        println("pa=$pa")
//        val m_x = sin(pa) / LunarEclipseCalculation.getAngularRadius(ephem_moon, 1737.4)
//        println("m_x=$m_x")
//        val m_y = cos(pa) / LunarEclipseCalculation.getAngularRadius(ephem_moon, 1737.4)
//        println("m_y=$m_y")
//        val m_r = 1.0 / hypot(m_x, m_y)
//        println("m_r=$m_r")
//        val s_r = LunarEclipseCalculation.getAngularRadius(ephem_sun, 696000.0)
//        println("s_r=$s_r")

        println("sun=${convert<RectangularVector>(ephem_sun)}")

        val r_MS = (ephem_moon - ephem_sun).normalize()
        println("r_MS=${r_MS * AU}")
        val e = (ephem_moon - ephem_sun) / r_MS
        println("e=$e")
        val e_spher: SphericalVector = convert<SphericalVector>(e)
        println("e_spher=${e_spher.phi}")

        val s0 = -(ephem_moon dot e)
        println("s0=${s0 * AU}")

        val R_Sun = 696000.0
        val R_Moon = 1737.4
        //  val R_Moon =  1738.0
//        val R_Earth = 6376.0606
        val R_Earth = 6378.137

        val Delta = s0 * s0 + R_Earth * R_Earth / AU / AU - (ephem_moon dot ephem_moon)
        println("Delta=${Delta * AU}")
        val r0 = sqrt(R_Earth * R_Earth - Delta * AU)
        println("r0=${r0}")


        val dUmbra = (R_Sun - R_Moon) * (s0 / r_MS) - R_Moon
        val dPenumbra = (R_Sun - R_Moon) * (s0 / r_MS) + R_Moon

        println("umbra=${dUmbra}")
        println("dPenumbra=${dPenumbra}")


        val s = s0 - sqrt(Delta)
        println("s=$s")
        val r = ephem_moon + e * s
        println("r=$r")
        val rr = RectangularVector(r.component1(), r.component2(), (1.0 - 1.0 / 298.257223563) * r.component3())
        println("rr=$rr")

        val D_UmbraS = (R_Sun - R_Moon) * (s / r_MS) - R_Moon
        println("dUmbraSurface=$D_UmbraS")


        val r_G: SphericalVector = convert(Matrix.getRotateZ(getGMST(jd0)) * rr)
        val lambda = (r_G.phi + PI).modulo(PI2) - PI
        println("lambda=${lambda.toDeg()}")
        println("lambda=${printLat(SphericalVector(0.0, abs(lambda)))}")
        val phi = r_G.theta
        val phiG = phi + 0.1924 * DEG_TO_RAD * sin(2 * phi)
        println("phig=${phiG.toDeg()}")
        println("phig=${printLat(SphericalVector(0.0, abs(phiG)))}")


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