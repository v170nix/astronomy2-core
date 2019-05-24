package net.arwix.astronomy2.core.calendar

import net.arwix.astronomy2.core.*
import java.lang.Math.pow
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

/**
 * Среднее гринвичское звездное время
 * Greenwich Mean Sidereal Time
 * @param mJD Время в форме модифицированной юлианской даты
 * @return GMST в радианах
 */
fun getGMST(mJD: MJD): Radian {
    val mJD0 = Math.floor(mJD)
    val uT = SECS_IN_DAY * (mJD - mJD0) // [сек]
    val jT0 = (mJD0 - 51544.5) / 36525.0
    val jT = (mJD - 51544.5) / 36525.0
    val gmst = 24110.54841 + 8640184.812866 * jT0 + 1.0027379093 * uT + (0.093104 - 0.0000062 * jT) * jT * jT // [сек]
//    var gmst0 = 6.697374558 + 0.06570982441908 * jT0 * 36525.0 + 1.00273790935 * uT / 60.0 / 60.0 + 0.000026 * jT * jT
//    gmst0 = gmst0 * 60.0 * 60.0
//    println("gmst=$gmst")
//    println("gmst0=$gmst0")
    return PI2 / SECS_IN_DAY * (gmst % SECS_IN_DAY) // [рад]
}

/**
 * Вычисление юлианского столетия на эпоху J2000
 * @param mJD юлианская дата
 * @return JT
 */
fun getJT(mJD: MJD): JT = (mJD - MJD_J2000) / 36525.0

/**
 * @param month 1-12
 */
fun getMJD(year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int, millisecond: Int, isJulianDate: Boolean = false): MJD {
    var y = year
    val m = if (month <= 2) {
        --y; month + 12
    } else month
//if (10000L * y + 100L * m + day <= 15821004L)
    val b: Long = if (isJulianDate) -2L + (y + 4716L) / 4 - 1179L else (y / 400L - y / 100 + y / 4)

    val mJDN = 365 * y - 679004L + b + (30.6001 * (m + 1)).toInt() + day
    val mJDF = (abs(hour) + abs(minute) / 60.0 + abs(second + millisecond / 1000.0) / 3600.0) / 24.0

    return mJDN + mJDF
}

fun isJulianDate(year: Int, month: Int, day: Int): Boolean {
    var y = year
    val m = if (month <= 2) {
        --y; month + 12
    } else month
    return 10000L * y + 100L * m + day <= 15821004L
}

fun getMJD(milliseconds: Long): MJD {
    return milliseconds / 1000.0 * JD_SECOND + 40587.0
}

fun getDeltaT(milliseconds: Long): Double {
    val y = milliseconds / 1000.0 / SECS_IN_DAY / TROPICAL_YEAR + 1970L
    return getDeltaT(y.toInt(), ((y - y.toInt()) * 12).toInt() + 1)
}


/**
 * @param month 1-12
 * @return deltaT
 */
fun getDeltaT(year: Int, month: Int, unit: TimeUnit): Double = getDeltaT(year, month) * when (unit) {
    TimeUnit.NANOSECONDS -> 1000000000.0
    TimeUnit.MICROSECONDS -> 1000000.0
    TimeUnit.MILLISECONDS -> 1000.0
    TimeUnit.SECONDS -> 1.0
    TimeUnit.MINUTES -> 1.0 / 60.0
    TimeUnit.HOURS -> 1.0 / 3600.0
    TimeUnit.DAYS -> 1.0 / 86400.0
}

/**
 * @param month 1-12
 * @return deltaT is sec
 */
