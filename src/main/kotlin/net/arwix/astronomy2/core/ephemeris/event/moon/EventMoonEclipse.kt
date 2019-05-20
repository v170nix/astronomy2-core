package net.arwix.astronomy2.core.ephemeris.event.moon

import net.arwix.astronomy2.core.ephemeris.event.EventEclipse

@Suppress("unused")
sealed class EventMoonEclipse(val timeOfMaximumEclipseMJD: Double) : EventEclipse {

    class Penumbral(timeOfMaximumEclipseMJD: Double,
                    val magnitude: Double,
                    val radius: Double,
                    val partialPhasePenumbraSemiDuration: Double
    ) : EventMoonEclipse(timeOfMaximumEclipseMJD)

    class Partial(timeOfMaximumEclipseMJD: Double,
                  val magnitude: Double,
                  val radiusPenumbral: Double,
                  val radiusUmbral: Double,
                  val partialPhasePenumbraSemiDuration: Double,
                  val partialPhaseSemiDuration: Double
    ) : EventMoonEclipse(timeOfMaximumEclipseMJD)

    class Total(timeOfMaximumEclipseMJD: Double,
                val magnitude: Double,
                val radiusPenumbral: Double,
                val radiusUmbral: Double,
                val partialPhasePenumbraSemiDuration: Double,
                val partialPhaseSemiDuration: Double,
                val totalPhaseSemiDuration: Double
    ) : EventMoonEclipse(timeOfMaximumEclipseMJD)

}