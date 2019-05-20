package net.arwix.astronomy2.core.kepler

import net.arwix.astronomy2.core.JT
import net.arwix.astronomy2.core.vector.RectangularVector
import kotlin.math.abs
import kotlin.math.sqrt

@Suppress("LocalVariableName")
object ParabolicOrbit {

    /**
     * computes values for the Stumpff functions C1, C2 and C3
     * @param eccentricAnomaly2 square of eccentric anomaly (E2=E*E) in
     * @return C1 = sin(E)/E, C2 = (1-cos(E))/(E*E), C3 = (E-sin(E))/(E^3)
     */
    private fun stumpff(eccentricAnomaly2: Double): Triple<Double, Double, Double> {
        val eps = Math.ulp(100.0)
        var c1 = 0.0
        var c2 = 0.0
        var c3 = 0.0
        var add = 1.0
        var n = 1.0
        do {
            c1 += add; add /= (2.0 * n)
            c2 += add; add /= (2.0 * n + 1.0)
            c3 += add; add *= -eccentricAnomaly2
            n += 1.0
        } while (abs(add) >= eps)
        return Triple(c1, c2, c3)
    }

    /**
     * Computes position and velocity vectors for parabolic and near parabolic orbits
     * @param GM Product of gravitational constant and centre mass [AU^3*d^-2]
     * @param t0 Time of perihelion passage
     * @param t Time for calculation
     * @param q Perihelion distance in AU
     * @param e eccentricity of the orbit (~1)
     */
    fun getOrbitalPlane(GM: Double, t0: JT, t: JT, q: Double, e: Double, maxInteractions: Int = 15): OrbitalPlane {

        val eps = Math.ulp(100.0)

        var i = 0
        var E20: Double
        var E2 = 0.0
        var u: Double
        var u2: Double
        var c: Triple<Double, Double, Double>
        var fac = 0.5 * e

        val k = sqrt(GM / (q * (1.0 + e)))
        val tau = sqrt(GM) * (t - t0)

        do {
            ++i
            E20 = E2
            val A = 1.5 * sqrt(fac / (q * q * q)) * tau
            val B = Math.pow(sqrt(A * A + 1.0) + A, 1.0 / 3.0)
            u = B - 1.0 / B
            u2 = u * u
            E2 = u2 * (1.0 - e) / fac
            c = stumpff(E2)
            fac = 3.0 * e * c.third
            if (i == maxInteractions) throw IndexOutOfBoundsException("Convergence problems in Parabolic Orbit")

        } while (abs(E2 - E20) >= eps)

        val (c1, c2) = c

        val R = q * (1.0 + u2 * c2 * e / fac)
        val r = RectangularVector(q * (1.0 - u2 * c2 / fac), q * Math.sqrt((1.0 + e) / fac) * u * c1, 0.0)
        return OrbitalPlane(r, RectangularVector(-k * r.y / R, k * (r.x / R + e), 0.0))
    }

}