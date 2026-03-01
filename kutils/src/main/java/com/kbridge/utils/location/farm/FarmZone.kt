package com.kbridge.utils.location.farm

import com.kbridge.utils.location.TownLocation

sealed interface FarmZone {
    val name: String
    val town: TownLocation
    val teleportPath: List<Int>
    val spot: SpotPoint
    val buffDialogPath: List<Int>
    val petBuffDialogPath: List<Int>
        get() = emptyList()
    val zoneFile: String
    val configFile: String?
        get() = null
}
