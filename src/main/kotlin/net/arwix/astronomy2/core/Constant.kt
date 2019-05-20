package net.arwix.astronomy2.core

const val PI2 = 2.0 * Math.PI
const val PI4 = 4.0 * Math.PI
const val ARCSEC_2RAD = PI2 / (360.0 * 3600.0)

/** Arc minutes in one degree = 60.  */
const val MINUTES_PER_DEGREE = 60.0
/** Arc seconds in one degree = 3600.  */
const val SECONDS_PER_DEGREE = 60.0 * MINUTES_PER_DEGREE
/** Arc seconds to radians.  */
const val ARCSEC_TO_RAD = Math.PI / (180.0 * 3600.0)

/** Radians to arc seconds.  */
const val RAD_TO_ARCSEC = 1.0 / ARCSEC_TO_RAD
/** Arc seconds to degrees.  */
const val ARCSEC_TO_DEG = 1.0 / 3600.0
/** Radians to hours.  */
const val RAD_TO_HOUR = 180.0 / (15.0 * Math.PI)
/** Radians to days.  */
const val RAD_TO_DAY = RAD_TO_HOUR / 24.0
/** Radians to degrees.  */
const val RAD_TO_DEG = 180.0 / Math.PI
/** Degrees to radians.  */
const val DEG_TO_RAD = 1.0 / RAD_TO_DEG

/** Pi divided by two.  */
const val PI_OVER_TWO = Math.PI / 2.0
/** Pi divided by four.  */
const val PI_OVER_FOUR = Math.PI / 4.0
/** Pi divided by six.  */
const val PI_OVER_SIX = Math.PI / 6.0

// радиусы Земли, Солнца, Луны в км
// @JvmField val R_Earth = 6378.137
// @JvmField val R_Sun = 696000.0
// @JvmField val R_Moon = 1738.0

const val MJD_J2000 = 51544.5        // MJD на эпоху J2000.0
const val DELTA_JD_MJD = 2400000.5
const val T_J2000 = 0.0           // эпоха J2000.0
const val T_B1950 = -0.500002108   // эпоха B1950
const val JD_SECOND = 0.000011574074074074074074
const val JD_MINUTE = 0.00069444444444444444444
const val JD_HOUR = 0.041666666666666666666
const val JD_DAY = 1.0

/** Julian century conversion constant = 100 * days per year.  */
const val JULIAN_DAYS_PER_CENTURY = 36525.0
/** Our default epoch.
 * The Julian Day which represents noon on 2000-01-01.  */
const val JD_2000 = 2451545.0
/** Length of a tropical year in days for B1950.  */
const val TROPICAL_YEAR = 365.242198781


/** Hours in one day as a double.  */
const val HOURS_PER_DAY = 24.0

/** Minutes in one hour as a double.  */
const val MINUTES_PER_HOUR = 60.0

/** Seconds in one minute.  */
const val SECONDS_PER_MINUTE = 60.0

/** Seconds in one hour.  */
const val SECONDS_PER_HOUR = MINUTES_PER_HOUR * SECONDS_PER_MINUTE

/** Seconds in one day.  */
const val SECONDS_PER_DAY = HOURS_PER_DAY * SECONDS_PER_HOUR


const val SECS_IN_DAY = 86400.0 // колличество секунд в сутках

/** Earth orbit mean rate in rad/day (Gauss Gravitational constant). kGauss */
const val EARTH_MEAN_ORBIT_RATE = 0.01720209895  // гравитационная константа
const val GM_Sun = EARTH_MEAN_ORBIT_RATE * EARTH_MEAN_ORBIT_RATE  // [AU^3/d^2]

const val AU = 149597870.7    // 1ае

const val C_Light = 173.144632685         // скорость света [AU/d]