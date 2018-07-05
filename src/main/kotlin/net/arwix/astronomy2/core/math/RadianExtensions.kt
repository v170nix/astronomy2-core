package net.arwix.astronomy2.core.math

import net.arwix.astronomy2.core.*

/**
 * Reduce an angle in radians to the range (0 - 2 Pi).
 * @return The reduced radian value.
 */
inline fun Radian.normalize(): Radian {
    if (this >= 0 && this < PI2) return this
    if (this < 0 && this >= -PI2) return this + PI2
    if (this >= PI2 && this < PI4) return this - PI2

    var d = this - PI2 * Math.floor(this / PI2)
    // Can't use Math.IEEE remainder here because remainder differs
    // from modulus for negative numbers.
    if (d < 0.0) d += PI2
    return d
}

/**
 * Radians to hours
 */
inline fun Radian.toHour(): Double = this * RAD_TO_HOUR
inline fun Radian.toDay(): Double = this * RAD_TO_DAY
inline fun Radian.toDeg(): Degree = this * RAD_TO_DEG