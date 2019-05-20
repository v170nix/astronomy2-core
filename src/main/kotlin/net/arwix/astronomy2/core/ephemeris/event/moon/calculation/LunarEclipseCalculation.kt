package net.arwix.astronomy2.core.ephemeris.event.moon.calculation

import net.arwix.astronomy2.core.*
import net.arwix.astronomy2.core.calendar.getJT
import net.arwix.astronomy2.core.vector.RectangularVector
import net.arwix.astronomy2.core.vector.SphericalVector
import net.arwix.astronomy2.core.vector.Vector
import net.arwix.astronomy2.core.vector.convert
import kotlin.math.atan
import kotlin.math.hypot


class LunarEclipseCalculation(
        val jd0: Double,
        val moonEphemeris: suspend (jt: JT) -> Vector,
        val sunEphemeris: suspend (jt: JT) -> Vector
) {

    private var recommendedTimeOffsetToNextEvent = 1.0 / SECONDS_PER_DAY
    private var recommendedTimeOffsetToNextEventAfterTotality = 1.0 / SECONDS_PER_DAY
    private val moonMeanOrbitalRate = 2.0 * Math.PI / 29.5

    lateinit var events: DoubleArray

    init {

    }

//    /**
//     * Sets the desired accuracy of the iterative method in seconds for
//     * eclipses produced by any moon besides the Moon. In case of Lunar
//     * eclipses or input values <= 0 this method will have no effect.
//     * @param s Accuracy in seconds, must be > 0. Default initial value is 1s.
//     */
//    protected abstract fun setAccuracy(s: Double)

    suspend fun init() {
        var jd = jd0
        val precission = 1.0 / SECONDS_PER_DAY
        val out = doubleArrayOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)

        val jd0 = jd
        do {
            recommendedTimeOffsetToNextEvent = 0.0
            recommendedTimeOffsetToNextEventAfterTotality = 0.0
            jd += precission
            val new_time = jd
            val event = checkEclipse(getJT(new_time - DELTA_JD_MJD))

            if (event[0] && out[0] == 0.0)
                out[0] = jd
            if (event[1] && out[1] == 0.0)
                out[1] = jd
            if (event[2] && out[2] == 0.0)
                out[2] = jd
            if (event[3] && out[3] == 0.0)
                out[3] = jd

            if (!event[3] && out[4] == 0.0 && out[3] != 0.0)
                out[4] = jd
            if (!event[2] && out[5] == 0.0 && out[2] != 0.0)
                out[5] = jd
            if (!event[1] && out[6] == 0.0 && out[1] != 0.0)
                out[6] = jd
            if (!event[0] && out[7] == 0.0 && out[0] != 0.0)
                out[7] = jd

            if (out[3] == 0.0 && out[4] == 0.0 && out[5] == 0.0 && out[6] == 0.0) {
                jd += 0.5 * recommendedTimeOffsetToNextEvent
            } else {
                jd += 0.5 * recommendedTimeOffsetToNextEventAfterTotality
            }
        } while (out[7] == 0.0)

        // It is necessary to repeat calculations for strictly partial penumbral
        // eclipses
        if (out[6] == 0.0 && out[7] != 0.0) {
            jd = jd0
            out[7] = 0.0
            out[0] = out[7]
            do {

                recommendedTimeOffsetToNextEvent = 0.0
                recommendedTimeOffsetToNextEventAfterTotality = 0.0


                jd += precission
                val new_time = jd
                val event = checkEclipse(getJT(new_time - DELTA_JD_MJD))

                if (event[0] && out[0] == 0.0)
                    out[0] = jd
                if (event[1] && out[1] == 0.0)
                    out[1] = jd
                if (event[2] && out[2] == 0.0)
                    out[2] = jd
                if (event[3] && out[3] == 0.0)
                    out[3] = jd

                if (!event[3] && out[4] == 0.0 && out[3] != 0.0)
                    out[4] = jd
                if (!event[2] && out[5] == 0.0 && out[2] != 0.0)
                    out[5] = jd
                if (!event[1] && out[6] == 0.0 && out[1] != 0.0)
                    out[6] = jd
                if (!event[0] && out[7] == 0.0 && out[0] != 0.0)
                    out[7] = jd

                if (out[0] == 0.0) {
                    jd += 0.5 * recommendedTimeOffsetToNextEvent
                } else {
                    jd += 0.5 * recommendedTimeOffsetToNextEventAfterTotality
                }
            } while (out[7] == 0.0)
        }

        this.events = out
    }

    protected suspend fun checkEclipse(jt: JT): BooleanArray {

        val ephem_moon = convert<SphericalVector>(moonEphemeris(jt))
        val ephem_sun = convert<SphericalVector>(sunEphemeris(jt))

        var inside_shadow = false
        var totality = false
        var inside_penumbra = false
        var totality_penumbra = false


        val ephem = SphericalVector(ephem_sun.phi + Math.PI, -ephem_sun.theta, ephem_moon.r)

        // Get shadow cone direction
//        ephem.rightAscension += Math.PI
//        ephem.declination = -ephem.declination
//        ephem.distance = ephem_moon.distance

        val earthEquatorialRadius = 6378.1366
        val earthPolarRadius = 6356.7519
        val sunEquatorialRadius = 696000.0
        val moonEquatorialRadius = 1737.4

        val ephem_sun_angularRadius = getangularRadius(ephem_sun, sunEquatorialRadius / AU)
        val ephem_moon_angularRadius = getangularRadius(ephem_moon, moonEquatorialRadius / AU)


        // The main calculation is to position the center of the Earth shadow
        // cone. We consider this cone to be indeed an oval with a size
        // slightly larger than the Earth's equatorial and polar radius.
        // This excess can be understood taking into account Earth surface
        // elevation and opacity of the atmosphere. Values fitted to lunar
        // eclipses in 2007. Note AA supplement uses 1.02 in page 429, but more
        // precision is required (and a separation between polar and equatorial
        // axis) to get an accuracy of 1 second or better.
        // See http://eclipse.gsfc.nasa.gov/LEcat5/shadow.html for a discussion
        val val_eq = 1.0131
        val val_pol = 1.015
        val EarthShadowConeSize = earthEquatorialRadius / (AU * Math
                .tan(ephem_sun_angularRadius))
        val shadow_loc = SphericalVector(ephem.phi, ephem.theta, 1.0)
        val ang_radius_max = Math.atan2(earthEquatorialRadius / AU, ephem_moon.r) * (val_eq - ephem_moon.r / EarthShadowConeSize)
        val ang_radius_min = Math.atan2(earthPolarRadius / AU, ephem_moon.r) * (val_pol - ephem_moon.r / EarthShadowConeSize)
        val penumbra_ang_radius = 2.0 * ephem_sun_angularRadius
        val penumbra_scale_max = ang_radius_max + penumbra_ang_radius
        val penumbra_scale_min = ang_radius_min + penumbra_ang_radius

        val moon_loc = SphericalVector(ephem_moon.phi, ephem_moon.theta, 1.0)
        val dist = getAngularDistance(moon_loc, shadow_loc)
        val pa = 3.0 * PI_OVER_TWO - getPositionAngle(moon_loc, shadow_loc)

        var s_x = Math.sin(pa) / ang_radius_max
        var s_y = Math.cos(pa) / ang_radius_min
        var s_r = 1.0 / hypot(s_x, s_y)

        if (dist <= s_r + ephem_moon_angularRadius) {
            inside_shadow = true
        } else {
            recommendedTimeOffsetToNextEvent = 0.25 * Math.abs(dist - (s_r + ephem_moon_angularRadius)) / moonMeanOrbitalRate
        }
        if (dist <= s_r - ephem_moon_angularRadius) {
            totality = true

            recommendedTimeOffsetToNextEvent = 0.25 * Math.abs(dist - (s_r - ephem_moon_angularRadius)) / moonMeanOrbitalRate
            recommendedTimeOffsetToNextEventAfterTotality = recommendedTimeOffsetToNextEvent

        } else {
            if (inside_shadow) {
                recommendedTimeOffsetToNextEvent = 0.25 * Math.abs(dist - (s_r - ephem_moon_angularRadius)) / moonMeanOrbitalRate
                recommendedTimeOffsetToNextEventAfterTotality = 0.25 * Math.abs(dist - (s_r + ephem_moon_angularRadius)) / moonMeanOrbitalRate
            }
        }

        s_x = Math.sin(pa) / penumbra_scale_max
        s_y = Math.cos(pa) / penumbra_scale_min
        s_r = 1.0 / hypot(s_x, s_y)

        if (dist <= s_r + ephem_moon_angularRadius) {
            inside_penumbra = true
            if (!inside_shadow)
                recommendedTimeOffsetToNextEventAfterTotality = 0.25 * Math.abs(dist - (s_r + ephem_moon_angularRadius)) / moonMeanOrbitalRate
        } else {
            recommendedTimeOffsetToNextEvent = 0.125 * Math.abs(dist - (s_r + ephem_moon_angularRadius)) / moonMeanOrbitalRate
            recommendedTimeOffsetToNextEventAfterTotality = 0.0

        }
        if (dist <= s_r - ephem_moon_angularRadius) {
            totality_penumbra = true
            if (!inside_shadow)
                recommendedTimeOffsetToNextEventAfterTotality = 0.25 * Math.abs(dist - (s_r - ephem_moon_angularRadius)) / moonMeanOrbitalRate
        } else {
            if (inside_penumbra)
                recommendedTimeOffsetToNextEvent = 0.25 * Math.abs(dist - (s_r - ephem_moon_angularRadius)) / moonMeanOrbitalRate
            // Disabled to properly account for eclipses whose penumbra totality
            // ends before shadow egress
            // if (inside_penumbra) recommended_time_offset_to_next_event_after_totality = Math.abs(dist - (s_r + ephem_moon.angularRadius)) / LunarEclipse.moonMeanOrbitalRate;
        }

        return booleanArrayOf(inside_penumbra, totality_penumbra, inside_shadow, totality)
    }

    protected fun getEclipseMaximum() {

    }

    protected fun getEclipseType() {

    }

    /**
     * Obtain angular distance between two spherical coordinates.
     *
     * @param loc1 Location object.
     * @param loc2 Location object.
     * @return The distance in radians, from 0 to PI.
     */
    fun getAngularDistance(loc1: SphericalVector, loc2: SphericalVector): Double {
        val xyz1 = convert<RectangularVector>(SphericalVector(loc1.phi, loc1.theta, 1.0))
        val xyz2 = convert<RectangularVector>(SphericalVector(loc2.phi, loc2.theta, 1.0))

//        (xyz1 - xyz2).normalize()

        val dx = xyz1[0] - xyz2[0]
        val dy = xyz1[1] - xyz2[1]
        val dz = xyz1[2] - xyz2[2]

        val r2 = dx * dx + dy * dy + dz * dz

        return Math.acos(1.0 - r2 * 0.5)
        /*
 		// Haversine formula
 		double dLat = loc1.lat - loc2.lat;
		double dLon = loc1.lon - loc2.lon;
		double a = sin(dLat/2) * sin(dLat/2) + cos(loc1.lat) * cos(loc2.lat) * sin(dLon/2) * sin(dLon/2);
		return 2.0 * atan2(Math.sqrt(a), sqrt(1.0-a));
*/
    }

    /**
     * Obtain exact position angle between two spherical coordinates. Performance will be poor.
     *
     * @param loc1 Location object.
     * @param loc2 Location object.
     * @return The position angle in radians.
     */
    fun getPositionAngle(loc1: SphericalVector, loc2: SphericalVector): Double {
        val al = loc1.phi
        val ap = loc1.theta
        val bl = loc2.phi
        val bp = loc2.theta
        val dl = bl - al
        val cbp = Math.cos(bp)
        val y = Math.sin(dl) * cbp
        val x = Math.sin(bp) * Math.cos(ap) - cbp * Math.sin(ap) * Math.cos(dl)
        var pa = 0.0
        if (x != 0.0 || y != 0.0) pa = -Math.atan2(y, x)
        return pa
    }

    fun getangularRadius(vector: SphericalVector, equatorialRadius: Double) = atan(equatorialRadius / vector.r)

}