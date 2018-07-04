package net.arwix.astronomy2.core.ephemeris.precession

import net.arwix.astronomy2.core.JT
import net.arwix.astronomy2.core.vector.Matrix
import net.arwix.astronomy2.core.vector.Vector

interface PrecessionElements {

    val id: IdPrecession
    val jT: JT
    val isEcliptic: Boolean
    val fromJ2000Matrix: Matrix
    val toJ2000Matrix: Matrix

    fun transformFromJ2000(vector: Vector): Vector = fromJ2000Matrix * vector
    fun transformToJ2000(vector: Vector): Vector = toJ2000Matrix * vector
}