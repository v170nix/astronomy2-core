package net.arwix.astronomy2.core.calendar

import net.arwix.astronomy2.core.JT
import net.arwix.astronomy2.core.MJD
import net.arwix.astronomy2.core.MJD_1970
import net.arwix.astronomy2.core.SECS_IN_DAY
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit

fun Calendar.getMJD(isJulianDate: Boolean = false): MJD {
    val daylight = if (timeZone.inDaylightTime(time)) timeZone.dstSavings else 0
    return getMJD(year(), month() + 1, dayOfMonth(), hourOfDay(), minute(), second(), millisecond(), isJulianDate) -
            (zoneOffset() + daylight) / SECS_IN_DAY / 1000
}

fun Instant.toMJD(): MJD = epochSecond.toDouble() / SECS_IN_DAY + MJD_1970

fun Instant.toJT(applyDeltaT: Boolean = false): JT {
    return getJT(toMJD()) + if (applyDeltaT) getDeltaT(toEpochMilli()) / SECS_IN_DAY else 0.0
}

fun Instant.roundToMinute(): Instant {
    val s = epochSecond
    val m = s / 60L
    val ds = s - m * 60L
    val round = if (ds > 30L) s + 60L - ds else s - ds
    return Instant.ofEpochSecond(round)
}

fun Instant.getDeltaT(unit: TimeUnit) = getDeltaT(toEpochMilli()) * when (unit) {
    TimeUnit.NANOSECONDS -> 1000000000.0
    TimeUnit.MICROSECONDS -> 1000000.0
    TimeUnit.MILLISECONDS -> 1000.0
    TimeUnit.SECONDS -> 1.0
    TimeUnit.MINUTES -> 1.0 / 60.0
    TimeUnit.HOURS -> 1.0 / 3600.0
    TimeUnit.DAYS -> 1.0 / 86400.0
}


//fun ZonedDateTime.getMJD(isJulianDate: Boolean = false): MJD {
//    val instant = this.toInstant()
//    val daylight = zone.rules.getDaylightSavings(instant).toMillis()
//    return getMJD(year, monthValue, dayOfMonth, hour, minute, second, 0, isJulianDate) -
//            (zone.rules.getOffset(instant).totalSeconds * 1000 + daylight) / SECS_IN_DAY / 1000
//}

fun Calendar.getDeltaT(unit: TimeUnit): Double =
    getDeltaT(year(), month() + 1) * when (unit) {
        TimeUnit.NANOSECONDS -> 1000000000.0
        TimeUnit.MICROSECONDS -> 1000000.0
        TimeUnit.MILLISECONDS -> 1000.0
        TimeUnit.SECONDS -> 1.0
        TimeUnit.MINUTES -> 1.0 / 60.0
        TimeUnit.HOURS -> 1.0 / 3600.0
        TimeUnit.DAYS -> 1.0 / 86400.0
    }
//
//fun ZonedDateTime.getDeltaT(unit: TimeUnit): Double =
//    getDeltaT(year, month.value) * when (unit) {
//        TimeUnit.NANOSECONDS -> 1000000000.0
//        TimeUnit.MICROSECONDS -> 1000000.0
//        TimeUnit.MILLISECONDS -> 1000.0
//        TimeUnit.SECONDS -> 1.0
//        TimeUnit.MINUTES -> 1.0 / 60.0
//        TimeUnit.HOURS -> 1.0 / 3600.0
//        TimeUnit.DAYS -> 1.0 / 86400.0
//    }

fun Calendar.copy(): Calendar = Calendar.getInstance(this.timeZone).also {
    it.timeInMillis = this.timeInMillis
}

fun Calendar.applyMJD(mjd: MJD, applyDeltaT: Boolean = false): Calendar {
    fromMJDToCalendar(mjd, this, applyDeltaT)
    return this
}

fun Calendar.getJT(applyDeltaT: Boolean = false): JT =
    getJT(getMJD() + if (applyDeltaT) getDeltaT(TimeUnit.DAYS) else 0.0)

fun MJD.toCalendar(applyDeltaT: Boolean = false, timeZone: TimeZone = TimeZone.getTimeZone("UTC")): Calendar =
    Calendar.getInstance(timeZone).apply {
        fromMJDToCalendar(this@toCalendar, this, applyDeltaT)
    }

fun Calendar.year() = get(Calendar.YEAR)
fun Calendar.month() = get(Calendar.MONTH)
fun Calendar.dayOfMonth() = get(Calendar.DAY_OF_MONTH)
fun Calendar.dayOfYear() = get(Calendar.DAY_OF_YEAR)
fun Calendar.hourOfDay() = get(Calendar.HOUR_OF_DAY)
fun Calendar.minute() = get(Calendar.MINUTE)
fun Calendar.second() = get(Calendar.SECOND)
fun Calendar.millisecond() = get(Calendar.MILLISECOND)
fun Calendar.zoneOffset() = get(Calendar.ZONE_OFFSET)

fun Calendar.year(year: Int): Calendar {
    set(Calendar.YEAR, year)
    return this
}

fun Calendar.month(month: Int): Calendar {
    this.set(Calendar.MONTH, month)
    return this
}

fun Calendar.dayOfMonth(day: Int): Calendar {
    this.set(Calendar.DAY_OF_MONTH, day)
    return this
}

fun Calendar.dayOfYear(day: Int): Calendar {
    this.set(Calendar.DAY_OF_YEAR, day)
    return this
}

fun Calendar.hourOfDay(hour: Int): Calendar {
    this.set(Calendar.HOUR_OF_DAY, hour)
    return this
}

fun Calendar.minute(minute: Int): Calendar {
    this.set(Calendar.MINUTE, minute)
    return this
}

fun Calendar.second(second: Int): Calendar {
    this.set(Calendar.SECOND, second)
    return this
}

fun Calendar.millisecond(millisecond: Int): Calendar {
    this.set(Calendar.MILLISECOND, millisecond)
    return this
}

fun Calendar.resetTime(): Calendar {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
    return this
}

//fun ZonedDateTime.resetTime(): ZonedDateTime {
//    return with { LocalTime.of(0, 0) }
//}

fun Calendar.getHours(): Double {
    return hourOfDay() + (minute() + second() / 60.0) / 60.0
}

fun Calendar.setHours(hours: Double): Calendar {
    val hour = hours.toInt()
    val minutes = (hours - hour) * 60.0
    val minute = minutes.toInt()
    val seconds = (minutes - minute) * 60.0
    val second = seconds.toInt()
    val millisecond = ((seconds - second) * 1000.0).toInt()

    set(Calendar.HOUR_OF_DAY, hour)
    set(Calendar.MINUTE, minute)
    set(Calendar.SECOND, second)
    set(Calendar.MILLISECOND, millisecond)
    return this
}

fun Calendar.addHours(hours: Double): Calendar {
    add(Calendar.MILLISECOND, (hours * 60.0 * 60.0 * 1000.0).toInt())
    return this
}


