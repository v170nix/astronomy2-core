package net.arwix.astronomy2.core.ephemeris

import net.arwix.astronomy2.core.JT
import net.arwix.astronomy2.core.ephemeris.nutation.createNutationElements
import net.arwix.astronomy2.core.ephemeris.obliquity.createObliquityElements
import net.arwix.astronomy2.core.ephemeris.precession.*

class EclipticToEquatorialElements(idPrecession: IdPrecession, jt: JT) {

    val precessionElements = createPrecessionElements(idPrecession, jt)
    val obliquityElements = createObliquityElements(findNearestObliquityModel(precessionElements.id),
            if (precessionElements.isEcliptic) jt else 0.0)
    val nutationElements = createNutationElements(findNearestNutationModel(precessionElements.id),
            jt,
            obliquityElements.obliquity)

    val transformMatrix = if (precessionElements.isEcliptic) {
        nutationElements.equatorialMatrix!! * obliquityElements.eclipticToEquatorialMatrix * precessionElements.fromJ2000Matrix
    } else {
        nutationElements.equatorialMatrix!! * precessionElements.fromJ2000Matrix * obliquityElements.eclipticToEquatorialMatrix
    }
}