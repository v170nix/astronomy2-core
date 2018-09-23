package net.arwix.astronomy2.core.kepler

import net.arwix.astronomy2.core.JT
import net.arwix.astronomy2.core.Radian
import net.arwix.astronomy2.core.vector.RectangularVector
import kotlin.math.*

object HyperbolicOrbit {

    /**
     * Computes the eccentric anomaly for hyperbolic orbits
     */
    fun getEccentricAnomaly(meanAnomaly: Radian, eccentricity: Double, maxInteractions: Int = 15): Radian {

        val eps = Math.ulp(100.0)
        var i = 0
        var f: Double
        // Starting value
        var H = ln(2.0 * abs(meanAnomaly) / eccentricity + 1.8)
        if (meanAnomaly < 0.0) H = -H
        do {
            f = eccentricity * sinh(H) - H - meanAnomaly
            H -= f / (eccentricity * cosh(H) - 1.0)
            ++i
            if (i == maxInteractions) throw IndexOutOfBoundsException("Convergence problems in EccentricAnomaly")

        } while (abs(f) > eps * (1.0 + abs(H + meanAnomaly)))
        return H
    }

    /**
     * @param GM Product of gravitational constant and centre mass [AU^3*d^-2]
     * @param t0 Time of perihelion passage
     * @param t Time for calculation
     * @param A Semi-major axis of the orbit in AU
     * @param e eccentricity of the orbit (>1)
     */
    fun getOrbitalPlane(GM: Double, t0: JT, t: JT, A: Double, eccentricity: Double, maxInteractions: Int = 15): OrbitalPlane {

        val a = abs(A)
        val k = sqrt(GM / a)
        val Mh = k * (t - t0) / a
        val H = getEccentricAnomaly(Mh, eccentricity, maxInteractions)
        val coshH = cosh(H)
        val sinhH = sinh(H)
        val fac = sqrt((eccentricity + 1.0) * (eccentricity - 1.0))
        val rho = eccentricity * coshH - 1.0

        return OrbitalPlane(
                RectangularVector(
                        a * (eccentricity - coshH),
                        a * fac * sinhH,
                        0.0),
                RectangularVector(
                        -k * sinhH / rho,
                        k * fac * coshH / rho,
                        0.0))
    }


}