fun getDeltaT(year: Int, month: Int): Double {
    val y = year + (month - 0.5) / 12.0

    return when (year) {
        in 2005..2050 -> {
            val t = y - 2000.0
            62.92 + (0.32217 + 0.005589 * t) * t
        }
        in -1999..-500 -> -20.0 + 32.0 * ((year - 1820.0) / 100.0 * (year - 1820.0) / 100.0)
        in -500..500 -> {
            val u = y / 100.0
            10583.6 + (-1014.41 + (33.78311 + (-5.952053 + (-0.1798452 + (0.022174192 + 0.0090316521 * u) * u) * u) * u) * u) * u
        }
        in 500..1600 -> {
            val u = (y - 1000.0) / 100.0
            1574.2 + (-556.01 + (71.23472 + (0.319781 + (-0.8503463 + (-0.005050998 + 0.0083572073 * u) * u) * u) * u) * u) * u
        }
        in 1600..1700 -> {
            val u = y - 1600.0
            120.0 + (-0.9808 + (-0.01532 + u / 7129.0) * u) * u
        }
        in 1700..1800 -> {
            val u = y - 1700.0
            8.83 + (0.1603 + (-0.0059285 + (0.00013336 - u / 1174000.0) * u) * u) * u
        }
        in 1800..1860 -> {
            val u = y - 1800.0
            13.72 + (-0.332447 + (0.0068612 + (0.0041116 + (-0.00037436 + (0.0000121272 + (-0.0000001699 + 0.000000000875 * u) * u) * u) * u) * u) * u) * u
        }
        in 1860..1900 -> {
            val u = y - 1860.0
            7.62 + (0.5737 + (-0.251754 + (0.01680668 + (-0.0004473624 + u / 233174.0) * u) * u) * u) * u
        }
        in 1900..1920 -> {
            val u = y - 1900.0
            -2.79 + (1.494119 + (-0.0598939 + (0.0061966 - 0.000197 * u) * u) * u) * u
        }
        in 1920..1941 -> {
            val u = y - 1920.0
            21.20 + (0.84493 + (-0.076100 + 0.0020936 * u) * u) * u
        }
        in 1941..1961 -> {
            val u = y - 1950.0
            29.07 + (0.407 + (-1 / 233.0 + u / 2547.0) * u) * u
        }
        in 1961..1986 -> {
            val u = y - 1975.0
            45.45 + (1.067 + (-1 / 260.0 + -u / 718.0) * u) * u
        }
        in 1086..2005 -> {
            val u = y - 2000.0
            63.86 + (0.3345 + (-0.060374 + (0.0017275 + (0.000651814 + 0.00002373599 * u) * u) * u) * u) * u
        }
        in 2050..2150 -> -20.0 + 32.0 * pow((y - 1820.0) / 100.0, 2.0) - 0.5628 * (2150.0 - y)
        in 2150..3000 -> -20.0 + 32.0 * pow((y - 1820.0) / 100.0, 2.0)
        else -> -20.0 + 32.0 * pow((y - 1820.0) / 100.0, 2.0)
    }
}

fun fromMJDToCalendar(mjd: MJD, calendar: Calendar, applyDeltaT: Boolean = false) {

    val a = (mjd + 2400001.0).toLong()
    val c = if (a < 2299161) a + 1524L else {
        val b = ((a - 1867216.25) / 36524.25).toLong()
        a + b - (b / 4) + 1525
    }

    val d = ((c - 122.1) / 365.25).toLong()
    val e = 365 * d + d / 4
    val f = ((c - e) / 30.6001).toLong()

    val day = c - e - (30.6001 * f).toLong()
    val month = f - 1 - 12 * (f / 14)
    val year = d - 4715 - ((7 + month) / 10)
    val hours = mjd - Math.floor(mjd) + calendar.zoneOffset() / SECS_IN_DAY / 1000.0
    calendar.set(year.toInt(), month.toInt() - 1, day.toInt())
    calendar.setHours(hours * 24.0)
    val daylight = if (calendar.timeZone.inDaylightTime(calendar.time)) calendar.timeZone.dstSavings else 0
    if (daylight != 0) calendar.millisecond(calendar.millisecond() + daylight)
    if (applyDeltaT) calendar.second(calendar.second() - calendar.getDeltaT(TimeUnit.SECONDS).toInt())
}