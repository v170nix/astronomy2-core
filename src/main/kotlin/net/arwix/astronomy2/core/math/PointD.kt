package net.arwix.astronomy2.core.math

import kotlin.math.sqrt

data class PointD(
        @JvmField var x: Double = 0.0,
        @JvmField var y: Double = 0.0) {

    fun set(p: PointD) {
        x = p.x
        y = p.y
    }

    fun set(x: Double, y: Double) {
        this.x = x
        this.y = y
    }

    fun offset(dx: Double, dy: Double) {
        x += dx
        y += dy
    }

    fun length(): Double = sqrt(x * x + y *y)
}