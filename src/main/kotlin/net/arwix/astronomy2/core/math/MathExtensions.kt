package net.arwix.astronomy2.core.math

import kotlin.math.floor

/**
 * Module operation in arcseconds.
 * @param x Value in arcseconds.
 * @return module
 */
fun Double.mod3600() = this - 1296000.0 * Math.floor(this / 1296000.0)

fun DoubleArray.polynomialSum(x: Double): Double {
    var t = 1.0
    return fold(0.0) { acc, d -> (acc + d * t).let { t *= x; it } }
}

fun frac(x: Double): Double {
    return x - floor(x)
}

infix fun Double.modulo(y: Double) = y * frac(this / y)
