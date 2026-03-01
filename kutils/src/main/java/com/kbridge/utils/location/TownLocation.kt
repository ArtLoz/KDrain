package com.kbridge.utils.location

import com.kbridge.utils.LocationPoint

sealed interface TownLocation {
    val centerPoint: TownCenterPoint
    val npcGatekeeper: NpcInfo
    val npcGrocer: NpcInfo?
    val npcNewbie: NpcInfo
    val npcServer: NpcInfo?
    val npcServerBuffer: NpcInfo?

}

data object KamaelVillage : TownLocation {
    override val centerPoint: TownCenterPoint
        get() = TownCenterPoint(location = LocationPoint(-118088, 46024, 376), range = 5000)
    override val npcGatekeeper: NpcInfo
        get() = NpcInfo(
            id = 32163,
            location = LocationPoint(-116879, 46591, 360),
            gpsPointName = "NPC_KM_Tp"
        )
    override val npcGrocer: NpcInfo
        get() = NpcInfo(
            id = 32167,
            location = LocationPoint(-117153, 48075, 456),
            gpsPointName = "NPC_KM_Nika"
        )
    override val npcNewbie: NpcInfo
        get() = NpcInfo(
            id = 32135,
            location = LocationPoint(-119692, 44504, 360),
            gpsPointName = "NPC_KM_Newbie"
        )
    override val npcServer: NpcInfo
        get() = NpcInfo(
            id = 826,
            location = LocationPoint(-116804, 46573, 360),
            gpsPointName = "NPC_KM_Server_Npc"
        )
    override val npcServerBuffer: NpcInfo
        get() = NpcInfo(
            id = 624,
            location = LocationPoint(-116987, 46597, 360),
            gpsPointName = "NPC_KM_Server_Buffer"
        )
    val npcBenis: NpcInfo
        get() = NpcInfo(
            id = 32170,
            location = LocationPoint(-114975, 44658, 512),
            gpsPointName = "NPC_KM_Benis"
        )
    val npcMarkela: NpcInfo
        get() = NpcInfo(
            id = 32173,
            location = LocationPoint(-119378, 49242, 8),
            gpsPointName = "NPC_KM_Markela"
        )
}

data object ElvenVillage : TownLocation {
    override val centerPoint: TownCenterPoint
        get() = TownCenterPoint(location = LocationPoint(45508, 49552, -3064), range = 5000)
    override val npcGatekeeper: NpcInfo
        get() = NpcInfo(
            id = 30146,
            location = LocationPoint(46949, 51507, -2976),
            gpsPointName = "NPC_E_GK"
        )
    override val npcGrocer: NpcInfo
        get() = NpcInfo(
            id = 30151,
            location = LocationPoint(47868, 50167, -2983),
            gpsPointName = "NPC_E_Chad"
        )
    override val npcNewbie: NpcInfo
        get() = NpcInfo(
            id = 30599,
            location = LocationPoint(45475, 48359, -3060),
            gpsPointName = "NPC_E_Newbie"
        )
    override val npcServer: NpcInfo
        get() = NpcInfo(
            id = 826,
            location = LocationPoint(46853, 51521, -2976),
            gpsPointName = "NPC_E_Server_Npc"
        )
    override val npcServerBuffer: NpcInfo
        get() = NpcInfo(
            id = 624,
            location = LocationPoint(47014, 51517, -2976),
            gpsPointName = "NPC_E_Server_Buffer"
        )

}

data object TalkingIsland : TownLocation {
    override val centerPoint: TownCenterPoint
        get() = TownCenterPoint(location = LocationPoint(-84399, 242867, -3704), range = 5000)
    override val npcGatekeeper: NpcInfo
        get() = NpcInfo(
            id = 30006,
            location = LocationPoint(-84108, 244604, -3729),
            gpsPointName = "NPC_TI_GK"
        )
    override val npcGrocer: NpcInfo
        get() = NpcInfo(
            id = 30004,
            location = LocationPoint(-84204, 240403, -3717),
            gpsPointName = "NPC_TI_Katerina"
        )
    override val npcNewbie: NpcInfo
        get() = NpcInfo(
            id = 30598,
            location = LocationPoint(-84081, 243227, -3723),
            gpsPointName = "NPC_TI_Newbie"
        )
    override val npcServer: NpcInfo
        get() = NpcInfo(
            id = 826,
            location = LocationPoint(-84170, 244665, -3728),
            gpsPointName = "NPC_TI_Server_Npc"
        )
    override val npcServerBuffer: NpcInfo
        get() = NpcInfo(
            id = 624,
            location = LocationPoint(-84217, 244716, -3728),
            gpsPointName = "NPC_TI_Server_Buffer"
        )
    val npcElias: NpcInfo
        get() = NpcInfo(
            id = 30050,
            location = LocationPoint(-84057, 242832, -3729),
            gpsPointName = "NPC_TI_Elias"
        )
    val npcYohanes: NpcInfo
        get() = NpcInfo(
            id = 30032,
            location = LocationPoint(-84981, 244764, -3726),
            gpsPointName = "NPC_TI_Yohanes"
        )
}

