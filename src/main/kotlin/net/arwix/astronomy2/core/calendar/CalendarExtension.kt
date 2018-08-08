package net.arwix.astronomy2.core.calendar

import net.arwix.astronomy2.core.JT
import net.arwix.astronomy2.core.MJD
import net.arwix.astronomy2.core.SECS_IN_DAY
import sun.util.calendar.JulianCalendar
import java.util.*
import java.util.concurrent.TimeUnit

fun Calendar.getMJD(isJulianDate: Boolean = false): MJD {
    val daylight = if (timeZone.inDaylightTime(time)) timeZone.dstSavings else 0
    return getMJD(year(), month() + 1, dayOfMonth(), hourOfDay(), minute(), second(), millisecond(), isJulianDate) -
            (zoneOffset() + daylight) / SECS_IN_DAY / 1000
}

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

fun Calendar.copy(): Calendar = Calendar.getInstance(timeZone).also {
    it.timeInMillis = this.timeInMillis
}

fun Calendar.getJT(applyDeltaT: Boolean = false): JT =
        getJT(getMJD() + if (applyDeltaT) getDeltaT(TimeUnit.DAYS) else 0.0)

fun MJD.toCalendar(applyDeltaT: Boolean = false) = fromMJDToCalendar(this, Calendar.getInstance(), applyDeltaT)

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


