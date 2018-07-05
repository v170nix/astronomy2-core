package net.arwix.astronomy2.core.ephemeris.nutation

import net.arwix.astronomy2.core.RAD_TO_DEG
import net.arwix.astronomy2.core.Radian

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.*
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class NutationTest {

    @ParameterizedTest
    @MethodSource("getIAU1980Data")
    fun `IAU1980`(data: Data) {
        val angles = getNutationAngles(ID_NUTATION_IAU_1980, data.jT)
        assertEquals(radToDegString(data.nutationAngles.deltaLongitude),
                radToDegString(angles.deltaLongitude))
        assertEquals(radToDegString(data.nutationAngles.deltaObliquity),
                radToDegString(angles.deltaObliquity))
    }

    @ParameterizedTest
    @MethodSource("getIAU2000Data")
    fun `IAU2000`(data: Data) {
        val angles = getNutationAngles(ID_NUTATION_IAU_2000, data.jT)
        assertEquals(radToDegString(data.nutationAngles.deltaLongitude),
                radToDegString(angles.deltaLongitude))
        assertEquals(radToDegString(data.nutationAngles.deltaObliquity),
                radToDegString(angles.deltaObliquity))
    }

    @ParameterizedTest
    @MethodSource("getIAU2006Data")
    fun `IAU2006`(data: Data) {
        val angles = getNutationAngles(ID_NUTATION_IAU_2006, data.jT)
        assertEquals(radToDegString(data.nutationAngles.deltaLongitude),
                radToDegString(angles.deltaLongitude))
        assertEquals(radToDegString(data.nutationAngles.deltaObliquity),
                radToDegString(angles.deltaObliquity))
    }

    internal data class Data(val jT: Double, val nutationAngles: NutationAngles) {
        override fun toString(): String {
            return "f($jT)=${radToDegString(nutationAngles.deltaLongitude)}, ${radToDegString(nutationAngles.deltaObliquity)}"
        }
    }

    fun getIAU1980Data() = listOf(
            Data(0.0, NutationAngles(-6.750247617532478E-5, -2.7992212383770136E-5)),
            Data(-.5, NutationAngles(-1.6025705416771623E-5, 4.031770174572796E-5)),
            Data(10.0, NutationAngles(6.179874108902156E-5, -3.46804461672148E-5))
    )
    fun getIAU2000Data() = listOf(
            Data(0.0, NutationAngles(-6.754422426417299E-5, -2.7970831192374143E-5)),
            Data(-.5, NutationAngles(-1.601424665756601E-5, 4.0351623057366754E-5)),
            Data(10.0, NutationAngles(6.180463412738758E-5, -3.467609027834701E-5))
    )
    fun getIAU2006Data() = listOf(
            Data(0.0, NutationAngles(-6.754422426417299E-5, -2.7970831192374143E-5)),
            Data(-.5, NutationAngles(-1.601424665756601E-5, 4.0351623057366754E-5)),
            Data(10.0, NutationAngles(6.180463412738758E-5, -3.4677053372078404E-5))
    )

    internal companion object {
        fun radToDegString(radian: Radian): String {
            val g = (RAD_TO_DEG * radian).toInt()
            val mm = (RAD_TO_DEG * radian - g) * 60.0
            val m = mm.toInt()
            val s = (mm - m) * 60.0
            return String.format(Locale.ENGLISH, "%1$.3f″",  Math.abs(s))
        }
    }
}