data object OrcVillage : TownLocation {
    override val centerPoint: TownCenterPoint
        get() = TownCenterPoint(location = LocationPoint(-44803, -113699, -192), range = 5000)
    override val npcGatekeeper: NpcInfo
        get() = NpcInfo(
            id = 30576,
            location = LocationPoint(-45264, -112512, -235),
            gpsPointName = "NPC_ORC_GK"
        )
    override val npcGrocer: NpcInfo
        get() = NpcInfo(
            id = 30561,
            location = LocationPoint(-43950, -115457, -194),
            gpsPointName = "NPC_ORC_Papuma"
        )
    override val npcNewbie: NpcInfo
        get() = NpcInfo(
            id = 30602,
            location = LocationPoint(-45032, -113598, -192),
            gpsPointName = "NPC_ORC_Newbie"
        )
    override val npcServer: NpcInfo
        get() = NpcInfo(
            id = 826,
            location = LocationPoint(-45271, -112596, -240),
            gpsPointName = "NPC_ORC_Server_Npc"
        )
    override val npcServerBuffer: NpcInfo
        get() = NpcInfo(
            id = 624,
            location = LocationPoint(-45267, -112413, -240),
            gpsPointName = "NPC_ORC_Server_Buffer"
        )
    val npcLivina: NpcInfo
        get() = NpcInfo(
            id = 30572,
            location = LocationPoint(-45864, -112540, -200),
            gpsPointName = "NPC_Livina"
        )
}

data object SchuttgartTown : TownLocation {
    override val centerPoint: TownCenterPoint
        get() = TownCenterPoint(location = LocationPoint(87377, -142212, -1336), range = 5000)
    override val npcGatekeeper: NpcInfo
        get() = NpcInfo(
            id = 31964,
            location = LocationPoint(87056, -143460, -1288),
            gpsPointName = "NPC_SH_GK"
        )
    override val npcGrocer: NpcInfo
        get() = NpcInfo(
            id = 31952,
            location = LocationPoint(85744, -141360, -1528),
            gpsPointName = "NPC_SH_Pole"
        )
    override val npcNewbie: NpcInfo
        get() = NpcInfo(
            id = 32327,
            location = LocationPoint(87152, -141328, -1336),
            gpsPointName = "NPC_SH_Newbie"
        )
    override val npcServer: NpcInfo
        get() = NpcInfo(
            id = 826,
            location = LocationPoint(87000, -143372, -1288),
            gpsPointName = "NPC_ORC_Server_Npc"
        )
    override val npcServerBuffer: NpcInfo
        get() = NpcInfo(
            id = 624,
            location = LocationPoint(86999, -143306, -1288),
            gpsPointName = "NPC_ORC_Server_Buffer"
        )
    val npcMoira: NpcInfo
        get() = NpcInfo(
            id = 31979,
            location = LocationPoint(89988, -143176, -1520),
            gpsPointName = "NPC_SH_Moira"
        )

}

data object GludinVillage : TownLocation {
    override val centerPoint: TownCenterPoint
        get() = TownCenterPoint(location = LocationPoint(-82969, 150846, -3120), range = 5000)
    override val npcGatekeeper: NpcInfo
        get() = NpcInfo(
            id = 30320,
            location = LocationPoint(-80778, 149757, -3040),
            gpsPointName = "NPC_GV_GK"
        )
    override val npcGrocer: NpcInfo
        get() = NpcInfo(
            id = 30315,
            location = LocationPoint(-79405, 153963, -3152),
            gpsPointName = "NPC_GV_Poesia"
        )
    override val npcNewbie: NpcInfo
        get() = NpcInfo(
            id = 31076,
            location = LocationPoint(-83123, 150868, -3128),
            gpsPointName = "NPC_GV_Newbie"
        )
    override val npcServer: NpcInfo
        get() = NpcInfo(
            id = 826,
            location = LocationPoint(-80686, 149839, -3040),
            gpsPointName = "NPC_GV_Server_Npc"
        )
    override val npcServerBuffer: NpcInfo
        get() = NpcInfo(
            id = 624,
            location = LocationPoint(86999, -143306, -1288),
            gpsPointName = "NPC_ORC_Server_Buffer"
        )
    val npcAllana: NpcInfo
        get() = NpcInfo(
            id = 30424,
            location = LocationPoint(-92310, 154223, -3284),
            gpsPointName = "NPC_GV_Allana"
        )
    val npcPerrin: NpcInfo
        get() = NpcInfo(
            id = 30428,
            location = LocationPoint(-93275, 147654, -2674),
            gpsPointName = "NPC_GV_Perrin"
        )
}

data object GludioVillage : TownLocation {
    override val centerPoint: TownCenterPoint
        get() = TownCenterPoint(location = LocationPoint(-14476, 123160, -3128), range = 5000)
    override val npcGatekeeper: NpcInfo
        get() = NpcInfo(
            id = 30256,
            location = LocationPoint(-14526, 124049, -3112),
            gpsPointName = "NPC_G_GK"
        )
    override val npcGrocer: NpcInfo
        get() = NpcInfo(
            id = 30254,
            location = LocationPoint(-14934, 124468, -3114),
            gpsPointName = "NPC_G_Harmony"
        )
    override val npcNewbie: NpcInfo
        get() = NpcInfo(
            id = 31077,
            location = LocationPoint(-13920, 121977, -2984),
            gpsPointName = "NPC_G_Newbie"
        )
    override val npcServer: NpcInfo
        get() = NpcInfo(
            id = 826,
            location = LocationPoint(-14598, 124009, -3112),
            gpsPointName = "NPC_G_Server_Npc"
        )
    override val npcServerBuffer: NpcInfo
        get() = NpcInfo(
            id = 624,
            location = LocationPoint(-14662, 124012, -3112),
            gpsPointName = "NPC_G_Server_buffer"
        )
    val npcManuel: NpcInfo
        get() = NpcInfo(
            id = 30293,
            location = LocationPoint(-13487, 121541, -2968),
            gpsPointName = "NPC_G_Manuel"
        )
}