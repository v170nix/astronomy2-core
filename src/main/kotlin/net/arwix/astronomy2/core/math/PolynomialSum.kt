package net.arwix.astronomy2.core.math

inline fun DoubleArray.polynomialSum(x: Double): Double {
    var t = 1.0
    return fold(0.0) { acc, d -> (acc + d * t).let { t *= x; it } }
}
