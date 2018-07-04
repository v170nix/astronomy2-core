package net.arwix.astronomy2.core.vector

data class RectangularVector(@JvmField var x: Double,
                             @JvmField var y: Double,
                             @JvmField var z: Double): Vector() {

    constructor(array: DoubleArray): this(array[0], array[1], array[2])
    constructor(): this(0.0, 0.0, 0.0)
    constructor(vector: Vector): this() {
        set(vector)
    }

    override fun set(vector: Vector) {
        val rectangularVector: RectangularVector = convert(vector)
        set(rectangularVector.x, rectangularVector.y, rectangularVector.z)
    }

    fun set(x: Double, y: Double, z: Double) {
        this.x = x
        this.y = y
        this.z = z
    }

    override fun toArray(): DoubleArray = doubleArrayOf(x, y, z)

    override fun get(index: Int): Double = when(index) {
        0 -> x
        1 -> y
        2 -> z
        else -> throw  IndexOutOfBoundsException()
    }

    override fun set(i: Int, element: Double) {
        when(i) {
            0 -> x = element
            1 -> y = element
            2 -> z = element
            else -> throw IndexOutOfBoundsException()
        }
    }

    override fun unaryMinus(): Vector = RectangularVector(-x, -y, -z)

    override fun plus(vector: Vector): Vector {
        val rightVector: RectangularVector = convert(vector)
        return RectangularVector(x + rightVector.x, y + rightVector.y, z + rightVector.z)
    }

    override fun minus(vector: Vector): Vector {
        val rightVector: RectangularVector = convert(vector)
        return RectangularVector(x - rightVector.x, y - rightVector.y, z - rightVector.z)
    }

    override fun times(scalar: Double): Vector {
        return RectangularVector(x * scalar, y * scalar, z * scalar)
    }

    override fun times(vector: Vector): Vector {
        val rightVector: RectangularVector = convert(vector)
        return RectangularVector(
                y * rightVector.z - z * rightVector.y,
                z * rightVector.x - x * rightVector.z,
                x * rightVector.y - y * rightVector.x
        )
    }

    override fun times(right: Matrix): Vector {
        return RectangularVector().apply {
            (0..2).forEach { j -> this[j] = (0..2).sumByDouble { i -> this@RectangularVector[i] * right[i, j] } }
        }
    }

    override fun div(scalar: Double): Vector = this * (1.0 / scalar)

    override fun dot(vector: Vector): Double {
        val rightVector: RectangularVector = convert(vector)
        return x * rightVector.x + y * rightVector.y + z * rightVector.z
    }
}