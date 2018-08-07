package net.arwix.astronomy2.core.calendar

import net.arwix.astronomy2.ephemeris.vsop87a.ID_VSOP87_JUPITER
import net.arwix.astronomy2.ephemeris.vsop87a.createSuspendedVsop87ACoordinates
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class TimeFunctionsTest {

    @Test
    fun getGMST() {
    }

    @Test
    fun getJT() {
    }

    @Test
    fun `MJD`() {
        assertEquals(58421.0,
                getMJD(2018, 10, 30, 0, 0, 0, 0))
        assertEquals(131444.30777777778,
                getMJD(2218, 10, 5, 7, 23, 12, 0))
        assertEquals(21629.30638888889,
                getMJD(1918, 2, 5, 7, 21, 12, 0))
        assertEquals(-416661.6936111111,
                getMJD(718, 2, 5, 7, 21, 12, 0))
        assertEquals(-416657.6936111111,
                getMJD(718, 2, 5, 7, 21, 12, 0, true))
    }

    @Test
    fun `MJD time in mills`() {
        assertEquals(58303.697233796294,getMJD(1530722641000))
    }

    @Test
    fun getDeltaT() {
        assertEquals(17190.0, getDeltaT(-500, 1), 430.0)
        assertEquals(10580.0, getDeltaT(0, 1), 260.0)
        assertEquals(1570.0, getDeltaT(1000, 1), 55.0)
        assertEquals(120.0, getDeltaT(1600, 1), 20.0)
        assertEquals(14.0, getDeltaT(1800, 1), 1.0)
        assertEquals(29.0, getDeltaT(1950, 1), 0.1)
        assertEquals(63.8, getDeltaT(2000, 1), 0.1)
        assertEquals(66.07, getDeltaT(2010, 1), 1.0)
    }

    @Test
    fun getDeltaT1() {
    }

    @Test
    fun fromMJDToCalendar() {
    }
}