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

typealias IdBodyKeplerElements = Int

const val ID_MERCURY_KEPLER_ELEMENTS = 1
const val ID_VENUS_KEPLER_ELEMENTS = 2
const val ID_EARTH_KEPLER_ELEMENTS = 3
const val iD_EARTH_MOON_BARYCENTER_KEPLER_ELEMENTS = 31
const val ID_MARS_KEPLER_ELEMENTS = 4
const val ID_JUPITER_KEPLER_ELEMENTS = 5
const val ID_SATURN_KEPLER_ELEMENTS = 6
const val ID_URANUS_KEPLER_ELEMENTS = 7
const val ID_NEPTUNE_KEPLER_ELEMENTS = 8
const val ID_PLUTO_KEPLER_ELEMENTS = 900

