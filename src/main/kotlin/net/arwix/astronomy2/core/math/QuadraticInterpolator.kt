package net.arwix.astronomy2.core.math

import kotlin.math.abs
import kotlin.math.sqrt


sealed class QuadraticResult(val extremum: PointD) {
    class None(extremum: PointD): QuadraticResult(extremum)
    class Root(extremum: PointD, val root: Double): QuadraticResult(extremum)
    class Roots(extremum: PointD, val root1: Double, val root2: Double): QuadraticResult(extremum)
}

fun findQuadraticRoots(yMinus: Double, c: Double, yPlus: Double): QuadraticResult {
    // y=a*x^2+b*x+c
    val a = 0.5 * (yPlus + yMinus) - c
    val b = 0.5 * (yPlus - yMinus)

    val xe = -b / (2.0 * a)
    val extremum = PointD(xe, (a * xe + b) * xe + c)
    val dis = b * b - 4.0 * a * c
    if (dis >= 0) {
        val dx = 0.5 * sqrt(dis) / abs(a)
        var root1 = extremum.x - dx
        val root2 = extremum.x + dx
        var count = 0

        if (abs(root1) <= 1.0) count++
        if (abs(root2) <= 1.0) count++
        if (root1 < -1.0) root1 = root2
        if (count == 0) return QuadraticResult.None(extremum)
        return if (count > 1) QuadraticResult.Roots(extremum, root1, root2) else QuadraticResult.Root(extremum, root1)
    } else return QuadraticResult.None(extremum)
}