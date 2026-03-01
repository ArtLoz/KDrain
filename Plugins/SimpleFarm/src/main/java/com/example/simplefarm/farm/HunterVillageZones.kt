package com.example.simplefarm.farm

import com.kbridge.utils.LocationPoint
import com.kbridge.utils.location.HunterVillage
import com.kbridge.utils.location.TownLocation

data object HvZone40to46 : FarmZone {
    override val name: String
        get() = "HvZone40to46"
    override val town: TownLocation
        get() = HunterVillage
    override val teleportPath: List<Int>
        get() = emptyList()
    override val spot: SpotPoint
        get() = SpotPoint(
            location = LocationPoint(125430, 82538, -2312),
            gpsPointName = "SPOT_HV_40_FIRST"
        )
    override val buffDialogPath: List<Int> get() = listOf(1, 3, 3)
    override val zoneFile: String
        get() = "zone/HV_40_TO_46_FIRST.zmap"
}
