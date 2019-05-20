package net.arwix.astronomy2.core.ephemeris.event.sun

import net.arwix.astronomy2.core.ephemeris.event.EventEclipse

@Suppress("unused")
sealed class EventSolarEclipse(val timeOfMaximumEclipseMJD: Double) : EventEclipse {
    class Total(timeOfMaximumEclipseMJD: Double, val isCentral: Boolean) : EventSolarEclipse(timeOfMaximumEclipseMJD)
    class Annular(timeOfMaximumEclipseMJD: Double, val isCentral: Boolean) : EventSolarEclipse(timeOfMaximumEclipseMJD)
    class Hybrid(timeOfMaximumEclipseMJD: Double, val isCentral: Boolean) : EventSolarEclipse(timeOfMaximumEclipseMJD)
    class Partial(timeOfMaximumEclipseMJD: Double, val magnitude: Double) : EventSolarEclipse(timeOfMaximumEclipseMJD)
}