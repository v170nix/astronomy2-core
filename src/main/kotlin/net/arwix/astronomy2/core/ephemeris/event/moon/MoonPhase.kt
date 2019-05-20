package net.arwix.astronomy2.core.ephemeris.event.moon

import java.lang.Math.abs
import java.lang.Math.round

enum class MoonPhase(val delta: Double) {
    NEW(0.0),
    FIRST_QUARTER(0.25),
    FULL(0.5),
    LAST_QUARTER(0.75);

    companion object {
        fun getPhase(k: Double): MoonPhase = when (abs(round((k % 1) * 100))) {
            0L -> NEW
            25L -> FIRST_QUARTER
            50L -> FULL
            75L -> LAST_QUARTER
            else -> throw IllegalArgumentException("$k is not valid")
        }

        fun getClosestPhase(k: Double): MoonPhase = when (abs(round((k % 1) * 100))) {
            in 88L..100L, in 0L..12L -> NEW
            in 13L..37L -> FIRST_QUARTER
            in 38L..62L -> FULL
            in 63L..87L -> LAST_QUARTER
            else -> throw IllegalArgumentException("$k is not valid")
        }
    }
}