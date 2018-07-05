package net.arwix.astronomy2.core.kepler

import net.arwix.astronomy2.core.Ecliptic
import net.arwix.astronomy2.core.JT
import net.arwix.astronomy2.core.Radian
import net.arwix.astronomy2.core.vector.Matrix
import net.arwix.astronomy2.core.vector.Matrix.Companion.AXIS_X
import net.arwix.astronomy2.core.vector.Matrix.Companion.AXIS_Z
import kotlin.math.abs
import kotlin.math.sqrt

object KeplerOrbit {

    /**
     * Computes the transformation matrix from the orbital plane coordinate system to the ecliptic
     *
     * @param Omega Longitude of the ascending node of the orbit
     * @param i inclination of the orbit to the ecliptic
     * @param omega Argument of perihelion
     * @return transformation matrix containing the Gaussian vectors P, Q and R
     */
    @Ecliptic
    fun createGaussianMatrix(Omega: Radian,@Ecliptic i: Radian, omega: Radian): Matrix
            = Matrix(AXIS_Z, -Omega) * Matrix(AXIS_X, -i) * Matrix(AXIS_Z, -omega)

    /**
     * Computes position and velocity vectors for Keplerian orbits w.r.t. the ecliptic
     *
     * @param GM Product of gravitational constant and centre mass [AU^3*d^-2]
     * @param t0 Time of perihelion passage
     * @param t Time for calculation
     * @param q Perihelion distance in AU
     * @param eccentricity Eccentricity of the orbit
     * @param PQR Transformation orbital plane -> ecliptic (Gaussian vectors)
     */
    @Ecliptic
    fun getOrbitalPlane(GM: Double, t0: JT, t: JT, q: Double, eccentricity: Double, PQR: Matrix): OrbitalPlane {
        val M0 = 0.1
        val eps = 0.1
        //
        // Variables
        //
        val delta = abs(1.0 - eccentricity)
        val invax = delta / q;
        val tau = sqrt(GM) * (t - t0)
        val M = tau * sqrt(invax * invax * invax);
        val orbit =
                if ((M < M0) && (delta < eps)) ParabolicOrbit.getOrbitalPlane(GM, t0, t, q, eccentricity)
                else if (eccentricity < 1.0) EllipticOrbit.getOrbitalPlane(GM, M, 1.0 / invax, eccentricity)
                else HyperbolicOrbit.getOrbitalPlane(GM, t0, t, 1.0 / invax, eccentricity)
        return OrbitalPlane(PQR * orbit.position, PQR * orbit.velocity)
    }

}