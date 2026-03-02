package com.example.simplefarm.farm

import com.kbridge.utils.LocationPoint
import com.kbridge.utils.location.OrenTown
import com.kbridge.utils.location.TownLocation

data object OrenClFirst : FarmZone {
    override val name: String get() = "OrenClFirst"
    override val town: TownLocation get() = OrenTown
    override val teleportPath: List<Int> get() = listOf(1, 9)
    override val spot: SpotPoint get() = SpotPoint(
        location = LocationPoint(81631, 3992, -2896),
        gpsPointName = "IT_CL_First"
    )
    override val buffDialogPath: List<Int> get() = listOf(1, 3, 3)
    override val zoneFile: String get() = "zone/OREN_CL_FIRST.zmap"
    override val configFile: String get() = "OREN_CL_SPOIL_CF.xml"
}

data object OrenClSecond : FarmZone {
    override val name: String get() = "OrenClSecond"
    override val town: TownLocation get() = OrenTown
    override val teleportPath: List<Int> get() = listOf(1, 9)
    override val spot: SpotPoint get() = SpotPoint(
        location = LocationPoint(77973, 6825, -3168),
        gpsPointName = "IT_CL_SECOND"
    )
    override val buffDialogPath: List<Int> get() = listOf(1, 3, 3)
    override val zoneFile: String get() = "zone/OREN_CL_SECOND.zmap"
    override val configFile: String get() = "OREN_CL_SPOIL_CF.xml"
}

data object OrenClThree : FarmZone {
    override val name: String get() = "OrenClThree"
    override val town: TownLocation get() = OrenTown
    override val teleportPath: List<Int> get() = listOf(1, 9)
    override val spot: SpotPoint get() = SpotPoint(
        location = LocationPoint(80147, 8344, -3504),
        gpsPointName = "IT_CL_THREE"
    )
    override val buffDialogPath: List<Int> get() = listOf(1, 3, 3)
    override val zoneFile: String get() = "zone/OREN_CL_THREE.zmap"
    override val configFile: String get() = "OREN_CL_SPOIL_CF.xml"
}

data object OrenClFour : FarmZone {
    override val name: String get() = "OrenClFour"
    override val town: TownLocation get() = OrenTown
    override val teleportPath: List<Int> get() = listOf(1, 9)
    override val spot: SpotPoint get() = SpotPoint(
        location = LocationPoint(75446, 12133, -3576),
        gpsPointName = "IT_CL_FOUR"
    )
    override val buffDialogPath: List<Int> get() = listOf(1, 3, 3)
    override val zoneFile: String get() = "zone/OREN_CL_FOUR.zmap"
    override val configFile: String get() = "OREN_CL_SPOIL_CF.xml"
}
data object Oren46to51One : FarmZone {
    override val name: String get() = "Oren46to51One"
    override val town: TownLocation get() = OrenTown
    override val teleportPath: List<Int> get() = listOf(1, 9)
    override val spot: SpotPoint get() = SpotPoint(
        location = LocationPoint(91161, 27880, -3640),
        gpsPointName = "IT_46_TO_51_FIRST"
    )
    override val buffDialogPath: List<Int> get() = listOf(1, 3, 3)
    override val zoneFile: String get() = "zone/IT_46_TO_51_FIRST.zmap"
    override val configFile: String get() = "IT_SPOIL_46_TO_51.xml"
}

data object Oren46to51Second : FarmZone {
    override val name: String get() = "Oren46to51Second"
    override val town: TownLocation get() = OrenTown
    override val teleportPath: List<Int> get() = listOf(1, 9)
    override val spot: SpotPoint get() = SpotPoint(
        location = LocationPoint(91161, 27880, -3640),
        gpsPointName = "IT_46_TO_51_SECOND"
    )
    override val buffDialogPath: List<Int> get() = listOf(1, 3, 3)
    override val zoneFile: String get() = "zone/IT_46_TO_51_SECOND.zmap"
    override val configFile: String get() = "IT_SPOIL_46_TO_51.xml"
}

data object Oren46to51Three : FarmZone {
    override val name: String get() = "Oren46to51Three"
    override val town: TownLocation get() = OrenTown
    override val teleportPath: List<Int> get() = listOf(1, 9)
    override val spot: SpotPoint get() = SpotPoint(
        location = LocationPoint(82533, 25242, -3520),
        gpsPointName = "IT_46_TO_51_THREE"
    )
    override val buffDialogPath: List<Int> get() = listOf(1, 3, 3)
    override val zoneFile: String get() = "zone/IT_46_TO_51_THREE.zmap"
    override val configFile: String get() = "IT_SPOIL_46_TO_51.xml"
}
data object Oren46to51Four : FarmZone {
    override val name: String get() = "Oren46to51Three"
    override val town: TownLocation get() = OrenTown
    override val teleportPath: List<Int> get() = listOf(1, 9)
    override val spot: SpotPoint get() = SpotPoint(
        location = LocationPoint(82533, 25242, -3520),
        gpsPointName = "IT_46_TO_51_FOUR"
    )

    override val buffDialogPath: List<Int> get() = listOf(1, 3, 3)
    override val zoneFile: String get() = "zone/IT_46_TO_51_FOUR.zmap"
    override val configFile: String get() = "IT_SPOIL_46_TO_51.xml"
}


