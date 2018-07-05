package net.arwix.astronomy2.core.math

import net.arwix.astronomy2.core.DEG_TO_RAD
import net.arwix.astronomy2.core.Degree
import net.arwix.astronomy2.core.Radian

fun Degree.normalizeDegree(): Degree {
    if (this < 0.0 && this >= -360.0) return this + 360.0
    if (this >= 360.0 && this < 720) return this - 360.0
    if (this >= 0 && this < 360.0) return this

    var d = this - 360.0 * Math.floor(this / 360.0)
    // Can't use Math.IEEEremainder here because remainder differs
    // from modulus for negative numbers.
    if (d < 0.0) d += 360.0

    return d
}

fun Degree.toRad(): Radian = this * DEG_TO_RAD
