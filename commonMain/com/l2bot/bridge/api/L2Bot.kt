package com.l2bot.bridge.api

import com.l2bot.bridge.models.rpc.L2RpcException
import kotlinx.coroutines.flow.SharedFlow

interface L2Bot : L2Control {

    val charName: String

    val errors: SharedFlow<L2RpcException>

    val gps: L2Gps

    suspend fun connect()

    suspend fun disconnect()

    fun launch(block: suspend L2Bot.() -> Unit)
}
