package net.arwix.astronomy2.core.vector

import net.arwix.astronomy2.core.Radian

/**
 * @param phi longitude
 * @param theta latitude
 * @param r radius
 */
class SphericalVector(@JvmField var phi: Radian, @JvmField var theta: Radian, @JvmField var r: Double) : Vector() {

    constructor() : this(0.0, 0.0, 0.0)
    constructor(phi: Radian, theta: Radian) : this(phi, theta, 1.0)
    constructor(vector: Vector) : this() {
        set(vector)
    }

    override fun toArray() = doubleArrayOf(phi, theta, r)

    fun set(phi: Radian, theta: Radian, r: Double) {
        this.phi = phi
        this.theta = theta
        this.r = r
    }

    override fun set(vector: Vector) {
        val sphericalVector: SphericalVector = convert(vector)
        set(sphericalVector.phi, sphericalVector.theta, sphericalVector.r)
    }

    override fun set(i: Int, element: Double) {
        when(i) {
            0 -> phi = element
            1 -> theta = element
            2 -> r = element
            else -> throw IndexOutOfBoundsException()
        }
    }

    override fun get(index: Int): Double = when(index) {
        0 -> phi
        1 -> theta
        2 -> r
        else -> throw IndexOutOfBoundsException()
    }

    override fun unaryMinus(): Vector {
        val rectangularVector: RectangularVector = convert(this)
        rectangularVector.x = -rectangularVector.x
        rectangularVector.y = -rectangularVector.y
        rectangularVector.z = -rectangularVector.z
        return rectangularVector
    }

    override fun plus(vector: Vector) = convert<RectangularVector>(this) + vector
    override fun minus(vector: Vector) = convert<RectangularVector>(this) - vector
    override fun times(scalar: Double) = convert<RectangularVector>(this) * scalar
    override fun times(vector: Vector) = convert<RectangularVector>(this) * vector
    override fun times(right: Matrix) = convert<RectangularVector>(this) * right
    override fun div(scalar: Double) = convert<RectangularVector>(this) / scalar
    override fun dot(vector: Vector) = convert<RectangularVector>(this) dot vector

    override fun component1(): Double = phi
    override fun component2(): Double = theta
    override fun component3(): Double = r

}