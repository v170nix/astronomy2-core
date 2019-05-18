package net.arwix.astronomy2.core.ephemeris.coordinates

import net.arwix.astronomy2.core.Azimuthal
import net.arwix.astronomy2.core.Equatorial
import net.arwix.astronomy2.core.Geocentric
import net.arwix.astronomy2.core.Radian
import net.arwix.astronomy2.core.vector.Matrix
import net.arwix.astronomy2.core.vector.SphericalVector
import net.arwix.astronomy2.core.vector.Vector
import net.arwix.astronomy2.core.vector.convert

/**
 * Transform from Equatorial Geocentric to Azimuthal Geocentric
 * phi - location latitude
 */
@Suppress("unused")
class AzimuthalTransform(phi: Radian) {

    @Suppress("MemberVisibilityCanBePrivate")
    val hourMatrix = Matrix.getRotateY(Math.PI / 2.0 - phi)

    /**
     * Получение азимутальных координат
     * @param lambda longitude
     * @param GMST Среднее гринвичское звездное время
     * @param vector ГеоЦентрические экваториальные координаты
     * @return азимутальный вектор
     */
    @Azimuthal
    @Geocentric
    fun getCoordinates(lambda: Radian, GMST: Double, @Equatorial @Geocentric vector: Vector)
            : Vector = getCoordinates(lambda, GMST, convert(vector))

    @Azimuthal
    @Geocentric
    fun getCoordinates(lambda: Radian, GMST: Double, @Equatorial @Geocentric vector: SphericalVector): Vector {
        val tau = GMST + lambda - vector.phi
        return hourMatrix * SphericalVector(tau, vector.theta, vector.r)
    }
}

@Suppress("unused")
fun Vector.toAzimuthal(lambda: Radian, phi: Radian, GMST: Double): Vector {
    val sphericalVector: SphericalVector = convert(this)
    val tau = GMST + lambda - sphericalVector.phi
    val hourMatrix = Matrix.getRotateY(Math.PI / 2.0 - phi)
    return hourMatrix * SphericalVector(tau, sphericalVector.theta, sphericalVector.r)
}

