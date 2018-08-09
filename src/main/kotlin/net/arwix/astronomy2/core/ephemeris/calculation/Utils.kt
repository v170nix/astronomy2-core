package net.arwix.astronomy2.core.ephemeris.calculation

import net.arwix.astronomy2.core.*
import net.arwix.astronomy2.core.calendar.getGMST
import net.arwix.astronomy2.core.vector.SphericalVector
import net.arwix.astronomy2.core.vector.Vector
import net.arwix.astronomy2.core.vector.convert
import kotlin.math.cos
import kotlin.math.sin

/**
 * Синус высоты объекта над горизонтом
 * @param MJD         на расчетную дату
 * @param deltaT      в долях дня
 * @param longitude   долгота в радианах
 * @param cosLatitude косинус широты
 * @param sinLatitude синус широты
 * @return cинус высоты Солнца или Луны в момент искомого события
 */
internal suspend inline fun getSinAltitude(MJD: Double,
                                   deltaT: Double,
                                   longitude: Double,
                                   cosLatitude: Double,
                                   sinLatitude: Double,
                                   @Geocentric
                                   @Equatorial
                                   @Apparent
                                   noinline findCoordinates: suspend (jT: JT) -> Vector): Double {
    val jt = (MJD - MJD_J2000 - deltaT) / 36525.0
    val position = findCoordinates(jt)
    val p: SphericalVector = convert(position)
    // часовой угол
    val tau = getGMST(MJD) + longitude - p.phi
    return sinLatitude * sin(p.theta) + cosLatitude * cos(p.theta) * cos(tau)
}