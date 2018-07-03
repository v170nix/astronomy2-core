package net.arwix.astronomy2.core.vector

import kotlin.math.sqrt

abstract class Vector {

    abstract fun set(vector: Vector)
    abstract fun toArray(): DoubleArray

    abstract operator fun get(index: Int): Double
    abstract operator fun set(i: Int, element: Double)
    abstract operator fun unaryMinus(): Vector
    abstract operator fun plus(vector: Vector): Vector
    abstract operator fun minus(vector: Vector): Vector
    abstract operator fun times(scalar: Double): Vector
    abstract operator fun times(vector: Vector): Vector
    abstract operator fun times(right: Matrix): Vector
    abstract operator fun div(scalar: Double): Vector
    abstract infix fun dot(vector: Vector): Double
    fun normalize(): Double = sqrt(this dot this)
    operator fun plusAssign(vector: Vector) {
        set(this + vector)
    }
    operator fun minusAssign(vector: Vector) {
        set(this - vector)
    }
    operator fun timesAssign(scalar: Double) {
        set(this * scalar)
    }
    operator fun timesAssign(vector: Vector) {
        set(this * vector)
    }
    operator fun timesAssign(matrix: Matrix) {
        set(this * matrix)
    }
    operator fun divAssign(scalar: Double) {
        set(this / scalar)
    }

}