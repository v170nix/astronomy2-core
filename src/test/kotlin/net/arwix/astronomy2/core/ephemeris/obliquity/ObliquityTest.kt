package net.arwix.astronomy2.core.ephemeris.obliquity

import net.arwix.astronomy2.core.RAD_TO_DEG
import net.arwix.astronomy2.core.Radian
import org.junit.jupiter.api.*

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.*
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ObliquityTest {

    @ParameterizedTest
    @MethodSource("getWilliamsData")
    fun `williams 1994`(data: Data) {
        assertEquals(
                radToDegString(getObliquity(ID_OBLIQUITY_WILLIAMS_1994, data.jT)),
                radToDegString(data.obliquity))
    }

    @ParameterizedTest
    @MethodSource("getSimonData")
    fun `simon 1994`(data: Data) {
        assertEquals(
                radToDegString(getObliquity(ID_OBLIQUITY_SIMON_1994, data.jT)),
                radToDegString(data.obliquity))
    }

    @ParameterizedTest
    @MethodSource("getLaskarData")
    fun `laskar 1996`(data: Data) {
        assertEquals(
                radToDegString(getObliquity(ID_OBLIQUITY_LASKAR_1996, data.jT)),
                radToDegString(data.obliquity))
    }

    @ParameterizedTest
    @MethodSource("getIAU1976Data")
    fun `IAU 1976`(data: Data) {
        assertEquals(
                radToDegString(getObliquity(ID_OBLIQUITY_IAU_1976, data.jT)),
                radToDegString(data.obliquity))
    }

    @ParameterizedTest
    @MethodSource("getIAU2006Data")
    fun `IAU 2006`(data: Data) {
        assertEquals(
                radToDegString(getObliquity(ID_OBLIQUITY_IAU_2006, data.jT)),
                radToDegString(data.obliquity))
    }

    @ParameterizedTest
    @MethodSource("getVONDRAK2011Data")
    fun `VONDRAK 2011`(data: Data) {
        assertEquals(
                radToDegString(getObliquity(ID_OBLIQUITY_VONDRAK_2011, data.jT)),
                radToDegString(data.obliquity))
    }

    fun getWilliamsData() = listOf(
            Data(0.0, 0.40909260143931053),
            Data(0.5, 0.40897907371566994),
            Data(-0.5, 0.4092061287384277),
            Data(10.0, 0.40683159588417683)
    )

    fun getSimonData() = listOf(
            Data(0.0, 0.4090926296894037),
            Data(0.5, 0.4089791618438889),
            Data(-0.5, 0.4092060971661488),
            Data(10.0, 0.4068328322899633)
    )

    fun getLaskarData() = listOf(
            Data(0.0, 0.40909280422232897),
            Data(0.5, 0.4089793218435321),
            Data(-0.5, 0.4092062717679479),
            Data(10.0, 0.40683300561085434)
    )

    fun getIAU1976Data() = listOf(
            Data(0.0, 0.40909280422232897),
            Data(0.5, 0.4089793218435321),
            Data(-0.5, 0.4092062851709255),
            Data(10.0, 0.40683165260618137)
    )

    fun getIAU2006Data() = listOf(
            Data(0.0, 0.40909260143931053),
            Data(0.5, 0.40897907371566994),
            Data(-0.5, 0.4092061287384277),
            Data(10.0, 0.40683146498328676)
    )

    fun getVONDRAK2011Data() = listOf(
            Data(0.0, 0.40909260143931053),
            Data(0.5, 0.40897907371566994),
            Data(-0.5, 0.4092061287384277),
            Data(10.0, 0.4068314158060073)
    )

    internal data class Data(val jT: Double, val obliquity: Double) {
        override fun toString(): String {
            return "f($jT)=${radToDegString(obliquity)}"
        }
    }

    internal companion object {
        fun radToDegString(radian: Radian): String {
            val g = (RAD_TO_DEG * radian).toInt()
            val mm = (RAD_TO_DEG * radian - g) * 60.0
            val m = mm.toInt()
            val s = (mm - m) * 60.0
            return String.format(Locale.ENGLISH, "%1$02d° %2$02d′ %3$.2f″", g, Math.abs(m), Math.abs(s))
        }
    }
}