package net.arwix.astronomy2.core.vector

import net.arwix.astronomy2.core.Radian
import java.lang.Math.cos
import java.lang.Math.sin

class Matrix() {

    constructor(array1: DoubleArray, array2: DoubleArray, array3: DoubleArray) : this() {
        this[0] = array1
        this[1] = array2
        this[2] = array3
    }

    constructor(vector1: Vector, vector2: Vector, vector3: Vector) : this() {
        this[0] = vector1
        this[1] = vector2
        this[2] = vector3
    }

    private var elements = arrayOf(
            doubleArrayOf(0.0, 0.0, 0.0),
            doubleArrayOf(0.0, 0.0, 0.0),
            doubleArrayOf(0.0, 0.0, 0.0))

    fun transpose(): Matrix {
        val out = Matrix()
        for (i in 0..2) {
            for (j in 0..2) {
                out[i, j] = this[j, i]
            }
        }
        return out
    }

    operator fun times(right: Matrix): Matrix = Matrix().apply {
        for (i in 0..2) {
            for (j in 0..2) {
                this[i, j] = (0..2).sumByDouble { k -> this@Matrix[i, k] * right[k, j] }
            }
        }
    }

    operator fun times(right: Vector): Vector {
        val vector: RectangularVector = convert(right)
        return RectangularVector().apply {
            for (i in 0..2) {
                this[i] = (0..2).sumByDouble { j -> this@Matrix[i, j] * vector[j] }
            }
        }

    }

    operator fun timesAssign(matrix: Matrix) {
        elements = (this * matrix).elements
    }

    operator fun get(i: Int, j: Int): Double = elements[i][j]

    operator fun set(i: Int, j: Int, element: Double) {
        elements[i][j] = element
    }

    operator fun get(i: Int): Vector = RectangularVector(elements[i])
    operator fun set(i: Int, array: DoubleArray) {
        elements[i] = array
    }

    operator fun set(i: Int, vector: Vector) {
        elements[i] = convert<RectangularVector>(vector).toArray()
    }

    fun toArray(): Array<DoubleArray> = elements.copyOf()

    companion object {

        const val AXIS_X = 1
        const val AXIS_Y = 2
        const val AXIS_Z = 3

        operator fun invoke(axis: Int, angle: Radian): Matrix = when (axis) {
            AXIS_X -> getRotateX(angle)
            AXIS_Y -> getRotateY(angle)
            AXIS_Z -> getRotateZ(angle)
            else -> throw IndexOutOfBoundsException()
        }

        /**
         * матрицы поворта вокруг осей базиса
         * elementary rotations
         */
        fun getRotateX(angle: Radian): Matrix {
            val s = sin(angle)
            val c = cos(angle)
            return Matrix(
                    doubleArrayOf(1.0, 0.0, 0.0),
                    doubleArrayOf(0.0, c, s),
                    doubleArrayOf(0.0, -s, c)
            )
        }

        fun getRotateY(angle: Radian): Matrix {
            val s = sin(angle)
            val c = cos(angle)
            return Matrix(
                    doubleArrayOf(c, 0.0, -s),
                    doubleArrayOf(0.0, 1.0, 0.0),
                    doubleArrayOf(s, 0.0, c)
            )
        }

        fun getRotateZ(angle: Radian): Matrix {
            val s = sin(angle)
            val c = cos(angle)
            return Matrix(
                    doubleArrayOf(c, s, 0.0),
                    doubleArrayOf(-s, c, 0.0),
                    doubleArrayOf(0.0, 0.0, 1.0)
            )
        }

    }
}