package net.arwix.astronomy2.core.ephemeris.calculation

import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.coroutineScope
import kotlinx.coroutines.experimental.yield
import net.arwix.astronomy2.core.*
import net.arwix.astronomy2.core.calendar.*
import net.arwix.astronomy2.core.math.QuadraticResult
import net.arwix.astronomy2.core.math.findQuadraticRoots
import net.arwix.astronomy2.core.vector.Vector
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.cos
import kotlin.math.sin

sealed class RiseSetCalculationResult {
    data class Rise(val calendar: Calendar) : RiseSetCalculationResult()
    data class Set(val calendar: Calendar) : RiseSetCalculationResult()
    data class RiseSet(val rise: Rise, val set: Set) : RiseSetCalculationResult()
    data class None(val isAbove: Boolean) : RiseSetCalculationResult()
}

enum class ObjectType constructor(angle: Double) {
    SUN(-0.833), MOON(+0.133), DOT(-0.5667);

    internal val sinRefractionAngle = sin(angle * DEG_TO_RAD)
}

suspend fun findRiseSet(
        objectType: ObjectType,
        calendar: Calendar,
        latitude: Radian,
        longitude: Radian,
        @Geocentric
        @Equatorial
        @Apparent
        findCoordinates: suspend (jT: JT) -> Vector
): RiseSetCalculationResult = coroutineScope {

    val innerCalendar = calendar.copy().resetTime()
    val deltaT = innerCalendar.getDeltaT(TimeUnit.DAYS)
    val MJD0 = innerCalendar.getMJD()

    val cosLatitude = cos(latitude)
    val sinLatitude = sin(latitude)

    var hour = 1.0
    var y0: Double
    var yPlus: Double

    var yMinus = getSinAltitude(
            MJD0 + (hour - 1.0) / 24.0,
            deltaT,
            longitude,
            cosLatitude, sinLatitude,
            findCoordinates) - objectType.sinRefractionAngle

    var rise: RiseSetCalculationResult.Rise? = null
    var set: RiseSetCalculationResult.Set? = null

    do {
        val defferedY0 = async {
            getSinAltitude(MJD0 + hour / 24.0,
                    deltaT,
                    longitude,
                    cosLatitude,
                    sinLatitude,
                    findCoordinates) - objectType.sinRefractionAngle
        }

        val defferedY1 = async {
            getSinAltitude(MJD0 + (hour + 1.0) / 24.0,
                    deltaT,
                    longitude,
                    cosLatitude,
                    sinLatitude,
                    findCoordinates) - objectType.sinRefractionAngle
        }

        y0 = defferedY0.await()
        yield()
        yPlus = defferedY1.await()
        yield()
        // определние параболы по трем значением y_minus,y_0,y_plus
        val quadraticResult = findQuadraticRoots(yMinus, y0, yPlus)

        when (quadraticResult) {
            is QuadraticResult.Root -> {
                if (yMinus < 0.0)
                    rise = RiseSetCalculationResult.Rise(innerCalendar.copy().setHours(hour + quadraticResult.root))
                else
                    set = RiseSetCalculationResult.Set(innerCalendar.copy().setHours(hour + quadraticResult.root))
            }
            is QuadraticResult.Roots -> {
                val (LT_Rise, LT_Set) = if (quadraticResult.extremum.y < 0.0)
                    Pair(hour + quadraticResult.root2, hour + quadraticResult.root1) else
                    Pair(hour + quadraticResult.root1, hour + quadraticResult.root2)

                return@coroutineScope RiseSetCalculationResult.RiseSet(
                        RiseSetCalculationResult.Rise(innerCalendar.copy().setHours(LT_Rise)),
                        RiseSetCalculationResult.Set(innerCalendar.copy().setHours(LT_Set))
                )
            }
        }
        yMinus = yPlus // подготовка к обработке следующего интервала
        hour += 2.0
    } while (!((hour == 25.0) || (rise != null && set != null)))

    if (rise != null && set != null) return@coroutineScope RiseSetCalculationResult.RiseSet(rise, set)
    if (rise != null) return@coroutineScope rise
    if (set != null) return@coroutineScope set
    return@coroutineScope RiseSetCalculationResult.None(yMinus > 0.0)
}
