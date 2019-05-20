package net.arwix.astronomy2.core.ephemeris.event.moon

import net.arwix.astronomy2.core.ephemeris.event.EventEclipse

data class MoonEvent(val mJdET: Double, val phase: MoonPhase, val eclipse: EventEclipse? = null)