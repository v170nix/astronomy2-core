package net.arwix.astronomy2.core.ephemeris.precession

import javafx.scene.chart.Axis
import net.arwix.astronomy2.core.ARCSEC_TO_RAD
import net.arwix.astronomy2.core.Equatorial
import net.arwix.astronomy2.core.PI2
import net.arwix.astronomy2.core.vector.Matrix
import net.arwix.astronomy2.core.vector.Matrix.Companion.AXIS_X
import net.arwix.astronomy2.core.vector.Matrix.Companion.AXIS_Z
import net.arwix.astronomy2.core.vector.RectangularVector
import net.arwix.astronomy2.core.vector.Vector
import kotlin.math.cos
import kotlin.math.sin


@Equatorial
internal fun createVondrakMatrix(T: Double): Matrix {
    var w = PI2 * T
    val EPS0 = 84381.406 * ARCSEC_TO_RAD
    val (PSIA, OMEGAA, CHIA) = (0..13).fold(RectangularVector(0.0, 0.0, 0.0)) { acc, i ->
        var a = w / xyper[i][0]
        var s = sin(a)
        var c = cos(a)
        acc.x += c * xyper[i][1] + s * xyper[i][3]
        acc.y += c * xyper[i][2] + s * xyper[i][4]

        a = w / zper[i][0]
        s = sin(a)
        c = cos(a)
        acc.z += c * zper[i][1] + s * zper[i][2]
        return@fold acc
    }.let {
        w = 1.0
        for (j in 0..3) {
            it.x += xypol[0][j] * w
            it.y += xypol[1][j] * w
            it.z += xypol[2][j] * w
            w *= T
        }
        return@let it
    } * ARCSEC_TO_RAD

    // COMPUTE ELEMENTS OF PRECESSION ROTATION MATRIX
    // EQUIVALENT TO R3(CHI_A)R1(-OMEGA_A)R3(-PSI_A)R1(EPSILON_0)
    return Matrix(AXIS_Z, CHIA) * Matrix(AXIS_X, -OMEGAA) * Matrix(AXIS_Z, -PSIA) * Matrix(AXIS_X, EPS0)
}

private val xypol by lazy {
    arrayOf(
            doubleArrayOf(8473.343527, 5042.7980307, -0.00740913, 289E-9),
            doubleArrayOf(84283.175915, -0.4436568, 0.00000146, 151E-9),
            doubleArrayOf(-19.657270, 0.0790159, 0.00001472, -61E-9))
}

private val xyper by lazy {
    arrayOf(
            doubleArrayOf(402.90, -22206.325946, 1267.727824, -3243.236469, -8571.476251),
            doubleArrayOf(256.75, 12236.649447, 1702.324248, -3969.723769, 5309.796459),
            doubleArrayOf(292.00, -1589.008343, -2970.553839, 7099.207893, -610.393953),
            doubleArrayOf(537.22, 2482.103195, 693.790312, -1903.696711, 923.201931),
            doubleArrayOf(241.45, 150.322920, -14.724451, 146.435014, 3.759055),
            doubleArrayOf(375.22, -13.632066, -516.649401, 1300.630106, -40.691114),
            doubleArrayOf(157.87, 389.437420, -356.794454, 1727.498039, 80.437484),
            doubleArrayOf(274.20, 2031.433792, -129.552058, 299.854055, 807.300668),
            doubleArrayOf(203.00, 363.748303, 256.129314, -1217.125982, 83.712326),
            doubleArrayOf(440.00, -896.747562, 190.266114, -471.367487, -368.654854),
            doubleArrayOf(170.72, -926.995700, 95.103991, -441.682145, -191.881064),
            doubleArrayOf(713.37, 37.070667, -332.907067, -86.169171, -4.263770),
            doubleArrayOf(313.00, -597.682468, 131.337633, -308.320429, -270.353691),
            doubleArrayOf(128.38, 66.282812, 82.731919, -422.815629, 11.602861))
}

private val zper by lazy {
    arrayOf(
            doubleArrayOf(402.90, -13765.924050, -2206.967126),
            doubleArrayOf(256.75, 13511.858383, -4186.752711),
            doubleArrayOf(292.00, -1455.229106, 6737.949677),
            doubleArrayOf(537.22, 1054.394467, -856.922846),
            doubleArrayOf(375.22, -112.300144, 957.149088),
            doubleArrayOf(157.87, 202.769908, 1709.440735),
            doubleArrayOf(274.20, 1936.050095, 154.425505),
            doubleArrayOf(202.00, 327.517465, -1049.071786),
            doubleArrayOf(440.00, -655.484214, -243.520976),
            doubleArrayOf(170.72, -891.898637, -406.539008),
            doubleArrayOf(315.00, -494.780332, -301.504189),
            doubleArrayOf(136.32, 585.492621, 41.348740),
            doubleArrayOf(128.38, -333.322021, -446.656435),
            doubleArrayOf(490.00, 110.512834, 142.525186))
}