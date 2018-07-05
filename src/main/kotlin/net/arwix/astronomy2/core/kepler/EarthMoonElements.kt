/*
 * Copyright 2017 Vitaliy Sheyanov vit.onix@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.arwix.astronomy2.core.kepler

import net.arwix.astronomy2.core.ARCSEC_TO_RAD
import net.arwix.astronomy2.core.Ecliptic
import net.arwix.astronomy2.core.JT
import net.arwix.astronomy2.core.Radian
import net.arwix.astronomy2.core.math.polynomialSum



/**
 * Mean elongation of moon = elongation
 */
fun getMeanElongationOfMoon(jT: JT): Radian = ARCSEC_TO_RAD *
        meanElongationOfMoonData[0].polynomialSum(jT) + meanElongationOfMoonData[1].polynomialSum(jT) * jT * jT

/**
 * Mean distance of moon from its ascending node = ascendingNode
 */
fun getAscendingNode(jT: JT): Radian = ARCSEC_TO_RAD *
        ascendingNodeData[0].polynomialSum(jT) + ascendingNodeData[1].polynomialSum(jT) * jT * jT

/**
 * Mean anomaly of sun = l' (J. Laskar)
 */
fun getMeanAnomalyOfSun(jT: JT): Radian = ARCSEC_TO_RAD *
        meanSunAnomalyData[0].polynomialSum(jT) + meanSunAnomalyData[1].polynomialSum(jT) * jT * jT

/**
 * Mean anomaly of moon = l
 */
fun getMeanAnomalyOfMoon(jT: JT): Radian = ARCSEC_TO_RAD *
        moonAnomalyData[0].polynomialSum(jT) + moonAnomalyData[1].polynomialSum(jT) * jT * jT

/**
 * Mean longitude of moon, re mean ecliptic and equinox of date = L
 */
@Ecliptic
fun getLongitudeOfMoon(jT: JT): Radian = ARCSEC_TO_RAD *
        longitudeData[0].polynomialSum(jT) + longitudeData[1].polynomialSum(jT) * jT * jT

/**
 *  Lunar free librations
 *  74.7 years. Denoted W or LA
 */
fun getLunarFreeLibrations(jT: JT): Radian = ARCSEC_TO_RAD * ((-0.112 * jT + 1.73655499e6) * jT - 389552.81)

/**
 * 2.891725 years. Denoted LB
 */
fun getLB(jT: JT): Radian = ARCSEC_TO_RAD * (4.48175409e7 * jT + 806045.7)


/**
 * 24.2 years. Denoted P or LC
 */
fun getLC(jT: JT): Radian = ARCSEC_TO_RAD * (5.36486787e6 * jT - 391702.8)

/**
 * Precession of the equinox pA
 */
fun getPrecessionOfEquinox(jT: JT): Radian = ARCSEC_TO_RAD * precessionData.polynomialSum(jT)

/**
 * Usual node term re equinox of date, denoted NA
 */
fun getNA(jT: JT): Radian = getLongitudeOfMoon(jT) - getAscendingNode(jT)

/**
 * Fancy node term, denoted NB.
 * Capital Pi of ecliptic motion (Williams 1994)
 */
fun getNB(jT: JT): Radian {
    val x = (((-0.000004 * jT + 0.000026) * jT + 0.153382) * jT - 867.919986) * jT + 629543.967373
    return getNA(jT) + ARCSEC_TO_RAD * (3.24e5 - x) - getPrecessionOfEquinox(jT)
}


private val meanElongationOfMoonData by lazy {
    listOf(doubleArrayOf(1.0722612202445078e+06, 1.6029616009939659e+09),
            doubleArrayOf(-6.7352202374457519e+00, 6.9492746836058421e-03, -3.702060118571e-005, +2.560078201452e-009, 2.555243317839e-011, -3.207663637426e-013)
    )
}

private val ascendingNodeData by lazy {
    listOf(doubleArrayOf(3.3577951412884740e+05, 1.7395272628437717e+09),
            doubleArrayOf(-1.3117809789650071e+01, -7.5311878482337989e-04, -2.165750777942e-006, -2.790392351314e-009, 4.189032191814e-011, 4.474984866301e-013))
}

private val meanSunAnomalyData by lazy {
    listOf(doubleArrayOf(1.2871027407441526e+06, 1.2959658102304320e+08),
            doubleArrayOf(-5.5281306421783094e-01, 8.7473717367324703e-05, -1.1297037031e-5, -4.77258489e-8, 8.8555011e-11, 4.237343e-13, -3.83508e-15, -1.0390e-17, 1.62e-20))
}

private val moonAnomalyData by lazy {
    listOf(doubleArrayOf(4.8586817465825332e+05, 1.7179159228846793e+09),
            doubleArrayOf(3.1501359071894147e+01, 5.2099641302735818e-02, -2.536291235258e-004, -2.506365935364e-008, 3.452144225877e-011, -1.755312760154e-012))
}

private val longitudeData by lazy {
    listOf(doubleArrayOf(7.8593980921052420e+05, 1.7325643720442266e+09),
            doubleArrayOf(-5.6550460027471399e+00, 6.9017248528380490e-03, -6.073960534117e-005, -1.024222633731e-008, 2.235210987108e-010, 7.200592540556e-014))
}

private val precessionData by lazy {
    doubleArrayOf(0.0, 5028.791959, 1.105414, 0.000076, -0.0000235316, -1.8055e-8, 1.7451e-10, 1.3095e-12, 2.424e-15, -4.759e-17, -8.66e-20)
}