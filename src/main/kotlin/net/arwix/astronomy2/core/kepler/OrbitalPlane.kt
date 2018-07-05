package net.arwix.astronomy2.core.kepler

import net.arwix.astronomy2.core.GM_Sun
import net.arwix.astronomy2.core.JT
import net.arwix.astronomy2.core.vector.Vector

//   r        Position w.r.t. orbital plane in [AU]
//   v        Velocity w.r.t. orbital plane in [AU/d]
data class OrbitalPlane(val position: Vector, val velocity: Vector)


fun KeplerElements.getOrbitalPlane(jT: JT): OrbitalPlane {
    val p = getPrecessionOfEquinox(jT)
    val M = getMeanAnomaly(jT)

    val orbitalPlane = EllipticOrbit.getOrbitalPlane(
            GM_Sun,
            M,
            getSemiMajorAxis(jT),
            getEccentricity(jT))

    val PQR = KeplerOrbit.createGaussianMatrix(
            getAscendingNodeLongitude(jT) + p,
            getInclination(jT),
            getPerihelionLongitude(jT) -
                    getAscendingNodeLongitude(jT))

    return OrbitalPlane(PQR * orbitalPlane.position, PQR * orbitalPlane.velocity)
}