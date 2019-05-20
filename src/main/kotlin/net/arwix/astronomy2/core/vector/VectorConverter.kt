package net.arwix.astronomy2.core.vector

import java.lang.Math.*


inline fun <reified T: Vector> convert(vector: Vector): T {
    when (vector) {
        is T -> return vector
        is SphericalVector -> {
            val cosEl = cos(vector.theta)
            val rectangularVector = RectangularVector(
                    vector.r * cos(vector.phi) * cosEl,
                    vector.r * sin(vector.phi) * cosEl,
                    vector.r * sin(vector.theta)
            )
            if (rectangularVector is T) return rectangularVector else throw NotImplementedError()
        }
        is RectangularVector -> {
            val sphericalVector = SphericalVector()
            val XYSqr = vector.x * vector.x + vector.y * vector.y
            // Модуль вектора
            sphericalVector.r = sqrt(XYSqr + vector.z * vector.z)
            // Азимут вектора
            sphericalVector.phi = if (vector.x == 0.0 && vector.y == 0.0) 0.0 else atan2(vector.y, vector.x)
            if (sphericalVector.phi < 0.0) sphericalVector.phi += 2.0 * PI
            // высота вектора
            val rho = sqrt(XYSqr)
            sphericalVector.theta = if (vector.z == 0.0 && rho == 0.0) 0.0 else atan2(vector.z, rho)
            if (sphericalVector is T) return sphericalVector else throw NotImplementedError()
        }
        else -> throw NotImplementedError()
    }
}