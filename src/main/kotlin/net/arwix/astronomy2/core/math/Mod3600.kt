package net.arwix.astronomy2.core.math

/**
 * Module operation in arcseconds.
 * @param x Value in arcseconds.
 * @return module
 */
inline fun Double.mod3600() = this - 1296000.0 * Math.floor(this / 1296000.0)