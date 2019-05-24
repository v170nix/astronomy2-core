package net.arwix.astronomy2.core.ephemeris.event.sun.calculation

import net.arwix.astronomy2.core.*
import net.arwix.astronomy2.core.calendar.getGMST
import net.arwix.astronomy2.core.calendar.getJT
import net.arwix.astronomy2.core.math.modulo
import net.arwix.astronomy2.core.math.toDeg
import net.arwix.astronomy2.core.vector.*
import net.arwix.astronomy2.core.vector.Vector
import java.util.*
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import kotlin.math.sqrt

class SolarEclipseCalculation(
        var jd0: MJD,
        @Geocentric
        @Apparent
        @Equatorial
        val moonEphemeris: suspend (jt: JT) -> Vector,
        val sunEphemeris: suspend (jt: JT) -> Vector) {

    suspend fun intersect(jd: MJD) {
        val fac = 0.996647
        val jt = getJT(jd)
//        val obl = createObliquityElements(ID_OBLIQUITY_WILLIAMS_1994, jt)
        val ephemMoon = convert<RectangularVector>(moonEphemeris(jt))
        val ephemSun = convert<RectangularVector>(sunEphemeris(jt))
        ephemMoon.z = ephemMoon.z / fac
        ephemSun.z = ephemSun.z / fac
        println()

        val r_MS = (ephemMoon - ephemSun).normalize()
//        println("r_MS=${r_MS * AU}")
        val e = (ephemMoon - ephemSun) / r_MS
//        println("e=$e")
        val e_spher: SphericalVector = convert<SphericalVector>(e)
//        println("e_spher=${e_spher.phi}")

        val s0 = -(ephemMoon dot e)
//        println("s0=${s0 * AU}")

        val R_Sun = 696000.0
        val R_Moon = 1737.4
        //  val R_Moon =  1738.0
//        val R_Earth = 6376.0606
        val R_Earth = 6378.137

        val Delta = s0 * s0 + R_Earth * R_Earth / AU / AU - (ephemMoon dot ephemMoon)
//        println("Delta=${Delta * AU}")
        val r0 = sqrt(R_Earth * R_Earth - Delta * AU * AU)
        print(" ; r0=${r0}")

        val dUmbra = (R_Sun - R_Moon) * (s0 / r_MS) - R_Moon
        val dPenumbra = (R_Sun - R_Moon) * (s0 / r_MS) + R_Moon

        print(" ; umbra=${dUmbra}")
        print(" ; dPenumbra=${dPenumbra}")

        val s = s0 - sqrt(Delta)
//        println("s=$s")
        val r = convert<RectangularVector>(ephemMoon + e * s)
//        println("r=$r")
        val rr = RectangularVector(r.x, r.y, fac * r.z)
//        println("rr=$rr")

        val r_G: SphericalVector = convert(Matrix.getRotateZ(getGMST(jd)) * rr)
        val lambda = (r_G.phi + PI).modulo(PI2) - PI
        print(" ; lambda=${lambda.toDeg()}")
        print(" ; lambda=${printLat(SphericalVector(0.0, abs(lambda)))}")
        val phi = r_G.theta
        val phiG = phi + 0.1924 * DEG_TO_RAD * sin(2 * phi)
        print(" ; phig=${phiG.toDeg()}")
        print(" ; phig=${printLat(SphericalVector(0.0, abs(phiG)))}")



        if (r0 < R_Earth) {
            val s = s0 - sqrt(Delta)
//            println("s=$s")
            val r = convert<RectangularVector>(ephemMoon + e * s)
//            println("r=$r")
            val rr = RectangularVector(r.x, r.y, fac * r.z)
//            println("rr=$rr")

            val D_UmbraS = (R_Sun - R_Moon) * (s / r_MS) - R_Moon
            print("  dUmbraSurface=$D_UmbraS")
            if (D_UmbraS > 0.0) {
                print("  annular")
            } else {
                print("  total")
            }
        } else {
            if (r0 < R_Earth + 0.5 * abs(dUmbra)) {
                if (dUmbra > 0.0) {
                    print("  NonCenAnn")
                } else {
                    print("  NonCenTot")
                }
            } else {
                if (r0 < R_Earth + 0.5 * abs(dPenumbra)) {
                    print("  Partical")
                } else {
                    print("  NoEclipse")
                }
            }
        }



    }

    private fun printLat(p: Vector): String {
        val vector = convert<SphericalVector>(p)

        val g = Math.toDegrees(vector.theta).toInt()
        val mm = (Math.toDegrees(vector.theta) - g) * 60.0
        val m = mm.toInt()
        val s = (mm - m) * 60.0
        return String.format(Locale.ENGLISH, "%1$02d %2$02d %3$.1f", g, Math.abs(m), Math.abs(s))
    }
}