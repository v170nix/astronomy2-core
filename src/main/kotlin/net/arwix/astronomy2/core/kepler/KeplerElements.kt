package net.arwix.astronomy2.core.kepler

import net.arwix.astronomy2.core.JT
import net.arwix.astronomy2.core.Radian

interface KeplerElements {
    fun getSemiMajorAxis(jT: JT): Double //a
    fun getEccentricity(jT: JT): Radian // e
    fun getInclination(jT: JT): Radian //i

    fun getLongitude(jT: JT): Radian //L
    fun getPerihelionLongitude(jT: JT): Radian //w
    fun getAscendingNodeLongitude(jT: JT): Radian // O
    fun getMeanAnomaly(jT: JT): Radian = getLongitude(jT) - getPerihelionLongitude(jT)
}