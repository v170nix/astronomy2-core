package net.arwix.astronomy2.core.kepler

import net.arwix.astronomy2.core.PI2
import net.arwix.astronomy2.core.Radian
import net.arwix.astronomy2.core.math.modulo
import net.arwix.astronomy2.core.vector.RectangularVector
import kotlin.math.*

object EllipticOrbit {
    /**
     * Computes the eccentric anomaly for elliptic orbits
     *
     * @param meanAnomaly Mean anomaly
     * @param eccentricity Eccentricity of the orbit [0,1[
     * @return Eccentric anomaly
     */
    fun getEccentricAnomaly(meanAnomaly: Radian, eccentricity: Double, maxInteractions: Int = 15): Radian {

        val eps = Math.ulp(100.0)

        var i = 0
        var f: Double
        // Starting value
        val innerM = meanAnomaly modulo PI2
        var eccentricAnomaly = if (eccentricity < 0.8) innerM else PI

        // Iteration
        do {
            f = eccentricAnomaly - eccentricity * sin(eccentricAnomaly) - innerM
            eccentricAnomaly -= f / (1.0 - eccentricity * cos(eccentricAnomaly))
            ++i
            if (i == maxInteractions) throw IndexOutOfBoundsException("Convergence problems in EccentricAnomaly")
        } while (abs(f) > eps)

        return eccentricAnomaly
    }

    /**
     * Computes position and velocity vectors for elliptic orbits
     *
     * @param GM Product of gravitational constant and centre mass [AU^3*d^-2]
     * @param meanAnomaly Mean anomaly
     * @param a Semi-major axis of the orbit in AU
     * @param eccentricity Eccentricity of the orbit (<1)
     */
    fun getOrbitalPlane(GM: Double, meanAnomaly: Radian, a: Double, eccentricity: Double, maxInteractions: Int = 15): OrbitalPlane {
        val k = sqrt(GM / a)
        val eccentricAnomaly = getEccentricAnomaly(meanAnomaly, eccentricity, maxInteractions)
        val cosE = cos(eccentricAnomaly)
        val sinE = sin(eccentricAnomaly)
        val fac = sqrt((1.0 - eccentricity) * (1.0 + eccentricity))
        val rho = 1.0 - eccentricity * cosE
        return OrbitalPlane(
                RectangularVector(
                        a * (cosE - eccentricity),
                        a * fac * sinE,
                        0.0),
                RectangularVector(
                        -k * sinE / rho,
                        k * fac * cosE / rho,
                        0.0))
    }
}